#!/bin/bash
#-------------------------------------------------------------------------------------------------
# Script is exporting existing Jar files to repository
#-------------------------------------------------------------------------------------------------
# 
SCRIPT_HOME=`dirname $0`

mkdir $SCRIPT_HOME/maven-repo
MAVEN_REPO=$SCRIPT_HOME/maven-repo


mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file  -Dfile=$SCRIPT_HOME/../target/clas-common-3.0-SNAPSHOT.jar \
    -DgroupId=org.clas \
    -DartifactId=coat-libs \
    -Dversion=2.0-SNAPSHOT \
    -Dpackaging=jar \
    -DlocalRepositoryPath=$MAVEN_REPO

scp -r $MAVEN_REPO/org clas12@jlabl1:/group/clas/www/clasweb/html/clas12maven/.
