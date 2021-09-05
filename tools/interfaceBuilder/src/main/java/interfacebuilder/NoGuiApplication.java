// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder;

import interfacebuilder.integration.CommandLineParams;
import interfacebuilder.integration.ipc.IpcServerThread;
import interfacebuilder.ui.AppController;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class NoGuiApplication {
	
	private final IpcServerThread serverThread;
	private final ConfigurableApplicationContext context;
	private final CommandLineParams startingParams;
	
	public NoGuiApplication(final String[] args, final IpcServerThread serverThread) {
		this.serverThread = serverThread;
		
		context = new SpringApplicationBuilder(SpringBootApplication.class).build().run(args);
		
		if (serverThread != null && serverThread.isAlive()) {
			this.serverThread.setAppController(context.getBean(AppController.class));
		}
		startingParams = new CommandLineParams(false, args);
	}
	
	public void start() {
		context.publishEvent(new PrimaryStageReadyEvent(this, startingParams));
		// TODO add mechanic so in the end close is called
	}
	
	public void stop() {
		if (serverThread != null && serverThread.isAlive()) {
			serverThread.interrupt();
		}
		context.publishEvent(new AppClosingEvent(this));
		context.close();
	}
}
