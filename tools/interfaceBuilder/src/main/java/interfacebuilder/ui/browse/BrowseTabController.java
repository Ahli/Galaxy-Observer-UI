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
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class BrowseTabController implements Updateable {
	private static final String PATH_SEPARATOR = " > ";
	private static final String ANCHOR_RIGHT = "Anchor-Right";
	private static final String DEFAULT_STATE = "DefaultState";
	private static final String STATE_PREFIX = "State ";
	private static final String DRIVER = "Driver";
	private static final String EVENT_PREFIX = "Event ";
	private static final String ANCHOR_TOP = "Anchor-Top";
	private static final String ANCHOR_LEFT = "Anchor-Left";
	private static final String ANCHOR_BOTTOM = "Anchor-Bottom";
	private static final String KEY_PREFIX = "Key ";
	private static final String WHEN_PREFIX = "When ";
	private static final String ACTION_PREFIX = "Action ";
	private static final String VAL = "val";
	private static final String ATTRIBUTE_SEPARATOR = ", ";
	private static final Logger logger = LogManager.getLogger(BrowseTabController.class);
	private static final String GAME_UI = "GameUI";
	private static final String SPACE_HIVEN_SPACE = " - ";
	private final StringProperty queryString;
	@FXML
	private TextFlow pathTextFlow;
	@FXML
	private TextField treeFilter;
	@FXML
	private TableView<Map.Entry<String, String>> tableView;
	@FXML
	private TableColumn<Map.Entry<String, String>, String> columnAttributes;
	@FXML
	private TableColumn<Map.Entry<String, String>, String> columnValues;
	@FXML
	private TreeView<UIElement> frameTree;
	@FXML
	private ComboBox<String> fileDropdown;
	@FXML
	private ComboBox<String> templateDropdown;
	private Map<String, UITemplate> templateMap;
	private int framesTotal;
	private UICatalog uiCatalog;
	private volatile boolean queryRunning;
	
	public BrowseTabController() {
		// nothing to do
		queryString = new SimpleStringProperty("");
	}
	
	private static String prettyPrintAttributeStringList(final List<String> attributes) {
		if (attributes.size() == 2 && VAL.equals(attributes.get(0))) {
			return attributes.get(1);
		}
		final StringBuilder str = new StringBuilder();
		str.append(attributes.get(0)).append('=').append(attributes.get(1));
		for (int i = 2, len = attributes.size(); i < len; i += 2) {
			str.append(ATTRIBUTE_SEPARATOR).append(attributes.get(i)).append('=').append(attributes.get(i + 1));
		}
		return str.toString();
	}
	
	private static StringBuilder addParentPath(final StringBuilder sb, final TreeItem<UIElement> elem) {
		if (elem.getParent() != null) {
			addParentPath(sb, elem.getParent());
			sb.append(PATH_SEPARATOR);
		}
		return sb.append(elem.getValue().getName());
	}
	
	private static String prettyPrint(final UIAttribute attr) {
		return prettyPrintAttributeStringList(attr.getKeyValues());
	}
	
	/**
	 * Automatically called by FxmlLoader
	 */
	public void initialize() {
		templateMap = new UnifiedMap<>();
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
			logger.info("Tree creation: {}ms , {} frames", (System.currentTimeMillis() - start), framesTotal);
		}));
		frameTree.getSelectionModel().selectedItemProperty()
				.addListener((observable, oldValue, newValue) -> showInTableView(newValue));
		
		columnAttributes.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().getKey()));
		columnValues.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().getValue()));
		columnAttributes.prefWidthProperty().bind(tableView.widthProperty().divide(3));
		columnValues.prefWidthProperty().bind(tableView.widthProperty().divide(1.5).subtract(5));
		columnAttributes.sortTypeProperty().set(TableColumn.SortType.ASCENDING);
		tableView.getSortOrder().add(columnAttributes);
		
		treeFilter.textProperty().addListener((observable, oldValue, newValue) -> filterTree(newValue));
		
		final ObservableList<Node> children = pathTextFlow.getChildren();
		final var header = new Text("Path: ");
		header.setStyle("-fx-font-weight: bold; -fx-fill: white; -fx-font-smoothing-type: lcd;");
		children.add(header);
	}
	
	/**
	 * @param filter
	 */
	private void filterTree(final String filter) {
		synchronized (BrowseTabController.class) {
			if (!queryRunning) {
				queryRunning = true;
				final var root = frameTree.getRoot();
				final var selectedItem = frameTree.getSelectionModel().getSelectedItem();
				final var tableViewPlaceholderText = tableView.getPlaceholder();
				tableView.setPlaceholder(new Text(""));
				frameTree.setRoot(null);
				
				new Thread(() -> {
					String queuedQuery = filter;
					while (queuedQuery != null) {
						final long startTime = System.currentTimeMillis();
						final String str = queuedQuery;
						queuedQuery = null;
						queryString.set(str.toUpperCase());
						logger.info("filter apply: {}ms", (System.currentTimeMillis() - startTime));
					}
					Platform.runLater(() -> {
						frameTree.setRoot(root);
						frameTree.getSelectionModel().select(selectedItem);
						tableView.setPlaceholder(tableViewPlaceholderText);
						final int selectedIndex = frameTree.getSelectionModel().getSelectedIndex();
						frameTree.scrollTo(selectedIndex);
						// clear tableview & path if the selected item is not visible OR re-show it when visible
						showInTableView(selectedIndex < 0 ? null : selectedItem);
					});
					queryRunning = false;
				}, "BrowseFilter").start();
			}
		}
	}
	
	private void showInTableView(final TreeItem<UIElement> selected) {
		updatePath(selected);
		if (selected == null) {
			tableView.getItems().clear();
		} else {
			final UIElement el = selected.getValue();
			final Map<String, String> map = new UnifiedMap<>();
			if (el instanceof UIFrame) {
				final UIFrame elem = (UIFrame) el;
				UIAnchorSide side = UIAnchorSide.TOP;
				map.put(ANCHOR_TOP,
						elem.getAnchorRelative(side) + SPACE_HIVEN_SPACE + elem.getAnchorPos(side) + SPACE_HIVEN_SPACE +
								elem.getAnchorOffset(side));
				side = UIAnchorSide.LEFT;
				map.put(ANCHOR_LEFT,
						elem.getAnchorRelative(side) + SPACE_HIVEN_SPACE + elem.getAnchorPos(side) + SPACE_HIVEN_SPACE +
								elem.getAnchorOffset(side));
				side = UIAnchorSide.BOTTOM;
				map.put(ANCHOR_BOTTOM,
						elem.getAnchorRelative(side) + SPACE_HIVEN_SPACE + elem.getAnchorPos(side) + SPACE_HIVEN_SPACE +
								elem.getAnchorOffset(side));
				side = UIAnchorSide.RIGHT;
				map.put(ANCHOR_RIGHT,
						elem.getAnchorRelative(side) + SPACE_HIVEN_SPACE + elem.getAnchorPos(side) + SPACE_HIVEN_SPACE +
								elem.getAnchorOffset(side));
				for (final UIAttribute attr : elem.getAttributes()) {
					map.put(attr.getName(), prettyPrint(attr));
				}
			} else if (el instanceof UIStateGroup) {
				final UIStateGroup elem = (UIStateGroup) el;
				final String dfltState = elem.getDefaultState();
				if (dfltState != null) {
					map.put(DEFAULT_STATE, dfltState);
				}
				int i = 0;
				for (final UIElement state : elem.getStates()) {
					i++;
					map.put(STATE_PREFIX + i, state.getName());
				}
			} else if (el instanceof UIAnimation) {
				final UIAnimation elem = (UIAnimation) el;
				final UIAttribute driver = elem.getDriver();
				if (driver != null) {
					map.put(DRIVER, prettyPrint(driver));
				}
				int i = 0;
				for (final UIAttribute event : elem.getEvents()) {
					i++;
					map.put(EVENT_PREFIX + i, prettyPrint(event));
				}
			} else if (el instanceof UIController) {
				final UIController elem = (UIController) el;
				int i = 0;
				for (final UIAttribute attr : elem.getKeys()) {
					i++;
					map.put(KEY_PREFIX + i, prettyPrint(attr));
				}
			} else if (el instanceof UIState) {
				final UIState elem = (UIState) el;
				int i = 0;
				for (final UIAttribute attr : elem.getWhens()) {
					i++;
					map.put(WHEN_PREFIX + i, prettyPrint(attr));
				}
				i = 0;
				for (final UIAttribute attr : elem.getActions()) {
					i++;
					map.put(ACTION_PREFIX + i, prettyPrint(attr));
				}
			}
			tableView.getItems().setAll(map.entrySet());
			tableView.sort();
		}
	}
	
	private void updatePath(final TreeItem<UIElement> elem) {
		final ObservableList<Node> children = pathTextFlow.getChildren();
		if (children.size() > 1) {
			children.remove(1);
		}
		if (elem != null) {
			final String path = addParentPath(new StringBuilder(), elem).toString();
			final var text = new Text(path);
			text.setStyle("-fx-fill: white; -fx-font-smoothing-type: lcd;");
			children.add(text);
		}
	}
	
	/**
	 * @param template
	 */
	@SuppressWarnings ("squid:S1604") // replace anonymous class with lambda suggestion
	private void createTree(final UITemplate template) {
		if (template != null) {
			final UIElement rootElement = template.getElement();
			final FilterableTreeItem<UIElement> rootItem = new FilterableTreeItem<>(rootElement);
			
			// set filter predicate
			rootItem.predicateProperty()
					.bind(Bindings.createObjectBinding(() -> TreeItemPredicate.create(new Predicate<>() {
						/* do not turn the Predicate into a lambda -> for some reason it becomes 2-3x slower except
						for first usage */
						@Override
						public boolean test(final UIElement element) {
							/* I could not get this code any faster than this form (caching toUpperString() was not faster) */
							return queryString.getValue().isEmpty() || (element.getName() != null &&
									element.getName().toUpperCase().contains(queryString.getValue()));
						}
					}), queryString));
			
			frameTree.setRoot(rootItem);
			framesTotal += 1;
			final ObservableList<TreeItem<UIElement>> treeItemChildren = rootItem.getInternalChildren();
			for (final UIElement child : rootElement.getChildren()) {
				createTree(child, treeItemChildren);
			}
			rootItem.expandedProperty().setValue(true);
			frameTree.getSelectionModel().select(0);
		} else {
			frameTree.setRoot(null);
		}
	}
	
	/**
	 * @param element
	 * @param parentsChildren
	 */
	private void createTree(final UIElement element, final ObservableList<TreeItem<UIElement>> parentsChildren) {
		final FilterableTreeItem<UIElement> treeItem = new FilterableTreeItem<>(element);
		parentsChildren.add(treeItem);
		framesTotal += 1;
		final ObservableList<TreeItem<UIElement>> treeItemChildren = treeItem.getInternalChildren();
		final List<UIElement> children = element.getChildrenRaw();
		if (children != null) {
			for (final UIElement child : children) {
				createTree(child, treeItemChildren);
			}
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
			final Set<String> fileNamesSet = new UnifiedSet<>(uiCatalog.getTemplates().size());
			for (final UITemplate template : uiCatalog.getTemplates()) {
				fileNamesSet.add(template.getFileName());
			}
			final ObservableList<String> fileNames = FXCollections.observableList(new ArrayList<>(fileNamesSet));
			fileNames.sort(null);
			fileDropdown.setItems(fileNames);
			final String firstSelection = fileNamesSet.contains(GAME_UI) ? GAME_UI : fileNames.get(0);
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
			if (firstSelection != null) {
				templateDropdown.setValue(firstSelection);
			} else {
				templateDropdown.selectionModelProperty().get().selectFirst();
			}
		}
	}
	
}
