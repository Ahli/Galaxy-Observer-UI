@echo off
: Batch Script to extract files from CASC
: author: @AhliSC2

: User Params:
set PRODUCT=sc2
set LOCALE=enUS
set DESTINATION_SUBFOLDER=sc2


: Get GamePath from settings.ini
: code based on http://stackoverflow.com/questions/2866117/read-ini-from-windows-batch-file
pushd ..
set parentLevel=%cd%
popd
: code from http://stackoverflow.com/questions/2866117/read-ini-from-windows-batch-file
set file=%parentLevel%\settings.ini
set area=[GamePaths]
set key=StarCraft2_Path
@setlocal enableextensions enabledelayedexpansion
set currarea=
for /f "usebackq delims=" %%a in ("!file!") do (
    set ln=%%a
    if "x!ln:~0,1!"=="x[" (
        set currarea=!ln!
    ) else (
        for /f "tokens=1,2 delims==" %%b in ("!ln!") do (
            set currkey=%%b
            set currval=%%c
            if "x!area!"=="x!currarea!" if "x!key! "=="x!currkey!" (
                set val=!currval:~1!
            )
        )
    )
)
( endlocal & rem return
   Set "GAMEPATH=%val%"
)

: Set up Parameters
echo ===============Parameters==============
set DESTINATION=%~dp0%DESTINATION_SUBFOLDER%\
set CASCPROGRAM=%parentLevel%\tools\plugins\casc\CASCConsole.exe
set CASCSETTINGSPROG=%parentLevel%\tools\cascToolSettingEdit\CascExplorerConfigFileEdit.jar
set CASCSETTINGSFILE=%parentLevel%\tools\plugins\casc\CASCConsole.exe.config

echo GAMEPATH=%GAMEPATH%
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
@RD /S /Q %DESTINATION%\campaigns
echo 		OK.

: Extract Files from CASC (parallel execution from: https://stackoverflow.com/questions/12577442/waiting-for-parallel-batch-scripts )
echo echo | set /p=...EXTRACTING FILES...

setlocal
set "lock=%temp%\wait%random%.lock"
:: Launch one and two asynchronously, with stream 9 redirected to a lock file.
:: The lock file will remain locked until the script ends.
ping -n 1 127.0.0.1 >nul
start /MIN "Extracting Layouts" cmd /c 9>"%lock%1" "%CASCPROGRAM%" *.SC2Layout %DESTINATION% %LOCALE% None
ping -n 5 127.0.0.1 >nul
start /MIN "Extracting Assets" cmd /c 9>"%lock%2" "%CASCPROGRAM%" *Assets.txt %DESTINATION% %LOCALE% None
ping -n 5 127.0.0.1 >nul
start /MIN "Extracting Styles" cmd /c 9>"%lock%3" "%CASCPROGRAM%" *.SC2Style %DESTINATION% %LOCALE% None
ping -n 5 127.0.0.1 >nul
::start /WAIT /MIN "" "%CASCPROGRAM%" *.SC2Layout %DESTINATION% %LOCALE% None
::start /WAIT /MIN "" "%CASCPROGRAM%" *Assets.txt %DESTINATION% %LOCALE% None
::start /WAIT /MIN "" "%CASCPROGRAM%" *.SC2Style %DESTINATION% %LOCALE% None

:Wait for all processes to finish (wait until lock files are no longer locked)
1>nul 2>nul ping /n 2 ::1
for %%F in ("%lock%*") do (
  (call ) 9>"%%F" || goto :Wait
) 2>nul
::delete the lock files
del "%lock%*"

echo 	OK.

: Reset Configuration File
echo echo | set /p=...resetting configuration file...
start /WAIT /MIN cmd /K ""%CASCSETTINGSPROG%" "%CASCSETTINGSFILE%" "QQQ" "QQQ" "QQQ" "QQQ" & exit"
echo 	OK.

: Clear Debug output file from casc tool
del %~dp0debug.log

: pause