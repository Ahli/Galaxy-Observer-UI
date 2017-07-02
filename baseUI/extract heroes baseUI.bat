@echo off
: Batch Script to extract files from CASC
: author: @AhliSC2

: User Params:
set PRODUCT=hero
set LOCALE=enUS
set DESTINATION_SUBFOLDER=heroes


: Get GamePath from settings.ini
: code based on http://stackoverflow.com/questions/2866117/read-ini-from-windows-batch-file
pushd ..
set parentLevel=%cd%
popd
: code from http://stackoverflow.com/questions/2866117/read-ini-from-windows-batch-file
set file=%parentLevel%\settings.ini
set area=[GamePaths]
set key=Heroes_Path
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
            if "x!area!"=="x!currarea!" if "x!key!"=="x!currkey!" (
                set val=!currval!
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
set CASCSETTINGSPROG=%parentLevel%\tools\cascToolSettingEdit\cascExplorerSettingsEdit.jar
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
echo 		OK.

: Extract Files from CASC (parallel execution from: https://stackoverflow.com/questions/12577442/waiting-for-parallel-batch-scripts )
echo echo | set /p=...EXTRACTING FILES...

setlocal
set "lock=%temp%\wait%random%.lock"
:: Launch one and two asynchronously, with stream 9 redirected to a lock file.
:: The lock file will remain locked until the script ends.
ping -n 1 127.0.0.1 >nul
start /MIN "Extracting Layouts" cmd /c 9>"%lock%1" "%CASCPROGRAM%" *.StormLayout %DESTINATION% %LOCALE% None
ping -n 1 127.0.0.1 >nul
start /MIN "Extracting Assets" cmd /c 9>"%lock%2" "%CASCPROGRAM%" *Assets.txt %DESTINATION% %LOCALE% None
ping -n 1 127.0.0.1 >nul
start /MIN "Extracting Styles" cmd /c 9>"%lock%3" "%CASCPROGRAM%" *.StormStyle %DESTINATION% %LOCALE% None
ping -n 1 127.0.0.1 >nul
::start /WAIT /MIN "" "%CASCPROGRAM%" *.StormLayout %DESTINATION% %LOCALE% None
::start /WAIT /MIN "" "%CASCPROGRAM%" *Assets.txt %DESTINATION% %LOCALE% None
::start /WAIT /MIN "" "%CASCPROGRAM%" *.StormStyle %DESTINATION% %LOCALE% None

:: hack to wait 1 second to reduce chance that loop exits before all commands were started
ping -n 1 127.0.0.1 >nul

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


: Make Live Heroes active
set file=%parentLevel%\settings.ini
set findstr="PTRactive=true"
set replacestr="PTRactive=false"
call :FindReplace %findstr% %replacestr% %file%
exit /b 
: Functions from http://stackoverflow.com/questions/23087463/batch-script-to-find-and-replace-a-string-in-text-file-within-a-minute-for-files
:FindReplace <findstr> <replstr> <file>
set tmp="%temp%\tmpGalaxyObsUI.txt"
If not exist %temp%\_.vbs call :MakeReplace
for /f "tokens=*" %%a in ('dir "%3" /s /b /a-d /on') do (
  for /f "usebackq" %%b in (`Findstr /mic:"%~1" "%%a"`) do (
    echo(&Echo Replacing "%~1" with "%~2" in file %%~nxa
    <%%a cscript //nologo %temp%\_.vbs "%~1" "%~2">%tmp%
    if exist %tmp% move /Y %tmp% "%%~dpnxa">nul
  )
)
del %temp%\_.vbs
exit /b
:MakeReplace
>%temp%\_.vbs echo with Wscript
>>%temp%\_.vbs echo set args=.arguments
>>%temp%\_.vbs echo .StdOut.Write _
>>%temp%\_.vbs echo Replace(.StdIn.ReadAll,args(0),args(1),1,-1,1)
>>%temp%\_.vbs echo end with
exit /b


: pause
