// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.game;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class GameDefTest {
	
	@Test
	@SuppressWarnings("JUnitTestMethodWithNoAssertions")
	void equalsContract() {
		EqualsVerifier.forClass(GameDef.class).verify();
	}
}
