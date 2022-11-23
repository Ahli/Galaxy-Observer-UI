module GalaxyLib {
	requires static lombok; // static ones can be absent at run time
	
	// automatic modules :(
	requires org.apache.commons.configuration2;
	requires org.eclipse.collections.impl;
	requires org.eclipse.collections.api;
	//	requires vtd.xml;
	
	requires transitive java.xml;
	requires transitive org.jetbrains.annotations;
	requires org.slf4j;
	requires com.fasterxml.aalto;
	
	exports com.ahli.cloning;
	exports com.ahli.files;
	exports com.ahli.galaxy;
	exports com.ahli.galaxy.archive;
	exports com.ahli.galaxy.game;
	exports com.ahli.galaxy.ui;
	exports com.ahli.galaxy.ui.abstracts;
	exports com.ahli.galaxy.ui.exceptions;
	exports com.ahli.galaxy.ui.interfaces;
	exports com.ahli.galaxy.parser;
	exports com.ahli.galaxy.parser.abstracts;
	exports com.ahli.galaxy.parser.interfaces;
	exports com.ahli.memory;
	exports com.ahli.memory.interner;
	exports com.ahli.mpq;
	exports com.ahli.mpq.i18n;
	exports com.ahli.mpq.mpqeditor;
	exports com.ahli.xml;
	
	opens com.ahli.cloning;
	opens com.ahli.files;
	opens com.ahli.galaxy.archive;
	opens com.ahli.galaxy.game;
	opens com.ahli.galaxy;
	opens com.ahli.galaxy.ui;
	opens com.ahli.galaxy.ui.abstracts;
	opens com.ahli.galaxy.ui.interfaces;
	opens com.ahli.galaxy.parser.interfaces;
	opens com.ahli.memory;
	opens com.ahli.mpq;
	opens com.ahli.mpq.mpqeditor;
	opens com.ahli.xml;
}
