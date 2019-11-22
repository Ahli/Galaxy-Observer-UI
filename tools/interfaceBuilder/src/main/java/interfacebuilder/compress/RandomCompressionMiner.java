// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.compress;

import com.ahli.galaxy.ModData;
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
import java.util.Random;
import java.util.stream.Stream;

/**
 * This miner searches for a better compression via randomized rules.
 */
public class RandomCompressionMiner {
	private static final String STAR = "*";
	private static final String OGG = ".ogg";
	private static final String OGV = ".ogv";
	private static final String WAV = ".wav";
	private static final String TXT = ".txt";
	private static final String SWF = ".swf";
	private static final String TTF = ".ttf";
	private static final String OTF = ".otf";
	private static final String M_3 = ".m3";
	private static final String WILDCARD = STAR;
	private static final Logger logger = LogManager.getLogger(RandomCompressionMiner.class);
	private final ModData mod;
	private final MpqEditorInterface mpqInterface;
	private final Random random = new Random();
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
	public RandomCompressionMiner(final ModData mod, final String mpqCachePath, final String mpqEditorPath,
			final FileService fileService) throws IOException, MpqException, InterruptedException {
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
	public RandomCompressionMiner(final ModData mod, final String mpqCachePath, final String mpqEditorPath,
			final MpqEditorCompressionRule[] oldBestRuleset, final FileService fileService)
			throws IOException, MpqException, InterruptedException {
		this.mod = mod;
		mpqInterface = new MpqEditorInterface(mpqCachePath + Thread.currentThread().getId(), mpqEditorPath);
		final File cacheDir = new File(mpqInterface.getMpqCachePath());
		mod.setMpqCacheDirectory(cacheDir);
		mpqInterface.clearCacheExtractedMpq();
		fileService.copyFileOrDirectory(mod.getSourceDirectory(), cacheDir);
		
		// use old ruleset if one was specified and test it
		if (oldBestRuleset != null) {
			// check for file coverage holes
			final List<File> untrackedFiles = getUntrackedFiles(oldBestRuleset, cacheDir.toPath());
			if (!untrackedFiles.isEmpty()) {
				rules = addRulesForFiles(oldBestRuleset, untrackedFiles, cacheDir);
				oldRulesetHadMissingFiles = true;
			} else {
				rules = oldBestRuleset;
			}
			rules = removeUnusedMaskEnries(rules, cacheDir);
			fillFileSizeMap(rules, cacheDir);
			replaceForbiddenRulesets(rules);
			
			bestRuleSet = deepCopy(rules);
			bestSize = build(rules, true);
			
		} else {
			bestSize = Long.MAX_VALUE;
		}
		
		// compare with usual, initial ruleset and use the better one
		final MpqEditorCompressionRule[] initRules = createInitialRuleset(cacheDir.toPath());
		final long initRuleSetSize = build(initRules, true);
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
	private static MpqEditorCompressionRule[] addRulesForFiles(final MpqEditorCompressionRule[] oldBestRuleset,
			final List<File> untrackedFiles, final File cacheDir) {
		final int oldRuleCount = oldBestRuleset.length;
		final MpqEditorCompressionRule[] merged = new MpqEditorCompressionRule[oldRuleCount + untrackedFiles.size()];
		// copy old
		System.arraycopy(oldBestRuleset, 0, merged, 0, oldRuleCount);
		// insert new untracked
		for (int i = 0, len = untrackedFiles.size(); i < len; i++) {
			merged[oldRuleCount + i] =
					new MpqEditorCompressionRuleMask(getFileMask(untrackedFiles.get(i).toPath(), cacheDir))
							.setSingleUnit(true).setCompress(true)
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
	private static MpqEditorCompressionRule[] removeUnusedMaskEnries(final MpqEditorCompressionRule[] dirty,
			final File cacheDir) {
		final List<MpqEditorCompressionRule> clean = new ArrayList<>();
		String mask;
		for (final MpqEditorCompressionRule rule : dirty) {
			if (rule instanceof MpqEditorCompressionRuleMask) {
				mask = ((MpqEditorCompressionRuleMask) rule).getMask();
				if (isValidFileSpecificMask(mask, cacheDir)) {
					clean.add(rule);
				} else {
					if (logger.isTraceEnabled()) {
						logger.trace("removing rule from ruleset due to invalid mask: {}", mask);
					}
				}
			} else {
				if (rule != null) {
					clean.add(rule);
				} else {
					if (logger.isTraceEnabled()) {
						logger.trace("removing null entry from ruleset");
					}
				}
			}
		}
		return clean.toArray(new MpqEditorCompressionRule[0]);
	}
	
	/**
	 * @param rules
	 * @param cache
	 */
	private void fillFileSizeMap(final MpqEditorCompressionRule[] rules, final File cache) {
		fileSizeMap = new ObjectLongHashMap<>();
		final Path cachePath = cache.toPath();
		for (final MpqEditorCompressionRule rule : rules) {
			if (rule instanceof MpqEditorCompressionRuleMask) {
				final String mask = ((MpqEditorCompressionRuleMask) rule).getMask();
				fileSizeMap.put(mask, getFileSize(mask, cachePath));
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
				if (rule instanceof MpqEditorCompressionRuleSize &&
						((MpqEditorCompressionRuleSize) rule).getMaxSize() == 0 &&
						!rule.getCompressionMethod().equals(MpqEditorCompressionRuleMethod.NONE)) {
					((MpqEditorCompressionRuleSize) rule).setMinSize(0);
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
		for (int i = 0; i < len; i++) {
			clone[i] = ((MpqEditorCompressionRule) rules[i].deepCopy());
		}
		return clone;
	}
	
	/**
	 * Build the mpq with the specified ruleset. CompressXml should only be enabled for the first build.
	 *
	 * @param rules
	 * @param compressXml
	 * @return
	 * @throws InterruptedException
	 * @throws MpqException
	 * @throws IOException
	 */
	private long build(final MpqEditorCompressionRule[] rules, final boolean compressXml)
			throws InterruptedException, MpqException, IOException {
		mpqInterface.setCustomCompressionRules(rules);
		final File targetFile = mod.getTargetFile();
		mpqInterface.buildMpq(targetFile.getAbsolutePath(), compressXml, MpqEditorCompression.CUSTOM, false);
		return targetFile.length();
	}
	
	/**
	 * Creates an initial ruleset.
	 *
	 * @param cacheModDirectory
	 * @return
	 * @throws IOException
	 */
	private MpqEditorCompressionRule[] createInitialRuleset(final Path cacheModDirectory) throws IOException {
		final List<MpqEditorCompressionRule> initRules = new ArrayList<>();
		initRules.add(new MpqEditorCompressionRuleSize(0, 0).setSingleUnit(true));
		final File sourceDir = mod.getMpqCacheDirectory();
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
			if (rule instanceof MpqEditorCompressionRuleMask) {
				final String cleanedMask =
						File.separator + ((MpqEditorCompressionRuleMask) rule).getMask().replace(STAR, "");
				if (p.toString().endsWith(cleanedMask)) {
					return true;
				}
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
	private static String getFileMask(final Path p, final File sourceDir) {
		final String name = p.normalize().toString();
		return name.substring(sourceDir.getAbsolutePath().length() + 1);
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
	private static boolean isValidFileSpecificMask(final String mask, final File cacheDir) {
		if (mask.contains(WILDCARD)) {
			return false;
		}
		final File referencedFile = new File(cacheDir.getAbsolutePath() + File.separator + mask);
		return referencedFile.exists() && referencedFile.isFile();
	}
	
	/**
	 * @param mask
	 * @return
	 */
	private long getFileSize(final String mask, final Path cache) {
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
	private static MpqEditorCompressionRuleMask getDefaultRule(final Path path, final File sourceDir) {
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
	
	/**
	 * Builds the archive with the compression rules.
	 *
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
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
			if (rule instanceof MpqEditorCompressionRuleMask) {
				rule.setCompressionMethod(getRandomCompressionMethod(((MpqEditorCompressionRuleMask) rule).getMask()));
				rule.setSingleUnit(random.nextBoolean());
			}
		}
	}
	
	/**
	 * Returns a random compression method.
	 *
	 * @return
	 */
	private MpqEditorCompressionRuleMethod getRandomCompressionMethod(final String mask) {
		// exclude LZMA -> crash
		MpqEditorCompressionRuleMethod method;
		if (fileSizeMap.get(mask) > 0) {
			method = random.nextBoolean() ? MpqEditorCompressionRuleMethod.ZLIB : MpqEditorCompressionRuleMethod.BZIP2;
		} else {
			final int max = compressionSetting.length;
			do {
				method = compressionSetting[random.nextInt(max)];
			} while (hasInvalidCompressionRuleset(method));
		}
		//		method = random.nextBoolean() ? MpqEditorCompressionRuleMethod.ZLIB : MpqEditorCompressionRuleMethod.BZIP2;
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
		mpqInterface.clearCacheExtractedMpq();
	}
}
