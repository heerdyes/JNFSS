#!/usr/bin/env bash
set -euo pipefail
IFS=$'\r\n'

rm -rf bin/
javac -d bin src/vortex/jnfss/*.java src/realm/jnfss/ui/*.java src/realm/jnfss/comm/*.java src/realm/jnfss/serv/*.java src/realm/jnfss/ctrl/*.java src/realm/jnfss/sh/*.java src/realm/jnfss/*.java -Xlint:deprecation

