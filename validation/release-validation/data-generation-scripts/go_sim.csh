#!/bin/csh -f

foreach round (`seq 0 199`)
foreach lumi (33 34 35)

	if(!(-e GEMCoutputFiles/sim_L"$lumi"_"$round".evio)) then

		sed -e "s|LUMINOSITY|$lumi|g" auger-sim-template > temp1
		sed -e "s|ROUND|$round|g" temp1 > temp2
		
		jsub temp2
		rm temp1 temp2

	endif

end
end
