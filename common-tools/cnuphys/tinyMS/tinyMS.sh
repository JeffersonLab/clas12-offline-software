#!/bin/bash

SCRIPT_DIR=`dirname $0`

echo $SCRIPT_DIR
JARNAME=$SCRIPT_DIR/tinyMS.jar
MAIN=cnuphys.ced.frame.TinyMessageServer
VARGS="-Xmx256M -Xss512k"
java $VARGS -jar $JARNAME $MAIN
