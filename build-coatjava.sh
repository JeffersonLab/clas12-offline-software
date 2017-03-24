#!/bin/bash -f

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
./build.sh -m
cd -
rm coatjava/lib/clas/coat-libs*.jar
cp common-tools/target/coat-libs*.jar coatjava/lib/clas/

# jcsg
export COATJAVA=$PWD/coatjava/
cd common-tools/clas-jcsg
./gradlew assemble
cd -
exit

# cvt
cd reconstruction/cvt
mvn install
cd -
cp reconstruction/cvt/target/cvt-1.0-SNAPSHOT.jar coatjava/lib/services/

#tar and clean up
tar -zcvf coatjava.tar.gz coatjava
rm -rf coatjava
