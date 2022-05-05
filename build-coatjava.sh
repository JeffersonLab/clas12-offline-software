#!/bin/bash

usage='build-coatjava.sh [-h] [--quiet] [--spotbugs] [--nomaps] [--unittests]'

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
        echo "$usage"
        exit
    fi
done

top="$(cd "$(dirname "${BASH_SOURCE[0]}")" &> /dev/null && pwd)"

wget='wget'
mvn="mvn --settings $top/maven-settings.xml"
if [ "$quiet" == "yes" ]
then
    wget='wget --progress=dot:mega'
    mvn="mvn -q -B --settings $top/maven-settings.xml"
fi

command_exists () {
    type "$1" &> /dev/null
}
download () {
    ret=0
    if command_exists wget ; then
        # -N only redownloads if timestamp/filesize is newer/different
        $wget -N --no-check-certificate $1
        ret=$?
    elif command_exists curl ; then
        if ! [ -e ${1##*/} ]; then
          curl $1 -o ${1##*/}
          ret=$?
        fi
    else
        ret=1
        echo ERROR:::::::::::  Could not find wget nor curl.
    fi
    return $ret
}


# download the default field maps, as defined in bin/env.sh:
# (and duplicated in etc/services/reconstruction.yaml):
source `dirname $0`/bin/env.sh
if [ $downloadMaps == "yes" ]; then
  echo 'Retrieving field maps ...'
  webDir=https://clasweb.jlab.org/clas12offline/magfield
  locDir=etc/data/magfield
  mkdir -p $locDir
  cd $locDir
  for map in $COAT_MAGFIELD_SOLENOIDMAP $COAT_MAGFIELD_TORUSMAP $COAT_MAGFIELD_TORUSSECONDARYMAP
  do
    download $webDir/$map
    if [ $? -ne 0 ]; then
        echo ERROR:::::::::::  Could not download field map:
        echo $webDir/$map
        echo One option is to download manually into etc/data/magfield and then run this build script with --nomaps
        exit
    fi
  done
  cd -
fi

rm -rf coatjava
mkdir -p coatjava
cp -r bin coatjava/
cp -r etc coatjava/
# create schema directories for partial reconstruction outputs		
which python >& /dev/null && python=python || python=python3
$python etc/bankdefs/util/bankSplit.py coatjava/etc/bankdefs/hipo4 || exit 1
mkdir -p coatjava/lib/clas
cp external-dependencies/JEventViewer-1.1.jar coatjava/lib/clas/
cp external-dependencies/vecmath-1.3.1-2.jar coatjava/lib/clas/
mkdir -p coatjava/lib/utils
cp external-dependencies/jclara-4.3-SNAPSHOT.jar coatjava/lib/utils
#cp external-dependencies/jaw-1.0.jar coatjava/lib/utils
mkdir -p coatjava/lib/services

### clean up any cache copies ###
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
cp reconstruction/*/target/clas12detector-*-SNAPSHOT*.jar coatjava/lib/services/

echo "COATJAVA SUCCESSFULLY BUILT !"
