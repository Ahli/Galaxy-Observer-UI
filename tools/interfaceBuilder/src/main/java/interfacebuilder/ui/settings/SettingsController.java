// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.settings;

import interfacebuilder.ui.FXMLSpringLoader;
import interfacebuilder.ui.Updateable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

public class SettingsController implements Updateable {
	private static final Logger logger = LogManager.getLogger(SettingsController.class);
	/*
	categories: 0=GamePaths, 1=GuiMmode, 2=CommandLineMode
	 */
	@SuppressWarnings("unchecked")
	private final TreeItem<String>[] categories = new TreeItem[3];
	@Autowired
	private ApplicationContext appContext;
	@FXML
	private TreeView<String> categoryTree;
	@FXML
	private ScrollPane contentContainer;
	
	public SettingsController() {
		// nothing to do
	}
	
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
			public void changed(final ObservableValue<? extends TreeItem<String>> observable,
					final TreeItem<String> oldVal, final TreeItem<String> newVal) {
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
	private void addCategoryItem(final TreeItem<String> item, final int index) {
		categoryTree.getRoot().getChildren().add(item);
		categories[index] = item;
	}
	
	/**
	 * @param category
	 */
	private void loadSettingsContent(final TreeItem<String> category) {
		final Parent content;
		final FXMLSpringLoader loader = new FXMLSpringLoader(appContext);
		final Object controller;
		switch (getCategoryIndex(category)) {
			case 0 -> content = initFXML(loader, "classpath:view/Content_Settings_GamesPaths.fxml");
			case 1 -> content = initFXML(loader, "classpath:view/Content_Settings_GuiTool.fxml");
			case 2 -> content = initFXML(loader, "classpath:view/Content_Settings_CommandLineTool.fxml");
			default -> {
				logger.error("Attempted to load a settings category that does not exist");
				return;
			}
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
	private static Parent initFXML(final FXMLSpringLoader loader, final String path) {
		try {
			return loader.load(path);
		} catch (final IOException e) {
			logger.error("failed to load FXML: " + path + ".", e);
		}
		return null;
	}
	
	@Override
	public void update() {
		final MultipleSelectionModel<?> selectionModel = categoryTree.getSelectionModel();
		// select first category if nothing is selected
		if (selectionModel.getSelectedItem() == null) {
			selectionModel.select(categoryTree.getRow(categories[0]));
		}
	}
}
