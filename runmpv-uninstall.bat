@echo off
setlocal enableextensions enabledelayedexpansion
path %SystemRoot%\System32;%SystemRoot%;%SystemRoot%\System32\Wbem

:: Unattended install flag. When set, the script will not require user input.
set unattended=no
if "%1"=="/u" set unattended=yes

:: Make sure the script is running as admin
call :ensure_admin

:: Delete "App Paths" entry
reg delete "HKLM\SOFTWARE\Microsoft\Windows\CurrentVersion\App Paths\runmpv.exe" /f >nul

:: Delete HKCR subkeys
set classes_root_key=HKLM\SOFTWARE\Classes
reg delete "%classes_root_key%\Applications\runmpv.exe" /f >nul
reg delete "%classes_root_key%\SystemFileAssociations\video\OpenWithList\runmpv.exe" /f >nul
:: reg delete "%classes_root_key%\SystemFileAssociations\audio\OpenWithList\runmpv.exe" /f >nul

:: Delete AutoPlay handlers
set autoplay_key=HKLM\SOFTWARE\Microsoft\Windows\CurrentVersion\Explorer\AutoplayHandlers
:: reg delete "%autoplay_key%\Handlers\MpvPlayDVDMovieOnArrival" /f >nul
:: reg delete "%autoplay_key%\EventHandlers\PlayDVDMovieOnArrival" /v "MpvPlayDVDMovieOnArrival" /f >nul
:: reg delete "%autoplay_key%\Handlers\MpvPlayBluRayOnArrival" /f >nul
:: reg delete "%autoplay_key%\EventHandlers\PlayBluRayOnArrival" /v "MpvPlayBluRayOnArrival" /f >nul

:: Delete "Default Programs" entry
reg delete "HKLM\SOFTWARE\RegisteredApplications" /v "runmpv" /f >nul
reg delete "HKLM\SOFTWARE\Clients\Media\runmpv\Capabilities" /f >nul

:: Delete all OpenWithProgIds referencing ProgIds that start with io.runmpv.
for /f "usebackq eol= delims=" %%k in (`reg query "%classes_root_key%" /f "io.runmpv.*" /s /v /c`) do (
	set "key=%%k"
	echo !key!| findstr /r /i "^HKEY_LOCAL_MACHINE\\SOFTWARE\\Classes\\\.[^\\][^\\]*\\OpenWithProgIds$" >nul
	if not errorlevel 1 (
		for /f "usebackq eol= tokens=1" %%v in (`reg query "!key!" /f "io.runmpv.*" /v /c`) do (
			set "value=%%v"
			echo !value!| findstr /r /i "^io\.runmpv\.[^\\][^\\]*$" >nul
			if not errorlevel 1 (
				echo Deleting !key!\!value!
				reg delete "!key!" /v "!value!" /f >nul
			)
		)
	)
)

:: Delete all ProgIds starting with io.runmpv.
for /f "usebackq eol= delims=" %%k in (`reg query "%classes_root_key%" /f "io.runmpv.*" /k /c`) do (
	set "key=%%k"
	echo !key!| findstr /r /i "^HKEY_LOCAL_MACHINE\\SOFTWARE\\Classes\\io\.runmpv\.[^\\][^\\]*$" >nul
	if not errorlevel 1 (
		echo Deleting !key!
		reg delete "!key!" /f >nul
	)
)

echo Uninstalled successfully
if [%unattended%] == [yes] exit 0
pause
exit 0

:die
	if not [%1] == [] echo %~1
	if [%unattended%] == [yes] exit 1
	pause
	exit 1

:ensure_admin
	:: 'openfiles' is just a command that is present on all supported Windows
	:: versions, requires admin privileges and has no side effects, see:
	:: https://stackoverflow.com/questions/4051883/batch-script-how-to-check-for-admin-rights
	openfiles >nul 2>&1
	if errorlevel 1 (
		echo This batch script requires administrator privileges. Right-click on
		echo runmpv-uninstall.bat and select "Run as administrator".
		call :die
	)
	goto :EOF
