#!/bin/sh -f

# coatjava must already be built at ../../coatjava/

# set environment
COAT="../../coatjava/"
classPath="$COAT/lib/services/*:$COAT/lib/clas/*:$COAT/lib/utils/*:../lib/*:src/"


# compile codes
javac -cp $classPath src/events/TestEvent.java
if [ $? != 0 ] ; then echo "TestEvent compilation failure" ; exit 1 ; fi

javac -cp $classPath src/events/RandomEventGenerator.java
if [ $? != 0 ] ; then echo "RandomEventGenerator compilation failure" ; exit 1 ; fi

javac -cp $classPath src/dc/DCReconstructionTest.java
if [ $? != 0 ] ; then echo "DCReconstructionTest compilation failure" ; exit 1 ; fi

javac -cp $classPath src/ec/ECReconstructionTest.java
if [ $? != 0 ] ; then echo "ECReconstructionTest compilation failure" ; exit 1 ; fi


# run dc junit tests
java -DCLAS12DIR="$COAT" -Xmx1536m -Xms1024m -cp $classPath org.junit.runner.JUnitCore dc.DCReconstructionTest
if [ $? != 0 ] ; then echo "dc unit test failure" ; exit 1 ; else echo "dc passed unit tests" ; fi

# run ec junit tests
java -DCLAS12DIR="$COAT" -Xmx1536m -Xms1024m -cp $classPath org.junit.runner.JUnitCore ec.ECReconstructionTest
if [ $? != 0 ] ; then echo "ec unit test failure" ; exit 1 ; else echo "ec passed unit tests" ; fi


# run event generator
#java -DCLAS12DIR="$COAT" -Xmx1536m -Xms1024m -cp $classPath events.RandomEventGenerator
