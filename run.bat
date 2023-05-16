@echo off
set levelDir=.\g5\src\main\resources\levels
set classesDir=.\g5\target\classes
set clientName=dtu.aimas.SearchClient
set arguments=-safepath split:region
set flags=-g -f
set level=mishmash.lvl
java -jar .\mavis.jar -l "%levelDir%\%level%" -c "java -cp %classesDir% %clientName% %arguments%" %flags%
