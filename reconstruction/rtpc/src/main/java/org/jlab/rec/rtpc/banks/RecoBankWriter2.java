package org.jlab.rec.rtpc.banks;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Vector;

import org.jlab.clas.physics.Vector3;

//import java.util.List;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
//import org.jlab.rec.rtpc.hit.Hit;
import org.jlab.rec.rtpc.hit.HitParameters;
import org.jlab.rec.rtpc.hit.HitVector;
import org.jlab.rec.rtpc.hit.RecoHitVector;
import java.util.HashMap;

public class RecoBankWriter2 {

	/**
	 * 
	 * @param hitlist the list of  hits that are of the type Hit.
	 * @return hits bank
	 *
	 */
	public  DataBank fillRTPCHitsBank(DataEvent event, HitParameters params) {
		/*if(hitlist==null)
			return null;
		if(hitlist.size()==0)
			return null;*/
		int listsize = 0;
		int row = 0;
		HashMap<Integer, Vector<RecoHitVector>> recohitvector = params.get_recohitvector();
		
		for(int TID:recohitvector.keySet()) {
			for(int i = 0; i < recohitvector.get(TID).size(); i++) {
				listsize++;
			}
		}

		DataBank bank = event.createBank("RTPC::rec", listsize);

		if (bank == null) {
            System.err.println("COULD NOT CREATE A BANK!!!!!!");
            return null;
        }
		
		for(int TID : recohitvector.keySet()) {
			for(int i = 0; i < recohitvector.get(TID).size(); i++) {
				int cellID = recohitvector.get(TID).get(i).pad();
				double x_rec = recohitvector.get(TID).get(i).x();
				double y_rec = recohitvector.get(TID).get(i).y();
				double z_rec = recohitvector.get(TID).get(i).z();
				double time  = recohitvector.get(TID).get(i).time();
				double tdiff = recohitvector.get(TID).get(i).dt();
				
				bank.setInt("TID", row, TID);
				bank.setInt("cellID", row, cellID);
				bank.setFloat("time", row, (float) time);
				bank.setFloat("posX", row, (float) x_rec);
				bank.setFloat("posY", row, (float) y_rec);
				bank.setFloat("posZ", row, (float) z_rec);				
				bank.setFloat("tdiff", row, (float) tdiff);
				
				row++;
			}
		}
		/*for(int i =0; i< listsize; i++) {
			double x_rec = alltracks.get(k
			double y_rec = params.get_YVec().get(i);
			double z_rec = params.get_ZVec().get(i);
			double time = params.get_time().get(i);
			//System.out.println(params.get_time().size());
			bank.setInt("id", i, 1);
			bank.setInt("cellID",i, params.get_PadNum().get(i));
			bank.setFloat("posX",i, (float) x_rec);
			bank.setFloat("posY",i, (float) y_rec);
			bank.setFloat("posZ",i, (float) z_rec);
			//bank.setDouble("Edep",i, hitlist.get(i).get_Edep());
			bank.setFloat("time", i, (float) time);

          
		}*/

		return bank;

	}

	
	
}