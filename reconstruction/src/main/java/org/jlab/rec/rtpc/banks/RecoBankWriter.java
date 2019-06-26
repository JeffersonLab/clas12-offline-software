package org.jlab.rec.rtpc.banks;

import java.util.List;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.rtpc.hit.Hit;

public class RecoBankWriter {

	/**
	 * 
	 * @param hitlist the list of  hits that are of the type Hit.
	 * @return hits bank
	 *
	 */
	public  DataBank fillRTPCHitsBank(DataEvent event, List<Hit> hitlist) {
		if(hitlist==null)
			return null;
		if(hitlist.size()==0)
			return null;
		

		DataBank bank = event.createBank("RTPC::rec", hitlist.size());

		for(int i =0; i< hitlist.size(); i++) {
			//System.out.println(hitlist.get(i).get_PosX());
			bank.setInt("id", i, hitlist.get(i).get_Id());
			bank.setInt("cellID",i, hitlist.get(i).get_cellID());
			bank.setFloat("posX",i, (float) hitlist.get(i).get_PosX());
			bank.setFloat("posY",i, (float) hitlist.get(i).get_PosY());
			bank.setFloat("posZ",i, (float) hitlist.get(i).get_PosZ());
			//bank.setDouble("Edep",i, hitlist.get(i).get_Edep());
			bank.setFloat("time", i, (float) hitlist.get(i).get_Time());

          
		}

		return bank;

	}

	
	
}
