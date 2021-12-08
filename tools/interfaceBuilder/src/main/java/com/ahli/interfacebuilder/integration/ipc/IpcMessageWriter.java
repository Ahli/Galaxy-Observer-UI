// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com
package com.ahli.interfacebuilder.integration.ipc;

public interface IpcMessageWriter {
	
	void send(String message);
	
	void sendTerminationSignal();
	
}
