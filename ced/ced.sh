#!/bin/bash
#launch script for ced

#make one entry for each required jar
COAT=../coatjava/lib/clas/coat-libs-2.0-SNAPSHOT.jar
CED=../lib/ced.jar
CNU=../lib/cnuphys.jar
JOGL=../JOGL/gluegen-rt.jar:../JOGL/jogl-all.jar

#append all necessary jars
CP=$COAT:$CNU:$JOGL:$CED

echo $CP

#spell out the class with main()
MAIN=cnuphys.ced.frame.Ced

#whatever VM arguments you want
VMARG="-Xmx1000M -Xss512k -DCLAS12DIR=../coatjava"

#data dirpath where field maps, etc live
TORUS=../data/clas12_torus_fieldmap_binary.dat
echo $TORUS
SOLENOID=../data/solenoid-srr.dat
echo $SOLENOID

#whatever command arguments you want
ARG="-torus $TORUS -solenoid $SOLENOID"
echo $ARG

#run
java $VMARG -cp $CP $MAIN $ARG
