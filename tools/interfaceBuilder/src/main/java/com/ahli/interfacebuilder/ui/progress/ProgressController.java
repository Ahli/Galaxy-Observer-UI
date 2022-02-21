package com.ahli.interfacebuilder.ui.progress;

import com.ahli.interfacebuilder.i18n.Messages;
import com.ahli.interfacebuilder.integration.log4j.StylizedTextAreaAppender;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.text.TextFlow;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class ProgressController {
	public static final String FATAL_ERROR = "FATAL ERROR: ";
	
	private final List<ErrorTabController> errorTabControllers;
	private final TabPaneController tabPaneController;
	
	public ProgressController(final TabPaneController tabPaneController) {
		this.tabPaneController = tabPaneController;
		tabPaneController.setProgressController(this);
		errorTabControllers = new ArrayList<>(0);
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
	 */
	public void addThreadlogTab(
			final String threadName, final String tabName, final boolean errorPreventsExit) {
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
	public void addErrorTabController(final ErrorTabController errorTabCtrl) {
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
	
	private static final class CloseThreadlogTabAction implements EventHandler<ActionEvent> {
		private final Tab tab;
		private final ErrorTabController controller;
		private final ProgressController progressController;
		
		private CloseThreadlogTabAction(
				final Tab tab, final ErrorTabController controller, final ProgressController progressController) {
			this.tab = tab;
			this.controller = controller;
			this.progressController = progressController;
		}
		
		@Override
		public void handle(final ActionEvent event) {
			tab.getTabPane().getTabs().remove(tab);
			tab.setContextMenu(null);
			progressController.removeErrorTabController(controller);
		}
	}
}
