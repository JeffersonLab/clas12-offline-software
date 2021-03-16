#!/bin/bash

usage='build-coatjava.sh [--quiet] [--spotbugs] [--nomaps] [--unittests]'

quiet="no"
runSpotBugs="no"
downloadMaps="yes"
runUnitTests="no"
for xx in $@
do
    if [ "$xx" == "--spotbugs" ]
    then
        runSpotBugs="yes"
    elif [ "$xx" == "-n" ]
    then
        runSpotBugs="no"
    elif [ "$xx" == "--nomaps" ]
    then
        downloadMaps="no"
    elif [ "$xx" == "--unittests" ]
    then
        runUnitTests="yes"
    elif [ "$xx" == "--quiet" ]
    then
        quiet="yes"
    else
        echo $usage
        exit
    fi
done

wget='wget'
mvn='mvn'
if [ "$quiet" == "yes" ]
then
    wget='wget --progress=dot:mega'
    mvn='mvn -q -B'
fi

# download the default field maps, as defined in bin/env.sh:
# (and duplicated in etc/services/reconstruction.yaml):
source `dirname $0`/bin/env.sh
if [ $downloadMaps == "yes" ]; then
  echo 'Retrieving field maps ...'
  webDir=http://129.57.64.108/clas12offline/magfield
  locDir=etc/data/magfield
  mkdir -p $locDir
  cd $locDir
  for map in $COAT_MAGFIELD_SOLENOIDMAP $COAT_MAGFIELD_TORUSMAP $COAT_MAGFIELD_TORUSSECONDARYMAP
  do
    # -N only redownloads if timestamp/filesize is newer/different
    $wget -N --no-check-certificate $webDir/$map
  done
  cd -
fi

rm -rf coatjava
mkdir -p coatjava
cp -r bin coatjava/
cp -r etc coatjava/
# create schema directories for partial reconstruction outputs		
python etc/bankdefs/util/bankSplit.py coatjava/etc/bankdefs/hipo4 || exit 1
mkdir -p coatjava/lib/clas
cp external-dependencies/JEventViewer-1.1.jar coatjava/lib/clas/
cp external-dependencies/vecmath-1.3.1-2.jar coatjava/lib/clas/
mkdir -p coatjava/lib/utils
cp external-dependencies/jclara-4.3-SNAPSHOT.jar coatjava/lib/utils
cp external-dependencies/clas12mon-3.1.jar coatjava/lib/utils
cp external-dependencies/KPP-Plots-3.2.jar coatjava/lib/utils
#cp external-dependencies/jaw-1.0.jar coatjava/lib/utils
mkdir -p coatjava/lib/services

### clean up any cache copies ###
rm -rf ~/.m2/repository/org/hep/hipo
rm -rf ~/.m2/repository/org/jlab
cd common-tools/coat-lib; $mvn clean; cd -

unset CLAS12DIR
if [ $runUnitTests == "yes" ]; then
	$mvn install # also runs unit tests
	if [ $? != 0 ] ; then echo "mvn install failure" ; exit 1 ; fi
else
	$mvn -Dmaven.test.skip=true install
	if [ $? != 0 ] ; then echo "mvn install failure" ; exit 1 ; fi
fi

if [ $runSpotBugs == "yes" ]; then
	# mvn com.github.spotbugs:spotbugs-maven-plugin:spotbugs # spotbugs goal produces a report target/spotbugsXml.xml for each module
	$mvn com.github.spotbugs:spotbugs-maven-plugin:check # check goal produces a report and produces build failed if bugs
	# the spotbugsXml.xml file is easiest read in a web browser
	# see http://spotbugs.readthedocs.io/en/latest/maven.html and https://spotbugs.github.io/spotbugs-maven-plugin/index.html for more info
	if [ $? != 0 ] ; then echo "spotbugs failure" ; exit 1 ; fi
fi

cd common-tools/coat-lib
$mvn package
if [ $? != 0 ] ; then echo "mvn package failure" ; exit 1 ; fi
cd -

cp common-tools/coat-lib/target/coat-libs-*-SNAPSHOT.jar coatjava/lib/clas/
cp reconstruction/*/target/clas12detector-*-SNAPSHOT.jar coatjava/lib/services/
cp etc/data/T2D_DeltaDoca.txt coatjava/etc/data/T2D_DeltaDoca.txt 

echo "COATJAVA SUCCESSFULLY BUILT !"
