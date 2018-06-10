package application.baseUi;

import application.config.ConfigService;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class DiscCacheService {
	@Autowired
	ConfigService configService;
	
	/**
	 * @param catalog
	 * @param id
	 * @throws IOException
	 */
	public void put(final UICatalog catalog, final String id) throws IOException {
		final ObjectMapper objMapper = new ObjectMapper();
		final String path = configService.getBasePath() + File.separator + "Cache" + File.separator + id + ".zip";
		final File f = new File(path);
		Files.createDirectories(f.getParentFile().toPath());
		Files.deleteIfExists(f.toPath());
		
		try (final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f))) {
			out.putNextEntry(new ZipEntry("c.json"));
			objMapper.writeValue(out, catalog);
		}
	}
	
	/**
	 * @param id
	 * @param clazz
	 * @param <T>
	 * @return
	 * @throws IOException
	 */
	public <T> T get(final String id, final Class<T> clazz) throws IOException {
		final T catalog;
		final ObjectMapper objMapper = new ObjectMapper();
		final String path = configService.getBasePath() + File.separator + "Cache" + File.separator + id + ".zip";
		final File f = new File(path);
		try (final ZipInputStream in = new ZipInputStream(new FileInputStream(f))) {
			in.getNextEntry();
			catalog = objMapper.readValue(in, clazz);
		}
		return catalog;
	}
}
