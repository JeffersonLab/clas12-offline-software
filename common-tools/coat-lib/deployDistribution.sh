#-------------------------------------------------------------------------------------------------
# Script is exporting existing Jar files to repository
#-------------------------------------------------------------------------------------------------
#  JEVIO
REPO="/Users/devita/NetBeansProjects/clas12-offline-software/myLocalMvnRepo"
VERSION="6.0.0-SNAPSHOT"

mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file  -Dfile=target/coat-libs-6.0.0-SNAPSHOT.jar \
    -DgroupId=org.jlab.coat \
    -DartifactId=coat-libs \
    -Dversion=$VERSION \
    -Dpackaging=jar \
    -DlocalRepositoryPath=$REPO

scp -r $REPO/org/jlab/coat/coat-libs/$VERSION clas12@jlabl1:/group/clas/www/clasweb/html/clas12maven/org/jlab/coat/coat-libs/.

