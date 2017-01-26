@echo off
: Batch Script to extract files from CASC
: author: @AhliSC2

: User Params:
set GAMEPATH=F:\Spiele\Heroes of the Storm
set PRODUCT=hero
set LOCALE=enUS
set DESTINATION_SUBFOLDER=heroes

: Set up Parameters
echo ===============Parameters==============
set DESTINATION=%~dp0%DESTINATION_SUBFOLDER%\
pushd ..
set parentLevel=%cd%
popd
set CASCPROGRAM=%parentLevel%\tools\plugins\casc\CASCConsole.exe
set CASCSETTINGSPROG=%parentLevel%\tools\cascToolSettingEdit\cascExplorerSettingsEdit.jar
set CASCSETTINGSFILE=%parentLevel%\tools\plugins\casc\CASCConsole.exe.config

echo DESTINATION=%DESTINATION%
echo CASCPROGRAM=%CASCPROGRAM%
echo CASCSETTINGSPROG=%CASCSETTINGSPROG%
echo CASCSETTINGSFILE=%CASCSETTINGSFILE%
echo PRODUCT=%PRODUCT%
echo =======================================

: Edit Configuration File
: params: [ConfigFilePath] [StoragePath] [OnlineMode] [Product] [Locale]
: TESTING/DEBUGGING: java -jar cascExplorerSettingsEdit.jar D:\GalaxyObsUI\tools\plugins\casc\CASCConsole.exe.config "F:\Spiele\Heroes of the Storm" "False" "hero" "enUS"
echo echo | set /p=...editing configuration file...
start /WAIT /MIN cmd /K ""%CASCSETTINGSPROG%" "%CASCSETTINGSFILE%" "%GAMEPATH%" "False" "%PRODUCT%" "%LOCALE%" & exit"
echo 	OK.

: Clear Target Directory
echo echo | set /p=...clearing target directory...
@RD /S /Q %DESTINATION%\mods
echo 		OK.

: Extract Files from CASC
echo echo | set /p=...EXTRACTING FILES...
start /WAIT /MIN "" "%CASCPROGRAM%" *.StormLayout %DESTINATION% %LOCALE% None
start /WAIT /MIN "" "%CASCPROGRAM%" *Assets.txt %DESTINATION% %LOCALE% None
start /WAIT /MIN "" "%CASCPROGRAM%" *.StormStyle %DESTINATION% %LOCALE% None
echo 	OK.

: Reset Configuration File
echo echo | set /p=...resetting configuration file...
start /WAIT /MIN cmd /K ""%CASCSETTINGSPROG%" "%CASCSETTINGSFILE%" "QQQ" "QQQ" "QQQ" "QQQ" & exit"
echo 	OK.

: Clear Debug output file from casc tool
del %~dp0debug.log

: pause