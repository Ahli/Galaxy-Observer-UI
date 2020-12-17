// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.progress.appender;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static interfacebuilder.InterfaceBuilderApp.FATAL_ERROR;

public class TextFlowAppender implements Appender {
	private static final String STYLE = "INFO";
	private static final Logger logger = LogManager.getLogger(TextFlowAppender.class);
	private final TextFlow textFlow;
	
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
				logger.fatal(FATAL_ERROR, e);
			}
		});
	}
	
}
