// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.integration.kryo;

import java.util.Arrays;
import java.util.Objects;

public record KryoGameInfo(int[] version, String gameName, boolean isPtr) {
	
	@Override
	public String toString() {
		return "KryoGameInfo{gameName=" + gameName + ", isPtr=" + isPtr + ", version=" + Arrays.toString(version) + "}";
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof final KryoGameInfo that)) {
			return false;
		}
		return isPtr == that.isPtr && Arrays.equals(version, that.version) && Objects.equals(gameName, that.gameName);
	}
	
	@Override
	public int hashCode() {
		@SuppressWarnings("ObjectInstantiationInEqualsHashCode")
		int result = Objects.hash(gameName, isPtr);
		result = 31 * result + Arrays.hashCode(version);
		return result;
	}
}
