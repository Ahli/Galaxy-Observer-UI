// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.integration.ipc;

import interfacebuilder.integration.CommandLineParams;
import interfacebuilder.integration.log4j.InterProcessCommunicationAppender;
import interfacebuilder.ui.AppController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

public class UnixDomainSocketCommunication implements AutoCloseable {
	private static final Logger logger = LogManager.getLogger(UnixDomainSocketCommunication.class);
	private final UnixDomainSocketAddress address;
	private ServerSocketChannel server;
	
	public UnixDomainSocketCommunication(final Path socketFileAddress) {
		address = UnixDomainSocketAddress.of(socketFileAddress);
	}
	
	public static boolean isAvailable(final Path path) {
		if (Files.exists(path)) {
			try (final FileChannel channelLock = FileChannel.open(path, StandardOpenOption.APPEND)) {
				logger.trace("socket file exists and can be locked => orphaned file");
			} catch (final IOException e) {
				logger.trace("channelLock test error", e);
				return false;
			}
			try {
				Files.delete(path);
				return true;
			} catch (final IOException e) {
				logger.error("Failed to clear socket file: {}", path, e);
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Sends the arguments to the server. Received answers are logged.
	 * <p>
	 * This is a blocking instruction!
	 *
	 * @param args
	 * @return true, if a connection with a server was established and stopped; else false
	 */
	public boolean sendToServer(final String[] args) {
		
		try (final var clientChannel = SocketChannel.open(address)) {
			// sending parameters
			final String command = Arrays.toString(args);
			logger.info("Sending: {}", command);
			clientChannel.write(ByteBuffer.wrap(command.getBytes(StandardCharsets.UTF_8)));
			
			// TODO wait for answers
		} catch (final IOException e) {
			logger.error("Exception while sending parameters to primary instance.", e);
		}
		
		//		try (final Socket socket = new Socket(InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }), port)) {
		//			try (final PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
		//			     final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
		//					     StandardCharsets.UTF_8))) {
		//				// sending parameters
		//				final String command = Arrays.toString(args);
		//				logger.info("Sending: {}", command);
		//				out.println(command);
		//
		//				// receive answers
		//				String inputLine;
		//				while ((inputLine = in.readLine()) != null) {
		//					if ("#BYE".equals(inputLine)) {
		//						return true;
		//					} else {
		//						logger.info(inputLine);
		//					}
		//				}
		//			}
		//		} catch (final IOException e1) {
		//			logger.error("Exception while sending parameters to primary instance.", e1);
		//		}
		return false;
	}
	
	/**
	 * @throws IOException
	 */
	@Override
	public void close() throws IOException {
		if (server != null) {
			server.close();
			server = null;
			Files.deleteIfExists(address.getPath());
		}
	}
	
	/**
	 * Create a server daemon thread and listen to the specified Unix Domain Socket file address.
	 *
	 * @return a running IpcServerThread listening to the specified socket
	 * @throws IOException
	 * 		- If an I/O error occurs
	 * @throws AlreadyBoundException
	 * 		– If the socket is already bound
	 */
	public IpcServerThread actAsServer() throws IOException {
		// ensure that a pre-existing socket file won't be deleted
		ServerSocketChannel newServer = null;
		try {
			if (server != null) {
				try {
					server.close();
				} catch (final IOException e) {
					logger.error("Closing the old server failed.", e);
				}
				server = null;
			}
			newServer = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
			newServer.configureBlocking(true);
			newServer.bind(address);
			server = newServer;
			newServer = null;
		} finally {
			if (newServer != null) {
				try {
					newServer.close();
				} catch (final IOException e) {
					logger.error("Closing the new server failed.", e);
				}
			}
		}
		
		final IpcServerThread serverThread = new UnixDomainSocketServerThread("IpcServer", server);
		serverThread.start();
		return serverThread;
	}
	
	public static final class UnixDomainSocketServerThread extends Thread implements IpcServerThread {
		
		private static final Pattern COMMA_SEPARATED_REGEX_PATTERN = Pattern.compile(", ");
		private final ServerSocketChannel server;
		private AppController appController;
		
		public UnixDomainSocketServerThread(final String name, final ServerSocketChannel server) {
			super(name);
			setDaemon(true);
			setPriority(Thread.MIN_PRIORITY);
			this.server = server;
		}
		
		@Override
		public void run() {
			while (server.isOpen()) {
				try (final SocketChannel channel = server.accept()) {
					handleConnectionAsServer(channel);
				} catch (final AsynchronousCloseException ignored) {
					logger.info("Server Socket shut down.");
				} catch (final IOException e) {
					logger.error("I/O Exception while waiting for client connections.", e);
				}
			}
		}
		
		private void handleConnectionAsServer(final SocketChannel channel) {
			try {
				readMessageFromSocket(channel).ifPresent(this::handleReceivedMessage);
			} catch (final Exception e) {
				logger.error("Error while handling socket's message.", e);
			} finally {
				InterProcessCommunicationAppender.setPrintWriter(null);
			}
		}
		
		private static Optional<String> readMessageFromSocket(final SocketChannel channel) throws IOException {
			final ByteBuffer buffer = ByteBuffer.allocate(1024);
			final int bytesRead = channel.read(buffer);
			if (bytesRead < 0) {
				return Optional.empty();
			}
			
			final byte[] bytes = new byte[bytesRead];
			buffer.flip();
			buffer.get(bytes);
			return Optional.of(new String(bytes));
		}
		
		private void handleReceivedMessage(final String message) {
			logger.info("received message from client: {}", message);
			final String[] params = COMMA_SEPARATED_REGEX_PATTERN.split(message.substring(1, message.length() - 1));
			executeCommand(params);
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
