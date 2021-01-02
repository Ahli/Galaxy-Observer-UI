// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com
package com.ahli.util;

import org.junit.jupiter.api.Test;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class StringInternerTest {
	
	@Test
	void testWeakRefStringInterner() {
		final double value = Math.random();
		final String text = "HELLO WORLD! ";
		String firstToBeInterned = text + value;
		
		// first intern
		String firstInterned = StringInterner.intern(firstToBeInterned);
		assertSame(firstInterned, firstToBeInterned, "first interned received different reference");
		
		// second intern
		final String secondToBeInterned = text + value;
		assertNotSame(
				firstToBeInterned,
				secondToBeInterned,
				"first and second interned are same reference - VM is breaking this test with optimizations");
		String secondInterned = StringInterner.intern(secondToBeInterned);
		
		assertEquals(secondInterned, firstInterned, "second interned changed value during interning - first vs second");
		assertEquals(
				secondInterned,
				secondToBeInterned,
				"second interned changed value during interning - second input vs output");
		assertNotSame(secondInterned, secondToBeInterned, "second interned delivered same reference back");
		assertSame(firstInterned, secondInterned, "first interned received different reference");
		
		// test garbage collection
		final var referenceQueue = new ReferenceQueue<String>();
		final var weakRef = new WeakReference<>(firstInterned, referenceQueue);
		firstToBeInterned = null;
		firstInterned = null;
		secondInterned = null;
		
		int i = 0;
		final int maxAttempts = 3;
		while (referenceQueue.poll() == null && i < maxAttempts) {
			++i;
			System.gc();
			System.runFinalization();
		}
		// fails on J9 JDK
		assertTrue(i < maxAttempts, "GC did not remove the WeakReference within " + maxAttempts + " attempts");
	}
}