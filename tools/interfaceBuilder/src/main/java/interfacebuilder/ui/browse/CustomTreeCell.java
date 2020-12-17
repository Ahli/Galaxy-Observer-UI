// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.browse;

import com.ahli.galaxy.ui.abstracts.UIElement;
import javafx.scene.control.TreeCell;

public class CustomTreeCell extends TreeCell<UIElement> {
	
	private final TextFlowFactory flowFactory;
	
	public CustomTreeCell(final TextFlowFactory flowFactory) {
		super();
		this.flowFactory = flowFactory;
	}
	
	@Override
	protected void updateItem(final UIElement item, final boolean empty) {
		super.updateItem(item, empty);
		
		if (empty) {
			setGraphic(null);
		} else {
			setGraphic(flowFactory.getFlow(item));
		}
		setText(null);
	}
}
