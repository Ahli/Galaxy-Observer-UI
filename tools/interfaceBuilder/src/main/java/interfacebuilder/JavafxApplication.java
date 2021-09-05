// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder;

import interfacebuilder.integration.CommandLineParams;
import interfacebuilder.integration.ipc.IpcServerThread;
import interfacebuilder.ui.AppController;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class JavafxApplication extends Application {
	
	private static final Logger logger = LoggerFactory.getLogger(JavafxApplication.class);
	
	private IpcServerThread serverThread;
	private ConfigurableApplicationContext context;
	
	@Override
	public void init() {
		serverThread = findServerThread(CommandLineParams.getServerThreadId(getParameters()));
		
		final ApplicationContextInitializer<GenericApplicationContext> initializer = genericApplicationContext -> {
			genericApplicationContext.registerBean(Application.class, () -> JavafxApplication.this);
			genericApplicationContext.registerBean(Parameters.class, JavafxApplication.this::getParameters);
			genericApplicationContext.registerBean(HostServices.class, JavafxApplication.this::getHostServices);
		};
		
		context = new SpringApplicationBuilder(SpringBootApplication.class).initializers(initializer)
				.build()
				.run(toArray(getParameters()));
		
		if (serverThread != null && serverThread.isAlive()) {
			serverThread.setAppController(context.getBean(AppController.class));
		}
	}
	
	@SuppressWarnings("java:S3014") // ThreadGroup is ok to be used here
	private static IpcServerThread findServerThread(final String id) {
		try {
			if (id != null && !id.isEmpty()) {
				final long idLong = Long.parseLong(id);
				
				final ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
				// HACK: the server thread is always on index 3
				final Thread[] threads = new Thread[3];
				//final Thread[] threads = new Thread[threadGroup.activeCount()];
				threadGroup.enumerate(threads, false);
				for (int i = threads.length - 1; i > 0; --i) {
					if (threads[i].getId() == idLong && threads[i] instanceof final IpcServerThread serverThread) {
						return serverThread;
					}
				}
			}
		} catch (final NumberFormatException ignored) {
			// ignored
		}
		logger.error("Failed to find IpcServerThread of id: {}", id);
		return null;
	}
	
	private static String[] toArray(final Parameters param) {
		return param != null ? param.getRaw().toArray(new String[0]) : new String[0];
	}
	
	@Override
	public void start(final Stage primaryStage) throws Exception {
		Thread.currentThread().setName("UI");
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		final CommandLineParams startingParams = new CommandLineParams(getParameters());
		hidePreloader();
		context.publishEvent(new PrimaryStageReadyEvent(primaryStage, startingParams));
	}
	
	private void hidePreloader() {
		notifyPreloader(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));
	}
	
	@Override
	public void stop() {
		if (serverThread != null && serverThread.isAlive()) {
			serverThread.interrupt();
		}
		context.publishEvent(new AppClosingEvent(this));
		context.close();
		Platform.exit();
	}
	
}
