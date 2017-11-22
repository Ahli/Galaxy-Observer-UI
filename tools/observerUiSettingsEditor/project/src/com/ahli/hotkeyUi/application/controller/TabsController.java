package com.ahli.hotkeyUi.application.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ahli.hotkeyUi.application.Main;
import com.ahli.hotkeyUi.application.i18n.Messages;
import com.ahli.hotkeyUi.application.model.ValueDef;
import com.ahli.hotkeyUi.application.ui.ResetDefaultButtonTableCell;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 * 
 * @author Ahli
 * 
 */
public class TabsController {
	static Logger LOGGER = LogManager.getLogger(TabsController.class);
	
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
	
	// based on:
	// http://jluger.de/blog/20160731_javafx_text_rendering_in_tableview.html
	private static final Callback<TableColumn<ValueDef, String>, TableCell<ValueDef, String>> WRAPPING_CELL_FACTORY = new Callback<>() {
		
		@Override
		public TableCell<ValueDef, String> call(final TableColumn<ValueDef, String> param) {
			final TableCell<ValueDef, String> tableCell = new TableCell<>() {
				@Override
				protected void updateItem(final String item, final boolean empty) {
					if (empty || item == null) {
						LOGGER.trace("update wrapping table cell - null");
						super.updateItem(item, empty);
						super.setGraphic(null);
						super.setText(null);
					} else {
						// check if old value equals new value
						final boolean equals = item.equals(getItem());
						super.updateItem(item, empty);
						if (!equals) {
							LOGGER.trace("update wrapping table cell - newLabel " + item);
							final Label l = new Label(item);
							l.setWrapText(true);
							final VBox box = new VBox(l);
							l.heightProperty().addListener((observable, oldValue, newValue) -> {
								box.setPrefHeight(newValue.doubleValue() + 7);
								Platform.runLater(() -> getTableRow().requestLayout());
							});
							super.setGraphic(box);
						} else {
							LOGGER.trace("update wrapping table cell - equal");
						}
					}
				}
			};
			return tableCell;
		}
	};
	
	private static final Callback<CellDataFeatures<ValueDef, Boolean>, ObservableValue<Boolean>> ActionColumnCellValueFactory = new Callback<>() {
		@Override
		public ObservableValue<Boolean> call(final TableColumn.CellDataFeatures<ValueDef, Boolean> p) {
			return new SimpleBooleanProperty(p.getValue() != null);
		}
	};
	
	private static final Callback<TableColumn<ValueDef, Boolean>, TableCell<ValueDef, Boolean>> ActionColumnCellFactoryReset = new Callback<>() {
		@Override
		public TableCell<ValueDef, Boolean> call(final TableColumn<ValueDef, Boolean> p) {
			return new ResetDefaultButtonTableCell(Messages.getString("TabsController.Reset")); //$NON-NLS-1$
		}
	};
	
	/**
	 * On Controller initialization.
	 */
	@FXML
	public void initialize() {
		LOGGER.trace("initializing");
		
		hotkeysNameCol.setCellValueFactory(new PropertyValueFactory<>("id"));
		hotkeysDescriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
		hotkeysDescriptionCol.setCellFactory(WRAPPING_CELL_FACTORY);
		hotkeysDefaultCol.setCellValueFactory(new PropertyValueFactory<>("defaultValue"));
		hotkeysKeyCol.setCellValueFactory(new PropertyValueFactory<>("value"));
		hotkeysKeyCol.setCellFactory(TextFieldTableCell.<ValueDef>forTableColumn());
		hotkeysKeyCol.setOnEditCommit((final CellEditEvent<ValueDef, String> t) -> {
			if (!t.getOldValue().equals(t.getNewValue())) {
				t.getTableView().getItems().get(t.getTablePosition().getRow()).setValue(t.getNewValue());
				LOGGER.debug("write hotkey val: " + t.getNewValue());
				main.notifyFileDataWasChanged();
			}
		});
		hotkeysActionsCol.setSortable(false);
		hotkeysActionsCol.setCellValueFactory(ActionColumnCellValueFactory);
		hotkeysActionsCol.setCellFactory(ActionColumnCellFactoryReset);
		
		settingsNameCol.setCellValueFactory(new PropertyValueFactory<>("id"));
		settingsDescriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
		settingsDescriptionCol.setCellFactory(WRAPPING_CELL_FACTORY);
		settingsDefaultCol.setCellValueFactory(new PropertyValueFactory<>("defaultValue"));
		settingsValueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
		settingsValueCol.setCellFactory(TextFieldTableCell.<ValueDef>forTableColumn());
		settingsValueCol.setOnEditCommit((final CellEditEvent<ValueDef, String> t) -> {
			if (!t.getOldValue().equals(t.getNewValue())) {
				t.getTableView().getItems().get(t.getTablePosition().getRow()).setValue(t.getNewValue());
				LOGGER.debug("write setting val: " + t.getNewValue());
				main.notifyFileDataWasChanged();
			}
		});
		settingsActionsCol.setSortable(false);
		settingsActionsCol.setCellValueFactory(ActionColumnCellValueFactory);
		settingsActionsCol.setCellFactory(ActionColumnCellFactoryReset);
		
		hotkeysTable.setItems(hotkeysData);
		settingsTable.setItems(settingsData);
	}
	
	/**
	 * 
	 * @param main
	 */
	public void setMainApp(final Main main) {
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
