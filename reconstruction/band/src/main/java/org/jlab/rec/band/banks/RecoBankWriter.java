package org.jlab.rec.band.banks;

import java.util.ArrayList;

import org.apache.commons.math3.analysis.function.Sqrt;
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
			//System.err.println("COULD NOT CREATE A BAND::Hits BANK!!!!!!");
			return null;
		}


		//i should only go to 1 but keep for loop for future extensions if more hits are required
		for(int i =0;  i<hitlist.size(); i++) {
			bank.setShort("id",i, (short)(i+1));

			bank.setByte("sector",i, (byte) hitlist.get(i).GetSector());
			bank.setByte("layer",i, (byte) hitlist.get(i).GetLayer());
			bank.setShort("component",i, (short) hitlist.get(i).GetComponent());
			
			bank.setFloat("energy", i, (float) Math.sqrt(hitlist.get(i).GetAdcLeft() * hitlist.get(i).GetAdcRight()));

			bank.setFloat("time",i, (float) hitlist.get(i).GetMeanTime_TDC());
			bank.setFloat("timeFadc",i, (float) hitlist.get(i).GetMeanTime_FADC());

			bank.setFloat("difftime",i, (float) hitlist.get(i).GetDiffTime_TDC());
			bank.setFloat("difftimeFadc",i, (float) hitlist.get(i).GetDiffTime_FADC());

			bank.setShort("indexLpmt",i, (short) hitlist.get(i).GetIndexLpmt());
			bank.setShort("indexRpmt",i, (short) hitlist.get(i).GetIndexRpmt()); 

			bank.setFloat("x",i, (float) (hitlist.get(i).GetX()));
			bank.setFloat("y",i, (float) (hitlist.get(i).GetY()));
			bank.setFloat("z",i, (float) (hitlist.get(i).GetZ()));
			bank.setFloat("ex",i, (float) (hitlist.get(i).GetUx()));
			bank.setFloat("ey",i, (float) (hitlist.get(i).GetUy()));
			bank.setFloat("ez",i, (float) (hitlist.get(i).GetUz()));

			bank.setShort("status",i, (short) hitlist.get(i).GetStatus());
		}
		return bank;

	}

	public static void appendBANDBanks(DataEvent event,ArrayList<BandHit> hitlist) {

		DataBank bank = fillBandHitBanks((DataEvent) event, hitlist);
		if (bank != null) {
			//bank.show();
			event.appendBank(bank);
		}
	}

}


