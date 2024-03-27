// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder;

import com.ahli.interfacebuilder.integration.CommandLineParams;
import com.ahli.interfacebuilder.integration.ipc.IpcServerThread;
import com.ahli.interfacebuilder.ui.AppController;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Log4j2
public class JavafxApplication extends Application {
	
	@Nullable
	private IpcServerThread serverThread;
	@Nullable
	private ConfigurableApplicationContext context;
	
	public JavafxApplication() {
		// explicit constructor
	}
	
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
		
		if (serverThread != null && serverThread.isAlive() && context != null) {
			serverThread.setAppController(context.getBean(AppController.class));
		}
		
		logAllBeans();
	}
	
	@SuppressWarnings("java:S3014") // ThreadGroup is ok to be used here
	@Nullable
	private static IpcServerThread findServerThread(@NonNull final String id) {
		try {
			if (!id.isEmpty()) {
				final long idLong = Long.parseLong(id);
				
				final ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
				// HACK: the server thread is always on index 3 (2 while debugging); shaves off like 100ms during startup
				final Thread[] threads = new Thread[4];
				//final Thread[] threads = new Thread[threadGroup.activeCount()];
				threadGroup.enumerate(threads, false);
				for (int i = threads.length - 1; i > 0; --i) {
					if (threads[i].threadId() == idLong && threads[i] instanceof final IpcServerThread serverThread) {
						return serverThread;
					}
				}
			}
		} catch (final NumberFormatException _) {
			// ignored
		}
		log.error("Failed to find IpcServerThread of id: {}", id);
		return null;
	}
	
	private static String[] toArray(@Nullable final Parameters param) {
		return param != null ? param.getRaw().toArray(new String[0]) : new String[0];
	}
	
	private void logAllBeans() {
		if (context != null) {
			final String[] allBeanNames = context.getBeanDefinitionNames();
			log.trace("Spring Beans created: {}", (Object) allBeanNames);
		}
	}
	
	@Override
	public void start(final Stage primaryStage) {
		Thread.currentThread().setName("UI");
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		final CommandLineParams startingParams = new CommandLineParams(getParameters());
		hidePreloader();
		// fix app freezing when windows is made too small in browse tab
		primaryStage.setMinHeight(50);
		primaryStage.setMinWidth(50);
		if (context != null) {
			context.publishEvent(new PrimaryStageReadyEvent(primaryStage, startingParams));
		}
	}
	
	private void hidePreloader() {
		notifyPreloader(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));
	}
	
	@Override
	public void stop() {
		if (serverThread != null && serverThread.isAlive()) {
			serverThread.interrupt();
		}
		if (context != null) {
			context.publishEvent(new AppClosingEvent(this));
			context.close();
		}
		Platform.exit();
	}
	
}
