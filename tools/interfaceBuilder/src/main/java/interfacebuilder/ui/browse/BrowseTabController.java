package interfacebuilder.ui.browse;

import com.ahli.galaxy.ui.UITemplate;
import com.ahli.galaxy.ui.abstracts.UIElement;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import gnu.trove.map.hash.THashMap;
import interfacebuilder.ui.settings.Updateable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BrowseTabController implements Updateable {
	private static final String GAME_UI = "GameUI";
	
	@FXML
	private TreeView<UIElement> frameTree;
	@FXML
	private ComboBox<String> fileDropdown;
	private AutoCompletionBinding<String> fileDropdownAutoCompleteBinding;
	@FXML
	private ComboBox<String> templateDropdown;
	private AutoCompletionBinding<String> templateDropdownAutoCompleteBinding;
	private Map<String, UITemplate> templateMap;
	
	private UICatalog uiCatalog;
	
	/**
	 * Automatically called by FxmlLoader
	 */
	public void initialize() {
		templateMap = new THashMap<>();
		fileDropdown.setOnAction(actionEvent -> Platform.runLater(() -> {
			final String selectedFile = fileDropdown.getSelectionModel().getSelectedItem();
			updateTemplateDropdown(selectedFile);
		}));
		templateDropdown.setOnAction(actionEvent -> Platform.runLater(() -> {
			String selectedTemplateRootElem = templateDropdown.getSelectionModel().getSelectedItem();
			final UITemplate template = templateMap.get(selectedTemplateRootElem);
			createTree(template);
		}));
	}
	
	private void createTree(final UITemplate template) {
		if (template != null) {
			final UIElement rootElement = template.getElement();
			TreeItem<UIElement> treeItem = new TreeItem<>(rootElement);
			frameTree.setRoot(treeItem);
			final ObservableList<TreeItem<UIElement>> treeItemChildren = frameTree.getRoot().getChildren();
			for (UIElement child : rootElement.getChildren()) {
				createTree(child, treeItemChildren);
			}
			treeItem.expandedProperty().setValue(true);
		} else {
			frameTree.setRoot(null);
		}
	}
	
	private void createTree(final UIElement element, ObservableList<TreeItem<UIElement>> parentsChildren) {
		TreeItem<UIElement> treeItem = new TreeItem<>(element);
		parentsChildren.add(treeItem);
		final ObservableList<TreeItem<UIElement>> treeItemChildren = treeItem.getChildren();
		for (UIElement child : element.getChildren()) {
			createTree(child, treeItemChildren);
		}
		treeItem.expandedProperty().setValue(true);
	}
	
	public void setData(final UICatalog uiCatalog) {
		this.uiCatalog = uiCatalog;
		updateDropdowns();
	}
	
	private void updateDropdowns() {
		if (uiCatalog != null) {
			final Set<String> fileNamesSet = new HashSet<>();
			for (UITemplate template : uiCatalog.getTemplates()) {
				fileNamesSet.add(template.getFileName());
			}
			final ObservableList<String> fileNames = FXCollections.observableList(new ArrayList<>(fileNamesSet));
			fileNames.sort(null);
			fileDropdown.setItems(fileNames);
			final String firstSelection = fileNamesSet.contains(GAME_UI) ? GAME_UI : fileNames.get(0);
			fileDropdown.setValue(firstSelection);
			if (fileDropdownAutoCompleteBinding != null) {
				fileDropdownAutoCompleteBinding.dispose();
			}
			fileDropdownAutoCompleteBinding =
					TextFields.bindAutoCompletion(fileDropdown.getEditor(), fileDropdown.getItems());
			fileDropdownAutoCompleteBinding.setMaxWidth(fileDropdown.getWidth());
			fileDropdownAutoCompleteBinding.setMinWidth(fileDropdown.getWidth());
			fileDropdownAutoCompleteBinding.setPrefWidth(fileDropdown.getWidth());
		}
	}
	
	@Override
	public void update() {
		// nothing to do
	}
	
	private void updateTemplateDropdown(final String fileName) {
		if (uiCatalog != null) {
			templateMap.clear();
			List<String> templatesOfFile = new ArrayList<>();
			String firstSelection = null;
			String name;
			for (UITemplate template : uiCatalog.getTemplates()) {
				if (template.getFileName().equals(fileName)) {
					name = template.getElement().getName();
					templatesOfFile.add(name);
					templateMap.put(name, template);
					if (name.equals(GAME_UI)) {
						firstSelection = name;
					}
				}
			}
			final ObservableList<String> elementNames = FXCollections.observableList(new ArrayList<>(templatesOfFile));
			templateDropdown.setItems(elementNames);
			if (templateDropdownAutoCompleteBinding != null) {
				templateDropdownAutoCompleteBinding.dispose();
			}
			templateDropdownAutoCompleteBinding =
					TextFields.bindAutoCompletion(templateDropdown.getEditor(), templateDropdown.getItems());
			templateDropdownAutoCompleteBinding.setMaxWidth(templateDropdown.getWidth());
			templateDropdownAutoCompleteBinding.setMinWidth(templateDropdown.getWidth());
			templateDropdownAutoCompleteBinding.setPrefWidth(templateDropdown.getWidth());
			if (firstSelection != null) {
				templateDropdown.setValue(firstSelection);
			} else {
				templateDropdown.selectionModelProperty().get().selectFirst();
			}
		}
	}
}
