@echo off
setlocal
set "PROJECT_ROOT=%~dp0"
set "MAVEN_HOME=%PROJECT_ROOT%.tools\apache-maven-3.9.16"
set "MAVEN_CMD=%MAVEN_HOME%\bin\mvn.cmd"

if not exist "%MAVEN_CMD%" (
  echo Maven not found: "%MAVEN_CMD%"
  exit /b 1
)

if not defined JAVA_HOME (
  for /d %%J in ("%ProgramFiles%\JetBrains\IntelliJ IDEA*\jbr") do (
    if exist "%%~fJ\bin\java.exe" set "JAVA_HOME=%%~fJ"
  )
)

if not defined JAVA_HOME (
  echo JAVA_HOME is not set and IntelliJ JBR was not found.
  exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"
call "%MAVEN_CMD%" %*
