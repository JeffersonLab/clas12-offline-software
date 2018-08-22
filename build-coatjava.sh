#!/bin/bash

NC='\033[0m'              # No Color
RED='\033[0;31m'          # Red
GREEN='\033[0;32m'        # Green
YELLOW='\033[0;33m'       # Yellow
BLUE='\033[0;34m'         # Blue
PURPLE='\033[0;35m'       # Purple

BUILD_DIR=$PWD

usage='build-coatjava.sh [--nospotbugs] [--nomaps] [--nounittests] [--clean]'

runSpotBugs="yes"
downloadMaps="yes"
runUnitTests="yes"
clean="no"
for xx in $@
do
    if [ "$xx" == "--nospotbugs" ]
    then
        runSpotBugs="no"
    elif [ "$xx" == "-n" ]
    then
        runSpotBugs="no"
    elif [ "$xx" == "--nomaps" ]
    then
        downloadMaps="no"
    elif [ "$xx" == "--nounittests" ]
    then
        runUnitTests="no"
    elif [ "$xx" == "--clean" ]
    then
        clean="yes"
    else
        echo $usage
        exit
    fi
done

if [ -z ${CLAS12DIR+x} ];
  then
    export CLAS12DIR=$BUILD_DIR/coatjava
  else
    echo -en "${GREEN}";
    echo +-------------------------------------------------------------------------
    echo "| Using CLAS12DIR, manually set to: $CLAS12DIR";
    echo +-------------------------------------------------------------------------
    echo -en "${NC}";
fi

### clean up any cache copies ###
if [ $clean == "yes" ]; then
  echo -e "${RED} Warning deleting $CLAS12DIR and maven cache! \n\n\t(You have 5 seconds to top this) \n\n${NC}"
  sleep 5
  rm -rf ~/.m2/repository/org/hep/hipo
  rm -rf ~/.m2/repository/org/jlab
  rm -rf $CLAS12DIR
fi


mkdir -p $CLAS12DIR

export TORUSMAP=Symm_torus_r2501_phi16_z251_24Apr2018.dat
export SOLENOIDMAP=Symm_solenoid_r601_phi1_z1201_13June2018.dat
if [ $downloadMaps == "yes" ]; then
  webDir=http://clasweb.jlab.org/clas12offline/magfield
  MAPS=$CLAS12DIR/etc/data/magfield
  mkdir -p $MAPS
  cd $MAPS
  if [ ! -f $SOLENOIDMAP ] || [ ! -f $TORUSMAP ]; then
    for map in $SOLENOIDMAP $TORUSMAP
      do
        # -N only redownloads if timestamp/filesize is newer/different
        wget -N --no-check-certificate $webDir/$map
      done
  fi
  cd $BUILD_DIR
fi

#Remove and recreate directory structure
mkdir -p $CLAS12DIR/lib/clas
mkdir -p $CLAS12DIR/lib/services
mkdir -p $CLAS12DIR/lib/utils

#Copy in files from
cp -r bin $CLAS12DIR/
cp -r etc $CLAS12DIR/
cp external-dependencies/JEventViewer-1.1.jar $CLAS12DIR/lib/clas/
cp external-dependencies/vecmath-1.3.1-2.jar $CLAS12DIR/lib/clas/
cp external-dependencies/jclara-4.3-SNAPSHOT.jar $CLAS12DIR/lib/utils/
cp external-dependencies/KPP-Plots-2.0.jar $CLAS12DIR/lib/utils/

if [ $runUnitTests == "yes" ]; then
	mvn install # also runs unit tests
	if [ $? != 0 ] ; then echo "mvn install failure" ; exit 1 ; fi
else
	mvn -Dmaven.test.skip=true install
	if [ $? != 0 ] ; then echo "mvn install failure" ; exit 1 ; fi
fi

if [ $runSpotBugs == "yes" ]; then
	# mvn com.github.spotbugs:spotbugs-maven-plugin:spotbugs # spotbugs goal produces a report target/spotbugsXml.xml for each module
	mvn com.github.spotbugs:spotbugs-maven-plugin:check # check goal produces a report and produces build failed if bugs
	# the spotbugsXml.xml file is easiest read in a web browser
	# see http://spotbugs.readthedocs.io/en/latest/maven.html and https://spotbugs.github.io/spotbugs-maven-plugin/index.html for more info
	if [ $? != 0 ] ; then echo "spotbugs failure" ; exit 1 ; fi
fi

cd $BUILD_DIR/common-tools/coat-lib
mvn package
if [ $? != 0 ] ; then echo "mvn package failure" ; exit 1 ; fi
cd $BUILD_DIR

cp common-tools/coat-lib/target/coat-libs-5.1-SNAPSHOT.jar $CLAS12DIR/lib/clas/
cp reconstruction/dc/target/clas12detector-dc-1.0-SNAPSHOT.jar $CLAS12DIR/lib/services/
cp reconstruction/tof/target/tof-1.0-SNAPSHOT.jar $CLAS12DIR/lib/services/
cp reconstruction/cvt/target/cvt-1.0-SNAPSHOT.jar $CLAS12DIR/lib/services/
cp reconstruction/ft/target/clas12detector-ft-1.0-SNAPSHOT.jar $CLAS12DIR/lib/services/
cp reconstruction/ec/target/clas12detector-ec-1.0-SNAPSHOT.jar $CLAS12DIR/lib/services/
cp reconstruction/ltcc/target/clasrec-ltcc-1.0-SNAPSHOT.jar $CLAS12DIR/lib/services/
cp reconstruction/htcc/target/clasrec-htcc-1.0-SNAPSHOT.jar $CLAS12DIR/lib/services/
cp reconstruction/cnd/target/clas12detector-cnd-1.0-SNAPSHOT.jar $CLAS12DIR/lib/services/
cp reconstruction/rich/target/clas12detector-rich-1.0-SNAPSHOT.jar $CLAS12DIR/lib/services/
cp reconstruction/fvt/target/clas12detector-fmt-1.0-SNAPSHOT.jar $CLAS12DIR/lib/services/
cp reconstruction/eb/target/clas12detector-eb-1.0-SNAPSHOT.jar $CLAS12DIR/lib/services/

echo "COATJAVA SUCCESSFULLY BUILT !"
echo "Exporting CLAS12DIR=$CLAS12DIR"

export CLAS12DIR=$CLAS12DIR
