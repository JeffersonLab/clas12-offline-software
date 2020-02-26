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
			//System.err.println("COULD NOT CREATE A BAND::Hits BANK!!!!!!");
			return null;
		}


		//i should only go to 1 but keep for loop for future extensions if more hits are required
		for(int i =0;  i<hitlist.size(); i++) {
			bank.setShort("id",i, (short)(i+1));

			bank.setByte("sector",i, (byte) hitlist.get(i).GetSector());
			bank.setByte("layer",i, (byte) hitlist.get(i).GetLayer());
			bank.setShort("component",i, (short) hitlist.get(i).GetComponent());

			bank.setFloat("meantimeTdc",i, (float) hitlist.get(i).GetMeanTime_TDC());
			bank.setFloat("meantimeFadc",i, (float) hitlist.get(i).GetMeanTime_FADC());

			bank.setFloat("difftimeTdc",i, (float) hitlist.get(i).GetDiffTime_TDC());
			bank.setFloat("difftimeFadc",i, (float) hitlist.get(i).GetDiffTime_FADC());

			bank.setFloat("adcLcorr",i, (float) hitlist.get(i).GetAdcLeft());
			bank.setFloat("adcRcorr",i, (float) hitlist.get(i).GetAdcRight());
			bank.setFloat("tFadcLcorr",i, (float) hitlist.get(i).GetTLeft_FADC());
			bank.setFloat("tFadcRcorr",i, (float) hitlist.get(i).GetTRight_FADC());
			bank.setFloat("tTdcLcorr",i, (float) hitlist.get(i).GetTLeft_TDC());
			bank.setFloat("tTdcRcorr",i, (float) hitlist.get(i).GetTRight_TDC());

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
			//bank.show();
			event.appendBank(bank);
		}
	}

}


