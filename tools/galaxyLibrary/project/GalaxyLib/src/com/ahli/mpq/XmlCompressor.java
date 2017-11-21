package com.ahli.mpq;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XmlCompressor {
	static Logger LOGGER = LogManager.getLogger(XmlCompressor.class);
	
	/**
	 * 
	 * @param cachePath
	 * @param ignoreCommentCountPerFile
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static void processCache(String cachePath, int ignoreCommentCountPerFile)
			throws ParserConfigurationException, SAXException, IOException {
		
		LOGGER.info("Compressing XML files...");
		LOGGER.debug("cachePath: " + cachePath);
		
		File cache = new File(cachePath);
		boolean recursive = true;
		
		Collection<File> filesOfCache = FileUtils.listFiles(cache, null, recursive);
		
		DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		// provide error handler that does not print incompatible files into
		// console
		dBuilder.setErrorHandler(new ErrorHandler() {
			@Override
			public void warning(SAXParseException e) throws SAXException {
			}
			
			@Override
			public void fatalError(SAXParseException e) throws SAXException {
				throw e;
			}
			
			@Override
			public void error(SAXParseException e) throws SAXException {
				throw e;
			}
		});
		
		InputStream is = null;
		
		x: for (File curFile : filesOfCache) {
			Document doc = null;
			try {
				// parse XML file
				
				// THIS DOES NOT CLOSE THE INPUTSTREAM ON EXCEPTION
				// CREATING TONS OF FILE ACCESS PROBLEMS. DO NOT USE!
				// doc = dBuilder.parse(curFile);
				
				// WORKAROUND -> provide Inputstream
				is = new FileInputStream(curFile);
				doc = dBuilder.parse(is);
				
			} catch (SAXParseException | IOException e) {
				// couldn't parse, most likely no XML file
				if (is != null) {
					is.close();
				}
				continue x;
			} finally {
				if (is != null) {
					is.close();
				}
			}
			
			LOGGER.debug("compression - processing file: " + curFile.getPath());
			
			// process all nodes
			NodeList nodes = doc.getElementsByTagName("*");
			for (int i = 0; i < nodes.getLength(); i++) {
				Node curNode = nodes.item(i);
				
				// remove whitespace
				trimWhitespace(curNode);
			}
			
			// remove comment nodes except first one
			Element elem = doc.getDocumentElement();
			NodeList childNodes = elem.getChildNodes();
			removeCommentsInChildNodes(childNodes, ignoreCommentCountPerFile);
			
			// write DOM back to XML
			Source source = new DOMSource(doc);
			Result result = new StreamResult(curFile);
			Transformer xformer;
			try {
				xformer = TransformerFactory.newInstance().newTransformer();
				xformer.transform(source, result);
			} catch (TransformerFactoryConfigurationError | TransformerException e) {
				LOGGER.error("Transforming to generate XML file failed.", e);
				e.printStackTrace();
			}
			
		}
		
	}
	
	private static void removeCommentsInChildNodes(NodeList childNodes, int ignoreCount) {
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node curNode = childNodes.item(i);
			
			if (curNode.getNodeType() == Node.COMMENT_NODE) {
				if (ignoreCount == 0) {
					
					// keep hotkeys/settings definition alive
					Comment comment = (Comment) curNode;
					String text = comment.getData().trim().toLowerCase(Locale.ENGLISH);
					if (!text.contains("@hotkey") && !text.contains("@setting")) {
						
						curNode.getParentNode().removeChild(curNode);
						
					}
					
				} else {
					ignoreCount--;
				}
			} else {
				removeCommentsInChildNodes(curNode.getChildNodes(), ignoreCount);
			}
		}
	}
	
	public static void trimWhitespace(Node node) {
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if (child.getNodeType() == Node.TEXT_NODE) {
				child.setTextContent(child.getTextContent().trim());
			}
			trimWhitespace(child);
		}
	}
	
}
