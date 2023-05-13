@echo off

set /p file=Choose level file: 
set classesDir=.\g5\target\classes
set clientName=dtu.aimas.SearchClient
set arguments=-si sub:ibs -log debug
set flags=-g -f
java -jar .\mavis.jar -l %file% -c "java -cp %classesDir% %clientName% %arguments%" %flags%
