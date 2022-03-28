#!/bin/sh -f

# User-dependant directories.
INPUT_DIR="~/data/hipo-in"
OUTPUT_DIR="~/data/hipo-out"

# Set up environment. Clara is assumed to be installed in $PWD/clara_installation
CLARA_HOME=$PWD/clara_installation/; export CLARA_HOME
COAT=$CLARA_HOME/plugins/clas12/
classPath="$COAT/lib/services/*:$COAT/lib/clas/*:$COAT/lib/utils/*:../lib/*:src/"

# Copy the coatjava installation to clara.
cp -r ../../coatjava $CLARA_HOME/plugins
mv clara_installation/plugins/coatjava $CLARA_HOME/plugins/clas12

# Run reconstruction with clara.
echo "set inputDir $INPUT_DIR" > cook.clara
echo "set outputDir $OUTPUT_DIR" >> cook.clara
echo "set threads 1" >> cook.clara
echo "set javaOptions \"-Xmx2g -Djava.util.logging.config.file=$PWD/../../etc/logging/debug.properties\"" >> cook.clara
echo "set session s_cook" >> cook.clara
echo "set description d_cook" >> cook.clara
echo "clas_005038.1231.hipo" > files.list
echo "set fileList $PWD/files.list" >> cook.clara
echo "set servicesFile $CLARA_HOME/plugins/clas12/config/data.yaml" >> cook.clara
echo "run local" >> cook.clara
echo "exit" >> cook.clara
$CLARA_HOME/bin/clara-shell cook.clara

rm cook.clara
rm files.list
