#!/usr/bin/env bash
set -euo pipefail
IFS=$'\r\n'

jar -cvf jnfss.jar -C bin .

