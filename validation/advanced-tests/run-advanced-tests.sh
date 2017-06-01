#!/bin/bash

# coatjava must already be built at ../../coatjava/

# set up environment
CLARA_HOME=$PWD/clara_installation/
COAT=$CLARA_HOME/plugins/clas12/

# tar the local coatjava build so it can be installed with clara
tar -zcvf coatjava-local.tar.gz ../../coatjava/

# install clara
wget --no-check-certificate https://claraweb.jlab.org/clara/_downloads/install-claracre-clas.sh
chmod +x install-claracre-clas.sh
./install-claracre-clas.sh -l local
if [ $? != 0 ] ; then echo "clara installation error" ; exit 1 ; fi
rm install-claracre-clas.sh

# download test files
wget --no-check-certificate http://clasweb.jlab.org/clas12offline/distribution/coatjava/validation_files/GEMCoutputFiles/gen.pid_{-13,-211,13,22,211,2112,2212}.{0..1}.dat.evio

# run evio2hipo convertor
for f in gen.pid_*.0.dat.evio ; do
	pid=`echo $f | cut -f2 -d. | cut -f2 -d_`
	$COAT/bin/evio2hipo -r 11 -t -1.0 -s 1.0 -o pid_"$pid".hipo gen.pid_"$pid".*.dat.evio
	if [ $? != 0 ] ; then echo "evio2hipo error" ; exit 1 ; fi
done

# run reconstruction with clara
echo "set inputDir $PWD/" > cook.clara
echo "set outputDir $PWD/" >> cook.clara
echo "set threads 2" >> cook.clara
echo "set javaMemory 2" >> cook.clara
echo "set session s_cook" >> cook.clara
echo "set description d_cook" >> cook.clara
ls pid*hipo > files.list
echo "set fileList $PWD/files.list" >> cook.clara
echo "run local" >> cook.clara
echo "exit" >> cook.clara
$CLARA_HOME/bin/clara-shell cook.clara
