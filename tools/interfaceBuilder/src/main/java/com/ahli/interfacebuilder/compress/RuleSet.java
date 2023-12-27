// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.compress;

import com.ahli.mpq.mpqeditor.MpqEditorCompressionRule;
import com.ahli.mpq.mpqeditor.MpqEditorCompressionRuleParser;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import org.hibernate.Hibernate;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "rule_set")
public class RuleSet implements Serializable {
	
	@Serial
	private static final long serialVersionUID = -8442665535029507145L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	@Getter
	private Long id;
	
	@Transient
	private MpqEditorCompressionRule[] compressionRules;
	
	@Getter
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "rule_set_compression_rules_string", joinColumns = { @JoinColumn(name = "rule_set_id") })
	@Column(name = "compression_rules_string")
	private List<String> compressionRulesString;
	
	public RuleSet() {
		// required for hibernate
	}
	
	public RuleSet(final MpqEditorCompressionRule... compressionRules) {
		setCompressionRulesPrivate(compressionRules);
	}
	
	@Transient
	private void setCompressionRulesPrivate(final MpqEditorCompressionRule... compressionRules) {
		this.compressionRules = compressionRules;
		
		// update string representation in DB
		final List<String> rulesStrings = new ArrayList<>(compressionRules.length);
		for (final MpqEditorCompressionRule compressionRule : compressionRules) {
			rulesStrings.add(compressionRule.toString());
		}
		compressionRulesString = rulesStrings;
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
		return Objects.equals(getId(), ruleSet.getId());
	}
	
	@Override
	public int hashCode() {
		// for generated primary keys, the hashcode must be constant before and after
		return 0;
	}
}
