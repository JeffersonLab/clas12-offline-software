#!/bin/bash

# coatjava must already be built at ../../coatjava/

# set up environment
CLARA_HOME=$PWD/clara_installation/
COAT=$CLARA_HOME/plugins/clas12/
classPath="$COAT/lib/services/*:$COAT/lib/clas/*:$COAT/lib/utils/*:../lib/*:src/"

echo $CLARA_HOME
ls $CLARA_HOME
echo $COAT
echo $classPath

## tar the local coatjava build so it can be installed with clara
#tar -zcvf coatjava-local.tar.gz ../../coatjava/
#echo "HEYHEY"
#ls -lthr
#
## install clara
#wget --no-check-certificate https://claraweb.jlab.org/clara/_downloads/install-claracre-clas.sh
#chmod +x install-claracre-clas.sh
#./install-claracre-clas.sh -l local
#if [ $? != 0 ] ; then echo "clara installation error" ; exit 1 ; fi
#rm install-claracre-clas.sh
#echo "HEYHEY"
#ls -lthr
#
## download test files
#wget --no-check-certificate http://clasweb.jlab.org/clas12offline/distribution/coatjava/validation_files/twoTrackEvents_809_raw.evio.tar.gz
#if [ $? != 0 ] ; then echo "wget validation files failure" ; exit 1 ; fi
#tar -zxvf twoTrackEvents_809_raw.evio.tar.gz
#echo "HEYHEY"
#ls -lthr
#
## run decoder
#$COAT/bin/decoder -t -0.5 -s 0.0 -i ./twoTrackEvents_809_raw.evio -o ./twoTrackEvents_809.hipo -c 2
#echo "HEYHEY"
#ls -lthr
#
## run reconstruction with clara
#echo "set inputDir $PWD/" > cook.clara
#echo "set outputDir $PWD/" >> cook.clara
#echo "set threads 2" >> cook.clara
#echo "set javaMemory 2" >> cook.clara
#echo "set session s_cook" >> cook.clara
#echo "set description d_cook" >> cook.clara
#ls twoTrackEvents_809.hipo > files.list
#echo "set fileList $PWD/files.list" >> cook.clara
#echo "run local" >> cook.clara
#echo "exit" >> cook.clara
#$CLARA_HOME/bin/clara-shell cook.clara
##if [ $? != 0 ] ; then echo "reconstruction with clara failure" ; exit 1 ; fi
#echo "HEYHEY"
#ls -lthr
#
## compile codes
#javac -cp $classPath src/kpptracking/KppTrackingTest.java 
#if [ $? != 0 ] ; then echo "KppTrackingTest compilation failure" ; exit 1 ; fi
#echo "HEYHEY"
#ls -lthr
#
## run KppTracking junit tests
#java -DCLAS12DIR="$COAT" -Xmx1536m -Xms1024m -cp $classPath org.junit.runner.JUnitCore kpptracking.KppTrackingTest
#if [ $? != 0 ] ; then echo "KppTracking unit test failure" ; exit 1 ; else echo "KppTracking passed unit tests" ; fi
#echo "HEYHEY"
#ls -lthr
