// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder;

import interfacebuilder.config.AppConfiguration;
import interfacebuilder.config.ConfigService;
import interfacebuilder.config.FxmlConfiguration;
import interfacebuilder.integration.CommandLineParams;
import interfacebuilder.integration.InterProcessCommunication;
import interfacebuilder.ui.AppController;
import javafx.application.Application;
import javafx.application.Preloader;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * Interface Compiler and Builder Application.
 *
 * @author Ahli
 */

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
public class InterfaceBuilderApp extends Application {
	public static final String FATAL_ERROR = "FATAL ERROR: ";
	public static final int INTER_PROCESS_COMMUNICATION_PORT = 12317;
	private static final Logger logger = LogManager.getLogger(InterfaceBuilderApp.class);
	//	static {
	//				System.setProperty("log4j2.debug", "true");
	//	}
	private ConfigurableApplicationContext appContext;
	@Autowired
	private AppController appController;
	@Autowired
	private ConfigService configService;
	@Autowired
	private ForkJoinPool executor;
	
	public InterfaceBuilderApp() {
		// nothing to do
	}
	
	/**
	 * Entry point of the App.
	 *
	 * @param args
	 * 		command line arguments
	 */
	public static void launch(final String[] args) {
		try (final InterProcessCommunication interProcessCommunication = new InterProcessCommunication()) {
			if (interProcessCommunication.initInterProcessCommunication(args, INTER_PROCESS_COMMUNICATION_PORT)) {
				// this is the server
				logger.trace("System's Log4j2 Configuration File: {}",
						() -> System.getProperty("log4j.configurationFile"));
				logger.info("Launch arguments: {}", () -> Arrays.toString(args));
				logger.info("Max Heap Space: {}mb.", Runtime.getRuntime().maxMemory() / 1_048_576L);
				
				// set preloader while booting javafx
				System.setProperty("javafx.preloader", AppPreloader.class.getCanonicalName());
				
				// initialize JavaFX
				Application.launch(InterfaceBuilderApp.class, args);
			}
			// else: this is the client and communication with the server ended already -> exit
		} catch (final IOException e) {
			logger.error("Closing InterProcessCommunication failed", e);
		}
	}
	
	@Override
	public void init() {
		// initialize Spring
		try {
			appContext =
					SpringApplication.run(InterfaceBuilderApp.class, getParameters().getRaw().toArray(new String[0]));
		} catch (final IllegalStateException e) {
			logger.fatal(e);
			throw e;
		}
		// trigger autowiring of this JavaFX-created instance
		final AutowireCapableBeanFactory autowireCapableBeanFactory = appContext.getAutowireCapableBeanFactory();
		autowireCapableBeanFactory.autowireBean(this);
		autowireCapableBeanFactory.initializeBean(this, getClass().getName());
	}
	
	/**
	 * Called when the App is starting within the UI Thread.
	 */
	@Override
	public void start(final Stage primaryStage) throws Exception {
		Thread.currentThread().setName("UI");
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		initUi(primaryStage);
		
		final CommandLineParams startingParams = initParams();
		printVariables(startingParams);
		
		if (startingParams.isHasParamCompilePath()) {
			// command line tool build
			appController.buildStartReplayExit(primaryStage, startingParams);
		} else {
			appController.checkBaseUiUpdate();
		}
	}
	
	/**
	 * Initialize GUI.
	 *
	 * @param primaryStage
	 * 		the main App's Stage
	 * @throws IOException
	 * 		when loading the UI definition fails
	 */
	private void initUi(final Stage primaryStage) throws IOException {
		appController.initUi(primaryStage);
		logger.info("Initializing App...");
		hidePreloader();
	}
	
	/**
	 * Turns App's parameters into variables.
	 */
	private CommandLineParams initParams() {
		return new CommandLineParams(getParameters());
	}
	
	/**
	 * Prints variables into console.
	 */
	private void printVariables(final CommandLineParams params) {
		logger.info("basePath: {}", configService.getBasePath());
		logger.info("documentsPath: {}", configService.getDocumentsPath());
		final String paramCompilePath = params.getParamCompilePath();
		if (paramCompilePath != null) {
			logger.info("compile param path: {}", paramCompilePath);
			if (params.isCompileAndRun()) {
				logger.info("run after compile: true");
			}
		}
		final String paramRunPath = params.getParamRunPath();
		if (paramRunPath != null) {
			logger.info("run param path: {}", paramRunPath);
		}
	}
	
	private void hidePreloader() {
		notifyPreloader(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));
	}
	
	/**
	 * Is called when the App is closing.
	 */
	@Override
	public void stop() {
		logger.info("App is about to shut down.");
		if (!executor.isShutdown()) {
			executor.shutdownNow();
		} else {
			logger.info("Executor was already shut down.");
		}
		try {
			//noinspection ResultOfMethodCallIgnored
			executor.awaitTermination(120L, TimeUnit.SECONDS);
		} catch (final InterruptedException e) {
			logger.error("ERROR: Executor timed out waiting for Worker Theads to terminate. A Thread might run rampage.",
					e);
			Thread.currentThread().interrupt();
		}
		logger.info("App waves Goodbye!");
		appContext.stop();
	}
	
}
