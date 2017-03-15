#!/bin/bash
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
    cd coat-lib
    mvn package
    #cp target/coat-libs-3.0-SNAPSHOT.jar $COATJAVA/lib/clas/
    ls -lthr target/coat-libs-*
    cd ..
fi
#=================================================================
# Documentation build
if [[ ! -z $dopt ]]
then
    echo "---> Building documentation ...."
    javadoc -d javadoc/clas-io -sourcepath clas-io/src/main/java/ -subpackages org
    javadoc -d javadoc/clas-geometry   -sourcepath clas-geometry/src/main/java/ -subpackages org
    javadoc -d javadoc/clas-io         -sourcepath clas-io/src/main/java/  -subpackages org
    javadoc -d javadoc/clas-physics    -sourcepath clas-physics/src/main/java/  -subpackages org
    javadoc -d javadoc/clas-detector   -sourcepath clas-detector/src/main/java/ -subpackages org
    javadoc -d javadoc/clas-utils      -sourcepath clas-utils/src/main/java/    -subpackages org
    #scp -r javadoc clas12@ifarm65:/group/clas/www/clasweb/html/clas12offline/docs/.
fi

#=================================================================
# Finishing touches
echo ""
echo "--> Done building....."
echo ""
echo " \t Usage : build.sh -d -m"
echo ""


