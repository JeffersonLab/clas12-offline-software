#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
echo Script location: $SCRIPT_DIR
JARNAME=$SCRIPT_DIR/ced.jar
MAIN=cnuphys.ced.frame.Ced
VARGS="-Dsun.java2d.pmoffscreen=false -Xmx1024M -Xss512k"
CLAS12DIR=$SCRIPT_DIR/coatjava
echo CLAS12DIR used by ced: $CLAS12DIR
java $VARGS -DCLAS12DIR="$CLAS12DIR" -jar $JARNAME $MAIN USE3D
