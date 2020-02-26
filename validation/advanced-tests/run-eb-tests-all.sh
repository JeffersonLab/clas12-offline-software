#!/bin/sh
#
# This runs all existing EB tests.
# Requires about 10 minutes to fully succeed (single-threaded), else aborts ASAP.
#

for xx in `awk '{print$1}' src/eb/scripts/list.txt`
do
    if [ "$xx" == "electrongammaC" ]
    then
        echo "This test is ignored (pid is not assigned for central photons): $xx"
        continue
    fi
    ./run-eb-tests.sh -100 $xx
    if [ $? != 0 ]
    then
        echo run-eb-tests.sh:  failed on $xx
        exit
    fi
done

