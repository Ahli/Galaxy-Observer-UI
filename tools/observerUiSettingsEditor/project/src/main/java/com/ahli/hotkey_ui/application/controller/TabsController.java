// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.hotkey_ui.application.controller;

import com.ahli.hotkey_ui.application.SettingsEditorApplication;
import com.ahli.hotkey_ui.application.i18n.Messages;
import com.ahli.hotkey_ui.application.model.ValueDef;
import com.ahli.hotkey_ui.application.ui.ResetDefaultButtonTableCell;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Ahli
 */
public class TabsController {
	private static final Logger logger = LogManager.getLogger(TabsController.class);
	private static final Callback<CellDataFeatures<ValueDef, Boolean>, ObservableValue<Boolean>>
			ActionColumnCellValueFactory = new Callback<>() {
		@Override
		public ObservableValue<Boolean> call(final TableColumn.CellDataFeatures<ValueDef, Boolean> p) {
			return new SimpleBooleanProperty(p.getValue() != null);
		}
	};
	private static final Callback<TableColumn<ValueDef, Boolean>, TableCell<ValueDef, Boolean>>
			ActionColumnCellFactoryReset = new Callback<>() {
		@Override
		public TableCell<ValueDef, Boolean> call(final TableColumn<ValueDef, Boolean> p) {
			return new ResetDefaultButtonTableCell(Messages.getString("TabsController.Reset"));
		}
	};
	// based on:
	// http://jluger.de/blog/20160731_javafx_text_rendering_in_tableview.html
	private static final Callback<TableColumn<ValueDef, String>, TableCell<ValueDef, String>> WRAPPING_CELL_FACTORY =
			new Callback<>() {
				
				@Override
				public TableCell<ValueDef, String> call(final TableColumn<ValueDef, String> param) {
					return new TableCell<>() {
						@Override
						protected void updateItem(final String item, final boolean empty) {
							if (empty || item == null) {
								if (logger.isTraceEnabled()) {
									logger.trace("update wrapping table cell - null");
								}
								super.updateItem(item, empty);
								super.setGraphic(null);
								super.setText(null);
							} else {
								// check if old value equals new value
								final boolean equals = item.equals(getItem());
								super.updateItem(item, false);
								if (!equals) {
									if (logger.isTraceEnabled()) {
										logger.trace("update wrapping table cell - newLabel " + item);
									}
									final Label l = new Label(item);
									l.setWrapText(true);
									final VBox box = new VBox(l);
									l.heightProperty().addListener(new ChangeListener<>() {
										@Override
										public void changed(final ObservableValue<? extends Number> observable,
												final Number oldValue, final Number newValue) {
											box.setPrefHeight(newValue.doubleValue() + 7);
											Platform.runLater(() -> getTableRow().requestLayout());
										}
									});
									super.setGraphic(box);
								} else {
									if (logger.isTraceEnabled()) {
										logger.trace("update wrapping table cell - equal");
									}
								}
							}
						}
					};
				}
			};
	private final ObservableList<ValueDef> hotkeysData = FXCollections.observableArrayList();
	private final ObservableList<ValueDef> settingsData = FXCollections.observableArrayList();
	private SettingsEditorApplication main;
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
	
	public TabsController() {
		// nothing to do
	}
	
	/**
	 * On Controller initialization.
	 */
	@FXML
	public void initialize() {
		if (logger.isTraceEnabled()) {
			logger.trace("initializing");
		}
		
		hotkeysNameCol.setCellValueFactory(new PropertyValueFactory<>("id"));
		hotkeysDescriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
		hotkeysDescriptionCol.setCellFactory(WRAPPING_CELL_FACTORY);
		hotkeysDefaultCol.setCellValueFactory(new PropertyValueFactory<>("defaultValue"));
		hotkeysKeyCol.setCellValueFactory(new PropertyValueFactory<>("value"));
		hotkeysKeyCol.setCellFactory(TextFieldTableCell.forTableColumn());
		hotkeysKeyCol.setOnEditCommit(new EventHandler<>() {
			@Override
			public void handle(final CellEditEvent<ValueDef, String> t) {
				if (!t.getOldValue().equals(t.getNewValue())) {
					t.getTableView().getItems().get(t.getTablePosition().getRow()).setValue(t.getNewValue());
					if (logger.isTraceEnabled()) {
						logger.trace("write hotkey val: " + t.getNewValue());
					}
					getMain().notifyFileDataWasChanged();
				}
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
		settingsValueCol.setCellFactory(TextFieldTableCell.forTableColumn());
		settingsValueCol.setOnEditCommit(new EventHandler<>() {
			@Override
			public void handle(final CellEditEvent<ValueDef, String> t) {
				if (!t.getOldValue().equals(t.getNewValue())) {
					t.getTableView().getItems().get(t.getTablePosition().getRow()).setValue(t.getNewValue());
					if (logger.isTraceEnabled()) {
						logger.trace("write setting val: " + t.getNewValue());
					}
					getMain().notifyFileDataWasChanged();
				}
			}
		});
		settingsActionsCol.setSortable(false);
		settingsActionsCol.setCellValueFactory(ActionColumnCellValueFactory);
		settingsActionsCol.setCellFactory(ActionColumnCellFactoryReset);
		
		hotkeysTable.setItems(hotkeysData);
		settingsTable.setItems(settingsData);
	}
	
	/**
	 * @return the main
	 */
	public SettingsEditorApplication getMain() {
		return main;
	}
	
	/**
	 * @param main
	 */
	public void setMainApp(final SettingsEditorApplication main) {
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
