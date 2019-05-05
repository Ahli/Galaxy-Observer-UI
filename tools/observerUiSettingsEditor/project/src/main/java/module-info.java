module ObserverUiSettingsEditor {
	requires java.datatransfer;
	requires java.desktop;
	requires java.xml;
	requires java.sql;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires org.apache.commons.io;
	requires org.apache.commons.lang3;
	requires org.apache.logging.log4j;
	requires org.apache.logging.log4j.core;
	requires GalaxyLib;
	exports com.ahli.hotkey_ui.application to javafx.graphics;
	exports com.ahli.hotkey_ui.application.controller to javafx.fxml;
	opens com.ahli.hotkey_ui.application.controller to javafx.fxml;
	opens com.ahli.hotkey_ui.application.model to javafx.base;
	opens res;
	opens view;
}
