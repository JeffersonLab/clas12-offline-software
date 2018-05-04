#!/bin/sh -f

SCRIPT_DIR=`dirname $0`
DISTRO_DIR=$SCRIPT_DIR/../ ; export DISTRO_DIR
CLAS12DIR=$SCRIPT_DIR/../ ; export CLAS12DIR
CLARA_SERVICES=$DISTRO_DIR/lib/services; export CLARA_SERVICES
DATAMINING=$DISTRO_DIR ; export DATAMINING
TORUSMAP=clas12-fieldmap-torus.dat ; export TORUSMAP
SOLENOIDMAP=clas12-fieldmap-solenoid.dat ; export SOLENOIDMAP

