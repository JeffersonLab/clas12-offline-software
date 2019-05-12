#!/bin/sh
#
# surely this should be done more properly with only maven, meanwhile ...
#

mvn javadoc:javadoc -Ddoclint=none 

src=target/site/apidocs
dest=docs/javadoc

rm -rf $dest

for dir in `find common-tools/*/$src reconstruction/*/$src -maxdepth 0 -mindepth 0 -type d`
do
    topName=${dir%%/*}
    packageName=${dir#*/}
    packageName=${packageName%%/*}
    destDir=$dest/$topName

    mkdir -p $destDir

    cp -r $dir $destDir/$packageName

done

