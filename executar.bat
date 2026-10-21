@echo off
setlocal
cd /d "%~dp0"
call compilar.bat
if errorlevel 1 (
  pause
  exit /b 1
)
call scripts\java-env.bat
"%JAVA_CMD%" -Dfile.encoding=UTF-8 -cp out Main
