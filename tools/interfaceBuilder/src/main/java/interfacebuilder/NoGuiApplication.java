package interfacebuilder;

import interfacebuilder.integration.CommandLineParams;
import interfacebuilder.integration.InterProcessCommunication;
import interfacebuilder.ui.AppController;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class NoGuiApplication {
	
	private final InterProcessCommunication.IpcServerThread serverThread;
	private final ConfigurableApplicationContext context;
	private final CommandLineParams startingParams;
	
	public NoGuiApplication(final String[] args, final Thread serverThread) {
		this.serverThread = (InterProcessCommunication.IpcServerThread) serverThread;
		
		context = new SpringApplicationBuilder(SpringBootApplication.class).build().run(args);
		
		if (serverThread != null && serverThread.isAlive()) {
			this.serverThread.setAppController(context.getBean(AppController.class));
		}
		startingParams = new CommandLineParams(false, args);
	}
	
	public void start() {
		context.publishEvent(new PrimaryStageReadyEvent(this, startingParams));
	}
	
	public void stop() {
		if (serverThread != null && serverThread.isAlive()) {
			serverThread.interrupt();
		}
		context.publishEvent(new AppClosingEvent(this));
		context.close();
	}
}
