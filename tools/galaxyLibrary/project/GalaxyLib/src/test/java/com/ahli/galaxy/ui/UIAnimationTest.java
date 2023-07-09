// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

class UIAnimationTest {
	
	@Test
	@SuppressWarnings("JUnitTestMethodWithNoAssertions")
	void equalsContract() {
		EqualsVerifier.forClass(UIAnimationMutable.class).withRedefinedSuperclass()
				//.withIgnoredFields("hash", "hashIsZero", "hashIsDirty")
				.suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
