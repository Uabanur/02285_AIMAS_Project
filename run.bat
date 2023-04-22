@echo off
set levelDir=.\g5\src\main\resources\levels
set classesDir=.\g5\target\classes
set clientName=dtu.aimas.SearchClient
set arguments=-bb
set flags=-g
set level=MAPF03C.lvl
java -jar .\mavis.jar -l "%levelDir%\%level%" -c "java -cp %classesDir% %clientName% %arguments%" %flags%
