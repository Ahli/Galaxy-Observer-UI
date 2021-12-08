// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com
package com.ahli.interfacebuilder.integration.ipc;

import java.io.IOException;

public interface IpcCommunication extends AutoCloseable {
	
	boolean isAvailable();
	
	/**
	 * Sends the arguments to the specified port. Received answers are logged.
	 * <p>
	 * This is a blocking instruction!
	 *
	 * @param args
	 * 		arguments send to the server
	 * @return true, if a connection with a server was established and stopped; else false
	 */
	boolean sendToServer(String... args);
	
	/**
	 * Create a server daemon thread and listen to the specified socket.
	 *
	 * @return a running IpcServerThread listening to the specified socket; else null
	 */
	IpcServerThread actAsServer() throws IOException;
}
