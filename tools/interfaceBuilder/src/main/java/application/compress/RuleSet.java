package application.compress;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OrderColumn;

@Entity
public class RuleSet {
	
	@Id
	private Long id;
	
	//	@ElementCollection (fetch = FetchType.LAZY)
	@OrderColumn
	private String[] compressionRulesString;
	
	public RuleSet() {
		// required
	}
	
	public RuleSet(final String[] compressionRulesString) {
		this.compressionRulesString = compressionRulesString;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(final Long id) {
		this.id = id;
	}
	
	public String[] getCompressionRulesString() {
		return compressionRulesString;
	}
	
	public void setCompressionRulesString(final String[] compressionRulesString) {
		this.compressionRulesString = compressionRulesString;
	}
}
