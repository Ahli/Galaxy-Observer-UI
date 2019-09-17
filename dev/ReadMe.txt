READ ME - compile-spring-boot.jar
=============================================================================================================================================
Author: Ahli
=============================================================================================================================================

A log file is created in your system's user.home path. For example: C:\Users\Ahli\.GalaxyObsUI\logs

=============================================================================================================================================
Parameters:
--compile="pathToInterfaceSourceFolder"
	This will compile a specific Interface
--compileRun="pathToInterfaceSourceFolder"
	This will compile a specific Interface and launch the appropriate game based on the settings.ini with the most recent replay.
--run="pathToGamesSwitcher.exe"
	This will launch the game's specified Switcher.exe with the most recent replay.

See the following example:
=============================================================================================================================================
Example code for Notepad++'s NppExec plugin:
javaw -jar D:\GalaxyObsUI\dev\compile-spring-boot.jar --compileRun="$(CURRENT_DIRECTORY)"
	(You might have to enable NppExec's Follow $(CURRENT_DIRECTORY) setting inside the dropdown.)
	(Suggested highlight for NppExec: "*ERROR:*%FILE%*" and "	at*".)
=============================================================================================================================================

There are two modes supported:
- If the Builder is already running, the commands are transferred. The output is send back and shown in the console.
- If the Builder is not running, the Builder is booting, performs the task and exits itself again, if there was no error.
