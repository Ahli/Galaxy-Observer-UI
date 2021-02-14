; Galaxy Observer UI Settings Editor - NSIS Installer Creation Script
;---------------------------------------------------------------------
Unicode True
!define APPNAME "Observer UI Settings Editor"
!define COMPANYNAME "Ahli"
!define DESCRIPTION "App to edit Observer Interface files for StarCraft II and Heroes of the Storm."

; This is the size (in kB) of all the files copied into "Program Files"
!define INSTALLSIZE 60770

; The name of the installer
Name "${COMPANYNAME} - ${APPNAME}"
Icon "content/logo.ico"
LicenseData "license.rtf"

; The file to write
OutFile "Observer UI Settings Editor Setup.exe"

; The default installation directory
InstallDir "$PROGRAMFILES64\Observer UI Settings Editor"

; The text to prompt the user to enter a directory
DirText "This will install Ahli's Observer UI Settings Editor on your computer. Choose a directory!"

; Compression used
SetCompressor /SOLID lzma

;---------------------------------------------------------------------
Page license
Page directory
Page instfiles

; MACROS
!include LogicLib.nsh
!macro VerifyUserIsAdmin
UserInfo::GetAccountType
pop $0
${If} $0 != "admin" ;Require admin rights on NT4+
        messageBox mb_iconstop "Administrator rights required!"
        setErrorLevel 740 ;ERROR_ELEVATION_REQUIRED
        quit
${EndIf}
!macroend

Function .onInit
	SetShellVarContext all
	!insertmacro VerifyUserIsAdmin
FunctionEnd




;---------------------------------------------------------------------
Section "install"
	; Set output path to the installation directory.
	SetOutPath $INSTDIR
	; Extract content.
	File /r content\*.*
	
	; Uninstaller - See function un.onInit and section "uninstall" for configuration
	WriteUninstaller "$INSTDIR\uninstall.exe"

	; Start Menu
	CreateDirectory "$SMPROGRAMS\${COMPANYNAME}"
	CreateShortCut "$SMPROGRAMS\${COMPANYNAME}\${APPNAME}.lnk" "$INSTDIR\run Observer UI Settings Editor.bat" "" "$INSTDIR\logo.ico"
	
	; Desktop Shortcut
	CreateShortCut "$DESKTOP\${APPNAME}.lnk" "$INSTDIR\run Observer UI Settings Editor.bat" "" "$INSTDIR\logo.ico"
	
	; Registry information for add/remove programs
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "DisplayName" "${COMPANYNAME} - ${APPNAME} - ${DESCRIPTION}"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "UninstallString" "$\"$INSTDIR\uninstall.exe$\""
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "QuietUninstallString" "$\"$INSTDIR\uninstall.exe$\" /S"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "InstallLocation" "$\"$INSTDIR$\""
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "DisplayIcon" "$\"$INSTDIR\logo.ico$\""
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "Publisher" "$\"${COMPANYNAME}$\""
;	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "HelpLink" "$\"${HELPURL}$\""
;	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "URLUpdateInfo" "$\"${UPDATEURL}$\""
;	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "URLInfoAbout" "$\"${ABOUTURL}$\""
;	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "DisplayVersion" "$\"${VERSIONMAJOR}.${VERSIONMINOR}.${VERSIONBUILD}$\""
;	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "VersionMajor" ${VERSIONMAJOR}
;	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "VersionMinor" ${VERSIONMINOR}
	; There is no option for modifying or repairing the install
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "NoModify" 1
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "NoRepair" 1
	; Set the INSTALLSIZE constant (!defined at the top of this script) so Add/Remove Programs can accurately report the size
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}" "EstimatedSize" ${INSTALLSIZE}
SectionEnd
; -----------------------------------------------------



; Uninstaller
Function un.onInit
	SetShellVarContext all
 
	; Verify the uninstaller - last chance to back out
	MessageBox MB_OKCANCEL "Permanantly remove ${APPNAME}?" IDOK next
		Abort
	next:
	!insertmacro VerifyUserIsAdmin
FunctionEnd
 
Section "uninstall"
	; Remove Start Menu launcher
	Delete "$SMPROGRAMS\${COMPANYNAME}\${APPNAME}.lnk"
	; Try to remove the Start Menu folder - this will only happen if it is empty
	RMDir "$SMPROGRAMS\${COMPANYNAME}"
 
	; Remove files
	Delete "$INSTDIR\run Observer UI Settings Editor.bat"
	Delete $INSTDIR\logo.ico
	Delete $INSTDIR\ReadMe.txt
	RMDir /r $INSTDIR\plugins
	RMDir /r $INSTDIR\vm
	
	; Remove Desktop Shortcut
	Delete "$DESKTOP\${APPNAME}.lnk"
 
	; Always delete uninstaller as the last action
	Delete $INSTDIR\uninstall.exe
 
	; Try to remove the install directory - this will only happen if it is empty
	RMDir $INSTDIR
 
	; Remove uninstaller information from the registry
	DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${COMPANYNAME} ${APPNAME}"
SectionEnd

