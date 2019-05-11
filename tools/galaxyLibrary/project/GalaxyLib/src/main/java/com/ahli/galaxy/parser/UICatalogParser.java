// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

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
import gnu.trove.set.hash.THashSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	private final Map<UIElement, UIElement> addedFinalElements;
	private final Set<UIElement> deduplicatedElements;
	private final boolean paramDeduplicate;
	private int attributeDeduplications;
	private int constantDeduplications;
	private List<UIElement> addedElements;
	private UIElement curElement;
	private int curLevel;
	private boolean curIsDevLayout;
	private String raceId;
	private String consoleSkinId;
	private UITemplate curTemplate;
	private boolean editingMode;
	private String curFileName;
	private Deque<Object> toDeduplicate;
	private int postProcessDeduplications;
	
	public UICatalogParser(final UICatalog catalog, final XmlParser parser, final boolean deduplicate) {
		this.catalog = catalog;
		this.parser = parser;
		statesToClose = new ArrayList<>();
		statesToCloseLevel = new ArrayList<>();
		addedFinalElements = new HashMap<>();
		paramDeduplicate = deduplicate;
		addedElements = deduplicate ? new ArrayList<>(35000) : null;
		deduplicatedElements = deduplicate ? new THashSet<>(13000) : null;
		parser.setConsumer(this);
	}
	
	@Override
	public void parseFile(final Path p, final String raceId, final boolean isDevLayout, final String consoleSkinId)
			throws IOException {
		this.raceId = raceId;
		this.consoleSkinId = consoleSkinId;
		curIsDevLayout = isDevLayout;
		curFileName = p.getFileName().toString();
		final int dotIndex = curFileName.lastIndexOf('.');
		if (dotIndex > 0) {
			curFileName = curFileName.substring(0, dotIndex);
		}
		parser.parseFile(p);
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
			editingMode = false;
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
			if (level != 2) {
				logger.warn("WARNING: Unexpected attribute 'file=' found in " + curElement);
			}
			// TODO enable to test modification of existing templates, feature is incomplete
			//			editingMode = true;
			curTemplate = catalog.getTemplateOfPath(attrValues.get(i));
		}
		String name = ((i = attrTypes.indexOf(NAME)) != -1) ?
				catalog.getConstantValue(attrValues.get(i), raceId, curIsDevLayout, consoleSkinId) : null;
		
		UIElement newElem = null;
		
		// open existing element, if editing mode is enabled
		if (editingMode && curTemplate != null) {
			if (name == null) {
				logger.error("Template is used without defining a name.");
				name = "UnnamedFrame" + Math.random();
			}
			
			// newElement needs to be the current element
			newElem = curTemplate.getElement().receiveFrameFromPath(name);
			// curElement needs to be the parent of that frame
			final int j = name.lastIndexOf('/');
			if (j > 0) {
				final String parentName = name.substring(0, j);
				curElement = curTemplate.getElement().receiveFrameFromPath(parentName);
			} else {
				curElement = curTemplate.getElement();
			}
		}
		
		// handle template attribute
		i = attrTypes.indexOf(TEMPLATE);
		//		if(editingMode && newElem != null){
		// TODO copy template settings, if template used in existing frame & editing mode
		// copy template into newElem
		// }
		
		// create from template (actions may use template= and need to be ignored)
		if (i != -1 && newElem == null) {
			newElem = (!ACTION.equals(tagName)) ? instanciateTemplate(attrValues.get(i), name) : null;
			if (newElem != null) {
				registerInstance(newElem);
			}
		}
		
		// use lowercase for cases!
		switch (tagName) {
			case FRAME:
				if (newElem == null) {
					newElem = new UIFrame(name);
					registerInstance(newElem);
				}
				String type = ((i = attrTypes.indexOf(TYPE)) != -1) ?
						catalog.getConstantValue(attrValues.get(i), raceId, curIsDevLayout, consoleSkinId) : null;
				if (type == null) {
					logger.error("Unknown type defined in child element of: " + curElement);
					type = "Frame";
				}
				if (!checkFrameTypeCompatibility(type, ((UIFrame) newElem).getType())) {
					logger.warn("WARN: The type of the frame is not compatible with the used template.");
				}
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
					registerInstance(newElem);
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
				registerInstance(newElem);
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
				registerInstance(newElem);
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
				registerInstance(newElem);
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
					logger.error("Constant '" + name + "' has no value defined");
					return;
				}
				((UIConstant) newElem).setValue(val);
				if (paramDeduplicate) {
					final UIElement refToDuplicate = addedFinalElements.get(newElem);
					if (refToDuplicate != null) {
						newElem = refToDuplicate;
						deduplicatedElements.add(refToDuplicate);
						constantDeduplications++;
					} else {
						addedFinalElements.put(newElem, newElem);
					}
				}
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
				if (paramDeduplicate) {
					final UIElement refToDuplicate = addedFinalElements.get(newElem);
					if (refToDuplicate != null) {
						newElem = refToDuplicate;
						deduplicatedElements.add(refToDuplicate);
						attributeDeduplications++;
					} else {
						addedFinalElements.put(newElem, newElem);
					}
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
	
	private void registerInstance(final UIElement newElem) {
		if (paramDeduplicate) {
			addedElements.add(newElem);
		}
	}
	
	/**
	 * @param typeTemplate
	 * @param typeFrame
	 * @return
	 */
	private static boolean checkFrameTypeCompatibility(final String typeTemplate, final String typeFrame) {
		// TODO
		return true;
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
				if (relative == null) {
					logger.error("'Anchor' attribute has no 'relative' attribute defined in parent element: " +
							curElement.getName());
				} else {
					try {
						frame.setAnchor(relative, offset);
					} catch (final NumberFormatException e) {
						logger.error("A frame's offset '" + offset + "' is not a numerical value. Using 0 instead.");
						frame.setAnchorOffset(UIAnchorSide.RIGHT, UIFrame.ZERO);
						frame.setAnchorOffset(UIAnchorSide.BOTTOM, UIFrame.ZERO);
					}
				}
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
					logger.error("'Anchor' attribute has unrecognizable value for 'side='. Value is '" + side +
							"' in parent element: " + curElement.getName());
				}
				if (sideVal != null) {
					if (offset == null) {
						logger.error("'Anchor' attribute has no 'offset' attribute defined in parent element: " +
								curElement.getName());
					} else {
						if (pos == null) {
							logger.error("'Anchor' attribute has no 'pos' attribute defined in parent element: " +
									curElement.getName());
						} else {
							if (relative == null) {
								logger.error(
										"'Anchor' attribute has no 'relative' attribute defined in parent element: " +
												curElement.getName());
							} else {
								frame.setAnchor(sideVal, relative, pos, offset);
							}
						}
					}
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
		final List<UIElement> controllers = thisElem.getControllers();
		for (final UIElement uiElem : controllers) {
			final UIController contr = (UIController) uiElem;
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
				//				if (paramDeduplicate) {
				//					addToAddedElements(clone); // not necessary as the parent is deduplicated right
				//					// now and elements are not modified atm.
				//				}
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
	private static String getImplicitName(final String type, final List<UIElement> controllers) {
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
	
	private void addToAddedElements(final UIElement elem) {
		addedElements.add(elem);
		if (elem instanceof UIState) {
			final UIState state = (UIState) elem;
			addedElements.addAll(state.getActions());
			addedElements.addAll(state.getWhens());
		} else if (elem instanceof UIFrame) {
			final UIFrame frame = (UIFrame) elem;
			addedElements.addAll(frame.getAttributes());
			final List<UIElement> childrenRaw = frame.getChildrenRaw();
			if (childrenRaw != null) {
				for (final UIElement child : childrenRaw) {
					addToAddedElements(child);
				}
			}
		} else if (elem instanceof UIStateGroup) {
			final UIStateGroup stateGroup = (UIStateGroup) elem;
			addedElements.addAll(stateGroup.getStates());
		} else if (elem instanceof UIController) {
			final UIController controller = (UIController) elem;
			addedElements.addAll(controller.getKeys());
		} else if (elem instanceof UIAnimation) {
			final UIAnimation anim = (UIAnimation) elem;
			final UIAttribute driver = anim.getDriver();
			if (driver != null) {
				addedElements.add(driver);
			}
			for (final UIElement event : anim.getEvents()) {
				addToAddedElements(event);
			}
			for (final UIElement controller : anim.getControllers()) {
				addToAddedElements(controller);
			}
		}
		// Constants and Attributes do not contain any further elements
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
	
	@Override
	public void deduplicate() {
		if (paramDeduplicate) {
			// finish mapping
			for (final UIElement elem : addedElements) {
				addedFinalElements.putIfAbsent(elem, elem);
			}
			logger.info("elements added that can be deduplicated during postprocessing: " + addedElements.size());
			logger.info("unique elements added that were deduplicated during parsing: " + addedFinalElements.size());
			addedElements = null;
			toDeduplicate = new ArrayDeque<>(74000);
			// replace instancesb
			
			toDeduplicate.addAll(catalog.getTemplates());
			toDeduplicate.addAll(catalog.getBlizzOnlyTemplates());
			int maxDequeSize = 0;
			while (!toDeduplicate.isEmpty()) {
				maxDequeSize = Math.max(maxDequeSize, toDeduplicate.size());
				deduplicate(toDeduplicate.pop());
			}
			logger.info("postProcessDeduplications: " + postProcessDeduplications + ", attributeDeduplications: " +
					attributeDeduplications + ", constantsDeduplications=" + constantDeduplications + ", " +
					"maxDequeSize: " + maxDequeSize + ", totalDeduplicatedElements: " + deduplicatedElements.size());
		}
	}
	
	private void deduplicate(final Object obj) {
		if (obj instanceof UIFrame) {
			deduplicate((UIFrame) obj);
		} else if (obj instanceof UIAnimation) {
			deduplicate((UIAnimation) obj);
		} else if (obj instanceof UIStateGroup) {
			deduplicate((UIStateGroup) obj);
		} else if (obj instanceof UITemplate) {
			deduplicate((UITemplate) obj);
		} else if (!(obj instanceof UIAttribute || obj instanceof UIConstant || obj instanceof UIController ||
				obj instanceof UIState)) {
			// attributes, constants, controllers do not contain any deduplicated objects
			// attributes and constants can be deduplicated when initially created
			logger.error("Object cannot be handled in dedpuplication: " + obj.toString());
		}
	}
	
	private void deduplicate(final UIFrame frame) {
		final List<UIElement> childrenRaw = frame.getChildrenRaw();
		if (childrenRaw != null) {
			for (int i = 0, len = childrenRaw.size(); i < len; ++i) {
				final UIElement child = childrenRaw.get(i);
				final UIElement duplicate = addedFinalElements.get(child);
				if (duplicate != null && child != duplicate) {
					// replace in parent
					childrenRaw.set(i, duplicate);
					deduplicatedElements.add(duplicate);
					postProcessDeduplications++;
				} else {
					toDeduplicate.add(child);
				}
			}
		}
	}
	
	private void deduplicate(final UIAnimation anim) {
		final List<UIElement> controllers = anim.getControllers();
		for (int i = 0, len = controllers.size(); i < len; ++i) {
			final UIElement controller = controllers.get(i);
			final UIElement duplicate = addedFinalElements.get(controller);
			if (duplicate != null && controller != duplicate) {
				// replace in parent
				controllers.set(i, duplicate);
				deduplicatedElements.add(duplicate);
				postProcessDeduplications++;
			}
			// controllers cannot be deduplicated any further at this point (Attributes were already)
		}
	}
	
	private void deduplicate(final UIStateGroup stateGroup) {
		final List<UIElement> states = stateGroup.getStates();
		for (int i = 0, len = states.size(); i < len; ++i) {
			final UIElement child = states.get(i);
			final UIElement duplicate = addedFinalElements.get(child);
			if (duplicate != null && child != duplicate) {
				// replace in parent
				states.set(i, duplicate);
				deduplicatedElements.add(duplicate);
				postProcessDeduplications++;
			}
			// states cannot be deduplicated any further at this point (Attributes were already)
		}
	}
	
	private void deduplicate(final UITemplate template) {
		final UIElement elem = template.getElement();
		final UIElement duplicate = addedFinalElements.get(elem);
		if (duplicate != null && elem != duplicate) {
			// replace in parent
			template.setElement(duplicate);
			deduplicatedElements.add(duplicate);
			postProcessDeduplications++;
		} else {
			toDeduplicate.add(elem);
		}
	}
}
