package interfacebuilder.base_ui;

import com.ahli.galaxy.ui.interfaces.UICatalog;
import com.fasterxml.jackson.databind.ObjectMapper;
import interfacebuilder.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class DiscCacheService {
	@Autowired
	private ConfigService configService;
	
	/**
	 * @param catalog
	 * @param id
	 * @throws IOException
	 */
	public void put(final UICatalog catalog, final String id, final boolean isPtr) throws IOException {
		final ObjectMapper objMapper = new ObjectMapper();
		final File f = getCacheFile(id, isPtr);
		Files.createDirectories(f.getParentFile().toPath());
		final Path p = f.toPath();
		Files.deleteIfExists(p);
		
		try (final ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(p))) {
			out.putNextEntry(new ZipEntry("c.json"));
			objMapper.writeValue(out, catalog);
		}
	}
	
	/**
	 * @param id
	 * @return
	 */
	private File getCacheFile(final String id, final boolean isPtr) {
		final String path = configService.getCachePath() + File.separator + id + (isPtr ? " PTR" : "") + ".zip";
		return new File(path);
	}
	
	/**
	 * @param id
	 * @param isPtr
	 * @param clazz
	 * @param <T>
	 * @return
	 * @throws IOException
	 */
	public <T> T get(final String id, final boolean isPtr, final Class<T> clazz) throws IOException {
		final T catalog;
		final ObjectMapper objMapper = new ObjectMapper();
		final Path p = getCacheFile(id, isPtr).toPath();
		try (final ZipInputStream in = new ZipInputStream(Files.newInputStream(p))) {
			in.getNextEntry();
			catalog = objMapper.readValue(in, clazz);
		}
		return catalog;
	}
	
	/**
	 * @param id
	 * @param isPtr
	 * @throws IOException
	 */
	public void remove(final String id, final boolean isPtr) throws IOException {
		final File f = getCacheFile(id, isPtr);
		if (f.exists()) {
			Files.delete(f.toPath());
		}
	}
	
	/**
	 * @param id
	 * @param isPtr
	 * @return
	 */
	public boolean exists(final String id, final boolean isPtr) {
		return getCacheFile(id, isPtr).exists();
	}
}
