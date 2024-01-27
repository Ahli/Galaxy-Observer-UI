package com.ahli.interfacebuilder.integration.h2;

import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Log4j2
public final class H2Integration {
	public static final String DB_FILE_NAME = "DB.mv.db";
	public static final String DB_TRACE_FILE_NAME = "DB.trace.db";
	
	private H2Integration() {
		// no instances allowed
	}
	
	/**
	 * Writes exportMigrate.zip and deletes the database files. The zip archive should be loaded by the DB migration.
	 *
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void migrateH2Db() throws IOException, InterruptedException {
		final Path dbDir = getDbDir();
		int oldVersion = oldH2DbFilePresent();
		switch (oldVersion) {
			case 1 -> {
				backupH2Db("1.4.200", dbDir);
				Files.deleteIfExists(dbDir.resolve(DB_FILE_NAME));
				Files.deleteIfExists(dbDir.resolve(DB_TRACE_FILE_NAME));
			}
			case 2 -> {
				backupH2Db("2.1.214", dbDir);
				Files.deleteIfExists(dbDir.resolve(DB_FILE_NAME));
				Files.deleteIfExists(dbDir.resolve(DB_TRACE_FILE_NAME));
			}
			default -> Files.deleteIfExists(dbDir.resolve("exportMigrate.zip"));
		}
	}
	
	private static Path getDbDir() {
		return Path.of(System.getProperty("user.home"), ".GalaxyObsUI", "database");
	}
	
	private static int oldH2DbFilePresent() {
		final Path dbPath = getDbDir().resolve(DB_FILE_NAME);
		if (Files.exists(dbPath)) {
			try (final BufferedReader brTest = Files.newBufferedReader(dbPath)) {
				final String firstLine = brTest.readLine();
				if (firstLine.contains(",format:1,")) {
					return 1;
				}
				if (firstLine.contains(",format:2,")) {
					return 2;
				}
			} catch (final IOException e) {
				log.error("Failed to read H2 DB file to determine version.");
			}
		}
		return 0;
	}
	
	private static void backupH2Db(final String h2Version, final Path dbDir) throws IOException, InterruptedException {
		final Path h2JarFile = dbDir.resolve("h2-" + h2Version + ".jar");
		if (!Files.exists(h2JarFile)) {
			// download e.g. https://repo1.maven.org/maven2/com/h2database/h2/1.4.200/h2-1.4.200.jar
			final URI uri = URI.create(
					"https://repo1.maven.org/maven2/com/h2database/h2/" + h2Version + "/h2-" + h2Version + ".jar");
			try (final ReadableByteChannel readableByteChannel = Channels.newChannel(uri.toURL().openStream());
			     final FileOutputStream fileOutputStream = new FileOutputStream(h2JarFile.toFile())) {
				fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
			}
		}
		
		// java -cp "h2-1.4.200.jar" org.h2.tools.Script -url jdbc:h2:~/test -user sa -script test.zip -options compression zip
		final String exportZipFile = getDbDir() + File.separator + "exportMigrate.zip";
		
		final String[] cmd = new String[] { "java", "-cp", h2JarFile.toString(), "org.h2.tools.Script", "-url",
				"jdbc:h2:file:~/.GalaxyObsUI/database/DB", "-user", "sa", /*"-password", "",*/ "-script", exportZipFile,
				"-options", "compression", "zip" };
		
		if (log.isTraceEnabled()) {
			log.trace("executing: {}", Arrays.toString(cmd));
		}
		Runtime.getRuntime().exec(cmd).waitFor();
		
		Files.deleteIfExists(h2JarFile);
	}
}
