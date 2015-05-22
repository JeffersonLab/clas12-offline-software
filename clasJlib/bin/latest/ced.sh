#!/bin/bash
#launch script for ced

#the root of clasJlib should be two dirs up
CLASJLIB="../.."

#make one entry for each required jar
JEVIO=$CLASJLIB/jevio/v4.3.1/lib/jevio-4.3.1.jar
BCNU=$CLASJLIB/bCNU/v1.0/lib/bCNU.jar
IMG=$CLASJLIB/bCNU/v1.0/lib/bcnuimages.jar
CED=$CLASJLIB/ced/v1.0/lib/ced.jar
BANKDEF=$CLASJLIB/bankDictionary/v1.0/lib/bankDictionary.jar
MAGFIELD=$CLASJLIB/magfield/v1.0/lib/magfield.jar
SWIMMER=$CLASJLIB/swimmer/v1.0/lib/swimmer.jar
SPLOT=$CLASJLIB/splot/v1.0/lib/splot.jar
NR1=$CLASJLIB/numRec/v3.3/lib/numRec.jar
NR2=$CLASJLIB/numRec/v3.3/lib/f2jutil.jar
ET=$CLASJLIB/et/v14.0/lib/et-14.0.jar

#append all necessary jars
CLASSPATH=$JEVIO:$BCNU:$IMG:$BANKDEF:$ET:$MAGFIELD:$SWIMMER:$SPLOT:$NR1:$NR2:$CED

echo $CLASSPATH

#spell out the class with main()
MAIN=cnuphys.ced.frame.Ced

#whatever VM arguments you want
VMARG="-Xmx128M -Xss512k"

#data dirpath where field maps, etc live
TORUS=$CLASJLIB/data/torus/v1.0
SOLENOID=$CLASJLIB/data/solenoid/v1.0
CEDDATA=$CLASJLIB/ced/v1.0/data
DATA=$TORUS:$SOLENOID:$CEDDATA

#whatever command arguments you want
ARG="-3d -dataPath $DATA"
echo $ARG

#run
java $VMARG -cp $CLASSPATH $MAIN $ARG 
