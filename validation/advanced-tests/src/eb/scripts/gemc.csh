#!/bin/csh -f

if ( ! -e "$1.txt" ) then
    echo Missing input file:  $1.txt
    exit
endif
if ( -e "$1.evio" ) then
    echo Output file already exists:  $1.evio
    exit
endif

source /group/clas12/gemc/environment.csh 4a.2.2
set run = 11
set nEvents = 1000
set gcard = ${GEMC}/../clas12.gcard

gemc \
    $gcard \
    -INPUT_GEN_FILE="LUND, $1.txt" \
    -OUTPUT="evio, $1.evio" \
    -RUNNO=$run \
    -USE_GUI=0 \
    -N=$nEvents

