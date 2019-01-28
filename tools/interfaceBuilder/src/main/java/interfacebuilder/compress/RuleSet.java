// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.compress;

import com.ahli.mpq.mpqeditor.MpqEditorCompressionRule;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRuleParser;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Entity
public class RuleSet {
	
	@Id
	@GeneratedValue (strategy = GenerationType.AUTO)
	private Long id;
	
	@Transient
	private MpqEditorCompressionRule[] compressionRules;
	
	@ElementCollection (fetch = FetchType.EAGER)
	private List<String> compressionRulesString;
	
	public RuleSet() {
		// required
	}
	
	public RuleSet(final MpqEditorCompressionRule... compressionRules) {
		setCompressionRules(compressionRules);
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(final Long id) {
		this.id = id;
	}
	
	@Transient
	public MpqEditorCompressionRule[] getCompressionRules() throws IOException {
		// lazy-load array
		if (compressionRules == null) {
			compressionRules = new MpqEditorCompressionRule[getCompressionRulesString().size()];
			for (int i = 0; i < compressionRules.length; i++) {
				compressionRules[i] = MpqEditorCompressionRuleParser.parse(compressionRulesString.get(i));
			}
		}
		return compressionRules;
	}
	
	@Transient
	public void setCompressionRules(final MpqEditorCompressionRule... compressionRules) {
		this.compressionRules = compressionRules;
		
		// update string representation in DB
		final String[] rulesStrings = new String[compressionRules.length];
		for (int i = 0; i < compressionRules.length; i++) {
			rulesStrings[i] = compressionRules[i].toString();
		}
		compressionRulesString = Arrays.asList(rulesStrings);
	}
	
	public List<String> getCompressionRulesString() {
		return compressionRulesString;
	}
	
	public void setCompressionRulesString(final List<String> compressionRulesString) {
		this.compressionRulesString = compressionRulesString;
	}
}
