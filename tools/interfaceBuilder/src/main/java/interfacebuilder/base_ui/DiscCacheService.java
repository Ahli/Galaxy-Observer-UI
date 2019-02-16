// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.base_ui;

import com.ahli.galaxy.ui.interfaces.UICatalog;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import interfacebuilder.config.ConfigService;
import interfacebuilder.integration.kryo.KryoService;
import interfacebuilder.integration.kryo.KryoGameInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class DiscCacheService {
	private static final Logger logger = LogManager.getLogger(DiscCacheService.class);
	@Autowired
	private ConfigService configService;
	@Autowired
	private KryoService kryoService;
	
	/**
	 * @param catalog
	 * @param gameDefName
	 * @throws IOException
	 */
	public void put(final UICatalog catalog, final String gameDefName, final boolean isPtr, final int[] version)
			throws IOException {
		final File f = getCacheFile(gameDefName, isPtr);
		final Path p = f.toPath();
		Files.deleteIfExists(p);
		
		final KryoGameInfo metaInfo = new KryoGameInfo(version, gameDefName, isPtr);
		
		try (final DeflaterOutputStream out = new DeflaterOutputStream(Files.newOutputStream(p))) {
			try (final Output output = new Output(out)) {
				final Kryo kryo = kryoService.getKryoForUICatalog();
				kryo.writeObject(output, catalog);
				kryo.writeObject(output, metaInfo);
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
				final Kryo kryo = kryoService.getKryoForUICatalog();
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
			logger.trace("Cleaning cache of {} in {}", () -> gameDefName, f::getAbsolutePath);
		} else {
			logger.trace("Could not find cache of {} in {} to clean it", () -> gameDefName, f::getAbsolutePath);
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
