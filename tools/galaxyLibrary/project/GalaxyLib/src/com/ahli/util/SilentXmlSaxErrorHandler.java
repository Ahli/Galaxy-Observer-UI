package com.ahli.util;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SilentXmlSaxErrorHandler implements ErrorHandler {

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

}
