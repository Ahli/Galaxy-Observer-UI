// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.projects;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

class ProjectEntityTest {
	@Test
	@SuppressWarnings("JUnitTestMethodWithNoAssertions")
	void equalsContract() {
		EqualsVerifier.forClass(ProjectEntity.class)
				.suppress(Warning.STRICT_HASHCODE)
				.suppress(Warning.SURROGATE_KEY)
				.suppress(Warning.NONFINAL_FIELDS) // TODO remove once equalsverifier supports jakarta annotations
				.suppress(Warning.ALL_FIELDS_SHOULD_BE_USED) // TODO remove once equalsverifier supports jakarta annotations
				.verify();
	}
}
