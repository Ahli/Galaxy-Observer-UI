// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui;

import com.ahli.galaxy.ui.abstracts.UIElement;
import com.ahli.util.StringInterner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Ahli
 */
public class UIController extends UIElement {
	private final List<String> attributesKeyValueList;
	private List<UIAttribute> keys;
	private boolean nextAdditionShouldOverride;
	private boolean nameIsImplicit = true;
	
	public UIController() {
		super(null);
		attributesKeyValueList = new ArrayList<>(0);
		keys = new ArrayList<>(0);
	}
	
	/**
	 * @param name
	 */
	public UIController(final String name) {
		super(name);
		attributesKeyValueList = new ArrayList<>(0);
		keys = new ArrayList<>(0);
	}
	
	/**
	 * @param name
	 */
	public UIController(final String name, final int initialValuesMaxCapacity, final int initialKeysMaxCapacity) {
		super(name);
		attributesKeyValueList = new ArrayList<>(initialValuesMaxCapacity);
		keys = new ArrayList<>(initialKeysMaxCapacity);
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object deepCopy() {
		final UIController clone = new UIController(getName(), attributesKeyValueList.size(), keys.size());
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
	public List<UIAttribute> getKeys() {
		return keys;
	}
	
	/**
	 * @param keys
	 * 		the keys to set
	 */
	public void setKeys(final List<UIAttribute> keys) {
		this.keys = keys;
	}
	
	/**
	 * @return the nextAdditionShouldOverride
	 */
	public boolean isNextAdditionShouldOverride() {
		return nextAdditionShouldOverride;
	}
	
	/**
	 * @param nextAdditionShouldOverride
	 * 		the nextAdditionShouldOverride to set
	 */
	public void setNextAdditionShouldOverride(final boolean nextAdditionShouldOverride) {
		this.nextAdditionShouldOverride = nextAdditionShouldOverride;
	}
	
	/**
	 * Adds a value for the key and returns any overridden value.
	 *
	 * @param key
	 * @param value
	 */
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
	public String getValue(final String key) {
		int i = 0;
		for (final int len = attributesKeyValueList.size(); i < len; i += 2) {
			if (attributesKeyValueList.get(i).equals(key)) {
				return attributesKeyValueList.get(i + 1);
			}
		}
		return null;
	}
	
	/**
	 * @return the nameIsImplicit
	 */
	public boolean isNameIsImplicit() {
		return nameIsImplicit;
	}
	
	/**
	 * @param nameIsImplicit
	 * 		the nameIsImplicit to set
	 */
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
	public List<UIElement> getChildrenRaw() {
		return null; // returning null is desired here
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		final Object[] signatureFields = getSignatureFields();
		final Object[] thatSignatureFields = ((UIController) obj).getSignatureFields();
		for (int i = 0; i < signatureFields.length; ++i) {
			if (!(signatureFields[i] instanceof Object[])) {
				if (!Objects.equals(signatureFields[i], thatSignatureFields[i])) {
					return false;
				}
			} else {
				if (!Arrays.deepEquals((Object[]) signatureFields[i], (Object[]) thatSignatureFields[i])) {
					return false;
				}
			}
		}
		return true;
	}
	
	private Object[] getSignatureFields() {
		return new Object[] { getName(), keys, nextAdditionShouldOverride, nameIsImplicit, attributesKeyValueList };
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getSignatureFields());
	}
}
