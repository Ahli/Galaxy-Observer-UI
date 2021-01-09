// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.integration.kryo.serializer;

import com.ahli.util.StringInterner;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers;

import static com.esotericsoftware.kryo.Kryo.NULL;

public class KryoInternStringArraySerializer extends DefaultArraySerializers.StringArraySerializer {
	
	@Override
	public String[] read(final Kryo kryo, final Input input, final Class type) {
		int length = input.readVarInt(true);
		if (length == NULL) {
			return null;
		}
		final String[] array = new String[--length];
		if (kryo.getReferences() && kryo.getReferenceResolver().useReferences(String.class)) {
			final var serializer = kryo.getSerializer(String.class);
			for (int i = 0; i < length; ++i) {
				array[i] = kryo.readObjectOrNull(input, String.class, serializer);
			}
		} else {
			for (int i = 0; i < length; ++i) {
				array[i] = StringInterner.intern(input.readString());
			}
		}
		return array;
	}
	
}