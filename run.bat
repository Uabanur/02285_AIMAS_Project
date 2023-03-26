@echo off
set levelDir=.\g5\src\main\resources\levels
set classesDir=.\g5\target\classes
set clientName=dtu.aimas.SearchClient
set arguments=-bfs -log debug servermessages
set flags=-g

java -jar .\mavis.jar -l "%levelDir%\SAD1.lvl" -c "java -cp %classesDir% %clientName% %arguments%" %flags%
