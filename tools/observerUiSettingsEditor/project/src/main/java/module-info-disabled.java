//module ObserverUiSettingsEditor {
//	requires java.datatransfer;
//	requires java.xml;
//	requires javafx.base;
//	requires javafx.controls;
//	requires javafx.fxml;
//	requires javafx.graphics;
//	requires org.apache.commons.io;
//	requires org.apache.commons.lang3;
//	requires org.apache.logging.log4j;
//	requires GalaxyLib;
//	requires java.desktop;
//	requires java.sql;
//
//	exports com.ahli.hotkeyUi.application to javafx.graphics;
//	exports com.ahli.hotkeyUi.application.controller to javafx.fxml;
//	opens com.ahli.hotkeyUi.application.controller to javafx.fxml;
//	opens com.ahli.hotkeyUi.application.model to javafx.base;
//}