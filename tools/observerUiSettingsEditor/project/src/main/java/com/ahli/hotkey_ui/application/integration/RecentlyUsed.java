package com.ahli.hotkey_ui.application.integration;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.ListReferenceResolver;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

@Slf4j
public class RecentlyUsed {
	private static final int MAX_RECENT_ELEMENTS = 5;
	
	private final ArrayDeque<Path> recent = new ArrayDeque<>(MAX_RECENT_ELEMENTS);
	private final Kryo kryo;
	
	public RecentlyUsed() {
		kryo = new Kryo(new ListReferenceResolver());
		kryo.setRegistrationRequired(true);
		loadItems(Path.of(System.getProperty("user.home"), ".GalaxyObsUiSettingsEditor/recent.bin"));
	}
	
	private void loadItems(final Path recentSaveFile) {
		if (Files.exists(recentSaveFile)) {
			try (final InflaterInputStream in = new InflaterInputStream(Files.newInputStream(recentSaveFile))) {
				try (final Input input = new Input(in)) {
					while (input.position() < input.limit()) {
						Path path = Path.of(kryo.readObject(input, String.class));
						if (Files.exists(path)) {
							recent.add(path);
						}
					}
				} catch (final KryoException e) {
					log.error("Failed to load {} file.", recentSaveFile.getFileName(), e);
				}
				if (recent.isEmpty()) {
					Files.deleteIfExists(recentSaveFile);
				}
			} catch (final IOException e) {
				log.error("Failed to delete {} file.", recentSaveFile.getFileName(), e);
			}
		}
	}
	
	public Deque<Path> getRecent() {
		return recent;
	}
	
	public void addRecent(final Path path) {
		if (!recent.remove(path) && recent.size() >= MAX_RECENT_ELEMENTS) {
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
			log.error("Failed to write save file {}.", path.getFileName(), e);
		}
	}
	
}
