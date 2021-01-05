package interfacebuilder;

import interfacebuilder.integration.CommandLineParams;
import interfacebuilder.integration.InterProcessCommunication;
import interfacebuilder.ui.AppController;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class JavafxApplication extends Application {
	
	private InterProcessCommunication.IpcServerThread serverThread;
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
	
	private InterProcessCommunication.IpcServerThread findServerThread(final String id) {
		try {
			if (id != null && !id.isEmpty()) {
				final long idLong = Long.parseLong(id);
				
				// getAllStackTraces() is not optimal as it creates a lot of waste :(
				for (final Thread thread : Thread.getAllStackTraces().keySet()) {
					if (thread.getId() == idLong && thread instanceof InterProcessCommunication.IpcServerThread) {
						return (InterProcessCommunication.IpcServerThread) thread;
					}
				}
			}
		} catch (final NumberFormatException ignored) {
			// ignored
		}
		return null;
	}
	
	private String[] toArray(final Parameters param) {
		return param != null ? param.getRaw().toArray(new String[0]) : new String[0];
	}
	
	@Override
	public void start(final Stage primaryStage) throws Exception {
		super.stop();
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
	
	public static class PrimaryStageReadyEvent extends ApplicationEvent {
		
		private final CommandLineParams startingParams;
		
		public PrimaryStageReadyEvent(
				final Stage source, final CommandLineParams startingParams) {
			super(source);
			this.startingParams = startingParams;
		}
		
		public Stage getStage() {
			return (Stage) getSource();
		}
		
		public CommandLineParams getStartingParams() {
			return startingParams;
		}
	}
	
	public static class AppClosingEvent extends ApplicationEvent {
		
		public AppClosingEvent(final Object source) {
			super(source);
		}
	}
}
