@echo off
cd /d %~dp0
if not exist build mkdir build
dir /s /b src\*.java > build\sources.txt
javac -d build -cp "lib\*" @build\sources.txt
del build\sources.txt
echo Build completed successfully.