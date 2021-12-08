// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.ui.home;

import com.ahli.interfacebuilder.compress.RuleSet;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRule;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRuleMask;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRuleSize;
import com.ahli.interfacebuilder.projects.Project;
import com.ahli.interfacebuilder.projects.ProjectService;
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

import java.io.IOException;

public class ViewRuleSetController {
	private final ProjectService projectService;
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
	
	public ViewRuleSetController(final ProjectService projectService) {
		this.projectService = projectService;
	}
	
	/**
	 * Automatically called by FxmlLoader
	 */
	public void initialize() {
		final DialogPane dialogPane = dialog.getDialogPane();
		dialogPane.getButtonTypes().addAll(ButtonType.OK);
		
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
			if (rule instanceof final MpqEditorCompressionRuleMask ruleMask) {
				return new SimpleStringProperty(ruleMask.getMask());
			} else if (rule instanceof final MpqEditorCompressionRuleSize ruleSize) {
				return new SimpleStringProperty(ruleSize.getMinSize() + " - " + ruleSize.getMaxSize());
			} else {
				return new SimpleStringProperty("");
			}
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
	}
	
	/**
	 * Inject a project whose best compression ruleset is shown.
	 *
	 * @param project
	 */
	public void setProject(final Project project) throws IOException {
		dialog.setTitle(String.format("Best Compression Ruleset for %s", project.getName()));
		showProjectsRuleSet(project);
	}
	
	/**
	 * Shows the compression ruleset of a specified project.
	 *
	 * @param project
	 */
	public void showProjectsRuleSet(final Project project) throws IOException {
		final ObservableList<MpqEditorCompressionRule> ruleSetObservableItems = FXCollections.observableArrayList();
		final RuleSet bestCompressionRuleSet = projectService.fetchBestCompressionRuleSet(project);
		if (bestCompressionRuleSet != null) {
			ruleSetObservableItems.setAll(bestCompressionRuleSet.getCompressionRules());
		}
		ruleSetTable.setItems(ruleSetObservableItems);
		ruleSetTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		// columns are properly resized for content, if no prefWidth was defined!
	}
}
