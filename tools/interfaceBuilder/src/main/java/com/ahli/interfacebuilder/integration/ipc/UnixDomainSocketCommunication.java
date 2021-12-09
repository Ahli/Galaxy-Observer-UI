// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.integration.ipc;

import com.ahli.interfacebuilder.integration.log4j.InterProcessCommunicationAppender;
import com.ahli.interfacebuilder.integration.CommandLineParams;
import com.ahli.interfacebuilder.ui.AppController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

public class UnixDomainSocketCommunication implements IpcCommunication {
	private static final Logger logger = LogManager.getLogger(UnixDomainSocketCommunication.class);
	private final UnixDomainSocketAddress address;
	private ServerSocketChannel server;
	
	public UnixDomainSocketCommunication(final Path socketFileAddress) {
		address = UnixDomainSocketAddress.of(socketFileAddress);
	}
	
	/**
	 * Sends the arguments to the server. Received answers are logged.
	 * <p>
	 * This is a blocking instruction!
	 *
	 * @param args
	 * @return true, if a connection with a server was established and stopped; else false
	 */
	@Override
	public boolean sendToServer(final String... args) {
		
		try (final SocketChannel clientChannel = SocketChannel.open(address)) {
			// sending parameters
			final String command = Arrays.toString(args);
			logger.info("Sending: {}", command);
			
			sendMessage(clientChannel, command);
			
			// TODO messages are not properly received
			final ByteBuffer buffer = ByteBuffer.allocate(1024);
			//int bytesRead;
			while ((/*bytesRead =*/ clientChannel.read(buffer)) != -1) {
				//				byte[] bytes = new byte[bytesRead];
				//				buffer.flip();
				//				buffer.get(bytes);
				//				String message = new String(bytes);
				final String message = StandardCharsets.UTF_8.decode(buffer).toString();
				message.lines().forEach(this::handleServerAnswer);
			}
			return true;
		} catch (final IOException e) {
			logger.error("Exception while sending parameters to primary instance.", e);
		}
		return false;
	}
	
	private void sendMessage(final SocketChannel channel, final String message) throws IOException {
		final ByteBuffer buf = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
		while (buf.hasRemaining()) {
			channel.write(buf);
		}
	}
	
	private void handleServerAnswer(final String line) {
		if (!line.startsWith("#BYE")) {
			logger.info(line);
		}
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
	@Override
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
			// clears orphaned files
			isAvailable();
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
	
	@Override
	public boolean isAvailable() {
		if (Files.exists(address.getPath())) {
			
			//			try (final FileChannel channelLock = FileChannel.open(address.getPath(), StandardOpenOption.APPEND)) {
			//				logger.info("socket file exists and can be locked => orphaned file");
			//			} catch (final IOException e) {
			//				logger.info("channelLock test error", e);
			//				return false;
			//			}
			try (final SocketChannel ignored = SocketChannel.open(UnixDomainSocketAddress.of(address.getPath()))) {
				UnixDomainSocketCommunication.logger.info("socket can be connected to => not orphaned socket file");
				return false;
			} catch (final IOException e) {
				UnixDomainSocketCommunication.logger.info("channelLock test error means that it is orphaned?", e);
			}
			
			try {
				Files.delete(address.getPath());
				return true;
			} catch (final IOException e) {
				UnixDomainSocketCommunication.logger.error("Failed to clear socket file: {}", address.getPath(), e);
			}
			return false;
		}
		return true;
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
				InterProcessCommunicationAppender.setWriter(new UnixDomainSocketMessageWriter(channel));
				readMessageFromSocket(channel).ifPresent(this::handleReceivedMessage);
			} catch (final Exception e) {
				logger.error("Error while handling socket's message.", e);
			} finally {
				InterProcessCommunicationAppender.setWriter(null);
			}
		}
		
		private static Optional<String> readMessageFromSocket(final SocketChannel channel) throws IOException {
			//			final ByteBuffer buffer = ByteBuffer.allocate(1024);
			//			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			//			int bytesRead;
			
			//			while ((bytesRead = channel.read(buffer)) != -1) {
			//				//				byte[] bytes = new byte[bytesRead];
			//				//				buffer.flip();
			//				//				buffer.get(bytes);
			//				//				String message = new String(bytes);
			//				// use StringBuffer?
			//				baos.write(buffer.array(), 0, bytesRead);
			//				buffer.clear();
			//				//				String message = StandardCharsets.UTF_8.decode(buffer).toString();
			//			}
			//			final String message = baos.toString(StandardCharsets.UTF_8);
			//			return Optional.of(message);
			
			
			//			ByteBuffer buffer = ByteBuffer.allocate(2);
			//			int bytesRead;
			//			while ((bytesRead = channel.read(buffer)) != -1) {
			//				if(bytesRead == buffer.capacity()) {
			//					buffer = ByteBuffer.allocate(buffer.capacity() + 1024);
			//				}
			//			}
			//			String message = StandardCharsets.UTF_8.decode(buffer).toString();
			//			return Optional.of(message);
			
			
			final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
			final boolean eof = channel.read(byteBuffer) == -1;
			byteBuffer.flip();
			logger.info("Read {} bytes from socket", byteBuffer.limit());
			
			final CharBuffer charBuffer = CharBuffer.allocate(1024);
			final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
			final StringBuilder stringBuilder = new StringBuilder(byteBuffer.limit());
			CoderResult decodeResult;
			do {
				charBuffer.clear();
				decodeResult = decoder.decode(byteBuffer, charBuffer, false);
				charBuffer.flip();
				logger.info("decoded {} chars from byte buffer", charBuffer.length());
				stringBuilder.append(charBuffer);
			} while (decodeResult == CoderResult.OVERFLOW);
			
			return Optional.of(stringBuilder.toString());
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