package interfacebuilder.projects;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

class ProjectTest {
	@Test
	@SuppressWarnings("JUnitTestMethodWithNoAssertions")
	public void equalsContract() {
		EqualsVerifier.forClass(Project.class).suppress(Warning.NONFINAL_FIELDS).verify();
	}
}