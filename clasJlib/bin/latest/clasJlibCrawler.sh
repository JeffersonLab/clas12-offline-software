#!/bin/bash
#launch script for the class crawler

#the root of clasJlib should be two dirs up
CLASJLIB="../.."

#make one entry for each required jar
BCNU=$CLASJLIB/bCNU/v1.0/lib/bCNU.jar
CCRAW=$CLASJLIB/clasJlibCrawler/v1.0/lib/clasJlibCrawler.jar

#append all necessary jars
CLASSPATH=$BCNU:$CCRAW

echo $CLASSPATH

#spell out the class with main()
MAIN=cnuphys.crawler.Crawler

#whatever VM arguments you want
VMARG="-Xmx128M -Xss512k"

#whatever command arguments you want
ARG=""

#run
java $VMARG -cp $CLASSPATH $MAIN $ARG 
