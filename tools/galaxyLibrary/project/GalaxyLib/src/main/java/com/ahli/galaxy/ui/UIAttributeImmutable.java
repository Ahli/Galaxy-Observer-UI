// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui;

import com.ahli.galaxy.ui.abstracts.UIElementAbstract;
import com.ahli.galaxy.ui.interfaces.UIAttribute;
import com.ahli.galaxy.ui.interfaces.UIElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Basic Attribute implementation to describe default UI's attribute.
 *
 * @author Ahli
 */
public class UIAttributeImmutable extends UIElementAbstract implements UIAttribute {
	private final List<String> keyValueList;
	
	/**
	 * Constructor for Kryo?
	 */
	private UIAttributeImmutable() {
		super(null);
		keyValueList = new ArrayList<>(0);
	}
	
	/**
	 * Constructor
	 *
	 * @param name
	 * 		Element's name
	 * @param keyValuesList
	 * 		List where Key and Value entries alternate: Key1, Value1, Key2, Value2, ...
	 */
	public UIAttributeImmutable(final String name, final List<String> keyValuesList) {
		super(name);
		keyValueList = keyValuesList;
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object deepCopy() {
		// UIAttribute is immutable
		return this;
	}
	
	@Override
	public void setName(final String name) {
		throw new UnsupportedOperationException("UIAttributes are immutable");
	}
	
	//	/**
	//	 * Adds a value for the key and returns any overridden value.
	//	 *
	//	 * @param key
	//	 * @param value
	//	 */
	//	public void addValue(final String key, final String value) {
	//		int i = 0;
	//		final int len = keyValueList.size();
	//		for (; i < len; i += 2) {
	//			if (keyValueList.get(i).equals(key)) {
	//				break;
	//			}
	//		}
	//		if (i >= len) {
	//			// not found
	//			keyValueList.add(StringInterner.intern(key));
	//			keyValueList.add(StringInterner.intern(value));
	//		} else {
	//			keyValueList.set(i, StringInterner.intern(value));
	//		}
	//	}
	
	/**
	 * @param key
	 * @return
	 */
	@Override
	public String getValue(final String key) {
		int i = 0;
		for (final int len = keyValueList.size(); i < len; i += 2) {
			if (keyValueList.get(i).equals(key)) {
				return keyValueList.get(i + 1);
			}
		}
		return null;
	}
	
	/**
	 * Returns a List of Key-Value pairs. Every key is followed by an entry for its value.
	 *
	 * @return
	 */
	@Override
	public List<String> getKeyValues() {
		return keyValueList;
	}
	
	//	/**
	//	 * Clears all existing key-value-pairs and adds all entries from the list.
	//	 *
	//	 * @param keyValues
	//	 */
	//	public void setKeyValues(final List<String> keyValues) {
	//		keyValueList.clear();
	//		keyValueList.addAll(keyValues);
	//	}
	
	@Override
	public UIElement receiveFrameFromPath(final String path) {
		return (path == null || path.isEmpty()) ? this : null;
	}
	
	@Override
	public String toString() {
		return "<" + getName() + ">";
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
		if (!(obj instanceof UIAttributeImmutable)) {
			return false;
		}
		if (!super.equals(obj)) {
			return false;
		}
		final UIAttributeImmutable that = (UIAttributeImmutable) obj;
		return that.canEqual(this) && Objects.equals(keyValueList, that.keyValueList);
	}
	
	@Override
	public boolean canEqual(final Object other) {
		return (other instanceof UIAttributeImmutable);
	}
	
	@Override
	public final int hashCode() {
		//noinspection ObjectInstantiationInEqualsHashCode
		int h = hash;
		if (hashIsDirty || (h == 0 && !hashIsZero)) {
			h = calcHashCode();
			if (h == 0) {
				hashIsZero = true;
			} else {
				hash = h;
			}
			hashIsDirty = false;
		}
		return h;
	}
	
	private int calcHashCode() {
		return Objects.hash(super.hashCode(), keyValueList);
	}
}
