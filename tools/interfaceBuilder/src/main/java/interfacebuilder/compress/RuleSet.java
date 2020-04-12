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
import java.util.Objects;

//import jakarta.persistence.ElementCollection;
//import jakarta.persistence.Entity;
//import jakarta.persistence.FetchType;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.Transient;

@Entity
public class RuleSet {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Transient
	private MpqEditorCompressionRule[] compressionRules;
	
	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> compressionRulesString;
	
	public RuleSet() {
		// required
	}
	
	public RuleSet(final MpqEditorCompressionRule... compressionRules) {
		setCompressionRulesPrivate(compressionRules);
	}
	
	@Transient
	private void setCompressionRulesPrivate(final MpqEditorCompressionRule... compressionRules) {
		this.compressionRules = compressionRules;
		
		// update string representation in DB
		final String[] rulesStrings = new String[compressionRules.length];
		for (int i = 0; i < compressionRules.length; i++) {
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
			for (int i = 0; i < compressionRules.length; i++) {
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
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		final Object[] signatureFields = getSignatureFields();
		final Object[] thatSignatureFields = ((RuleSet) obj).getSignatureFields();
		for (int i = 0; i < signatureFields.length; i++) {
			if (!(signatureFields[i] instanceof Object[])) {
				if (!Objects.equals(signatureFields[i], thatSignatureFields[i])) {
					return false;
				}
			} else {
				if (!Arrays.deepEquals((Object[]) signatureFields[i], (Object[]) thatSignatureFields[i])) {
					return false;
				}
			}
		}
		return true;
	}
	
	private Object[] getSignatureFields() {
		return new Object[] { compressionRules };
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getSignatureFields());
	}
}
