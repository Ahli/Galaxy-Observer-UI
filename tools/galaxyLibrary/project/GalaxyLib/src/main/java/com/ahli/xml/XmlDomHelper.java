// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.xml;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

/**
 * Helper methods for handling XML content.
 *
 * @author Ahli
 */
public final class XmlDomHelper {
	
	private static final String REQUIREDTOLOAD = "requiredtoload";
	
	private static final String NEG_PREFIX = "!";
	
	private XmlDomHelper() {
		// no instances allowed
	}
	
	/**
	 * Checks if any attribute is the requiredToLoad one and does not start with "!".
	 *
	 * @param attributes
	 * @return
	 */
	public static boolean isFailingRequiredToLoad(final NamedNodeMap attributes) {
		final Node requiredtoloadAttr = XmlDomHelper.getNamedItemIgnoringCase(attributes, REQUIREDTOLOAD);
		return requiredtoloadAttr != null && !requiredtoloadAttr.getNodeValue().trim().startsWith(NEG_PREFIX);
	}
	
	/**
	 * Returns named item of a specified NamedNodeMap (e.g. from attributes).
	 *
	 * @param nodes
	 * 		NamedNodeMap that are scanned
	 * @param name
	 * 		String
	 * @return first node with that name ignoring case
	 */
	public static Node getNamedItemIgnoringCase(final NamedNodeMap nodes, final String name) {
		final Node node = nodes.getNamedItem(name);
		if (node == null) {
			for (int i = 0, len = nodes.getLength(); i < len; ++i) {
				final Node curNode = nodes.item(i);
				if (name.equalsIgnoreCase(curNode.getNodeName())) {
					return curNode;
				}
			}
		}
		return node;
	}
	
	public static DocumentBuilder buildSecureDocumentBuilder() throws ParserConfigurationException {
		return buildSecureDocumentBuilder(false, false);
	}
	
	/**
	 * Builds an instance of a secure DocumentBuilder for safe XML processing.
	 *
	 * @param useSilentErrorHandler
	 * 		uses a silent error handler to avoid printing errors like incompatible files into console
	 * @param ignoreComments
	 * 		ignore comments
	 * @return secure DocumentBuilder instance
	 * @throws ParserConfigurationException
	 * 		if the safe configuration is invalid
	 */
	public static DocumentBuilder buildSecureDocumentBuilder(
			final boolean useSilentErrorHandler, final boolean ignoreComments) throws ParserConfigurationException {
		// according to https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#jaxp-documentbuilderfactory-saxparserfactory-and-dom4j
		final DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
		dbFac.setValidating(false);
		dbFac.setAttribute("http://xml.org/sax/features/external-general-entities", false);
		dbFac.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		dbFac.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		dbFac.setXIncludeAware(false);
		dbFac.setExpandEntityReferences(false);
		// other
		dbFac.setAttribute(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		dbFac.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		dbFac.setNamespaceAware(false);
		if (ignoreComments) {
			dbFac.setIgnoringComments(true);
		}
		final DocumentBuilder dBuilder = dbFac.newDocumentBuilder();
		if (useSilentErrorHandler) {
			// provide error handler that does not print incompatible files into console
			dBuilder.setErrorHandler(new SilentXmlSaxErrorHandler());
		}
		return dBuilder;
	}
	
	public static Transformer buildSecureTransformer() throws TransformerConfigurationException {
		final TransformerFactory factory = TransformerFactory.newInstance();
		// according to https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#TransformerFactory
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		// other:
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		return factory.newTransformer();
	}
}
