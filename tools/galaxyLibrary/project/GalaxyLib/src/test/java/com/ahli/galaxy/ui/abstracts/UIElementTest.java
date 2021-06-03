// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui.abstracts;

import com.ahli.galaxy.ui.UIAnimationMutable;
import com.ahli.galaxy.ui.UIAttributeImmutable;
import com.ahli.galaxy.ui.UIConstantImmutable;
import com.ahli.galaxy.ui.UIControllerMutable;
import com.ahli.galaxy.ui.UIFrameMutable;
import com.ahli.galaxy.ui.UIStateGroupMutable;
import com.ahli.galaxy.ui.UIStateMutable;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

class UIElementTest {
	
	@Test
	void equalsContract() {
		EqualsVerifier.forClass(UIElementAbstract.class)
				.withRedefinedSubclass(UIFrameMutable.class)
				.withRedefinedSubclass(UIStateGroupMutable.class)
				.withRedefinedSubclass(UIStateMutable.class)
				.withRedefinedSubclass(UIAttributeImmutable.class)
				.withRedefinedSubclass(UIAnimationMutable.class)
				.withRedefinedSubclass(UIConstantImmutable.class)
				.withRedefinedSubclass(UIControllerMutable.class)
				.withIgnoredFields("hash", "hashIsZero", "hashIsDirty")
				.suppress(Warning.NONFINAL_FIELDS)
				.verify();
	}
	
}