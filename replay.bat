set /p LOG_FILE=Log file path: 
set flags=-g
java -jar .\mavis.jar -r %LOG_FILE% %flags%
