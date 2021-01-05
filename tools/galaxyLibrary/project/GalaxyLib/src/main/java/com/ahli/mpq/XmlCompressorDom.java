// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.mpq;

import com.ahli.util.SilentXmlSaxErrorHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;

/**
 * @author Ahli
 */
public final class XmlCompressorDom {
	private static final String AHLI_SETTING = "@setting";
	private static final String AHLI_HOTKEY = "@hotkey";
	private static final String ANY_TAGNAME = "*";
	private static final Logger logger = LogManager.getLogger(XmlCompressorDom.class);
	
	/**
	 *
	 */
	private XmlCompressorDom() {
	}
	
	/**
	 * @param cachePath
	 * @param ignoreCommentCountPerFile
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws TransformerConfigurationException
	 */
	public static void processCache(final Path cachePath, final int ignoreCommentCountPerFile)
			throws ParserConfigurationException, TransformerConfigurationException, IOException {
		
		logger.info("Compressing XML files...");
		logger.trace("cachePath: {}", cachePath);
		
		//		final Collection<File> filesOfCache = FileUtils.listFiles(new File(cachePath), null, true);
		
		final DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		dbFac.setNamespaceAware(false);
		dbFac.setValidating(false);
		dbFac.setAttribute("http://xml.org/sax/features/external-general-entities", false);
		dbFac.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		dbFac.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		dbFac.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		dbFac.setXIncludeAware(false);
		dbFac.setExpandEntityReferences(false);
		dbFac.setAttribute(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		final DocumentBuilder dBuilder = dbFac.newDocumentBuilder();
		// provide error handler that does not print incompatible files into console
		dBuilder.setErrorHandler(new SilentXmlSaxErrorHandler());
		
		final TransformerFactory factory = TransformerFactory.newInstance();
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		final Transformer transformer = factory.newTransformer();
		
		final FileVisitor<Path> visitor = new FileProcessor(dBuilder, transformer, ignoreCommentCountPerFile);
		Files.walkFileTree(cachePath, visitor);
	}
	
	/**
	 * @param childNodes
	 * @param ignoreCount
	 */
	private static void removeCommentsInChildNodes(final NodeList childNodes, int ignoreCount) {
		for (int i = 0, len = childNodes.getLength(); i < len; ++i) {
			final Node curNode = childNodes.item(i);
			// curNode can be null because items might have been removed while looping; getLength checks every loop are too costly
			if (curNode != null) {
				if (curNode.getNodeType() == Node.COMMENT_NODE) {
					if (ignoreCount == 0) {
						
						// keep hotkeys/settings definition alive
						final Comment comment = (Comment) curNode;
						final String text = comment.getData().trim().toLowerCase(Locale.ENGLISH);
						if (!text.contains(AHLI_HOTKEY) && !text.contains(AHLI_SETTING)) {
							
							curNode.getParentNode().removeChild(curNode);
							
						}
						
					} else {
						--ignoreCount;
					}
				} else {
					removeCommentsInChildNodes(curNode.getChildNodes(), ignoreCount);
				}
			}
		}
	}
	
	/**
	 * @param node
	 */
	public static void trimWhitespace(final Node node) {
		final NodeList childNodes = node.getChildNodes();
		for (int i = 0, len = childNodes.getLength(); i < len; ++i) {
			final Node child = childNodes.item(i);
			if (child.getNodeType() == Node.TEXT_NODE) {
				child.setTextContent(child.getTextContent().trim());
			}
			trimWhitespace(child);
		}
	}
	
	private static final class FileProcessor extends SimpleFileVisitor<Path> {
		
		private final Transformer transformer;
		private final DocumentBuilder dBuilder;
		private final int ignoreCommentCountPerFile;
		
		public FileProcessor(
				final DocumentBuilder dBuilder, final Transformer transformer, final int ignoreCommentCountPerFile) {
			super();
			this.dBuilder = dBuilder;
			this.transformer = transformer;
			this.ignoreCommentCountPerFile = ignoreCommentCountPerFile;
		}
		
		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
			final Document doc;
			try (final InputStream is = new BufferedInputStream(Files.newInputStream(file))) {
				
				doc = dBuilder.parse(is);
				
			} catch (final IOException | SAXException e) {
				logger.trace("Error while compressing xml.", e);
				return FileVisitResult.CONTINUE;
			}
			
			logger.trace("compression - processing file: {}", file);
			
			// process all nodes
			final NodeList nodes = doc.getElementsByTagName(ANY_TAGNAME);
			for (int i = 0, len = nodes.getLength(); i < len; ++i) {
				final Node curNode = nodes.item(i);
				
				// remove whitespace
				trimWhitespace(curNode);
			}
			
			// remove comment nodes except first one
			final Element elem = doc.getDocumentElement();
			final NodeList childNodes = elem.getChildNodes();
			removeCommentsInChildNodes(childNodes, ignoreCommentCountPerFile);
			
			// write DOM back to XML
			try {
				transformer.transform(new DOMSource(doc), new StreamResult(file.toString()));
			} catch (final TransformerException e) {
				logger.error("Transforming to generate XML file failed.", e);
			}
			
			return FileVisitResult.CONTINUE;
		}
		
		@Override
		public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
			logger.error("Failed to access file: {}", file);
			throw exc;
		}
	}
	
}
