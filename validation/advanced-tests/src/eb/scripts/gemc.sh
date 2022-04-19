#!/bin/sh

if ! [ -e "$1.txt" ]
then
    echo Missing input file:  $1.txt
    exit
fi
if [ -e "$1.evio" ] || [ -e "$1.hipo" ]
then
    echo Output file already exists:  ${1}.evio/hipo
    exit
fi

run=11
nEvents=100
gcard=$GEMC/../config/clas12-default.gcard 

gemc \
    $gcard \
    -INPUT_GEN_FILE="LUND, $1.txt" \
    -OUTPUT="hipo, $1.hipo" \
    -RUNNO=$run \
    -USE_GUI=0 \
    -N=$nEvents

