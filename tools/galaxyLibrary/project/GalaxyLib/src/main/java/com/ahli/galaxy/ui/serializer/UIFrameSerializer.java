package com.ahli.galaxy.ui.serializer;

import com.ahli.galaxy.ui.UIAnchorSide;
import com.ahli.galaxy.ui.UIFrame;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
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
	public static final String PARENT = "$parent";
	public static final char NEXT_ARRAY_VAL_SEPARATOR = '<';
	public static final char NEXT_TYPE_SEPARATOR = '>';
	public static final String EMPTYANCHOR = "" + NEXT_TYPE_SEPARATOR + NEXT_TYPE_SEPARATOR;
	public static final String ZERO = "0";
	public static final String MIN = "Min";
	public static final String MAX = "Max";
	private static final Logger logger = LogManager.getLogger();
	private static final Set<String> ignoredFields = new HashSet<>(Arrays.asList("pos", "offset", "relative", "type"));
	private static final List<Field> fields;
	
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
	public void serializeWithType(final UIFrame instance, final JsonGenerator gen, final SerializerProvider provider,
			final TypeSerializer typeSer) throws IOException {
		final WritableTypeId typeIdDef = typeSer.writeTypePrefix(gen, typeSer.typeId(instance, JsonToken.START_OBJECT));
		customSerialize(instance, gen, provider);
		typeSer.writeTypeSuffix(gen, typeIdDef);
	}
	
	private void customSerialize(final UIFrame instance, final JsonGenerator gen, final SerializerProvider provider)
			throws IOException {
		
		final String type = instance.getType();
		if (!"Frame".equals(type)) {
			gen.writeStringField("T", type);
		}
		gen.writeStringField("N", instance.getName());
		
		// first attempt -> good for simple fields, not for child UIFrames and attributes
		Object fieldInstance;
		for (final Field field : fields) {
			try {
				field.setAccessible(true);
				fieldInstance = field.get(instance);
				if (fieldInstance != null) {
					if (field.getName().equals("children")) {
						gen.writeFieldName("children");
					} else {
						gen.writeObjectField(field.getName(), field.get(instance));
					}
				}
			} catch (final IllegalAccessException e) {
				logger.warn("ignoring exception", e);
			}
		}
		
		
		// // like Object node
		// // https://github.com/FasterXML/jackson-databind/blob/master/src/main/java/com/fasterxml/jackson/databind/node/ObjectNode.java#L314
		//		@SuppressWarnings("deprecation")
		//		final boolean trimEmptyArray = (provider != null) &&
		//				!provider.isEnabled(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
		//		for (Map.Entry<String, JsonNode> en : _children.entrySet()) {
		//			BaseJsonNode value = (BaseJsonNode) en.getValue();
		//
		//			if (trimEmptyArray && value.isArray() && value.isEmpty(provider)) {
		//				continue;
		//			}
		//
		//			gen.writeFieldName(en.getKey());
		//			value.serialize(gen, provider);
		//		}
		
		
		// like deserialization -> Problem: cannot put own code there
		//		final SerializationConfig config = provider.getConfig();
		//		final JavaType javaType = TypeFactory.defaultInstance().constructType(UIFrame.class);
		//		final JsonSerializer<Object> serializer = BeanSerializerFactory.instance.createSerializer(provider, javaType);
		//		serializer.serialize(instance,gen,provider);
		
		
		final String anchor = getAnchor(instance);
		if (!EMPTYANCHOR.equals(anchor)) {
			gen.writeStringField("A", getAnchor(instance));
		}
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
	
	@Override
	public void serialize(final UIFrame instance, final JsonGenerator gen, final SerializerProvider provider)
			throws IOException {
		gen.writeStartObject(instance);
		customSerialize(instance, gen, provider);
		gen.writeEndObject();
	}
}
