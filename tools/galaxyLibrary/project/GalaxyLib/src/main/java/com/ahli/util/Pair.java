// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.util;

import lombok.Data;

import java.util.Objects;

/**
 * <p> A convenience class to represent name-value pairs. </p>
 */
@Data
public class Pair<K, V> {
	private final K key;
	private final V value;
	
	@Override
	public final boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Pair)) {
			return false;
		}
		final Pair<?, ?> pair = (Pair<?, ?>) obj;
		return Objects.equals(key, pair.key) && Objects.equals(value, pair.value);
	}
	
	@Override
	public final int hashCode() {
		return Objects.hash(key, value);
	}
}
