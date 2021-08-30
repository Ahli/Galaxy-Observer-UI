// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.integration.ipc;

import interfacebuilder.integration.CommandLineParams;
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
import java.util.regex.Pattern;

public class TcpIpSocketCommunication implements AutoCloseable {
	private static final Logger logger = LogManager.getLogger(TcpIpSocketCommunication.class);
	private ServerSocket serverSocket;
	
	public static boolean isPortFree(final int port) {
		try (final var ignored1 = new ServerSocket(port, 4, InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }))) {
			return true;
		} catch (final IOException ignored) {
			return false;
		}
	}
	
	/**
	 * Sends the arguments to the specified port. Received answers are logged.
	 * <p>
	 * This is a blocking instruction!
	 *
	 * @param port
	 * @param args
	 * @return true, if a connection with a server was established and stopped; else false
	 */
	public static boolean sendToServer(final int port, final String... args) {
		
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
						return true;
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
	
	/**
	 * @throws IOException
	 */
	@Override
	public void close() throws IOException {
		if (serverSocket != null) {
			serverSocket.close();
			serverSocket = null;
		}
	}
	
	/**
	 * Create a server daemon thread and listen to the specified port.
	 *
	 * @param port
	 * @return a running IpcServerThread listening to the specified socket; else null
	 */
	public IpcServerThread actAsServer(final int port) {
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
			serverSocket = new ServerSocket(port, 4, InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }));
		} catch (final UnknownHostException e) {
			logger.fatal("Could not retrieve localhost address.", e);
			return null;
		} catch (final IOException e) {
			logger.trace("Server socket error.", e);
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
					logger.error("I/O Exception while waiting for client connections.", e);
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
					logger.info("received message from client: {}", inputLine);
					final String[] params =
							COMMA_SEPARATED_REGEX_PATTERN.split(inputLine.substring(1, inputLine.length() - 1));
					executeCommand(params);
				}
			} catch (final IOException e) {
				// client closed connection
				logger.trace("client closed connection.", e);
			} catch (final Exception e) {
				logger.fatal("FATAL ERROR: ", e);
			} finally {
				InterProcessCommunicationAppender.setWriter(null);
			}
		}
		
		/**
		 * Executes the specified command line arguments.
		 *
		 * @param parameters
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
					logger.error("Server is not ready to process commands, yet.");
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
