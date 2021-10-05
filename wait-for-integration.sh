#!/bin/sh
TIMEOUT=180
COMMAND=$@
run_integration() {
  for i in `seq $TIMEOUT` ; do
    result=$(curl -s http://localhost:9028/stillingsimport/internal/isAlive)
    if [ "$result" = "OK" ] ; then
      if [ -n "$COMMAND" ] ; then
        exec $COMMAND
      fi
      exit 0
    fi
    sleep 1
  done
  echo "Timed out" >&2
  exit 1
}
run_integration
