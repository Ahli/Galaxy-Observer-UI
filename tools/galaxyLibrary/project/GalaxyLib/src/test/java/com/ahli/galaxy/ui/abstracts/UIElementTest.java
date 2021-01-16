package com.ahli.galaxy.ui.abstracts;

import com.ahli.galaxy.ui.UIAnimation;
import com.ahli.galaxy.ui.UIAttribute;
import com.ahli.galaxy.ui.UIConstant;
import com.ahli.galaxy.ui.UIController;
import com.ahli.galaxy.ui.UIFrame;
import com.ahli.galaxy.ui.UIState;
import com.ahli.galaxy.ui.UIStateGroup;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

class UIElementTest {
	
	@Test
	public void equalsContract() {
		EqualsVerifier.forClass(UIElement.class)
				.withRedefinedSubclass(UIFrame.class)
				.withRedefinedSubclass(UIStateGroup.class)
				.withRedefinedSubclass(UIState.class)
				.withRedefinedSubclass(UIAttribute.class)
				.withRedefinedSubclass(UIAnimation.class)
				.withRedefinedSubclass(UIConstant.class)
				.withRedefinedSubclass(UIController.class)
				.suppress(Warning.NONFINAL_FIELDS)
				.verify();
	}
	
}