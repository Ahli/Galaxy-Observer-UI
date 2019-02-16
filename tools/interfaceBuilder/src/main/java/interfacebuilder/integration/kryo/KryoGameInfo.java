// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.integration.kryo;

import java.util.Arrays;
import java.util.Objects;

public class KryoGameInfo {
	private final String gameName;
	private final boolean isPtr;
	private final int[] version;
	
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
		if (!(obj instanceof KryoGameInfo)) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		final Object[] signatureFields = getSignatureFields();
		final Object[] thatSignatureFields = ((KryoGameInfo) obj).getSignatureFields();
		for (int i = 0; i < signatureFields.length; i++) {
			if (!(signatureFields[i] instanceof Object[])) {
				if (!Objects.equals(signatureFields[i], thatSignatureFields[i])) {
					return false;
				}
			} else {
				if (!Arrays.deepEquals((Object[]) signatureFields[i], (Object[]) thatSignatureFields[i])) {
					return false;
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
}
