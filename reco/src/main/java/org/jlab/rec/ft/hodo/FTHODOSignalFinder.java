package org.jlab.rec.ft.hodo;

import java.util.ArrayList;

import org.jlab.rec.ft.hodo.FTHODOHit;

public class FTHODOSignalFinder {

	public FTHODOSignalFinder(){
		// empty constructor
	}

	FTHODOSignal[] SignalArray;
	public ArrayList<FTHODOSignal> findSignals(ArrayList<FTHODOHit> hitlist)
	{
		
		ArrayList<FTHODOSignal> allSignals = new ArrayList<FTHODOSignal>();
		
		int signalNb =0;
		if(hitlist.size()>0) {
			
			SignalArray = new FTHODOSignal[hitlist.size()];
					
			for(int i = 0; i< hitlist.size(); i++) {
				if(hitlist.get(i).get_SignalIndex()==0)  // this hit is not yet associated with a cluster
				{		
					signalNb++;
					hitlist.get(i).set_SignalIndex(signalNb);
					// DUMMY ALGORITHM TO BE REPLACED WITH REAL ONE					
					SignalArray[i] = new FTHODOSignal(i);	
					SignalArray[i].add(hitlist.get(i));
				}
			}
		}
		for(int n = 0; n< signalNb; n++ ) {
			double signalEWeightedTime =0;
			double signalEnergy =0;
			double signalX =0;
			double signalY =0;
			double signalZ =0;
			double signalDX =0;
			double signalDY =0;
			
			// loop over the hits in the  cluster
			for(FTHODOHit hit_in_clus : SignalArray[n]) {
				signalEnergy += hit_in_clus.get_Edep();                                            // the reconstructed energy for all hits is added to give the reconstructed energy of the cluster before corrections
				signalEWeightedTime += hit_in_clus.get_Edep()*hit_in_clus.get_Time();              // the energy weighted time distribution of the cluster
				// the moments:
				signalX += hit_in_clus.get_Edep()*hit_in_clus.get_Dx();
				signalY += hit_in_clus.get_Edep()*hit_in_clus.get_Dy();
				signalZ += hit_in_clus.get_Edep()*hit_in_clus.get_Dz();
			}
			
			// time
			signalEWeightedTime /= signalEnergy;
			SignalArray[n].set_signalTime(signalEWeightedTime);                             // the energy-normalized energy-weighted time distribution 
			
			//energy
			SignalArray[n].set_signalEnergy(signalEnergy); 
			
			// position
			signalX /= signalEnergy;
			signalY /= signalEnergy;
			signalZ /= signalEnergy;
			
			SignalArray[n].set_signalX(signalX);
			SignalArray[n].set_signalY(signalY);
			SignalArray[n].set_signalZ(signalZ);

			// delta in X & Y
			SignalArray[n].set_signalDX(signalDX);
			SignalArray[n].set_signalDY(signalDY);
			

			// angles
			double calcTheta = Math.atan(Math.sqrt(signalX*signalX + signalY*signalY)/signalZ)*180./Math.PI;
			SignalArray[n].set_signalTheta(calcTheta);
			
			double calcPhi = Math.atan2(signalY, signalX)*180./Math.PI;
			SignalArray[n].set_signalPhi(calcPhi);
			
			// add all cluster to arrayList of clusters
			allSignals.add(SignalArray[n]);
			// set the ID according to the order the cluster is added to the list
			allSignals.get(allSignals.size()-1).set_signalID(allSignals.size()-1);
		}
		
		return allSignals;
		
	}	

	
/*	public boolean passClusterSelection(CalHit hit1, CalHit hit2) {
		double tDiff = Math.abs(hit1.get_Time() - hit2.get_Time());
		double xDiff = Math.abs(hit1.get_IDX() - hit2.get_IDX());
		double yDiff = Math.abs(hit1.get_IDY() - hit2.get_IDY());
		if(tDiff <= CalorimeterConstants.TIME_WINDOW && xDiff <= 1 && yDiff <= 1 && (xDiff + yDiff) >0) {
			return true;
		} else {
			return false;
		}
	} */
	

	
	
}
