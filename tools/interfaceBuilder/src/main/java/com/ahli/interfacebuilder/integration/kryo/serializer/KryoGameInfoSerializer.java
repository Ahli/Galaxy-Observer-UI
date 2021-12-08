// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.integration.kryo.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.ahli.interfacebuilder.integration.kryo.KryoGameInfo;

public class KryoGameInfoSerializer extends Serializer<KryoGameInfo> {
	
	@Override
	public void write(final Kryo kryo, final Output output, final KryoGameInfo gameInfo) {
		output.setVariableLengthEncoding(false);
		output.writeInts(gameInfo.getVersion(), 0, 4, true);
		output.writeString(gameInfo.getGameName());
		output.writeBoolean(gameInfo.isPtr());
	}
	
	@Override
	public KryoGameInfo read(final Kryo kryo, final Input input, final Class<? extends KryoGameInfo> type) {
		input.setVariableLengthEncoding(false);
		final int[] version = input.readInts(4, true);
		final String gameName = input.readString();
		final boolean isPtr = input.readBoolean();
		return new KryoGameInfo(version, gameName, isPtr);
	}
	
}