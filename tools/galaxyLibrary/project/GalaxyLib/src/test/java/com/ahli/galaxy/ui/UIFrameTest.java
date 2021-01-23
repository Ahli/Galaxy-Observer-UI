// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class UIFrameTest {
	
	@Test
	void deepCopy() {
		final UIFrame frame = new UIFrame("UIFrameTest_setAnchor");
		
		frame.setAnchor("$parent", "0");
		assertEquals("$parent", frame.getAnchorRelative(UIAnchorSide.TOP));
		assertEquals("$parent", frame.getAnchorRelative(UIAnchorSide.LEFT));
		assertEquals("$parent", frame.getAnchorRelative(UIAnchorSide.BOTTOM));
		assertEquals("$parent", frame.getAnchorRelative(UIAnchorSide.RIGHT));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.TOP));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.LEFT));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.BOTTOM));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.RIGHT));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.TOP));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.LEFT));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.BOTTOM));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.RIGHT));
		
		final UIFrame frame2 = (UIFrame) frame.deepCopy();
		assertEquals("$parent", frame2.getAnchorRelative(UIAnchorSide.TOP));
		assertEquals("$parent", frame2.getAnchorRelative(UIAnchorSide.LEFT));
		assertEquals("$parent", frame2.getAnchorRelative(UIAnchorSide.BOTTOM));
		assertEquals("$parent", frame2.getAnchorRelative(UIAnchorSide.RIGHT));
		assertEquals("Min", frame2.getAnchorPos(UIAnchorSide.TOP));
		assertEquals("Min", frame2.getAnchorPos(UIAnchorSide.LEFT));
		assertEquals("Max", frame2.getAnchorPos(UIAnchorSide.BOTTOM));
		assertEquals("Max", frame2.getAnchorPos(UIAnchorSide.RIGHT));
		assertEquals("0", frame2.getAnchorOffset(UIAnchorSide.TOP));
		assertEquals("0", frame2.getAnchorOffset(UIAnchorSide.LEFT));
		assertEquals("0", frame2.getAnchorOffset(UIAnchorSide.BOTTOM));
		assertEquals("0", frame2.getAnchorOffset(UIAnchorSide.RIGHT));
		
		frame2.setAnchor(UIAnchorSide.BOTTOM, "$Minimap", "mid", "11");
		assertEquals("$parent", frame2.getAnchorRelative(UIAnchorSide.TOP));
		assertEquals("$parent", frame2.getAnchorRelative(UIAnchorSide.LEFT));
		assertEquals("$Minimap", frame2.getAnchorRelative(UIAnchorSide.BOTTOM));
		assertEquals("$parent", frame2.getAnchorRelative(UIAnchorSide.RIGHT));
		assertEquals("Min", frame2.getAnchorPos(UIAnchorSide.TOP));
		assertEquals("Min", frame2.getAnchorPos(UIAnchorSide.LEFT));
		assertEquals("mid", frame2.getAnchorPos(UIAnchorSide.BOTTOM));
		assertEquals("Max", frame2.getAnchorPos(UIAnchorSide.RIGHT));
		assertEquals("0", frame2.getAnchorOffset(UIAnchorSide.TOP));
		assertEquals("0", frame2.getAnchorOffset(UIAnchorSide.LEFT));
		assertEquals("11", frame2.getAnchorOffset(UIAnchorSide.BOTTOM));
		assertEquals("0", frame2.getAnchorOffset(UIAnchorSide.RIGHT));
		
	}
	
	@Test
	void setAnchor1() {
		final UIFrame frame = new UIFrame("UIFrameTest_setAnchor");
		
		frame.setAnchor("$parent", "0");
		assertEquals("$parent", frame.getAnchorRelative(UIAnchorSide.TOP));
		assertEquals("$parent", frame.getAnchorRelative(UIAnchorSide.LEFT));
		assertEquals("$parent", frame.getAnchorRelative(UIAnchorSide.BOTTOM));
		assertEquals("$parent", frame.getAnchorRelative(UIAnchorSide.RIGHT));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.TOP));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.LEFT));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.BOTTOM));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.RIGHT));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.TOP));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.LEFT));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.BOTTOM));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.RIGHT));
		
		frame.setAnchor("$this", "0");
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.TOP));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.LEFT));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.BOTTOM));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.RIGHT));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.TOP));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.LEFT));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.BOTTOM));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.RIGHT));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.TOP));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.LEFT));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.BOTTOM));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.RIGHT));
		
		frame.setAnchor("$SomeHandle/$parent/child", "10");
		assertEquals("$SomeHandle/$parent/child", frame.getAnchorRelative(UIAnchorSide.TOP));
		assertEquals("$SomeHandle/$parent/child", frame.getAnchorRelative(UIAnchorSide.LEFT));
		assertEquals("$SomeHandle/$parent/child", frame.getAnchorRelative(UIAnchorSide.BOTTOM));
		assertEquals("$SomeHandle/$parent/child", frame.getAnchorRelative(UIAnchorSide.RIGHT));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.TOP));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.LEFT));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.BOTTOM));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.RIGHT));
		assertEquals("10", frame.getAnchorOffset(UIAnchorSide.TOP));
		assertEquals("10", frame.getAnchorOffset(UIAnchorSide.LEFT));
		assertEquals("-10", frame.getAnchorOffset(UIAnchorSide.BOTTOM));
		assertEquals("-10", frame.getAnchorOffset(UIAnchorSide.RIGHT));
	}
	
	@Test
	void setAnchor2() {
		final UIFrame frame = new UIFrame("UIFrameTest_setAnchor");
		
		frame.setAnchor(UIAnchorSide.BOTTOM, "$parent", "mid", "11");
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.TOP));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.LEFT));
		assertEquals("$parent", frame.getAnchorRelative(UIAnchorSide.BOTTOM));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.RIGHT));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.TOP));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.LEFT));
		assertEquals("mid", frame.getAnchorPos(UIAnchorSide.BOTTOM));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.RIGHT));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.TOP));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.LEFT));
		assertEquals("11", frame.getAnchorOffset(UIAnchorSide.BOTTOM));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.RIGHT));
	}
	
	@Test
	void setAnchorRelative() {
		final UIFrame frame = new UIFrame("UIFrameTest_setAnchor");
		
		frame.setAnchorRelative(UIAnchorSide.BOTTOM, "$parent");
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.TOP));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.LEFT));
		assertEquals("$parent", frame.getAnchorRelative(UIAnchorSide.BOTTOM));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.RIGHT));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.TOP));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.LEFT));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.BOTTOM));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.RIGHT));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.TOP));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.LEFT));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.BOTTOM));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.RIGHT));
		
		frame.setAnchorRelative(UIAnchorSide.BOTTOM, "$this");
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.TOP));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.LEFT));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.BOTTOM));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.RIGHT));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.TOP));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.LEFT));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.BOTTOM));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.RIGHT));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.TOP));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.LEFT));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.BOTTOM));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.RIGHT));
	}
	
	@Test
	void setAnchorPos() {
		final UIFrame frame = new UIFrame("UIFrameTest_setAnchor");
		
		frame.setAnchorPos(UIAnchorSide.BOTTOM, "min");
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.TOP));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.LEFT));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.BOTTOM));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.RIGHT));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.TOP));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.LEFT));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.BOTTOM));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.RIGHT));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.TOP));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.LEFT));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.BOTTOM));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.RIGHT));
		
		frame.setAnchorPos(UIAnchorSide.BOTTOM, "max");
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.TOP));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.LEFT));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.BOTTOM));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.RIGHT));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.TOP));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.LEFT));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.BOTTOM));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.RIGHT));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.TOP));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.LEFT));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.BOTTOM));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.RIGHT));
		
		frame.setAnchorPos(UIAnchorSide.BOTTOM, "mid");
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.TOP));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.LEFT));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.BOTTOM));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.RIGHT));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.TOP));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.LEFT));
		assertEquals("mid", frame.getAnchorPos(UIAnchorSide.BOTTOM));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.RIGHT));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.TOP));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.LEFT));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.BOTTOM));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.RIGHT));
		
		frame.setAnchorPos(UIAnchorSide.BOTTOM, "0.123");
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.TOP));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.LEFT));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.BOTTOM));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.RIGHT));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.TOP));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.LEFT));
		assertEquals("0.123", frame.getAnchorPos(UIAnchorSide.BOTTOM));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.RIGHT));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.TOP));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.LEFT));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.BOTTOM));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.RIGHT));
	}
	
	@Test
	void setAnchorOffset() {
		final UIFrame frame = new UIFrame("UIFrameTest_setAnchor");
		
		frame.setAnchorOffset(UIAnchorSide.BOTTOM, "11");
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.TOP));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.LEFT));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.BOTTOM));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.RIGHT));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.TOP));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.LEFT));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.BOTTOM));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.RIGHT));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.TOP));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.LEFT));
		assertEquals("11", frame.getAnchorOffset(UIAnchorSide.BOTTOM));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.RIGHT));
		
		frame.setAnchorOffset(UIAnchorSide.BOTTOM, "0");
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.TOP));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.LEFT));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.BOTTOM));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.RIGHT));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.TOP));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.LEFT));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.BOTTOM));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.RIGHT));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.TOP));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.LEFT));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.BOTTOM));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.RIGHT));
		
		frame.setAnchorOffset(UIAnchorSide.BOTTOM, "-5.2");
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.TOP));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.LEFT));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.BOTTOM));
		assertEquals("$this", frame.getAnchorRelative(UIAnchorSide.RIGHT));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.TOP));
		assertEquals("Min", frame.getAnchorPos(UIAnchorSide.LEFT));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.BOTTOM));
		assertEquals("Max", frame.getAnchorPos(UIAnchorSide.RIGHT));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.TOP));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.LEFT));
		assertEquals("-5.2", frame.getAnchorOffset(UIAnchorSide.BOTTOM));
		assertEquals("0", frame.getAnchorOffset(UIAnchorSide.RIGHT));
	}
	
	@Test
	@SuppressWarnings("JUnitTestMethodWithNoAssertions")
	void equalsContract() {
		EqualsVerifier.forClass(UIFrame.class)
				.withRedefinedSuperclass()
				.withIgnoredFields("hash", "hashIsZero", "hashIsDirty")
				.suppress(Warning.NONFINAL_FIELDS)
				.verify();
	}
}
