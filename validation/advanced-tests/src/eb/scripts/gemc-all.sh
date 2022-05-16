#!/bin/sh
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
gemc=${DIR}/gemc.sh
list=${DIR}/list.txt

for xx in `awk '{print$1}' $list`
do
    $gemc $xx
done
