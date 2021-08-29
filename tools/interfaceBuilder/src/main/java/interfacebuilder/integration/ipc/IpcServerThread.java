package interfacebuilder.integration.ipc;

import interfacebuilder.ui.AppController;

public interface IpcServerThread {
	void setAppController(AppController appController);
	
	// interesting API methods from Thread to avoid casting:
	void start();
	
	void interrupt();
	
	long getId();
	
	boolean isAlive();
}
