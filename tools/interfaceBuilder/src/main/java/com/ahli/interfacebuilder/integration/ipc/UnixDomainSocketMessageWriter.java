// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com
package com.ahli.interfacebuilder.integration.ipc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class UnixDomainSocketMessageWriter implements IpcMessageWriter {
	private final SocketChannel channel;
	
	public UnixDomainSocketMessageWriter(final SocketChannel channel) {
		this.channel = channel;
	}
	
	@Override
	public void sendTerminationSignal() throws IOException {
		send("#BYE");
	}
	
	@Override
	public void send(final String message) throws IOException {
		final ByteBuffer buf = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
		while (buf.hasRemaining()) {
			channel.write(buf);
		}
	}
}
