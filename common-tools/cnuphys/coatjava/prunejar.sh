#!/bin/bash
#prune jars from the big coat java jar so that I can depend on projects

#mv the jar 
COAT=coat-libs-4.0-SNAPSHOT.jar
OLDCOAT=old_$COAT
mv $COAT $OLDCOAT

#make a temp dir
mkdir temp
cd temp

#extract the jar
jar xvf ../$OLDCOAT

#remove the cnuphys jars
rm -fr cnuphys

#rejar
jar cvf $COAT .

#mv new jar into place
mv $COAT ..

#move back up
cd ..

#rm temp dir
rm -fr ./temp

echo "done."

