# Galaxy-Observer-UI
This is a toolset to create Observer Interface files for StarCraft II and Heroes of the Storm.

## User Guide:

### User Installation:
Required Software (for Windows):
* [Java 17 or higher](https://adoptopenjdk.net)
* A text editor preferably with XML syntax support, e.g. [Notepad++](https://notepad-plus-plus.org/) with the [NppExec plugin](https://sourceforge.net/projects/npp-plugins/files/NppExec/)

### Using the Builder:
To compile all archives, just start 'compile-spring-boot.jar' within the 'dev' folder via the .bat file.
To compile a selected archive and start a replay, consult the 'ReadMe.txt' in the same folder.

#### Installation Steps:
1. Install the Java SDK.
2. Download and place this development environment somewhere.
3. Start the Builder via the .bat file located in the dev directory. Add your projects or create new ones in the home screen.
4. Open the Settings screen and set your game paths.
5. In the Browse screen's Manage tab, extract the base UI of your games.

### Tools-Developer Installation:
Required Software (for Windows):
* [Java 17 or higher](https://adoptopenjdk.net)
* Java IDE, for example [IntelliJ](https://www.jetbrains.com/idea/download/#section=windows) or [Eclipse for Java Developers](https://www.eclipse.org/downloads/eclipse-packages/)
* A text editor preferably with XML syntax support, e.g. [Notepad++](https://notepad-plus-plus.org/) with optionally the [NppExec plugin](https://sourceforge.net/projects/npp-plugins/files/NppExec/) or alternatively [VSCode](https://code.visualstudio.com) with [Talv's SC2Layout plugin](https://github.com/Talv/sc2-layouts)
* Git or some of its GUI software like [Sourcetree](https://www.sourcetreeapp.com/).
* [NSIS](https://nsis.sourceforge.io/Main_Page) to create installers for Settings Editor

#### Tools-Developer Installation Steps:
1. Install Java.
2. Install Git and clone this repository.
3. Install Notepad++ and its NppExec plugin.
    To install the plugin via Notepad's Plugin Admin dialog. Then configure the plugin with the parameters suggested in dev/ReadMe.txt.
    Alternatively, use [VSCode](https://code.visualstudio.com) and install [Talv's plugin for SC2Layout files](https://github.com/Talv/sc2-layouts)
4. Install your Java IDE
5. Install NSIS

## Further Software already included:
* [MPQ Editor by Ladislav Zezula](http://www.zezula.net/en/mpq/download.html). It is used to open and create MPQ files.
