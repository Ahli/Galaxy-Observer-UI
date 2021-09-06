// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com
package interfacebuilder.integration.ipc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class UnixDomainSocketMessageWriter implements IpcMessageWriter {
	private static final Logger logger = LogManager.getLogger(UnixDomainSocketMessageWriter.class);
	
	private final SocketChannel channel;
	
	public UnixDomainSocketMessageWriter(final SocketChannel channel) {
		this.channel = channel;
	}
	
	@Override
	public void sendTerminationSignal() {
		send("#BYE");
	}
	
	@Override
	public void send(final String message) {
		final ByteBuffer buf = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
		while (buf.hasRemaining()) {
			try {
				channel.write(buf);
			} catch (final IOException e) {
				logger.error("Error while sending message to client.", e);
			}
		}
	}
}
