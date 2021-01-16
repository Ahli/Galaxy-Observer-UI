// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui;

import com.ahli.galaxy.ui.abstracts.UIElement;
import com.ahli.util.StringInterner;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Ahli
 */
public class UIConstant extends UIElement {
	private String value;
	
	public UIConstant() {
		super(null);
	}
	
	/**
	 * @param name
	 */
	public UIConstant(final String name) {
		super(name);
	}
	
	public UIConstant(final String name, final String value) {
		super(name);
		this.value = StringInterner.intern(value);
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object deepCopy() {
		final UIConstant clone = new UIConstant(getName());
		clone.value = value;
		return clone;
	}
	
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * @param value
	 * 		the value to set
	 */
	public void setValue(final String value) {
		this.value = StringInterner.intern(value);
	}
	
	/**
	 * @param path
	 * @return
	 */
	@Override
	public UIElement receiveFrameFromPath(final String path) {
		return (path == null || path.isEmpty()) ? this : null;
	}
	
	@Override
	public String toString() {
		return "<Constant name='" + getName() + "', value='" + value + "'>";
	}
	
	@Override
	public List<UIElement> getChildren() {
		return Collections.emptyList();
	}
	
	@Override
	public List<UIElement> getChildrenRaw() {
		return null; // returning null is desired here
	}
	
	@Override
	public final boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof UIConstant)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		final UIConstant that = (UIConstant) o;
		return that.canEqual(this) && Objects.equals(value, that.value);
	}
	
	@Override
	public boolean canEqual(final Object other) {
		return (other instanceof UIConstant);
	}
	
	@Override
	public final int hashCode() {
		//noinspection ObjectInstantiationInEqualsHashCode
		return Objects.hash(super.hashCode(), value);
	}
}
