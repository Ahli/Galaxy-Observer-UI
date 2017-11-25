package application.util.logger.log4j2plugin;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.fxmisc.richtext.StyleClassedTextArea;

import javafx.application.Platform;

/**
 * TextAreaAppender for Log4j2. Source:
 * http://blog.pikodat.com/2015/10/11/frontend-logging-with-javafx/ , modified
 * for org.fxmisc.richtext.StyleClassedTextArea: Ahli
 */
@Plugin(name = "StylizedTextAreaAppender", category = "Core", elementType = "appender", printObject = true)
public final class StylizedTextAreaAppender extends AbstractAppender {
	
	private static StyleClassedTextArea textArea;
	private static Map<Long, StyleClassedTextArea> specialTextAreas = new HashMap<>();
	
	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	private final Lock readLock = rwLock.readLock();
	
	/**
	 * 
	 * @param name
	 * @param filter
	 * @param layout
	 * @param ignoreExceptions
	 */
	protected StylizedTextAreaAppender(final String name, final Filter filter,
			final Layout<? extends Serializable> layout, final boolean ignoreExceptions) {
		super(name, filter, layout, ignoreExceptions);
	}
	
	/**
	 * This method is where the appender does the work.
	 *
	 * @param event
	 *            Log event with log data
	 */
	@Override
	public void append(final LogEvent event) {
		readLock.lock();
		
		// append log text to TextArea
		try {
			final String message = new String(getLayout().toByteArray(event), StandardCharsets.UTF_8);
			final Level level = event.getLevel();
			final long id = event.getThreadId();
			
			Platform.runLater(() -> {
				try {
					final StyleClassedTextArea txtArea = getTextArea(id);
					if (txtArea != null) {
						final int length = txtArea.getLength();
						txtArea.appendText(message);
						txtArea.setStyleClass(length, txtArea.getLength(), level.toString());
					}
				} catch (final Throwable t) {
					System.err.println("Error while append to TextArea: " + t.getMessage());
				}
			});
		} catch (final IllegalStateException ex) {
			ex.printStackTrace();
		} finally {
			readLock.unlock();
		}
	}
	
	/**
	 * Factory method. Log4j will parse the configuration and call this factory
	 * method to construct the appender with the configured attributes.
	 *
	 * @param name
	 *            Name of appender
	 * @param layout
	 *            Log layout of appender
	 * @param filter
	 *            Filter for appender
	 * @return The TextAreaAppender
	 */
	@PluginFactory
	public static StylizedTextAreaAppender createAppender(@PluginAttribute("name") final String name,
			@PluginElement("Layout") Layout<? extends Serializable> layout,
			@PluginElement("Filter") final Filter filter) {
		if (name == null) {
			LOGGER.error("No name provided for StylizedTextAreaAppender");
			return null;
		}
		if (layout == null) {
			layout = PatternLayout.createDefaultLayout();
		}
		return new StylizedTextAreaAppender(name, filter, layout, true);
	}
	
	/**
	 * Set TextArea to append.
	 *
	 * @param textArea
	 *            TextArea to append
	 */
	public static void setTextArea(final StyleClassedTextArea textArea) {
		StylizedTextAreaAppender.textArea = textArea;
	}
	
	/**
	 * Set TextArea to append.
	 *
	 * @param textArea
	 *            TextArea to append
	 */
	public static void setSpecialTextArea(final StyleClassedTextArea textArea, final long id) {
		StylizedTextAreaAppender.specialTextAreas.put(id, textArea);
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	private StyleClassedTextArea getTextArea(final long id) {
		return specialTextAreas.getOrDefault(id, textArea);
	}
}
