module GalaxyLib {
	requires static lombok; // static ones can be absent at run time
	
	// automatic modules :(
	requires org.apache.commons.configuration2;
	requires vtd.xml;
	requires org.eclipse.collections.impl;
	requires org.eclipse.collections.api;
	
	requires transitive java.xml;
	//requires org.apache.commons.lang3; // redundant... not required?
	requires org.slf4j;
	requires java.sql; // configuration2 INI requires java.sql.Date for some reason // redundant, but required!
	
	exports com.ahli.galaxy;
	exports com.ahli.galaxy.archive;
	exports com.ahli.galaxy.game;
	exports com.ahli.galaxy.parser;
	exports com.ahli.galaxy.parser.abstracts;
	exports com.ahli.galaxy.parser.interfaces;
	exports com.ahli.galaxy.ui;
	exports com.ahli.galaxy.ui.abstracts;
	exports com.ahli.galaxy.ui.exception;
	exports com.ahli.galaxy.ui.interfaces;
	exports com.ahli.mpq;
	exports com.ahli.mpq.i18n;
	exports com.ahli.mpq.mpqeditor;
	exports com.ahli.util;
	
	opens com.ahli.galaxy.ui;
	opens com.ahli.galaxy.ui.abstracts;
	opens com.ahli.galaxy.ui.interfaces;
	opens com.ahli.util;
	opens com.ahli.galaxy.parser.interfaces;
	opens com.ahli.galaxy.game;
	opens com.ahli.mpq;
	opens com.ahli.galaxy.archive;
	opens com.ahli.mpq.mpqeditor;
	opens com.ahli.galaxy;
}
