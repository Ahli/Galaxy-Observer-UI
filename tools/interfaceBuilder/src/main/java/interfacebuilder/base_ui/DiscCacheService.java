// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.base_ui;

import com.ahli.galaxy.ui.UICatalogImpl;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import com.esotericsoftware.kryo.Kryo;
import interfacebuilder.config.ConfigService;
import interfacebuilder.integration.kryo.KryoCachedBaseUi;
import interfacebuilder.integration.kryo.KryoGameInfo;
import interfacebuilder.integration.kryo.KryoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
		final List<Object> payload = new ArrayList<>();
		payload.add(new KryoCachedBaseUi(metaInfo, (UICatalogImpl) catalog));
		
		final Kryo kryo = kryoService.getKryoForUICatalog();
		kryoService.put(p, payload, kryo);
		logger.info("Cached UI for " + gameDefName + " - templates=" + catalog.getTemplates().size() +
				", blizzOnlyTemplates=" + catalog.getBlizzOnlyTemplates().size() + ", constants=" +
				catalog.getConstants().size() + ", blzzOnlyConstants=" + catalog.getBlizzOnlyConstants().size() +
				", devLayouts=" + catalog.getDevLayouts().size());
	}
	
	/**
	 * @param gameDefName
	 * @return
	 */
	public File getCacheFile(final String gameDefName, final boolean isPtr) {
		final String path =
				configService.getCachePath() + File.separator + gameDefName + (isPtr ? " PTR" : "") + ".kryo";
		return new File(path);
	}
	
	
	/**
	 * @param gameDefName
	 * @param isPtr
	 * @return
	 * @throws IOException
	 */
	public KryoCachedBaseUi getCachedBaseUi(final String gameDefName, final boolean isPtr) throws IOException {
		return getCachedBaseUi(getCacheFile(gameDefName, isPtr).toPath());
	}
	
	public KryoCachedBaseUi getCachedBaseUi(final Path path) throws IOException {
		final Kryo kryo = kryoService.getKryoForUICatalog();
		final List<Class<? extends Object>> payloadClasses = new ArrayList<>();
		//		payloadClasses.add(UICatalogImpl.class);
		//		payloadClasses.add(KryoGameInfo.class);
		//		return kryoService.get(path, payloadClasses, kryo);
		payloadClasses.add(KryoCachedBaseUi.class);
		return (KryoCachedBaseUi) kryoService.get(path, payloadClasses, kryo).get(0);
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
