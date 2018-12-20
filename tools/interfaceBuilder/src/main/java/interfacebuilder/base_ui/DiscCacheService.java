package interfacebuilder.base_ui;

import com.ahli.galaxy.ui.UIAnimation;
import com.ahli.galaxy.ui.UIAttribute;
import com.ahli.galaxy.ui.UICatalogImpl;
import com.ahli.galaxy.ui.UIConstant;
import com.ahli.galaxy.ui.UIController;
import com.ahli.galaxy.ui.UIFrame;
import com.ahli.galaxy.ui.UIState;
import com.ahli.galaxy.ui.UIStateGroup;
import com.ahli.galaxy.ui.UITemplate;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import interfacebuilder.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class DiscCacheService {
	@Autowired
	private ConfigService configService;
	
	/**
	 * @param catalog
	 * @param gameDefName
	 * @throws IOException
	 */
	public void put(final UICatalog catalog, final String gameDefName, final boolean isPtr) throws IOException {
		final File f = getCacheFile(gameDefName, isPtr);
		final Path p = f.toPath();
		Files.deleteIfExists(p);
		
		try (final DeflaterOutputStream out = new DeflaterOutputStream(Files.newOutputStream(p))) {
			try (final Output output = new Output(out)) {
				final Kryo kryo = getKryo();
				kryo.writeObject(output, catalog);
			}
		}
	}
	
	/**
	 * @param gameDefName
	 * @return
	 */
	private File getCacheFile(final String gameDefName, final boolean isPtr) {
		final String path =
				configService.getCachePath() + File.separator + gameDefName + (isPtr ? " PTR" : "") + ".kryo";
		return new File(path);
	}
	
	private Kryo getKryo() {
		final Kryo kryo = new Kryo();
		kryo.register(UICatalogImpl.class);
		kryo.register(ArrayList.class);
		kryo.register(UITemplate.class);
		kryo.register(String[].class);
		kryo.register(UIFrame.class);
		kryo.register(UIAttribute.class);
		kryo.register(UIConstant.class);
		kryo.register(UIState.class);
		kryo.register(UIController.class);
		kryo.register(UIAnimation.class);
		kryo.register(UIStateGroup.class);
		kryo.setReferences(true);
		return kryo;
	}
	
	/**
	 * @param gameDefName
	 * @param isPtr
	 * @param clazz
	 * @param <T>
	 * @return
	 * @throws IOException
	 */
	public <T> T get(final String gameDefName, final boolean isPtr, final Class<T> clazz) throws IOException {
		final Path p = getCacheFile(gameDefName, isPtr).toPath();
		
		try (final InflaterInputStream in = new InflaterInputStream(Files.newInputStream(p))) {
			try (final Input input = new Input(in)) {
				final Kryo kryo = getKryo();
				return kryo.readObject(input, clazz);
			} catch (final KryoException e) {
				Files.deleteIfExists(p);
				throw new IOException(e);
			}
		}
	}
	
	/**
	 * @param gameDefName
	 * @param isPtr
	 * @throws IOException
	 */
	public void remove(final String gameDefName, final boolean isPtr) throws IOException {
		final File f = getCacheFile(gameDefName, isPtr);
		if (f.exists()) {
			Files.delete(f.toPath());
		}
	}
	
	/**
	 * @param gameDefName
	 * @param isPtr
	 * @return
	 */
	public boolean exists(final String gameDefName, final boolean isPtr) {
		return getCacheFile(gameDefName, isPtr).exists();
	}
}
