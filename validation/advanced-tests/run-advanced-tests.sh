#!/bin/sh -f

# coatjava must already be built at ../../coatjava/
OS=$(uname)

# set up environment
export CLARA_HOME=$PWD/clara_installation/ ; export CLARA_HOME
export CLAS12DIR=$CLARA_HOME/plugins/clas12/
export CLARA_USER_DATA=.
export JAVA_OPTS=-Xmx2g
classPath="$CLAS12DIR/lib/services/*:$CLAS12DIR/lib/clas/*:$CLAS12DIR/lib/utils/*:../lib/*:src/"

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
[ $? != 0 ] && echo "clara installation error" && exit 1
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
[ $? != 0 ] && echo "wget validation files failure" && exit 2
tar -zxvf twoTrackEvents_809_raw.evio.tar.gz

# run decoder
$CLAS12DIR/bin/decoder -t -0.5 -s 0.0 -i ./twoTrackEvents_809_raw.evio -o ./twoTrackEvents_809.hipo -c 2

# run reconstruction with clara
mkdir -p $CLARA_USER_DATA && cd $CLARA_USER_DATA && mkdir -p log config data/output
ls twoTrackEvents_809.hipo > files.list
$CLARA_HOME/lib/clara/run-clara \
    -i . \
    -o . \
    -z out_ \
    -x . \
    -t 1 \
    -s kpp \
    $CLARA_HOME/plugins/clas12/config/kpp.yaml \
    ./files.list
[ $? != 0 ] && echo "KppTrackingTest reconstruction failure" && exit 3

# compile test codes
javac -cp $classPath src/kpptracking/KppTrackingTest.java 
[ $? != 0 ] && echo "KppTrackingTest compilation failure" && exit 4

# run KppTracking junit tests
java -DCLAS12DIR="$CLAS12DIR" -Xmx1536m -Xms1024m -cp $classPath org.junit.runner.JUnitCore kpptracking.KppTrackingTest
[ $? != 0 ] && echo "KppTrackingTest failure" && exit 5

echo "KppTrackingTest passed"
exit 0

