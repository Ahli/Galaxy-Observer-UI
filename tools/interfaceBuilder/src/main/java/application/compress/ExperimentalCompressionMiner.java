package application.compress;

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
	private long bestSize;
	private MpqEditorCompressionRule[] bestRuleSet = null;
	
	/**
	 * @param mod
	 * 		GameMod with a set sourcePath
	 * @throws IOException
	 */
	public ExperimentalCompressionMiner(final ModData mod, final String mpqCachePath, final String mpqEditorPath)
			throws IOException, MpqException, InterruptedException {
		this.mod = mod;
		mpqInterface = new MpqEditorInterface(mpqCachePath + Thread.currentThread().getId(), mpqEditorPath);
		rules = buildInitialRuleset(mod.getSourceDirectory().toPath());
		bestRuleSet = deepCopy(rules);
		bestSize = build(rules);
	}
	
	private MpqEditorCompressionRule[] buildInitialRuleset(final Path cacheModDirectory) throws IOException {
		final List<MpqEditorCompressionRule> initRules = new ArrayList<>();
		initRules.add(new MpqEditorCompressionRuleSize(0, 0).setSingleUnit(true).setCompress(true));
		try (Stream<Path> ps = Files.walk(cacheModDirectory)) {
			ps.filter(Files::isRegularFile)
					.forEach(p -> initRules.add(new MpqEditorCompressionRuleMask(p.getFileName().toString())));
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
	
	private long build(final MpqEditorCompressionRule[] rules) throws InterruptedException, MpqException, IOException {
		mpqInterface.setCustomRuleSet(rules);
		final File targetFile = mod.getTargetFile();
		mpqInterface.buildMpq(targetFile.getAbsolutePath(), true, MpqEditorCompression.CUSTOM, false);
		return targetFile.length();
	}
	
	public long randomizeRuleAndBuild() throws InterruptedException, IOException, MpqException {
		randomizeRules(rules);
		final long size = build(rules);
		if (size < bestSize) {
			bestSize = size;
			bestRuleSet = rules.clone();
		}
		return size;
	}
	
	private void randomizeRules(final MpqEditorCompressionRule[] rules) {
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
