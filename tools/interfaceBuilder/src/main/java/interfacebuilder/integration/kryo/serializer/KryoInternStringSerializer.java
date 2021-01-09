// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.integration.kryo.serializer;

import com.ahli.util.StringInterner;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.ImmutableSerializer;

public class KryoInternStringSerializer extends ImmutableSerializer<String> {
	
	public KryoInternStringSerializer() {
		setAcceptsNull(true);
	}
	
	@Override
	public void write(final Kryo kryo, final Output output, final String object) {
		output.writeString(object);
	}
	
	@Override
	public String read(final Kryo kryo, final Input input, final Class<? extends String> type) {
		return StringInterner.intern(input.readString());
	}
	
}
