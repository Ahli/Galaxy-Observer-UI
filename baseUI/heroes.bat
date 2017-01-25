@echo off
: Store Paths
set DESTINATION=%~dp0\heroes2\
echo DESTINATION=%DESTINATION%
pushd ..
set parentLevel=%cd%
echo parentLevel=%parentLevel%
popd
set CASCPROGRAM=%parentLevel%\tools\plugins\casc\CASCConsole.exe
echo CASCPROGRAM=%CASCPROGRAM%

: Remove Directory
@RD /S /Q %DESTINATION%\mods

: Extract
start /MIN "" "%CASCPROGRAM%" *.StormLayout %DESTINATION% enUS None
start /MIN "" "%CASCPROGRAM%" *Assets.txt %DESTINATION% enUS None
start /MIN "" "%CASCPROGRAM%" *.StormStyle %DESTINATION% enUS None

pause
