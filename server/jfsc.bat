@echo OFF
echo "compiling source..."
javac -d bin src/realm/jnfss/serv/*.java src/vortex/jnfss/*.java -Xlint:deprecation
pause
