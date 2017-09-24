# Galaxy-Observer-UI
This is a toolset to create Observer Interface files for StarCraft II and Heroes of the Storm.

## IDE Installation:
Required Software (for Windows):
* [Java 9 SDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Java 8 SDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* Java IDE, for example [Eclipse for Java Developers](http://www.eclipse.org/downloads/eclipse-packages/)
* A text editor preferably with XML syntax support, e.g. [Notepad++](https://notepad-plus-plus.org/) with the [NppExec plugin](https://sourceforge.net/projects/npp-plugins/files/NppExec/)
* Git or some of its management software like [Sourcetree](https://www.sourcetreeapp.com/).
* [WiX Toolset](http://wix.sf.net)
* [Inno Setup 5](http://www.jrsoftware.org/isdl.php)

### Installation Steps:
1. Install both Java SDKs.
2. Install Sourcetree and clone this repository.
3. Install Notepad++ and its NppExec plugin.
    To install the plugin, place it within the 'plugins' folder of notepad++'s install directory.
4. Install Inno Setup 5.
5. Install WiX Toolset.
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
