package interfacebuilder.projects;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ProjectEntityTest {
	@Test
	@SuppressWarnings("JUnitTestMethodWithNoAssertions")
	public void equalsContract() {
		EqualsVerifier.forClass(ProjectEntity.class).verify();
	}
}