#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
mkdir -p build
find src -name "*.java" > build/sources.txt
javac -d build -cp "lib/*" @build/sources.txt
rm -f build/sources.txt

# Self-contained runnable jar: dist/LMS.jar + bundled JDBC driver.
mkdir -p dist/lib
cp lib/sqlite-jdbc-*.jar dist/lib/
JDBC_JAR=$(ls lib/sqlite-jdbc-*.jar | head -1 | xargs basename)
cat > build/manifest.mf <<EOF
Main-Class: lms.Main
Class-Path: lib/$JDBC_JAR
EOF
jar cfm dist/LMS.jar build/manifest.mf -C build .
echo "Build completed successfully. (dist/LMS.jar)"
