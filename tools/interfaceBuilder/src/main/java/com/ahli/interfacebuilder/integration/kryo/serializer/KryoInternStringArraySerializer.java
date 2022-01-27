// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.integration.kryo.serializer;

import com.ahli.util.StringInterner;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers;

public class KryoInternStringArraySerializer extends DefaultArraySerializers.StringArraySerializer {
	
	@Override
	public String[] read(final Kryo kryo, final Input input, final Class type) {
		final String[] array = super.read(kryo, input, type);
		
		for (int i = array.length - 1; i >= 0; --i) {
			if (array[i] != null) {
				array[i] = StringInterner.intern(array[i]);
			}
		}
		return array;
	}
	
}
