// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui;

import com.ahli.galaxy.ui.abstracts.UIElementAbstract;
import com.ahli.galaxy.ui.interfaces.UIAttribute;
import com.ahli.galaxy.ui.interfaces.UIController;
import com.ahli.galaxy.ui.interfaces.UIElement;
import com.ahli.memory.StringInterner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Ahli
 */
public class UIControllerMutable extends UIElementAbstract implements UIController {
	private final List<String> attributesKeyValueList;
	private final List<UIAttribute> keys;
	private boolean nextAdditionShouldOverride;
	private boolean nameIsImplicit = true;
	
	/**
	 * Constructor for Kryo
	 */
	private UIControllerMutable() {
		super(null);
		attributesKeyValueList = new ArrayList<>(0);
		keys = new ArrayList<>(0);
	}
	
	/**
	 * @param name
	 */
	public UIControllerMutable(final String name) {
		super(name);
		attributesKeyValueList = new ArrayList<>(0);
		keys = new ArrayList<>(0);
	}
	
	/**
	 * @param name
	 * @param attributesCapacity
	 * @param keysCapacity
	 */
	public UIControllerMutable(final String name, final int attributesCapacity, final int keysCapacity) {
		super(name);
		attributesKeyValueList = new ArrayList<>(attributesCapacity);
		keys = new ArrayList<>(keysCapacity);
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object deepCopy() {
		final UIControllerMutable clone =
				new UIControllerMutable(getName(), attributesKeyValueList.size(), keys.size());
		for (int i = 0, len = keys.size(); i < len; ++i) {
			clone.keys.add((UIAttribute) keys.get(i).deepCopy());
		}
		for (int i = 0, len = attributesKeyValueList.size(); i < len; ++i) {
			clone.attributesKeyValueList.add(attributesKeyValueList.get(i));
		}
		clone.nextAdditionShouldOverride = nextAdditionShouldOverride;
		clone.nameIsImplicit = nameIsImplicit;
		return clone;
	}
	
	/**
	 * @return the keys
	 */
	@Override
	public List<UIAttribute> getKeys() {
		return keys;
	}
	
//	/**
//	 * @param keys
//	 * 		the keys to set
//	 */
//	@Override
//	public void setKeys(final List<UIAttribute> keys) {
//		this.keys = keys;
//	}
	
//	/**
//	 * @return the nextAdditionShouldOverride
//	 */
//	@Override
//	public boolean isNextAdditionShouldOverride() {
//		return nextAdditionShouldOverride;
//	}
	
//	/**
//	 * @param nextAdditionShouldOverride
//	 * 		the nextAdditionShouldOverride to set
//	 */
//	@Override
//	public void setNextAdditionShouldOverride(final boolean nextAdditionShouldOverride) {
//		this.nextAdditionShouldOverride = nextAdditionShouldOverride;
//	}
	
	/**
	 * Adds a value for the key and returns any overridden value.
	 *
	 * @param key
	 * @param value
	 */
	@Override
	public void addValue(final String key, final String value) {
		int i = 0;
		final int len = attributesKeyValueList.size();
		for (; i < len; i += 2) {
			if (attributesKeyValueList.get(i).equals(key)) {
				break;
			}
		}
		if (i >= len) {
			// not found
			attributesKeyValueList.add(StringInterner.intern(key));
			attributesKeyValueList.add(StringInterner.intern(value));
		} else {
			attributesKeyValueList.set(i, StringInterner.intern(value));
		}
	}
	
	/**
	 * @param key
	 * @return
	 */
	@Override
	public String getValue(final String key) {
		int i = 0;
		for (final int len = attributesKeyValueList.size(); i < len; i += 2) {
			if (attributesKeyValueList.get(i).equals(key)) {
				return attributesKeyValueList.get(i + 1);
			}
		}
		return null;
	}
	
//	/**
//	 * @return the nameIsImplicit
//	 */
//	@Override
//	public boolean isNameIsImplicit() {
//		return nameIsImplicit;
//	}
	
	/**
	 * @param nameIsImplicit
	 * 		the nameIsImplicit to set
	 */
	@Override
	public void setNameIsImplicit(final boolean nameIsImplicit) {
		this.nameIsImplicit = nameIsImplicit;
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
		return "<Controller name='" + getName() + "'>";
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
	public final boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof final UIControllerMutable that)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		return that.canEqual(this) && nextAdditionShouldOverride == that.nextAdditionShouldOverride &&
				nameIsImplicit == that.nameIsImplicit &&
				Objects.equals(attributesKeyValueList, that.attributesKeyValueList) && Objects.equals(keys, that.keys);
	}
	
	@Override
	public boolean canEqual(final Object other) {
		return (other instanceof UIControllerMutable);
	}
	
	@Override
	public final int hashCode() {
		//noinspection ObjectInstantiationInEqualsHashCode
		return Objects.hash(super.hashCode(), attributesKeyValueList, keys, nextAdditionShouldOverride, nameIsImplicit);
	}
}
