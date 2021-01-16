package com.ahli.util;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class PairTest {
	@Test
	@SuppressWarnings("JUnitTestMethodWithNoAssertions")
	public void equalsContract() {
		EqualsVerifier.forClass(Pair.class).verify();
	}
}