#!/bin/sh -f

SCRIPT_DIR=`dirname $0`
DISTRO_DIR=$SCRIPT_DIR/../ ; export DISTRO_DIR
CLAS12DIR=$SCRIPT_DIR/../ ; export CLAS12DIR
CLARA_SERVICES=$DISTRO_DIR/lib/services; export CLARA_SERVICES
DATAMINING=$DISTRO_DIR ; export DATAMINING
TORUSMAP=$CLAS12DIR/etc/data/magfield/clas12-fieldmap-torus.dat ; export TORUSMAP
SOLENOIDMAP=$CLAS12DIR/etc/data/magfield/clas12-fieldmap-solenoid.dat ; export SOLENOIDMAP

#CLARA_HOME=`dirname $0`
#CLARA_SERVICES=`dirname $0`
#DATAMINING=`dirname $0`

echo +-------------------------------------------------------------------------
echo "| Starting CLARA-PLATFORM with CLARA_SERVICES = " $CLARA_SERVICES
echo +-------------------------------------------------------------------------
echo "\n"

echo "INSTALLATION DIRECTORY = " $CLARA_HOME
echo "LIBRARY DIRECTORY      = " $DATAMINING/lib/clas/

#java -cp "$DATAMINING/lib/clas/core/*" org.jlab.coda.eventViewer.EventTreeFrame $*
java -Xms1024m -cp "$DATAMINING/lib/clas/*:$DATAMINING/lib/plugins/*" org.jlab.display.ec.ECPion $*
