package interfacebuilder.integration.ipc;

import java.io.PrintWriter;

public class TcpIpSocketMessageWriter implements IpcMessageWriter {
	
	private final PrintWriter printWriter;
	
	public TcpIpSocketMessageWriter(final PrintWriter printWriter) {
		this.printWriter = printWriter;
	}
	
	@Override
	public void send(final String message) {
		printWriter.print(message);
		printWriter.flush();
	}
	
	@Override
	public void sendTerminationSignal() {
		printWriter.println("#BYE");
	}
	
}
