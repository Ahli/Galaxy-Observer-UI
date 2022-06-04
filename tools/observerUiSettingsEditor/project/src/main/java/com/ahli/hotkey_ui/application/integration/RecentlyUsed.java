package com.ahli.hotkey_ui.application.integration;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.ListReferenceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class RecentlyUsed {
	
	private static final Logger logger = LoggerFactory.getLogger(RecentlyUsed.class);
	private final ArrayDeque<Path> recent = new ArrayDeque<>(5);
	private final Kryo kryo;
	
	public RecentlyUsed() {
		kryo = new Kryo(new ListReferenceResolver());
		kryo.setRegistrationRequired(true);
		load(Path.of(System.getProperty("user.home"), ".GalaxyObsUiSettingsEditor/recent.bin"));
	}
	
	private void load(final Path path) {
		if (Files.exists(path)) {
			try (final InflaterInputStream in = new InflaterInputStream(Files.newInputStream(path))) {
				int i = 0;
				try (final Input input = new Input(in)) {
					for (i = 0; i < 5; ++i) {
						recent.add(Path.of(kryo.readObject(input, String.class)));
					}
				} catch (final KryoException e) {
					logger.error("Failed to load save file.", e);
					if (i == 0) {
						Files.deleteIfExists(path);
					}
				}
			} catch (final IOException e) {
				logger.error("Failed to delete save file.", e);
			}
		}
	}
	
	public Deque<Path> getRecent() {
		return recent;
	}
	
	public void addRecent(final Path path) {
		if (!recent.remove(path) && recent.size() >= 5) {
			recent.pollFirst();
		}
		recent.add(path);
		save(Path.of(System.getProperty("user.home"), ".GalaxyObsUiSettingsEditor/recent.bin"));
	}
	
	private void save(final Path path) {
		try {
			Files.createDirectories(path.getParent());
			try (final DeflaterOutputStream out = new DeflaterOutputStream(Files.newOutputStream(path))) {
				try (final Output output = new Output(out)) {
					for (final var entry : recent) {
						kryo.writeObject(output, entry.toAbsolutePath().toString());
					}
				}
			} catch (final KryoException e) {
				throw new IOException(e);
			}
		} catch (final IOException e) {
			logger.error("Failed to write save file.", e);
		}
	}
	
}
