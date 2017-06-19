#!/bin/bash

JARNAME=tinyMS.jar
MAIN=cnuphys.tinyMS.server.TinyMessageServer
VARGS="-Xmx256M -Xss512k"
java $VARGS -jar $JARNAME $MAIN
