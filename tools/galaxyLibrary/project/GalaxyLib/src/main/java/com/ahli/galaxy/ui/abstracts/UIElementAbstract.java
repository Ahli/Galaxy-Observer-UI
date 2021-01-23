// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui.abstracts;

import com.ahli.galaxy.ui.interfaces.UIElement;
import com.ahli.util.StringInterner;

/**
 * @author Ahli
 */
public abstract class UIElementAbstract implements UIElement {
	
	// for hashcode caching
	protected boolean hashIsDirty;
	protected boolean hashIsZero;
	protected int hash;
	private String name;
	
	/**
	 * Constructor.
	 *
	 * @param name
	 * 		element's name
	 */
	protected UIElementAbstract(final String name) {
		this.name = name != null ? StringInterner.intern(name) : null;
	}
	
	@Override
	public String toString() {
		return "<UIElement name='" + name + "'>";
	}
	
	@Override
	public boolean equals(final Object obj) {
		// based on lombok
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof UIElementAbstract)) {
			return false;
		}
		final UIElementAbstract other = (UIElementAbstract) obj;
		if (!other.canEqual(this)) {
			return false;
		}
		return name == null ? other.name == null : name.equals(other.name);
	}
	
	@Override
	public boolean canEqual(final Object other) {
		return other instanceof UIElementAbstract;
	}
	
	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}
	
	/**
	 * @param name
	 * 		the name to set
	 */
	@Override
	public void setName(final String name) {
		this.name = name != null ? StringInterner.intern(name) : null;
	}
	
	@Override
	public int hashCode() {
		// based on lombok
		return 59 + (name == null ? 43 : name.hashCode());
	}
	
	@Override
	public void invalidateHashcode() {
		hashIsDirty = true;
	}
}
