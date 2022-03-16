#!/bin/sh -f

# coatjava must already be built at ../../coatjava/
OS=$(uname)

# set up environment
CLARA_HOME=$PWD/clara_installation/ ; export CLARA_HOME
COAT=$CLARA_HOME/plugins/clas12/
classPath="$COAT/lib/services/*:$COAT/lib/clas/*:$COAT/lib/utils/*:../lib/*:src/"

# tar the local coatjava build so it can be installed with clara
cd ../..
tar -zcvf coatjava-local.tar.gz coatjava
mv coatjava-local.tar.gz validation/advanced-tests/
cd -

# install clara

case $OS in
    'Linux')
       wget --no-check-certificate https://claraweb.jlab.org/clara/_downloads/install-claracre-clas.sh
     ;;
     'Darwin')
       echo "Getting Clara..."
       curl -OL "https://claraweb.jlab.org/clara/_downloads/install-claracre-clas.sh" -o install-claracre-clas.sh
     ;;
     *) ;;
esac


chmod +x install-claracre-clas.sh
echo Y | ./install-claracre-clas.sh -f 5.0.2 -j 11 -l local
if [ $? != 0 ] ; then echo "clara installation error" ; exit 1 ; fi
rm install-claracre-clas.sh

# download test files

case $OS in
    'Linux')
       wget --no-check-certificate http://clasweb.jlab.org/clas12offline/distribution/coatjava/validation_files/twoTrackEvents_809_raw.evio.tar.gz
     ;;
     'Darwin')
       curl -OL "http://clasweb.jlab.org/clas12offline/distribution/coatjava/validation_files/twoTrackEvents_809_raw.evio.tar.gz" -o twoTrackEvents_809_raw.evio.tar.gz
     ;;
     *) ;;
esac



if [ $? != 0 ] ; then echo "wget validation files failure" ; exit 1 ; fi
tar -zxvf twoTrackEvents_809_raw.evio.tar.gz

export JAVA_OPTS="-Djava.util.logging.config.file=$PWD/../../etc/logging/debug.properties"

# run decoder
$COAT/bin/decoder -t -0.5 -s 0.0 -i ./twoTrackEvents_809_raw.evio -o ./twoTrackEvents_809.hipo -c 2

# run reconstruction with clara
echo "set inputDir $PWD/" > cook.clara
echo "set outputDir $PWD/" >> cook.clara
echo "set threads 1" >> cook.clara
echo "set javaOptions \"-Xmx2g -Djava.util.logging.config.file=$PWD/../../etc/logging/debug.properties\"" >> cook.clara
echo "set session s_cook" >> cook.clara
echo "set description d_cook" >> cook.clara
ls twoTrackEvents_809.hipo > files.list
echo "set fileList $PWD/files.list" >> cook.clara
echo "set servicesFile $CLARA_HOME/plugins/clas12/config/kpp.yaml" >> cook.clara
echo "run local" >> cook.clara
echo "exit" >> cook.clara
$CLARA_HOME/bin/clara-shell cook.clara
#if [ $? != 0 ] ; then echo "reconstruction with clara failure" ; exit 1 ; fi

# compile test codes
javac -cp $classPath src/kpptracking/KppTrackingTest.java 
if [ $? != 0 ] ; then echo "KppTrackingTest compilation failure" ; exit 1 ; fi

# run KppTracking junit tests
java -DCLAS12DIR="$COAT" -Xmx1536m -Xms1024m -cp $classPath org.junit.runner.JUnitCore kpptracking.KppTrackingTest
if [ $? != 0 ] ; then echo "KppTracking unit test failure" ; exit 1 ; else echo "KppTracking passed unit tests" ; fi
