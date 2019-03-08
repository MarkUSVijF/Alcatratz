@echo off
if "x%JAVA_HOME%" == "x" (call set-env.bat)

echo build in netbeans!
echo.
::echo compiling common
::call compile-common.bat
::echo compiling client
::call compile-client.bat
::echo.

echo starting client
call java net.technikumwien.bic4b18_01.client.local.GUI

echo EOF
pause
goto :eof
