// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package performance;

import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.stax.InputFactoryImpl;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class ReadAaltoTest {
	private static final List<Object> list = new ArrayList<>();
	
	private ReadAaltoTest() {
	}
	
	public static void main(final String[] args) {
		final long endMem;
		int iterations = 0;
		try {
			Thread.sleep(1_000);
		} catch (final InterruptedException e1) {
			e1.printStackTrace();
			Thread.currentThread().interrupt();
		}
		final Runtime rt = Runtime.getRuntime();
		final long startMem = rt.totalMemory() - rt.freeMemory();
		System.out.println("memory: " + ((float) startMem) / (1 << 20) + " MB.");
		final long startTime = System.currentTimeMillis();
		final Path p =
				Path.of("D:\\GalaxyObsUi\\baseUI\\heroes\\mods\\core.stormmod\\base.stormdata\\ui\\layout\\ui\\gameui.stormlayout");
		final AsyncXMLInputFactory asyncInputFactory;
		
		try {
			asyncInputFactory = new InputFactoryImpl();
			asyncInputFactory.configureForSpeed();
			// for (int i = 0; i < 1000; ++i) {
			while (System.currentTimeMillis() - startTime < 60_000) {
				list.clear();
				loadXml(asyncInputFactory, p);
				iterations++;
				// if (i % 100 == 0) {
				// endMem = rt.totalMemory() - rt.freeMemory();
				// System.out.println("Memory Use: " + ((float) endMem - startMem) / (1 << 20) +
				// " MB.");
				// }
			}
		} catch (final IOException | XMLStreamException e) {
			e.printStackTrace();
		}
		
		final long executionTime = System.currentTimeMillis() - startTime;
		endMem = rt.totalMemory() - rt.freeMemory();
		if (iterations != 0) {
			System.out.println(
					"recursive traversal took " + executionTime + "ms. Per iteration: " + executionTime / iterations +
							"." + executionTime % iterations * 10 / iterations + "ms.");
		} else {
			System.out.println("no iterations performed");
		}
		System.out.println("elements: " + list.size());
		System.out.println("Memory Use: " + ((float) endMem - startMem) / (1 << 20) + " MB.");
		System.out.println("iterations: " + iterations);
		
		try {
			Thread.sleep(1_000);
		} catch (final InterruptedException e1) {
			e1.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}
	
	public static void loadXml(final AsyncXMLInputFactory asyncInputFactory, final Path p)
			throws IOException, XMLStreamException {
		
		// long startTime = System.currentTimeMillis();
		final byte[] bytes = Files.readAllBytes(p);
		final int offset = bytes[0] == -17 && bytes[1] == -69 && bytes[2] == -65 ? 3 : 0;
		final AsyncXMLStreamReader<AsyncByteArrayFeeder> parser =
				asyncInputFactory.createAsyncFor(bytes, offset, bytes.length - offset);
		
		// long executionTime = (System.currentTimeMillis() - startTime);
		// System.out.println("file loading took " + executionTime + "ms.");
		// startTime = System.currentTimeMillis();
		parser.next();
		int eventType;
		//		if (eventType == XMLStreamConstants.START_DOCUMENT) {
		while ((eventType = parser.next()) != XMLStreamConstants.END_DOCUMENT) {
			if (eventType == XMLStreamConstants.START_ELEMENT) {
				final String tagName = parser.getName().getLocalPart().toLowerCase(Locale.ROOT);
				final int level = parser.getDepth();
				final int attributeCount = parser.getAttributeCount();
				list.add(level);
				list.add(tagName);
				for (int i = 0; i < attributeCount; ++i) {
					list.add(parser.getAttributeName(i).getLocalPart().toLowerCase(Locale.ROOT));
					list.add(parser.getAttributeValue(i));
				}
			} else if (eventType == AsyncXMLStreamReader.EVENT_INCOMPLETE) {
				return;
			}
			//			}
		}
	}
}
