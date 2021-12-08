// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.integration.kryo;

import com.ahli.galaxy.ui.UIAnimationMutable;
import com.ahli.galaxy.ui.UIAttributeImmutable;
import com.ahli.galaxy.ui.UICatalogImpl;
import com.ahli.galaxy.ui.UIConstantImmutable;
import com.ahli.galaxy.ui.UIControllerMutable;
import com.ahli.galaxy.ui.UIFrameMutable;
import com.ahli.galaxy.ui.UIStateGroupMutable;
import com.ahli.galaxy.ui.UIStateMutable;
import com.ahli.galaxy.ui.UITemplate;
import com.ahli.interfacebuilder.integration.kryo.serializer.KryoGameInfoSerializer;
import com.ahli.interfacebuilder.integration.kryo.serializer.KryoInternStringArraySerializer;
import com.ahli.interfacebuilder.integration.kryo.serializer.KryoInternStringSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.HashMapReferenceResolver;
import com.esotericsoftware.kryo.util.ListReferenceResolver;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class KryoService {
	
	public Kryo getKryoForUICatalog() {
		// Kryo logging
		//		Log.ERROR = true;
		//		Log.WARN = true;
		//		Log.INFO = true;
		//		Log.DEBUG = true;
		//		Log.TRACE = false;
		final Kryo kryo = new Kryo(new HashMapReferenceResolver());
		kryo.register(ArrayList.class, 9);
		kryo.register(UIAttributeImmutable.class, 10);
		kryo.register(String[].class, new KryoInternStringArraySerializer(), 11);
		kryo.register(UIFrameMutable.class, 12);
		kryo.register(UITemplate.class, 13);
		kryo.register(UIConstantImmutable.class, 14);
		kryo.register(UIStateMutable.class, 15);
		kryo.register(UIControllerMutable.class, 16);
		kryo.register(UIAnimationMutable.class, 17);
		kryo.register(UIStateGroupMutable.class, 18);
		kryo.register(UICatalogImpl.class, 19);
		kryo.register(KryoGameInfo.class, new KryoGameInfoSerializer(), 20);
		kryo.register(int[].class, 21);
		kryo.register(byte[].class, 22);
		kryo.register(UnifiedMap.class, 23);
		kryo.register(String.class, new KryoInternStringSerializer(), 24);
		kryo.setRegistrationRequired(true);
		return kryo;
	}
	
	public Kryo getKryoForBaseUiMetaFile() {
		final Kryo kryo = new Kryo(new ListReferenceResolver());
		kryo.register(KryoGameInfo.class, new KryoGameInfoSerializer(), 9);
		kryo.register(int[].class, 10);
		kryo.setRegistrationRequired(true);
		return kryo;
	}
	
	public List<Object> get(final Path path, final List<Class<?>> payloadClasses, final Kryo kryo) throws IOException {
		try (final InflaterInputStream in = new InflaterInputStream(Files.newInputStream(path))) {
			try (final Input input = new Input(in)) {
				final List<Object> result = new ArrayList<>(payloadClasses.size());
				for (final var clazz : payloadClasses) {
					result.add(kryo.readObject(input, clazz));
				}
				return result;
			} catch (final KryoException e) {
				Files.deleteIfExists(path);
				throw new IOException(e);
			}
		}
	}
	
	public List<Object> get(
			final Path path, final List<Class<?>> payloadClasses, final Kryo kryo, final int stopAfterIndex)
			throws IOException {
		try (final InflaterInputStream in = new InflaterInputStream(Files.newInputStream(path))) {
			try (final Input input = new Input(in)) {
				final List<Object> result = new ArrayList<>(payloadClasses.size());
				int i = 0;
				for (final var clazz : payloadClasses) {
					result.add(kryo.readObject(input, clazz));
					if (i == stopAfterIndex) {
						break;
					}
					++i;
				}
				return result;
			} catch (final KryoException e) {
				Files.deleteIfExists(path);
				throw new IOException(e);
			}
		}
	}
	
	public void put(final Path path, final Iterable<Object> payload, final Kryo kryo) throws IOException {
		Files.createDirectories(path.getParent());
		try (final DeflaterOutputStream out = new DeflaterOutputStream(Files.newOutputStream(path))) {
			try (final Output output = new Output(out)) {
				for (final var obj : payload) {
					kryo.writeObject(output, obj);
				}
			}
		} catch (final KryoException e) {
			throw new IOException(e);
		}
	}
	
}
