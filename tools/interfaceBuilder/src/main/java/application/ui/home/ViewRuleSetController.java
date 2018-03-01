package application.ui.home;

import application.projects.Project;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ViewRuleSetController {
	private static final Logger logger = LogManager.getLogger();
	
	@FXML
	private ListView<String> ruleSetList;
	@FXML
	private Dialog<Void> dialog;
	
	private Project project;
	private ObservableList<String> rulesetStringsObservable;
	
	/**
	 * Automatically called by FxmlLoader
	 */
	public void initialize() {
		dialog.setTitle("Add Observer Interface Project...");
		final DialogPane dialogPane = dialog.getDialogPane();
		dialogPane.getButtonTypes().addAll(ButtonType.OK);
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
	private void showProjectsRuleSet(final Project project) {
		final String[] ruleSet = project.getBestCompressionRuleSet().getCompressionRulesString();
		if (ruleSet != null) {
			rulesetStringsObservable = FXCollections.observableArrayList(ruleSet);
		} else {
			rulesetStringsObservable = FXCollections.observableArrayList();
		}
		
		ruleSetList.setItems(rulesetStringsObservable);
		ruleSetList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		ruleSetList.setCellFactory(new Callback<>() {
			@Override
			public ListCell<String> call(final ListView<String> p) {
				return new ListCell<>() {
					@Override
					protected void updateItem(final String strVal, final boolean empty) {
						super.updateItem(strVal, empty);
						if (empty || strVal == null) {
							setText(null);
						} else {
							setText(strVal);
						}
					}
				};
			}
		});
	}
}
