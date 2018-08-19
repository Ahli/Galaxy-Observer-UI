package com.ahli.galaxy.ui;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith (MockitoExtension.class)
public class UIFrameTest {
	private static final Logger logger = LogManager.getLogger();
	
	@BeforeEach
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testJSON() throws IOException {
		final ObjectMapper objMapper = new ObjectMapper();
		
		final UIFrame frame = new UIFrame("StandardFrame", "Frame");
		frame.setAnchor(UIAnchorSide.TOP, "$parent", "Min", "0");
		frame.setAnchor(UIAnchorSide.LEFT, "$parent", "Min", "0");
		frame.setAnchor(UIAnchorSide.BOTTOM, "$parent", "Max", "0");
		frame.setAnchor(UIAnchorSide.RIGHT, "$parent", "Max", "0");
		final String result = objMapper.writeValueAsString(frame);
		print(frame);
		logger.info(result);
		
		final UIFrame frame2 = new UIFrame("ComplexFrame", "GameUIb");
		frame2.setAnchor(UIAnchorSide.TOP, "$parent/a", "Mid", "-42");
		frame2.setAnchor(UIAnchorSide.LEFT, "$parent/a", "Mid", "-42");
		frame2.setAnchor(UIAnchorSide.BOTTOM, "$parent/a", "Mid", "42");
		frame2.setAnchor(UIAnchorSide.RIGHT, "$parent/a", "Mid", "42");
		final String result2 = objMapper.writeValueAsString(frame2);
		print(frame2);
		logger.info(result2);
		
		final UIFrame reborn = objMapper.readValue("{\"@c\":\".UIFrame\",\"n\":\"StandardFrame\"}", UIFrame.class);
		print(reborn);
		
		final UIFrame reborn2 = objMapper.readValue(
				"{\"@c\":\".UIFrame\",\"t\":\"GameUIb\",\"n\":\"ComplexFrame\",\"a\":\"$parent/a>Mid>-42<-42<42<42\"}",
				UIFrame.class);
		print(reborn2);
		
		assertEquals("{\"@c\":\".UIFrame\",\"n\":\"StandardFrame\"}", result);
		assertEquals(
				"{\"@c\":\".UIFrame\",\"t\":\"GameUIb\",\"n\":\"ComplexFrame\",\"a\":\"$parent/a>Mid>-42<-42<42<42\"}",
				result2);
		assertEquals(reborn, frame);
		assertEquals(reborn2, frame2);
	}
	
	private void print(final UIFrame frame) {
		logger.info(frame);
		for (final var side : UIAnchorSide.values()) {
			logger.info(
					side.toString() + ", " + frame.getAnchorRelative(side) + ", " + frame.getAnchorPos(side) + ", " +
							frame.getAnchorOffset(side));
		}
	}
	
}
