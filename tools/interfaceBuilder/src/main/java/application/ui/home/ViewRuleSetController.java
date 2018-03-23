package application.ui.home;

import application.compress.RuleSet;
import application.projects.Project;
import application.projects.ProjectService;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRule;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRuleMask;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRuleSize;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class ViewRuleSetController {
	private static final Logger logger = LogManager.getLogger();
	@FXML
	private TableColumn<MpqEditorCompressionRule, String> columnMarkedForDeletion;
	@FXML
	private TableColumn<MpqEditorCompressionRule, String> columnIncludeSectorChecksum;
	@FXML
	private TableColumn<MpqEditorCompressionRule, String> columnEncryptAdjusted;
	@FXML
	private TableColumn<MpqEditorCompressionRule, String> columnEncrypt;
	@FXML
	private TableColumn<MpqEditorCompressionRule, String> columnCompress;
	@FXML
	private TableColumn<MpqEditorCompressionRule, String> columnSingleFile;
	@FXML
	private TableColumn<MpqEditorCompressionRule, String> columnCompressionAlgo;
	@FXML
	private TableColumn<MpqEditorCompressionRule, String> columnMaskSize;
	@FXML
	private TableColumn<MpqEditorCompressionRule, String> columnType;
	
	@FXML
	private TableView<MpqEditorCompressionRule> ruleSetTable;
	@FXML
	private Dialog<Void> dialog;
	
	@Autowired
	private ProjectService projectService;
	
	private Project project;
	private ObservableList<MpqEditorCompressionRule> ruleSetObservableItems;
	
	/**
	 * Automatically called by FxmlLoader
	 */
	public void initialize() {
		dialog.setTitle("Add Observer Interface Project...");
		final DialogPane dialogPane = dialog.getDialogPane();
		dialogPane.getButtonTypes().addAll(ButtonType.OK);
		
		columnCompress.setCellValueFactory(cellData -> {
			return new SimpleBooleanProperty(cellData.getValue().isCompress()).asString();
		});
		columnEncrypt.setCellValueFactory(cellData -> {
			return new SimpleBooleanProperty(cellData.getValue().isEncrypt()).asString();
		});
		columnEncryptAdjusted.setCellValueFactory(cellData -> {
			return new SimpleBooleanProperty(cellData.getValue().isEncryptAdjusted()).asString();
		});
		columnIncludeSectorChecksum.setCellValueFactory(cellData -> {
			return new SimpleBooleanProperty(cellData.getValue().isIncludeSectorChecksum()).asString();
		});
		columnMarkedForDeletion.setCellValueFactory(cellData -> {
			return new SimpleBooleanProperty(cellData.getValue().isMarkedForDeletion()).asString();
		});
		columnSingleFile.setCellValueFactory(cellData -> {
			return new SimpleBooleanProperty(cellData.getValue().isSingleUnit()).asString();
		});
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
		columnCompressionAlgo.setCellValueFactory(cellData -> {
			return new SimpleStringProperty(cellData.getValue().getCompressionMethod().toString());
		});
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
	}
	
	/**
	 * Inject a project whose best compression ruleset is shown.
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
}
