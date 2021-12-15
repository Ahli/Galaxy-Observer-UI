// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.base_ui;

import com.ahli.galaxy.ui.UICatalogImpl;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import com.ahli.interfacebuilder.config.ConfigService;
import com.ahli.interfacebuilder.integration.kryo.KryoGameInfo;
import com.ahli.interfacebuilder.integration.kryo.KryoService;
import com.esotericsoftware.kryo.Kryo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DiscCacheService {
	private static final Logger logger = LogManager.getLogger(DiscCacheService.class);
	private final ConfigService configService;
	private final KryoService kryoService;
	
	public DiscCacheService(final ConfigService configService, final KryoService kryoService) {
		this.configService = configService;
		this.kryoService = kryoService;
	}
	
	/**
	 * @param catalog
	 * @param gameDefName
	 * @throws IOException
	 */
	public void put(final UICatalog catalog, final String gameDefName, final boolean isPtr, final int[] version)
			throws IOException {
		final Path p = getCacheFilePath(gameDefName, isPtr);
		Files.deleteIfExists(p);
		
		final KryoGameInfo metaInfo = new KryoGameInfo(version, gameDefName, isPtr);
		final List<Object> payload = new ArrayList<>(2);
		payload.add(metaInfo);
		payload.add(catalog);
		
		final Kryo kryo = kryoService.getKryoForUICatalog();
		kryoService.put(p, payload, kryo);
		logger.info(
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
	public Path getCacheFilePath(final String gameDefName, final boolean isPtr) {
		return configService.getCachePath().resolve(gameDefName + (isPtr ? " PTR" : "") + ".kryo");
	}
	
	/**
	 * @param gameDefName
	 * @param isPtr
	 * @return
	 * @throws IOException
	 */
	public UICatalog getCachedBaseUi(final String gameDefName, final boolean isPtr) throws IOException {
		return getCachedBaseUi(getCacheFilePath(gameDefName, isPtr));
	}
	
	public UICatalog getCachedBaseUi(final Path path) throws IOException {
		final Kryo kryo = kryoService.getKryoForUICatalog();
		final List<Class<?>> payloadClasses = new ArrayList<>(2);
		payloadClasses.add(KryoGameInfo.class);
		payloadClasses.add(UICatalogImpl.class);
		return (UICatalog) kryoService.get(path, payloadClasses, kryo).get(1);
	}
	
	public KryoGameInfo getCachedBaseUiInfo(final Path path) throws IOException {
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
	public void remove(final String gameDefName, final boolean isPtr) throws IOException {
		final Path p = getCacheFilePath(gameDefName, isPtr);
		if (Files.exists(p)) {
			Files.delete(p);
			logger.trace("Cleaning cache of {} in {}", () -> gameDefName, p::toAbsolutePath);
		} else {
			logger.trace("Could not find cache of {} in {} to clean it", () -> gameDefName, p::toAbsolutePath);
		}
	}
	
	/**
	 * @param gameDefName
	 * @param isPtr
	 * @return
	 */
	public boolean exists(final String gameDefName, final boolean isPtr) {
		return Files.exists(getCacheFilePath(gameDefName, isPtr));
	}
	
}
