// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.integration.ipc;

import com.ahli.interfacebuilder.integration.CommandLineParams;
import com.ahli.interfacebuilder.integration.log4j.InterProcessCommunicationAppender;
import com.ahli.interfacebuilder.ui.AppController;
import lombok.extern.log4j.Log4j2;

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

@Log4j2
public class UnixDomainSocketCommunication implements IpcCommunication {
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
			clientChannel.configureBlocking(true);
			
			// sending parameters
			final String command = Arrays.toString(args);
			log.info("Sending: {}", command);
			
			sendMessage(clientChannel, command);
			
			final ByteBuffer byteBuffer = ByteBuffer.allocate(16194);
			while (clientChannel.isConnected()) {
				final int bytesRead = clientChannel.read(byteBuffer);
				if (bytesRead > 0) {
					byteBuffer.flip();
					
					final CharBuffer charBuffer = CharBuffer.allocate(1024);
					final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
					final StringBuilder stringBuilder = new StringBuilder(byteBuffer.limit());
					CoderResult decodeResult;
					do {
						charBuffer.clear();
						decodeResult = decoder.decode(byteBuffer, charBuffer, false);
						charBuffer.flip();
						stringBuilder.append(charBuffer);
					} while (decodeResult == CoderResult.OVERFLOW);
					
					for (final String line : stringBuilder.toString().lines().toList()) {
						if (!line.startsWith("#BYE")) {
							log.info(line);
						} else {
							return true;
						}
					}
					
					byteBuffer.clear();
				}
			}
		} catch (final IOException e) {
			log.error("Exception while sending parameters to server instance.", e);
		}
		return false;
	}
	
	private void sendMessage(final SocketChannel channel, final String message) throws IOException {
		final ByteBuffer buf = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
		while (buf.hasRemaining()) {
			channel.write(buf);
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
					log.error("Closing the old server failed.", e);
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
					log.error("Closing the new server failed.", e);
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
			
			try (final SocketChannel ignored = SocketChannel.open(UnixDomainSocketAddress.of(address.getPath()))) {
				log.trace("socket can be connected to => not orphaned socket file");
				return false;
			} catch (final IOException e) {
				log.trace("channelLock test error means that it is orphaned?", e);
			}
			
			try {
				Files.delete(address.getPath());
				return true;
			} catch (final IOException e) {
				log.error("Failed to clear socket file: {}", address.getPath(), e);
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
					log.info("Server Socket shut down.");
				} catch (final IOException e) {
					log.error("I/O Exception while waiting for client connections.", e);
				}
			}
		}
		
		private void handleConnectionAsServer(final SocketChannel channel) {
			try {
				InterProcessCommunicationAppender.setWriter(new UnixDomainSocketMessageWriter(channel));
				readMessageFromSocket(channel).ifPresent(this::handleReceivedMessage);
			} catch (final Exception e) {
				log.error("Error while handling socket's message.", e);
			} finally {
				InterProcessCommunicationAppender.setWriter(null);
			}
		}
		
		private static Optional<String> readMessageFromSocket(final SocketChannel channel) throws IOException {
			final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
			final boolean eof = channel.read(byteBuffer) == -1;
			if (eof) {
				return Optional.empty();
			}
			byteBuffer.flip();
			
			final CharBuffer charBuffer = CharBuffer.allocate(1024);
			final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
			final StringBuilder stringBuilder = new StringBuilder(byteBuffer.limit());
			CoderResult decodeResult;
			do {
				charBuffer.clear();
				decodeResult = decoder.decode(byteBuffer, charBuffer, false);
				charBuffer.flip();
				stringBuilder.append(charBuffer);
			} while (decodeResult == CoderResult.OVERFLOW);
			
			return Optional.of(stringBuilder.toString());
		}
		
		private void handleReceivedMessage(final String message) {
			log.info("received message from client: {}", message);
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
					log.error("Server is not ready to process commands, yet.");
				}
			}
			InterProcessCommunicationAppender.sendTerminationSignal();
		}
		
		@Override
		public void setAppController(final AppController appController) {
			this.appController = appController;
		}
	}
	
}
