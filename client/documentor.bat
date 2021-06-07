@echo OFF
dir
javadoc -subpackages realm -d docs/docr
javadoc -subpackages vortex -d docs/docv
pause
