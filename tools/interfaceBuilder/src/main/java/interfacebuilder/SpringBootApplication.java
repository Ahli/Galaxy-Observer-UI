// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder;

import interfacebuilder.config.AppConfiguration;
import interfacebuilder.config.FxmlConfiguration;
import interfacebuilder.integration.CommandLineParams;
import interfacebuilder.integration.InterProcessCommunication;
import javafx.application.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.util.Arrays;

import static interfacebuilder.ui.AppController.FATAL_ERROR;

// start with VM parameter: -add-opens=javafx.controls/javafx.scene.control=interfacex.builder

@EnableAutoConfiguration(excludeName = { // exclude based on beans in context on runtime
		"org.springframework.boot.autoconfigure.aop.AopAutoConfiguration", // not required
		//"org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration", // required for Resources
		//"org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration", // req for Resources
		"org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration",
		"org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration",
		//"org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration", // required for JPA Repository
		"org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration",
		//"org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration", // req
		"org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration",
		"org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration",
		//"org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration", // req
		"org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration",
		"org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration",
		//"org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration", // req
		"org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration" })
@Import({ AppConfiguration.class, FxmlConfiguration.class })
public final class SpringBootApplication {
	
	public static final int INTER_PROCESS_COMMUNICATION_PORT = 12317;
	
	private static final Logger logger = LogManager.getLogger(SpringBootApplication.class);
	
	public static void main(final String[] args) {
		try {
			// TODO app mode without GUI
			if (InterProcessCommunication.isPortFree(INTER_PROCESS_COMMUNICATION_PORT)) {
				if (!actAsServer(args)) {
					logger.warn("Failed to create Server Thread. Starting App without it...");
					launch(args, null);
				}
			} else {
				logger.info("App already running. Passing over command line arguments.");
				actAsClient(args);
			}
		} catch (final Exception e) {
			logger.error(FATAL_ERROR, e);
		}
	}
	
	private static boolean actAsClient(final String[] args) {
		if (InterProcessCommunication.sendToServer(args, INTER_PROCESS_COMMUNICATION_PORT)) {
			return true;
		} else {
			logger.error("InterProcessCommunication as Client failed. The port might not be free anymore.");
		}
		return false;
	}
	
	private static boolean actAsServer(final String[] args) {
		try (final InterProcessCommunication interProcessCommunication = new InterProcessCommunication()) {
			final Thread serverThread = interProcessCommunication.actAsServer(INTER_PROCESS_COMMUNICATION_PORT);
			if (serverThread != null) {
				launch(args, serverThread);
				return true;
			} else {
				logger.error("InterProcessCommunication failed");
			}
		} catch (final IOException e) {
			logger.error("Closing InterProcessCommunication failed", e);
		}
		return false;
	}
	
	private static void launch(final String[] args, final Thread serverThread) {
		logger.trace("System's Log4j2 Configuration File: {}", () -> System.getProperty("log4j.configurationFile"));
		logger.info(
				"Launch arguments: {}\nMax Heap Space: {}mb.",
				() -> Arrays.toString(args),
				() -> Runtime.getRuntime().maxMemory() / 1_048_576L);
		
		boolean noGui = false;
		for (final String arg : args) {
			if ("--noGUI".equalsIgnoreCase(arg)) {
				noGui = true;
				break;
			}
		}
		if (noGui) {
			launchNoGui(args, serverThread);
		} else {
			launchGui(args, serverThread);
		}
	}
	
	private static void launchNoGui(final String[] args, final Thread serverThread) {
		final NoGuiApplication app = new NoGuiApplication(args, serverThread);
		app.start();
	}
	
	private static void launchGui(final String[] args, final Thread serverThread) {
		setJavaFxPreloader(AppPreloader.class.getCanonicalName());
		Application.launch(JavafxApplication.class, argsWithServerThread(args, serverThread));
	}
	
	private static void setJavaFxPreloader(final String canonicalPath) {
		System.setProperty("javafx.preloader", canonicalPath);
	}
	
	private static String[] argsWithServerThread(final String[] args, final Thread serverThread) {
		if (serverThread != null) {
			final String[] destArray = Arrays.copyOf(args, args.length + 1);
			destArray[destArray.length - 1] =
					CommandLineParams.PARAM_PREFIX + CommandLineParams.SERVER + CommandLineParams.EQUAL +
							serverThread.getId();
			return destArray;
		}
		return args;
	}
	
}
