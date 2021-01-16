package com.ahli.galaxy.validator;

import com.ahli.galaxy.ModData;
import com.ahli.galaxy.ui.UIAnchorSide;
import com.ahli.galaxy.ui.UIAnimation;
import com.ahli.galaxy.ui.UIAttribute;
import com.ahli.galaxy.ui.UIController;
import com.ahli.galaxy.ui.UIFrame;
import com.ahli.galaxy.ui.UIStateGroup;
import com.ahli.galaxy.ui.UITemplate;
import com.ahli.galaxy.ui.abstracts.UIElement;
import com.ahli.galaxy.ui.interfaces.UICatalog;

import java.util.ArrayDeque;
import java.util.List;

public class ReferenceValidator {
	
	private final UICatalog uiCatalog;
	
	public ReferenceValidator(final ModData mod) {
		uiCatalog = mod.getUiCatalog();
	}
	
	private static void validate(final UIAnimation element, final ValidatorData data) {
		for (final UIAttribute event : element.getEvents()) {
			validate(element, event.getValue("frame"), data);
		}
		for (final UIElement c : element.getControllers()) {
			final UIController controller = (UIController) c;
			validate(element, controller, controller.getValue("frame"), data);
		}
	}
	
	private static void validate(
			final UIAnimation element, final UIController controller, final String frame, final ValidatorData data) {
		
	}
	
	private static void validate(
			final UIFrame element, final UIAnchorSide anchorSide, final ValidatorData data) {
		validate(element, element.getAnchorRelative(anchorSide), data);
	}
	
	private static void validate(final UIElement element, final String relative, final ValidatorData data) {
		// TODO $this
		// TODO $parent
		// TODO $ancestor[@type=...]
		// TODO $root // used to create elements in a few special tags, e.g. <NormalImage val="NormalImage"/> or <HoverImage val="HoverImage"/>
		// TODO $layer
		// TODO $sibling
		// TODO $handle
	}
	
	public void validate() {
		if (uiCatalog != null) {
			final ValidatorData data = new ValidatorData(20);
			for (final UITemplate template : uiCatalog.getTemplates()) {
				validate(template.getElement(), data);
			}
		}
	}
	
	private void validate(final UIElement element, final ValidatorData data) {
		if (element instanceof UIFrame) {
			validate((UIFrame) element, data);
		} else if (element instanceof UIAnimation) {
			validate((UIAnimation) element, data);
		} else if (element instanceof UIStateGroup) {
			validate((UIStateGroup) element, data);
		}
	}
	
	private void validate(final UIFrame element, final ValidatorData data) {
		validate(element, UIAnchorSide.TOP, data);
		validate(element, UIAnchorSide.LEFT, data);
		validate(element, UIAnchorSide.RIGHT, data);
		validate(element, UIAnchorSide.BOTTOM, data);
		
		final List<UIElement> children = element.getChildrenRaw();
		if (children != null && !children.isEmpty()) {
			data.parents.addFirst(element);
			for (final UIElement child : children) {
				validate(child, data);
			}
			data.parents.removeFirst();
		}
	}
	
	private void validate(final UIStateGroup element, final ValidatorData data) {
	
	}
	
	private static class ValidatorData {
		private final ArrayDeque<UIFrame> parents;
		//private boolean isDevLayout = false;
		
		private ValidatorData(final int parentStackCapacity) {
			parents = new ArrayDeque<>(parentStackCapacity);
		}
	}
}
