#!/usr/bin/env bash
set -euo pipefail
IFS='$\r\n'

javadoc -sourcepath src -subpackages vortex.jnfss realm.jnfss.serv -d docs

