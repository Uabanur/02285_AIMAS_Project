#!/bin/bash

levelDir="./g5/src/main/resources/levels"
classesDir="./g5/target/classes"
clientName="dtu.aimas.SearchClient"
arguments="-cbs -log debug servermessages"
flags="-g"

java -jar ./mavis.jar -l "$levelDir/SAD1.lvl" -c "java -cp $classesDir $clientName $arguments" $flags