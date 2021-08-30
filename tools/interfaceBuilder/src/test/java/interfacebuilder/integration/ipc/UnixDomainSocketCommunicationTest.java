package interfacebuilder.integration.ipc;

import interfacebuilder.ui.AppController;
import org.junit.jupiter.api.BeforeEach;
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
	void init() throws IOException, InterruptedException {
		serverThread = socketCommunication.actAsServer();
		serverThread.setAppController(appController);
		Thread.sleep(1000);
	}
	
	@Test
	void testSendAndReceive() {
		socketCommunication.sendToServer("TEST");
	}
	
}