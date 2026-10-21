@echo off
setlocal
cd /d "%~dp0"
call scripts\java-env.bat
if errorlevel 1 exit /b 1
if not exist out mkdir out
"%JAVAC_CMD%" -encoding UTF-8 --release 17 -Xlint:all -d out src\*.java
if errorlevel 1 exit /b 1
echo Compilacao concluida.
