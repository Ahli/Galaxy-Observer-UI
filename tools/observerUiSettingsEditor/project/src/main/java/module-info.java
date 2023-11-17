module ObserverUiSettingsEditor {
	requires static lombok; // static ones can be absent at run time
	
	// automatic modules :(
	requires org.apache.commons.lang3;
	requires com.esotericsoftware.kryo;
	
	requires java.desktop;
	requires java.xml;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires org.slf4j;
	requires ch.qos.logback.classic; // entry required for logging after jlink (NOT redundant!)
	requires ch.qos.logback.core; // required for logback classic (NOT redundant!)
	requires java.naming; // required for logback classic (NOT redundant!)
	
	requires GalaxyLib;
	exports com.ahli.hotkey_ui.application to javafx.graphics;
	exports com.ahli.hotkey_ui.application.controllers to javafx.fxml;
	exports com.ahli.hotkey_ui.application.model to javafx.fxml; // for the TabsController
	opens com.ahli.hotkey_ui.application.controllers to javafx.fxml;
	opens com.ahli.hotkey_ui.application.model to javafx.base;
	opens res;
	opens view;
	exports com.ahli.hotkey_ui.application.model.abstracts to javafx.fxml;
	opens com.ahli.hotkey_ui.application.model.abstracts to javafx.base;
}
