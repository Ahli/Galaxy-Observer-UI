package com.ahli.galaxy.parser;

import com.ahli.galaxy.parser.interfaces.ParsedXmlConsumer;
import com.ahli.galaxy.parser.interfaces.XmlParser;
import com.ahli.galaxy.ui.UIAnchorSide;
import com.ahli.galaxy.ui.UIAnimation;
import com.ahli.galaxy.ui.UIAttribute;
import com.ahli.galaxy.ui.UIConstant;
import com.ahli.galaxy.ui.UIController;
import com.ahli.galaxy.ui.UIFrame;
import com.ahli.galaxy.ui.UIState;
import com.ahli.galaxy.ui.UIStateGroup;
import com.ahli.galaxy.ui.UITemplate;
import com.ahli.galaxy.ui.abstracts.UIElement;
import com.ahli.galaxy.ui.exception.UIException;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UICatalogParser implements ParsedXmlConsumer {
	private static final String ACTION = "action";
	private static final String KEY = "key";
	private static final String FILE = "file";
	private static final String NAME = "name";
	private static final String TEMPLATE = "template";
	private static final String FRAME = "frame";
	private static final String ANCHOR = "anchor";
	private static final String STATE = "state";
	private static final String CONTROLLER = "controller";
	private static final String ANIMATION = "animation";
	private static final String STATEGROUP = "stategroup";
	private static final String CONSTANT = "constant";
	private static final String DESC = "desc";
	private static final String DESCFLAGS = "descflags";
	private static final String INCLUDE = "include";
	private static final String PATH = "path";
	private static final String REQUIREDTOLOAD = "requiredtoload";
	private static final String VAL = "val";
	private static final String EVENT = "event";
	private static final String DRIVER = "driver";
	private static final String WHEN = "when";
	private static final String DEFAULTSTATE = "defaultstate";
	private static final String SIDE = "side";
	private static final String RELATIVE = "relative";
	private static final String POS = "pos";
	private static final String OFFSET = "offset";
	private static final String LEFT = "left";
	private static final String BOTTOM = "bottom";
	private static final String RIGHT = "right";
	private static final String TOP = "top";
	private static final String TYPE = "type";
	private static final Logger logger = LogManager.getLogger(UICatalogParser.class);
	private final UICatalog catalog;
	private final XmlParser parser;
	private final List<UIElement> curPath = new ArrayList<>();
	private final List<UIState> statesToClose;
	private final List<Integer> statesToCloseLevel;
	private UIElement curElement;
	private int curLevel;
	private boolean curIsDevLayout;
	private String raceId;
	private String consoleSkinId;
	
	// private UITemplate curTemplate;
	// private boolean editingMode;
	private String curFileName;
	
	public UICatalogParser(final UICatalog catalog, final XmlParser parser) {
		this.catalog = catalog;
		this.parser = parser;
		statesToClose = new ArrayList<>();
		statesToCloseLevel = new ArrayList<>();
		parser.setConsumer(this);
		
	}
	
	public void parseFile(final File f, final String raceId, final boolean isDevLayout, final String consoleSkinId)
			throws IOException {
		this.raceId = raceId;
		this.consoleSkinId = consoleSkinId;
		curIsDevLayout = isDevLayout;
		curFileName = f.getName();
		final int dotIndex = curFileName.lastIndexOf('.');
		if (dotIndex > 0) {
			curFileName = curFileName.substring(0, dotIndex);
		}
		parser.parseFile(f);
	}
	
	@Override
	public void parse(final int level, final String tagName, final List<String> attrTypes,
			final List<String> attrValues) throws UIException {
		logger.trace("level={}, tag={}", () -> level, () -> tagName);
		if (tagName == null) {
			logger.error("ERROR: tag in XML is null.");
			return;
		}
		
		// move curElement to parent position of new frame
		if (level <= 2) {
			// root
			curPath.clear();
			curLevel = level;
			curElement = null;
			// default editing mode unless the parsed aspect defines another one
			// editingMode = false;
			if (logger.isTraceEnabled()) {
				logger.trace("resetting path to root");
			}
		} else {
			while (level <= curLevel) {
				curLevel--;
				// curLevel - 2 because root is level 2 on list index 0
				curElement = curPath.get(curLevel - 2);
				if (logger.isTraceEnabled()) {
					logger.trace("shrinking path: curElement=" + curElement + ", level=" + curLevel);
					logger.trace("path  pre-dropLast: " + curPath);
				}
				curPath.remove(curPath.size() - 1);
				
				if (logger.isTraceEnabled()) {
					logger.trace("path afterDropLast: " + curPath);
				}
			}
		}
		
		// close action of states to enable overriding of whens/actions on next edit
		int i;
		{
			int stateLevel;
			for (i = statesToClose.size() - 1; i >= 0; i--) {
				stateLevel = statesToCloseLevel.get(i);
				if (stateLevel >= level) {
					final UIState state = statesToClose.get(i);
					state.setNextAdditionShouldOverrideActions(true);
					state.setNextAdditionShouldOverrideWhens(true);
					statesToClose.remove(i);
					statesToCloseLevel.remove(i);
					i--;
				}
			}
		}
		
		// file in attributes or template (filtering out key and action tags as they can
		// contain file=, e.g. for cutscene frames)
		if ((i = attrTypes.indexOf(FILE)) != -1 && !KEY.equals(tagName) && !ACTION.equals(tagName)) {
			if (level == 2) {
				// editingMode = true;
				// TODO edit existing template
				// curTemplate = getUITemplateOfPath
			} else {
				logger.error("unexpected attribute 'file=' found in " + curElement);
			}
		}
		final String name = ((i = attrTypes.indexOf(NAME)) != -1) ?
				catalog.getConstantValue(attrValues.get(i), raceId, curIsDevLayout, consoleSkinId) : null;
		// TODO editingMode retrieving existing element if possible
		
		// TODO copy template settings, if template used in existing frame
		
		// create from template (actions may use template= and need to be ignored)
		UIElement newElem = ((i = attrTypes.indexOf(TEMPLATE)) != -1 && !ACTION.equals(tagName)) ?
				instanciateTemplate(attrValues.get(i), name) : null;
		
		// use lowercase for cases!
		switch (tagName) {
			case FRAME:
				if (newElem == null) {
					newElem = new UIFrame(name);
				}
				final String type = ((i = attrTypes.indexOf(TYPE)) != -1) ?
						catalog.getConstantValue(attrValues.get(i), raceId, curIsDevLayout, consoleSkinId) : null;
				//				if (!checkFrameTypeCompatibility(type, ((UIFrame) newElem).getType())) {
				//					logger.error("ERROR: The type of the frame is not compatible with the used template.");
				//				}
				((UIFrame) newElem).setType(type);
				// add to parent
				if (curElement != null) {
					if (curElement instanceof UIFrame) {
						((UIFrame) curElement).getChildren().add(newElem);
					} else {
						logger.error("Frame appearing in unexpected parent element: " + curElement);
					}
				}
				break;
			case ANCHOR:
				parseAnchor(attrTypes, attrValues);
				return;
			case STATE:
				if (newElem == null) {
					newElem = new UIState(name);
				}
				if (level == 2) {
					catalog.addTemplate(curFileName, newElem, curIsDevLayout);
				} else {
					// add to parent
					if (curElement instanceof UIStateGroup) {
						((UIStateGroup) curElement).getStates().add((UIState) newElem);
						
						// set flags to override on edit after parsing children
						statesToClose.add((UIState) newElem);
						statesToCloseLevel.add(level);
					} else {
						logger.error("State appearing outside a stategroup.");
					}
				}
				break;
			case CONTROLLER:
				newElem = new UIController(name);
				// add to parent
				if (curElement != null) {
					if (curElement instanceof UIAnimation) {
						((UIAnimation) curElement).getControllers().add((UIController) newElem);
						
						for (int j = 0, len = attrValues.size(); j < len; j++) {
							((UIController) newElem).addValue(attrTypes.get(j), attrValues.get(j));
						}
						
						if (name == null) {
							setImplicitControllerNames((UIAnimation) curElement);
						}
					} else {
						logger.error("Controller appearing in unexpected parent element: " + curElement);
					}
				}
				break;
			case ANIMATION:
				newElem = new UIAnimation(name);
				// add to parent
				if (curElement != null) {
					if (curElement instanceof UIFrame) {
						((UIFrame) curElement).getChildren().add(newElem);
					} else {
						logger.error("Animation appearing in unexpected parent element: " + curElement);
					}
				}
				break;
			case STATEGROUP:
				newElem = new UIStateGroup(name);
				// add to parent
				if (curElement != null) {
					if (curElement instanceof UIFrame) {
						((UIFrame) curElement).getChildren().add(newElem);
					} else {
						logger.error("StateGroup appearing in unexpected parent element: " + curElement);
					}
				}
				break;
			case CONSTANT:
				if (newElem == null) {
					newElem = new UIConstant(name);
				}
				final String val = ((i = attrTypes.indexOf(VAL)) != -1) ?
						catalog.getConstantValue(attrValues.get(i), raceId, curIsDevLayout, consoleSkinId) : null;
				if (val == null) {
					logger.error("Value of Constant '" + name + "' is null");
					return;
				}
				((UIConstant) newElem).setValue(val);
				catalog.addConstant((UIConstant) newElem, curIsDevLayout);
				return;
			case DESC:
				// nothing to do
				return;
			case DESCFLAGS:
				// locked or internal
				return;
			case INCLUDE:
				final int j = attrTypes.indexOf(PATH);
				if (j != -1) {
					final String path = attrValues.get(j);
					final boolean isDevLayout = curIsDevLayout || attrTypes.contains(REQUIREDTOLOAD);
					catalog.processInclude(path, isDevLayout, raceId, consoleSkinId);
				}
				
				break;
			default:
				// attribute or something unknown that will cause an error
				newElem = new UIAttribute(tagName);
				i = 0;
				for (final int len = attrTypes.size(); i < len; i++) {
					((UIAttribute) newElem).addValue(attrTypes.get(i),
							catalog.getConstantValue(attrValues.get(i), raceId, curIsDevLayout, consoleSkinId));
				}
				// add to parent
				if (curElement instanceof UIFrame) {
					// Frame's attributes
					((UIFrame) curElement).addAttribute((UIAttribute) newElem);
				} else if (curElement instanceof UIAnimation) {
					// Animation's events
					if (tagName.equals(EVENT)) {
						((UIAnimation) curElement).addEvent((UIAttribute) newElem);
					} else if (tagName.equals(DRIVER)) {
						((UIAnimation) curElement).setDriver((UIAttribute) newElem);
					} else {
						logger.error("found an attribute that cannot be added to UIAnimation: " + newElem.toString());
					}
				} else if (curElement instanceof UIController) {
					// Controller's keys
					if (tagName.equals(KEY)) {
						((UIController) curElement).getKeys().add((UIAttribute) newElem);
					} else {
						logger.error("found an attribute that cannot be added to UIController: " + newElem.toString());
					}
				} else if (curElement instanceof UIStateGroup) {
					if (tagName.equals(DEFAULTSTATE)) {
						final String stateVal = ((UIAttribute) newElem).getValue(VAL);
						if (stateVal != null) {
							((UIStateGroup) curElement).setDefaultState(stateVal);
						} else {
							logger.error("found <DefaultState> in <StateGroup '{}'> without val", curElement.getName());
						}
					} else {
						logger.error("found an attribute that cannot be added to UIController: " + newElem.toString());
					}
				} else if (curElement instanceof UIState) {
					if (tagName.equals(WHEN)) {
						((UIState) curElement).getWhens().add((UIAttribute) newElem);
					} else if (tagName.equals(ACTION)) {
						((UIState) curElement).getActions().add((UIAttribute) newElem);
					} else {
						logger.error("found an attribute that cannot be added to UIState: " + newElem.toString());
					}
				} else {
					logger.error("found an attribute that cannot be added to anything: " + newElem.toString());
				}
				
				newElem = null;
				break;
		}
		
		// register template
		if (level == 2) {
			if (newElem != null) {
				if (logger.isTraceEnabled()) {
					logger.trace("adding new template: " + curFileName + " with " + newElem.getName());
				}
				// curTemplate =
				catalog.addTemplate(curFileName, newElem, curIsDevLayout);
			} else {
				if (logger.isTraceEnabled()) {
					logger.trace("skipped creating a template because newElem was null. curFileName: " + curFileName);
				}
			}
		}
		
		// enter new Element for next parse calls
		if (newElem != null) {
			curPath.add(newElem);
			curElement = newElem;
			curLevel = level;
		}
		
		// TODO state whens/actions overriding
		
	}
	
	/**
	 * @param path
	 * @param newName
	 * @return Template instance
	 */
	private UIElement instanciateTemplate(String path, final String newName) {
		if (path == null) {
			return null;
		}
		
		if (logger.isTraceEnabled()) {
			logger.trace("Instanciating Template of path " + path);
		}
		path = path.replace('\\', '/');
		final int seperatorIndex = path.indexOf('/');
		if (seperatorIndex < 0) {
			logger.error("ERROR: Template paths must follow the pattern 'FileName/FrameName'. Found '" + path + "' " +
					"instead.");
			return null;
		}
		final String fileName = path.substring(0, seperatorIndex);
		
		// 1. check templates
		UIElement templateInstance = instanciateTemplateFromList(catalog.getTemplates(), fileName, path, newName);
		if (templateInstance != null) {
			return templateInstance;
		} else {
			// 2. if fail -> check dev templates
			templateInstance = instanciateTemplateFromList(catalog.getBlizzOnlyTemplates(), fileName, path, newName);
			if (templateInstance != null) {
				if (!curIsDevLayout) {
					logger.error(
							"ERROR: the non-Blizz-only frame '" + curElement + "' uses a Blizz-only template '" + path +
									"'.");
				}
				return templateInstance;
			}
		}
		// template does not exist or its layout was not loaded, yet
		if (!curIsDevLayout) {
			logger.error("ERROR: Template of path '" + path + "' could not be found.");
		} else {
			logger.warn("WARNING: Template of path '" + path +
					"' could not be found, but we are creating a Blizz-only layout, so this is fine.");
		}
		return null;
	}
	
	/**
	 * @param attrTypes
	 * @param attrValues
	 */
	private void parseAnchor(final List<String> attrTypes, final List<String> attrValues) {
		int i;
		final String side = ((i = attrTypes.indexOf(SIDE)) != -1) ?
				catalog.getConstantValue(attrValues.get(i), raceId, curIsDevLayout, consoleSkinId) : null;
		final String relative = ((i = attrTypes.indexOf(RELATIVE)) != -1) ?
				catalog.getConstantValue(attrValues.get(i), raceId, curIsDevLayout, consoleSkinId) : null;
		final String pos = ((i = attrTypes.indexOf(POS)) != -1) ?
				catalog.getConstantValue(attrValues.get(i), raceId, curIsDevLayout, consoleSkinId) : null;
		final String offset = ((i = attrTypes.indexOf(OFFSET)) != -1) ?
				catalog.getConstantValue(attrValues.get(i), raceId, curIsDevLayout, consoleSkinId) : null;
		
		if (curElement instanceof UIFrame) {
			final UIFrame frame = (UIFrame) curElement;
			if (side == null) {
				if (logger.isTraceEnabled()) {
					logger.trace("relative=" + relative + ", offset=" + offset);
				}
				frame.setAnchor(relative, offset);
			} else {
				UIAnchorSide sideVal = null;
				if (side.compareToIgnoreCase(LEFT) == 0) {
					sideVal = UIAnchorSide.LEFT;
				} else if (side.compareToIgnoreCase(BOTTOM) == 0) {
					sideVal = UIAnchorSide.BOTTOM;
				} else if (side.compareToIgnoreCase(RIGHT) == 0) {
					sideVal = UIAnchorSide.RIGHT;
				} else if (side.compareToIgnoreCase(TOP) == 0) {
					sideVal = UIAnchorSide.TOP;
				} else {
					logger.error("'Anchor' attribute has unrecognizable value for 'side='. Value is '" + side + "'.");
				}
				if (sideVal != null) {
					frame.setAnchorOffset(sideVal, offset);
					frame.setAnchorPos(sideVal, pos);
					frame.setAnchorRelative(sideVal, relative);
				}
			}
		}
	}
	
	/**
	 * Set the implicit names of controllers in animations.
	 *
	 * @param thisElem
	 */
	private void setImplicitControllerNames(final UIAnimation thisElem) {
		if (logger.isTraceEnabled()) {
			logger.trace("Setting implicit controller names for UIAnimation " + thisElem.getName());
		}
		final List<UIController> controllers = thisElem.getControllers();
		for (final UIController contr : controllers) {
			if (contr.getName() == null) {
				final String type = contr.getValue(TYPE);
				logger.trace("type = {}", () -> type);
				contr.setName(getImplicitName(type, controllers));
				contr.setNameIsImplicit(true);
			}
		}
	}
	
	/**
	 * @param templates
	 * @param fileName
	 * @param path
	 * @param newName
	 * @return
	 */
	private UIElement instanciateTemplateFromList(final List<UITemplate> templates, final String fileName,
			final String path, final String newName) {
		final String newPath = UIElement.removeLeftPathLevel(path);
		
		for (final UITemplate curTemplate : templates) {
			if (curTemplate.getFileName().equalsIgnoreCase(fileName)) {
				// found template file
				final UIElement frameFromPath = curTemplate.receiveFrameFromPath(newPath);
				
				if (frameFromPath == null) {
					// not the correct template
					continue;
				}
				final UIElement clone = (UIElement) frameFromPath.deepCopy();
				clone.setName(newName);
				return clone;
			}
		}
		return null;
	}
	
	/**
	 * @param type
	 * @param controllers
	 * @return
	 */
	private String getImplicitName(final String type, final List<UIController> controllers) {
		if (logger.isTraceEnabled()) {
			logger.trace("Constructing implicit controller name");
		}
		if (type == null) {
			logger.error("'type=\"...\"' of Controller is not set or invalid.");
			return "";
		}
		
		int i = 0;
		while (true) {
			final String name = type + "_" + i;
			
			if (controllers.stream()
					.noneMatch(t -> t.getName() != null && t.getName().compareToIgnoreCase(name) == 0)) {
				logger.trace("Constructing implicit controller name: {}", () -> name);
				return name;
			}
			logger.trace("Implicit controller name existing: {}", () -> name);
			i++;
		}
	}
	
	/**
	 * @param type
	 * @param type2
	 * @return
	 */
	private boolean checkFrameTypeCompatibility(final String type, final String type2) {
		// TODO
		return true;
	}
	
	@Override
	public void endLayoutFile() {
		// close all states
		for (final UIState s : statesToClose) {
			s.setNextAdditionShouldOverrideActions(true);
			s.setNextAdditionShouldOverrideWhens(true);
		}
		statesToClose.clear();
		statesToCloseLevel.clear();
	}
}
