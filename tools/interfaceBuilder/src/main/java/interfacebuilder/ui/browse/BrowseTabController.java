// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.browse;

import com.ahli.galaxy.ui.UIAnchorSide;
import com.ahli.galaxy.ui.UIFrameMutable;
import com.ahli.galaxy.ui.UITemplate;
import com.ahli.galaxy.ui.interfaces.UIAnimation;
import com.ahli.galaxy.ui.interfaces.UIAttribute;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import com.ahli.galaxy.ui.interfaces.UIController;
import com.ahli.galaxy.ui.interfaces.UIElement;
import com.ahli.galaxy.ui.interfaces.UIFrame;
import com.ahli.galaxy.ui.interfaces.UIState;
import com.ahli.galaxy.ui.interfaces.UIStateGroup;
import interfacebuilder.ui.Updateable;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import static interfacebuilder.ui.AppController.FATAL_ERROR;

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
	private final Callable<TreeItemPredicate<UIElement>> searchCallable;
	private final TextFlowFactory flowFactory;
	private final Object instanceLock = new Object();
	private AutoCompleteComboBox fileSelector;
	private AutoCompleteComboBox templateSelector;
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
	private AnchorPane anchorPane;
	private Map<String, UITemplate> templateMap;
	private int framesTotal;
	private UICatalog uiCatalog;
	/**
	 * Indicates that the filter query Thread is still working on searches.
	 */
	private volatile boolean queryIdling;
	/**
	 * Value used for the next Query executed. This needs to be on the controller instance's scope, so it can be
	 * overridden to skip unnecessary searches.
	 */
	@SuppressWarnings("squid:S1450") // no, it cannot be made local.
	private volatile String queriedFilter;
	private TreeFilteringChangeListener treeFilterListener;
	private FrameTreeSelectionChangedHandler treeSelectionlistener;
	
	public BrowseTabController() {
		queryIdling = true;
		queryString = new SimpleStringProperty("");
		
		fileSelector = new AutoCompleteComboBox();
		fileSelector.setPrefHeight(25);
		fileSelector.setPrefWidth(218);
		
		templateSelector = new AutoCompleteComboBox();
		templateSelector.setPrefHeight(25);
		templateSelector.setPrefWidth(218);
		
		searchCallable = new SearchCallable(queryString);
		flowFactory = new TextFlowFactory();
	}
	
	private static String prettyPrintAttributeStringList(final List<String> attributes) {
		if (attributes.size() == 2 && VAL.equals(attributes.get(0))) {
			return attributes.get(1);
		}
		final StringBuilder str = new StringBuilder(16).append(attributes.get(0)).append('=').append(attributes.get(1));
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
		AnchorPane.setTopAnchor(fileSelector, 5.0d);
		AnchorPane.setLeftAnchor(fileSelector, 5.0d);
		AnchorPane.setRightAnchor(fileSelector, 5.0d);
		
		AnchorPane.setTopAnchor(templateSelector, 35.0d);
		AnchorPane.setLeftAnchor(templateSelector, 5.0d);
		AnchorPane.setRightAnchor(templateSelector, 5.0d);
		
		anchorPane.getChildren().add(fileSelector);
		anchorPane.getChildren().add(templateSelector);
		
		// must be strong EventHandler reference
		treeSelectionlistener = new FrameTreeSelectionChangedHandler(this);
		frameTree.getSelectionModel().selectedItemProperty().addListener(treeSelectionlistener);
		frameTree.setCellFactory(new FlowTreeCellFactory(flowFactory));
		
		// dropdowns
		templateMap = new UnifiedMap<>();
		
		// must be strong EventHandler reference
		fileSelector.setOnAction(new FileSelectionEventHandler(this));
		
		// must be strong EventHandler reference
		templateSelector.setOnAction(new TemplateSelectionEventHandler(this));
		
		// table
		columnAttributes.setCellValueFactory(new KeyCellFactory());
		columnValues.setCellValueFactory(new ValueCellFactory());
		
		columnValues.setCellFactory(tableColumn -> new WrappingTextTableCell());
		
		columnAttributes.prefWidthProperty().bind(tableView.widthProperty().divide(3));
		columnValues.prefWidthProperty().bind(tableView.widthProperty().divide(1.5).subtract(5));
		columnAttributes.sortTypeProperty().set(TableColumn.SortType.ASCENDING);
		tableView.getSortOrder().add(columnAttributes);
		
		
		// filter (must be strong reference)
		treeFilterListener = new TreeFilteringChangeListener(this);
		treeFilter.textProperty().addListener(treeFilterListener);
		
		// Path header
		final ObservableList<Node> children = pathTextFlow.getChildren();
		final Text header = new Text("Path: ");
		header.setStyle("-fx-font-weight: bold; -fx-fill: white; -fx-font-smoothing-type: lcd;");
		children.add(header);
	}
	
	/**
	 * @param filter
	 */
	private void filterTree(final String filter) {
		synchronized (instanceLock) {
			queriedFilter = filter;
			if (queryIdling) {
				queryIdling = false;
				final TreeItem<UIElement> root = frameTree.getRoot();
				final TreeItem<UIElement> selectedItem = frameTree.getSelectionModel().getSelectedItem();
				final Node tableViewPlaceholderText = tableView.getPlaceholder();
				tableView.setPlaceholder(new Text(""));
				frameTree.setRoot(null);
				
				new Thread(
						new TreeFilteringRunnable(filter, root, selectedItem, tableViewPlaceholderText, this),
						"BrowseFilter").start();
			}
		}
	}
	
	@SuppressWarnings("ObjectAllocationInLoop")
	private void showInTableView(final TreeItem<UIElement> selected) {
		updatePath(selected);
		if (selected == null) {
			tableView.getItems().clear();
		} else {
			final UIElement el = selected.getValue();
			final Map<String, String> map = new UnifiedMap<>();
			if (el instanceof final UIFrameMutable elem) {
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
				if (elem.getAttributesRaw() != null) {
					for (final UIAttribute attr : elem.getAttributes()) {
						map.put(attr.getName(), prettyPrint(attr));
					}
				}
			} else if (el instanceof final UIStateGroup elem) {
				final String dfltState = elem.getDefaultState();
				if (dfltState != null) {
					map.put(DEFAULT_STATE, dfltState);
				}
				int i = 0;
				for (final UIElement state : elem.getChildren()) {
					++i;
					map.put(STATE_PREFIX + i, state.getName());
				}
			} else if (el instanceof final UIAnimation elem) {
				final UIAttribute driver = elem.getDriver();
				if (driver != null) {
					map.put(DRIVER, prettyPrint(driver));
				}
				int i = 0;
				for (final UIAttribute event : elem.getEvents()) {
					++i;
					map.put(EVENT_PREFIX + i, prettyPrint(event));
				}
			} else if (el instanceof final UIController elem) {
				int i = 0;
				for (final UIAttribute attr : elem.getKeys()) {
					++i;
					map.put(KEY_PREFIX + i, prettyPrint(attr));
				}
				// TODO keyValueList, e.g. in a new panel where it would show template, file and type, too?
				
			} else if (el instanceof final UIState elem) {
				int i = 0;
				for (final UIAttribute attr : elem.getWhens()) {
					++i;
					map.put(WHEN_PREFIX + i, prettyPrint(attr));
				}
				i = 0;
				for (final UIAttribute attr : elem.getActions()) {
					++i;
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
			final String path = addParentPath(new StringBuilder(16), elem).toString();
			final Text text = new Text(path);
			text.setStyle("-fx-fill: white; -fx-font-smoothing-type: lcd;");
			children.add(text);
		}
	}
	
	/**
	 * @param uiCatalog
	 */
	public void setData(final UICatalog uiCatalog) {
		this.uiCatalog = uiCatalog;
		updateDropdowns();
		
		// clean up memory leaks
		if (uiCatalog == null) {
			logger.trace("Cleaning up BrowseTabController");
			fileSelector.setOnAction(null);
			fileSelector = null;
			templateSelector.setOnAction(null);
			templateSelector = null;
			
			treeFilter.textProperty().removeListener(treeFilterListener);
			treeFilterListener = null;
			
			frameTree.getSelectionModel().selectedItemProperty().removeListener(treeSelectionlistener);
			treeSelectionlistener = null;
			
			// causes tree item predicates to be garbage collected
			frameTree.setRoot(null);
			
			// cause cellfactory to be garbage collected
			frameTree.setCellFactory(null);
			columnAttributes.setCellValueFactory(null);
			columnValues.setCellValueFactory(null);
		}
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
			final String firstSelection = fileNamesSet.contains(GAME_UI) ? GAME_UI : fileNames.get(0);
			logger.trace("updating dropdown - fileNames: {}, firstSelection: {}", fileNamesSet.size(), firstSelection);
			Platform.runLater(() -> {
				try {
					logger.trace("updating file dropdown items");
					fileSelector.setItems(fileNames);
					logger.trace("updating file dropdown value");
					fileSelector.setValue(firstSelection);
					logger.trace("finished updating file dropdown");
				} catch (final Exception e) {
					logger.fatal(FATAL_ERROR, e);
				}
			});
		}
	}
	
	@Override
	public void update() {
		// nothing to do
	}
	
	/**
	 * Cell Factory for TreeCells that contain a TextFlow
	 */
	private static final class FlowTreeCellFactory implements Callback<TreeView<UIElement>, TreeCell<UIElement>> {
		private final TextFlowFactory flowFactory;
		
		private FlowTreeCellFactory(final TextFlowFactory flowFactory) {
			this.flowFactory = flowFactory;
		}
		
		@Override
		public TreeCell<UIElement> call(final TreeView<UIElement> treeView) {
			return new CustomTreeCell(flowFactory);
		}
	}
	
	private static final class TreeFilteringChangeListener implements ChangeListener<String> {
		private final BrowseTabController controller;
		
		private TreeFilteringChangeListener(final BrowseTabController controller) {
			this.controller = controller;
		}
		
		@Override
		public void changed(
				final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
			controller.filterTree(newValue);
		}
	}
	
	private static final class FrameTreeSelectionChangedHandler implements ChangeListener<TreeItem<UIElement>> {
		private final BrowseTabController controller;
		
		private FrameTreeSelectionChangedHandler(final BrowseTabController controller) {
			this.controller = controller;
		}
		
		@Override
		public void changed(
				final ObservableValue<? extends TreeItem<UIElement>> observable,
				final TreeItem<UIElement> oldValue,
				final TreeItem<UIElement> newValue) {
			logger.trace("selection in tree changed: {}", newValue);
			controller.showInTableView(newValue);
		}
	}
	
	private static final class SearchCallable implements Callable<TreeItemPredicate<UIElement>> {
		private final StringProperty queryString;
		
		private SearchCallable(final StringProperty queryString) {
			this.queryString = queryString;
		}
		
		@Override
		public TreeItemPredicate<UIElement> call() {
			return TreeItemPredicate.create(new QueryPredicate(queryString));
		}
		
		private static final class QueryPredicate implements Predicate<UIElement> {
			private final StringProperty queryString;
			
			private QueryPredicate(final StringProperty queryString) {
				this.queryString = queryString;
			}
			
			/* do not turn the Predicate into a lambda -> for some reason it becomes 2-3x slower except
						for first usage */
			@SuppressWarnings("squid:S1067") // reduce number of conditional operators
			@Override
			public boolean test(final UIElement element) {
						/* I could not get this code any faster than this form (caching toUpperCase() was not
						faster) */
				return queryString.getValue().isEmpty() || (element.getName() != null &&
						AutoCompleteComboBox.containsIgnoreCase(element.getName(), queryString.getValue())) ||
						(element instanceof UIFrame frame &&
								AutoCompleteComboBox.containsIgnoreCase(frame.getType(), queryString.getValue()));
			}
		}
	}
	
	private static class KeyCellFactory implements
			Callback<TableColumn.CellDataFeatures<Map.Entry<String, String>, String>, ObservableValue<String>> {
		@Override
		public ObservableValue<String> call(final TableColumn.CellDataFeatures<Map.Entry<String, String>, String> p) {
			return new SimpleObjectProperty<>(p.getValue().getKey());
		}
	}
	
	private static class ValueCellFactory implements
			Callback<TableColumn.CellDataFeatures<Map.Entry<String, String>, String>, ObservableValue<String>> {
		@Override
		public ObservableValue<String> call(final TableColumn.CellDataFeatures<Map.Entry<String, String>, String> p) {
			return new SimpleObjectProperty<>(p.getValue().getValue());
		}
	}
	
	private static final class FileSelectionEventHandler implements EventHandler<ActionEvent> {
		private final BrowseTabController browseTabController;
		
		private FileSelectionEventHandler(final BrowseTabController browseTabController) {
			this.browseTabController = browseTabController;
		}
		
		@Override
		public void handle(final ActionEvent event) {
			logger.trace("selection of file changed: {}",
					() -> browseTabController.fileSelector.getSelectionModel().getSelectedItem());
			try {
				final String selectedFileName = browseTabController.fileSelector.getSelectionModel().getSelectedItem();
				updateTemplateDropdown(selectedFileName);
			} catch (final Exception e) {
				logger.fatal(FATAL_ERROR, e);
			}
		}
		
		/**
		 * @param fileName
		 */
		private void updateTemplateDropdown(final String fileName) {
			if (browseTabController.uiCatalog != null) {
				browseTabController.templateMap.clear();
				final List<String> templatesOfFile = new ArrayList<>();
				String firstSelection = null;
				String name;
				logger.trace("filling templatemap");
				for (final UITemplate template : browseTabController.uiCatalog.getTemplates()) {
					if (template.getFileName().equals(fileName)) {
						name = template.getElement().getName();
						templatesOfFile.add(name);
						browseTabController.templateMap.put(name, template);
						if (name.equals(GAME_UI)) {
							firstSelection = name;
						}
					}
				}
				logger.trace("templates of file: {}", templatesOfFile.size());
				final ObservableList<String> elementNames =
						FXCollections.observableList(new ArrayList<>(templatesOfFile));
				browseTabController.templateSelector.setItems(elementNames);
				if (firstSelection != null) {
					logger.trace("first selection: {}", firstSelection);
					browseTabController.templateSelector.setValue(firstSelection);
				} else {
					browseTabController.templateSelector.selectionModelProperty().get().selectFirst();
				}
				logger.trace("template dropdown updated");
			}
		}
	}
	
	private static final class TemplateSelectionEventHandler implements EventHandler<ActionEvent> {
		private final BrowseTabController browseTabController;
		private UITemplate curTreeTemplate;
		
		private TemplateSelectionEventHandler(final BrowseTabController browseTabController) {
			this.browseTabController = browseTabController;
		}
		
		@Override
		public void handle(final ActionEvent event) {
			try {
				if (browseTabController.templateMap != null) {
					final String selectedTemplateRootElem =
							browseTabController.templateSelector.getSelectionModel().getSelectedItem();
					logger.trace("selection of template changed: {}", selectedTemplateRootElem);
					final UITemplate template = browseTabController.templateMap.get(selectedTemplateRootElem);
					if (template != curTreeTemplate) {
						createTree(template);
					}
				}
			} catch (final Exception e) {
				logger.fatal(FATAL_ERROR, e);
			}
		}
		
		/**
		 * @param template
		 */
		private void createTree(final UITemplate template) {
			logger.trace("creating Tree");
			final long start = System.currentTimeMillis();
			curTreeTemplate = template;
			browseTabController.framesTotal = 0;
			if (template != null) {
				final UIElement rootElement = template.getElement();
				new Thread(() -> {
					final FilterableTreeItem<UIElement> rootItem = new FilterableTreeItem<>(rootElement);
					
					browseTabController.framesTotal += 1;
					final ObservableList<TreeItem<UIElement>> treeItemChildren = rootItem.getInternalChildren();
					for (final UIElement child : rootElement.getChildren()) {
						createTree(child, treeItemChildren);
					}
					rootItem.expandedProperty().setValue(true);
					// set filter predicate
					rootItem.predicateProperty()
							.bind(Bindings.createObjectBinding(browseTabController.searchCallable,
									browseTabController.queryString));
					Platform.runLater(() -> {
						logger.trace("setting root");
						browseTabController.frameTree.setRoot(rootItem);
						logger.trace("selecting first entry");
						browseTabController.frameTree.getSelectionModel().select(0);
						if (logger.isTraceEnabled()) {
							logger.trace("Tree creation: {}ms , {} frames",
									(System.currentTimeMillis() - start),
									browseTabController.framesTotal);
						}
					});
				}).start();
			} else {
				browseTabController.frameTree.setRoot(null);
				if (logger.isTraceEnabled()) {
					logger.trace("Tree creation: {}ms , {} frames",
							(System.currentTimeMillis() - start),
							browseTabController.framesTotal);
				}
			}
		}
		
		/**
		 * @param element
		 * @param parentsChildren
		 */
		private void createTree(final UIElement element, final ObservableList<TreeItem<UIElement>> parentsChildren) {
			final FilterableTreeItem<UIElement> treeItem = new FilterableTreeItem<>(element);
			parentsChildren.add(treeItem);
			browseTabController.framesTotal += 1;
			final List<UIElement> children = element.getChildrenRaw();
			if (children != null) {
				final ObservableList<TreeItem<UIElement>> treeItemChildren = treeItem.getInternalChildren();
				for (final UIElement child : children) {
					createTree(child, treeItemChildren);
				}
			}
			treeItem.expandedProperty().setValue(true);
		}
	}
	
	private static final class TreeFilteringRunnable implements Runnable {
		private final String filter;
		private final TreeItem<UIElement> root;
		private final TreeItem<UIElement> selectedItem;
		private final Node tableViewPlaceholderText;
		private final BrowseTabController browseTabController;
		
		private TreeFilteringRunnable(
				final String filter,
				final TreeItem<UIElement> root,
				final TreeItem<UIElement> selectedItem,
				final Node tableViewPlaceholderText,
				final BrowseTabController browseTabController) {
			this.filter = filter;
			this.root = root;
			this.selectedItem = selectedItem;
			this.tableViewPlaceholderText = tableViewPlaceholderText;
			this.browseTabController = browseTabController;
		}
		
		@Override
		public void run() {
			String str = null;
			while (!browseTabController.queriedFilter.equals(str)) {
				final long startTime = System.currentTimeMillis();
				str = browseTabController.queriedFilter;
				browseTabController.flowFactory.setHighlight(str);
				browseTabController.queryString.set(str.toUpperCase(Locale.getDefault()));
				logger.trace("filter apply: {}ms - {}", (System.currentTimeMillis() - startTime), str);
			}
			Platform.runLater(new TreeFilteringUiRunnable(str,
					filter,
					browseTabController,
					root,
					selectedItem,
					tableViewPlaceholderText));
		}
		
		private static final class TreeFilteringUiRunnable implements Runnable {
			private final String curFilter;
			private final String filter;
			private final BrowseTabController browseTabController;
			private final TreeItem<UIElement> selectedItem;
			private final Node tableViewPlaceholderText;
			private final TreeItem<UIElement> root;
			
			private TreeFilteringUiRunnable(
					final String curFilter,
					final String filter,
					final BrowseTabController browseTabController,
					final TreeItem<UIElement> root,
					final TreeItem<UIElement> selectedItem,
					final Node tableViewPlaceholderText) {
				this.curFilter = curFilter;
				this.filter = filter;
				this.browseTabController = browseTabController;
				this.root = root;
				this.selectedItem = selectedItem;
				this.tableViewPlaceholderText = tableViewPlaceholderText;
			}
			
			@Override
			public void run() {
				try {
					if (browseTabController.frameTree != null && browseTabController.queriedFilter != null) {
						final TreeView<UIElement> frameTree = browseTabController.frameTree;
						frameTree.setRoot(root);
						frameTree.getSelectionModel().select(selectedItem);
						browseTabController.tableView.setPlaceholder(tableViewPlaceholderText);
						final int selectedIndex = frameTree.getSelectionModel().getSelectedIndex();
						// scroll to slightly above the selected item
						frameTree.scrollTo(Math.max(selectedIndex - 4, 0));
						// clear tableview & path if the selected item is not visible OR re-show it when visible
						browseTabController.showInTableView(selectedIndex < 0 ? null : selectedItem);
						browseTabController.queryIdling = true;
						if (!browseTabController.queryIdling && !browseTabController.queriedFilter.equals(curFilter)) {
							//Query set, but Filter Thread is dead. -> try again
							logger.error("Query set, but Filter Thread is dead. -> try again. Does this work?");
							browseTabController.filterTree(filter);
						}
					}
				} catch (final Exception e) {
					logger.fatal(FATAL_ERROR, e);
				}
			}
		}
	}
}
