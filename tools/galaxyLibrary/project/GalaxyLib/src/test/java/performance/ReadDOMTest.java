// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package performance;

import com.ahli.xml.XmlDomHelper;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class ReadDOMTest {
	private static final List<Object> list = new ArrayList<>();
	private static final String TAG = "*";
	
	private ReadDOMTest() {
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
		final File f = new File(
				"D:\\GalaxyObsUi\\baseUI\\heroes\\mods\\core.stormmod\\base.stormdata\\ui\\layout\\ui\\gameui.stormlayout");
		final DocumentBuilder dBuilder;
		
		try {
			dBuilder = XmlDomHelper.buildSecureDocumentBuilder();
			// for (int i = 0; i < 1000; ++i) {
			while (System.currentTimeMillis() - startTime < 60_000) {
				list.clear();
				loadXml(dBuilder, f);
				iterations++;
				// if (i % 100 == 0) {
				// endMem = rt.totalMemory() - rt.freeMemory();
				// System.out.println("Memory Use: " + ((float) endMem - startMem) / (1 << 20) +
				// " MB.");
				// }
			}
		} catch (final SAXException | IOException | ParserConfigurationException e) {
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
	
	public static void loadXml(final DocumentBuilder dBuilder, final File f) throws SAXException, IOException {
		
		// long startTime = System.currentTimeMillis();
		
		final Document doc = dBuilder.parse(f);
		
		// long executionTime = (System.currentTimeMillis() - startTime);
		// System.out.println("parsing took " + executionTime + "ms.");
		// startTime = System.currentTimeMillis();
		
		final NodeList elements = doc.getElementsByTagName(TAG);
		Node node;
		int n;
		String elName;
		NamedNodeMap attributes;
		Node attr;
		String attrName;
		String attrVal;
		int i;
		int j;
		final int len;
		int len2;
		for (j = 0, len = elements.getLength(); j < len; ++j) {
			node = elements.item(j);
			// final int n = nav.getCurrentIndex();
			n = getLevel(node.getParentNode());
			// final int d = nav.getCurrentDepth();
			elName = node.getNodeName().toLowerCase(Locale.ROOT);
			list.add(n);
			list.add(elName);
			// executionTime = (System.currentTimeMillis() - startTime);
			// System.out.println(j + " - " + elName + " @ " + executionTime);
			
			attributes = node.getAttributes();
			for (i = 0, len2 = attributes.getLength(); i < len2; ++i) {
				attr = attributes.item(i);
				// i will be attr name, i+1 will be attribute value
				attrName = attr.getNodeName().toLowerCase(Locale.ROOT);
				attrVal = attr.getNodeValue();
				list.add(attrName);
				list.add(attrVal);
				// executionTime = (System.currentTimeMillis() - startTime);
				// System.out.println(attrName + "=" + attrVal + " @ " + executionTime);
			}
		}
	}
	
	private static int getLevel(final Node parent) {
		int i = 0;
		Node n = parent;
		while (n != null) {
			n = n.getParentNode();
			++i;
		}
		return i;
	}
}
