@echo off
cd /d %~dp0
java -cp "%~dp0build;%~dp0lib\*" lms.Main %*