// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.integration.kryo;

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
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		final Object[] signatureFields = getSignatureFields();
		final Object[] thatSignatureFields = ((KryoGameInfo) obj).getSignatureFields();
		for (int i = 0; i < signatureFields.length; ++i) {
			final var thisObj = signatureFields[i];
			final var thatObj = thatSignatureFields[i];
			final boolean objectArray = thisObj instanceof Object[];
			final boolean isArray = thisObj.getClass().isArray();
			if (!(objectArray || isArray)) {
				if (!Objects.equals(thisObj, thatObj)) {
					return false;
				}
			} else {
				if (objectArray) {
					if (!Arrays.deepEquals((Object[]) thisObj, (Object[]) thatObj)) {
						return false;
					}
				} else {
					final var typeThis = thisObj.getClass().getComponentType();
					if (!typeThis.equals(thatObj.getClass().getComponentType())) {
						return false;
					}
					
					if (typeThis.equals(int[].class)) {
						if (!Arrays.equals((int[]) thisObj, (int[]) thatObj)) {
							return false;
						}
					} else if (typeThis.equals(boolean[].class)) {
						if (!Arrays.equals((boolean[]) thisObj, (boolean[]) thatObj)) {
							return false;
						}
					} else if (typeThis.equals(short[].class)) {
						if (!Arrays.equals((short[]) thisObj, (short[]) thatObj)) {
							return false;
						}
					} else if (typeThis.equals(char[].class)) {
						if (!Arrays.equals((char[]) thisObj, (char[]) thatObj)) {
							return false;
						}
					} else if (typeThis.equals(byte[].class)) {
						if (!Arrays.equals((byte[]) thisObj, (byte[]) thatObj)) {
							return false;
						}
					} else if (typeThis.equals(long[].class)) {
						if (!Arrays.equals((long[]) thisObj, (long[]) thatObj)) {
							return false;
						}
					} else if (typeThis.equals(float[].class)) {
						if (!Arrays.equals((float[]) thisObj, (float[]) thatObj)) {
							return false;
						}
					} else if (typeThis.equals(double[].class) &&
							!Arrays.equals((double[]) thisObj, (double[]) thatObj)) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private Object[] getSignatureFields() {
		return new Object[] { gameName, version, isPtr };
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getSignatureFields());
	}
	
	@Override
	public String toString() {
		return "KryoGameInfo{gameName=" + gameName + ", isPtr=" + isPtr + ", version=" + Arrays.toString(version) + "}";
	}
}
