#!/bin/bash
levelDir="./g5/src/main/resources/levels"
classesDir="./g5/target/classes"
clientName="dtu.aimas.SearchClient"
arguments="-cbs"
flags="-g"

java -jar ./mavis.jar -l "$levelDir/TunnelWithBox.lvl" -c "java -cp $classesDir $clientName $arguments" $flags