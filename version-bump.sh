#!/bin/bash

if [ -z $2 ]
then
    echo Usage:  bump-version.sh oldversion newversion
    exit
fi

old=$1
new=$2

if [[ "$OSTYPE" == "darwin"* ]]; then
  find . -type f -name pom.xml -exec sed -i '' -e "s/$old-SNAPSHOT/$new-SNAPSHOT/" "{}" \;
else
  find . -type f -name pom.xml -exec sed -i -e "s/$old-SNAPSHOT/$new-SNAPSHOT/" "{}" \;
fi


