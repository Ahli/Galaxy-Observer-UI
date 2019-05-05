// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.browse;

import com.ahli.galaxy.ui.UIAnchorSide;
import com.ahli.galaxy.ui.UIAnimation;
import com.ahli.galaxy.ui.UIAttribute;
import com.ahli.galaxy.ui.UIController;
import com.ahli.galaxy.ui.UIFrame;
import com.ahli.galaxy.ui.UIState;
import com.ahli.galaxy.ui.UIStateGroup;
import com.ahli.galaxy.ui.UITemplate;
import com.ahli.galaxy.ui.abstracts.UIElement;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import interfacebuilder.ui.settings.Updateable;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BrowseTabController implements Updateable {
	private static final Logger logger = LogManager.getLogger(BrowseTabController.class);
	private static final String GAME_UI = "GameUI";
	private static final String SPACE_HIVEN_SPACE = " - ";
	//	private final StringProperty filterProp = new SimpleStringProperty();
	@FXML
	private Label pathLabel;
	@FXML
	private TextField treeFilter;
	@FXML
	private TableView<Map.Entry<String, String>> tableView;
	@FXML
	private TableColumn<Map.Entry<String, String>, String> columnAttributes;
	@FXML
	private TableColumn<Map.Entry<String, String>, String> columnValues;
	@FXML
	private AnchorPane frameTreeContainer;
	@FXML
	private TreeView<UIElement> frameTree;
	private Map<TreeItem<UIElement>, List<TreeItem<UIElement>>> hiddenTreeChildMap;
	@FXML
	private ComboBox<String> fileDropdown;
	//	private AutoCompletionBinding<String> fileDropdownAutoCompleteBinding;
	@FXML
	private ComboBox<String> templateDropdown;
	//	private AutoCompletionBinding<String> templateDropdownAutoCompleteBinding;
	private Map<String, UITemplate> templateMap;
	private int framesTotal;
	//	private Runnable queryRunnable;
	//	private Thread queryThread;
	private UICatalog uiCatalog;
	
	public BrowseTabController() {
		// nothing to do
	}
	
	private static String prettyPrintAttributeStringList(final List<String> attributes) {
		if (attributes.size() == 2 && "val".equals(attributes.get(0))) {
			return attributes.get(1);
		}
		final StringBuilder str = new StringBuilder();
		str.append(attributes.get(0)).append('=').append(attributes.get(1));
		final String separator = ", ";
		for (int i = 2, len = attributes.size(); i < len; i += 2) {
			str.append(separator).append(attributes.get(i)).append('=').append(attributes.get(i + 1));
		}
		return str.toString();
	}
	
	private static StringBuilder addParentPath(final StringBuilder sb, final TreeItem<UIElement> elem) {
		if (elem.getParent() != null) {
			addParentPath(sb, elem.getParent());
			sb.append(" > ");
		}
		return sb.append(elem.getValue().getName());
	}
	
	/**
	 * Automatically called by FxmlLoader
	 */
	public void initialize() {
		templateMap = new HashMap<>();
		fileDropdown.setOnAction(actionEvent -> Platform.runLater(() -> {
			final String selectedFile = fileDropdown.getSelectionModel().getSelectedItem();
			updateTemplateDropdown(selectedFile);
		}));
		templateDropdown.setOnAction(actionEvent -> Platform.runLater(() -> {
			final String selectedTemplateRootElem = templateDropdown.getSelectionModel().getSelectedItem();
			final UITemplate template = templateMap.get(selectedTemplateRootElem);
			final long start = System.currentTimeMillis();
			framesTotal = 0;
			createTree(template);
			logger.info("Tree creation: " + (System.currentTimeMillis() - start) + "ms , " + framesTotal + " frames");
		}));
		frameTree.getSelectionModel().selectedItemProperty()
				.addListener((observable, oldValue, newValue) -> showInTableView(newValue));
		
		columnAttributes.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().getKey()));
		columnValues.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().getValue()));
		columnAttributes.prefWidthProperty().bind(tableView.widthProperty().divide(3));
		columnValues.prefWidthProperty().bind(tableView.widthProperty().divide(1.5).subtract(5));
		columnAttributes.sortTypeProperty().set(TableColumn.SortType.ASCENDING);
		tableView.getSortOrder().add(columnAttributes);
		
		treeFilter.textProperty().addListener((observable, oldValue, newValue) -> filterTree(newValue/*, oldValue*/));
	}
	
	/**
	 * @param filter
	 */
	private void filterTree(final String filter/*, final String filterBefore*/) {
		final long startTime = System.currentTimeMillis();
		//		frameTreeContainer.getChildren().remove(frameTree);
		
		if (hiddenTreeChildMap == null) {
			hiddenTreeChildMap = new HashMap<>();
		}
		final String filterUpper = filter.toUpperCase();
		
		
		//		queryRunnable = () -> {
		//			synchronized (BrowseTabController.class) {
		//				try {
		final long midTime = System.currentTimeMillis();
		logger.info("filter-show: " + filterUpper + ", preparation: " + (midTime - startTime));
		filterTreeShow(filterUpper);
		final long midTime2 = System.currentTimeMillis();
		logger.info("filter-show execution: " + (midTime2 - midTime));
		if (!filterUpper.isEmpty()) {
			logger.info("filter-hide: " + filterUpper);
			filterTreeHide(filterUpper, frameTree.getRoot());
		}
		final long midTime3 = System.currentTimeMillis();
		logger.info("filter-hide execution: " + (midTime3 - midTime2));
		
		// release memory
		if (hiddenTreeChildMap.isEmpty()) {
			hiddenTreeChildMap = null;
		}
		
		//		frameTreeContainer.getChildren().add(frameTree);
		logger.info("filter cleanup: " + (System.currentTimeMillis() - midTime3));
		//				} catch (final InterruptedException e) {
		//					// do nothing
		//				}
		//			}
		
		//		};
		
		//		if (queryThread != null) {
		//			queryThread.interrupt();
		//		}
		//		queryThread = new Thread(queryRunnable);
		//		queryThread.start();
		
		//		filterProp.setValue(filter);
	}
	
	private void filterTreeShow(final String queryUpper) {
		List<TreeItem<UIElement>> children;
		for (final var entry : Set.copyOf(hiddenTreeChildMap.entrySet())) {
			//			if (Thread.currentThread().isInterrupted()) {
			//				throw new InterruptedException();
			//			}
			children = entry.getValue();
			final TreeItem<UIElement> parent = entry.getKey();
			final var visibleChildren = parent.getChildren();
			
			final int lenTotal = children.size();
			int indexVisible = 0;
			int sizeVisible = visibleChildren.size();
			boolean hasNoInvisChildLeft = true;
			TreeItem<UIElement> visChild;
			for (int i = 0; i < lenTotal; i++) {
				visChild = indexVisible < sizeVisible ? visibleChildren.get(indexVisible) : null;
				final TreeItem<UIElement> child = children.get(i);
				if (child != visChild) {
					final UIElement elem = child.getValue();
					final String name = elem.getName();
					if (name != null && name.toUpperCase().contains(queryUpper)) {
						// is a match -> make visible
						visibleChildren.add(indexVisible, child);
						sizeVisible++;
						indexVisible++;
					} else {
						// no match -> remain invisible
						hasNoInvisChildLeft = false;
					}
				} else {
					// already visible -> compare next
					indexVisible++;
				}
			}
			// clear the list, if possible
			if (hasNoInvisChildLeft) {
				hiddenTreeChildMap.remove(parent);
			}
		}
	}
	
	private boolean filterTreeHide(final String queryUpper, final TreeItem<UIElement> elem) {
		//		if (Thread.currentThread().isInterrupted()) {
		//			throw new InterruptedException();
		//		}
		final var children = elem.getChildren();
		if (!children.isEmpty()) {
			for (int i = 0, len = children.size(); i < len; i++) {
				if (filterTreeHide(queryUpper, children.get(i))) {
					i--;
					len--;
				}
			}
		}
		boolean removed = false;
		if (children.isEmpty()) {
			final String name = elem.getValue().getName();
			if (name == null || !name.toUpperCase().contains(queryUpper)) {
				removed = true;
				final var parent = elem.getParent();
				if (parent != null) {
					final var siblings = parent.getChildren();
					if (!hiddenTreeChildMap.containsKey(parent)) {
						final List<TreeItem<UIElement>> siblingsCopy = new ArrayList<>(siblings);
						hiddenTreeChildMap.put(parent, siblingsCopy);
					}
					siblings.remove(elem);
				} else {
					logger.info("root? ", name);
				}
			}
		}
		return removed;
	}
	
	private void showInTableView(final TreeItem<UIElement> selected) {
		updatePath(selected);
		if (selected == null) {
			tableView.getItems().clear();
		} else {
			final UIElement el = selected.getValue();
			// TODO maybe use something different that holds entries
			final Map<String, String> map = new HashMap<>((framesTotal * 75 / 100) + 1, 0.75F);
			if (el instanceof UIFrame) {
				final UIFrame elem = (UIFrame) el;
				UIAnchorSide side = UIAnchorSide.TOP;
				map.put("Anchor-Top",
						elem.getAnchorRelative(side) + SPACE_HIVEN_SPACE + elem.getAnchorPos(side) + SPACE_HIVEN_SPACE +
								elem.getAnchorOffset(side));
				side = UIAnchorSide.LEFT;
				map.put("Anchor-Left",
						elem.getAnchorRelative(side) + SPACE_HIVEN_SPACE + elem.getAnchorPos(side) + SPACE_HIVEN_SPACE +
								elem.getAnchorOffset(side));
				side = UIAnchorSide.BOTTOM;
				map.put("Anchor-Bottom",
						elem.getAnchorRelative(side) + SPACE_HIVEN_SPACE + elem.getAnchorPos(side) + SPACE_HIVEN_SPACE +
								elem.getAnchorOffset(side));
				side = UIAnchorSide.RIGHT;
				map.put("Anchor-Right",
						elem.getAnchorRelative(side) + SPACE_HIVEN_SPACE + elem.getAnchorPos(side) + SPACE_HIVEN_SPACE +
								elem.getAnchorOffset(side));
				for (final UIAttribute attr : elem.getAttributes()) {
					map.put(attr.getName(), prettyPrint(attr));
				}
			} else if (el instanceof UIStateGroup) {
				final UIStateGroup elem = (UIStateGroup) el;
				final String dfltState = elem.getDefaultState();
				if (dfltState != null) {
					map.put("DefaultState", dfltState);
				}
				final String statePrefix = "State ";
				int i = 0;
				for (final UIState state : elem.getStates()) {
					i++;
					map.put(statePrefix + i, state.getName());
				}
			} else if (el instanceof UIAnimation) {
				final UIAnimation elem = (UIAnimation) el;
				final UIAttribute driver = elem.getDriver();
				if (driver != null) {
					map.put("Driver", prettyPrint(driver));
				}
				int i = 0;
				final String eventPrefix = "Event ";
				for (final UIAttribute event : elem.getEvents()) {
					i++;
					map.put(eventPrefix + i, prettyPrint(event));
				}
			} else if (el instanceof UIController) {
				final UIController elem = (UIController) el;
				int i = 0;
				final String keyPrefix = "Key ";
				for (final UIAttribute attr : elem.getKeys()) {
					i++;
					map.put(keyPrefix + i, prettyPrint(attr));
				}
			} else if (el instanceof UIState) {
				final UIState elem = (UIState) el;
				int i = 0;
				final String whenPrefix = "When ";
				for (final UIAttribute attr : elem.getWhens()) {
					i++;
					map.put(whenPrefix + i, prettyPrint(attr));
				}
				i = 0;
				final String actionPrefix = "Action ";
				for (final UIAttribute attr : elem.getActions()) {
					i++;
					map.put(actionPrefix + i, prettyPrint(attr));
				}
			}
			tableView.getItems().setAll(map.entrySet());
			tableView.sort();
		}
	}
	
	private String prettyPrint(final UIAttribute attr) {
		return prettyPrintAttributeStringList(attr.getKeyValues());
	}
	
	private void updatePath(final TreeItem<UIElement> elem) {
		final String path = elem != null ? addParentPath(new StringBuilder(), elem).toString() : "";
		pathLabel.setText(path);
	}
	
	/**
	 * @param template
	 */
	private void createTree(final UITemplate template) {
		if (template != null) {
			final UIElement rootElement = template.getElement();
			final TreeItem<UIElement> rootItem = new TreeItem<>(rootElement);
			frameTree.setRoot(rootItem);
			framesTotal += 1;
			final ObservableList<TreeItem<UIElement>> treeItemChildren = rootItem.getChildren();
			for (final UIElement child : rootElement.getChildren()) {
				createTree(child, treeItemChildren);
			}
			rootItem.expandedProperty().setValue(true);
			
			//			rootItem.predicateProperty().bind(Bindings.createObjectBinding(new Callable<>() {
			//				@Override
			//				public TreeItemPredicate<UIElement> call() throws Exception {
			//					return TreeItemPredicate.<UIElement>create(
			//							elem -> {return elem.getName().equalsIgnoreCase(filterProp.getValue());});
			//				}
			//			}, filterProp));
		} else {
			frameTree.setRoot(null);
		}
		frameTree.getSelectionModel().select(0);
		
		// TEST
		//		MyNode myRootNode = myService.getTreeRootNode();
		//		FilterableTreeItem<MyNode> rootItem = new FilterableTreeItem<>(myRootNode);
		//		rootItem.setExpanded(true);
		//		for (MyNode node : myRootNode.getChildren()) {
		//			FilterableTreeItem<MyNode> item = new FilterableTreeItem<>(node);
		//			rootItem.getInternalChildren().add(item);
		//		}
		//		tree.setRoot(rootItem);
	}
	
	/**
	 * @param element
	 * @param parentsChildren
	 */
	private void createTree(final UIElement element, final ObservableList<TreeItem<UIElement>> parentsChildren) {
		final TreeItem<UIElement> treeItem = new TreeItem<>(element);
		parentsChildren.add(treeItem);
		framesTotal += 1;
		final ObservableList<TreeItem<UIElement>> treeItemChildren = treeItem.getChildren();
		for (final UIElement child : element.getChildren()) {
			createTree(child, treeItemChildren);
		}
		treeItem.expandedProperty().setValue(true);
	}
	
	/**
	 * @param uiCatalog
	 */
	public void setData(final UICatalog uiCatalog) {
		this.uiCatalog = uiCatalog;
		updateDropdowns();
	}
	
	/**
	 *
	 */
	private void updateDropdowns() {
		if (uiCatalog != null) {
			final Set<String> fileNamesSet = new HashSet<>();
			for (final UITemplate template : uiCatalog.getTemplates()) {
				fileNamesSet.add(template.getFileName());
			}
			final ObservableList<String> fileNames = FXCollections.observableList(new ArrayList<>(fileNamesSet));
			fileNames.sort(null);
			fileDropdown.setItems(fileNames);
			final String firstSelection = fileNamesSet.contains(GAME_UI) ? GAME_UI : fileNames.get(0);
			//			if (fileDropdownAutoCompleteBinding != null) {
			//				fileDropdownAutoCompleteBinding.dispose();
			//			}
			//			fileDropdownAutoCompleteBinding =
			//					TextFields.bindAutoCompletion(fileDropdown.getEditor(), fileDropdown.getItems());
			//			fileDropdownAutoCompleteBinding.setMaxWidth(fileDropdown.getWidth());
			//			fileDropdownAutoCompleteBinding.setMinWidth(fileDropdown.getWidth());
			//			fileDropdownAutoCompleteBinding.setPrefWidth(fileDropdown.getWidth());
			// delay required to fix some internal nullpointer on the first usage of such an element
			Platform.runLater(() -> fileDropdown.setValue(firstSelection));
		}
	}
	
	@Override
	public void update() {
		// nothing to do
	}
	
	/**
	 * @param fileName
	 */
	private void updateTemplateDropdown(final String fileName) {
		if (uiCatalog != null) {
			templateMap.clear();
			final List<String> templatesOfFile = new ArrayList<>();
			String firstSelection = null;
			String name;
			for (final UITemplate template : uiCatalog.getTemplates()) {
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
			//			if (templateDropdownAutoCompleteBinding != null) {
			//				templateDropdownAutoCompleteBinding.dispose();
			//			}
			//			templateDropdownAutoCompleteBinding =
			//					TextFields.bindAutoCompletion(templateDropdown.getEditor(), templateDropdown.getItems());
			//			templateDropdownAutoCompleteBinding.setMaxWidth(templateDropdown.getWidth());
			//			templateDropdownAutoCompleteBinding.setMinWidth(templateDropdown.getWidth());
			//			templateDropdownAutoCompleteBinding.setPrefWidth(templateDropdown.getWidth());
			if (firstSelection != null) {
				templateDropdown.setValue(firstSelection);
			} else {
				templateDropdown.selectionModelProperty().get().selectFirst();
			}
		}
	}
}
