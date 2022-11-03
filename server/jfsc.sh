#!/usr/bin/env bash
set -euo pipefail
IFS=$'\r\n'

rm -rf bin/
javac -d bin src/realm/jnfss/serv/*.java src/vortex/jnfss/*.java -Xlint:deprecation

