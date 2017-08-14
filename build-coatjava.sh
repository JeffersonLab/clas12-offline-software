#!/bin/bash

mkdir -p coatjava
cp -r bin coatjava/
cp -r etc coatjava/
mkdir -p coatjava/lib/clas
cp external-dependencies/JEventViewer-1.1.jar coatjava/lib/clas/
cp external-dependencies/vecmath-1.3.1-2.jar coatjava/lib/clas/
mkdir -p coatjava/lib/utils
cp external-dependencies/jclara-4.3-SNAPSHOT.jar coatjava/lib/utils
cp external-dependencies/KPP-Monitoring-1.0.jar coatjava/lib/utils
cp external-dependencies/KPP-Plots-1.0.jar coatjava/lib/utils
mkdir -p coatjava/lib/services

### clean up any cache copies ###
rm -rf ~/.m2/repository/org/hep/hipo
rm -rf ~/.m2/repository/org/jlab

### coat-libs ###
cd common-tools
mvn install
if [ $? != 0 ] ; then echo "common tools failure 1" ; exit 1 ; fi
cd -
cd common-tools/coat-lib
mvn package
if [ $? != 0 ] ; then echo "common tools failure 2" ; exit 1 ; fi
cd -
cp common-tools/coat-lib/target/coat-libs*.jar coatjava/lib/clas/

### jcsg ###
export COATJAVA=$PWD/coatjava/
cd common-tools/clas-jcsg
./gradlew assemble
if [ $? != 0 ] ; then echo "jcsg failure" ; exit 1 ; fi
cd -
cp common-tools/clas-jcsg/build/libs/jcsg-0.3.2.jar coatjava/lib/clas/

### create local mvn repo containing coat-libs and jcsg ##
mvn deploy:deploy-file -Dfile=./common-tools/coat-lib/target/coat-libs-3.0-SNAPSHOT.jar -DgroupId=org.jlab.clas -DartifactId=common-tools -Dversion=0.0 -Dpackaging=jar -Durl=file:./myLocalMvnRepo/ -DrepositoryId=myLocalMvnRepo -DupdateReleaseInfo=true
if [ $? != 0 ] ; then echo "failed to create local mvn repo" ; exit 1 ; fi
mvn deploy:deploy-file -Dfile=./common-tools/clas-jcsg/build/libs/jcsg-0.3.2.jar -DgroupId=org.jlab.clas -DartifactId=clas-jcsg -Dversion=0.0 -Dpackaging=jar -Durl=file:./myLocalMvnRepo/ -DrepositoryId=myLocalMvnRepo -DupdateReleaseInfo=true
if [ $? != 0 ] ; then echo "failed to create local mvn repo" ; exit 1 ; fi
 
### dc (depends on jcsg) ###
cd reconstruction/dc
mvn install
if [ $? != 0 ] ; then echo "dc failure" ; exit 1 ; fi
cd -
cp reconstruction/dc/target/clas12detector-dc-1.0-SNAPSHOT.jar coatjava/lib/services/

### add dc jar to local mvn repo ###
mvn deploy:deploy-file -Dfile=./reconstruction/dc/target/clas12detector-dc-1.0-SNAPSHOT.jar -DgroupId=org.jlab.service.dc -DartifactId=clas12detector-dc -Dversion=0.0 -Dpackaging=jar -Durl=file:./myLocalMvnRepo/ -DrepositoryId=myLocalMvnRepo -DupdateReleaseInfo=true
if [ $? != 0 ] ; then echo "dc failure" ; exit 1 ; fi

### tof (depends on jcsg and dc) ###
cd reconstruction/tof
mvn install
if [ $? != 0 ] ; then echo "tof failure" ; exit 1 ; fi
cd -
cp reconstruction/tof/target/tof-1.0-SNAPSHOT.jar coatjava/lib/services/

### cvt ###
cd reconstruction/cvt
mvn install
if [ $? != 0 ] ; then echo "cvt failure" ; exit 1 ; fi
cd -
cp reconstruction/cvt/target/cvt-1.0-SNAPSHOT.jar coatjava/lib/services/

### ft ###
cd reconstruction/ft
mvn install
if [ $? != 0 ] ; then echo "ft failure" ; exit 1 ; fi
cd -
cp reconstruction/ft/target/clas12detector-ft-1.0-SNAPSHOT.jar coatjava/lib/services/

### ec ###
cd reconstruction/ec
mvn install
if [ $? != 0 ] ; then echo "ec failure" ; exit 1 ; fi
cd -
cp reconstruction/ec/target/clas12detector-ec-1.0-SNAPSHOT.jar coatjava/lib/services/

### ltcc ###
cd reconstruction/ltcc
mvn install
if [ $? != 0 ] ; then echo "ltcc failure" ; exit 1 ; fi
cd -
cp reconstruction/ltcc/target/clasrec-ltcc-1.0-SNAPSHOT.jar coatjava/lib/services/

### htcc ###
cd reconstruction/htcc
mvn install
if [ $? != 0 ] ; then echo "htcc failure" ; exit 1 ; fi
cd -
cp reconstruction/htcc/target/clasrec-htcc-1.0-SNAPSHOT.jar coatjava/lib/services/

### eb ###
cd reconstruction/eb
mvn install
if [ $? != 0 ] ; then echo "eb failure" ; exit 1 ; fi
cd -
cp reconstruction/eb/target/clas12detector-eb-1.0-SNAPSHOT.jar coatjava/lib/services/

### end ###
echo "COATJAVA SUCCESSFULLY BUILT !"
