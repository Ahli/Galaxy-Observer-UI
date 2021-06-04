// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui.browse;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

// based on:
// http://jluger.de/blog/20160731_javafx_text_rendering_in_tableview.html
public class WrappingTextTableCell extends TableCell<Map.Entry<String, String>, String> {
	private static final Logger logger = LogManager.getLogger(WrappingTextTableCell.class);
	
	public WrappingTextTableCell() {
		super();
	}
	
	@Override
	protected void updateItem(final String item, final boolean empty) {
		if (empty || item == null) {
			logger.trace("update wrapping table cell - null");
			super.updateItem(item, empty);
			setGraphic(null);
			setText(null);
		} else {
			// check if old value equals new value
			final boolean notEquals = !item.equals(getItem());
			super.updateItem(item, false);
			if (notEquals) {
				logger.trace("update wrapping table cell - newLabel {}", item);
				final Label l = new Label(item);
				l.setWrapText(true);
				final VBox box = new VBox(l);
				l.heightProperty().addListener((observable, oldValue, newValue) -> {
					box.setPrefHeight(newValue.doubleValue() + 7);
					Platform.runLater(() -> getTableRow().requestLayout());
				});
				setGraphic(box);
			} else {
				logger.trace("update wrapping table cell - equal");
			}
		}
	}
}
