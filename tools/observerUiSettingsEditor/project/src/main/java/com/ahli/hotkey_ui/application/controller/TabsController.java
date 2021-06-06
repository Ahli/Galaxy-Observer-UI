// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.hotkey_ui.application.controller;

import com.ahli.hotkey_ui.application.i18n.Messages;
import com.ahli.hotkey_ui.application.model.ValueDef;
import com.ahli.hotkey_ui.application.ui.DynamicValueDefEditingTableCell;
import com.ahli.hotkey_ui.application.ui.ResetDefaultButtonTableCell;
import com.ahli.hotkey_ui.application.ui.WrappingTextTableCell;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Ahli
 */
public class TabsController {
	private static final Logger logger = LogManager.getLogger(TabsController.class);
	
	private static final Callback<TableColumn<ValueDef, Boolean>, TableCell<ValueDef, Boolean>>
			ActionColumnCellFactoryReset =
			tableColumn -> new ResetDefaultButtonTableCell(Messages.getString("TabsController.ResetDefault"),
					Messages.getString("TabsController.ResetOldValue"));
	
	private static final Callback<TableColumn<ValueDef, String>, TableCell<ValueDef, String>> WRAPPING_CELL_FACTORY =
			tableColumn -> new WrappingTextTableCell();
	
	private static final Callback<TableColumn<ValueDef, String>, TableCell<ValueDef, String>>
			VALUEDEF_EDIT_CELL_FACTORY = tableColumn -> new DynamicValueDefEditingTableCell();
	
	private final ObservableList<ValueDef> hotkeysData = FXCollections.observableArrayList();
	private final ObservableList<ValueDef> settingsData = FXCollections.observableArrayList();
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
	
	/**
	 * On Controller initialization.
	 */
	@FXML
	public void initialize() {
		logger.trace("initializing");
		
		final Callback<CellDataFeatures<ValueDef, String>, ObservableValue<String>> idFac =
				new PropertyValueFactory<>("id");
		final Callback<CellDataFeatures<ValueDef, String>, ObservableValue<String>> descFac =
				new PropertyValueFactory<>("description");
		final Callback<CellDataFeatures<ValueDef, String>, ObservableValue<String>> defaultValueFac =
				new PropertyValueFactory<>("defaultValue");
		final Callback<CellDataFeatures<ValueDef, String>, ObservableValue<String>> valueFac =
				new PropertyValueFactory<>("value");
		final Callback<CellDataFeatures<ValueDef, Boolean>, ObservableValue<Boolean>> hasChangedFac =
				new PropertyValueFactory<>("hasChanged");
		
		hotkeysNameCol.setCellValueFactory(idFac);
		hotkeysDescriptionCol.setCellValueFactory(descFac);
		hotkeysDescriptionCol.setCellFactory(WRAPPING_CELL_FACTORY);
		hotkeysDefaultCol.setCellValueFactory(defaultValueFac);
		hotkeysKeyCol.setCellValueFactory(valueFac);
		hotkeysKeyCol.setCellFactory(VALUEDEF_EDIT_CELL_FACTORY);
		hotkeysKeyCol.setSortable(false);
		hotkeysActionsCol.setSortable(false);
		hotkeysActionsCol.setCellValueFactory(hasChangedFac);
		hotkeysActionsCol.setCellFactory(ActionColumnCellFactoryReset);
		
		settingsNameCol.setCellValueFactory(idFac);
		settingsDescriptionCol.setCellValueFactory(descFac);
		settingsDescriptionCol.setCellFactory(WRAPPING_CELL_FACTORY);
		settingsDefaultCol.setCellValueFactory(defaultValueFac);
		settingsValueCol.setCellValueFactory(valueFac);
		settingsValueCol.setCellFactory(VALUEDEF_EDIT_CELL_FACTORY);
		settingsValueCol.setSortable(false);
		settingsActionsCol.setSortable(false);
		settingsActionsCol.setCellValueFactory(hasChangedFac);
		settingsActionsCol.setCellFactory(ActionColumnCellFactoryReset);
		
		hotkeysTable.setItems(hotkeysData);
		settingsTable.setItems(settingsData);
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
