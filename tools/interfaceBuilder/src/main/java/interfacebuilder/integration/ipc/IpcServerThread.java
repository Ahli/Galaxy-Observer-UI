// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com
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
