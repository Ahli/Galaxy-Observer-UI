; example1.nsi
;
; This script is perhaps one of the simplest NSIs you can make. All of the
; optional settings are left to their default settings. The installer simply 
; prompts the user asking them where to install, and drops a copy of "MyProg.exe"
; there.

;--------------------------------

; The name of the installer
Name "Observer UI Settings Editor"

; The file to write
OutFile "Observer UI Settings Editor Setup.exe"

; The default installation directory
InstallDir $PROGRAMFILES\Observer UI Settings Editor

; The text to prompt the user to enter a directory
DirText "This will install Ahli's Observer UI Settings Editor on your computer. Choose a directory!"

;--------------------------------

; The stuff to install
Section "" ;No components page, name is not important

; Set output path to the installation directory.
SetOutPath $INSTDIR

; Put file there
File MyProg.exe

SectionEnd ; end the section