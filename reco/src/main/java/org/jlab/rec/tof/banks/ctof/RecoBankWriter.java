package org.jlab.rec.tof.banks.ctof;

import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.rec.tof.cluster.Cluster;
import org.jlab.rec.tof.hit.ctof.Hit;

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
		          event.getDictionary().createBank("CTOFRec::rawhits", hitlist.size());

		for(int i =0; i< hitlist.size(); i++) {
			bank.setInt("paddle_id",i, hitlist.get(i).get_Paddle());
			bank.setInt("paddle_status",i, Integer.parseInt(hitlist.get(i).get_StatusWord()));
			bank.setFloat("energy_up",i, (float) hitlist.get(i).get_Energy1());
			bank.setFloat("energy_down",i, (float) hitlist.get(i).get_Energy2());
			bank.setFloat("energy_up_unc",i, (float) hitlist.get(i).get_Energy1Unc()); 		
			bank.setFloat("energy_down_unc",i, (float) hitlist.get(i).get_Energy2Unc()); 		
			bank.setFloat("time_up",i, (float) hitlist.get(i).get_t1());
			bank.setFloat("time_down",i, (float) hitlist.get(i).get_t2());
			bank.setFloat("time_up_unc",i, (float) hitlist.get(i).get_t1Unc()); 			
			bank.setFloat("time_down_unc",i, (float) hitlist.get(i).get_t2Unc()); 			
			
		}

		return bank;

	}
	public static EvioDataBank fillRecHitsBank(EvioDataEvent event, List<Hit> hitlist) {
		if(hitlist==null)
			return null;
		if(hitlist.size()==0)
			return null;
		
		EvioDataBank bank = (EvioDataBank)
		          event.getDictionary().createBank("CTOFRec::ctofhits", hitlist.size());

		for(int i =0; i< hitlist.size(); i++) {
			bank.setInt("paddle",i, hitlist.get(i).get_Paddle());
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
		          event.getDictionary().createBank("CTOFRec::ctofclusters", cluslist.size());

		for(int i =0; i< cluslist.size(); i++) {
			bank.setInt("paddle",i, cluslist.get(i).get(0).get_Paddle());		// paddle id of hit with lowest paddle id in cluster [Check the sorting!!!]
			bank.setInt("paddle_status",i, Integer.parseInt(cluslist.get(i).get_StatusWord()));
			bank.setFloat("energy",i, (float) cluslist.get(i).get_Energy());
			bank.setFloat("energy_unc",i, (float) cluslist.get(i).get_EnergyUnc()); 										
			bank.setFloat("time",i, (float) cluslist.get(i).get_t());
			bank.setFloat("time_unc",i, (float) cluslist.get(i).get_tUnc()); 										
			bank.setFloat("x",i, (float) cluslist.get(i).get_x());
			bank.setFloat("y",i, (float) cluslist.get(i).get_y());
			bank.setFloat("z",i, (float) cluslist.get(i).get_z());
			bank.setFloat("x_unc",i, 5); 											
			bank.setFloat("y_unc",i, (float) cluslist.get(i).get_y_locUnc()); 		
			bank.setFloat("z_unc",i, 10); 											
			
		}

		return bank;

	}
	
	public static void appendCTOFBanks(EvioDataEvent event,
			List<Hit> hits, List<Cluster> clusters) {
		List<DataBank> cTOFBanks = new ArrayList<DataBank>();
		
	
		DataBank bank1 = RecoBankWriter.fillRawHitsBank((EvioDataEvent) event, hits);	
		if(bank1!=null)
			cTOFBanks.add(bank1);
		
		DataBank bank2 = RecoBankWriter.fillRecHitsBank((EvioDataEvent) event, hits);	
		if(bank2!=null)
			cTOFBanks.add(bank2);
		
		DataBank bank3 = RecoBankWriter.fillClustersBank((EvioDataEvent) event, clusters);
		if(bank3!=null)
			cTOFBanks.add(bank3);
				
		
		
		if(cTOFBanks.size()==3)
			event.appendBanks(cTOFBanks.get(0),cTOFBanks.get(1),cTOFBanks.get(2));
		if(cTOFBanks.size()==2)
			event.appendBanks(cTOFBanks.get(0),cTOFBanks.get(1));
		if(cTOFBanks.size()==1)
			event.appendBanks(cTOFBanks.get(0));
		
	}

}
