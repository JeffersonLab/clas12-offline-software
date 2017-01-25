#!/bin/bash
#launch script for ced

SCRIPT_DIR=`dirname -- "$0"`
JARNAME=$SCRIPT_DIR/ced.jar

MAIN=cnuphys.ced.frame.Ced

#whatever VM arguments you want
VMARG="-Dsun.java2d.pmoffscreen=false -Xmx1000M -Xss512k"

#run
java $VMARG -jar ced.jar $MAIN
