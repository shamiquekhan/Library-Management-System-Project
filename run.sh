#!/usr/bin/env bash
DIR="$(cd "$(dirname "$0")" && pwd)"
exec java -cp "$DIR/build:$DIR/lib/*" lms.Main "$@"