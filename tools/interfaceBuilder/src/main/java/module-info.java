module interfacex.builder {
	// javafx
	requires java.desktop;
	requires java.persistence;
	requires java.xml;
	requires core;
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
	requires pecoff4j;
	
	// spring
	requires jakarta.activation;
	requires org.hibernate.orm.core;
	requires spring.beans;
	requires spring.boot.autoconfigure;
	requires spring.boot;
	requires spring.context;
	requires spring.core;
	requires spring.data.commons;
	requires spring.data.jpa;
	requires spring.tx;
	requires com.fasterxml.classmate;
	requires net.bytebuddy;
	requires java.sql;
	
	// own projects
	requires CascExplorerConfigFileEdit;
	requires GalaxyLib;
	
	// log4j export is a test to fix stylized text area appender
	exports interfacebuilder to javafx.graphics, javafx.fxml, spring.context;
	opens interfacebuilder to javafx.fxml, spring.core, spring.beans;
	opens interfacebuilder.config to spring.core;
	opens interfacebuilder.compress to org.hibernate.orm.core, spring.core;
	opens interfacebuilder.projects to org.hibernate.orm.core, spring.core, spring.beans;
	exports interfacebuilder.config to spring.beans, spring.context;
	opens interfacebuilder.build to spring.core;
	opens interfacebuilder.base_ui to spring.core, com.esotericsoftware.kryo;
	opens interfacebuilder.integration.kryo to spring.core, com.esotericsoftware.kryo;
	exports interfacebuilder.integration.kryo;
	opens view;
	opens i18n;
	opens res;
	opens interfacebuilder.ui.navigation to spring.core, javafx.fxml;
	opens interfacebuilder.ui.home to spring.core, javafx.fxml;
	opens interfacebuilder.ui.settings to spring.core, javafx.fxml;
	opens interfacebuilder.ui.browse to spring.core, javafx.fxml;
	opens interfacebuilder.ui.progress to javafx.fxml, spring.core;
	exports interfacebuilder.integration.log4j to org.apache.logging.log4j.core;
	exports interfacebuilder.integration;
	exports interfacebuilder.ui.navigation;
	
	// spring actuator for testing
	//	requires spring.boot.actuator;
	//	requires spring.boot.actuator.autoconfigure;
	//	requires spring.boot.starter.actuator;
}
