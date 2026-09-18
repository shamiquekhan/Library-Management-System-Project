#!/usr/bin/env bash
#
# Run the Library Management System inside a detached `screen` session.
#
#   ./run-local.sh          start the app in a detached session named `lms`
#   screen -r lms           attach to it (Ctrl+A then D to detach again)
#
# The session keeps running while detached, so background services
# (overdue monitor, backup daemon) stay alive and you can re-attach
# from any terminal.
#
set -e
cd "$(dirname "$0")"

SESSION=lms

if screen -list 2>/dev/null | grep -q "\.${SESSION}\b"; then
  echo "Session '$SESSION' is already running."
  echo "Attach with:  screen -r $SESSION"
  exit 0
fi

# Rebuild if the build output is missing
if [ ! -d build ]; then
  ./build.sh
fi

screen -dmS "$SESSION" bash -c 'java -cp "build:lib/*" lms.Main --cli 2>&1 | tee /tmp/lms_live.log'
sleep 2
echo "Library Management System started in detached screen session '$SESSION'."
echo "Attach with:            screen -r $SESSION"
echo "Detach again:           Ctrl+A then D"
echo "Stop the app:           attach, then choose 0. Exit"
echo "Kill session forcibly:  screen -S $SESSION -X quit"
