#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
run=${DIR}/../../../../../coatjava/bin/run-groovy
gen=${DIR}/gen.groovy
lst=${DIR}/list.txt

if [ "$#" -eq 0 ]
then
    while read line
    do
        line=`echo $line | sed 's/.* -pid/-pid/'`
        $run $gen $line
    done < $lst
else
  $run $gen $@
fi
