#!/bin/bash
#launch script for the event viewer in bCNU

#the root of clasJlib should be two dirs up
CLASJLIB="../.."

#make one entry for each required jar
JEVIO=$CLASJLIB/jevio/v4.1/lib/jevio-4.1.jar
BCNU=$CLASJLIB/bCNU/v1.0/lib/bCNU.jar

#append all necessary jars
CLASSPATH=$JEVIO:$BCNU

#spell out the class with main()
MAIN=cnuphys.bCNU.event.EventFrame

#whatever VM arguments you want
VMARG="-Xmx128M -Xss512k"

#run
java $VMARG -cp $CLASSPATH $MAIN 
