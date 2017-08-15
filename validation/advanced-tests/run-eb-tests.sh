#!/bin/sh -f

# coatjava must already be built at ../../coatjava/

# set up environment
CLARA_HOME=$PWD/clara_installation/ ; export CLARA_HOME
COAT=$CLARA_HOME/plugins/clas12/
classPath="$COAT/lib/services/*:$COAT/lib/clas/*:$COAT/lib/utils/*:../lib/*:src/"

webDir=http://clasweb.jlab.org/clas12offline/distribution/coatjava/validation_files/eb/v0/
webFileStub=electronproton
#webFileStub=electronpion
#webFileStub=electronkaon

# tar the local coatjava build so it can be installed with clara
cd ../..
tar -zcvf coatjava-local.tar.gz coatjava
mv coatjava-local.tar.gz validation/advanced-tests/
cd -

# install clara
if ! [ -d clara_installation ]
then
    wget --no-check-certificate https://claraweb.jlab.org/clara/_downloads/install-claracre-clas.sh
    chmod +x install-claracre-clas.sh
    ./install-claracre-clas.sh -l local
    if [ $? != 0 ] ; then echo "clara installation error" ; exit 1 ; fi
    rm install-claracre-clas.sh
fi

# download test files
if ! [ -e ${webFileStub}.evio.gz ]
then
    wget --no-check-certificate $webDir/${webFileStub}.evio.gz
    if [ $? != 0 ] ; then echo "wget validation files failure" ; exit 1 ; fi
    gunzip -f ${webFileStub}.evio.gz
fi

rm -f ${webFileStub}.hipo
rm -f out_${webFileStub}.hipo

# run decoder
$COAT/bin/decoder -t -0.5 -s 0.0 -i ./${webFileStub}.evio -o ./${webFileStub}.hipo -c 2

# run reconstruction with clara
echo "set inputDir $PWD/" > cook.clara
echo "set outputDir $PWD/" >> cook.clara
echo "set threads 2" >> cook.clara
echo "set javaMemory 2" >> cook.clara
echo "set session s_cook" >> cook.clara
echo "set description d_cook" >> cook.clara
ls ${webFileStub}.hipo > files.list
echo "set fileList $PWD/files.list" >> cook.clara
echo "run local" >> cook.clara
echo "exit" >> cook.clara
$CLARA_HOME/bin/clara-shell cook.clara

# RUN NUMBER PROBLEM (run=0)

# compile test codes
#javac -cp $classPath src/kpptracking/KppTrackingTest.java 
#if [ $? != 0 ] ; then echo "KppTrackingTest compilation failure" ; exit 1 ; fi

# run KppTracking junit tests
#java -DCLAS12DIR="$COAT" -Xmx1536m -Xms1024m -cp $classPath org.junit.runner.JUnitCore kpptracking.KppTrackingTest
#if [ $? != 0 ] ; then echo "KppTracking unit test failure" ; exit 1 ; else echo "KppTracking passed unit tests" ; fi

exit 0

