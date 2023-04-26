#!/bin/bash
levelDir="./g5/src/main/resources/levels"
classesDir="./g5/target/classes"
clientName="dtu.aimas.SearchClient"
arguments="-cbs"
flags="-g"
debugFlag="-agentlib:jdwp=transport=dt_socket,server=y,address=8000"

java -jar ./mavis.jar -l "$levelDir/FollowPushConflict.lvl" -c "java -cp $classesDir $clientName $arguments" $flags