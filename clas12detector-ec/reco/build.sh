#!/bin/sh
#=================================================================
# BUILDING SCRIPT for COATJAVA PROJECT (first maven build)
# then the documentatoin is build from the sources and commited
# to the documents page
#=================================================================
# Maven Build

while getopts dm name
do
    case $name in
       d) dopt=1;; # -d option is for generating Javadoc 
       m) mopt=1;; # -s option is for compiling the code
       *) echo "Invalid arg : "; echo "\t use : build.sh -m -d" ; echo "" ;exit;
    esac
done

if [[ ! -z $mopt ]]
then
    mvn install
    cp target/clas12detector-ec-1.0-SNAPSHOT.jar $COATJAVA/lib/services/.
#    cd ..
    echo "\n"
    echo "*****************************************"
    echo "* ---> Compilation is done....          *"
    echo "*****************************************"
    echo "\n"
    exit
fi
#=================================================================
# Documentation build
if [[ ! -z $dopt ]]
then
    echo "---> Building documentation ...."
fi

#=================================================================
# Finishing touches
echo ""
#echo "--> Done building....."
echo ""
echo "   Usage : build.sh -d -m"
echo "\n"
echo "\t -m : run the build script with maven and install plugin"
echo "\t -s : run the documentation generation with javadoc"
echo "\n\n"
