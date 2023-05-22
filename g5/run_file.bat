@echo off

set /p file=Choose level file: 
set flags=-g -f
java -jar .\server.jar -l %file% -c "java -jar g5-1.0.jar" %flags%
