package com.ahli.galaxy.ui.serializer;

import com.ahli.galaxy.ui.UIAnchorSide;
import com.ahli.galaxy.ui.UIFrame;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class UIFrameSerializer extends StdSerializer<UIFrame> {
	private static final Logger logger = LogManager.getLogger();
	private static final Set<String> ignoredFields = new HashSet<>(Arrays.asList("pos", "offset", "relative", "type"));
	private static final List<Field> fields;
	private static final String PARENT = "$parent";
	private static final char NEXT_ARRAY_VAL_SEPARATOR = '<';
	private static final char NEXT_TYPE_SEPARATOR = '>';
	private static final String EMPTYANCHOR = "" + NEXT_TYPE_SEPARATOR + NEXT_TYPE_SEPARATOR;
	private static final String ZERO = "0";
	private static final String MIN = "Min";
	private static final String MAX = "Max";
	
	static {
		fields = Arrays.stream(UIFrame.class.getDeclaredFields()).filter(UIFrameSerializer::filter).collect(toList());
	}
	
	public UIFrameSerializer() {
		this(null);
	}
	
	protected UIFrameSerializer(final Class<UIFrame> t) {
		super(t);
	}
	
	private static boolean filter(final Field field) {
		final int mod = field.getModifiers();
		return !Modifier.isStatic(mod) && !ignoredFields.contains(field.getName());
	}
	
	@Override
	public void serialize(final UIFrame value, final JsonGenerator gen, final SerializerProvider provider) {
		logger.error("attempting to serialize without type");
	}
	
	@Override
	public void serializeWithType(final UIFrame instance, final JsonGenerator gen, final SerializerProvider provider,
			final TypeSerializer typeSer) throws IOException {
		gen.writeStartObject();
		gen.writeStringField("@c", ".UIFrame");
		String type = instance.getType();
		if(!"Frame".equals(type)) {
			gen.writeStringField("t", type);
		}
		gen.writeStringField("n", instance.getName());
		Object fieldInstance;
		for (final Field field : fields) {
			try {
				field.setAccessible(true);
				fieldInstance = field.get(instance);
				if (fieldInstance != null) {
					gen.writeObjectField(field.getName(), field.get(instance));
				}
			} catch (final IllegalAccessException e) {
				logger.warn("ignoring exception", e);
			}
		}
		String anchor = getAnchor(instance);
		if(!EMPTYANCHOR.equals(anchor)) {
			gen.writeStringField("a", getAnchor(instance));
		}
		gen.writeEndObject();
	}
	
	private String getAnchor(final UIFrame value) {
		final String topRelative = emptyIf(value.getAnchorRelative(UIAnchorSide.TOP), PARENT);
		final String leftRelative = emptyIf(value.getAnchorRelative(UIAnchorSide.LEFT), PARENT);
		final String rightRelative = emptyIf(value.getAnchorRelative(UIAnchorSide.RIGHT), PARENT);
		final String bottomRelative = emptyIf(value.getAnchorRelative(UIAnchorSide.BOTTOM), PARENT);
		final StringBuilder str = new StringBuilder().append(topRelative);
		if (notEqual(topRelative, rightRelative, leftRelative, bottomRelative)) {
			str.append(NEXT_ARRAY_VAL_SEPARATOR).append(leftRelative).append(NEXT_ARRAY_VAL_SEPARATOR)
					.append(rightRelative).append(NEXT_ARRAY_VAL_SEPARATOR).append(bottomRelative);
		}
		
		final String topPos = emptyIf(value.getAnchorPos(UIAnchorSide.TOP), MIN);
		final String leftPos = emptyIf(value.getAnchorPos(UIAnchorSide.LEFT), MIN);
		final String rightPos = emptyIf(value.getAnchorPos(UIAnchorSide.RIGHT), MAX);
		final String bottomPos = emptyIf(value.getAnchorPos(UIAnchorSide.BOTTOM), MAX);
		str.append(NEXT_TYPE_SEPARATOR).append(topPos);
		if (notEqual(topPos, rightPos, leftPos, bottomPos)) {
			str.append(NEXT_ARRAY_VAL_SEPARATOR).append(leftPos).append(NEXT_ARRAY_VAL_SEPARATOR).append(rightPos)
					.append(NEXT_ARRAY_VAL_SEPARATOR).append(bottomPos);
		}
		
		final String topOffset = emptyIf(value.getAnchorOffset(UIAnchorSide.TOP), ZERO);
		final String leftOffset = emptyIf(value.getAnchorOffset(UIAnchorSide.LEFT), ZERO);
		final String rightOffset = emptyIf(value.getAnchorOffset(UIAnchorSide.RIGHT), ZERO);
		final String bottomOffset = emptyIf(value.getAnchorOffset(UIAnchorSide.BOTTOM), ZERO);
		str.append(NEXT_TYPE_SEPARATOR).append(topOffset);
		if (notEqual(topOffset, rightOffset, leftOffset, bottomOffset)) {
			str.append(NEXT_ARRAY_VAL_SEPARATOR).append(leftOffset).append(NEXT_ARRAY_VAL_SEPARATOR).append(rightOffset)
					.append(NEXT_ARRAY_VAL_SEPARATOR).append(bottomOffset);
		}
		
		return str.toString();
	}
	
	private String emptyIf(final String original, final String emptyIfThis) {
		if (emptyIfThis.equals(original)) {
			return "";
		}
		return original;
	}
	
	private boolean notEqual(final String a, final String b, final String c, final String d) {
		return !b.equals(a) || !c.equals(a) || !d.equals(a);
	}
}
