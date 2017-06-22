package com.ahli.hotkeyUi.application.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ahli.hotkeyUi.application.Main;
import com.ahli.hotkeyUi.application.ResetDefaultButtonTableCell;
import com.ahli.hotkeyUi.application.i18n.Messages;
import com.ahli.hotkeyUi.application.model.ValueDef;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;

/**
 * 
 * @author Ahli
 *
 */
public class TabsController {
	static Logger LOGGER = LogManager.getLogger("TabsController");

	private Main main;

	private final ObservableList<ValueDef> hotkeysData = FXCollections.observableArrayList();

	private final ObservableList<ValueDef> settingsData = FXCollections.observableArrayList();

	@FXML
	private Tab hotkeysTab;
	@FXML
	private TableView<ValueDef> hotkeysTable;
	@FXML
	private TableColumn<ValueDef, String> hotkeysNameCol;
	@FXML
	private TableColumn<ValueDef, String> hotkeysDescriptionCol;
	@FXML
	private TableColumn<ValueDef, String> hotkeysDefaultCol;
	@FXML
	private TableColumn<ValueDef, String> hotkeysKeyCol;
	@FXML
	private TableColumn<ValueDef, Boolean> hotkeysActionsCol;

	@FXML
	private Tab settingsTab;
	@FXML
	private TableView<ValueDef> settingsTable;
	@FXML
	private TableColumn<ValueDef, String> settingsNameCol;
	@FXML
	private TableColumn<ValueDef, String> settingsDescriptionCol;
	@FXML
	private TableColumn<ValueDef, String> settingsDefaultCol;
	@FXML
	private TableColumn<ValueDef, String> settingsValueCol;
	@FXML
	private TableColumn<ValueDef, Boolean> settingsActionsCol;

	/**
	 * On Controller initialization.
	 */
	@FXML
	public void initialize() {
		LOGGER.trace("initializing");

		hotkeysNameCol.setCellValueFactory(new PropertyValueFactory<>("id"));
		hotkeysDescriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
		hotkeysDefaultCol.setCellValueFactory(new PropertyValueFactory<>("defaultValue"));
		hotkeysKeyCol.setCellValueFactory(new PropertyValueFactory<>("value"));
		hotkeysKeyCol.setCellFactory(TextFieldTableCell.<ValueDef>forTableColumn());
		hotkeysKeyCol.setOnEditCommit((CellEditEvent<ValueDef, String> t) -> {
			if (!t.getOldValue().equals(t.getNewValue())) {
				((ValueDef) t.getTableView().getItems().get(t.getTablePosition().getRow())).setValue(t.getNewValue());
				LOGGER.debug("write hotkey val: " + t.getNewValue());
				main.notifyFileDataWasChanged();
			}
		});
		hotkeysActionsCol.setSortable(false);
		hotkeysActionsCol.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ValueDef, Boolean>, ObservableValue<Boolean>>() {
					@Override
					public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<ValueDef, Boolean> p) {
						return new SimpleBooleanProperty(p.getValue() != null);
					}
				});
		hotkeysActionsCol.setCellFactory(new Callback<TableColumn<ValueDef, Boolean>, TableCell<ValueDef, Boolean>>() {
			@Override
			public TableCell<ValueDef, Boolean> call(TableColumn<ValueDef, Boolean> p) {
				return new ResetDefaultButtonTableCell(Messages.getString("TabsController.Reset")); //$NON-NLS-1$
			}
		});

		settingsNameCol.setCellValueFactory(new PropertyValueFactory<>("id"));
		settingsDescriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
		settingsDefaultCol.setCellValueFactory(new PropertyValueFactory<>("defaultValue"));
		settingsValueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
		settingsValueCol.setCellFactory(TextFieldTableCell.<ValueDef>forTableColumn());
		settingsValueCol.setOnEditCommit((CellEditEvent<ValueDef, String> t) -> {
			if (!t.getOldValue().equals(t.getNewValue())) {
				((ValueDef) t.getTableView().getItems().get(t.getTablePosition().getRow())).setValue(t.getNewValue());
				LOGGER.debug("write setting val: " + t.getNewValue());
				main.notifyFileDataWasChanged();
			}
		});
		settingsActionsCol.setSortable(false);
		settingsActionsCol.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ValueDef, Boolean>, ObservableValue<Boolean>>() {
					@Override
					public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<ValueDef, Boolean> p) {
						return new SimpleBooleanProperty(p.getValue() != null);
					}
				});
		settingsActionsCol.setCellFactory(new Callback<TableColumn<ValueDef, Boolean>, TableCell<ValueDef, Boolean>>() {
			@Override
			public TableCell<ValueDef, Boolean> call(TableColumn<ValueDef, Boolean> p) {
				return new ResetDefaultButtonTableCell(Messages.getString("TabsController.Reset")); //$NON-NLS-1$
			}
		});

		hotkeysTable.setItems(hotkeysData);
		settingsTable.setItems(settingsData);
	}

	/**
	 * 
	 * @param main
	 */
	public void setMainApp(Main main) {
		this.main = main;
	}

	/**
	 * @return the hotkeysData
	 */
	public ObservableList<ValueDef> getHotkeysData() {
		return hotkeysData;
	}

	/**
	 * @return the settingsData
	 */
	public ObservableList<ValueDef> getSettingsData() {
		return settingsData;
	}

	/**
	 * Clears the data.
	 */
	public void clearData() {
		hotkeysData.clear();
		settingsData.clear();
	}

}
