// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.compress;

import com.ahli.galaxy.ModD;
import com.ahli.mpq.MpqEditorInterface;
import com.ahli.mpq.MpqException;
import com.ahli.mpq.mpqeditor.MpqEditorCompression;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRule;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRuleMask;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRuleMethod;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRuleSize;
import interfacebuilder.integration.FileService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectLongHashMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

/**
 * This miner searches for a better compression via randomized rules.
 */
public class RandomCompressionMiner {
	private static final String WILDCARD = "*";
	private static final String OGG = ".ogg";
	private static final String OGV = ".ogv";
	private static final String WAV = ".wav";
	private static final String TXT = ".txt";
	private static final String SWF = ".swf";
	private static final String TTF = ".ttf";
	private static final String OTF = ".otf";
	private static final String M_3 = ".m3";
	private static final Logger logger = LogManager.getLogger(RandomCompressionMiner.class);
	private final ModD mod;
	private final MpqEditorInterface mpqInterface;
	private final MpqEditorCompressionRuleMethod[] compressionSetting = MpqEditorCompressionRuleMethod.values();
	private ObjectLongHashMap<String> fileSizeMap;
	private MpqEditorCompressionRule[] rules;
	private long bestSize;
	private MpqEditorCompressionRule[] bestRuleSet;
	private boolean oldRulesetHadMissingFiles;
	
	/**
	 * Creates a Miner with a ruleset whose entries all refer to exactly a single file within the source location.
	 *
	 * @param mod
	 * @param mpqCachePath
	 * @param mpqEditorPath
	 * @param fileService
	 * @throws IOException
	 * @throws MpqException
	 * @throws InterruptedException
	 */
	public RandomCompressionMiner(
			final ModD mod, final Path mpqCachePath, final Path mpqEditorPath, final FileService fileService)
			throws IOException, MpqException, InterruptedException {
		this(mod, mpqCachePath, mpqEditorPath, new MpqEditorCompressionRule[0], fileService);
	}
	
	/**
	 * Creates a Miner with a ruleset whose entries all refer to exactly a single file based on the specified ruleset.
	 *
	 * @param mod
	 * @param mpqCachePath
	 * @param mpqEditorPath
	 * @param oldBestRuleset
	 * @param fileService
	 * @throws IOException
	 * @throws MpqException
	 * @throws InterruptedException
	 */
	public RandomCompressionMiner(
			final ModD mod,
			final Path mpqCachePath,
			final Path mpqEditorPath,
			final MpqEditorCompressionRule[] oldBestRuleset,
			final FileService fileService) throws IOException, MpqException, InterruptedException {
		this.mod = mod;
		final String id = Long.toString(Thread.currentThread().getId());
		mpqInterface = new MpqEditorInterface(mpqCachePath.resolve(id), mpqEditorPath);
		final Path cacheDir = mpqInterface.getCache();
		mod.setMpqCacheDirectory(cacheDir);
		mpqInterface.clearCacheExtractedMpq();
		fileService.copyFileOrDirectory(mod.getSourceDirectory(), cacheDir);
		boolean compressXml = true;
		
		// use old ruleset if one was specified and test it
		if (oldBestRuleset != null) {
			// check for file coverage holes
			final List<File> untrackedFiles = getUntrackedFiles(oldBestRuleset, cacheDir);
			if (!untrackedFiles.isEmpty()) {
				rules = addRulesForFiles(oldBestRuleset, untrackedFiles, cacheDir);
				oldRulesetHadMissingFiles = true;
			} else {
				// do not modify the original array
				rules = oldBestRuleset.clone();
			}
			rules = removeUnusedMaskEnries(rules, cacheDir);
			fillFileSizeMap(rules, cacheDir);
			replaceForbiddenRulesets(rules);
			
			bestRuleSet = deepCopy(rules);
			bestSize = build(rules, true);
			// only compress once
			compressXml = false;
		} else {
			bestSize = Long.MAX_VALUE;
		}
		
		// compare with usual, initial ruleset and use the better one
		final MpqEditorCompressionRule[] initRules = createInitialRuleset(cacheDir);
		final long initRuleSetSize = build(initRules, compressXml);
		if (initRuleSetSize < bestSize) {
			bestSize = initRuleSetSize;
			rules = initRules;
			bestRuleSet = deepCopy(initRules);
		}
	}
	
	/**
	 * Returns a list of files from the specified cache directory that are not tracked by the specified rules.
	 *
	 * @param rules
	 * @param cacheModDirectory
	 * @return list of files within the cache that are not covered by the rules
	 * @throws IOException
	 */
	public static List<File> getUntrackedFiles(final MpqEditorCompressionRule[] rules, final Path cacheModDirectory)
			throws IOException {
		final List<File> untrackedFiles = new ArrayList<>();
		try (final Stream<Path> ps = Files.walk(cacheModDirectory)) {
			ps.filter(Files::isRegularFile).forEach(p -> {
				if (!containsFile(rules, p)) {
					untrackedFiles.add(p.toFile());
				}
			});
		}
		return untrackedFiles;
	}
	
	/**
	 * Adds rules for files.
	 *
	 * @param oldBestRuleset
	 * @param untrackedFiles
	 * @param cacheDir
	 * @return
	 */
	private static MpqEditorCompressionRule[] addRulesForFiles(
			final MpqEditorCompressionRule[] oldBestRuleset, final List<File> untrackedFiles, final Path cacheDir) {
		final int oldRuleCount = oldBestRuleset.length;
		final MpqEditorCompressionRule[] merged = new MpqEditorCompressionRule[oldRuleCount + untrackedFiles.size()];
		// copy old
		System.arraycopy(oldBestRuleset, 0, merged, 0, oldRuleCount);
		// insert new untracked
		for (int i = 0, len = untrackedFiles.size(); i < len; ++i) {
			//noinspection ObjectAllocationInLoop
			merged[oldRuleCount + i] = new MpqEditorCompressionRuleMask(getFileMask(untrackedFiles.get(i).toPath(),
					cacheDir)).setSingleUnit(true)
					.setCompress(true)
					.setCompressionMethod(MpqEditorCompressionRuleMethod.NONE);
		}
		return merged;
	}
	
	/**
	 * Removes all entries of a specified ruleset that are mask rules and .
	 *
	 * @param dirty
	 * @return
	 */
	private static MpqEditorCompressionRule[] removeUnusedMaskEnries(
			final MpqEditorCompressionRule[] dirty, final Path cacheDir) {
		final List<MpqEditorCompressionRule> clean = new ArrayList<>();
		String mask;
		for (final MpqEditorCompressionRule rule : dirty) {
			if (rule instanceof final MpqEditorCompressionRuleMask ruleMask) {
				mask = ruleMask.getMask();
				if (isValidFileSpecificMask(mask, cacheDir)) {
					clean.add(rule);
				} else {
					logger.trace("removing rule from ruleset due to invalid mask: {}", mask);
				}
			} else {
				if (rule != null) {
					clean.add(rule);
				} else {
					logger.trace("removing null entry from ruleset");
				}
			}
		}
		return clean.toArray(new MpqEditorCompressionRule[0]);
	}
	
	/**
	 * @param rules
	 * @param cache
	 */
	private void fillFileSizeMap(final MpqEditorCompressionRule[] rules, final Path cache) {
		fileSizeMap = new ObjectLongHashMap<>();
		for (final MpqEditorCompressionRule rule : rules) {
			if (rule instanceof final MpqEditorCompressionRuleMask ruleMask) {
				final String mask = ruleMask.getMask();
				fileSizeMap.put(mask, getFileSize(mask, cache));
			}
		}
	}
	
	/**
	 * @param rules
	 */
	private static void replaceForbiddenRulesets(final MpqEditorCompressionRule[] rules) {
		for (final MpqEditorCompressionRule rule : rules) {
			if (rule instanceof MpqEditorCompressionRuleMask) {
				if (hasInvalidCompressionRuleset(rule.getCompressionMethod())) {
					rule.setCompressionMethod(MpqEditorCompressionRuleMethod.BZIP2);
				}
			} else {
				// Size-rule 0-0 is forced to use compression NONE
				if (rule instanceof final MpqEditorCompressionRuleSize ruleSize && ruleSize.getMaxSize() == 0 &&
						rule.getCompressionMethod() != MpqEditorCompressionRuleMethod.NONE) {
					ruleSize.setMinSize(0);
					rule.setCompressionMethod(MpqEditorCompressionRuleMethod.NONE);
				}
			}
		}
	}
	
	/**
	 * Creates a deep copy of the specified list of rules.
	 *
	 * @param rules
	 * @return
	 */
	private static MpqEditorCompressionRule[] deepCopy(final MpqEditorCompressionRule... rules) {
		final int len = rules.length;
		final MpqEditorCompressionRule[] clone = new MpqEditorCompressionRule[len];
		for (int i = 0; i < len; ++i) {
			clone[i] = ((MpqEditorCompressionRule) rules[i].deepCopy());
		}
		return clone;
	}
	
	/**
	 * Build the mpq with the specified ruleset. CompressXml should only be enabled for the first build.
	 *
	 * @param rules
	 * 		compression rules to use
	 * @param compressXml
	 * 		compresses the XML file content if true
	 * @return returns the size of the built artifact
	 * @throws InterruptedException
	 * 		when build was interrupted
	 * @throws MpqException
	 * @throws IOException
	 */
	private long build(final MpqEditorCompressionRule[] rules, final boolean compressXml)
			throws InterruptedException, MpqException, IOException {
		mpqInterface.setCustomCompressionRules(rules);
		final Path targetFile = mod.getTargetFile();
		mpqInterface.buildMpq(targetFile, compressXml, MpqEditorCompression.CUSTOM, false);
		return Files.size(targetFile);
	}
	
	/**
	 * Creates an initial ruleset.
	 *
	 * @param cacheModDirectory
	 * @return
	 * @throws IOException
	 */
	private MpqEditorCompressionRule[] createInitialRuleset(final Path cacheModDirectory) throws IOException {
		final List<MpqEditorCompressionRule> initRules = new ArrayList<>(20);
		initRules.add(new MpqEditorCompressionRuleSize(0, 0).setSingleUnit(true));
		final Path sourceDir = mod.getMpqCacheDirectory();
		fileSizeMap = new ObjectLongHashMap<>();
		try (final Stream<Path> ps = Files.walk(cacheModDirectory)) {
			ps.filter(Files::isRegularFile).forEach(p -> {
				final var compressionRule = getDefaultRule(p, sourceDir);
				initRules.add(compressionRule);
				fileSizeMap.put(compressionRule.getMask(), getFileSize(compressionRule.getMask(), cacheModDirectory));
			});
		}
		return initRules.toArray(new MpqEditorCompressionRule[0]);
	}
	
	/**
	 * @param rules
	 * @param p
	 * @return
	 */
	private static boolean containsFile(final MpqEditorCompressionRule[] rules, final Path p) {
		for (final MpqEditorCompressionRule rule : rules) {
			if (rule instanceof MpqEditorCompressionRuleMask ruleMask && p.toString().endsWith(cleanMask(ruleMask))) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the MpqCompressionRuleSet's mask String for the Path of an extracted interface's file.
	 *
	 * @param p
	 * @return
	 */
	private static String getFileMask(final Path p, final Path sourceDir) {
		final String name = p.normalize().toString();
		return name.substring(sourceDir.toString().length() + 1);
	}
	
	/**
	 * Checks whether or not a mask refers to an existing file within the specified cache directory.
	 *
	 * @param mask
	 * 		the mask
	 * @param cacheDir
	 * 		cache directory containing the map
	 * @return true if file exists within the cache and is not a directory
	 */
	private static boolean isValidFileSpecificMask(final String mask, final Path cacheDir) {
		if (mask.contains(WILDCARD)) {
			return false;
		}
		final Path referencedFile = Path.of(cacheDir.toString(), mask);
		return Files.isRegularFile(referencedFile);
	}
	
	/**
	 * @param mask
	 * @return
	 */
	private static long getFileSize(final String mask, final Path cache) {
		final Path path = Path.of(cache.toString(), mask);
		if (Files.exists(path) && Files.isRegularFile(path)) {
			try {
				return Files.size(path);
			} catch (final IOException e) {
				return 0;
			}
		}
		return 0;
	}
	
	/**
	 * @param method
	 * @return
	 */
	private static boolean hasInvalidCompressionRuleset(final MpqEditorCompressionRuleMethod method) {
		return method == MpqEditorCompressionRuleMethod.LZMA || method == MpqEditorCompressionRuleMethod.PKWARE ||
				method == MpqEditorCompressionRuleMethod.SPARSE;
	}
	
	/**
	 * @param path
	 * @param sourceDir
	 * @return
	 */
	private static MpqEditorCompressionRuleMask getDefaultRule(final Path path, final Path sourceDir) {
		final var rule = new MpqEditorCompressionRuleMask(getFileMask(path, sourceDir));
		rule.setSingleUnit(true).setCompress(true);
		if (path.endsWith(OGG) || path.endsWith(OGV) || path.endsWith(WAV) || path.endsWith(TXT) ||
				path.endsWith(SWF) || path.endsWith(TTF) || path.endsWith(OTF) || path.endsWith(M_3)) {
			rule.setCompressionMethod(MpqEditorCompressionRuleMethod.ZLIB);
		} else {
			rule.setCompressionMethod(MpqEditorCompressionRuleMethod.BZIP2);
		}
		return rule;
	}
	
	private static String cleanMask(final MpqEditorCompressionRuleMask ruleMask) {
		return File.separator + ruleMask.getMask().replace(WILDCARD, "");
	}
	
	/**
	 * Builds the archive with the compression rules.
	 *
	 * @return size of the built artifact
	 * @throws InterruptedException
	 * 		when build was interrupted
	 * @throws IOException
	 * 		when
	 * @throws MpqException
	 */
	public long build() throws InterruptedException, IOException, MpqException {
		final long size = build(rules, false);
		if (size < bestSize) {
			bestSize = size;
			bestRuleSet = deepCopy(rules);
		}
		return size;
	}
	
	/**
	 * Randomizes the compression of the saved rules.
	 */
	public void randomizeRules() {
		// fine for all: NONE, BZIP2, ZLIB, PKWARE, SPARSE, SPARSE_BZIP2, SPARSE_ZLIB
		// not fine: LZMA (crash during load)
		for (final MpqEditorCompressionRule rule : rules) {
			if (rule instanceof MpqEditorCompressionRuleMask ruleMask) {
				rule.setCompressionMethod(getRandomCompressionMethod(ruleMask.getMask()));
				rule.setSingleUnit(ThreadLocalRandom.current().nextBoolean());
			}
		}
	}
	
	/**
	 * Returns a random compression method.
	 *
	 * @return randomly selected compression method
	 */
	private MpqEditorCompressionRuleMethod getRandomCompressionMethod(final String mask) {
		// exclude LZMA -> crash
		MpqEditorCompressionRuleMethod method;
		final ThreadLocalRandom random = ThreadLocalRandom.current();
		if (fileSizeMap.get(mask) > 0) {
			method = random.nextFloat() > 0.95f ? MpqEditorCompressionRuleMethod.ZLIB :
					MpqEditorCompressionRuleMethod.BZIP2;
		} else {
			final int max = compressionSetting.length;
			do {
				method = compressionSetting[random.nextInt(max)];
			} while (hasInvalidCompressionRuleset(method));
		}
		return method;
	}
	
	public MpqEditorCompressionRule[] getBestCompressionRules() {
		return bestRuleSet;
	}
	
	public long getBestSize() {
		return bestSize;
	}
	
	public boolean isOldRulesetHadMissingFiles() {
		return oldRulesetHadMissingFiles;
	}
	
	/**
	 * Removes files that were used during the compression mining.
	 */
	public void cleanUp() {
		// clean up cache
		int i;
		for (i = 100; i > 0; --i) {
			if (mpqInterface.clearCacheExtractedMpq()) {
				break;
			} else {
				try {
					Thread.sleep(50);
				} catch (final InterruptedException ignored) {
					Thread.currentThread().interrupt();
					logger.trace("Interrupted while waiting to clean up");
				}
			}
		}
		if (i == 0) {
			logger.error("Failed to clean up compression mining cache");
		}
		// clean up target file
		final Path targetFile = mod.getTargetFile();
		for (i = 100; i > 0; --i) {
			try {
				Files.deleteIfExists(targetFile);
				return;
			} catch (final IOException e) {
				logger.trace("Error while cleaning up compression mining target file {}", targetFile, e);
				try {
					Thread.sleep(50);
				} catch (final InterruptedException ignored) {
					Thread.currentThread().interrupt();
					logger.trace("Interrupted while waiting to clean up");
				}
			}
		}
		logger.error("Failed to clean up compression mining target file");
	}
	
	public MpqEditorInterface getMpqInterface() {
		return mpqInterface;
	}
}
