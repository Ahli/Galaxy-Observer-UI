// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.ui.progress;

import com.ahli.galaxy.ModD;
import com.ahli.interfacebuilder.compress.GameService;
import com.ahli.interfacebuilder.compress.RandomCompressionMiner;
import com.ahli.interfacebuilder.compress.RuleSet;
import com.ahli.interfacebuilder.config.ConfigService;
import com.ahli.interfacebuilder.i18n.Messages;
import com.ahli.interfacebuilder.integration.FileService;
import com.ahli.interfacebuilder.projects.Project;
import com.ahli.interfacebuilder.projects.ProjectService;
import com.ahli.interfacebuilder.ui.FxmlController;
import com.ahli.interfacebuilder.ui.Updateable;
import com.ahli.mpq.MpqException;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRule;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRuleMask;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRuleSize;
import com.ahli.mpq.mpqeditor.MpqEditorInterface;
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
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ForkJoinPool;

import static com.ahli.interfacebuilder.ui.AppController.FATAL_ERROR;

@Log4j2
public class CompressionMiningController implements Updateable, FxmlController {
	private final GameService gameService;
	private final ProjectService projectService;
	private final ConfigService configService;
	private final FileService fileService;
	private final ForkJoinPool executor;
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
	private Project project;
	private ObservableList<MpqEditorCompressionRule> ruleSetObservableItems;
	private Runnable task;
	private boolean keepTaskRunning;
	private RandomCompressionMiner expCompMiner;
	
	public CompressionMiningController(
			final GameService gameService,
			final ProjectService projectService,
			final ConfigService configService,
			final FileService fileService,
			final ForkJoinPool executor) {
		this.gameService = gameService;
		this.projectService = projectService;
		this.configService = configService;
		this.fileService = fileService;
		this.executor = executor;
		keepTaskRunning = true;
	}
	
	/**
	 * Automatically called by FxmlLoader
	 */
	@Override
	public void initialize() {
		columnCompress.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue()
				.isCompress()).asString());
		columnEncrypt.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue()
				.isEncrypt()).asString());
		columnEncryptAdjusted.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue()
				.isEncryptAdjusted()).asString());
		columnIncludeSectorChecksum.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue()
				.isIncludeSectorChecksum()).asString());
		columnMarkedForDeletion.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue()
				.isMarkedForDeletion()).asString());
		columnSingleFile.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue()
				.isSingleUnit()).asString());
		columnMaskSize.setCellValueFactory(cellData -> {
			final MpqEditorCompressionRule rule = cellData.getValue();
			return switch (rule) {
				case MpqEditorCompressionRuleMask ruleMask -> new SimpleStringProperty(ruleMask.getMask());
				case MpqEditorCompressionRuleSize ruleSize ->
						new SimpleStringProperty(ruleSize.getMinSize() + " - " + ruleSize.getMaxSize());
				default -> new SimpleStringProperty("");
			};
		});
		columnCompressionAlgo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()
				.getCompressionMethod()
				.toString()));
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
	public void setProject(final Project project) throws IOException {
		this.project = project;
		showProjectsRuleSet(project);
	}
	
	/**
	 * Shows the compression ruleset of a specified project.
	 *
	 * @param project
	 */
	public void showProjectsRuleSet(final Project project) throws IOException {
		ruleSetObservableItems = FXCollections.observableArrayList();
		final RuleSet bestCompressionRuleSet = projectService.fetchBestCompressionRuleSet(project);
		if (bestCompressionRuleSet != null) {
			ruleSetObservableItems.setAll(bestCompressionRuleSet.getCompressionRules());
		}
		ruleSetTable.setItems(ruleSetObservableItems);
		ruleSetTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		// columns are properly resized for content, if no prefWidth was defined!
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
	
	/**
	 *
	 */
	public void startMining() {
		if (task != null) {
			return;
		}
		keepTaskRunning = true;
		task = () -> {
			try {
				long bestSize;
				final ModD mod = gameService.getModData(project.getGameType());
				bestSize = findBetterCompressions(mod);
				log.info("Best size before mining: {} kb.", bestSize / 1024);
				updateUiRules(expCompMiner.getBestCompressionRules());
				updateUiSizeToBeat(bestSize);
				long lastSize;
				final RandomCompressionMiner comprMiner = expCompMiner;
				for (int attempts = 1; keepTaskRunning && attempts < Integer.MAX_VALUE; ++attempts) {
					comprMiner.randomizeRules();
					lastSize = comprMiner.build();
					updateUiAttemptSize(lastSize, attempts);
					updateUiRules(comprMiner.getCurrentRules());
					if (lastSize < bestSize) {
						if (validateTargetFile(mod, comprMiner.getMpqInterface())) {
							bestSize = lastSize;
							log.info("Mined better compression of size {} kb.", lastSize / 1024);
							//noinspection ObjectAllocationInLoop
							project.setBestCompressionRuleSet(new RuleSet(comprMiner.getBestCompressionRules()));
							projectService.saveProject(project);
							updateUiSizeToBeat(lastSize);
							updateUiRules(comprMiner.getBestCompressionRules());
						} else {
							log.warn("Invalid file encountered: {} kb.", lastSize / 1024);
						}
					}
					if (Thread.currentThread().isInterrupted() || task == null) {
						log.info("Stopping the mining task.");
						return;
					}
				}
			} catch (final IOException | MpqException e) {
				log.error("Experimental Compression Miner experienced a problem.", e);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				log.error("Interrupted while compression mining");
			} finally {
				final RandomCompressionMiner comprMiner = expCompMiner;
				if (comprMiner != null) {
					comprMiner.cleanUp();
				}
			}
		};
		attemptCounterLabel.setText("0");
		executor.execute(task);
		miningButton.setText(Messages.getString("progress.compressionMining.stopMining"));
	}
	
	/**
	 *
	 */
	public void stopMining() {
		if (task == null) {
			return;
		}
		keepTaskRunning = false;
		task = null;
		if (expCompMiner != null) {
			final long newBest = expCompMiner.getBestSize();
			expCompMiner = null;
			log.info("Currently best Compression produces archives of size: {} kb", newBest / 1024);
		}
		miningButton.setText(Messages.getString("progress.compressionMining.startMining"));
	}
	
	private long findBetterCompressions(final ModD mod) throws IOException, MpqException, InterruptedException {
		long bestSize;
		final Path modTargetFile;
		final Path projectSource = project.getProjectPath();
		
		modTargetFile = configService.getMiningTempPath().resolve(projectSource.getFileName());
		mod.setTargetFile(modTargetFile);
		mod.setSourceDirectory(projectSource);
		
		final RuleSet bestCompressionRuleSet = projectService.fetchBestCompressionRuleSet(project);
		final MpqEditorCompressionRule[] prevBestCompressionRules =
				(bestCompressionRuleSet != null) ? bestCompressionRuleSet.getCompressionRules() : null;
		
		expCompMiner = new RandomCompressionMiner(
				mod,
				configService.getMpqCachePath(),
				configService.getMpqEditorPath(),
				prevBestCompressionRules,
				fileService);
		bestSize = expCompMiner.getBestSize();
						/* save initial as best, if there was no best before
						initial one might have been altered by the miner */
		if (bestCompressionRuleSet == null || expCompMiner.isOldRulesetHadMissingFiles() ||
				!new RuleSet(expCompMiner.getBestCompressionRules()).equals(bestCompressionRuleSet)) {
			project.setBestCompressionRuleSet(new RuleSet(expCompMiner.getBestCompressionRules()));
			projectService.saveProject(project);
		}
		return bestSize;
	}
	
	/**
	 * Updates the UI with the currently tested ruleset.
	 *
	 * @param rules
	 */
	private void updateUiRules(final MpqEditorCompressionRule... rules) {
		Platform.runLater(() -> {
			try {
				ruleSetObservableItems.setAll(rules);
			} catch (final Exception e) {
				log.fatal(FATAL_ERROR, e);
			}
		});
	}
	
	/**
	 * Updates the UI with the size that the miner needs to beat.
	 *
	 * @param bestSize
	 */
	private void updateUiSizeToBeat(final long bestSize) {
		Platform.runLater(() -> {
			try {
				sizeToBeatLabel.setText(bestSize / 1024 + " kb");
			} catch (final Exception e) {
				log.fatal(FATAL_ERROR, e);
			}
		});
	}
	
	/**
	 * Updates the UI with the mined size and mining attempt counter information.
	 *
	 * @param lastSize
	 * @param attempts
	 */
	private void updateUiAttemptSize(final long lastSize, final int attempts) {
		Platform.runLater(() -> {
			try {
				lastSizeLabel.setText(lastSize / 1024 + " kb");
				attemptCounterLabel.setText(String.valueOf(attempts));
			} catch (final Exception e) {
				log.fatal(FATAL_ERROR, e);
			}
		});
	}
	
	/**
	 * @param mod
	 * @param mpqInterface
	 * @return
	 */
	private static boolean validateTargetFile(final ModD mod, final MpqEditorInterface mpqInterface) {
		final Path targetFile = mod.getTargetFile();
		if (targetFile == null) {
			return false;
		}
		// TODO validate target mpq containing all files
		
		return true;
	}
}
