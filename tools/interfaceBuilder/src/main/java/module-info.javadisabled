/*
Building within IntelliJ does not work due to apache geronimo specs not being a module and automodule name cannot be
generated.
*/
module interfacex.builder {
	
	requires static lombok; // static ones can be absent at run time
	
	// javafx
	requires java.desktop;
	requires java.persistence;
	//requires jakarta.persistence;
	requires java.xml;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires de.jensd.fx.glyphs.fontawesome;
	
	// misc
	requires org.apache.commons.configuration2;
	requires org.apache.commons.io;
	requires org.apache.logging.log4j;
	requires org.apache.logging.log4j.core;
	requires com.esotericsoftware.kryo;
	//requires com.esotericsoftware.minlog;
	requires com.kichik.pecoff4j;
	//requires org.eclipse.collections.api;
	requires org.eclipse.collections.impl;
	
	// spring
	//requires jakarta.activation;
	requires org.hibernate.orm.core;
	requires spring.beans;
	requires spring.boot.autoconfigure;
	requires spring.boot;
	requires spring.context;
	requires spring.core;
	requires spring.data.commons;
	requires spring.data.jpa;
	requires spring.tx;
	requires com.fasterxml.classmate;//redundant, but required!
	requires net.bytebuddy; // redundant, but required!
	requires java.sql; //redundant, but required!
	requires java.xml.bind; // provides javax/xml/jaxbexception //redundant, but required!
	requires org.apache.commons.lang3;
	
	// own projects
	requires transitive GalaxyLib;
	
	// log4j export is a test to fix stylized text area appender
	exports interfacebuilder to javafx.graphics, javafx.fxml, spring.context;
	opens interfacebuilder to javafx.fxml, spring.core, spring.beans;
	opens interfacebuilder.config to spring.core;
	opens interfacebuilder.ui to spring.core;
	opens interfacebuilder.compress;
	// open to unnamed for unit test
	opens interfacebuilder.projects; // to org.hibernate.orm.core, spring.core, spring.beans;
	exports interfacebuilder.config; //to spring.beans, spring.context;
	opens interfacebuilder.build to spring.core;
	opens interfacebuilder.base_ui to spring.core, com.esotericsoftware.kryo;
	// open to unnamed for unit test
	opens interfacebuilder.integration.kryo; //to spring.core, com.esotericsoftware.kryo;
	exports interfacebuilder.integration.kryo;
	opens interfacebuilder.integration; // for unit test
	opens view;
	opens i18n;
	opens res;
	opens interfacebuilder.ui.navigation to spring.core, javafx.fxml;
	opens interfacebuilder.ui.home to spring.core, javafx.fxml;
	opens interfacebuilder.ui.settings to spring.core, javafx.fxml;
	opens interfacebuilder.ui.browse to spring.core, javafx.fxml;
	opens interfacebuilder.ui.progress to javafx.fxml, spring.core;
	opens interfacebuilder.threads to spring.core;
	exports interfacebuilder.integration.log4j to org.apache.logging.log4j.core;
	
	exports interfacebuilder.integration;
	exports interfacebuilder.ui.navigation;
	exports interfacebuilder.ui.progress.appender;
	exports interfacebuilder.ui;
	exports interfacebuilder.threads;
	
	// for public API only
	exports interfacebuilder.ui.progress;
	exports interfacebuilder.projects;
	exports interfacebuilder.compress;
	exports interfacebuilder.projects.enums;
	
	// for beans
	exports interfacebuilder.build;
	exports interfacebuilder.compile;
	exports interfacebuilder.base_ui;
	exports interfacebuilder.ui.home;
	exports interfacebuilder.ui.settings;
	exports interfacebuilder.ui.browse;
	
	// spring actuator for testing
	//	requires spring.boot.actuator;
	//	requires spring.boot.actuator.autoconfigure;
	//	requires spring.boot.starter.actuator;
}
