// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder;

import interfacebuilder.integration.CommandLineParams;
import interfacebuilder.integration.ipc.IpcServerThread;
import interfacebuilder.ui.AppController;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

public class NoGuiApplication {
	
	private final IpcServerThread serverThread;
	private final CommandLineParams startingParams;
	
	public NoGuiApplication(final String[] args, final IpcServerThread serverThread) {
		this.serverThread = serverThread;
		startingParams = new CommandLineParams(false, args);
		
		new SpringApplicationBuilder(SpringBootApplication.class).listeners(new OnStartupEvent()).build().run(args);
	}
	
	//	public void stop(final ConfigurableApplicationContext context) {
	//		if (serverThread != null && serverThread.isAlive()) {
	//			serverThread.interrupt();
	//		}
	//		context.publishEvent(new AppClosingEvent(this));
	//		context.close();
	//	}
	
	private final class OnStartupEvent implements ApplicationListener<ApplicationReadyEvent> {
		@Override
		public void onApplicationEvent(final ApplicationReadyEvent event) {
			
			if (serverThread != null && serverThread.isAlive()) {
				serverThread.setAppController(event.getApplicationContext().getBean(AppController.class));
			}
			
			event.getApplicationContext().publishEvent(new PrimaryStageReadyEvent(this, startingParams));
		}
	}
}
