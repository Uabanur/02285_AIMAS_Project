@echo off

::: Get unique folder name for log outputs
: parse current date
set CUR_YYYY=%date:~10,4%
set CUR_MM=%date:~4,2%
set CUR_DD=%date:~7,2%

: parse current time
set CUR_HH=%time:~0,2%
if %CUR_HH% lss 10 (set CUR_HH=0%time:~1,1%)
 
set CUR_NN=%time:~3,2%
set CUR_SS=%time:~6,2%
set CUR_MS=%time:~9,2%

set DATE_AND_TIME_SUFFIX=%CUR_YYYY%%CUR_MM%%CUR_DD%-%CUR_HH%%CUR_NN%%CUR_SS%
set LOG_NAME="output_%DATE_AND_TIME_SUFFIX%.zip"
set LOG_DIR="server_logs"

if not exist %LOG_DIR% mkdir %LOG_DIR% > NUL
set OUTPUT_PATH="%LOG_DIR%/%LOG_NAME%"

set levelDir=.\g5\src\main\resources\levels\complevels
set classesDir=.\g5\target\classes
set clientName=dtu.aimas.SearchClient
set arguments=-safepath region:color:agent:guided -log debug

: -l            level file or directory
: -g            start in gui mode
: -f            full screen
: -t <seconds>  time limit
: -o            output log(s)

set flags=-t 180 -o %OUTPUT_PATH%
java -jar .\mavis.jar -l "%levelDir%" -c "java -cp %classesDir% %clientName% %arguments%" %flags%
