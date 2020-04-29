#!/bin/bash

REPO="$( cd "$(dirname "$0")"/../.. ; pwd -P )"/myLocalMvnRepo

cd `dirname $0`

#-------------------------------------------------------------------------------------------------
# Script is exporting existing Jar files to repository
#-------------------------------------------------------------------------------------------------

VERSION=6c.5.4

mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file \
    -Dfile=target/coat-libs-${VERSION}-SNAPSHOT.jar \
    -DgroupId=org.jlab.coat \
    -DartifactId=coat-libs \
    -Dversion=${VERSION}-SNAPSHOT \
    -Dpackaging=jar \
    -DlocalRepositoryPath=$REPO

scp -r $REPO/org/jlab/coat/coat-libs/${VERSION}-SNAPSHOT \
    clas12@jlabl1:/group/clas/www/clasweb/html/clas12maven/org/jlab/coat/coat-libs/.


cd $REPO/..
tar -czvf coatjava-${VERSION}.tar.gz coatjava
scp coatjava-${VERSION}.tar.gz \
    clas12@jlabl1:/group/clas/www/clasweb/html/clas12offline/distribution/coatjava/.

