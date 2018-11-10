@echo off
: Batch Script 
: author: @AhliSC2

: use "vm\bin\java.exe" if the App is failing to start to see error info
start "" "vm\bin\javaw.exe" -m ObserverUiSettingsEditor/com.ahli.hotkeyUi.application.Main
