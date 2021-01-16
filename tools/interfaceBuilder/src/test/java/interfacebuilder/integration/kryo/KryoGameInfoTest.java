package interfacebuilder.integration.kryo;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class KryoGameInfoTest {
	@Test
	@SuppressWarnings("JUnitTestMethodWithNoAssertions")
	public void equalsContract() {
		EqualsVerifier.forClass(KryoGameInfo.class).verify();
	}
}