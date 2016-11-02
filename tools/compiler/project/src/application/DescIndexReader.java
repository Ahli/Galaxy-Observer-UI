package application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 * @author Ahli
 *
 */
public class DescIndexReader {

	/**
	 * Grabs all layout file paths from a given descIndex file.
	 * 
	 * @param f
	 *            descIndex file
	 * @return
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static ArrayList<String> getLayoutPathList(File f) throws SAXException, IOException, ParserConfigurationException {
		ArrayList<String> list = new ArrayList<>();

		DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = dBuilder.parse(f);

		// must be in a DataComponent node
		NodeList nodeList = doc.getElementsByTagName("Include");
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			String path = node.getAttributes().item(0).getNodeValue();
			list.add(path);
		}
		return list;
	}

}
