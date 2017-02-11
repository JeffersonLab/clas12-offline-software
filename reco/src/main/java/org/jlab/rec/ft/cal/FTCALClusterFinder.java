package org.jlab.rec.ft.cal;

import java.util.ArrayList;

import org.jlab.rec.ft.cal.FTCALConstantsLoader;
import org.jlab.rec.ft.cal.FTCALHit;


public class FTCALClusterFinder {

	public FTCALClusterFinder(){
		// empty constructor
	}

	FTCALCluster[] ClusArray;
	public ArrayList<FTCALCluster> findClusters(ArrayList<FTCALHit> hitlist)
	{
		
//		FTCALConstants.LoadCorrections();
		ArrayList<FTCALCluster> allClus = new ArrayList<FTCALCluster>();
		
		int clusNb =0;
		if(hitlist.size()>0) {
			
			ClusArray = new FTCALCluster[hitlist.size()+1];
//			int[] hitIndex = new int[hitlist.size()];
//			int[] clusIndex = new int[hitlist.size()];
			

//			Collections.sort(hitlist);

//			System.out.println("Second Time Hitlistcopy");
//			for(int i = 0; i < hitlist.size(); i++) 
//			{
//				hitIndex[i] = i;
//				System.out.println(hitlist.get(i).get_IDX()  + " " 
//							     + hitlist.get(i).get_IDY()  + " "
//							     + hitlist.get(i).get_Edep() + " "
//							     + hitlist.get(i).get_Time());
//			}
			
			for(int i = 0; i< hitlist.size(); i++) {
//				int ii = hitIndex[i];
//				if(clusIndex[ii]==0)  // this hit is not yet associated with a cluster
				if(hitlist.get(i).get_ClusIndex()==0)  // this hit is not yet associated with a cluster
				{
					for(int j = 0; j< hitlist.size(); j++) {
//						int jj = hitIndex[j];
//						if(clusIndex[jj]>0 && jj!=ii) 
						if(hitlist.get(j).get_ClusIndex()>0 && j!=i) 
							if(passClusterSelection(hitlist.get(i),hitlist.get(j)))
//								clusIndex[ii] = clusIndex[jj];  // attaching hit to previous cluster
								hitlist.get(i).set_ClusIndex(hitlist.get(j).get_ClusIndex());  // attaching hit to previous cluster
					}
//					if(clusIndex[ii] == 0 ) // new cluster found
					if(hitlist.get(i).get_ClusIndex() == 0 ) // new cluster found
					{
						clusNb++;
//						clusIndex[ii] = clusNb;
						hitlist.get(i).set_ClusIndex(clusNb);
						// create a new cluster						
						ClusArray[clusNb] = new FTCALCluster(clusNb);
					}
				}
			}
			//
			for(int i = 0; i< hitlist.size(); i++) {
//				int ii = hitIndex[i];
				for(int n = 1; n<= clusNb; n++ ) {
//					if(clusIndex[ii] == n ) {
					if(hitlist.get(i).get_ClusIndex() == n ) {
						ClusArray[n].add(hitlist.get(i));
						
					}
				}
			}
		}
		for(int n = 1; n<= clusNb; n++ ) {
			int    clusN=0;
			double clusEWeightedTime =0;
			double clusEnergy =0;
			double clusMaxEnergy =0;
			double clusX =0;
			double clusY =0;
			double clusXX =0;
			double clusYY =0;
			double wtot=0;
			
			// loop over the hits in the  cluster
			for(FTCALHit hit_in_clus : ClusArray[n]) {
				clusN++;
				if(hit_in_clus.get_Edep()>clusMaxEnergy)
					clusMaxEnergy=hit_in_clus.get_Edep();
				clusEnergy += hit_in_clus.get_Edep();                                            // the reconstructed energy for all hits is added to give the reconstructed energy of the cluster before corrections
				clusEWeightedTime += hit_in_clus.get_Edep()*hit_in_clus.get_Time();              // the energy weighted time distribution of the cluster
			}
			for(FTCALHit hit_in_clus : ClusArray[n]) {  			
				// the moments: this are calculated in a second loop because log weighting requires clusEnergy to be known
//				double wi = hit_in_clus.get_Edep();    // de-comment for arithmetic weighting
				double wi = Math.max(0., (3.45+Math.log(hit_in_clus.get_Edep()/clusEnergy)));
				wtot += wi;
				clusX += wi*hit_in_clus.get_Dx();
				clusY += wi*hit_in_clus.get_Dy();
				clusXX += wi*hit_in_clus.get_Dx()*hit_in_clus.get_Dx();
				clusYY += wi*hit_in_clus.get_Dy()*hit_in_clus.get_Dy();
			}
			
			// number of crystals in the cluster
			ClusArray[n].set_clusSize(clusN);

			// measured energies
			ClusArray[n].set_clusRecEnergy(clusEnergy);
			ClusArray[n].set_clusMaxEnergy(clusMaxEnergy);

			// the energy-weighted time distribution 			
			clusEWeightedTime /= clusEnergy;
			ClusArray[n].set_clusTime(clusEWeightedTime);                             
			
			// normalized moments
			clusX /= wtot;
			clusY /= wtot;
			clusXX /= wtot;
			clusYY /= wtot;			
			ClusArray[n].set_clusX(clusX);
			ClusArray[n].set_clusY(clusY);
			ClusArray[n].set_clusXX(clusXX);
			ClusArray[n].set_clusYY(clusYY);
			
			// sigma in X & Y
			ClusArray[n].set_clusSigmaX( Math.sqrt(clusXX - clusX*clusX) );
			ClusArray[n].set_clusSigmaY( Math.sqrt(clusYY - clusY*clusY) );
			// radius from moments
			ClusArray[n].set_clusRadius( Math.sqrt(clusXX - clusX*clusX + clusYY - clusY*clusY) );
			
			// energy correction
			double energyCorr = FTCALConstantsLoader.energy_corr[0] + FTCALConstantsLoader.energy_corr[1]*clusEnergy + FTCALConstantsLoader.energy_corr[2]*clusEnergy*clusEnergy;
			clusEnergy+=energyCorr;
			ClusArray[n].set_clusEnergy(clusEnergy);
			

			// angles
			double calcTheta = Math.atan(Math.sqrt(clusX*clusX + clusY*clusY)/(FTCALConstantsLoader.CRYS_ZPOS+FTCALConstantsLoader.depth_z))*180./Math.PI;
			ClusArray[n].set_clusTheta(calcTheta);
			double calcPhi = Math.atan2(clusY, clusX)*180./Math.PI;                                   						
			ClusArray[n].set_clusPhi(calcPhi);

			
			// add all cluster to arrayList of clusters
			allClus.add(ClusArray[n]);
			// set the ID according to the order the cluster is added to the list
			allClus.get(allClus.size()-1).set_clusID(allClus.size()-1);
		}
		
		return allClus;
		
	}	

	
	public boolean passClusterSelection(FTCALHit hit1, FTCALHit hit2) {
		double tDiff = Math.abs(hit1.get_Time() - hit2.get_Time());
		double xDiff = Math.abs(hit1.get_IDX() - hit2.get_IDX());
		double yDiff = Math.abs(hit1.get_IDY() - hit2.get_IDY());
		if(tDiff <= FTCALConstantsLoader.time_window && xDiff <= 1 && yDiff <= 1 && (xDiff + yDiff) >0) {
			return true;
		} else {
			return false;
		}
	}
	
	
}
