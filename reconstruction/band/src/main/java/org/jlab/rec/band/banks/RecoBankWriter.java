package org.jlab.rec.band.banks;

import java.util.ArrayList;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.band.hit.BandHit;


public class RecoBankWriter {


	// write useful information in the bank
	public static DataBank fillBandHitBanks(DataEvent event, ArrayList<BandHit> hitlist) {

		DataBank bank =  event.createBank("BAND::hits", hitlist.size());
		
		if (bank == null) {
			System.err.println("COULD NOT CREATE A BAND::Hits BANK!!!!!!");
			return null;
		}

		if (hitlist.size() !=1) {
			System.err.println("Hitlist for BAND::hits BANK writer has wrong length. It is "+hitlist.size());
			return null;
		}
		//i should only go to 1 but keep for loop for future extensions if more hits are required
		for(int i =0;  i<hitlist.size(); i++) {
			bank.setShort("id",i, (short)(i+1));
			bank.setInt("sector",i, (int) hitlist.get(i).GetSector());
			bank.setInt("layer",i, (int) hitlist.get(i).GetLayer());
			bank.setInt("component",i, (int) hitlist.get(i).GetComponent());
			bank.setDouble("meantime",i, (double) hitlist.get(i).GetMeanTime());
			bank.setDouble("difftime",i, (double) hitlist.get(i).GetDiffTime());
			bank.setDouble("adcleftcorr",  i, (double) hitlist.get(i).GetAdcLeft());
			bank.setDouble("tdcleftcorr",  i, (double) hitlist.get(i).GetTdcLeft());
			bank.setDouble("adcrightcorr",  i, (double) hitlist.get(i).GetAdcRight());
			bank.setDouble("tdcrightcorr",  i, (double) hitlist.get(i).GetTdcRight());
			bank.setDouble("x",i, (double) (hitlist.get(i).GetX()));
			bank.setDouble("y",i, (double) (hitlist.get(i).GetY()));
			bank.setDouble("z",i, (double) (hitlist.get(i).GetZ()));
			bank.setDouble("ux",i, (double) (hitlist.get(i).GetUx()));
			bank.setDouble("uy",i, (double) (hitlist.get(i).GetUy()));
			bank.setDouble("uz",i, (double) (hitlist.get(i).GetUz()));


		}
		return bank;

	}

	 public void appendBANDBanks(DataEvent event,ArrayList<BandHit> hitlist) {
		if(hitlist.size()!=0){
			DataBank bank = this.fillBandHitBanks((DataEvent) event, hitlist);
			//bank.show();
			event.appendBanks(bank);
			//event.show();
		}
	}

}


