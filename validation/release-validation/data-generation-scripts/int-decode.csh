#!/bin/csh -f

#foreach f (GEMCoutputFiles/*)
#
#	setenv fileName `basename $f`
#	setenv lumi `echo $fileName | cut -c6-7`
#	setenv round `echo $fileName | cut -f3 -d_ | cut -f1 -d.`
#
#	if(!(-e GEMCoutputFiles_hipo/sim_L"$lumi"_"$round".hipo)) then
#		$COATJAVA/bin/evio2hipo -r 11 -t -1.0 -s 1.0 -o GEMCoutputFiles_hipo/sim_L"$lumi"_"$round".hipo $f
#	endif
#
#end

#$COATJAVA/bin/evio2hipo -r 11 -t -1.0 -s 1.0 -o GEMCoutputFiles_hipo/sim_L33.hipo GEMCoutputFiles/sim_L33_*.evio
#$COATJAVA/bin/evio2hipo -r 11 -t -1.0 -s 1.0 -o GEMCoutputFiles_hipo/sim_L34.hipo GEMCoutputFiles/sim_L34_*.evio
$COATJAVA/bin/evio2hipo -r 11 -t -1.0 -s 1.0 -o GEMCoutputFiles_hipo/sim_L35.hipo GEMCoutputFiles/sim_L35_*.evio
