// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.integration.kryo;

import com.ahli.galaxy.ui.UICatalogImpl;
import com.ahli.galaxy.ui.UIConstant;
import com.ahli.galaxy.ui.UIFrame;
import com.ahli.galaxy.ui.exception.UIException;
import com.esotericsoftware.kryo.Kryo;
import interfacebuilder.InterfaceBuilderApp;
import interfacebuilder.projects.ProjectJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.SpringBootDependencyInjectionTestExecutionListener;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = InterfaceBuilderApp.class)
@TestExecutionListeners(
		listeners = { MockitoTestExecutionListener.class, SpringBootDependencyInjectionTestExecutionListener.class },
		mergeMode = TestExecutionListeners.MergeMode.REPLACE_DEFAULTS)
final class KryoServiceTest {
	
	@MockBean
	private ProjectJpaRepository projectRepoMock;
	
	@Autowired
	private KryoService kryoService;
	
	@Test
	void testMetaFile() throws IOException {
		final Kryo kryo = kryoService.getKryoForBaseUiMetaFile();
		final Path path = Path.of(System.getProperty("java.io.tmpdir"), "interfaceBuilderTest", "testMetaFile");
		Files.createDirectories(path.getParent());
		
		final KryoGameInfo kryoGameInfoA =
				new KryoGameInfo(new int[] { 42, 12, 44, 12345 }, "Heroes of the Storm", false);
		final List<Object> payload = new ArrayList<>();
		payload.add(kryoGameInfoA);
		
		kryoService.put(path, payload, kryo);
		
		final List<Class<?>> payloadClasses = new ArrayList<>();
		payloadClasses.add(KryoGameInfo.class);
		
		final KryoGameInfo kryoGameInfoB = (KryoGameInfo) kryoService.get(path, payloadClasses, kryo).get(0);
		Files.delete(path);
		
		assertEquals(kryoGameInfoA, kryoGameInfoB, "Meta file written and read mismatch");
	}
	
	@Test
	void testCacheMetaFile() throws IOException, UIException {
		final Kryo kryo = kryoService.getKryoForUICatalog();
		final Path path = Path.of(System.getProperty("java.io.tmpdir"), "interfaceBuilderTest", "testCacheMetaFile");
		Files.createDirectories(path.getParent());
		
		final KryoGameInfo kryoGameInfoA =
				new KryoGameInfo(new int[] { 42, 12, 44, 12345 }, "Heroes of the Storm", false);
		final UICatalogImpl uiCatalogA = new UICatalogImpl(1, 1, 1, 0, 1);
		uiCatalogA.addTemplate("fileName", new UIFrame("frame", "type"), false);
		uiCatalogA.addConstant(new UIConstant("asd asd asd", "qqq"), false);
		final List<Object> payload = new ArrayList<>();
		payload.add((uiCatalogA));
		payload.add(kryoGameInfoA);
		
		kryoService.put(path, payload, kryo);
		
		final Kryo kryo2 = kryoService.getKryoForUICatalog();
		final List<Class<?>> payloadClasses = new ArrayList<>();
		payloadClasses.add(UICatalogImpl.class);
		payloadClasses.add(KryoGameInfo.class);
		
		final List<Object> objects = kryoService.get(path, payloadClasses, kryo2);
		final UICatalogImpl uiCatalogB = (UICatalogImpl) objects.get(0);
		final KryoGameInfo kryoGameInfoB = (KryoGameInfo) objects.get(1);
		Files.delete(path);
		
		assertEquals(kryoGameInfoA, kryoGameInfoB, "Meta file written and read mismatch");
		assertEquals(uiCatalogA, uiCatalogB, "UiCatalog written and read mismatch");
	}
	
}
