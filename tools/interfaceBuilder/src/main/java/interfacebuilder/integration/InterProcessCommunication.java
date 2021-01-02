// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.integration;

import interfacebuilder.integration.log4j.InterProcessCommunicationAppender;
import interfacebuilder.ui.AppController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import java.util.List;

public class InterProcessCommunication implements AutoCloseable {
	private static final Logger logger = LogManager.getLogger(InterProcessCommunication.class);
	private AppController appController;
	private ServerSocket serverSocket;
	
	public InterProcessCommunication() {
		// nothing to do
	}
	
	public InterProcessCommunication(final AppController appController) {
		this.appController = appController;
	}
	
	public void setAppController(final AppController appController) {
		this.appController = appController;
	}
	
	/**
	 * @throws IOException
	 */
	@Override
	public void close() throws IOException {
		if (serverSocket != null) {
			serverSocket.close();
		}
	}
	
	/**
	 * Initiates the communication between processes.
	 *
	 * @param args
	 * @param port
	 * @return true, if this instance acts as the server; else false
	 */
	public boolean initInterProcessCommunication(final String[] args, final int port) {
		try {
			serverSocket = new ServerSocket(port, 4, InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }));
		} catch (final UnknownHostException e) {
			logger.fatal("Could not retrieve localhost address.", e);
			return false;
		} catch (final IOException e) {
			if (logger.isTraceEnabled()) {
				logger.trace("Server socket error.", e);
			}
			// port taken, so app is already running
			logger.info("App already running. Passing over command line arguments.");
			
			try (final Socket socket = new Socket(InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }), port)) {
				try (final PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
				     final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
						     StandardCharsets.UTF_8))) {
					// sending parameters
					final String command = Arrays.toString(args);
					logger.info("Sending: {}", command);
					out.println(command);
					
					// receive answers
					String inputLine;
					while ((inputLine = in.readLine()) != null) {
						if ("#BYE".equals(inputLine)) {
							return false;
						} else {
							logger.info(inputLine);
						}
					}
				}
			} catch (final IOException e1) {
				logger.fatal("Exception while sending parameters to primary instance.", e1);
			}
			return false;
		}
		
		final Thread serverThread = new Thread(() -> {
			Socket clientSocket;
			while (true) {
				try {
					clientSocket = serverSocket.accept();
					handleConnection(clientSocket);
				} catch (final IOException e) {
					final String message = e.getMessage();
					if ("socket closed".equalsIgnoreCase(message) || "Socket is closed".equals(message) ||
							("Interrupted function call: accept failed").equals(message)) {
						// close thread, socket was closed ("socket closed" ignores case for JDK 13+)
						return;
					}
					logger.error("I/O Exception while waiting for client connections.", e);
				}
			}
		});
		serverThread.setName("IPCserver");
		serverThread.setDaemon(true);
		serverThread.setPriority(Thread.MIN_PRIORITY);
		serverThread.start();
		return true;
	}
	
	private void handleConnection(final Socket clientSocket) throws IOException {
		try (clientSocket;
		     final PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true, StandardCharsets.UTF_8);
		     final BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(),
				     StandardCharsets.UTF_8))) {
			InterProcessCommunicationAppender.setPrintWriter(out);
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				logger.info("received message from client: {}", inputLine);
				final List<String> params = Arrays.asList(inputLine.substring(1, inputLine.length() - 1).split(", "));
				executeCommand(params);
			}
		} catch (final IOException e) {
			// client closed connection
			if (logger.isTraceEnabled()) {
				logger.trace("client closed connection.", e);
			}
		} finally {
			InterProcessCommunicationAppender.setPrintWriter(null);
		}
	}
	
	/**
	 * Executes the specified command line arguments.
	 *
	 * @param paramList
	 */
	private void executeCommand(final List<String> paramList) {
		final CommandLineParams params = new CommandLineParams(paramList.toArray(new String[0]));
		params.setParamsOriginateFromExternalSource(true);
		
		if (params.isHasParamCompilePath()) {
			appController.buildStartReplayExit(appController.getPrimaryStage(), params);
		} else {
			// end communication as no task was given
			InterProcessCommunicationAppender.sendTerminationSignal();
		}
	}
}
