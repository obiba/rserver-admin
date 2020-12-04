@echo off

if "%JAVA_OPTS%" == "" goto DEFAULT_JAVA_OPTS

:INVOKE
echo JAVA_HOME=%JAVA_HOME%
echo JAVA_OPTS=%JAVA_OPTS%
echo RSERVER_HOME=%RSERVER_HOME%

if "%RSERVER_HOME%" == "" goto RSERVER_HOME_NOT_SET

setlocal ENABLEDELAYEDEXPANSION

set RSERVER_DIST=%~dp0..
echo RSERVER_DIST=%RSERVER_DIST%

set RSERVER_LOG=%RSERVER_HOME%\logs
IF NOT EXIST "%RSERVER_LOG%" mkdir "%RSERVER_LOG%"
echo RSERVER_LOG=%RSERVER_LOG%

set CLASSPATH=%RSERVER_HOME%\conf;%RSERVER_DIST%\lib\*

set JAVA_DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n

rem Add %JAVA_DEBUG% to this line to enable remote JVM debugging (for developers)
java %JAVA_OPTS% -cp "%CLASSPATH%" -DRSERVER_HOME="%RSERVER_HOME%" -DRSERVER_DIST=%RSERVER_DIST% org.springframework.boot.loader.JarLauncher %*
goto :END

:DEFAULT_JAVA_OPTS
set JAVA_OPTS=-Xms1G -Xmx2G -XX:MaxPermSize=256M -XX:+UseG1GC
goto :INVOKE

:JAVA_HOME_NOT_SET
echo JAVA_HOME not set
goto :END

:RSERVER_HOME_NOT_SET
echo RSERVER_HOME not set
goto :END

:END
