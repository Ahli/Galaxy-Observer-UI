// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.integration.ipc;

import com.ahli.interfacebuilder.integration.CommandLineParams;
import com.ahli.interfacebuilder.integration.log4j.InterProcessCommunicationAppender;
import com.ahli.interfacebuilder.ui.AppController;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Pattern;

@Log4j2
public class TcpIpSocketCommunication implements IpcCommunication {
	private final int port;
	private ServerSocket serverSocket;
	
	public TcpIpSocketCommunication(final int port) {
		this.port = port;
	}
	
	@Override
	public boolean isAvailable() {
		try (final var ignored1 = new ServerSocket(port, 4, InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }))) {
			return true;
		} catch (final IOException ignored) {
			return false;
		}
	}
	
	@Override
	public boolean sendToServer(final String... args) {
		
		try (final Socket socket = new Socket(InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }), port)) {
			try (final PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
			     final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
					     StandardCharsets.UTF_8))) {
				// sending parameters
				final String command = Arrays.toString(args);
				log.info("Sending: {}", command);
				out.println(command);
				
				// receive answers
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					if ("#BYE".equals(inputLine)) {
						return true;
					} else {
						log.info(inputLine);
					}
				}
			}
		} catch (final IOException e1) {
			log.fatal("Exception while sending parameters to primary instance.", e1);
		}
		return false;
	}
	
	@Override
	public void close() throws IOException {
		if (serverSocket != null) {
			serverSocket.close();
			serverSocket = null;
		}
	}
	
	@Override
	public IpcServerThread actAsServer() {
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
			serverSocket = new ServerSocket(port, 4, InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }));
		} catch (final UnknownHostException e) {
			log.fatal("Could not retrieve localhost address.", e);
			return null;
		} catch (final IOException e) {
			log.trace("Server socket error.", e);
			// port taken, so app is already running
			return null;
		}
		
		final IpcServerThread serverThread = new TcpIpSocketServerThread("IpcServer", serverSocket);
		serverThread.start();
		return serverThread;
	}
	
	public static final class TcpIpSocketServerThread extends Thread implements IpcServerThread {
		
		private static final Pattern COMMA_SEPARATED_REGEX_PATTERN = Pattern.compile(", ");
		private final ServerSocket serverSocket;
		private AppController appController;
		
		private TcpIpSocketServerThread(final String name, final ServerSocket serverSocket) {
			super(name);
			setDaemon(true);
			setPriority(Thread.MIN_PRIORITY);
			this.serverSocket = serverSocket;
		}
		
		@Override
		public void run() {
			while (!serverSocket.isClosed()) {
				try {
					handleConnectionAsServer(serverSocket.accept());
				} catch (final IOException e) {
					final String message = e.getMessage();
					if ("socket closed".equalsIgnoreCase(message) || "Socket is closed".equals(message) ||
							("Interrupted function call: accept failed").equals(message)) {
						// close thread, socket was closed ("socket closed" ignores case for JDK 13+)
						return;
					}
					log.error("I/O Exception while waiting for client connections.", e);
				}
			}
		}
		
		private void handleConnectionAsServer(final Socket clientSocket) {
			try (clientSocket;
			     final PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true, StandardCharsets.UTF_8);
			     final BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(),
					     StandardCharsets.UTF_8))) {
				InterProcessCommunicationAppender.setWriter(new TcpIpSocketMessageWriter(out));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					log.info("received message from client: {}", inputLine);
					final String[] params =
							COMMA_SEPARATED_REGEX_PATTERN.split(inputLine.substring(1, inputLine.length() - 1));
					executeCommand(params);
				}
			} catch (final IOException e) {
				// client closed connection
				log.trace("client closed connection.", e);
			} catch (final Exception e) {
				log.fatal("FATAL ERROR: ", e);
			} finally {
				InterProcessCommunicationAppender.setWriter(null);
			}
		}
		
		/**
		 * Executes the specified command line arguments.
		 *
		 * @param parameters
		 * 		parameters as String[]
		 */
		private void executeCommand(final String[] parameters) {
			final CommandLineParams params = new CommandLineParams(true, parameters);
			
			if (params.isHasParamCompilePath() && appController != null) {
				appController.buildStartReplayExit(params);
			} else if (params.getParamRunPath() != null && !params.getParamRunPath().isEmpty() &&
					appController != null) {
				appController.runGameWithReplay(params);
			} else {
				if (appController == null) {
					log.error("Server is not ready to process commands, yet.");
				}
				// end communication as no task was given
				InterProcessCommunicationAppender.sendTerminationSignal();
			}
		}
		
		@Override
		public void setAppController(final AppController appController) {
			this.appController = appController;
		}
	}
	
}
