#!/bin/sh -f

# coatjava must already be built at ../../coatjava/

# set environment
COAT="../../coatjava/"
classPath="$COAT/lib/services/*:$COAT/lib/clas/*:$COAT/lib/utils/*:../lib/*:src/"


# compile code
javac -cp $classPath src/analysis/SPMCValidation.java
if [ $? != 0 ] ; then echo "SPMCValidation compilation failure" ; exit 1 ; fi

# run code
java -Xmx1536m -Xms1024m -cp $classPath analysis.SPMCValidation /Users/harrison/software_validation/single_e/cooked/out_sim_L33.hipo 4 0.0 10.0 4 0.0 30.0 3 -30.0 30.0
