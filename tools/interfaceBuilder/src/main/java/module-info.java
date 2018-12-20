module interfacex.builder {
	requires java.desktop;
	requires java.persistence;
	requires java.xml;
	//	requires controlsfx;
	requires core;
	requires de.jensd.fx.glyphs.fontawesome;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires org.apache.commons.configuration2;
	requires org.apache.commons.io;
	requires org.apache.logging.log4j;
	requires org.apache.logging.log4j.core;
	requires org.hibernate.orm.core;
	//	requires richtextfx; disabled because not properly supporting jdk11
	//	requires flowless; part of richtextfx
	requires spring.beans;
	requires spring.boot.autoconfigure;
	requires spring.boot;
	requires spring.context;
	requires spring.core;
	requires spring.data.commons;
	requires spring.data.jpa;
	requires spring.tx;
	requires CascExplorerConfigFileEdit;
	requires GalaxyLib;
	requires kryo;
	
	// fix: spring boot not finding sqlexception
	requires java.sql;
	// fix spring boot not finding jaxbexception
	requires java.xml.bind;
	// fix spring boot not finding bytebuddy classes for hibernate configuration
	requires net.bytebuddy;
	
	// log4j export is a test to fix stylized text area appender
	exports interfacebuilder to javafx.graphics, javafx.fxml, spring.context;
	opens interfacebuilder to javafx.fxml, spring.core, spring.beans;
	opens interfacebuilder.config to spring.core;
	opens interfacebuilder.compress to org.hibernate.orm.core, spring.core;
	opens interfacebuilder.projects to org.hibernate.orm.core, spring.core, spring.beans;
	exports interfacebuilder.config to spring.beans, spring.context;
	opens interfacebuilder.build to spring.core;
	opens interfacebuilder.base_ui to spring.core;
	opens view;
	opens i18n;
	opens res; // not sure if required atm
	opens interfacebuilder.ui.navigation to spring.core, javafx.fxml;
	opens interfacebuilder.ui.home to spring.core, javafx.fxml;
	opens interfacebuilder.ui.settings to spring.core, javafx.fxml;
	opens interfacebuilder.ui.browse to spring.core, javafx.fxml;
	// open to log4j to maybe fix the appender
	opens interfacebuilder.ui.progress to javafx.fxml, org.apache.logging.log4j;
	exports interfacebuilder.ui.progress to org.apache.logging.log4j.core;
	exports interfacebuilder.integration;
	exports interfacebuilder.ui.navigation;
}