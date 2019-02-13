package org.jlab.rec.band.banks;

import java.util.ArrayList;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.band.hit.BandHit;



public class RecoBankWriter {


	// write useful information in the bank
	public static DataBank fillBandHitBanks(DataEvent event, ArrayList<BandHit> hitlist) {

		if (hitlist == null) {
	        return null;
	    }
		DataBank bank =  event.createBank("BAND::hits", hitlist.size());
		
		if (bank == null) {
			System.err.println("COULD NOT CREATE A BAND::Hits BANK!!!!!!");
			return null;
		}

		
		//i should only go to 1 but keep for loop for future extensions if more hits are required
		for(int i =0;  i<hitlist.size(); i++) {
			bank.setShort("id",i, (short)(i+1));
			bank.setShort("sector",i, (short) hitlist.get(i).GetSector());
			bank.setShort("layer",i, (short) hitlist.get(i).GetLayer());
			bank.setInt("component",i, (int) hitlist.get(i).GetComponent());
			bank.setFloat("meantime",i, (float) hitlist.get(i).GetMeanTime());
			bank.setFloat("difftime",i, (float) hitlist.get(i).GetDiffTime());
			bank.setFloat("adcleftcorr",  i, (float) hitlist.get(i).GetAdcLeft());
			bank.setFloat("tdcleftcorr",  i, (float) hitlist.get(i).GetTdcLeft());
			bank.setFloat("ftdcleftcorr",  i, (float) hitlist.get(i).GetFtdcLeft());	
			bank.setFloat("adcrightcorr",  i, (float) hitlist.get(i).GetAdcRight());
			bank.setFloat("tdcrightcorr",  i, (float) hitlist.get(i).GetTdcRight());
			bank.setFloat("ftdcrightcorr",  i, (float) hitlist.get(i).GetFtdcRight());
			bank.setFloat("x",i, (float) (hitlist.get(i).GetX()));
			bank.setFloat("y",i, (float) (hitlist.get(i).GetY()));
			bank.setFloat("z",i, (float) (hitlist.get(i).GetZ()));
			bank.setFloat("ux",i, (float) (hitlist.get(i).GetUx()));
			bank.setFloat("uy",i, (float) (hitlist.get(i).GetUy()));
			bank.setFloat("uz",i, (float) (hitlist.get(i).GetUz()));


		}
		return bank;

	}

	 public static void appendBANDBanks(DataEvent event,ArrayList<BandHit> hitlist) {
		 
		 DataBank bank = fillBandHitBanks((DataEvent) event, hitlist);
		 if (bank != null) {
			 bank.show();
			 event.appendBank(bank);
         }
	     	 		
	}

}


