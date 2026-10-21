@echo off
setlocal
cd /d "%~dp0"
call compilar.bat
if errorlevel 1 exit /b 1
call scripts\java-env.bat
if not exist .tmp mkdir .tmp
"%JAVA_CMD%" -Dfile.encoding=UTF-8 -Djava.io.tmpdir=.tmp -cp out TestesAlgoritmos
exit /b %errorlevel%
