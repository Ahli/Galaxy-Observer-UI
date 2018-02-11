package application.controller;

import application.InterfaceBuilderApp;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class SettingsController implements Updateable {
	private static final Logger logger = LogManager.getLogger();
	
	@FXML
	private TreeView<String> categoryTree;
	
	@FXML
	private ScrollPane contentContainer;
	
	/*
	categories: 0=GamePaths, 1=GuiMmode, 2=CommandLineMode
	 */
	private TreeItem<String>[] categories = new TreeItem[3];
	
	/**
	 * Automatically called by FxmlLoader
	 */
	public void initialize() {
		categoryTree.setRoot(new TreeItem<>());
		addCategoryItem(new TreeItem<>("Games & Paths"), 0);
		addCategoryItem(new TreeItem<>("GUI Mode"), 1);
		addCategoryItem(new TreeItem<>("Command Line Mode"), 2);
		
		// load page for selected setting
		categoryTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldVal,
					TreeItem<String> newVal) {
				if (oldVal != newVal && newVal != null &&
						newVal == categoryTree.getSelectionModel().getSelectedItem()) {
					loadSettingsContent(newVal);
				}
			}
		});
	}
	
	/**
	 * Adds a category to the first level of the tree.
	 *
	 * @param item
	 * @param index
	 */
	private void addCategoryItem(TreeItem<String> item, int index) {
		categoryTree.getRoot().getChildren().add(item);
		categories[index] = item;
	}
	
	/**
	 * @param category
	 */
	private void loadSettingsContent(final TreeItem<String> category) {
		final Parent content;
		FXMLLoader loader = new FXMLLoader();
		Object controller;
		switch (getCategoryIndex(category)) {
			case 0:
				content = initFXML(loader, "view/Content_Settings_GamesPaths.fxml");
				break;
			case 1:
				content = initFXML(loader, "view/Content_Settings_GuiTool.fxml");
				break;
			case 2:
				content = initFXML(loader, "view/Content_Settings_CommandLineTool.fxml");
				break;
			default:
				logger.error("Attempted to load a settings category that does not exist");
				return;
		}
		controller = loader.getController();
		if (controller instanceof Updateable) {
			((Updateable) controller).update();
		}
		contentContainer.setContent(content);
	}
	
	/**
	 * Returns the index of the category within the array that stores all categories.
	 *
	 * @param category
	 * @return
	 */
	private int getCategoryIndex(final TreeItem<String> category) {
		for (int i = 0, len = categories.length; i < len; i++) {
			if (category == categories[i]) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Initializes the TabPane for the Compiling Progress.
	 *
	 * @param loader
	 * @param path
	 * @return
	 */
	private Parent initFXML(FXMLLoader loader, String path) {
		try {
			return loader.load(InterfaceBuilderApp.class.getResourceAsStream(path));
		} catch (IOException e) {
			logger.error("failed to load FXML: " + path + ".", e); //$NON-NLS-1$
		}
		return null;
	}
	
	@Override
	public void update() {
		MultipleSelectionModel<?> selectionModel = categoryTree.getSelectionModel();
		// select first category if nothing is selected
		if (selectionModel.getSelectedItem() == null) {
			selectionModel.select(categoryTree.getRow(categories[0]));
		}
	}
}
