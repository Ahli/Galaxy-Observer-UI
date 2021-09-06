// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com
package interfacebuilder.integration.ipc;

import interfacebuilder.ui.AppController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.mockito.Mockito.mock;

class UnixDomainSocketCommunicationTest {
	
	private final Path socketPath =
			Path.of(System.getProperty("user.home") + File.separator + ".GalaxyObsUI" + File.separator +
					"test_ipc.socket");
	
	private final UnixDomainSocketCommunication socketCommunication = new UnixDomainSocketCommunication(socketPath);
	private final AppController appController = mock(AppController.class);
	private IpcServerThread serverThread;
	
	@BeforeEach
	void init() throws IOException {
		serverThread = socketCommunication.actAsServer();
		serverThread.setAppController(appController);
	}
	
	@Disabled
	@Test
	void testSendAndReceive() {
		socketCommunication.sendToServer("TEST");
	}
	
}