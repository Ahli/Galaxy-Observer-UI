// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.integration.log4j;

import gnu.trove.map.hash.THashMap;
import interfacebuilder.ui.progress.ErrorTabController;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * TextAreaAppender for Log4j2. Source: http://blog.pikodat.com/2015/10/11/frontend-logging-with-javafx/ , modified for
 * org.fxmisc.richtext.StyleClassedTextArea: Ahli
 * <p>
 * If this Appender does not work, then the Log4j2Plugins.dat might not have been created.
 */
@Plugin (name = "StylizedTextAreaAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE,
         printObject = true)
public final class StylizedTextAreaAppender extends AbstractAppender {
	/* THashMap is more memory efficient than Java's one */
	private static final Map<String, ErrorTabController> workerTaskControllers = new THashMap<>();
	private static ErrorTabController generalController;
	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	private final Lock readLock = rwLock.readLock();
	
	/**
	 * @param name
	 * @param filter
	 * @param layout
	 * @param ignoreExceptions
	 */
	protected StylizedTextAreaAppender(final String name, final Filter filter,
			final Layout<? extends Serializable> layout, final boolean ignoreExceptions, final Property... properties) {
		super(name, filter, layout, ignoreExceptions, properties);
	}
	
	/**
	 * Factory method. Log4j will parse the configuration and call this factory method to construct the appender with
	 * the configured attributes.
	 *
	 * @param name
	 * 		Name of appender
	 * @param layout
	 * 		Log layout of appender
	 * @param filter
	 * 		Filter for appender
	 * @return The TextAreaAppender
	 */
	@PluginFactory
	public static StylizedTextAreaAppender createAppender(@PluginAttribute ("name") final String name,
			@PluginElement ("Layout") final Layout<? extends Serializable> layout,
			@PluginElement ("Filter") final Filter filter) {
		if (name == null) {
			LOGGER.error("No name provided for StylizedTextAreaAppender");
			return null;
		}
		final var resultLayout = layout == null ? PatternLayout.createDefaultLayout() : layout;
		return new StylizedTextAreaAppender(name, filter, resultLayout, true, Property.EMPTY_ARRAY);
	}
	
	
	/**
	 * @param controller
	 */
	public static void setGeneralController(final ErrorTabController controller) {
		StylizedTextAreaAppender.generalController = controller;
	}
	
	/**
	 * @param controller
	 * @param threadName
	 */
	public static void setWorkerTaskController(final ErrorTabController controller, final String threadName) {
		workerTaskControllers.put(threadName, controller);
	}
	
	/**
	 * @param threadName
	 */
	public static void finishedWork(final String threadName) {
		final ErrorTabController ctrl = getWorkerTaskController(threadName);
		Platform.runLater(() -> ctrl.setRunning(false));
	}
	
	/**
	 * @param threadName
	 * @return
	 */
	private static ErrorTabController getWorkerTaskController(final String threadName) {
		return workerTaskControllers.getOrDefault(threadName, generalController);
	}
	
	/**
	 * This method is where the appender does the work.
	 *
	 * @param event
	 * 		Log event with log data
	 */
	@Override
	public void append(final LogEvent event) {
		readLock.lock();
		
		// append log text to TextArea
		try {
			final ErrorTabController controller = getWorkerTaskController(event.getThreadName());
			if (controller != null) {
				final TextFlow txtArea = controller.getTextArea();
				final String message = new String(getLayout().toByteArray(event), StandardCharsets.UTF_8);
				final Level level = event.getLevel();
				
				Platform.runLater(() -> {
					try {
						final Text text = new Text(message);
						text.getStyleClass().add(level.toString());
						text.setFontSmoothingType(FontSmoothingType.LCD);
						final ObservableList<Node> children = txtArea.getChildren();
						children.add(text);
						
						if (level == Level.ERROR || level == Level.FATAL) {
							controller.reportError();
						} else if (level == Level.WARN) {
							controller.reportWarning();
						}
						
						if (children.size() > 2000) {
							children.remove(0);
						}
					} catch (final Exception e) {
						System.err.println("Error while append to TextArea: " + e.getMessage());
					}
				});
			}
		} catch (final IllegalStateException ex) {
			ex.printStackTrace();
		} finally {
			readLock.unlock();
		}
	}
}
