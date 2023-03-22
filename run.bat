@echo off
set levelDir=.\g5\src\main\resources\levels
set classesDir=.\g5\target\classes
set clientName=dtu.aimas.SearchClient
set arguments=-bfs -log debug servermessages

java -jar .\mavis.jar -l "%levelDir%\SAD1.lvl" -c "java -cp %classesDir% %clientName% %arguments%"
