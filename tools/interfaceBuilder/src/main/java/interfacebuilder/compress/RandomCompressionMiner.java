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
	private static final Logger logger = LogManager.getLogger(RandomCompressionMiner.class);
	private final ModData mod;
	private final MpqEditorInterface mpqInterface;
	private final Random random = new Random();
	//	private final MpqEditorCompressionRuleMethod[] compressionSetting = MpqEditorCompressionRuleMethod.values();
	private MpqEditorCompressionRule[] rules;
	private long bestSize;
	private MpqEditorCompressionRule[] bestRuleSet;
	private boolean oldRulesetHadMissingFiles = false;
	
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
		final File cache = new File(mpqInterface.getMpqCachePath());
		mod.setMpqCacheDirectory(cache);
		mpqInterface.clearCacheExtractedMpq();
		fileService.copyFileOrDirectory(mod.getSourceDirectory(), cache);
		
		// use old ruleset if one was specified and test it
		if (oldBestRuleset != null) {
			// check for file coverage holes
			final List<File> untrackedFiles = getUntrackedFiles(oldBestRuleset, cache.toPath());
			if (!untrackedFiles.isEmpty()) {
				rules = addRulesForFiles(oldBestRuleset, untrackedFiles, cache);
				oldRulesetHadMissingFiles = true;
			} else {
				rules = oldBestRuleset;
			}
			rules = removeUnusedMaskEnries(rules, cache);
			
			bestRuleSet = deepCopy(rules);
			bestSize = build(rules, true);
			
		} else {
			bestSize = Long.MAX_VALUE;
		}
		
		// compare with usual, initial ruleset and use the better one
		final MpqEditorCompressionRule[] initRules = createInitialRuleset(cache.toPath());
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
	private MpqEditorCompressionRule[] addRulesForFiles(final MpqEditorCompressionRule[] oldBestRuleset,
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
	private MpqEditorCompressionRule[] removeUnusedMaskEnries(final MpqEditorCompressionRule[] dirty,
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
						logger.trace("removing rule from ruleset due to invalid mask: " + mask);
					}
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
	 * Creates a deep copy of the specified list of rules.
	 *
	 * @param rules
	 * @return
	 */
	private MpqEditorCompressionRule[] deepCopy(final MpqEditorCompressionRule... rules) {
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
		try (final Stream<Path> ps = Files.walk(cacheModDirectory)) {
			ps.filter(Files::isRegularFile).forEach(p -> initRules.add(getDefaultRule(p, sourceDir)));
		}
		return initRules.toArray(new MpqEditorCompressionRule[0]);
	}
	
	private static boolean containsFile(final MpqEditorCompressionRule[] rules, final Path p) {
		for (final var rule : rules) {
			if (rule instanceof MpqEditorCompressionRuleMask) {
				final String cleanedMask =
						File.separator + ((MpqEditorCompressionRuleMask) rule).getMask().replace("*", "");
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
	private boolean isValidFileSpecificMask(final String mask, final File cacheDir) {
		if (mask.contains("*")) {
			return false;
		}
		final File referencedFile = new File(cacheDir.getAbsolutePath() + File.separator + mask);
		return referencedFile.exists() && referencedFile.isFile();
	}
	
	private MpqEditorCompressionRule getDefaultRule(final Path path, final File sourceDir) {
		final var rule = new MpqEditorCompressionRuleMask(getFileMask(path, sourceDir));
		switch (path.getFileName().toString()) {
			case "DocumentHeader":
				rule.setSingleUnit(true).setCompress(true).setCompressionMethod(MpqEditorCompressionRuleMethod.BZIP2);
				break;
			case "DocumentInfo":
				rule.setSingleUnit(true).setCompress(true).setCompressionMethod(MpqEditorCompressionRuleMethod.BZIP2);
				break;
			case "MapInfo":
				rule.setSingleUnit(true).setCompress(true).setCompressionMethod(MpqEditorCompressionRuleMethod.BZIP2);
				break;
			case "Objects":
				rule.setSingleUnit(true).setCompress(true).setCompressionMethod(MpqEditorCompressionRuleMethod.BZIP2);
				break;
			case "PaintedPathingLayer":
				rule.setSingleUnit(true).setCompress(true).setCompressionMethod(MpqEditorCompressionRuleMethod.BZIP2);
				break;
			case "Regions":
				rule.setSingleUnit(true).setCompress(true).setCompressionMethod(MpqEditorCompressionRuleMethod.BZIP2);
				break;
			case "t3CellFlags":
				rule.setSingleUnit(true).setCompress(true).setCompressionMethod(MpqEditorCompressionRuleMethod.BZIP2);
				break;
			case "t3FluffDoodad":
				rule.setSingleUnit(true).setCompress(true).setCompressionMethod(MpqEditorCompressionRuleMethod.BZIP2);
				break;
			case "t3HardTile":
				rule.setSingleUnit(true).setCompress(true).setCompressionMethod(MpqEditorCompressionRuleMethod.BZIP2);
				break;
			case "t3HeightMap":
				rule.setSingleUnit(true).setCompress(true).setCompressionMethod(MpqEditorCompressionRuleMethod.BZIP2);
				break;
			case "t3SyncCliffLevel":
				rule.setSingleUnit(true).setCompress(true).setCompressionMethod(MpqEditorCompressionRuleMethod.BZIP2);
				break;
			case "t3SyncHeightMap":
				rule.setSingleUnit(true).setCompress(true).setCompressionMethod(MpqEditorCompressionRuleMethod.BZIP2);
				break;
			case "t3SyncTextureInfo":
				rule.setSingleUnit(true).setCompress(true).setCompressionMethod(MpqEditorCompressionRuleMethod.BZIP2);
				break;
			case "t3TextureMasks":
				rule.setSingleUnit(true).setCompress(true).setCompressionMethod(MpqEditorCompressionRuleMethod.BZIP2);
				break;
			case "t3VertCol":
				rule.setSingleUnit(true).setCompress(true).setCompressionMethod(MpqEditorCompressionRuleMethod.BZIP2);
				break;
			case "t3Water":
				rule.setSingleUnit(true).setCompress(true).setCompressionMethod(MpqEditorCompressionRuleMethod.BZIP2);
				break;
			case "Triggers":
				rule.setSingleUnit(true).setCompress(true).setCompressionMethod(MpqEditorCompressionRuleMethod.BZIP2);
				break;
			case "Attributes":
				rule.setSingleUnit(true).setCompress(true).setCompressionMethod(MpqEditorCompressionRuleMethod.BZIP2);
				break;
			default:
				rule.setSingleUnit(true).setCompress(true).setCompressionMethod(MpqEditorCompressionRuleMethod.BZIP2);
				if (path.endsWith(".ogg") || path.endsWith(".ogv") || path.endsWith(".wav") || path.endsWith(".txt") ||
						path.endsWith(".swf") || path.endsWith(".ttf") || path.endsWith(".otf") ||
						path.endsWith(".m3")) {
					rule.setCompressionMethod(MpqEditorCompressionRuleMethod.ZLIB);
				}
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
				rule.setCompressionMethod(getRandomCompressionMethod());
				rule.setSingleUnit(random.nextBoolean());
			}
		}
	}
	
	/**
	 * Returns a random compression method.
	 *
	 * @return
	 */
	private MpqEditorCompressionRuleMethod getRandomCompressionMethod() {
		// exclude LZMA
		final MpqEditorCompressionRuleMethod method;
		//		final int max = compressionSetting.length;
		//		do {
		//			method = compressionSetting[random.nextInt(max)];
		//		} while (method == MpqEditorCompressionRuleMethod.LZMA || method == MpqEditorCompressionRuleMethod.NONE ||
		//				method == MpqEditorCompressionRuleMethod.PKWARE || method == MpqEditorCompressionRuleMethod.SPARSE ||
		//				method == MpqEditorCompressionRuleMethod.SPARSE_BZIP2 ||
		//				method == MpqEditorCompressionRuleMethod.SPARSE_ZLIB);
		method = random.nextBoolean() ? MpqEditorCompressionRuleMethod.ZLIB : MpqEditorCompressionRuleMethod.BZIP2;
		return method;
	}
	
	public MpqEditorCompressionRule[] getBestRuleSet() {
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