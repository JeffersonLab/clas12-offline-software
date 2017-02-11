package org.jlab.rec.ft.hodo;

import java.util.ArrayList;

import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;

public class FTHODOReconstruction {


	public int debugMode = 0;
	

	public void processEvent(EvioDataEvent event) {
		EvioDataBank bankDGTZ = null;
		EvioDataBank bankhits = null;
		EvioDataBank bankclust = null;

		if(debugMode>=1) System.out.println("\nAnalyzing new event");

		// getting raw data bank
		if(debugMode>=1) System.out.println("Getting raw hits from FTHODO:dgtz bank");

		if(event.hasBank("FTHODO::dgtz")==true) {
			bankDGTZ = (EvioDataBank) event.getBank("FTHODO::dgtz");
	     
			//1) read raw (dgtz) data and reconstruct single hits
			ArrayList<FTHODOHit> allhits = FTHODOHit.getRawHits(bankDGTZ);
			
			if(debugMode>=1) System.out.println("Found " + allhits.size() + " hits");
			for(int i = 0; i < allhits.size(); i++) {
				if(debugMode>=1) System.out.println(i + " Sector="    + allhits.get(i).get_Sector() 
													  + " Layer="     + allhits.get(i).get_Layer() 
													  + " ID="        + allhits.get(i).get_ID() 
													  + " ADC="       + allhits.get(i).get_ADC() 
													  + " Edep(MeV)=" + allhits.get(i).get_Edep()
													  + " Time(ns)="  + allhits.get(i).get_Time());
			}
			
			//2) select hits according to chosen selection algorithm
			ArrayList<FTHODOHit> hits = new ArrayList<FTHODOHit>();
			for(int i = 0; i < allhits.size(); i++) 
			{
				if(FTHODOHit.passHitSelection(allhits.get(i))) {
					hits.add(allhits.get(i));	
				}
			}	
			if(debugMode>=1) {
				System.out.println("List of selected hits");
				for(int i = 0; i < hits.size(); i++) 
				{	
					System.out.println(i + " Sector="     + hits.get(i).get_Sector() 
							             + " Layer="      + hits.get(i).get_Layer() 
							             + " ID="         + hits.get(i).get_ID() 
							             + " ADC="        + hits.get(i).get_ADC() 
							             + " Edep(MeV)="  + hits.get(i).get_Edep()
							             + " Time(ns)="   + hits.get(i).get_Time()
							             + " DGTZ Index=" + hits.get(i).get_DGTZIndex() 
							             + " Sig. Index=" + hits.get(i).get_SignalIndex());
				}
			}
			
			//3) exit if hit list is empty or find clusters from these hits
			if(hits.size()==0 ) return;	
			FTHODOSignalFinder clusFinder = new FTHODOSignalFinder();
			ArrayList<FTHODOSignal> signals = clusFinder.findSignals(hits);

			
			//4) write out data banks
			// hits banks
			if(hits.size()!=0) {
				bankhits = (EvioDataBank) event.getDictionary().createBank("FTHODORec::hits",hits.size());
				for(int i=0; i<hits.size(); i++) {
					bankhits.setInt("id",i,hits.get(i).get_ID());
					bankhits.setInt("sector",i,hits.get(i).get_Sector());
					bankhits.setInt("layer",i,hits.get(i).get_Layer());
					bankhits.setDouble("hitX",i,hits.get(i).get_Dx());
					bankhits.setDouble("hitY",i,hits.get(i).get_Dy());
					bankhits.setDouble("hitEnergy",i,hits.get(i).get_Edep());
					bankhits.setDouble("hitTime",i,hits.get(i).get_Time());
					bankhits.setInt("hitDGTZIndex",i,hits.get(i).get_DGTZIndex());
					bankhits.setInt("hitSignalIndex",i,hits.get(i).get_SignalIndex());
				}				
			}
			// signal bank
			if(signals.size()!=0){
				bankclust = (EvioDataBank) event.getDictionary().createBank("FTHODORec::signals",signals.size());
				for(int i =0; i< signals.size(); i++) {
					if(debugMode>=1) System.out.println(signals.get(i).get_signalEnergy());
					bankclust.setInt("signalID", i,signals.get(i).get_signalID());
					bankclust.setInt("signalSize", i,signals.get(i).size());
					bankclust.setDouble("signalX",i,signals.get(i).get_signalX());
					bankclust.setDouble("signalY",i,signals.get(i).get_signalY());
					bankclust.setDouble("signalDX",i,signals.get(i).get_signalDX());
					bankclust.setDouble("signalDY",i,signals.get(i).get_signalDY());
					bankclust.setDouble("signalTime",i,signals.get(i).get_signalTime());
					bankclust.setDouble("signalEnergy",i,signals.get(i).get_signalEnergy());
					bankclust.setDouble("signalTheta",i,signals.get(i).get_signalTheta());
					bankclust.setDouble("signalPhi",i,signals.get(i).get_signalPhi());				
				}
			}
			
			// If there are no clusters, punt here but save the reconstructed hits 
			if(bankclust!=null) {
				event.appendBanks(bankhits,bankclust);
			}
			else if (bankhits!=null) {
				event.appendBank(bankhits);
			}
		}
	
	}


}
