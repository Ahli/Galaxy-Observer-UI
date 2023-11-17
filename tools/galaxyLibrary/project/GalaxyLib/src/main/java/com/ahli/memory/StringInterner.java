// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com
package com.ahli.memory;

import com.ahli.memory.interner.ConcurrentWeakWeakHashMap;

public final class StringInterner {
	private static final ConcurrentWeakWeakHashMap<String> map = new ConcurrentWeakWeakHashMap<>(8, 0.9F, 1);
	
	private StringInterner() {
		// no instances allowed
	}
	
	public static String intern(final String s) {
		final String exists = map.putIfAbsent(s, s);
		return (exists == null) ? s : exists;
	}
	
//	public static void clear() {
//		map.clear();
//	}
	
	/**
	 * Removes obsolete WeakReference-Instances that remain after the VM garbage was collected.
	 */
	public static void cleanUpGarbage() {
		System.gc();
		System.gc();
		System.gc();
		boolean result = map.purgeKeys();
		while (!map.isEmpty() && result) {
			result = map.purgeKeys();
		}
	}
	
	/**
	 * Returns the number of interned strings stored.
	 *
	 * @return the number of stored strings
	 */
	public static int size() {
		return map.size();
	}
	
//	public static StringBuilder print() {
//		final StringBuilder sb = new StringBuilder(32);
//		for (final String key : map.keySet()) {
//			sb.append(key).append('\n');
//		}
//		return sb;
//	}
}
