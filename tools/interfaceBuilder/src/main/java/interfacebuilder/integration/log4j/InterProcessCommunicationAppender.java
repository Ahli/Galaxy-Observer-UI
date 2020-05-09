// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.integration.log4j;

import interfacebuilder.InterfaceBuilderApp;
import javafx.application.Platform;
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

import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * If this Appender does not work, then the Log4j2Plugins.dat might not have been created.
 */
@Plugin(name = "InterProcessCommunicationAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE,
        printObject = true)
public final class InterProcessCommunicationAppender extends AbstractAppender {
	private static PrintWriter printWriter;
	private static InterProcessCommunicationAppender instance;
	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	private final Lock readLock = rwLock.readLock();
	
	/**
	 * @param name
	 * @param filter
	 * @param layout
	 * @param ignoreExceptions
	 */
	protected InterProcessCommunicationAppender(final String name, final Filter filter,
			final Layout<? extends Serializable> layout, final boolean ignoreExceptions, final Property... properties) {
		super(name, filter, layout, ignoreExceptions, properties);
		if (instance == null) {
			instance = this;
		} else {
			System.err.println("INTER PROCESS COMMUNICATION APPENDER instantiated multiple times!");
		}
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
	public static InterProcessCommunicationAppender createAppender(@PluginAttribute("name") final String name,
			@PluginElement("Layout") final Layout<? extends Serializable> layout,
			@PluginElement("Filter") final Filter filter) {
		if (name == null) {
			LOGGER.error("No name provided for InterProcessCommunicationAppender");
			return null;
		}
		final var resultLayout = layout == null ? PatternLayout.createDefaultLayout() : layout;
		return new InterProcessCommunicationAppender(name, filter, resultLayout, true, Property.EMPTY_ARRAY);
	}
	
	public static void setPrintWriter(final PrintWriter printWriter) {
		InterProcessCommunicationAppender.printWriter = printWriter;
	}
	
	public static void sendTerminationSignal() {
		if (printWriter != null) {
			final InterProcessCommunicationAppender instance = getInstance();
			if (instance != null) {
				printWriter.println("#BYE");
			}
		}
	}
	
	private static InterProcessCommunicationAppender getInstance() {
		return instance;
	}
	
	/**
	 * This method is where the appender does the work.
	 *
	 * @param event
	 * 		Log event with log data
	 */
	@Override
	public void append(final LogEvent event) {
		if (printWriter != null) {
			readLock.lock();
			
			// send message to client
			try {
				if (InterfaceBuilderApp.javaFxInitialized) {
					final String message = new String(getLayout().toByteArray(event), StandardCharsets.UTF_8);
					Platform.runLater(() -> {
						try {
							if (printWriter != null) {
								printWriter.print(message);
								printWriter.flush();
							}
						} catch (final Exception e) {
							System.err.println("Error while sending message to IPC client: " + e.getMessage());
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
}
