// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui;

import com.ahli.util.XmlDomHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a Desc Index File.
 *
 * @author Ahli
 */
public final class DescIndexReader {
	private static final Logger logger = LoggerFactory.getLogger(DescIndexReader.class);
	
	private DescIndexReader() {
		// no instances allowed
	}
	
	/**
	 * Grabs all layout file paths from a given descIndex file.
	 *
	 * @param descIndexFile
	 * 		descIndex file
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static LayoutPathData getLayoutPathList(final Path descIndexFile)
			throws SAXException, IOException, ParserConfigurationException {
		final DocumentBuilder dBuilder = XmlDomHelper.buildSecureDocumentBuilder();
		logger.trace("reading layouts from descIndexFile: {}", descIndexFile);
		final Document doc = dBuilder.parse(descIndexFile.toFile());
		
		// must be in a DataComponent node
		final NodeList nodeList = doc.getElementsByTagName("*");
		final int nodeListLength = nodeList.getLength();
		final LayoutPathData result = new LayoutPathData(new ArrayList<>(nodeListLength),
				new ArrayList<>(nodeListLength),
				new ArrayList<>(nodeListLength));
		for (int i = 0; i < nodeListLength; ++i) {
			final Node node = nodeList.item(i);
			if ("Include".equalsIgnoreCase(node.getNodeName())) {
				final NamedNodeMap attributes = node.getAttributes();
				
				final List<String> list =
						XmlDomHelper.isFailingRequiredToLoad(attributes) ? result.notLoaded() : result.loaded();
				final String path = attributes.item(0).getNodeValue();
				list.add(path);
				result.combined().add(path);
				logger.trace("Adding layout path to layoutPathList: {}", path);
			}
		}
		return result;
	}
}
