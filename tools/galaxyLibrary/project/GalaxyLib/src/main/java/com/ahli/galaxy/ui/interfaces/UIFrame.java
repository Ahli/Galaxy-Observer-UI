// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.ui.interfaces;

import com.ahli.galaxy.ui.UIAnchorSide;

import java.util.List;

public interface UIFrame extends UIElement {
	@Override
	Object deepCopy();
	
	String getType();
	
	void setType(String type);
	
	@Override
	List<UIElement> getChildren();
	
	@Override
	List<UIElement> getChildrenRaw();
	
	void addAttribute(UIAttribute value);
	
//	UIAttribute getValue(String key);
	
	List<UIAttribute> getAttributes();
	
	List<UIAttribute> getAttributesRaw();
	
	String getAnchorRelative(UIAnchorSide side);
	
	String getAnchorOffset(UIAnchorSide side);
	
	String getAnchorPos(UIAnchorSide side);
	
	void setAnchor(String relative, String offset);
	
	void setAnchor(UIAnchorSide side, String relative, String pos, String offset);
	
	void setAnchorRelative(UIAnchorSide side, String relative);
	
	void setAnchorPos(UIAnchorSide side, String pos);
	
	void setAnchorOffset(UIAnchorSide side, String offset);
	
	@Override
	UIElement receiveFrameFromPath(String path);
	
	@Override
	String toString();
	
	@Override
	boolean equals(Object obj);
	
//	@Override
//	boolean canEqual(Object other);
	
	@Override
	int hashCode();
}
