#!/bin/sh -f

OS=$(uname)

# set up environment
gemcSolenoidDefault=-1.0
GEOMDBVAR="default"
export GEOMDBVAR

CLARA_HOME=$PWD/clara_installation/; export CLARA_HOME
COAT=$CLARA_HOME/plugins/clas12/
classPath="$COAT/lib/services/*:$COAT/lib/clas/*:$COAT/lib/utils/*:../lib/*:src/"

# tar the local coatjava build so it can be installed with clara
cd ../..
tar -zcf coatjava-local.tar.gz coatjava
mv coatjava-local.tar.gz validation/advanced-tests/
cd -

# install clara
echo Y | ./install-claracre-clas.sh -f 4.3.9 -l local
if [ $? != 0 ] ; then echo "clara installation error" ; exit 1 ; fi

# make sure that test code compiles before anything else
. $COAT/bin/env.sh
javac -cp $classPath src/eb/EBTwoTrackTest.java
if [ $? != 0 ]; then echo "EBTwoTrackTest compilation failure"; exit 1; fi

# run reconstruction:
echo "set inputDir $PWD/" > cook.clara
echo "set outputDir $PWD/" >> cook.clara
echo "set threads 1" >> cook.clara
echo "set javaMemory 2" >> cook.clara
echo "set maxEvents 5" >> cook.clara
echo "set session s_cook" >> cook.clara
echo "set description d_cook" >> cook.clara
# ls twoTrackEvents_809.hipo > files.list
ls fmt_data_rgf.hipo > files.list
echo "set fileList $PWD/files.list" >> cook.clara
# echo "set servicesFile $CLARA_HOME/plugins/clas12/config/data.yaml" >> cook.clara
echo "set servicesFile /home/twig/shared/data/clas12/fvt_rec.yaml" >> cook.clara
echo "run local" >> cook.clara
echo "exit" >> cook.clara
$CLARA_HOME/bin/clara-shell cook.clara

# run Event Builder tests:
# java -DCLAS12DIR="$COAT" -Xmx1536m -Xms1024m -cp $classPath -DINPUTFILE=out_fmt_data_rgf.hipo org.junit.runner.JUnitCore eb.EBTwoTrackTest
# if [ $? != 0 ]; then echo "EBTwoTrackTest unit test failure"; exit 1; else echo "EBTwoTrackTest passed unit tests"; fi

exit 0
