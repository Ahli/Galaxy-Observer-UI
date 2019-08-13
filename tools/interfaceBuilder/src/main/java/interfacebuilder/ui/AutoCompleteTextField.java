// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class is a TextField which implements an "autocomplete" functionality, based on a supplied list of entries.
 *
 * @author Caleb Brinkman
 */
public class AutoCompleteTextField extends TextField {
	/** The existing autocomplete entries. */
	private final SortedSet<String> entries;
	/** The popup used to select an entry. */
	private final ContextMenu entriesPopup;
	
	/** Construct a new AutoCompleteTextField. */
	public AutoCompleteTextField() {
		super();
		entries = new TreeSet<>();
		entriesPopup = new ContextMenu();
		textProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(final ObservableValue<? extends String> observableValue, final String s,
					final String s2) {
				if (getText().length() == 0) {
					entriesPopup.hide();
				} else {
					final List<String> searchResult =
							new ArrayList<>(entries.subSet(getText(), getText() + Character.MAX_VALUE));
					if (entries.isEmpty()) {
						entriesPopup.hide();
					} else {
						populatePopup(searchResult);
						if (!entriesPopup.isShowing()) {
							entriesPopup.show(AutoCompleteTextField.this, Side.BOTTOM, 0, 0);
						}
					}
				}
			}
		});
		
		focusedProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(final ObservableValue<? extends Boolean> observableValue, final Boolean aBoolean,
					final Boolean aBoolean2) {
				entriesPopup.hide();
			}
		});
		
	}
	
	/**
	 * Populate the entry set with the given search results.  Display is limited to 10 entries, for performance.
	 *
	 * @param searchResult
	 * 		The set of matching strings.
	 */
	private void populatePopup(final List<String> searchResult) {
		final List<CustomMenuItem> menuItems = new LinkedList<>();
		// If you'd like more entries, modify this line.
		final int maxEntries = 10;
		final int count = Math.min(searchResult.size(), maxEntries);
		for (int i = 0; i < count; i++) {
			final String result = searchResult.get(i);
			final Label entryLabel = new Label(result);
			final CustomMenuItem item = new CustomMenuItem(entryLabel, true);
			item.setOnAction(new EventHandler<>() {
				@Override
				public void handle(final ActionEvent actionEvent) {
					setText(result);
					entriesPopup.hide();
				}
			});
			menuItems.add(item);
		}
		entriesPopup.getItems().clear();
		entriesPopup.getItems().addAll(menuItems);
		
	}
	
	/**
	 * Get the existing set of autocomplete entries.
	 *
	 * @return The existing autocomplete entries.
	 */
	public SortedSet<String> getEntries() {
		return entries;
	}
}
