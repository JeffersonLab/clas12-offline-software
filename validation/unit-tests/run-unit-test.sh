#!/usr/bin/env dash

pathToTest=$1
testName=${pathToTest##*/}
testName=${testName%.java}
package=`head -1 $pathToTest | awk '{print$2}' | sed 's/;//'`

# coatjava must already be built at ../../coatjava/
COAT="../../coatjava/"
classPath="$COAT/lib/services/*:$COAT/lib/clas/*:$COAT/lib/utils/*:../lib/*:src/"

# compile the test:
javac -cp $classPath $pathToTest
if [ $? != 0 ] ; then echo "$testName compilation failure" ; exit 1 ; fi

# run the test:
java -DCLAS12DIR="$COAT" -Xmx1536m -Xms1024m -cp $classPath org.junit.runner.JUnitCore $package.$testName
if [ $? != 0 ] ; then echo "$package unit test failure" ; exit 1 ; else echo "$package passed unit tests" ; fi

