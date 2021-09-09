// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.compress;

import com.ahli.mpq.mpqeditor.MpqEditorCompressionRule;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRuleParser;
import org.hibernate.Hibernate;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Entity
public class RuleSet implements Serializable {
	
	@Serial
	private static final long serialVersionUID = -8442665535029507145L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Transient
	private MpqEditorCompressionRule[] compressionRules;
	
	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> compressionRulesString;
	
	protected RuleSet() {
		// required for hibernate
	}
	
	public RuleSet(final MpqEditorCompressionRule... compressionRules) {
		setCompressionRulesPrivate(compressionRules);
	}
	
	@Transient
	private void setCompressionRulesPrivate(final MpqEditorCompressionRule... compressionRules) {
		this.compressionRules = compressionRules;
		
		// update string representation in DB
		final String[] rulesStrings = new String[compressionRules.length];
		for (int i = 0; i < compressionRules.length; ++i) {
			rulesStrings[i] = compressionRules[i].toString();
		}
		compressionRulesString = Arrays.asList(rulesStrings);
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
			for (int i = 0; i < compressionRules.length; ++i) {
				compressionRules[i] = MpqEditorCompressionRuleParser.parse(compressionRulesString.get(i));
			}
		}
		return compressionRules;
	}
	
	@Transient
	public void setCompressionRules(final MpqEditorCompressionRule... compressionRules) {
		setCompressionRulesPrivate(compressionRules);
	}
	
	public List<String> getCompressionRulesString() {
		return compressionRulesString;
	}
	
	public void setCompressionRulesString(final List<String> compressionRulesString) {
		this.compressionRulesString = compressionRulesString;
	}
	
	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
			return false;
		}
		final RuleSet ruleSet = (RuleSet) o;
		// only compare primary keys
		return Objects.equals(id, ruleSet.id);
	}
	
	@Override
	public int hashCode() {
		// for generated primary keys, the hashcode must be constant before and after
		return 0;
	}
}
