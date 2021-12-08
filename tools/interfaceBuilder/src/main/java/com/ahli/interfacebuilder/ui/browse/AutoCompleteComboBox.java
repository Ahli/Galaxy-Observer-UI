// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.ui.browse;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a ComboBox which implements a search functionality which appears in a context menu.
 * <p>
 * This implementation is based on Caleb Brinkman's AutoCompleteTextField.
 */
@Log4j2
public class AutoCompleteComboBox extends ComboBox<String> {
	/** The popup used to select an entry. */
	private final ContextMenu entriesPopup;
	
	/** Construct a new AutoCompleteTextField. */
	public AutoCompleteComboBox() {
		setEditable(true);
		
		entriesPopup = new ContextMenu();
		
		final TextField editor = getEditor();
		if (editor != null) {
			editor.textProperty().addListener(new TextChangeListener());
		}
		
		focusedProperty().addListener((observableValue, aBoolean, aBoolean2) -> entriesPopup.hide());
		
	}
	
	/**
	 * Code by Icza from https://stackoverflow.com/a/25379180/12849006
	 *
	 * @param src
	 * @param what
	 * @return
	 */
	public static boolean containsIgnoreCase(final String src, final String what) {
		final int length = what.length();
		if (length == 0) {
			return true; // Empty string is contained
		}
		
		final char firstLo = Character.toLowerCase(what.charAt(0));
		final char firstUp = Character.toUpperCase(what.charAt(0));
		
		for (int i = src.length() - length; i >= 0; --i) {
			// Quick check before calling the more expensive regionMatches() method:
			final char ch = src.charAt(i);
			if (ch != firstLo && ch != firstUp) {
				continue;
			}
			
			if (src.regionMatches(true, i, what, 0, length)) {
				return true;
			}
		}
		return false;
	}
	
	private class TextChangeListener implements ChangeListener<String> {
		@Override
		public void changed(
				final ObservableValue<? extends String> observableValue, final String oldVal, final String newVal) {
			if (newVal.isEmpty() || !isFocused()) {
				entriesPopup.hide();
			} else {
				// TODO visually show the matching area in contextmenu's texts
				final int maxEntries = 20;
				final List<CustomMenuItem> menuItems = new ArrayList<>(maxEntries);
				int curEntries = 0;
				for (final String item : getItems()) {
					if (containsIgnoreCase(item, newVal)) {
						@SuppressWarnings("ObjectAllocationInLoop")
						final Label entryLabel = new Label(item);
						@SuppressWarnings("ObjectAllocationInLoop")
						final CustomMenuItem menuItem = new CustomMenuItem(entryLabel, true);
						//noinspection ObjectAllocationInLoop
						menuItem.setOnAction(new MenuItemEventHandler(AutoCompleteComboBox.this, item, entriesPopup));
						menuItems.add(menuItem);
						
						++curEntries;
						if (curEntries >= maxEntries) {
							break;
						}
					}
				}
				if (!menuItems.isEmpty()) {
					entriesPopup.getItems().clear();
					entriesPopup.getItems().addAll(menuItems);
					entriesPopup.show(AutoCompleteComboBox.this, Side.BOTTOM, 0, 0);
				} else {
					entriesPopup.hide();
					entriesPopup.getItems().clear();
				}
			}
		}
		
		private static class MenuItemEventHandler implements EventHandler<ActionEvent> {
			private final AutoCompleteComboBox autoCompleteComboBox;
			private final String item;
			private final ContextMenu entriesPopup;
			
			public MenuItemEventHandler(
					final AutoCompleteComboBox autoCompleteComboBox,
					final String item,
					final ContextMenu entriesPopup) {
				this.autoCompleteComboBox = autoCompleteComboBox;
				this.item = item;
				this.entriesPopup = entriesPopup;
			}
			
			@Override
			public void handle(final ActionEvent event) {
				autoCompleteComboBox.getSelectionModel().select(item);
				entriesPopup.hide();
			}
		}
	}
}
