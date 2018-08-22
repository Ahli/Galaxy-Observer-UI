package com.ahli.galaxy.ui;

import com.ahli.galaxy.ui.abstracts.UIElement;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Basic Attribute implementation to describe default UI's attribute.
 *
 * @author Ahli
 */
@JsonTypeInfo (use = JsonTypeInfo.Id.MINIMAL_CLASS)
@JsonInclude (JsonInclude.Include.NON_EMPTY)
public class UIAttribute extends UIElement {
	
	private final List<String> keyValueList;
	
	public UIAttribute() {
		super(null);
		keyValueList = new ArrayList<>(0);
	}
	
	/**
	 * Constructor.
	 *
	 * @param name
	 * 		Element's name
	 */
	public UIAttribute(final String name) {
		super(name);
		keyValueList = new ArrayList<>(0);
	}
	
	/**
	 * Constructor.
	 *
	 * @param name
	 * 		Element's name
	 */
	public UIAttribute(final String name, final int initialValuesMaxCapacity) {
		super(name);
		keyValueList = new ArrayList<>(initialValuesMaxCapacity);
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object deepCopy() {
		final UIAttribute clone = new UIAttribute(getName(), keyValueList.size());
		for (int i = 0, len = keyValueList.size(); i < len; i++) {
			clone.keyValueList.add(keyValueList.get(i));
		}
		return clone;
	}
	
	/**
	 * Adds a value for the key and returns any overridden value.
	 *
	 * @param key
	 * @param value
	 */
	public String addValue(final String key, final String value) {
		int i = 0;
		final int len = keyValueList.size();
		for (; i < len; i += 2) {
			if (keyValueList.get(i).equals(key)) {
				break;
			}
		}
		if (i >= len) {
			// not found
			keyValueList.add(key);
			keyValueList.add(value);
			return null;
		} else {
			return keyValueList.set(i, value);
		}
	}
	
	/**
	 * @param key
	 * @return
	 */
	public String getValue(final String key) {
		int i = 0;
		for (final int len = keyValueList.size(); i < len; i += 2) {
			if (keyValueList.get(i).equals(key)) {
				return keyValueList.get(i + 1);
			}
		}
		return null;
	}
	
	public List<String> getKeyValues() {
		return keyValueList;
	}
	
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
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof UIAttribute)) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		final Object[] signatureFields = getSignatureFields();
		final Object[] thatSignatureFields = ((UIAttribute) obj).getSignatureFields();
		for (int i = 0; i < signatureFields.length; i++) {
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
		return new Object[] { getName(), keyValueList };
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getSignatureFields());
	}
}
