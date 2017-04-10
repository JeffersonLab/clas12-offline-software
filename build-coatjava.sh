#!/bin/bash

mkdir coatjava
cp -r bin coatjava/
cp -r etc coatjava/
cp -r lib coatjava/
mkdir coatjava/lib/services

### coat-libs ###
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
cp common-tools/coat-lib/target/coat-libs*.jar coatjava/lib/clas/

### jcsg ###
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

### create local mvn repo containing coat-libs and jcsg ##
mvn deploy:deploy-file -Dfile=./common-tools/coat-lib/target/coat-libs-3.0-SNAPSHOT.jar -DgroupId=org.jlab.clas -DartifactId=common-tools -Dversion=0.0 -Dpackaging=jar -Durl=file:./myLocalMvnRepo/ -DrepositoryId=myLocalMvnRepo -DupdateReleaseInfo=true
if [ $? != 0 ]
then
	echo "failed to create local mvn repo"
	exit 1
fi
mvn deploy:deploy-file -Dfile=./common-tools/clas-jcsg/build/libs/jcsg-0.3.2.jar -DgroupId=org.jlab.clas -DartifactId=clas-jcsg -Dversion=0.0 -Dpackaging=jar -Durl=file:./myLocalMvnRepo/ -DrepositoryId=myLocalMvnRepo -DupdateReleaseInfo=true
if [ $? != 0 ]
then
	echo "failed to create local mvn repo"
	exit 1
fi
 
### dc (depends on jcsg) ###
cd reconstruction/dc
mvn install
if [ $? != 0 ]
then
	echo "dc failure"
	exit 1
fi
cd -
cp reconstruction/dc/target/clas12detector-dc-1.0-SNAPSHOT.jar coatjava/lib/services/

### add dc jar to local mvn repo ###
mvn deploy:deploy-file -Dfile=./reconstruction/dc/target/clas12detector-dc-1.0-SNAPSHOT.jar -DgroupId=org.jlab.service.dc -DartifactId=clas12detector-dc -Dversion=0.0 -Dpackaging=jar -Durl=file:./myLocalMvnRepo/ -DrepositoryId=myLocalMvnRepo -DupdateReleaseInfo=true
if [ $? != 0 ]
then
	echo "dc failure"
	exit 1
fi

### tof (depends on jcsg and dc) ###
cd reconstruction/tof
mvn install
if [ $? != 0 ]
then
	echo "tof failure"
	exit 1
fi
cd -
cp reconstruction/tof/target/tof-1.0-SNAPSHOT.jar coatjava/lib/services/

### cvt ###
cd reconstruction/cvt
mvn install
if [ $? != 0 ]
then
	echo "cvt failure"
	exit 1
fi
cd -
cp reconstruction/cvt/target/cvt-1.0-SNAPSHOT.jar coatjava/lib/services/

### end ###
echo "COATJAVA SUCCESSFULLY BUILT !"
