#!/bin/sh -f

echo ------------------------------------
echo run-unit-tests.sh
echo ------------------------------------

# coatjava must already be built at ../../coatjava/

# set environment
COAT="../../coatjava/"
CLASSPATH="$COAT/lib/services/*:$COAT/lib/clas/*:$COAT/lib/utils/*:../lib/*:src/"

function compileTest 
{
    echo TravisCompileTest $1
    path=$1
    stub=${path##*/}
    stub=${stub%%.java}
    echo javac -cp $CLASSPATH $path
    javac -cp $CLASSPATH $path
    if [ $? != 0 ]
    then
        echo "$stub compilation failure"
        exit 1 
    fi
}

function runTest
{
    echo TravisRunTest $1
    class=$1
    stub=${class%%.*}
    echo java -DCLAS12DIR="$COAT" -Xmx1536m -Xms1024m -cp $CLASSPATH org.junit.runner.JUnitCore $class
    java -DCLAS12DIR="$COAT" -Xmx1536m -Xms1024m -cp $CLASSPATH org.junit.runner.JUnitCore $class
    if [ $? != 0 ]
    then
        echo "$stub unit test failure"
        exit 1
    else
        echo "$stub passed unit tests"
    fi
}

# compile codes:
compileTest src/events/TestEvent.java
compileTest src/dc/DCReconstructionTest.java
compileTest src/cvt/CVTReconstructionTest.java
compileTest src/ec/ECReconstructionTest.java
compileTest src/eb/EBReconstructionTest.java

# run unit tests:
runTest dc.DCReconstructionTest
runTest cvt.CVTReconstructionTest
runTest ec.ECReconstructionTest
runTest eb.EBReconstructionTest

