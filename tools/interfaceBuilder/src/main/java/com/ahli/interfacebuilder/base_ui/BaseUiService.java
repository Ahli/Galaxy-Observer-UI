// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.base_ui;

import com.ahli.galaxy.game.Game;
import com.ahli.galaxy.game.GameDef;
import com.ahli.galaxy.parser.DeduplicationIntensity;
import com.ahli.galaxy.parser.UICatalogParser;
import com.ahli.galaxy.parser.XmlParserAalto;
import com.ahli.galaxy.ui.UICatalogImpl;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import com.ahli.interfacebuilder.compress.GameService;
import com.ahli.interfacebuilder.config.ConfigService;
import com.ahli.interfacebuilder.integration.FileService;
import com.ahli.interfacebuilder.integration.SettingsIniInterface;
import com.ahli.interfacebuilder.integration.kryo.KryoGameInfo;
import com.ahli.interfacebuilder.integration.kryo.KryoService;
import com.ahli.interfacebuilder.integration.log4j.StylizedTextAreaAppender;
import com.ahli.interfacebuilder.projects.enums.GameType;
import com.ahli.interfacebuilder.ui.PrimaryStageHolder;
import com.ahli.interfacebuilder.ui.progress.ProgressController;
import com.ahli.interfacebuilder.ui.progress.appenders.Appender;
import com.esotericsoftware.kryo.Kryo;
import com.kichik.pecoff4j.PE;
import com.kichik.pecoff4j.ResourceDirectory;
import com.kichik.pecoff4j.ResourceEntry;
import com.kichik.pecoff4j.constant.ResourceType;
import com.kichik.pecoff4j.io.PEParser;
import com.kichik.pecoff4j.io.ResourceParser;
import com.kichik.pecoff4j.resources.StringFileInfo;
import com.kichik.pecoff4j.resources.StringTable;
import com.kichik.pecoff4j.resources.VersionInfo;
import com.kichik.pecoff4j.util.ResourceHelper;
import lombok.extern.log4j.Log4j2;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2
public class BaseUiService {
	private static final String META_FILE_NAME = ".meta";
	private final ConfigService configService;
	private final GameService gameService;
	private final FileService fileService;
	private final DiscCacheService discCacheService;
	private final KryoService kryoService;
	private final ProgressController progressController;
	private final Map<String, Lock> baseUiParsingLock;
	private final PrimaryStageHolder primaryStage;
	
	public BaseUiService(
			@NonNull final ConfigService configService,
			@NonNull final GameService gameService,
			@NonNull final FileService fileService,
			@NonNull final DiscCacheService discCacheService,
			@NonNull final KryoService kryoService,
			@NonNull final ProgressController progressController,
			@NonNull final PrimaryStageHolder primaryStage) {
		this.configService = configService;
		this.gameService = gameService;
		this.fileService = fileService;
		this.discCacheService = discCacheService;
		this.kryoService = kryoService;
		this.progressController = progressController;
		this.primaryStage = primaryStage;
		baseUiParsingLock = new ConcurrentHashMap<>(2, 1.0f);
	}
	
	/**
	 * Checks if the specified game's baseUI is older than the game files.
	 *
	 * @param gameType
	 * @param usePtr
	 * @return true, if outdated
	 */
	public boolean isOutdated(@NonNull final GameType gameType, final boolean usePtr) throws IOException {
		final GameDef gameDef = gameService.getGameDef(gameType);
		final Path gameBaseUI = configService.getBaseUiPath(gameDef);
		
		if (!Files.exists(gameBaseUI) || fileService.isEmptyDirectory(gameBaseUI)) {
			return true;
		}
		
		final Path baseUiMetaFileDir = configService.getBaseUiPath(gameDef);
		final KryoGameInfo baseUiInfo;
		try {
			baseUiInfo = readMetaFile(baseUiMetaFileDir);
			if (baseUiInfo == null) {
				return true;
			}
		} catch (final IOException e) {
			final String msg = "Failed to read Game Info from extracted base UI.";
			log.warn(msg);
			log.trace(msg, e);
			return true;
		}
		final int[] versionBaseUi = baseUiInfo.getVersion();
		final int[] versionExe = getVersion(gameDef, usePtr);
		boolean isUpToDate = true;
		for (int i = 0; i < versionExe.length && isUpToDate; ++i) {
			isUpToDate = versionExe[i] <= versionBaseUi[i];
		}
		if (log.isTraceEnabled()) {
			log.trace("Exe version check - exe={} - {} - upToDate={}",
					Arrays.toString(versionExe),
					Arrays.toString(versionBaseUi),
					isUpToDate);
		}
		
		return !isUpToDate;
	}
	
	@Nullable
	private KryoGameInfo readMetaFile(@NonNull final Path directory) throws IOException {
		final Path path = directory.resolve(META_FILE_NAME);
		if (Files.exists(path)) {
			final Kryo kryo = kryoService.getKryoForBaseUiMetaFile();
			final List<Class<?>> payloadClasses = new ArrayList<>(1);
			payloadClasses.add(KryoGameInfo.class);
			return (KryoGameInfo) kryoService.get(path, payloadClasses, kryo).get(0);
		}
		return null;
	}
	
	@NonNull
	public int[] getVersion(@NonNull final GameDef gameDef, final boolean isPtr) {
		final int[] versions = new int[4];
		final Path path = Path.of(gameService.getGameDirPath(gameDef, isPtr),
				gameDef.supportDirectoryX64(),
				gameDef.switcherExeNameX64());
		try {
			final PE pe = PEParser.parse(path.toFile());
			final ResourceDirectory rd = pe.getImageData().getResourceTable();
			
			final ResourceEntry[] entries = ResourceHelper.findResources(rd, ResourceType.VERSION_INFO);
			
			for (final ResourceEntry entry : entries) {
				final byte[] data = entry.getData();
				@SuppressWarnings("ObjectAllocationInLoop")
				final VersionInfo version = ResourceParser.readVersionInfo(data);
				
				final StringFileInfo strings = version.getStringFileInfo();
				final StringTable table = strings.getTable(0);
				for (int j = 0; j < table.getCount(); ++j) {
					final String key = table.getString(j).getKey();
					if ("FileVersion".equals(key)) {
						final String value = table.getString(j).getValue();
						log.trace("found FileVersion={}", value);
						
						final String[] parts = value.split("\\.");
						for (int k = 0; k < 4; ++k) {
							versions[k] = Integer.parseInt(parts[k]);
						}
						return versions;
					}
				}
			}
		} catch (final IOException e) {
			log.error("Error attempting to parse FileVersion from game's exe: ", e);
		}
		return versions;
	}
	
	/**
	 * Creates Tasks that will extract the base UI for a specified game.
	 *
	 * @param gameType
	 * @param usePtr
	 * @param outputs
	 * @return list of executable tasks
	 */
	@NonNull
	public List<ForkJoinTask<Void>> createExtractionTasks(
			@NonNull final GameType gameType, final boolean usePtr, @NonNull final Appender[] outputs) {
		log.info("Extracting baseUI for {}", gameType);
		
		final GameDef gameDef = gameService.getGameDef(gameType);
		final Path destination = configService.getBaseUiPath(gameDef);
		
		try {
			if (!Files.exists(destination)) {
				Files.createDirectories(destination);
			} else {
				fileService.cleanDirectory(destination);
			}
			discCacheService.remove(gameDef.name(), usePtr);
		} catch (final IOException e) {
			log.error(String.format("Directory %s could not be cleaned.", destination), e);
			return Collections.emptyList();
		}
		
		final List<ForkJoinTask<Void>> tasks = new ArrayList<>(4);
		final File extractorExe = configService.getCascExtractorExeFile();
		final String gamePath = gameService.getGameDirPath(gameDef, usePtr);
		final String[] queryMasks = getQueryMasks(gameType);
		int i = 0;
		for (final String mask : queryMasks) {
			final Appender outputAppender = outputs[i++];
			//noinspection ObjectAllocationInLoop
			tasks.add(new CascFileExtractionTask(extractorExe, gamePath, mask, destination, outputAppender));
		}
		
		final ForkJoinTask<Void> task = new ExtractVersionTask(gameDef, usePtr, destination, this, kryoService);
		tasks.add(task);
		
		return tasks;
	}
	
	/**
	 * @param gameType
	 * @return
	 */
	@NonNull
	public static String[] getQueryMasks(@NonNull final GameType gameType) {
		return switch (gameType) {
			case SC2 -> new String[] { "*.SC2Layout", "*Assets.txt", "*.SC2Style" };
			case HEROES -> new String[] { "*.StormLayout", "*Assets.txt", "*.StormStyle" };
		};
	}
	
	/**
	 * Parses the baseUI of the GameData, if that is required for the settings.
	 *
	 * @param game
	 * @param useCmdLineSettings
	 */
	public void parseBaseUiIfNecessary(@NonNull final Game game, final boolean useCmdLineSettings) throws Exception {
		final boolean verifyLayout;
		final SettingsIniInterface settings = configService.getIniSettings();
		if (useCmdLineSettings) {
			verifyLayout = settings.isCmdLineVerifyLayout();
		} else {
			verifyLayout = settings.isGuiVerifyLayout();
		}
		if (game.getUiCatalog() == null && verifyLayout) {
			parseBaseUI(game);
		}
	}
	
	/**
	 * Parses the baseUI of the specified game. The parsing of the baseUI is synchronized.
	 *
	 * @param game
	 * 		game whose default UI is parsed
	 * @throws Exception
	 */
	public void parseBaseUI(@NonNull final Game game) throws Exception {
		// lock per game
		final String gameBaseUiDir =
				configService.getBaseUiPath(game.getGameDef()) + File.separator + game.getGameDef().modsSubDirectory();
		final Lock LOCK = getLock(gameBaseUiDir);
		LOCK.lock();
		try {
			final String gameName = game.getGameDef().name();
			UICatalog uiCatalog = game.getUiCatalog();
			if (uiCatalog != null) {
				log.trace("Aborting parsing baseUI for '{}' as was already parsed.", gameName);
			} else {
				final long startTime = System.currentTimeMillis();
				log.info("Loading baseUI for {}", gameName);
				boolean needToParseAgain = true;
				
				boolean isPtr = false;
				try {
					isPtr = isPtr(configService.getBaseUiPath(game.getGameDef()));
				} catch (final IOException e) {
					// do nothing
					log.trace("Ignoring error in isPtr() check on baseUiPath.", e);
				}
				try {
					if (cacheIsUpToDateCheckException(game.getGameDef(), isPtr)) {
						// load from cache
						uiCatalog = discCacheService.getCachedBaseUi(gameName, isPtr);
						game.setUiCatalog(uiCatalog);
						needToParseAgain = false;
						log.trace("Loaded baseUI for '{}' from cache", gameName);
					}
				} catch (final IOException e) {
					log.warn("ERROR: loading cached base UI failed.", e);
				}
				if (needToParseAgain) {
					// parse baseUI
					uiCatalog = new UICatalogImpl(game.getGameDef());
					uiCatalog.setParser(new UICatalogParser(uiCatalog,
							new XmlParserAalto(),
							DeduplicationIntensity.FULL));
					primaryStage.printInfoLogMessageToGeneral("Starting to parse base " + gameName + " UI.");
					if (primaryStage.hasPrimaryStage()) {
						progressController.addThreadlogTab(Thread.currentThread().getName(),
								game.getGameDef().nameHandle() + "UI",
								false);
					}
					try {
						for (final String modOrDir : game.getGameDef().coreModsOrDirectories()) {
							
							
							final Path directory = Path.of(gameBaseUiDir + File.separator + modOrDir);
							if (!Files.exists(directory) || !Files.isDirectory(directory)) {
								throw new IOException("BaseUI does not exist.");
							}
							
							final BaseUiDescIndexFileParsingVisitor fileVisitor =
									new BaseUiDescIndexFileParsingVisitor(game.getGameDef(), uiCatalog);
							Files.walkFileTree(directory, fileVisitor);
							final Optional<Exception> exception = fileVisitor.getException();
							if (exception.isPresent()) {
								throw exception.get();
							}
						}
						uiCatalog.postProcessParsing();
						game.setUiCatalog(uiCatalog);
						log.trace("Parsed BaseUI for {}", gameName);
					} finally {
						uiCatalog.setParser(null);
					}
					final String msg = "Finished parsing base UI for " + gameName + ".";
					log.info(msg);
					primaryStage.printInfoLogMessageToGeneral(msg);
					try {
						discCacheService.put(uiCatalog, gameName, isPtr, getVersion(game.getGameDef(), isPtr));
					} catch (final IOException e) {
						log.error("ERROR when creating cache file of UI", e);
					}
				}
				final long executionTime = (System.currentTimeMillis() - startTime);
				log.info("Loading BaseUI for {} took {}ms.", gameName, executionTime);
				if (needToParseAgain) {
					// set result in UI
					StylizedTextAreaAppender.finishedWork(Thread.currentThread().getName(), true, 50);
				}
			}
		} finally {
			LOCK.unlock();
		}
	}
	
	@NonNull
	private Lock getLock(@NonNull final String gameName) {
		return baseUiParsingLock.computeIfAbsent(gameName, k -> new ReentrantLock());
	}
	
	/**
	 * Checks if the baseUiDirectory is PTR.
	 *
	 * @param baseUiDirectory
	 * @return true if PTR; false if not or no file is existing
	 * @throws IOException
	 */
	public boolean isPtr(@NonNull final Path baseUiDirectory) throws IOException {
		final KryoGameInfo kryoGameInfo = readMetaFile(baseUiDirectory);
		return kryoGameInfo != null && kryoGameInfo.isPtr();
	}
	
	public boolean cacheIsUpToDateCheckException(@NonNull final GameDef gameDef, final boolean usePtr) {
		try {
			return cacheIsUpToDate(gameDef, usePtr);
		} catch (final NoSuchFileException e) {
			log.trace(String.format("No cache exists for %s", gameDef.name()), e);
		} catch (final IOException e) {
			log.info(String.format("Failed to check cache status of %s:", gameDef.name()), e);
		}
		return false;
	}
	
	public boolean cacheIsUpToDate(@NonNull final GameDef gameDef, final boolean usePtr) throws IOException {
		final Path baseUiMetaFileDir = configService.getBaseUiPath(gameDef);
		final Path cacheFilePath = discCacheService.getCacheFilePath(gameDef.name(), usePtr);
		return cacheIsUpToDate(cacheFilePath, baseUiMetaFileDir);
	}
	
	/**
	 * @param cacheFile
	 * @param metaFileDir
	 * @return
	 */
	public boolean cacheIsUpToDate(@NonNull final Path cacheFile, @NonNull final Path metaFileDir) throws IOException {
		// no cache -> not up to date
		if (!Files.exists(cacheFile)) {
			return false;
		}
		
		final KryoGameInfo cacheInfo = discCacheService.getCachedBaseUiInfo(cacheFile);
		final int[] versionCache = cacheInfo.getVersion();
		
		final KryoGameInfo baseUiInfo = readMetaFile(metaFileDir);
		if (baseUiInfo == null) {
			return false;
		}
		final int[] versionBaseUi = baseUiInfo.getVersion();
		
		boolean isUpToDate = true;
		for (int i = 0; i < versionCache.length && isUpToDate; ++i) {
			isUpToDate = versionCache[i] == versionBaseUi[i];
		}
		log.trace("Cache and baseUI versions match: {}", isUpToDate);
		return isUpToDate;
	}
	
	/**
	 * @return
	 */
	public boolean isHeroesPtrActive() {
		final Path baseUiMetaFileDir = configService.getBaseUiPath(gameService.getGameDef(GameType.HEROES));
		try {
			final KryoGameInfo baseUiInfo = readMetaFile(baseUiMetaFileDir);
			if (baseUiInfo != null) {
				return baseUiInfo.isPtr();
			}
		} catch (final IOException e) {
			log.error("ERROR while checking if ptr is active via baseUI cache file", e);
		}
		return false;
	}
	
	private static final class BaseUiDescIndexFileParsingVisitor extends SimpleFileVisitor<Path> {
		private final String descIndexFileName;
		private final UICatalog uiCatalog;
		private final GameDef gameDef;
		private Exception exception;
		
		private BaseUiDescIndexFileParsingVisitor(@NonNull final GameDef gameDef, @NonNull final UICatalog uiCatalog) {
			this.uiCatalog = uiCatalog;
			this.gameDef = gameDef;
			descIndexFileName = "descindex." + gameDef.layoutFileEnding();
		}
		
		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
			if (descIndexFileName.equalsIgnoreCase(file.getFileName().toString())) {
				log.info("parsing descIndexFile '{}'", file);
				try {
					uiCatalog.processDescIndex(file, gameDef.defaultRaceId(), gameDef.defaultConsoleSkinId());
				} catch (final InterruptedException e) {
					log.error("Interrupted while processing DescIndex {}", file, e);
					Thread.currentThread().interrupt();
					exception = e;
					return FileVisitResult.TERMINATE;
				} catch (final Exception e) {
					log.error("Exception while processing DescIndex {}", file, e);
					exception = e;
					return FileVisitResult.TERMINATE;
				}
			}
			return FileVisitResult.CONTINUE;
		}
		
		@NonNull
		public Optional<Exception> getException() {
			return Optional.ofNullable(exception);
		}
	}
	
	private static final class CascFileExtractionTask extends RecursiveAction {
		@Serial
		private static final long serialVersionUID = -775938058915435006L;
		
		private final File extractorExe;
		private final String gamePath;
		private final String mask;
		private final transient Path destination;
		private final transient Appender outputAppender;
		
		private CascFileExtractionTask(
				@NonNull final File extractorExe,
				@NonNull final String gamePath,
				@NonNull final String mask,
				@NonNull final Path destination,
				@Nullable final Appender outputAppender) {
			this.extractorExe = extractorExe;
			this.gamePath = gamePath;
			this.mask = mask;
			this.destination = destination;
			this.outputAppender = outputAppender;
		}
		
		@Override
		protected void compute() {
			try {
				if (extract(extractorExe, gamePath, mask, destination, outputAppender)) {
					Thread.sleep(50);
					if (extract(extractorExe, gamePath, mask, destination, outputAppender)) {
						log.warn(
								"Extraction failed due to a file access. Try closing the Battle.net App, if it is running and this fails to extract all files.");
					}
				}
			} catch (final IOException e) {
				log.error("Extracting files from CASC via CascExtractor failed.", e);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			} finally {
				if (outputAppender != null) {
					outputAppender.end();
				}
			}
		}
		
		/**
		 * @param extractorExe
		 * @param gamePath
		 * @param mask
		 * @param destination
		 * @param out
		 * @return
		 * @throws IOException
		 * @throws InterruptedException
		 */
		private static boolean extract(
				@NonNull final File extractorExe,
				@NonNull final String gamePath,
				@NonNull final String mask,
				@NonNull final Path destination,
				@Nullable final Appender out) throws IOException, InterruptedException {
			final ProcessBuilder pb = new ProcessBuilder(extractorExe.getAbsolutePath(),
					gamePath + File.separator,
					mask,
					destination + File.separator);
			// put error and normal output into the same stream
			pb.redirectErrorStream(true);
			
			boolean retry = false;
			final Process process = pb.start();
			// empty output buffers and print to console
			try (final InputStream is = process.getInputStream()) {
				do {
					//noinspection BusyWait
					Thread.sleep(50);
					final String processLog = new String(is.readAllBytes(), StandardCharsets.UTF_8);
					if (processLog.contains(
							"Unhandled Exception: System.IO.IOException: The process cannot access the file")) {
						retry = true;
					} else {
						if (out != null) {
							out.append(processLog);
						} else {
							log.info(processLog);
						}
					}
				} while (process.isAlive());
			}
			return retry;
		}
	}
	
	private static final class ExtractVersionTask extends RecursiveAction {
		
		@Serial
		private static final long serialVersionUID = 3071926102876787289L;
		
		private final transient GameDef gameDef;
		private final boolean usePtr;
		private final transient Path destination;
		private final transient BaseUiService baseUiService;
		private final transient KryoService kryoService;
		
		private ExtractVersionTask(
				@NonNull final GameDef gameDef,
				final boolean usePtr,
				@NonNull final Path destination,
				@NonNull final BaseUiService baseUiService,
				@NonNull final KryoService kryoService) {
			this.gameDef = gameDef;
			this.usePtr = usePtr;
			this.destination = destination;
			this.baseUiService = baseUiService;
			this.kryoService = kryoService;
		}
		
		@Override
		protected void compute() {
			final int[] version = baseUiService.getVersion(gameDef, usePtr);
			try {
				writeToMetaFile(destination, gameDef.name(), version, usePtr);
			} catch (final IOException e) {
				log.error("Failed to write metafile: ", e);
			}
		}
		
		private void writeToMetaFile(
				@NonNull final Path directory,
				@NonNull final String gameName,
				@NonNull final int[] version,
				final boolean isPtr) throws IOException {
			final Path path = directory.resolve(META_FILE_NAME);
			final KryoGameInfo metaInfo = new KryoGameInfo(version, gameName, isPtr);
			final List<Object> payload = new ArrayList<>(1);
			payload.add(metaInfo);
			final Kryo kryo = kryoService.getKryoForBaseUiMetaFile();
			kryoService.put(path, payload, kryo);
		}
	}
}
