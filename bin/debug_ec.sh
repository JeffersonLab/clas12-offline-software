#!/bin/bash

NC='\033[0m'              # No Color
RED='\033[0;31m'          # Red
GREEN='\033[0;32m'        # Green
YELLOW='\033[0;33m'       # Yellow
BLUE='\033[0;34m'         # Blue
PURPLE='\033[0;35m'       # Purple

if [ -z ${CLAS12DIR+x} ];
  then
    if [ -z ${CLARA_HOME+x} ];
      then
        echo -en "${RED}";
        echo "Error: CLAS12DIR and CLARA_HOME not set.";
        echo "Set one of these to continue";
        echo "Exiting";
        echo -en "${NC}";
        exit 1;
      else
        export CLARA_SERVICES=$CLARA_HOME/lib;
        export CLAS12DIR=$CLARA_HOME/plugins/clas12;
        echo -en "${GREEN}";
        echo +-------------------------------------------------------------------------
        echo "| Using CLARA_SERVICES to set CLAS12DIR: $CLAS12DIR";
        echo +-------------------------------------------------------------------------
        echo -en "${NC}";
    fi
  else
    echo -en "${GREEN}";
    echo +-------------------------------------------------------------------------
    echo "| Using CLAS12DIR, manually set to: $CLAS12DIR";
    echo +-------------------------------------------------------------------------
    echo -en "${NC}";
fi
source $CLAS12DIR/bin/env.sh

java -Xmx1024m -Xms256m -cp "$CLAS12DIR/lib/clas/*:$CLAS12DIR/lib/services/*" org.jlab.display.ec.ECPion $*
