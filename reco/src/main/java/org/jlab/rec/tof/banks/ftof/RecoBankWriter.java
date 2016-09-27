package org.jlab.rec.tof.banks.ftof;

import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.rec.tof.cluster.Cluster;
import org.jlab.rec.tof.hit.ftof.Hit;

/**
 * 
 * @author ziegler
 *
 */
public class RecoBankWriter {

	public RecoBankWriter() {
		// TODO Auto-generated constructor stub
	}

	public static EvioDataBank fillRawHitsBank(EvioDataEvent event, List<Hit> hitlist) {
		if(hitlist==null)
			return null;
		if(hitlist.size()==0)
			return null;
		
		EvioDataBank bank = (EvioDataBank)
		          event.getDictionary().createBank("FTOFRec::rawhits", hitlist.size());

		for(int i =0; i< hitlist.size(); i++) {
			bank.setInt("sector",i, hitlist.get(i).get_Sector());
			bank.setInt("panel_id",i, hitlist.get(i).get_Panel());
			bank.setInt("paddle_id",i, hitlist.get(i).get_Paddle());
			bank.setInt("paddle_status",i, Integer.parseInt(hitlist.get(i).get_StatusWord()));
			bank.setFloat("energy_left",i, (float) hitlist.get(i).get_Energy1());
			bank.setFloat("energy_right",i, (float) hitlist.get(i).get_Energy2());
			bank.setFloat("energy_left_unc",i, (float) hitlist.get(i).get_Energy1Unc()); 		
			bank.setFloat("energy_right_unc",i, (float) hitlist.get(i).get_Energy2Unc()); 		
			bank.setFloat("time_left",i, (float) hitlist.get(i).get_t1());
			bank.setFloat("time_right",i, (float) hitlist.get(i).get_t2());
			bank.setFloat("time_left_unc",i, (float) hitlist.get(i).get_t1Unc()); 			
			bank.setFloat("time_right_unc",i, (float) hitlist.get(i).get_t2Unc()); 			
			
		}

		return bank;

	}
	
	public static EvioDataBank fillRecHitsBank(EvioDataEvent event, List<Hit> hitlist) {
		if(hitlist==null)
			return null;
		if(hitlist.size()==0)
			return null;
		
		EvioDataBank bank = (EvioDataBank)
		          event.getDictionary().createBank("FTOFRec::ftofhits", hitlist.size());

		for(int i =0; i< hitlist.size(); i++) {
			bank.setInt("sector",i, hitlist.get(i).get_Sector());
			bank.setInt("panel_id",i, hitlist.get(i).get_Panel());
			bank.setInt("paddle_id",i, hitlist.get(i).get_Paddle());
			bank.setInt("paddle_status",i, Integer.parseInt(hitlist.get(i).get_StatusWord()));
			bank.setFloat("energy",i, (float) hitlist.get(i).get_Energy());
			bank.setFloat("energy_unc",i, (float) hitlist.get(i).get_EnergyUnc()); 				
			bank.setFloat("time",i, (float) hitlist.get(i).get_t());
			bank.setFloat("time_unc",i, (float) hitlist.get(i).get_tUnc()); 				
			bank.setFloat("x",i, (float) hitlist.get(i).get_Position().x());
			bank.setFloat("y",i, (float) hitlist.get(i).get_Position().y());
			bank.setFloat("z",i, (float) hitlist.get(i).get_Position().z());
			bank.setFloat("x_unc",i, 5); 			
			bank.setFloat("y_unc",i, (float) hitlist.get(i).get_yUnc()); 			
			bank.setFloat("z_unc",i, 10); 						
		}

		return bank;

	}
	
	public static EvioDataBank fillClustersBank(EvioDataEvent event, List<Cluster> cluslist) {
		if(cluslist==null)
			return null;
		if(cluslist.size()==0)
			return null;
		
		EvioDataBank bank = (EvioDataBank)
		          event.getDictionary().createBank("FTOFRec::ftofclusters", cluslist.size());

		for(int i =0; i< cluslist.size(); i++) {
			bank.setInt("sector",i, cluslist.get(i).get_Sector());
			bank.setInt("panel_id",i, cluslist.get(i).get_Panel());
			bank.setInt("paddle_id",i, cluslist.get(i).get(0).get_Paddle());		// paddle id of hit with lowest paddle id in cluster [Check the sorting!!!]
			bank.setInt("paddle_status",i, Integer.parseInt(cluslist.get(i).get_StatusWord()));
			bank.setFloat("energy",i, (float) cluslist.get(i).get_Energy());
			bank.setFloat("energy_unc",i, (float) cluslist.get(i).get_EnergyUnc()); 										
			bank.setFloat("time",i, (float) cluslist.get(i).get_t());
			bank.setFloat("time_unc",i, (float) cluslist.get(i).get_tUnc()); 										
			bank.setFloat("x",i, (float) cluslist.get(i).get_x());
			bank.setFloat("y",i, (float) cluslist.get(i).get_y());
			bank.setFloat("z",i, (float) cluslist.get(i).get_z());
			bank.setFloat("x_unc",i, 5); 											// At this stage the uncertainty is not calculated
			bank.setFloat("y_unc",i, (float) cluslist.get(i).get_y_locUnc()); 		
			bank.setFloat("z_unc",i, 10); 											// At this stage the uncertainty is not calculated

		}

		return bank;

	}
	
	private static DataBank fillClustersBank(EvioDataEvent event,
			ArrayList<ArrayList<Cluster>> matchedClusters) {
		if(matchedClusters==null)
			return null;
		if(matchedClusters.size()==0)
			return null;
		
		EvioDataBank bank = (EvioDataBank)
		          event.getDictionary().createBank("FTOFRec::ftofmatchedclusters", matchedClusters.size());
		
		for(int i =0; i< matchedClusters.size(); i++) {
			if(matchedClusters.get(i)== null)
				continue;
			bank.setInt("sector",i, matchedClusters.get(i).get(0).get_Sector());
			bank.setInt("paddle_id1A",i,  matchedClusters.get(i).get(0).get(0).get_Paddle());		  // paddle id of hit with lowest paddle id in cluster [Check the sorting!!!]
			bank.setInt("paddle_id1B",i,  matchedClusters.get(i).get(1).get(0).get_Paddle());		  // paddle id of hit with lowest paddle id in cluster [Check the sorting!!!]			
			bank.setInt("clusSize_1A",i,  matchedClusters.get(i).get(0).size());					  // size of cluster in 1a
			bank.setInt("clusSize_1B",i,  matchedClusters.get(i).get(1).size());					  // size of cluster in 1b
			bank.setFloat("tminAlgo_1B_tCorr",i,    (float) matchedClusters.get(i).get(1).get_tCorr()[0]);	  // uses tmin algorithm to compute the path length between counters
			bank.setFloat("midbarAlgo_1B_tCorr",i,  (float) matchedClusters.get(i).get(1).get_tCorr()[1]);    // uses middle of bar algorithm to compute the path length between counters
			bank.setFloat("EmaxAlgo_1B_tCorr",i,    (float) matchedClusters.get(i).get(1).get_tCorr()[2]);	  // uses Emax algorithm to compute the path length between counters
		}
		return bank;
	}
	
	public static void appendFTOFBanks(EvioDataEvent event,
			List<Hit> hits, List<Cluster> clusters, ArrayList<ArrayList<Cluster>> matchedClusters) {
		List<DataBank> fTOFBanks = new ArrayList<DataBank>();
		
		DataBank bank1 = RecoBankWriter.fillRawHitsBank((EvioDataEvent) event, hits);	
		if(bank1!=null)
			fTOFBanks.add(bank1);
		
		DataBank bank2 = RecoBankWriter.fillRecHitsBank((EvioDataEvent) event, hits);	
		if(bank2!=null)
			fTOFBanks.add(bank2);
		
		DataBank bank3 = RecoBankWriter.fillClustersBank((EvioDataEvent) event, clusters);
		if(bank3!=null)
			fTOFBanks.add(bank3);
				
		DataBank bank4 = RecoBankWriter.fillClustersBank((EvioDataEvent) event, matchedClusters);
		if(bank4!=null)
			fTOFBanks.add(bank4);
		
		if(fTOFBanks.size()==4) 
			event.appendBanks(fTOFBanks.get(0),fTOFBanks.get(1), fTOFBanks.get(2), fTOFBanks.get(3));
		if(fTOFBanks.size()==3) 
			event.appendBanks(fTOFBanks.get(0),fTOFBanks.get(1), fTOFBanks.get(2));
		if(fTOFBanks.size()==2)
			event.appendBanks(fTOFBanks.get(0),fTOFBanks.get(1));
		if(fTOFBanks.size()==1)
			event.appendBanks(fTOFBanks.get(0));
		
	}

	

}
