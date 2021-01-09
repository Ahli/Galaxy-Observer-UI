// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.browse;

import com.ahli.galaxy.ui.UIAnimation;
import com.ahli.galaxy.ui.UIController;
import com.ahli.galaxy.ui.UIFrame;
import com.ahli.galaxy.ui.UIState;
import com.ahli.galaxy.ui.UIStateGroup;
import com.ahli.galaxy.ui.abstracts.UIElement;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.commons.lang3.StringUtils;

public class TextFlowFactory {
	
	private String query;
	
	public void setHighlight(final String query) {
		this.query = query;
	}
	
	public TextFlow getFlow(final UIElement item) {
		final TextFlow flow = new TextFlow();
		
		if (item instanceof UIFrame) {
			final var frame = (UIFrame) item;
			append(flow, textUnimportant("<Frame type=\""));
			append(flow, textQueried(frame.getType()));
			append(flow, textUnimportant("\" name=\""));
			append(flow, textQueried(frame.getName()));
			append(flow, textUnimportant("\">"));
		} else if (item instanceof UIAnimation) {
			final var anim = (UIAnimation) item;
			append(flow, textUnimportant("<Animation name=\""));
			append(flow, textQueried(anim.getName()));
			append(flow, textUnimportant("\">"));
		} else if (item instanceof UIState) {
			final var state = (UIState) item;
			append(flow, textUnimportant("<State name=\""));
			append(flow, textQueried(state.getName()));
			append(flow, textUnimportant("\">"));
		} else if (item instanceof UIController) {
			final var ctrl = (UIController) item;
			append(flow, textUnimportant("<Controller name=\""));
			append(flow, textQueried(ctrl.getName()));
			append(flow, textUnimportant("\">"));
		} else if (item instanceof UIStateGroup) {
			final var stateGroup = (UIStateGroup) item;
			append(flow, textUnimportant("<StateGroup name=\""));
			append(flow, textQueried(stateGroup.getName()));
			append(flow, textUnimportant("\">"));
		} else {
			append(flow, textQueried(item.toString()));
		}
		return flow;
	}
	
	private static void append(final TextFlow flow, final Text text) {
		flow.getChildren().add(text);
	}
	
	private static Text textUnimportant(final String label) {
		final Text text = new Text(label);
		text.setStyle("-fx-fill: grey; -fx-font-smoothing-type: lcd;");
		return text;
	}
	
	private static void append(final TextFlow flow, final Text[] text) {
		flow.getChildren().addAll(text);
	}
	
	private Text[] textQueried(final String label) {
		int count = 1;
		int index = -1;
		if (query != null && !query.isEmpty()) {
			index = StringUtils.indexOfIgnoreCase(label, query);
			if (index > -1) {
				// match in the front = 2 segments; match in the middle = 3 segments
				count += (index > 0 ? 2 : 1);
			}
		}
		final Text[] text = new Text[count];
		
		if (count > 2) {
			text[0] = new Text(label.substring(0, index));
			text[1] = new Text(label.substring(index, index + query.length()));
			text[2] = new Text(label.substring(index + query.length()));
			text[0].setStyle("-fx-fill: white; -fx-font-smoothing-type: lcd;");
			text[1].setStyle("-fx-fill: orange; -fx-font-smoothing-type: lcd;");
			text[2].setStyle("-fx-fill: white; -fx-font-smoothing-type: lcd;");
			applyVisualHighlight(text[1]);
		} else if (count > 1) {
			text[0] = new Text(label.substring(0, index + query.length()));
			text[1] = new Text(label.substring(index + query.length()));
			text[0].setStyle("-fx-fill: orange; -fx-font-smoothing-type: lcd;");
			text[1].setStyle("-fx-fill: white; -fx-font-smoothing-type: lcd;");
			applyVisualHighlight(text[0]);
		} else {
			text[0] = new Text(label);
			text[0].setStyle("-fx-fill: white; -fx-font-smoothing-type: lcd;");
		}
		return text;
	}
	
	private static void applyVisualHighlight(final Text text) {
		final Blend blend = new Blend();
		blend.setMode(BlendMode.MULTIPLY);
		
		final DropShadow ds = new DropShadow();
		ds.setColor(Color.rgb(188, 254, 66, 0.1));
		ds.setOffsetX(0);
		ds.setOffsetY(0);
		ds.setRadius(13);
		ds.setSpread(0.8);
		ds.setBlurType(BlurType.TWO_PASS_BOX);
		
		blend.setBottomInput(ds);
		
		text.setEffect(blend);
	}
}
