// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.integration.kryo;

import com.ahli.galaxy.ui.UIAnimation;
import com.ahli.galaxy.ui.UIAttribute;
import com.ahli.galaxy.ui.UICatalogImpl;
import com.ahli.galaxy.ui.UIConstant;
import com.ahli.galaxy.ui.UIController;
import com.ahli.galaxy.ui.UIFrame;
import com.ahli.galaxy.ui.UIState;
import com.ahli.galaxy.ui.UIStateGroup;
import com.ahli.galaxy.ui.UITemplate;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class KryoService {
	
	public Kryo getKryoForUICatalog() {
		final Kryo kryo = new Kryo();
		kryo.register(ArrayList.class, 9);
		kryo.register(UIAttribute.class, 10);
		kryo.register(String[].class, 11);
		kryo.register(UIFrame.class, 12);
		kryo.register(UITemplate.class, 13);
		kryo.register(UIConstant.class, 14);
		kryo.register(UIState.class, 15);
		kryo.register(UIController.class, 16);
		kryo.register(UIAnimation.class, 17);
		kryo.register(UIStateGroup.class, 18);
		kryo.register(UICatalogImpl.class, 19);
		kryo.register(KryoGameInfo.class, 20);
		kryo.register(int[].class, 21);
		kryo.register(byte[].class, 22);
		kryo.register(KryoCachedBaseUi.class, 23);
		kryo.setReferences(true);
		return kryo;
	}
	
	public Kryo getKryoForBaseUiMetaFile() {
		final Kryo kryo = new Kryo();
		kryo.register(KryoGameInfo.class, 9);
		kryo.register(int[].class, 10);
		kryo.setReferences(true);
		return kryo;
	}
	
	public List<Object> get(final Path path, final List<Class<? extends Object>> payloadClasses, final Kryo kryo)
			throws IOException {
		try (final InflaterInputStream in = new InflaterInputStream(Files.newInputStream(path))) {
			try (final Input input = new Input(in)) {
				final List<Object> result = new ArrayList<>();
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
	
	public void put(final Path path, final List<Object> payload, final Kryo kryo) throws IOException {
		try (final DeflaterOutputStream out = new DeflaterOutputStream(Files.newOutputStream(path))) {
			try (final Output output = new Output(out)) {
				for (final var obj : payload) {
					kryo.writeObject(output, obj);
				}
			}
		}
	}
	
}
