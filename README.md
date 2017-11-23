# Galaxy-Observer-UI
This is a toolset to create Observer Interface files for StarCraft II and Heroes of the Storm.

## User Guide:

### User Installation:
Required Software (for Windows):
* [Java 9 SDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* A text editor preferably with XML syntax support, e.g. [Notepad++](https://notepad-plus-plus.org/) with the [NppExec plugin](https://sourceforge.net/projects/npp-plugins/files/NppExec/)

### Using the Compiler:
To compile all archives, just start 'compile.jar' within the 'dev' folder.
To compile a selected archive and start a replay, consult the 'ReadMe.txt' in the same folder.

#### Installation Steps:
1. Install the Java SDK.
2. Download and place this development environment somewhere.
3. Put your Observer Interface as a folder into the "dev" folder's subdirectory, e.g.: "dev/sc2/GameHeart.SC2Interface" or "dev/heroes/AhliObs.StormInterface"
4. Edit "settings.ini" within the root folder with your game paths and preferences.
5. Run MPQEditor found in the "tools/plugins/mpq" directory.
    Click on Tools > Compatibility > Select 'Custom Ruleset' > OK > exit the app and copy 'MPQEditor_Ruleset.ini' to 'C:\Users\YOUR_USERNAME\AppData\Roaming'. These rules will make the generated interface files smaller.

### Tools-Developer Installation:
Required Software (for Windows):
* [Java 9 SDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Java 8 SDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) (required to build the UI SettingsEditor for its [javafx-maven-plugin](https://github.com/javafx-maven-plugin/javafx-maven-plugin) that builds the standalone installer)
* Java IDE, for example [Eclipse for Java Developers](http://www.eclipse.org/downloads/eclipse-packages/)
* A text editor preferably with XML syntax support, e.g. [Notepad++](https://notepad-plus-plus.org/) with the [NppExec plugin](https://sourceforge.net/projects/npp-plugins/files/NppExec/)
* Git or some of its management software like [Sourcetree](https://www.sourcetreeapp.com/).
* [WiX Toolset](http://wix.sf.net) (required for UI SettingsEditor installer)
* [Inno Setup 5](http://www.jrsoftware.org/isdl.php) (required for UI SettingsEditor installer)

#### Tools-Developer Installation Steps:
1. Install both Java SDKs.
2. Install Sourcetree and clone this repository.
3. Install Notepad++ and its NppExec plugin.
    To install the plugin, place it within the 'plugins' folder of notepad++'s install directory.
4. Install Inno Setup 5
5. Install WiX Toolset
6. Run MPQEditor found in the "tools/plugins/mpq" directory.
    Click on Tools > Compatibility > Select 'Custom Ruleset' > OK > exit the app and copy 'MPQEditor_Ruleset.ini' to 'C:\Users\YOUR_USERNAME\AppData\Roaming'. These rules will make the generated interface files smaller.
7. Install Eclipse and import this repository.
    Suggested plugins to install:
    * e(fx)clipse - IDE
    * FindBugs
    * Quick Search for Eclipse

## Further Software already included:
* [MPQ Editor by Ladislav Zezula](http://www.zezula.net/en/mpq/download.html). It is used to read and write Interface files.
* [CASCexplorer by tomrus88](https://github.com/WoW-Tools/CASCExplorer/releases). It is used to easily extract the base UI from the games via a batch files.
