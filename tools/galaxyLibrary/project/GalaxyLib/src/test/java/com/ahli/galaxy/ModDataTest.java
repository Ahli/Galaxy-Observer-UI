package com.ahli.galaxy;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

class ModDataTest {
	@Test
	@SuppressWarnings("JUnitTestMethodWithNoAssertions")
	public void equalsContract() {
		EqualsVerifier.forClass(ModData.class).suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
