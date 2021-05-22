module ObserverUiSettingsEditor {
	requires java.desktop;
	requires java.xml;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires org.apache.commons.io;
	requires org.apache.commons.lang3;
	requires org.apache.logging.log4j;
	requires org.apache.logging.log4j.slf4j;
	requires GalaxyLib;
	exports com.ahli.hotkey_ui.application to javafx.graphics;
	exports com.ahli.hotkey_ui.application.controller to javafx.fxml;
	exports com.ahli.hotkey_ui.application.model to javafx.fxml; // for the TabsController
	opens com.ahli.hotkey_ui.application.controller to javafx.fxml;
	opens com.ahli.hotkey_ui.application.model to javafx.base;
	opens res;
	opens view;
}
