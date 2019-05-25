@echo off
: Batch Script 
: author: @AhliSC2

: requires NSIS to be installed: https://sourceforge.net/projects/nsis/
DEL "Observer UI Settings Editor Setup.exe" /s /q
DEL "../project/installer/content/vm" /s /q
xcopy "../project/target/jlink-image" "../project/installer/content/vm" /i /r /y /s
"C:\Program Files (x86)\NSIS\makensis.exe" ../project/installer/nsis_script.nsi

pause
