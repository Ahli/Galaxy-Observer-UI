@echo off
: Batch Script 
: author: @AhliSC2

"C:\Program Files\Java\jdk-11\bin\jlink.exe" --module-path "D:\Galaxy-Observer-UI\tools\observerUiSettingsEditor\runtimeConstructionYard\compiledClasses;$JAVA_HOME/jmods;D:\Galaxy-Observer-UI\tools\observerUiSettingsEditor\runtimeConstructionYard\dependencies" --add-modules ObserverUiSettingsEditor --compress=2 --exclude-files *.diz --output tinyRuntime

pause
