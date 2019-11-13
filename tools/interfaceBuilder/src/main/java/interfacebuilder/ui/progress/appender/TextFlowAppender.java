package interfacebuilder.ui.progress.appender;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class TextFlowAppender implements Appender {
	private static final String STYLE = "INFO";
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
		Platform.runLater(() -> children.add(text));
	}
	
}
