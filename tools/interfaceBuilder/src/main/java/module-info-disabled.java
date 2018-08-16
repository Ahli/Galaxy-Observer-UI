//module interfacex.builder {
//	requires java.desktop;
//	requires java.persistence;
//	requires java.xml;
//	requires com.fasterxml.jackson.databind;
//	requires com.fasterxml.jackson.annotation;
//	requires controlsfx;
//	requires core;
//	requires de.jensd.fx.glyphs.fontawesome;
//	requires flowless;
//	requires hibernate.core;
//	requires javafx.base;
//	requires javafx.controls;
//	requires javafx.fxml;
//	requires javafx.graphics;
//	requires org.apache.commons.configuration2;
//	requires org.apache.commons.io;
//	requires org.apache.logging.log4j;
//	requires org.apache.logging.log4j.core;
//	requires richtextfx;
//	requires spring.beans;
//	requires spring.boot.autoconfigure;
//	requires spring.context;
//	requires spring.core;
//	requires spring.data.commons;
//	requires spring.data.jpa;
//	requires spring.tx;
//	requires CascExplorerConfigFileEdit;
//	requires GalaxyLib;
//	requires java.sql;
//	//requires javax.transaction.api;
//
//	exports interfacebuilder to javafx.graphics, spring.core;
//	exports interfacebuilder.config to spring.core;
//	opens interfacebuilder to spring.core;
//	opens interfacebuilder.config to spring.core;
//}