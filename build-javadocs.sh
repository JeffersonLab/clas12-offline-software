#!/bin/sh
#
# surely this should be done more properly with only maven, meanwhile ...
#

mvn javadoc:javadoc -Ddoclint=none 

src=target/site/apidocs
dest=docs/javadoc

for dir in `find . -type d | grep $src$`
do
    mkdir -p $dest/${dir%$src}
    cp -r $dir/* $dest/${dir%$src}
done

#scp -r docs/javadoc/* ifarm:/group/clas/www/clasweb/html/clas12offline/docs/javadoc

