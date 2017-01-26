READ ME - Compile.jar
=============================================================================================================================================
Author: Ahli
=============================================================================================================================================

Execute compile.jar without parameters to build all Interfaces.

=============================================================================================================================================
Parameters:
--compile="pathToInterfaceSourceFolder"
	This will compile a specific Interface
--run="pathToGamesSwitcher.exe"
	This will launch the game's specified Switcher.exe with the most recent replay.

See the following example:
=============================================================================================================================================
Example code for Notepad++'s NppExec plugin:
javaw -jar D:\GalaxyObsUI\dev\compile.jar --compile="$(CURRENT_DIRECTORY)" --run="F:\Spiele\Heroes of the Storm\Support\HeroesSwitcher.exe"
	(You might have to enable NppExec's Follow $(CURRENT_DIRECTORY) setting inside the dropdown.)
	(Suggested highlight for NppExec: "*ERROR:*%FILE%*" and "	at*".)
=============================================================================================================================================

