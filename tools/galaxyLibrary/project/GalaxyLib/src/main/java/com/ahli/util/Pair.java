// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.util;

import lombok.Data;

/**
 * <p> A convenience class to represent name-value pairs. </p>
 */
@Data
public class Pair<K, V> {
	private final K key;
	private final V value;
}
