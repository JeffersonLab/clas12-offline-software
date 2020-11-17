#!/bin/csh -f

if ( ! -e "$1.txt" ) then
    echo Missing input file:  $1.txt
    exit
endif
if ( -e "$1.evio" ) then
    echo Output file already exists:  $1.evio
    exit
endif

#source /group/clas12/packages/setup.csh
#module load ccdb
#module load root
#module load gemc/4.3.2

set run = 11
set nEvents = 100
set gcard = ${GEMC}/../../gcards/clas12-default.gcard

gemc \
    $gcard \
    -INPUT_GEN_FILE="LUND, $1.txt" \
    -OUTPUT="evio, $1.evio" \
    -RUNNO=$run \
    -USE_GUI=0 \
    -N=$nEvents

gzip $1.evio

