package com.ahli.interfacebuilder.ui.progress;

import com.ahli.interfacebuilder.i18n.Messages;
import com.ahli.interfacebuilder.integration.log4j.StylizedTextAreaAppender;
import com.ahli.interfacebuilder.projects.Project;
import com.ahli.interfacebuilder.ui.FXMLSpringLoader;
import com.ahli.interfacebuilder.ui.Updateable;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.TextFlow;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Log4j2
public class ProgressController {
	public static final String FATAL_ERROR = "FATAL ERROR: ";
	
	private final List<ErrorTabController> errorTabControllers;
	private final TabPaneController tabPaneController;
	private final ApplicationContext appContext;
	private final List<Updateable> tabContentControllers;
	
	public ProgressController(final TabPaneController tabPaneController, ApplicationContext appContext) {
		this.tabPaneController = tabPaneController;
		this.appContext = appContext;
		tabPaneController.setProgressController(this);
		errorTabControllers = Collections.synchronizedList(new ArrayList<>(0));
		tabContentControllers = Collections.synchronizedList(new ArrayList<>(0));
	}
	
	public void removeErrorTabController(final ErrorTabController errorTabController) {
		errorTabControllers.remove(errorTabController);
	}
	
	/**
	 * @return
	 */
	public boolean anyErrorTrackerEncounteredError() {
		for (final ErrorTabController ctrl : errorTabControllers) {
			if (ctrl.hasEncounteredError() && ctrl.isErrorPreventsExit()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Clears the errors, so future build attempts can run.
	 */
	public void clearErrorTrackers() {
		for (final ErrorTabController ctrl : errorTabControllers) {
			if (ctrl.hasEncounteredError() && ctrl.isErrorPreventsExit()) {
				ctrl.clearError(false);
			}
		}
	}
	
	/**
	 * Adds a Tab for the specified Thread ID containing its log messages.
	 *
	 * @param threadName
	 * @param tabName
	 * @param errorPreventsExit
	 */
	public void addThreadlogTab(final String threadName, final String tabName, final boolean errorPreventsExit) {
		final ObservableList<Tab> tabs = getTabPane().getTabs();
		Tab newTab = null;
		
		// re-use existing tab with that name
		for (final Tab tab : tabs) {
			if (tab.getText().equals(tabName)) {
				newTab = tab;
				break;
			}
		}
		if (newTab == null) {
			// CASE: new tab
			newTab = new Tab(tabName);
			final TextFlow newTxtArea = new TextFlow();
			final ErrorTabController errorTabCtrl =
					new ErrorTabController(newTab, newTxtArea, true, false, errorPreventsExit);
			errorTabCtrl.setRunning(true);
			addErrorTabController(errorTabCtrl);
			
			final ScrollPane scrollPane = new ScrollPane(newTxtArea);
			scrollPane.setPannable(true);
			
			// auto-downscrolling
			scrollPane.vvalueProperty().bind(newTxtArea.heightProperty());
			
			newTab.setContent(scrollPane);
			StylizedTextAreaAppender.setWorkerTaskController(errorTabCtrl, threadName);
			
			// context menu with close option
			final ContextMenu contextMenu = new ContextMenu();
			final MenuItem closeItem = new MenuItem(Messages.getString("contextmenu.close"));
			closeItem.setOnAction(new CloseThreadlogTabAction(newTab, errorTabCtrl, this));
			contextMenu.getItems().add(closeItem);
			newTab.setContextMenu(contextMenu);
			
			// runlater needs to appear below the edits above, else it might be added before
			// which results in UI edits not in UI thread -> error
			final Tab newTabFinal = newTab;
			Platform.runLater(() -> {
				try {
					getTabPane().getTabs().add(newTabFinal);
				} catch (final Exception e) {
					log.fatal(FATAL_ERROR, e);
				}
			});
		} else {
			// CASE: recycle existing Tab
			final ErrorTabController errorTabCtrl = getErrorTabController(tabName);
			if (errorTabCtrl != null) {
				StylizedTextAreaAppender.setWorkerTaskController(errorTabCtrl, threadName);
				Platform.runLater(() -> {
					try {
						errorTabCtrl.setErrorPreventsExit(errorPreventsExit);
						errorTabCtrl.clearError(false);
						errorTabCtrl.clearWarning(false);
						errorTabCtrl.setRunning(true);
					} catch (final Exception e) {
						log.fatal(FATAL_ERROR, e);
					}
				});
			}
		}
	}
	
	/**
	 * @return
	 */
	public TabPane getTabPane() {
		return tabPaneController.getTabPane();
	}
	
	/**
	 * Adds an ErrorTabController.
	 *
	 * @param errorTabCtrl
	 */
	public synchronized void addErrorTabController(final ErrorTabController errorTabCtrl) {
		errorTabControllers.add(errorTabCtrl);
	}
	
	/**
	 * @param tabName
	 * @return
	 */
	public ErrorTabController getErrorTabController(final String tabName) {
		for (final var ctrl : errorTabControllers) {
			if (ctrl != null) {
				final Tab tab = ctrl.getTab();
				if (tab != null && tabName.equals(tab.getText())) {
					// found the correct one
					return ctrl;
				}
			}
		}
		return null;
	}
	
	/**
	 * Adds a Build Tab for the specified Thread ID containing its log messages. Build Tabs contain extra controls to
	 * re-do a build.
	 *
	 * @param threadName
	 * @param project
	 * @param errorPreventsExit
	 */
	public void addBuildTab(final String threadName, final Project project, final boolean errorPreventsExit)
			throws IOException {
		final ObservableList<Tab> tabs = getTabPane().getTabs();
		Tab newTab = null;
		String tabName = project.getProjectPath().getFileName().toString();
		BuildUiController buildUiController = null;
		
		// try to re-use existing tab with same name
		for (final Tab tab : tabs) {
			if (tab.getText().equals(tabName)) {
				newTab = tab;
				break;
			}
		}
		if (newTab != null) {
			// CASE: recycle existing Tab
			for (final var controller : tabContentControllers) {
				if (controller instanceof final BuildUiController currBuildUiCtrl) {
					final ErrorTabController errorTabCtrl = currBuildUiCtrl.getErrorTabController();
					if (errorTabCtrl != null) {
						newTab = errorTabCtrl.getTab();
						if (newTab != null && tabName.equals(newTab.getText())) {
							// found the correct one
							buildUiController = currBuildUiCtrl;
							StylizedTextAreaAppender.setWorkerTaskController(errorTabCtrl, threadName);
							Platform.runLater(() -> {
								try {
									errorTabCtrl.setErrorPreventsExit(errorPreventsExit);
									errorTabCtrl.clearError(false);
									errorTabCtrl.clearWarning(false);
									errorTabCtrl.setRunning(true);
								} catch (final Exception e) {
									log.fatal(FATAL_ERROR, e);
								}
							});
							break;
						}
					}
				}
			}
			if (buildUiController == null) {
				throw new IOException("The desired BuildUiController was not found while recycling a Tab.");
			}
		} else {
			// CASE: new tab
			newTab = new Tab(tabName);
			final FXMLSpringLoader loader = new FXMLSpringLoader(appContext);
			final AnchorPane rootNode = loader.load("classpath:view/Progress_BuildUi.fxml");
			
			newTab.setContent(rootNode);
			
			buildUiController = loader.getController();
			final ErrorTabController errorTabCtrl =
					new ErrorTabController(newTab, buildUiController.txtArea, true, false, errorPreventsExit);
			buildUiController.setProject(project);
			buildUiController.setErrorTabController(errorTabCtrl);
			tabContentControllers.add(buildUiController);
			
			StylizedTextAreaAppender.setWorkerTaskController(errorTabCtrl, threadName);
			
			// context menu with close option
			final ContextMenu contextMenu = new ContextMenu();
			final MenuItem closeItem = new MenuItem(Messages.getString("contextmenu.close"));
			closeItem.setOnAction(new CloseBuildUiTabAction(newTab, buildUiController, tabContentControllers, this));
			contextMenu.getItems().add(closeItem);
			newTab.setContextMenu(contextMenu);
			
			// runlater needs to appear below the edits above, else it might be added before
			// which results in UI edits not in UI thread -> error
			final Tab newTabFinal = newTab;
			Platform.runLater(() -> {
				try {
					getTabPane().getTabs().add(newTabFinal);
				} catch (final Exception e) {
					log.fatal(FATAL_ERROR, e);
				}
			});
		}
	}
	
	private record CloseThreadlogTabAction(Tab tab, ErrorTabController controller,
	                                       ProgressController progressController) implements EventHandler<ActionEvent> {
		
		@Override
		public void handle(final ActionEvent event) {
			tab.getTabPane().getTabs().remove(tab);
			tab.setContextMenu(null);
			progressController.removeErrorTabController(controller);
		}
	}
	
	private record CloseBuildUiTabAction(Tab tab, BuildUiController controller, List<Updateable> controllers,
	                                     ProgressController progressController) implements EventHandler<ActionEvent> {
		@Override
		public void handle(final ActionEvent event) {
			tab.getTabPane().getTabs().remove(tab);
			controllers.remove(controller);
			final ErrorTabController errorTabController = controller.getErrorTabController();
			progressController.removeErrorTabController(errorTabController);
			StylizedTextAreaAppender.unregister(errorTabController);
			controller.setErrorTabController(null);
			// for some reason this class was not garbage collected
			tab.getContextMenu().getItems().getFirst().setOnAction(null);
			tab.getContextMenu().getItems().clear();
			tab.setContextMenu(null);
		}
	}
}
