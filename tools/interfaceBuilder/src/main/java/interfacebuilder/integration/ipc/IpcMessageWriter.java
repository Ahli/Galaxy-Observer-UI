package interfacebuilder.integration.ipc;

public interface IpcMessageWriter {
	
	void send(String message);
	
	void sendTerminationSignal();
	
}
