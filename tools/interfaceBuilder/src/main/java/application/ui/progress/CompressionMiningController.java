package application.ui.progress;

import application.InterfaceBuilderApp;
import application.compress.GameService;
import application.compress.RandomCompressionMiner;
import application.compress.RuleSet;
import application.config.ConfigService;
import application.i18n.Messages;
import application.integration.FileService;
import application.projects.Project;
import application.projects.ProjectService;
import application.ui.settings.Updateable;
import com.ahli.galaxy.ModData;
import com.ahli.galaxy.game.def.abstracts.GameDef;
import com.ahli.mpq.MpqException;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRule;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRuleMask;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRuleSize;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;

public class CompressionMiningController implements Updateable {
	private static final Logger logger = LogManager.getLogger();
	
	@FXML
	private Button miningButton;
	@FXML
	private Label lastSizeLabel;
	@FXML
	private Label sizeToBeatLabel;
	@FXML
	private Label attemptCounterLabel;
	@FXML
	private TableView<MpqEditorCompressionRule> ruleSetTable;
	@FXML
	private TableColumn<MpqEditorCompressionRule, String> columnType;
	@FXML
	private TableColumn<MpqEditorCompressionRule, String> columnMaskSize;
	@FXML
	private TableColumn<MpqEditorCompressionRule, String> columnCompressionAlgo;
	@FXML
	private TableColumn<MpqEditorCompressionRule, String> columnSingleFile;
	@FXML
	private TableColumn<MpqEditorCompressionRule, String> columnCompress;
	@FXML
	private TableColumn<MpqEditorCompressionRule, String> columnEncrypt;
	@FXML
	private TableColumn<MpqEditorCompressionRule, String> columnEncryptAdjusted;
	@FXML
	private TableColumn<MpqEditorCompressionRule, String> columnIncludeSectorChecksum;
	@FXML
	private TableColumn<MpqEditorCompressionRule, String> columnMarkedForDeletion;
	
	@Autowired
	private GameService gameService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ConfigService configService;
	@Autowired
	private FileService fileService;
	
	private Project project;
	private ObservableList<MpqEditorCompressionRule> ruleSetObservableItems;
	private Runnable task;
	private RandomCompressionMiner expCompMiner = null;
	private long bestSize = Long.MAX_VALUE;
	
	/**
	 * Automatically called by FxmlLoader
	 */
	public void initialize() {
		columnCompress.setCellValueFactory(
				cellData -> new SimpleBooleanProperty(cellData.getValue().isCompress()).asString());
		columnEncrypt
				.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isEncrypt()).asString());
		columnEncryptAdjusted.setCellValueFactory(
				cellData -> new SimpleBooleanProperty(cellData.getValue().isEncryptAdjusted()).asString());
		columnIncludeSectorChecksum.setCellValueFactory(
				cellData -> new SimpleBooleanProperty(cellData.getValue().isIncludeSectorChecksum()).asString());
		columnMarkedForDeletion.setCellValueFactory(
				cellData -> new SimpleBooleanProperty(cellData.getValue().isMarkedForDeletion()).asString());
		columnSingleFile.setCellValueFactory(
				cellData -> new SimpleBooleanProperty(cellData.getValue().isSingleUnit()).asString());
		columnMaskSize.setCellValueFactory(cellData -> {
			final MpqEditorCompressionRule rule = cellData.getValue();
			if (rule instanceof MpqEditorCompressionRuleMask) {
				return new SimpleStringProperty(((MpqEditorCompressionRuleMask) rule).getMask());
			} else if (rule instanceof MpqEditorCompressionRuleSize) {
				return new SimpleStringProperty(((MpqEditorCompressionRuleSize) rule).getMinSize() + " - " +
						((MpqEditorCompressionRuleSize) rule).getMaxSize());
			} else {
				return new SimpleStringProperty("");
			}
		});
		columnCompressionAlgo.setCellValueFactory(
				cellData -> new SimpleStringProperty(cellData.getValue().getCompressionMethod().toString()));
		columnType.setCellValueFactory(cellData -> {
			final MpqEditorCompressionRule rule = cellData.getValue();
			if (rule instanceof MpqEditorCompressionRuleMask) {
				return new SimpleStringProperty("Mask");
			} else if (rule instanceof MpqEditorCompressionRuleSize) {
				return new SimpleStringProperty("Size");
			} else {
				return new SimpleStringProperty("Default");
			}
		});
		attemptCounterLabel.setText("0");
		sizeToBeatLabel.setText("- kb");
		lastSizeLabel.setText("- kb");
	}
	
	/**
	 * Inject a project whose ruleset is mined.
	 *
	 * @param project
	 */
	public void setProject(final Project project) {
		this.project = project;
		showProjectsRuleSet(project);
	}
	
	/**
	 * Shows the compression ruleset of a specified project.
	 *
	 * @param project
	 */
	public void showProjectsRuleSet(final Project project) {
		ruleSetObservableItems = FXCollections.observableArrayList();
		final RuleSet bestCompressionRuleSet = projectService.fetchBestCompressionRuleSet(project);
		if (bestCompressionRuleSet != null) {
			final MpqEditorCompressionRule[] rules = bestCompressionRuleSet.getCompressionRules();
			if (rules != null) {
				ruleSetObservableItems.setAll(rules);
			}
		}
		ruleSetTable.setItems(ruleSetObservableItems);
		ruleSetTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}
	
	@Override
	public void update() {
		// nothing to do?
	}
	
	@FXML
	public void toggleMiningTask() {
		if (task == null) {
			startMining();
		} else {
			stopMining();
		}
	}
	
	public void startMining() {
		if (task != null) {
			return;
		}
		task = new Runnable() {
			@Override
			public void run() {
				try {
					// TODO too complex?
					{
						final ModData mod = gameService.getModData(project.getGame());
						final GameDef gameDef = mod.getGameData().getGameDef();
						final File projectSource = new File(project.getProjectPath());
						final File f = new File(configService.getDocumentsPath() + File.separator +
								gameDef.getDocumentsGameDirectoryName() + File.separator +
								gameDef.getDocumentsInterfaceSubdirectoryName() + File.separator +
								projectSource.getName());
						mod.setTargetFile(f);
						mod.setSourceDirectory(projectSource);
						final RuleSet bestCompressionRuleSet = projectService.fetchBestCompressionRuleSet(project);
						final MpqEditorCompressionRule[] prevBestCompressionRules =
								(bestCompressionRuleSet != null) ? bestCompressionRuleSet.getCompressionRules() : null;
						expCompMiner = new RandomCompressionMiner(mod, configService.getMpqCachePath(),
								configService.getMpqEditorPath(), prevBestCompressionRules, fileService);
						bestSize = expCompMiner.getBestSize();
						/* save initial as best, if there was no best before
						initial one might have been altered by the miner */
						if (bestCompressionRuleSet == null ||
								!new RuleSet(expCompMiner.getBestRuleSet()).equals(bestCompressionRuleSet)) {
							project.setBestCompressionRuleSet(new RuleSet(expCompMiner.getBestRuleSet()));
							projectService.saveProject(project);
						}
					}
					logger.info(String.format("Best size before mining: %s kb)", bestSize / 1024));
					updateUiRules(expCompMiner.getBestRuleSet());
					updateUiSizeToBeat(bestSize);
					long lastSize;
					final RandomCompressionMiner comprMiner = expCompMiner;
					for (int attempts = 1; attempts < Integer.MAX_VALUE; attempts++) {
						comprMiner.randomizeRules();
						lastSize = comprMiner.build();
						updateUiAttemptSize(lastSize, attempts);
						if (lastSize < bestSize) {
							bestSize = lastSize;
							logger.info(String.format("Mined better compression of size %s kb.", lastSize / 1024));
							project.setBestCompressionRuleSet(new RuleSet(comprMiner.getBestRuleSet()));
							projectService.saveProject(project);
							updateUiSizeToBeat(lastSize);
							updateUiRules(comprMiner.getBestRuleSet());
						} else {
							logger.info(String.format("Mined compression of size %s kb.", lastSize / 1024));
						}
						Thread.sleep(50);
						if (Thread.currentThread().isInterrupted() || task == null) {
							logger.info("Stopping the mining task.");
							expCompMiner.cleanUp();
							expCompMiner = null;
							
							// save best ruleset if better than previously saved best
							final RuleSet bestCompressionRuleSet = new RuleSet(comprMiner.getBestRuleSet());
							if (!project.getBestCompressionRuleSet().equals(bestCompressionRuleSet)) {
								project.setBestCompressionRuleSet(bestCompressionRuleSet);
								projectService.saveProject(project);
							}
							return;
						}
					}
				} catch (final IOException | MpqException e) {
					logger.error("Experimental Compression Miner experienced a problem.", e);
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		};
		attemptCounterLabel.setText("0");
		InterfaceBuilderApp.getInstance().getExecutor().execute(task);
		miningButton.setText(Messages.getString("progress.compressionMining.stopMining"));
	}
	
	public void stopMining() {
		if (task == null) {
			return;
		}
		InterfaceBuilderApp.getInstance().getExecutor().remove(task);
		task = null;
		if (expCompMiner != null) {
			final long newBest = expCompMiner.getBestSize();
			project.setBestCompressionRuleSet(new RuleSet(expCompMiner.getBestRuleSet()));
			expCompMiner = null;
			logger.info(String.format("Best Compression mined has compression producing size: %s", newBest / 1024));
		}
		miningButton.setText(Messages.getString("progress.compressionMining.startMining"));
	}
	
	/**
	 * Updates the UI with the currently tested ruleset.
	 *
	 * @param rules
	 */
	private void updateUiRules(final MpqEditorCompressionRule[] rules) {
		Platform.runLater(() -> {
			ruleSetObservableItems.setAll(rules);
			ruleSetTable.setItems(ruleSetObservableItems);
		});
	}
	
	/**
	 * Updates the UI with the size that the miner needs to beat.
	 *
	 * @param bestSize
	 */
	private void updateUiSizeToBeat(final long bestSize) {
		Platform.runLater(() -> sizeToBeatLabel.setText(bestSize / 1024 + " kb"));
	}
	
	/**
	 * Updates the UI with the mined size and mining attempt counter information.
	 *
	 * @param lastSize
	 * @param attempts
	 */
	private void updateUiAttemptSize(final long lastSize, final int attempts) {
		Platform.runLater(() -> {
			lastSizeLabel.setText(lastSize / 1024 + " kb");
			attemptCounterLabel.setText(String.valueOf(attempts));
		});
	}
}
