package com.ahli.util;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Helper methods for handling XML content.
 * 
 * @author Ahli
 *
 */
public class XmlDomHelper {
	
	/**
	 * Returns named item of a specified NamedNodeMap (e.g. from attributes).
	 * 
	 * @param nodes
	 *            NamedNodeMap that are scanned
	 * @param name
	 *            String
	 * @return first node with that name ignoring case
	 */
	public static Node getNamedItemIgnoringCase(NamedNodeMap nodes, String name) {
		Node node = nodes.getNamedItem(name);
		if (node == null) {
			for (int i = 0, len = nodes.getLength(); i < len; i++) {
				Node curNode = nodes.item(i);
				if (name.equalsIgnoreCase(curNode.getNodeName())) {
					return curNode;
				}
			}
		}
		return node;
	}
	
}
