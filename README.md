# Galaxy-Observer-UI
This is a toolset to create Observer Interface files for StarCraft II and Heroes of the Storm.

## User Guide:

### User Installation:
Required Software (for Windows):
* [Java 11](https://jdk.java.net/11/) or [Java 12's EA version](https://jdk.java.net/12/) (to properly give RAM back to the system after building a UI)
* A text editor preferably with XML syntax support, e.g. [Notepad++](https://notepad-plus-plus.org/) with the [NppExec plugin](https://sourceforge.net/projects/npp-plugins/files/NppExec/)

### Using the Compiler:
To compile all archives, just start 'compile.jar' within the 'dev' folder via the .bat file.
To compile a selected archive and start a replay, consult the 'ReadMe.txt' in the same folder.

#### Installation Steps:
1. Install the Java SDK.
2. Download and place this development environment somewhere.
3. Put your Observer Interface as a folder into the "dev" folder's subdirectory, e.g.: "dev/sc2/GameHeart.SC2Interface" or "dev/heroes/AhliObs.StormInterface"
4. Edit "settings.ini" within the root folder with your game paths and preferences.
5. Run MPQEditor found in the "tools/plugins/mpq" directory.
    Click on Tools > Compatibility > Select 'Custom Ruleset' > OK > exit the app and copy 'MPQEditor_Ruleset.ini' to 'C:\Users\YOUR_USERNAME\AppData\Roaming'. These rules will make the generated Observer Interface files smaller.

### Tools-Developer Installation:
Required Software (for Windows):
* [Java 11](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* Java IDE, for example [Eclipse for Java Developers](http://www.eclipse.org/downloads/eclipse-packages/)
* A text editor preferably with XML syntax support, e.g. [Notepad++](https://notepad-plus-plus.org/) with the [NppExec plugin](https://sourceforge.net/projects/npp-plugins/files/NppExec/) or alternatively [VSCode](https://code.visualstudio.com) with [Talv's SC2Layout plugin](https://github.com/Talv/sc2-layouts)
* Git or some of its management software like [Sourcetree](https://www.sourcetreeapp.com/).
* [NSIS](https://nsis.sourceforge.io/Main_Page) to create installers for Settings Editor

#### Tools-Developer Installation Steps:
1. Install Java.
2. Install Sourcetree and clone this repository.
3. Install Notepad++ and its NppExec plugin.
    To install the plugin, place it within the 'plugins' folder of notepad++'s install directory.
    Alternatively, use [VSCode](https://code.visualstudio.com) and install [Talv's plugin for SC2Layout files](https://github.com/Talv/sc2-layouts)
4. Run MPQEditor found in the "tools/plugins/mpq" directory.
    Click on Tools > Compatibility > Select 'Custom Ruleset' > OK > exit the app and copy 'MPQEditor_Ruleset.ini' to 'C:\Users\YOUR_USERNAME\AppData\Roaming'. These rules will make the generated interface files smaller.
5. Install IntelliJ
6. Install NSIS

## Further Software already included:
* [MPQ Editor by Ladislav Zezula](http://www.zezula.net/en/mpq/download.html). It is used to open and create MPQ files.
* [CASCexplorer by tomrus88](https://github.com/WoW-Tools/CASCExplorer/releases). It is used to extract the base UI from the games.
