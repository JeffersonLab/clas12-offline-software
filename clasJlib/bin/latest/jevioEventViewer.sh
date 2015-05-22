#!/bin/bash
#launch script for the event viewer in jevio

#the root of clasJlib should be two dirs up
CLASJLIB="../.."

#make one entry for each required jar
JEVIO=$CLASJLIB/jevio/v4.1/lib/jevio-4.1.jar

#append all necessary jars
CLASSPATH=$JEVIO

#spell out the class with main()
MAIN=org.jlab.coda.jevio.graphics.EventTreeFrame

#whatever VM arguments you want
VMARG="-Xmx128M -Xss512k"

#run
java $VMARG -cp $CLASSPATH $MAIN 
