#!/bin/bash
#launch script for ced

MAIN=cnuphys.ced.frame.Ced

#whatever VM arguments you want
VMARG="-Xmx1000M -Xss512k"

#run
java $VMARG -jar ced.jar $MAIN
