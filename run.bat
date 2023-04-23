@echo off

set output=./server_logs/server.log
if exist %output% rm %output%

set levelDir=.\g5\src\main\resources\levels
set classesDir=.\g5\target\classes
set clientName=dtu.aimas.SearchClient
set arguments=-bb -log debug
set flags=-g -f -o %output%
set level=RowsOfAgentsAndBoxes.lvl


java -jar .\mavis.jar -l "%levelDir%\%level%" -c "java -cp %classesDir% %clientName% %arguments%" %flags%
