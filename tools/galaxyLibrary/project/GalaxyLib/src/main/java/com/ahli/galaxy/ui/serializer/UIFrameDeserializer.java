package com.ahli.galaxy.ui.serializer;

import com.ahli.galaxy.ui.UIFrame;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class UIFrameDeserializer extends StdDeserializer<UIFrame> {
	private static final Logger logger = LogManager.getLogger();
	
	public UIFrameDeserializer() {
		this(null);
	}
	
	protected UIFrameDeserializer(final Class<?> vc) {
		super(vc);
	}
	
	@Override
	public UIFrame deserialize(final JsonParser p, final DeserializationContext ctxt) {
		logger.error("attempting to deserialize without type");
		return null;
	}
	
	@Override
	public UIFrame deserializeWithType(final JsonParser p, final DeserializationContext ctxt,
			final TypeDeserializer typeDeserializer) throws IOException {
		
		final JsonNode node = p.getCodec().readTree(p);
		final String name = getNode(node, "n", null);
		final String type = getNode(node, "t", "Frame");
		final UIFrame frame = new UIFrame(name, type);
		
		
		// TODO other elements
		
		return frame;
	}
	
	private String getNode(final JsonNode parentNode, final String key, final String dflt) {
		final JsonNode node = parentNode.get(key);
		if (node != null) {
			return node.asText();
		}
		return dflt;
	}
	
}
