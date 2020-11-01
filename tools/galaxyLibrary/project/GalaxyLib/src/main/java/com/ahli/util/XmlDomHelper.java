// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.util;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Helper methods for handling XML content.
 *
 * @author Ahli
 */
public final class XmlDomHelper {
	
	private static final String REQUIREDTOLOAD = "requiredtoload";
	
	private static final String NEG_PREFIX = "!";
	
	/**
	 * Disabled Constructor.
	 */
	private XmlDomHelper() {
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
}
