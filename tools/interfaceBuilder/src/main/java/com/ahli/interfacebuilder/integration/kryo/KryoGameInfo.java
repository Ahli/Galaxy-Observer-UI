// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.integration.kryo;

import java.util.Arrays;
import java.util.Objects;

public class KryoGameInfo {
	private final int[] version;
	private final String gameName;
	private final boolean isPtr;
	
	public KryoGameInfo(final int[] version, final String gameName, final boolean isPtr) {
		this.version = version;
		this.gameName = gameName;
		this.isPtr = isPtr;
	}
	
	public String getGameName() {
		return gameName;
	}
	
	public boolean isPtr() {
		return isPtr;
	}
	
	public int[] getVersion() {
		return version;
	}
	
	@Override
	public String toString() {
		return "KryoGameInfo{gameName=" + gameName + ", isPtr=" + isPtr + ", version=" + Arrays.toString(version) + "}";
	}
	
	@Override
	public final boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof final KryoGameInfo that)) {
			return false;
		}
		return isPtr == that.isPtr && Arrays.equals(version, that.version) && Objects.equals(gameName, that.gameName);
	}
	
	@Override
	public final int hashCode() {
		@SuppressWarnings("ObjectInstantiationInEqualsHashCode")
		int result = Objects.hash(gameName, isPtr);
		result = 31 * result + Arrays.hashCode(version);
		return result;
	}
}
