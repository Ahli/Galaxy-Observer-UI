// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.ui.progress.appenders;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.extern.log4j.Log4j2;

import static com.ahli.interfacebuilder.ui.AppController.FATAL_ERROR;

@Log4j2
public class TextFlowAppender implements Appender {
	private static final String STYLE = "INFO";
	private final TextFlow textFlow;
	private SimpleBooleanProperty endedProp;
	
	public TextFlowAppender(final TextFlow textFlow) {
		this.textFlow = textFlow;
	}
	
	@Override
	public void append(final String line) {
		final Text text = new Text(line);
		text.setFontSmoothingType(FontSmoothingType.LCD);
		text.getStyleClass().add(STYLE);
		final ObservableList<Node> children = textFlow.getChildren();
		Platform.runLater(() -> {
			try {
				children.add(text);
			} catch (final Exception e) {
				log.fatal(FATAL_ERROR, e);
			}
		});
	}
	
	@Override
	public void end() {
		if (endedProp == null) {
			endedProp = new SimpleBooleanProperty(true);
		} else {
			endedProp.set(true);
		}
	}
	
	@Override
	public SimpleBooleanProperty endedProperty() {
		if (endedProp == null) {
			endedProp = new SimpleBooleanProperty(false);
		}
		return endedProp;
	}
	
}
