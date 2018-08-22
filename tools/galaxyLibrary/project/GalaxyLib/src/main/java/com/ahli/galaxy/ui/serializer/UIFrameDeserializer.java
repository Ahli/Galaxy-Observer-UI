package com.ahli.galaxy.ui.serializer;

import com.ahli.galaxy.ui.UIAnchorSide;
import com.ahli.galaxy.ui.UIFrame;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class UIFrameDeserializer extends StdDeserializer<UIFrame> {
	public static final String ANCHOR_REF_PARENT = "$parent";
	public static final String ANCHOR_PARTS_SEPARATOR_REGEX = ">";
	public static final String ANCHOR_ARRAY_SEPARATOR_REGEX = "<";
	public static final String DFLT_FRAME_TYPE = "Frame";
	public static final String KEY_NAME = "N";
	public static final String KEY_TYPE = "T";
	public static final String KEY_ANCHOR = "A";
	public static final String EMPTY_ANCHOR = ">>";
	private static final Logger logger = LogManager.getLogger();
	
	public UIFrameDeserializer() {
		this(null);
	}
	
	protected UIFrameDeserializer(final Class<?> vc) {
		super(vc);
	}
	
	@Override
	public UIFrame deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
		logger.error("attempting to deserialize without type");
		return deserializeWithType(p, ctxt, null);
	}
	
	@Override
	public UIFrame deserializeWithType(final JsonParser p, final DeserializationContext ctxt,
			final TypeDeserializer typeDeserializer) throws IOException {
		final ObjectCodec oc = p.getCodec();
		final JsonNode node = oc.readTree(p);
		logger.error("attempting to deserialize" + node);
		final String anchor = getNode(node, KEY_ANCHOR, EMPTY_ANCHOR);
		final String type = getNode(node, KEY_TYPE, DFLT_FRAME_TYPE);
		final String name = getNode(node, KEY_NAME, null);
		
		final DeserializationConfig config = ctxt.getConfig();
		final JavaType javaType = TypeFactory.defaultInstance().constructType(UIFrame.class);
		final JsonDeserializer<Object> defaultDeserializer =
				BeanDeserializerFactory.instance.buildBeanDeserializer(ctxt, javaType, config.introspect(javaType));
		if (defaultDeserializer instanceof ResolvableDeserializer) {
			((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
		}
		final JsonParser treeParser = oc.treeAsTokens(node);
		config.initialize(treeParser);
		if (treeParser.getCurrentToken() == null) {
			treeParser.nextToken();
		}
		final UIFrame frame = (UIFrame) defaultDeserializer.deserialize(treeParser, ctxt);
		
		frame.setType(type);
		frame.setName(name);
		
		final String[] relative = new String[4];
		final String[] pos = new String[4];
		final String[] offset = new String[4];
		final String[] anchorParts = anchor.split(ANCHOR_PARTS_SEPARATOR_REGEX, -1);
		String[] dflt = { ANCHOR_REF_PARENT, ANCHOR_REF_PARENT, ANCHOR_REF_PARENT, ANCHOR_REF_PARENT };
		setFrameAnchorElements(anchorParts[0], relative, dflt);
		dflt = new String[] { "Min", "Min", "Max", "Max" };
		setFrameAnchorElements(anchorParts[1], pos, dflt);
		dflt = new String[] { "0", "0", "0", "0" };
		setFrameAnchorElements(anchorParts[2], offset, dflt);
		final UIAnchorSide[] sides = UIAnchorSide.values();
		for (int i = 0; i < 4; i++) {
			frame.setAnchor(sides[i], relative[i], pos[i], offset[i]);
		}
		
		return frame;
	}
	
	private String getNode(final JsonNode parentNode, final String key, final String dflt) {
		final JsonNode node = parentNode.get(key);
		if (node != null) {
			return node.asText();
		}
		return dflt;
	}
	
	private void setFrameAnchorElements(final String anchorParts, final String[] result, final String[] dfltVal) {
		final String[] elemParts = anchorParts.split(ANCHOR_ARRAY_SEPARATOR_REGEX, -1);
		if (elemParts.length == 1) {
			for (int i = 0; i < 4; i++) {
				result[i] = elemParts[0].isEmpty() ? dfltVal[i] : elemParts[0];
			}
		} else {
			for (int i = 0; i < 4; i++) {
				result[i] = elemParts[i].isEmpty() ? dfltVal[i] : elemParts[i];
			}
		}
	}
	
}
