package com.ahli.util;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Error Handler for XML parsing that is completely silent.
 * 
 * @author Ahli
 *
 */
public class SilentXmlSaxErrorHandler implements ErrorHandler {
	
	@Override
	public void warning(final SAXParseException e) throws SAXException {
	}
	
	@Override
	public void fatalError(final SAXParseException e) throws SAXException {
		throw e;
	}
	
	@Override
	public void error(final SAXParseException e) throws SAXException {
		throw e;
	}
	
}
