#!/bin/bash

mkdir coatjava
cp -r bin coatjava/
cp -r etc coatjava/
cp -r lib coatjava/
ls coatjava/lib/services/
rm coatjava/lib/services/*.jar
ls coatjava/lib/services/

# coat-libs
rm -rf ~/.m2/repository/org/hep/hipo
rm -rf ~/.m2/repository/org/jlab/groot
cd common-tools
mvn install
if [ $? != 0 ]
then
	echo "common tools failure 1"
	exit 1
fi
cd -
cd common-tools/coat-lib
mvn package
if [ $? != 0 ]
then
	echo "common tools failure 2"
	exit 1
fi
cd -
rm coatjava/lib/clas/coat-libs*.jar
cp common-tools/coat-lib/target/coat-libs*.jar coatjava/lib/clas/

# jcsg
export COATJAVA=$PWD/coatjava/
cd common-tools/clas-jcsg
./gradlew assemble
if [ $? != 0 ]
then
	echo "jcsg failure"
	exit 1
fi
cd -
cp common-tools/clas-jcsg/build/libs/jcsg-0.3.2.jar coatjava/lib/clas/
 
# dc (depends on jcsg)
cd reconstruction/dc
mvn install
if [ $? != 0 ]
then
	echo "dc failure"
	exit 1
fi
cd -
cp reconstruction/dc/target/clas12detector-dc-1.0-SNAPSHOT.jar coatjava/lib/services/

# tof (depends on jcsg and dc)
cd reconstruction/tof
mvn install
if [ $? != 0 ]
then
	echo "tof failure"
	exit 1
fi
cd -
cp reconstruction/tof/target/tof-1.0-SNAPSHOT.jar coatjava/lib/services/

# cvt
cd reconstruction/cvt
mvn install
if [ $? != 0 ]
then
	echo "cvt failure"
	exit 1
fi
cd -
cp reconstruction/cvt/target/cvt-1.0-SNAPSHOT.jar coatjava/lib/services/
