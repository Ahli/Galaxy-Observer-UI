// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com
package com.ahli.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith (MockitoExtension.class)
final class StringInternerTest {
	
	//	@BeforeEach
	//	public void initMocks() {
	//		MockitoAnnotations.initMocks(this);
	//	}
	
	@Test
	public void testWeakRefStringInterner() {
		final double value = Math.random();
		final String text = "HELLO WORLD! ";
		String firstToBeInterned = text + value;
		
		// first intern
		String firstInterned = StringInterner.intern(firstToBeInterned);
		assertSame(firstInterned, firstToBeInterned, "first interned received different reference");
		
		// second intern
		final String secondToBeInterned = text + value;
		assertNotSame(firstToBeInterned, secondToBeInterned,
				"first and second interned are same reference - VM is breaking this test with optimizations");
		String secondInterned = StringInterner.intern(secondToBeInterned);
		
		assertEquals(secondInterned, firstInterned, "second interned changed value during interning - first vs second");
		assertEquals(secondInterned, secondToBeInterned,
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
		while (referenceQueue.poll() == null) {
			++i;
			System.gc();
		}
		assertTrue(i < 100, "GC did not remove the WeakReference at first attempt");
	}
}