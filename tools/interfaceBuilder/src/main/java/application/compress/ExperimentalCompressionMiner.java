package application.compress;

import application.integration.FileService;
import com.ahli.galaxy.ModData;
import com.ahli.mpq.MpqEditorInterface;
import com.ahli.mpq.MpqException;
import com.ahli.mpq.mpqeditor.MpqEditorCompression;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRule;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRuleMask;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRuleMethod;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRuleSize;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class ExperimentalCompressionMiner {
	//private static final Logger logging = LogManager.getLogger();
	
	private final MpqEditorCompressionRule[] rules;
	private final ModData mod;
	private final MpqEditorInterface mpqInterface;
	private final Random random = new Random();
	private final MpqEditorCompressionRuleMethod[] compressionSetting = MpqEditorCompressionRuleMethod.values();
	private final FileService fileService;
	private long bestSize;
	private MpqEditorCompressionRule[] bestRuleSet = null;
	
	/**
	 * @param mod
	 * 		GameMod with a set sourcePath
	 * @throws IOException
	 */
	public ExperimentalCompressionMiner(final ModData mod, final String mpqCachePath, final String mpqEditorPath,
			final FileService fileService) throws IOException, MpqException, InterruptedException {
		this.mod = mod;
		this.fileService = fileService;
		mpqInterface = new MpqEditorInterface(mpqCachePath + Thread.currentThread().getId(), mpqEditorPath);
		mod.setMpqCacheDirectory(new File(mpqInterface.getMpqCachePath()));
		fileService.copyFileOrDirectory(mod.getSourceDirectory(), mpqInterface.getCache());
		rules = createInitialRuleset(mod.getSourceDirectory().toPath());
		bestRuleSet = deepCopy(rules);
		bestSize = build(rules, true);
	}
	
	private MpqEditorCompressionRule[] createInitialRuleset(final Path cacheModDirectory) throws IOException {
		final List<MpqEditorCompressionRule> initRules = new ArrayList<>();
		initRules.add(new MpqEditorCompressionRuleSize(0, 0).setSingleUnit(true));
		try (Stream<Path> ps = Files.walk(cacheModDirectory)) {
			ps.filter(Files::isRegularFile).forEach(p -> initRules
					.add(new MpqEditorCompressionRuleMask(getFileMask(p)).setSingleUnit(true).setCompress(true)
							.setCompressionMethod(MpqEditorCompressionRuleMethod.BZIP2)));
		}
		return initRules.toArray(new MpqEditorCompressionRule[0]);
	}
	
	/**
	 * Creates a deep copy of the specified list of rules.
	 *
	 * @param rules
	 * @return
	 */
	private MpqEditorCompressionRule[] deepCopy(final MpqEditorCompressionRule[] rules) {
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
	 * Returns the MpqCompressionRuleSet's mask String for the Path of an extracted interface's file.
	 *
	 * @param p
	 * @return
	 */
	private String getFileMask(final Path p) {
		final String name = p.normalize().toString();
		return name.substring(mod.getSourceDirectory().getAbsolutePath().length() + 1);
	}
	
	/**
	 * Builds the rules.
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
			bestRuleSet = rules.clone();
		}
		return size;
	}
	
	public void randomizeRules() {
		for (final MpqEditorCompressionRule r : rules) {
			if (r instanceof MpqEditorCompressionRuleMask) {
				r.setCompressionMethod(getRandomCompressionMethod());
			}
		}
	}
	
	private MpqEditorCompressionRuleMethod getRandomCompressionMethod() {
		return compressionSetting[random.nextInt(compressionSetting.length)];
	}
	
	public MpqEditorCompressionRule[] getBestRuleSet() {
		return bestRuleSet;
	}
	
	public long getBestSize() {
		return bestSize;
	}
}
