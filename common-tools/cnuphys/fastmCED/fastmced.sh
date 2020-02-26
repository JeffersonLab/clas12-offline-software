#!/bin/bash

JARNAME=./fastmced.jar
MAIN=cnuphys.ced.frame.FastMCed
VARGS="-Dsun.java2d.pmoffscreen=false -Xmx1024M -Xss512k"
java $VARGS -jar $JARNAME $MAIN
