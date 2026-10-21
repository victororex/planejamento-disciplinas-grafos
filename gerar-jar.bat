@echo off
setlocal
cd /d "%~dp0"
call compilar.bat
if errorlevel 1 exit /b 1
call scripts\java-env.bat
if not exist dist mkdir dist
if not exist .tmp mkdir .tmp
"%JAR_CMD%" -J-Djava.io.tmpdir=.tmp --create --file dist\planejamento-disciplinas.jar --main-class Main -C out .
if errorlevel 1 exit /b 1
echo Criado: dist\planejamento-disciplinas.jar
echo Execute com: java -Dfile.encoding=UTF-8 -jar dist\planejamento-disciplinas.jar
