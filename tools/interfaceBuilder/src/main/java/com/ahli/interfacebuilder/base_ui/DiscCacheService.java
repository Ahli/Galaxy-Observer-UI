// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.base_ui;

import com.ahli.galaxy.ui.UICatalogImpl;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import com.ahli.interfacebuilder.config.ConfigService;
import com.ahli.interfacebuilder.integration.kryo.KryoGameInfo;
import com.ahli.interfacebuilder.integration.kryo.KryoService;
import com.esotericsoftware.kryo.Kryo;
import lombok.extern.log4j.Log4j2;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class DiscCacheService {
	private final ConfigService configService;
	private final KryoService kryoService;
	
	public DiscCacheService(@NonNull final ConfigService configService, @NonNull final KryoService kryoService) {
		this.configService = configService;
		this.kryoService = kryoService;
	}
	
	/**
	 * @param catalog
	 * @param gameDefName
	 * @throws IOException
	 */
	public void put(
			@NonNull final UICatalog catalog,
			@NonNull final String gameDefName,
			final boolean isPtr,
			@NonNull final int[] version) throws IOException {
		final Path p = getCacheFilePath(gameDefName, isPtr);
		Files.deleteIfExists(p);
		
		final KryoGameInfo metaInfo = new KryoGameInfo(version, gameDefName, isPtr);
		final List<Object> payload = new ArrayList<>(2);
		payload.add(metaInfo);
		payload.add(catalog);
		
		final Kryo kryo = kryoService.getKryoForUICatalog();
		kryoService.put(p, payload, kryo);
		log.info(
				"Cached UI for {} - templates={}, blizzOnlyTemplates={}, constants={}, blizzOnlyConstants={}, devLayouts={}, handles={}",
				gameDefName,
				catalog.getTemplates().size(),
				catalog.getBlizzOnlyTemplates().size(),
				catalog.getConstants().size(),
				catalog.getBlizzOnlyConstants().size(),
				catalog.getDevLayouts().size(),
				catalog.getHandles().size());
	}
	
	/**
	 * @param gameDefName
	 * @return
	 */
	@NonNull
	public Path getCacheFilePath(@NonNull final String gameDefName, final boolean isPtr) {
		return configService.getCachePath().resolve(gameDefName + (isPtr ? " PTR" : "") + ".kryo");
	}
	
	/**
	 * @param gameDefName
	 * @param isPtr
	 * @return
	 * @throws IOException
	 */
	@NonNull
	public UICatalog getCachedBaseUi(@NonNull final String gameDefName, final boolean isPtr) throws IOException {
		return getCachedBaseUi(getCacheFilePath(gameDefName, isPtr));
	}
	
	@NonNull
	public UICatalog getCachedBaseUi(@NonNull final Path path) throws IOException {
		final Kryo kryo = kryoService.getKryoForUICatalog();
		final List<Class<?>> payloadClasses = new ArrayList<>(2);
		payloadClasses.add(KryoGameInfo.class);
		payloadClasses.add(UICatalogImpl.class);
		return (UICatalog) kryoService.get(path, payloadClasses, kryo).get(1);
	}
	
	@NonNull
	public KryoGameInfo getCachedBaseUiInfo(@NonNull final Path path) throws IOException {
		final Kryo kryo = kryoService.getKryoForUICatalog();
		final List<Class<?>> payloadClasses = new ArrayList<>(2);
		payloadClasses.add(KryoGameInfo.class);
		payloadClasses.add(UICatalogImpl.class);
		return (KryoGameInfo) kryoService.get(path, payloadClasses, kryo, 0).get(0);
	}
	
	/**
	 * @param gameDefName
	 * @param isPtr
	 * @throws IOException
	 */
	public void remove(@NonNull final String gameDefName, final boolean isPtr) throws IOException {
		final Path p = getCacheFilePath(gameDefName, isPtr);
		if (Files.exists(p)) {
			Files.delete(p);
			log.trace("Cleaning cache of {} in {}", () -> gameDefName, p::toAbsolutePath);
		} else {
			log.trace("Could not find cache of {} in {} to clean it", () -> gameDefName, p::toAbsolutePath);
		}
	}
	
}
