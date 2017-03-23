#!/bin/csh -f

mkdir coatjava
cp -r bin coatjava/
cp -r etc coatjava/
cp -r lib coatjava/

# coat-libs
cd common-tools
./build.sh -m
cd -
rm coatjava/lib/clas/coat-libs*.jar
cp common-tools/target/coat-libs*.jar coatjava/lib/clas/

# cvt
cd reconstruction/cvt
mvn install
cd -
rm coatjava/lib/services/clasrec-cvt.jar
cp reconstruction/cvt/target/cvt-1.0-SNAPSHOT.jar coatjava/lib/services/

#tar and clean up
tar -zcvf coatjava.tar.gz coatjava
rm -rf coatjava
