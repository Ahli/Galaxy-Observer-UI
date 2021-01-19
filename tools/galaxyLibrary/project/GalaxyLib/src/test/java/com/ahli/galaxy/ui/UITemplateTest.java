package com.ahli.galaxy.ui;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

class UITemplateTest {
	@Test
	@SuppressWarnings("JUnitTestMethodWithNoAssertions")
	void equalsContract() {
		EqualsVerifier.forClass(UITemplate.class).suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
