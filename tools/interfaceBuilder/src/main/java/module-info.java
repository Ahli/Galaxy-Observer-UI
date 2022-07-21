/*
 * requires:
 * --add-opens=javafx.controls/javafx.scene.control=interfacex.builder
 */
module interfacex.builder {
	
	requires static lombok; // static ones can be absent at run time
	
	requires transitive jakarta.persistence;
	requires org.flywaydb.core; // automatic module
	
	// javafx
	requires java.desktop;
	requires java.xml;
	requires javafx.base;
	requires transitive javafx.controls;
	requires transitive javafx.fxml;
	requires transitive javafx.graphics;
	requires de.jensd.fx.glyphs.fontawesome;
	
	// misc
	requires org.apache.commons.configuration2; // automatic module
	requires org.apache.logging.log4j;
	requires org.apache.logging.log4j.core; // automatic module
	requires transitive com.esotericsoftware.kryo; // automatic module
	//requires com.esotericsoftware.minlog;
	requires com.kichik.pecoff4j;
	//requires org.eclipse.collections.api;
	requires org.eclipse.collections.impl; // automatic module
	
	// spring
	//requires jakarta.activation;
	requires org.hibernate.orm.core; // automatic module
	requires transitive spring.beans; // automatic module
	requires transitive spring.boot.autoconfigure; // automatic module
	requires spring.boot; // automatic module
	requires transitive spring.context; // automatic module
	requires transitive spring.core; // automatic module
	requires spring.data.commons; // automatic module
	requires transitive spring.data.jpa;
	requires spring.tx; // automatic module
	requires spring.jdbc; // automatic module
//	requires com.fasterxml.classmate;//redundant, but required!
//	requires net.bytebuddy; // redundant, but required!
	requires java.sql; //redundant, but required!
//	requires jakarta.xml.bind; // provides jakarta/xml/jaxbexception //redundant, but required!
	requires org.apache.commons.lang3; // automatic module
	requires org.slf4j; // automatic module
	
	// own projects
	requires transitive GalaxyLib;
	
	// log4j export is a test to fix stylized text area appender
	exports com.ahli.interfacebuilder; //to javafx.graphics, javafx.fxml, spring.context;
	opens com.ahli.interfacebuilder to javafx.fxml, spring.core, spring.beans;
	opens com.ahli.interfacebuilder.config to spring.core;
	opens com.ahli.interfacebuilder.ui to spring.core;
	opens com.ahli.interfacebuilder.compress;
	// open to unnamed for unit test
	opens com.ahli.interfacebuilder.projects; // to org.hibernate.orm.core, spring.core, spring.beans;
	exports com.ahli.interfacebuilder.config; //to spring.beans, spring.context;
	opens com.ahli.interfacebuilder.build to spring.core;
	opens com.ahli.interfacebuilder.base_ui to spring.core, com.esotericsoftware.kryo;
	// open to unnamed for unit test
	opens com.ahli.interfacebuilder.integration.kryo; //to spring.core, com.esotericsoftware.kryo;
	exports com.ahli.interfacebuilder.integration.kryo;
	opens com.ahli.interfacebuilder.integration; // for unit test
	opens view;
	opens i18n;
	opens res;
	opens com.ahli.interfacebuilder.ui.navigation to spring.core, javafx.fxml;
	opens com.ahli.interfacebuilder.ui.home to spring.core, javafx.fxml;
	opens com.ahli.interfacebuilder.ui.settings to spring.core, javafx.fxml;
	opens com.ahli.interfacebuilder.ui.browse to spring.core, javafx.fxml;
	opens com.ahli.interfacebuilder.ui.progress to javafx.fxml, spring.core;
	opens com.ahli.interfacebuilder.threads to spring.core;
	opens com.ahli.interfacebuilder.integration.ipc;
	opens db.migration;
	exports com.ahli.interfacebuilder.integration.log4j to org.apache.logging.log4j.core;
	
	exports com.ahli.interfacebuilder.integration;
	exports com.ahli.interfacebuilder.ui.navigation;
	exports com.ahli.interfacebuilder.ui.progress.appenders;
	exports com.ahli.interfacebuilder.ui;
	exports com.ahli.interfacebuilder.threads;
	exports com.ahli.interfacebuilder.integration.ipc;
	
	// for public API only
	exports com.ahli.interfacebuilder.ui.progress;
	exports com.ahli.interfacebuilder.projects;
	exports com.ahli.interfacebuilder.compress;
	exports com.ahli.interfacebuilder.projects.enums;
	
	// for beans
	exports com.ahli.interfacebuilder.build;
	exports com.ahli.interfacebuilder.compile;
	exports com.ahli.interfacebuilder.base_ui;
	exports com.ahli.interfacebuilder.ui.home;
	exports com.ahli.interfacebuilder.ui.settings;
	exports com.ahli.interfacebuilder.ui.browse;
	
	// spring actuator for testing
	//	requires spring.boot.actuator;
	//	requires spring.boot.actuator.autoconfigure;
	//	requires spring.boot.starter.actuator;
}
