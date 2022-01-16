module ObserverUiSettingsEditor {
	// automatic modules :(
	requires org.apache.commons.lang3;
	
	requires java.desktop;
	requires java.xml;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires org.slf4j;
	requires ch.qos.logback.classic;
	requires ch.qos.logback.core;
	requires java.naming; // required for logback classic
	
	requires GalaxyLib;
	exports com.ahli.hotkey_ui.application to javafx.graphics;
	exports com.ahli.hotkey_ui.application.controllers to javafx.fxml;
	exports com.ahli.hotkey_ui.application.model to javafx.fxml; // for the TabsController
	opens com.ahli.hotkey_ui.application.controllers to javafx.fxml;
	opens com.ahli.hotkey_ui.application.model to javafx.base;
	opens res;
	opens view;
}
