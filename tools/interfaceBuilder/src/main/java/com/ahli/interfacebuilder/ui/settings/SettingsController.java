// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.ui.settings;

import com.ahli.interfacebuilder.ui.FXMLSpringLoader;
import com.ahli.interfacebuilder.ui.FxmlController;
import com.ahli.interfacebuilder.ui.Updateable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

@Log4j2
public class SettingsController implements Updateable, FxmlController {
	
	private final Map<SettingsView, TreeItem<String>> categories;
	private final ApplicationContext appContext;
	@FXML
	private TreeView<String> categoryTree;
	@FXML
	private ScrollPane contentContainer;
	
	public SettingsController(final ApplicationContext appContext) {
		this.appContext = appContext;
		categories = new EnumMap<>(SettingsView.class);
	}
	
	/**
	 * Automatically called by FxmlLoader
	 */
	@Override
	public void initialize() {
		categoryTree.setRoot(new TreeItem<>());
		categories.clear();
		addCategoryItem(new TreeItem<>("Games & Paths"), SettingsView.GAMEPATHS);
		addCategoryItem(new TreeItem<>("GUI Mode"), SettingsView.GUITOOL);
		addCategoryItem(new TreeItem<>("Command Line Mode"), SettingsView.CMDLINETOOL);
		
		// load page for selected setting
		categoryTree.getSelectionModel().selectedItemProperty().addListener(new PageSelectionListener(this));
	}
	
	/**
	 * Adds a category to the first level of the tree.
	 *
	 * @param item
	 */
	private void addCategoryItem(final TreeItem<String> item, final SettingsView type) {
		categoryTree.getRoot().getChildren().add(item);
		categories.put(type, item);
	}
	
	@Override
	public void update() {
		final MultipleSelectionModel<?> selectionModel = categoryTree.getSelectionModel();
		// select first category if nothing is selected
		if (selectionModel.getSelectedItem() == null) {
			selectionModel.select(0);
		}
	}
	
	private enum SettingsView {
		GAMEPATHS, GUITOOL, CMDLINETOOL
	}
	
	private record PageSelectionListener(SettingsController settingsController)
			implements ChangeListener<TreeItem<String>> {
		
		@Override
		public void changed(
				final ObservableValue<? extends TreeItem<String>> observable,
				final TreeItem<String> oldVal,
				final TreeItem<String> newVal) {
			if (oldVal != newVal && newVal != null &&
					newVal == settingsController.categoryTree.getSelectionModel().getSelectedItem()) {
				loadSettingsContent(newVal);
			}
		}
		
		/**
		 * @param category
		 */
		private void loadSettingsContent(final TreeItem<String> category) {
			final String fxmlPath = switch (getCategoryIndex(category)) {
				case GAMEPATHS -> "classpath:view/Settings_GamesPaths.fxml";
				case GUITOOL -> "classpath:view/Settings_GuiTool.fxml";
				case CMDLINETOOL -> "classpath:view/Settings_CommandLineTool.fxml";
			};
			final FXMLSpringLoader loader = new FXMLSpringLoader(settingsController.appContext);
			final Parent content = initFXML(loader, fxmlPath);
			final Object controller = loader.getController();
			if (controller instanceof Updateable updateable) {
				updateable.update();
			}
			settingsController.contentContainer.setContent(content);
		}
		
		/**
		 * Returns the SettingsView of the category within the array that stores all categories.
		 *
		 * @param category
		 * @return
		 */
		private SettingsView getCategoryIndex(final TreeItem<String> category) {
			return settingsController.categories.entrySet()
					.stream()
					.filter(e -> category.equals(e.getValue()))
					.findFirst()
					.orElseThrow()
					.getKey();
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
				log.error(String.format("failed to load FXML: %s.", path), e);
			}
			return null;
		}
	}
}
