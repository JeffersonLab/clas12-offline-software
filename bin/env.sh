#!/bin/sh

export CLAS12DIR=`dirname $0`/..

# Set default field maps (but do not override user's env):
if [ -z "$COAT_MAGFIELD_TORUSMAP" ]; then
    export COAT_MAGFIELD_TORUSMAP=Symm_torus_r2501_phi16_z251_24Apr2018.dat
fi
if [ -z "$COAT_MAGFIELD_TORUSSECONDARYMAP" ]; then
    export COAT_MAGFIELD_TORUSSECONDARYMAP=Full_torus_r251_phi181_z251_08May2018.dat
fi
if [ -z "$COAT_MAGFIELD_SOLENOIDMAP" ]; then
    export COAT_MAGFIELD_SOLENOIDMAP=Symm_solenoid_r601_phi1_z1201_13June2018.dat
fi

echo +-------------------------------------------------------------------------
echo "| COATJAVA LIBRARY DIRECTORY = " $CLAS12DIR/lib/clas/
echo +-------------------------------------------------------------------------

