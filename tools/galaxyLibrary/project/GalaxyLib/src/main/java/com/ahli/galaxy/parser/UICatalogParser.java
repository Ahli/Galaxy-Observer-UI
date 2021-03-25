// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.galaxy.parser;

import com.ahli.galaxy.parser.interfaces.ParsedXmlConsumer;
import com.ahli.galaxy.parser.interfaces.XmlParser;
import com.ahli.galaxy.ui.UIAnchorSide;
import com.ahli.galaxy.ui.UIAnimationMutable;
import com.ahli.galaxy.ui.UIAttributeImmutable;
import com.ahli.galaxy.ui.UIConstantImmutable;
import com.ahli.galaxy.ui.UIControllerMutable;
import com.ahli.galaxy.ui.UIFrameMutable;
import com.ahli.galaxy.ui.UIStateGroupMutable;
import com.ahli.galaxy.ui.UIStateMutable;
import com.ahli.galaxy.ui.UITemplate;
import com.ahli.galaxy.ui.exception.UIException;
import com.ahli.galaxy.ui.interfaces.UIAnimation;
import com.ahli.galaxy.ui.interfaces.UIAttribute;
import com.ahli.galaxy.ui.interfaces.UICatalog;
import com.ahli.galaxy.ui.interfaces.UIConstant;
import com.ahli.galaxy.ui.interfaces.UIController;
import com.ahli.galaxy.ui.interfaces.UIElement;
import com.ahli.galaxy.ui.interfaces.UIFrame;
import com.ahli.galaxy.ui.interfaces.UIState;
import com.ahli.galaxy.ui.interfaces.UIStateGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
	private static final String HANDLE = "handle";
	private final UICatalog catalog;
	private final XmlParser parser;
	private final List<UIElement> curPath;
	private final List<UIState> statesToClose;
	private final List<Integer> statesToCloseLevel;
	private final List<UITemplate> newTemplatesOfCurFile;
	private final Map<UIElement, UIElement> addedFinalElements;
	private final DeduplicationIntensity deduplicationIntensity;
	private final boolean deduplicateDuringParsing;
	private final boolean deduplicatePostProcessing;
	private int attributeDeduplications;
	private int constantDeduplications;
	private UIElement curElement;
	private int curLevel;
	private boolean curIsDevLayout;
	private String raceId;
	private String consoleSkinId;
	private UITemplate curExtTemplate;
	private UITemplate curTemplate;
	//private boolean editingMode;
	private String curFileName;
	private int postProcessDeduplications;
	private boolean layoutFileDescLocked;
	private int unnamedFrameCounter;
	
	public UICatalogParser(
			final UICatalog catalog, final XmlParser parser, final DeduplicationIntensity deduplicationIntensity) {
		this.catalog = catalog;
		this.parser = parser;
		statesToClose = new ArrayList<>(10);
		statesToCloseLevel = new ArrayList<>(10);
		curPath = new ArrayList<>(10);
		newTemplatesOfCurFile = new ArrayList<>(250);
		this.deduplicationIntensity = deduplicationIntensity;
		logger.trace("deduplication intensity: {}", deduplicationIntensity);
		switch (deduplicationIntensity) {
			case NONE -> {
				deduplicateDuringParsing = false;
				deduplicatePostProcessing = false;
				addedFinalElements = null;
			}
			case SIMPLE -> {
				deduplicateDuringParsing = true;
				deduplicatePostProcessing = false;
				// parse obs interface
				addedFinalElements = new UnifiedMap<>(90_000 * 5 / 4); // TODO optimize memory
			}
			// FULL
			default -> {
				deduplicateDuringParsing = true;
				deduplicatePostProcessing = true;
				if (catalog.getTemplates().isEmpty()) {
					// parse a baseUI
					addedFinalElements = new UnifiedMap<>(61_000 * 5 / 4);
				} else {
					// parse obs interface
					addedFinalElements = new UnifiedMap<>(90_000 * 5 / 4);
				}
			}
		}
		//noinspection ThisEscapedInObjectConstruction
		parser.setConsumer(this);
	}
	
	//	/**
	//	 * @param typeTemplate
	//	 * @param typeFrame
	//	 * @return
	//	 */
	//	private static boolean checkFrameTypeCompatibility(final String typeTemplate, final String typeFrame) {
	//		// TODO frame type compatibility check
	//		return true;
	//	}
	
	/**
	 * Set the implicit names of controllers in animations.
	 *
	 * @param thisElem
	 */
	private static void setImplicitControllerNames(final UIAnimation thisElem) {
		logger.trace("Setting implicit controller names for UIAnimation {}", thisElem.getName());
		final List<UIElement> controllers = thisElem.getControllers();
		for (final UIElement uiElem : controllers) {
			final UIController contr = (UIController) uiElem;
			if (contr.getName() == null) {
				final String type = contr.getValue(TYPE);
				logger.trace("type = {}", type);
				contr.setName(getImplicitName(type, controllers));
				contr.setNameIsImplicit(true);
			}
		}
	}
	
	/**
	 * Returns the UIElement that resides in the specified UITemplates under the specified path and the specified file
	 * name.
	 *
	 * @param templates
	 * @param fileName
	 * @param path
	 * @return
	 */
	private static UIElement findTemplateFromList(
			final Iterable<UITemplate> templates, final String fileName, final String path) {
		final String newPath = UIElement.removeLeftPathLevel(path);
		
		for (final UITemplate currTemplate : templates) {
			if (currTemplate.getFileName().equalsIgnoreCase(fileName)) {
				// found a template file
				final UIElement frameFromPath = currTemplate.receiveFrameFromPath(newPath);
				
				if (frameFromPath != null) {
					return frameFromPath;
				}
				// else not the correct template
			}
		}
		return null;
	}
	
	/**
	 * @param type
	 * @param controllers
	 * @return
	 */
	@SuppressWarnings("ObjectAllocationInLoop")
	private static String getImplicitName(final String type, final List<UIElement> controllers) {
		logger.trace("Constructing implicit controller name");
		if (type == null) {
			logger.error("'type=\"...\"' of Controller is not set or invalid.");
			return "";
		}
		
		int i = 0;
		while (true) {
			final String name = type + "_" + i;
			
			if (controllers.stream()
					.noneMatch(t -> t.getName() != null && t.getName().compareToIgnoreCase(name) == 0)) {
				logger.trace("Constructing implicit controller name: {}", name);
				return name;
			}
			logger.trace("Implicit controller name existing: {}", name);
			++i;
		}
	}
	
	@SuppressWarnings("squid:S4973")
	private static void copyAttributes(
			final List<UIAttribute> attributesSource, final List<UIAttribute> attributesTarget) {
		for (final UIAttribute attrSource : attributesSource) {
			boolean noChanges = true;
			final String sourceName = attrSource.getName();
			// override attribute => remove existing one with same tag
			for (int i = 0, len = attributesTarget.size(); i < len; ++i) {
				final UIAttribute attrTarget = attributesTarget.get(i);
				final String targetName = attrTarget.getName();
				// both null or equal
				if (Objects.equals(targetName, sourceName)) {
					// UIAttributes are immutable, so the reference can be updated
					attributesTarget.set(i, attrSource);
					noChanges = false;
					break;
				}
			}
			if (noChanges) {
				// no matching existing attribute -> add
				attributesTarget.add(attrSource);
			}
		}
	}
	
	/**
	 * Copies a template's element into the target element. Iteratively calls all child elements to do the same.
	 *
	 * @param templateElem
	 * @param targetElem
	 */
	private static void applyTemplateElementToElement(final UIElement templateElem, final UIElement targetElem) {
		logger.trace("Applying template {} to element {}", templateElem.getName(), targetElem.getName());
		
		final List<UIElement> templateChildren;
		
		if (templateElem instanceof final UIFrame frame) {
			templateChildren = frame.getChildrenRaw();
			if (targetElem instanceof final UIFrame target) {
				// TODO do not set the undefined anchors (-> track if a side was defined or is on the initial value)
				target.setAnchor(UIAnchorSide.TOP,
						frame.getAnchorRelative(UIAnchorSide.TOP),
						frame.getAnchorPos(UIAnchorSide.TOP),
						frame.getAnchorOffset(UIAnchorSide.TOP));
				target.setAnchor(UIAnchorSide.LEFT,
						frame.getAnchorRelative(UIAnchorSide.LEFT),
						frame.getAnchorPos(UIAnchorSide.LEFT),
						frame.getAnchorOffset(UIAnchorSide.LEFT));
				target.setAnchor(UIAnchorSide.BOTTOM,
						frame.getAnchorRelative(UIAnchorSide.BOTTOM),
						frame.getAnchorPos(UIAnchorSide.BOTTOM),
						frame.getAnchorOffset(UIAnchorSide.BOTTOM));
				target.setAnchor(UIAnchorSide.RIGHT,
						frame.getAnchorRelative(UIAnchorSide.RIGHT),
						frame.getAnchorPos(UIAnchorSide.RIGHT),
						frame.getAnchorOffset(UIAnchorSide.RIGHT));
				if (frame.getAttributesRaw() != null) {
					copyAttributes(frame.getAttributes(), target.getAttributes());
				}
			} else {
				logger.error("Attempting to apply a template of type Frame to a different type.");
			}
			
			
			//		} else if (templateElem instanceof UIAttribute) {
			//			final UIAttribute attr = (UIAttribute) templateElem;
			//			templateChildren = attr.getChildrenRaw();
		} else if (templateElem instanceof final UIStateGroup stateGroup) {
			templateChildren = stateGroup.getChildrenRaw();
			if (targetElem instanceof final UIStateGroup target) {
				target.setDefaultState(stateGroup.getDefaultState());
				// states are the children
			} else {
				logger.error("Attempting to apply a template of type StateGroup to a different type.");
			}
		} else if (templateElem instanceof final UIController uiController) {
			templateChildren = uiController.getChildrenRaw();
			if (targetElem instanceof final UIController target) {
				copyAttributes(uiController.getKeys(), target.getKeys());
				// TODO attributesKeyValueList
				// TODO isNameImplicit?
				// TODO next edit overrides?
			} else {
				logger.error("Attempting to apply a template of type UIController to a different type.");
			}
		} else if (templateElem instanceof final UIAnimation uiAnimation) {
			templateChildren = uiAnimation.getChildrenRaw();
			if (targetElem instanceof UIAnimation) {
				// final UIAnimation target = (UIAnimation) targetElem;
				// TODO events
				// TODO controller
				// TODO driver
			} else {
				logger.error("Attempting to apply a template of type UIAnimation to a different type.");
			}
		} else if (templateElem instanceof final UIState uiState) {
			templateChildren = uiState.getChildrenRaw();
			if (targetElem instanceof final UIState target) {
				// TODO nextAdditionShouldOverrideActions
				copyAttributes(uiState.getActions(), target.getActions());
				// TODO nextAdditionShouldOverrideWhens
				copyAttributes(uiState.getWhens(), target.getWhens());
			} else {
				logger.error("Attempting to apply a template of type UIState to a different type.");
			}
		} else {
			templateChildren = null;
		}
		
		// copy template's children
		if (templateChildren != null) {
			for (final var templateChild : templateChildren) {
				if (templateChild.getName() != null) {
					final UIElement targetChild = targetElem.receiveFrameFromPath(templateChild.getName());
					if (targetChild != null) {
						applyTemplateElementToElement(templateChild, targetChild);
					}
				}
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
	private UIElement instanciateTemplateFromList(
			final List<UITemplate> templates, final String fileName, final String path, final String newName) {
		final UIElement frameFromPath = findTemplateFromList(templates, fileName, path);
		if (frameFromPath != null) {
			final UIElement clone = (UIElement) frameFromPath.deepCopy();
			clone.setName(newName);
			return clone;
		}
		return null;
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
	public void parse(
			final int level, final String tagName, final List<String> attrTypes, final List<String> attrValues)
			throws UIException {
		logger.trace("level={}, tag={}", level, tagName);
		if (tagName == null) {
			logger.error("ERROR: tag in XML is null.");
			return;
		}
		
		// move curElement to parent position of new frame
		if (level <= 2) {
			// root
			curPath.clear();
			curLevel = level;
			curElement = null; // no parent frame
			curExtTemplate = null;
			curTemplate = null;
			// default editing mode unless the parsed aspect defines another one
			//editingMode = false;
			logger.trace("resetting path to root");
		} else {
			while (level <= curLevel) {
				--curLevel;
				// curLevel - 2 because root is level 2 on list index 0
				curElement = curPath.get(curLevel - 2);
				logger.trace("shrinking path: curElement={}, level={}\npath  pre-dropLast: {}",
						curElement,
						curLevel,
						curPath);
				curPath.remove(curPath.size() - 1);
				logger.trace("path afterDropLast: {}", curPath);
			}
		}
		
		// close action of states to enable overriding of whens/actions on next edit
		int i = statesToClose.size() - 1;
		for (; i >= 0; --i) {
			if (statesToCloseLevel.get(i) >= level) {
				final UIState state = statesToClose.get(i);
				state.setNextAdditionShouldOverrideActions(true);
				state.setNextAdditionShouldOverrideWhens(true);
				statesToClose.remove(i);
				statesToCloseLevel.remove(i);
				--i;
			}
		}
		
		UITemplate[] potentiallyEditedTemplates = null;
		// file in attribute or template definition (filtering out key and action tags as they can
		// contain file=, e.g. for cutscene frames)
		if ((i = attrTypes.indexOf(FILE)) != -1 && !KEY.equals(tagName) && !ACTION.equals(tagName)) {
			if (level != 2) {
				logger.warn("WARNING: Unexpected attribute 'file=' found in {}", curElement);
			}
			//editingMode = true;
			potentiallyEditedTemplates = catalog.getTemplatesOfPath(attrValues.get(i));
		}
		String name = ((i = attrTypes.indexOf(NAME)) != -1) ?
				catalog.getConstantValue(attrValues.get(i), raceId, curIsDevLayout, consoleSkinId) : null;
		
		UIElement newElem = null;
		
		
		// editing a frame in another template?
		if (potentiallyEditedTemplates != null) {
			if (name == null) {
				logger.error("An existing frame should be edited without defining its name.");
				name = "UnnamedFrame" + (++unnamedFrameCounter);
			}
			
			// newElement needs to be the current element
			for (final var template : potentiallyEditedTemplates) {
				final UIElement editedElem = template.receiveFrameFromPath(name);
				if (editedElem != null) {
					newElem = editedElem;
					curExtTemplate = template; // entering that template
					break;
				}
			}
			
			// curElement needs to be the parent of that frame
			final int j = name.lastIndexOf('/');
			if (j > 0) {
				final String parentName = name.substring(0, j);
				if (curExtTemplate == null) {
					logger.error("ERROR: CurExtTemplate is null, but there is a path in the name {}", name);
					curElement = null;
				} else {
					curElement = curExtTemplate.receiveFrameFromPath(parentName);
				}
			} else {
				// newElem has no parent, it is the template's root
				curElement = null;
			}
		} else {
			// editing existing template within the current file?
			if (level <= 2 && name != null) {
				for (final UITemplate template : newTemplatesOfCurFile) {
					if (template.getElement().getName().equals(name)) {
						newElem = template.getElement();
						curElement = null;
						break;
					}
				}
			} else {
				// editing an existing sibling element?
				if (curElement != null && name != null) {
					// TODO name can have '/', e.g. <Frame type="Frame" name="UIContainer/FullscreenUIContainer">
					final List<UIElement> childrenRaw = curElement.getChildrenRaw();
					if (childrenRaw != null) {
						for (final UIElement child : childrenRaw) {
							// TODO what if the type is different, but name is identical?
							if (name.equals(child.getName())) {
								newElem = child;
								break;
							}
						}
					}
				}
			}
		}
		
		// handle template attribute
		if ((i = attrTypes.indexOf(TEMPLATE)) != -1) {
			if (newElem != null) {
				// This is editing mode!
				if (curExtTemplate == null || !curExtTemplate.isLocked()) {
					// recursively copy template (attrValues.get(i))'s attributes into existing frame (= newElem)
					applyTemplateElementToElement(attrValues.get(i), newElem);
				} else {
					if (curExtTemplate != null) {
						logger.error("ERROR: attempting to edit '{}' within the locked layout file '{}'",
								newElem.getName(),
								curExtTemplate.getFileName());
					}
				}
			} else {
				// create from template (actions may use template= and need to be ignored)
				if (!ACTION.equals(tagName)) {
					// TODO what can have a template?
					if (FRAME.equals(tagName) || ANIMATION.equals(tagName)) {
						if (name == null) {
							logger.error("A frame has no name, but should be instanciated with template='{}'",
									attrValues.get(i));
							name = "UnnamedFrame" + (++unnamedFrameCounter);
						}
						newElem = instanciateTemplate(attrValues.get(i), name);
					} else {
						logger.error("ERROR: unexpected 'template' attribute on '<{}>'", tagName);
					}
				}
			}
		}
		
		// prevent editing of layouts of locked templates
		if (curExtTemplate != null && curExtTemplate.isLocked()) {
			if (newElem != null) {
				logger.error("ERROR: attempting to edit '{}' within the locked layout file '{}'",
						newElem.getName(),
						curExtTemplate.getFileName());
				newElem = null;
			}
		} else {
			// use lowercase for cases!
			switch (tagName) {
				case FRAME:
					if (newElem == null) {
						if (name == null) {
							logger.error("A new 'Frame' was defined without a name.");
							name = "UnnamedFrame" + (++unnamedFrameCounter);
						}
						newElem = new UIFrameMutable(name);
					}
					String type = ((i = attrTypes.indexOf(TYPE)) != -1) ?
							catalog.getConstantValue(attrValues.get(i), raceId, curIsDevLayout, consoleSkinId) : null;
					if (type == null) {
						logger.error("Unknown or no type defined in child element of: {}", curElement);
						type = FRAME;
					}
					final var newElemUiFrame = (UIFrame) newElem;
					//				if (!checkFrameTypeCompatibility(type, newElemUiFrame.getType())) {
					//					logger.warn("WARN: The type of the frame is not compatible with the used template.");
					//				}
					newElemUiFrame.setType(type);
					// add to parent
					if (curElement != null) {
						if (curElement instanceof UIFrame) {
							curElement.getChildren().add(newElem);
						} else {
							logger.error("Frame appearing in unexpected parent element: {}", curElement);
						}
					}
					break;
				case ANCHOR:
					parseAnchor(attrTypes, attrValues);
					return;
				case STATE:
					if (newElem == null) {
						if (name == null) {
							logger.error("A new 'State' was defined without a name.");
							name = "UnnamedState" + (++unnamedFrameCounter);
						}
						newElem = new UIStateMutable(name);
					}
					if (level == 2) {
						catalog.addTemplate(curFileName, newElem, curIsDevLayout);
					} else {
						// add to parent
						if (curElement instanceof UIStateGroup) {
							curElement.getChildren().add(newElem);
							
							// set flags to override on edit after parsing children
							statesToClose.add((UIState) newElem);
							statesToCloseLevel.add(level);
						} else {
							logger.error("State appearing outside a stategroup.");
						}
					}
					break;
				case CONTROLLER:
					// name is allowed to be null here => receives an implicit name
					newElem = new UIControllerMutable(name);
					// add to parent
					if (curElement != null) {
						if (curElement instanceof final UIAnimation anim) {
							anim.getControllers().add(newElem);
							final var newElemUiController = (UIController) newElem;
							for (int j = 0, len = attrValues.size(); j < len; ++j) {
								newElemUiController.addValue(attrTypes.get(j), attrValues.get(j));
							}
							
							if (name == null) {
								setImplicitControllerNames(anim);
							}
						} else {
							logger.error("Controller appearing in unexpected parent element: {}", curElement);
						}
					}
					break;
				case ANIMATION:
					if (name == null) {
						logger.error("A new 'Animation' was defined without a name.");
						name = "UnnamedAnimation" + (++unnamedFrameCounter);
					}
					newElem = new UIAnimationMutable(name);
					// add to parent
					if (curElement != null) {
						if (curElement instanceof UIFrame) {
							curElement.getChildren().add(newElem);
						} else {
							logger.error("Animation appearing in unexpected parent element: {}", curElement);
						}
					}
					break;
				case STATEGROUP:
					if (name == null) {
						logger.error("A new 'StateGroup' was defined without a name.");
						name = "UnnamedStateGroup" + (++unnamedFrameCounter);
					}
					newElem = new UIStateGroupMutable(name);
					// add to parent
					if (curElement != null) {
						if (curElement instanceof UIFrame) {
							curElement.getChildren().add(newElem);
						} else {
							logger.error("StateGroup appearing in unexpected parent element: {}", curElement);
						}
					}
					break;
				case CONSTANT:
					if (name == null) {
						logger.error("A new 'Constant' was defined without a name.");
						name = "UnnamedConstant" + (++unnamedFrameCounter);
					}
					final String val = ((i = attrTypes.indexOf(VAL)) != -1) ?
							catalog.getConstantValue(attrValues.get(i), raceId, curIsDevLayout, consoleSkinId) : null;
					if (val == null) {
						logger.error("Constant '{}' has no value defined", name);
						return;
					}
					UIConstant newElemUiConstant = new UIConstantImmutable(name, val);
					newElem = newElemUiConstant;
					if (deduplicateDuringParsing) {
						final UIElement refToDuplicate = addedFinalElements.get(newElem);
						if (refToDuplicate != null) {
							newElemUiConstant = (UIConstant) refToDuplicate;
							++constantDeduplications;
						} else {
							addedFinalElements.put(newElem, newElem);
						}
					}
					catalog.addConstant(newElemUiConstant, curIsDevLayout);
					return;
				case DESC:      // nothing to do
					return;
				case DESCFLAGS: // locked or internal or empty to remove internal
					if (level <= 2) {
						// is on root level outside templates => must be 'locked'
						final int j = attrTypes.indexOf(VAL);
						if (j != -1) {
							final String attrVal =
									catalog.getConstantValue(attrValues.get(j), raceId, curIsDevLayout, consoleSkinId)
											.trim();
							if ("locked".equalsIgnoreCase(attrVal)) {
								layoutFileDescLocked = true;
							} else if (attrVal.isEmpty()) {
								layoutFileDescLocked = false;
							} else {
								logger.warn(
										"WARNING: unexpected value of <DescFlags>. Val is '{}'. Expects 'locked' or an empty value",
										attrVal);
								// TODO validate if this is correct
								layoutFileDescLocked = false;
							}
						} else {
							logger.error("ERROR: <DescFlags> requires a val attribute");
						}
					} else {
						// TODO 'locked' only on root level; other frames can only have 'internal' or ''
					}
					return;
				case INCLUDE:
					final int j = attrTypes.indexOf(PATH);
					if (j != -1) {
						final String path = attrValues.get(j);
						final boolean isDevLayout = curIsDevLayout || attrTypes.contains(REQUIREDTOLOAD);
						catalog.processInclude(path, isDevLayout, raceId, consoleSkinId, deduplicationIntensity);
					}
					break;
				default:
					// attribute or something unknown that will cause an error
					final ArrayList<String> attributeKeyValueList = new ArrayList<>(2 * attrTypes.size());
					i = 0;
					for (final int len = attrTypes.size(); i < len; ++i) {
						attributeKeyValueList.add(attrTypes.get(i));
						attributeKeyValueList.add(catalog.getConstantValue(attrValues.get(i),
								raceId,
								curIsDevLayout,
								consoleSkinId));
					}
					UIAttribute newElemUiAttr = new UIAttributeImmutable(tagName, attributeKeyValueList);
					newElem = newElemUiAttr;
					
					if (deduplicateDuringParsing) {
						final UIElement refToDuplicate = addedFinalElements.get(newElem);
						if (refToDuplicate != null) {
							newElem = refToDuplicate;
							newElemUiAttr = (UIAttribute) newElem;
							++attributeDeduplications;
						} else {
							addedFinalElements.put(newElem, newElem);
						}
					}
					
					// add to parent
					if (curElement instanceof final UIFrame frame) {
						// Frame's attributes
						frame.addAttribute(newElemUiAttr);
						// register handle
						if (HANDLE.equals(tagName)) {
							catalog.getHandles().put(newElemUiAttr.getValue(VAL), frame);
						}
					} else if (curElement instanceof final UIAnimation anim) {
						// Animation's events
						if (tagName.equals(EVENT)) {
							anim.addEvent(newElemUiAttr);
						} else if (tagName.equals(DRIVER)) {
							anim.setDriver(newElemUiAttr);
						} else {
							logger.error("found an attribute that cannot be added to UIAnimation: {}", newElem);
						}
					} else if (curElement instanceof final UIController controller) {
						// Controller's keys
						if (tagName.equals(KEY)) {
							controller.getKeys().add(newElemUiAttr);
						} else {
							logger.error("found an attribute that cannot be added to UIController: {}", newElem);
						}
					} else if (curElement instanceof final UIStateGroup stateGroup) {
						if (tagName.equals(DEFAULTSTATE)) {
							final String stateVal = newElemUiAttr.getValue(VAL);
							if (stateVal != null) {
								stateGroup.setDefaultState(stateVal);
							} else {
								logger.error("found <DefaultState> in <StateGroup '{}'> without val",
										curElement.getName());
							}
						} else {
							logger.error("found an attribute that cannot be added to UIController: {}", newElem);
						}
					} else if (curElement instanceof final UIState state) {
						if (tagName.equals(WHEN)) {
							state.getWhens().add(newElemUiAttr);
						} else if (tagName.equals(ACTION)) {
							state.getActions().add(newElemUiAttr);
						} else {
							logger.error("found an attribute that cannot be added to UIState: {}", newElem);
						}
					} else {
						logger.error("found an attribute that cannot be added to anything: {}", newElem);
					}
					
					newElem = null;
					break;
			}
		}
		
		// register new templates
		if (level == 2 && curExtTemplate == null) {
			if (newElem != null) {
				logger.trace("adding new template: '{}' added to '{}'", newElem.getName(), curFileName);
				curTemplate = catalog.addTemplate(curFileName, newElem, curIsDevLayout);
				newTemplatesOfCurFile.add(curTemplate);
			} else {
				// caused by not found template="..." or an <include>
				logger.trace("skipped creating a template because newElem was null. curFileName: {}", curFileName);
				curTemplate = null;
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
	
	private void applyTemplateElementToElement(final String pathParam, final UIElement targetElem) {
		logger.trace("Applying Template of path {} to element {} - searching the template",
				pathParam,
				targetElem.getName());
		final String path = pathParam.replace('\\', '/');
		final int seperatorIndex = path.indexOf('/');
		if (seperatorIndex < 0) {
			logger.error("ERROR: Template paths must follow the pattern 'FileName/FrameName'. Found '{}' instead.",
					path);
			return;
		}
		final String fileName = path.substring(0, seperatorIndex);
		
		// 1. check templates
		UIElement templateInstance = findTemplateFromList(catalog.getTemplates(), fileName, path);
		if (templateInstance == null) {
			// 2. if fail -> check dev templates
			templateInstance = findTemplateFromList(catalog.getBlizzOnlyTemplates(), fileName, path);
		}
		if (templateInstance == null) {
			// template does not exist or its layout was not loaded, yet
			if (!curIsDevLayout) {
				logger.error("ERROR: Template of path '{}' could not be found.", path);
			} else {
				logger.warn(
						"WARNING: Template of path '{}' could not be found, but we are creating a Blizz-only layout, so this is fine.",
						path);
			}
		} else {
			applyTemplateElementToElement(templateInstance, targetElem);
		}
	}
	
	/**
	 * @param path
	 * @param newName
	 * @return Template instance
	 */
	private UIElement instanciateTemplate(String path, final String newName) {
		logger.trace("Instanciating Template of path {}", path);
		path = path.replace('\\', '/');
		final int seperatorIndex = path.indexOf('/');
		if (seperatorIndex < 0) {
			logger.error("ERROR: Template paths must follow the pattern 'FileName/FrameName'. Found '{}' instead.",
					path);
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
					logger.error("ERROR: the non-Blizz-only frame '{}' uses a Blizz-only template '{}'.",
							curElement,
							path);
				}
				return templateInstance;
			}
		}
		// template does not exist or its layout was not loaded, yet
		if (!curIsDevLayout) {
			logger.error("ERROR: Template of path '{}' could not be found.", path);
		} else {
			logger.warn(
					"WARNING: Template of path '{}' could not be found, but we are creating a Blizz-only layout, so this is fine.",
					path);
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
		
		if (curElement instanceof final UIFrame frame) {
			if (side == null) {
				logger.trace("relative={}, offset={}", relative, offset);
				if (relative == null) {
					logger.error("'Anchor' attribute has no 'relative' attribute defined in parent element: {}",
							curElement.getName());
				} else {
					try {
						frame.setAnchor(relative, offset);
					} catch (final NumberFormatException e) {
						logger.error("A frame's offset '{}' is not a numerical value. Using 0 instead.", offset);
						frame.setAnchorOffset(UIAnchorSide.RIGHT, UIFrameMutable.ZERO);
						frame.setAnchorOffset(UIAnchorSide.BOTTOM, UIFrameMutable.ZERO);
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
					logger.error(
							"'Anchor' attribute has unrecognizable value for 'side='. Value is '{}' in parent element: {}",
							side,
							curElement.getName());
				}
				if (sideVal != null) {
					if (offset == null) {
						logger.error("'Anchor' attribute has no 'offset' attribute defined in parent element: {}",
								curElement.getName());
					} else {
						if (pos == null) {
							logger.error(
									"'Anchor' attribute has no 'pos' attribute defined in parent element: {}",
									curElement.getName());
						} else {
							if (relative == null) {
								logger.error(
										"'Anchor' attribute has no 'relative' attribute defined in parent element: {}",
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
	
	@Override
	public void endLayoutFile() {
		// close all states
		for (final UIState s : statesToClose) {
			s.setNextAdditionShouldOverrideActions(true);
			s.setNextAdditionShouldOverrideWhens(true);
		}
		statesToClose.clear();
		statesToCloseLevel.clear();
		
		// set templates locked state
		if (layoutFileDescLocked) {
			for (final UITemplate template : newTemplatesOfCurFile) {
				template.setLocked(true);
			}
			layoutFileDescLocked = false;
		}
		newTemplatesOfCurFile.clear();
	}
	
	@Override
	public void deduplicate() {
		if (deduplicatePostProcessing) {
			logger.info("unique elements added that were deduplicated during parsing: {}", addedFinalElements.size());
			// replace instances
			final Deque<Object> toDeduplicate =
					new ArrayDeque<>(catalog.getTemplates().size() + catalog.getBlizzOnlyTemplates().size());
			toDeduplicate.addAll(catalog.getBlizzOnlyTemplates());
			toDeduplicate.addAll(catalog.getTemplates());
			int maxDequeSize = 0;
			while (!toDeduplicate.isEmpty()) {
				maxDequeSize = Math.max(maxDequeSize, toDeduplicate.size());
				final Object pop = toDeduplicate.pop();
				logger.trace("deduplicating template content: {}, addedFinalElements: {}",
						pop,
						addedFinalElements.size());
				deduplicate(pop);
			}
			logger.info(
					"postProcessDeduplications: {}, attributeDeduplications: {}, constantsDeduplications={}, maxDequeSize: {}, addedFinalElements: {}",
					postProcessDeduplications,
					attributeDeduplications,
					constantDeduplications,
					maxDequeSize,
					addedFinalElements.size());
		}
	}
	
	private void deduplicate(final Object obj) {
		if (obj instanceof final UIFrame frame) {
			deduplicate(frame);
		} else if (obj instanceof final UIAnimation anim) {
			deduplicate(anim);
		} else if (obj instanceof final UIStateGroup stateGroup) {
			deduplicate(stateGroup);
		} else if (obj instanceof final UITemplate template) {
			deduplicate(template);
		} else if (!(obj instanceof UIAttribute || obj instanceof UIConstant || obj instanceof UIController ||
				obj instanceof UIState)) {
			// attributes, constants, controllers do not contain any deduplicated objects
			// attributes and constants can be deduplicated when initially created
			logger.error("Object cannot be handled in deduplication: {},", obj);
		}
	}
	
	@SuppressWarnings("java:S3824")
	private void deduplicate(final UIFrame frame) {
		//		logger.trace("deduplicating: {}", frame);
		final List<UIElement> childrenRaw = frame.getChildrenRaw();
		if (childrenRaw != null) {
			for (int i = 0, len = childrenRaw.size(); i < len; ++i) {
				final UIElement child = childrenRaw.get(i);
				final UIElement duplicate = addedFinalElements.get(child);
				if (duplicate != null && child != duplicate) {
					// replace in parent
					childrenRaw.set(i, duplicate);
					++postProcessDeduplications;
				} else {
					deduplicate(child);
					if (duplicate == null) {
						addedFinalElements.put(child, child);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("java:S3824")
	private void deduplicate(final UIAnimation anim) {
		//		logger.trace("deduplicating: {}", anim);
		final List<UIElement> controllers = anim.getControllers();
		for (int i = 0, len = controllers.size(); i < len; ++i) {
			final UIElement controller = controllers.get(i);
			final UIElement duplicate = addedFinalElements.get(controller);
			if (duplicate != null && controller != duplicate) {
				// replace in parent
				controllers.set(i, duplicate);
				++postProcessDeduplications;
			} else {
				// controllers cannot be deduplicated any further at this point (Attributes were already)
				if (duplicate == null) {
					addedFinalElements.put(controller, controller);
				}
			}
		}
	}
	
	@SuppressWarnings("java:S3824")
	private void deduplicate(final UIStateGroup stateGroup) {
		//		logger.trace("deduplicating: {}, deduplicatedElements; {}", stateGroup, addedFinalElements.size());
		final List<UIElement> states = stateGroup.getChildrenRaw();
		for (int i = 0, len = states.size(); i < len; ++i) {
			final UIElement child = states.get(i);
			final UIElement duplicate = addedFinalElements.get(child);
			if (duplicate != null && child != duplicate) {
				// replace in parent
				states.set(i, duplicate);
				++postProcessDeduplications;
			} else {
				// states cannot be deduplicated any further at this point (Attributes were already)
				if (duplicate == null) {
					addedFinalElements.put(child, child);
				}
			}
		}
	}
	
	@SuppressWarnings("java:S3824")
	private void deduplicate(final UITemplate template) {
		//		logger.trace("deduplicating: {}", template);
		final UIElement elem = template.getElement();
		final UIElement duplicate = addedFinalElements.get(elem);
		if (duplicate != null && elem != duplicate) {
			// replace in parent
			template.setElement(duplicate);
			++postProcessDeduplications;
		} else {
			deduplicate(elem);
			if (duplicate == null) {
				addedFinalElements.put(elem, elem);
			}
		}
	}
}
