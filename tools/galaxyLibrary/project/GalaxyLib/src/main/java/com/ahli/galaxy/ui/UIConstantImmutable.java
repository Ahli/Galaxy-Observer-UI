// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui;

import com.ahli.galaxy.ui.abstracts.UIElementAbstract;
import com.ahli.galaxy.ui.interfaces.UIConstant;
import com.ahli.galaxy.ui.interfaces.UIElement;
import com.ahli.memory.StringInterner;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Ahli
 */
public class UIConstantImmutable extends UIElementAbstract implements UIConstant {
	private final String value;
	
	// for hashcode caching
	//	protected boolean hashIsDirty;
	private boolean hashIsZero;
	private int hash;
	
	/**
	 * Constructor for Kryo
	 */
	private UIConstantImmutable() {
		super(null);
		value = null;
	}
	
	public UIConstantImmutable(final String name, final String value) {
		super(name);
		this.value = StringInterner.intern(value);
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object deepCopy() {
		// UIConstant is immutable
		return this;
	}
	
	@Override
	public void setName(final String name) {
		throw new UnsupportedOperationException("UIConstants are immutable");
	}
	
	/**
	 * @return the value
	 */
	@Override
	public String getValue() {
		return value;
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
	@SuppressWarnings("java:S1168")
	public List<UIElement> getChildrenRaw() {
		return null; // returning null is desired here
	}
	
	@Override
	public final boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof final UIConstantImmutable that)) {
			return false;
		}
		if (!super.equals(obj)) {
			return false;
		}
		return that.canEqual(this) && Objects.equals(value, that.value);
	}
	
	@Override
	public boolean canEqual(final Object other) {
		return (other instanceof UIConstantImmutable);
	}
	
	@Override
	public final int hashCode() {
		int h = hash;
		if ((h == 0 && !hashIsZero) /*|| hashIsDirty*/) {
			h = calcHashCode();
			if (h == 0) {
				hashIsZero = true;
			} else {
				hash = h;
			}
			// hashIsDirty = false;
		}
		return h;
	}
	
	private int calcHashCode() {
		return Objects.hash(super.hashCode(), value);
	}
}
