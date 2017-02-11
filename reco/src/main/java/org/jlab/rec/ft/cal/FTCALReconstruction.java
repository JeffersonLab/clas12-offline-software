package org.jlab.rec.ft.cal;

import java.util.ArrayList;
import java.util.Collections;

import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;


public class FTCALReconstruction {

	

	public int debugMode = 0;
	
	public void processEvent(EvioDataEvent event) {
		// initialize banks
		EvioDataBank bankDGTZ  = null;
		EvioDataBank bankhits  = null;
		EvioDataBank bankclust = null;
		
		if(debugMode>=1) System.out.println("\nAnalyzing new event");

		// CLASDetectorGeometry FTOFGeom = getGeometry();
		
		// getting raw data bank
		if(debugMode>=1) System.out.println("Getting raw hits from FTCAL:dgtz bank");
		
		if(event.hasBank("FTCAL::dgtz")==true) {
			bankDGTZ = (EvioDataBank) event.getBank("FTCAL::dgtz");
			if(bankDGTZ==null) return;
			
			//1) read raw (dgtz) data and reconstruct single hits
			ArrayList<FTCALHit> allhits = FTCALHit.getRawHits(bankDGTZ);
			
			if(debugMode>=1) System.out.println("Found " + allhits.size() + " hits");
			for(int i = 0; i < allhits.size(); i++) {
				if(debugMode>=1) System.out.println(i + " "
							               + allhits.get(i).get_COMPONENT() + " " 
							               + allhits.get(i).get_IDX() + " " 
						                   + allhits.get(i).get_IDY() + " " 
						                   + allhits.get(i).get_ADC() + " " 
								           + allhits.get(i).get_Edep()+ " "
								           + allhits.get(i).get_Time());
			}

			//2) select hits according to chosen selection algorithm and order them by energy
			ArrayList<FTCALHit> hits = new ArrayList<FTCALHit>();
			for(int i = 0; i < allhits.size(); i++) 
			{
				if(FTCALHit.passHitSelection(allhits.get(i))) {
					hits.add(allhits.get(i));	
				}
			}	
			Collections.sort(hits);
			if(debugMode>=1) {
				System.out.println("List of selected hits");
				for(int i = 0; i < hits.size(); i++) 
				{	
					System.out.println(i + " "
									 + hits.get(i).get_COMPONENT() + " " 
									 + hits.get(i).get_IDX()       + " " 
							 		 + hits.get(i).get_IDY()       + " "
							 		 + hits.get(i).get_Edep()      + " "
							 		 + hits.get(i).get_Time()      + " "
							 		 + hits.get(i).get_DGTZIndex() + " "
									 + hits.get(i).get_ClusIndex());
				}
			}

			//3) exit if hit list is empty or find clusters from these hits
			if(hits.size()==0 ) return;	
			FTCALClusterFinder clusFinder = new FTCALClusterFinder();
			ArrayList<FTCALCluster> clusters = clusFinder.findClusters(hits);
				
			//4) write out data banks
			// hits banks
			if(hits.size()!=0) {
				bankhits = (EvioDataBank) event.getDictionary().createBank("FTCALRec::hits",hits.size());
				for(int i=0; i<hits.size(); i++) {
					bankhits.setInt("idx",i,hits.get(i).get_IDX());
					bankhits.setInt("idy",i,hits.get(i).get_IDY());
					bankhits.setDouble("hitX",i,hits.get(i).get_Dx());
					bankhits.setDouble("hitY",i,hits.get(i).get_Dy());
					bankhits.setDouble("hitEnergy",i,hits.get(i).get_Edep());
					bankhits.setDouble("hitTime",i,hits.get(i).get_Time());
					bankhits.setInt("hitDGTZIndex",i,hits.get(i).get_DGTZIndex());
					bankhits.setInt("hitClusterIndex",i,hits.get(i).get_ClusIndex());	
				}				
			}	
			// cluster bank
			if(clusters.size()!=0){
				bankclust = (EvioDataBank) event.getDictionary().createBank("FTCALRec::clusters",clusters.size());
				for(int i =0; i< clusters.size(); i++) {
					if(debugMode>=1) System.out.println(" E = "     + clusters.get(i).get_clusEnergy() + 
														" Theta = " + clusters.get(i).get_clusTheta()  + 
														" Phi = "   + clusters.get(i).get_clusPhi());
					bankclust.setInt("clusID", i,clusters.get(i).get_clusID());
					bankclust.setInt("clusSize", i,clusters.get(i).get_clusSize());
					bankclust.setDouble("clusX",i,clusters.get(i).get_clusX());
					bankclust.setDouble("clusY",i,clusters.get(i).get_clusY());
					bankclust.setDouble("clusXX",i,clusters.get(i).get_clusXX());
					bankclust.setDouble("clusYY",i,clusters.get(i).get_clusYY());
					bankclust.setDouble("clusSigmaX",i,clusters.get(i).get_clusSigmaX());
					bankclust.setDouble("clusSigmaY",i,clusters.get(i).get_clusSigmaY());
					bankclust.setDouble("clusRadius",i,clusters.get(i).get_clusRadius());
					bankclust.setDouble("clusTime",i,clusters.get(i).get_clusTime());
					bankclust.setDouble("clusEnergy",i,clusters.get(i).get_clusEnergy());
					bankclust.setDouble("clusRecEnergy",i,clusters.get(i).get_clusRecEnergy());
					bankclust.setDouble("clusMaxEnergy",i,clusters.get(i).get_clusMaxEnergy());
					bankclust.setDouble("clusTheta",i,clusters.get(i).get_clusTheta());
					bankclust.setDouble("clusPhi",i,clusters.get(i).get_clusPhi());				
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

	
}
