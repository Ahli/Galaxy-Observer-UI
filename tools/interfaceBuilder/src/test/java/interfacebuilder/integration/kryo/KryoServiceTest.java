// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.integration.kryo;

import com.ahli.galaxy.ui.UICatalogImpl;
import com.ahli.galaxy.ui.UIConstant;
import com.ahli.galaxy.ui.UIFrame;
import com.ahli.galaxy.ui.exception.UIException;
import com.esotericsoftware.kryo.Kryo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class KryoServiceTest {
	
	private final KryoService kryoService = new KryoService();
	
	@Test
	void testMetaFile() throws IOException {
		final Kryo kryo = kryoService.getKryoForBaseUiMetaFile();
		final Path path = Path.of(System.getProperty("java.io.tmpdir"), "interfaceBuilderTest", "testMetaFile");
		Files.createDirectories(path.getParent());
		
		final KryoGameInfo kryoGameInfoA =
				new KryoGameInfo(new int[] { 42, 12, 44, 12345 }, "Heroes of the Storm", false);
		final List<Object> payload = new ArrayList<>(1);
		payload.add(kryoGameInfoA);
		
		kryoService.put(path, payload, kryo);
		
		final List<Class<?>> payloadClasses = new ArrayList<>(1);
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
		final UICatalogImpl uiCatalogA = new UICatalogImpl(1, 1, 1, 0, 1, 0);
		uiCatalogA.addTemplate("fileName", new UIFrame("frame", "type"), false);
		uiCatalogA.addConstant(new UIConstant("asd asd asd", "qqq"), false);
		final List<Object> payload = new ArrayList<>(2);
		payload.add((uiCatalogA));
		payload.add(kryoGameInfoA);
		
		kryoService.put(path, payload, kryo);
		
		final Kryo kryo2 = kryoService.getKryoForUICatalog();
		final List<Class<?>> payloadClasses = new ArrayList<>(2);
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
