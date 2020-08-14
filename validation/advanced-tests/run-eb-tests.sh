#!/bin/bash

webDir=http://clasweb.jlab.org/clas12offline/distribution/coatjava/validation_files/eb
webVersion=4.4.0-fid-r11
webDir=$webDir/$webVersion

# coatjava must already be built at ../../coatjava/

# whether to use CLARA (0=no)
useClara=0

# if non-zero, don't redownload dependencies, don't run reconstruction:
runTestOnly=0

# gemc default solenoid (changed in 4a.2.4):
gemcSolenoidDefault=-1.0
if [[ $webVersion = *"4a.2.2"* ]] || [[ $webVersion = *"4a.2.3"* ]]
then
    gemcSolenoidDefault=1.0
fi

# geometry variation for DC
geoDbVariation="default"
if [[ $webVersion = *"4a.2.2"* ]] || [[ $webVersion = *"4a.2.3"* ]] || [[ $webVersion = *"4a.2.4"* ]]
then
    geoDbVariation="dc_geo_gemc424"
fi

nEvents=-1

for arg in $@
do
    if [ "$arg" == "-t" ]
    then
        runTestOnly=1
    elif [[ $arg == "-100" ]]
    then
        webDir=${webDir}-100
    fi
done

# last argument is input file stub:
webFileStub="${@: -1}"

# sanity check on filestub name,
# just to error with reasonable message before proceeding:
case $webFileStub in
    # electron in forward, hadron in forward:
    electronproton)
        ;;
    electronkaon)
        ;;
    electronpion)
        ;;
    electrongamma)
        ;;
    electronneutron)
        ;;
    electronFTproton)
        ;;
    electronFTkaon)
        ;;
    electronFTpion)
        ;;
    electronFTgamma)
        ;;
    electrongammaFT)
        ;;
    electronprotonC)
        ;;
    electronkaonC)
        ;;
    electronpionC)
        ;;
    electrongammaC)
        ;;
    electronneutronC)
        ;;
    *)
      echo Invalid input evio file:  $webFileStub
      exit 1
esac

# set up environment
if [ $useClara -eq 0 ]
then
    COAT=../../coatjava
    source $COAT/bin/env.sh
else
    CLARA_HOME=$PWD/clara_installation/
    COAT=$CLARA_HOME/plugins/clas12/
    export CLARA_HOME
fi

classPath="$COAT/lib/services/*:$COAT/lib/clas/*:$COAT/lib/utils/*:../lib/*:src/"

classPath2="../../coatjava/lib/services/*:../../coatjava/lib/clas/*:../../coatjava/lib/utils/*:../lib/*:src/"

# make sure test code compiles before anything else:
javac -cp $classPath2 src/eb/EBTwoTrackTest.java
if [ $? != 0 ] ; then echo "EBTwoTrackTest compilation failure" ; exit 1 ; fi

# download and setup dependencies, run reconstruction:
if [ $runTestOnly -eq 0 ]
then

    if ! [ $useClara -eq 0 ]
    then
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
    fi

    # download test files, if necessary:
    rm -f ${webFileStub}.evio
    wget -N --no-check-certificate $webDir/${webFileStub}.evio.gz
    if [ $? != 0 ] ; then echo "wget validation files failure" ; exit 1 ; fi
    gunzip -f ${webFileStub}.evio.gz

    rm -f ${webFileStub}.hipo
    rm -f out_${webFileStub}.hipo

    # convert to hipo:
    $COAT/bin/evio2hipo -s $gemcSolenoidDefault -o ${webFileStub}.hipo ${webFileStub}.evio

    # run reconstruction:
    if [ $useClara -eq 0 ]
    then
        GEOMDBVAR=$geoDbVariation
        export GEOMDBVAR
        ../../coatjava/bin/recon-util -i ${webFileStub}.hipo -o out_${webFileStub}.hipo -c 2
    else
        echo "set inputDir $PWD/" > cook.clara
        echo "set outputDir $PWD/" >> cook.clara
        echo "set threads 7" >> cook.clara
        echo "set javaMemory 2" >> cook.clara
        echo "set session s_cook" >> cook.clara
        echo "set description d_cook" >> cook.clara
        ls ${webFileStub}.hipo > files.list
        echo "set fileList $PWD/files.list" >> cook.clara
        echo "run local" >> cook.clara
        echo "exit" >> cook.clara
        $CLARA_HOME/bin/clara-shell cook.clara
    fi
fi

# run Event Builder tests:
java -DCLAS12DIR="$COAT" -Xmx1536m -Xms1024m -cp $classPath2 -DINPUTFILE=out_${webFileStub}.hipo org.junit.runner.JUnitCore eb.EBTwoTrackTest
if [ $? != 0 ] ; then echo "EBTwoTrackTest unit test failure" ; exit 1 ; else echo "EBTwoTrackTest passed unit tests" ; fi

exit 0

