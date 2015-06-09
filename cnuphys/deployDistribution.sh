#-------------------------------------------------------------------------------------------------
# Script is exporting existing Jar files to repository
#-------------------------------------------------------------------------------------------------
#  JEVIO
REPO="/Users/gavalian/Work/MavenRepo"

mvn3 org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file  -Dfile=target/cnuphys-2.0-SNAPSHOT.jar \
    -DgroupId=cnuphys \
    -DartifactId=cnuphys \
    -Dversion=2.0-SNAPSHOT \
    -Dpackaging=jar \
    -DlocalRepositoryPath=$REPO

scp -r $REPO/cnuphys/cnuphys clas12@jlabl1:/group/clas/www/clasweb/html/clas12maven/cnuphys/.
