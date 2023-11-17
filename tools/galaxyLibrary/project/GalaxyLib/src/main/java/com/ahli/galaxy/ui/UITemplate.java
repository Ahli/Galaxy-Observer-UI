// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui;

import com.ahli.cloning.DeepCopyable;
import com.ahli.galaxy.ui.interfaces.UIElement;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * @author Ahli
 */
public class UITemplate implements DeepCopyable {
	@Getter
	private String fileName;
	@Setter
	@Getter
	private UIElement element;
	private boolean isLocked;
	
	private UITemplate() {
		// required for Kryo
	}
	
	/**
	 * @param fileName
	 * @param element
	 */
	public UITemplate(final String fileName, final UIElement element) {
		this.fileName = fileName;
		this.element = element;
	}
	
	/**
	 * Returns a deep clone of this.
	 */
	@Override
	public Object deepCopy() {
		final UITemplate clone = new UITemplate(fileName, (UIElement) element.deepCopy());
		clone.isLocked = isLocked;
		return clone;
	}
	
	public boolean isLocked() {
		return isLocked;
	}
	
	public void setLocked(final boolean isLocked) {
		this.isLocked = isLocked;
	}
	
	public UIElement receiveFrameFromPath(final String path) {
		final String curName = UIElement.getLeftPathLevel(path);
		// curName cannot be null here
		if (curName.equalsIgnoreCase(element.getName())) {
			final String newPath = UIElement.removeLeftPathLevel(path);
			return element.receiveFrameFromPath(newPath);
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "<Template fileName='" + fileName + "' elementName='" + element.getName() + "'>";
	}
	
	@Override
	public final boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof final UITemplate that)) {
			return false;
		}
		return isLocked == that.isLocked && Objects.equals(fileName, that.fileName) &&
				Objects.equals(element, that.element);
	}
	
	@Override
	public final int hashCode() {
		//noinspection ObjectInstantiationInEqualsHashCode
		return Objects.hash(fileName, element, isLocked);
	}
}
