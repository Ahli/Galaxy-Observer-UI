package com.ahli.galaxy.ui;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

class UIControllerTest {
	
	@Test
	@SuppressWarnings("JUnitTestMethodWithNoAssertions")
	void equalsContract() {
		EqualsVerifier.forClass(UIControllerMutable.class)
				.withRedefinedSuperclass()
				.withIgnoredFields("hash", "hashIsZero", "hashIsDirty")
				.suppress(Warning.NONFINAL_FIELDS)
				.verify();
	}
}
