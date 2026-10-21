@echo off
rem Chamado pelos scripts da raiz. Prioriza o JDK portatil local, depois JAVA_HOME e PATH.
set "JAVA_CMD=java"
set "JAVAC_CMD=javac"
set "JAR_CMD=jar"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" (
  set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
  set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
  set "JAR_CMD=%JAVA_HOME%\bin\jar.exe"
)
for /d %%J in ("%~dp0..\.tools\jdk\*") do if exist "%%~fJ\bin\javac.exe" (
  set "JAVA_CMD=%%~fJ\bin\java.exe"
  set "JAVAC_CMD=%%~fJ\bin\javac.exe"
  set "JAR_CMD=%%~fJ\bin\jar.exe"
)
"%JAVAC_CMD%" -version >nul 2>&1
if errorlevel 1 (
  echo JDK nao encontrado. Instale o JDK 17 ou superior e configure JAVA_HOME ou PATH.
  echo https://adoptium.net/temurin/releases/?version=17
  exit /b 1
)
exit /b 0
