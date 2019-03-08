@echo off
if "x%JAVA_HOME%" == "x" (call set-env.bat)

echo build in netbeans!
echo.
::echo compiling spread
::call compile-spread.bat
::echo.

echo starting spread
call java net.technikumwien.bic4b18_01.spread.local.DaemonApplication

echo EOF
goto :eof
