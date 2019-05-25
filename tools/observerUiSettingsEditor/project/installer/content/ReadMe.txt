Observer Interface Settings Editor
----------------------------------
Author: Christoph "Ahli" Ahlers (twitter: @AhliSC2)
version: alpha


Legal Information
-----------------
Use at your own risk. This software is free to use.
Do not duplicate, copy, steal, sell, eat, etc.
Copyright 2019 <Christoph Ahlers>


What does it do?
----------------
This program is a simple editor to load and edit hotkeys and settings within Observer Interface files which use the file types .SC2Interface and .StormInterface.
These Observer Interface files can be loaded in the game options of Blizzard Entertainment's games StarCraft II and Heroes of the Storm to adjust the UI of an observer.
The interfaces are required to define adjustable constants within a layout file with the syntax described at the end of this document.



Contained Files:
----------------
The files installed into your install directory.


Other Files that are created and persist:
-----------------------------------
- %AppData%\Roaming\MPQEditor.ini						Ladik's MpqEditor settings file.
- %AppData%\Roaming\MPQEditor_Ruleset.ini					Ladik's MpqEditor settings file for custom file compression.
- %UserHome%\.GalaxyObsUiSettingsEditor\logs\settingsEditor_lastRun.log		Log file of the last settings editor execution for debugging.



Credits:
--------
- @tinkoliu for translating the editor to Chinese and Taiwanese
- Ladislav "Ladik" Zezula for creating his MpqEditor. It is used to open and create the Observer Interface files.




How to add Hotkeys and Settings to your Observer Interface?
-----------------------------------------------------------

The editor will scan all layout files for special texts in comments that define hotkeys and settings.

Here is an example hotkey and setting definition:

	<!-- @Hotkey (	constant = "Hotkey - Toggle Talents",
			default = "Control+1",
			description = "Toggle bottom panel's talent tab."
			) -->
	<!-- @Setting (	constant = "LeaderPanelBottom_VerticalOffset",
			default = "0",
			description = "Controls the offset of the bottom panel. Use values like '-75' to move it upwards."
			) -->
	<Frame type="Frame" name="ObserverUiSettingsDefinitions">
	</Frame>

	<Constant name="Hotkey - Toggle Talents" val="Control+1"/>
	<Constant name="LeaderPanelBottom_VerticalOffset" val="0"/>

A definition has to be in a comment as shown above. The minimal definition requires only the name of the constant:

	@Hotkey ( constant = "name of the constant you want to modify" )

All settings you wish to edit has to use constants, so your actual control frames need to define a shortcut with a constant:

	<Frame type="ToggleControl" name="ToggleTalentOverview">
		<Anchor relative="$parent"/>
		<Shortcut val="#Hotkey - Toggle Talents"/>
	</Frame>

Furthermore, you should define a default value and a description for your settings and hotkeys. These information are not required, but help the user.


