package org.jlab.rec.band.banks;

import java.util.ArrayList;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.band.hit.BandHit;
import org.jlab.rec.band.hit.BandHitCandidate;
import org.jlab.rec.band.constants.CalibrationConstantsLoader;



public class RecoBankWriter {


	// write useful information in the bank
	public static DataBank fillBandHitBanks(DataEvent event, ArrayList<BandHit> hitlist) {

		if (hitlist == null) {
			return null;
		}
		//Get status for first hit. If it is 1 assume that the BandHit array contains laser hits
		short test_status = (short) hitlist.get(0).GetStatus();
		
		DataBank bank;
		if (test_status == 1) { //Initialize BAND::laser if status is  1 of the first hit
			bank =  event.createBank("BAND::laser", hitlist.size());
		}
		else { //in all other cases the data is something else (cosmics or real data)->Store in BAND::hits
			bank =  event.createBank("BAND::hits", hitlist.size());
		}
		
		if (bank == null) {
			System.err.println("COULD NOT CREATE A BAND::Hits/laser BANK in RecoBankWriter.fillBandHitBanks!!!!!!");
			return null;
		}

		//i just numbers the hits 
		for(int i = 0; i < hitlist.size(); i++) {
			bank.setShort("id",i, (short)(i+1));

			bank.setByte("sector",i, (byte) hitlist.get(i).GetSector());
			bank.setByte("layer",i, (byte) hitlist.get(i).GetLayer());
			bank.setShort("component",i, (short) hitlist.get(i).GetComponent());

			int barKey = hitlist.get(i).GetSector()*100+hitlist.get(i).GetLayer()*10+hitlist.get(i).GetComponent();
                        double energyconvert_params[] = CalibrationConstantsLoader.ENERGY_CONVERT.get( Integer.valueOf(barKey) );                        
                        double parA = energyconvert_params[0];
                        double parB = energyconvert_params[1];
                        double parC = energyconvert_params[2];                            
                      
                        double combo_adc = Math.sqrt(hitlist.get(i).GetAdcLeft() * hitlist.get(i).GetAdcRight());
			combo_adc = parA + parB*combo_adc + parC*combo_adc*combo_adc;

			//bank.setFloat("energy", i, (float) Math.sqrt(hitlist.get(i).GetAdcLeft() * hitlist.get(i).GetAdcRight()));
			bank.setFloat("energy", i, (float) combo_adc);
			bank.setFloat("time",i, (float) hitlist.get(i).GetMeanTime_TDC());
		
			bank.setFloat("x",i, (float) (hitlist.get(i).GetX()));
			bank.setFloat("y",i, (float) (hitlist.get(i).GetY()));
			bank.setFloat("z",i, (float) (hitlist.get(i).GetZ()));
			bank.setFloat("ex",i, (float) (hitlist.get(i).GetUx()));
			bank.setFloat("ey",i, (float) (hitlist.get(i).GetUy()));
			bank.setFloat("ez",i, (float) (hitlist.get(i).GetUz()));

			bank.setFloat("timeFadc",i, (float) hitlist.get(i).GetMeanTime_FADC());
			bank.setFloat("difftime",i, (float) hitlist.get(i).GetDiffTime_TDC());
			bank.setFloat("difftimeFadc",i, (float) hitlist.get(i).GetDiffTime_FADC());

			bank.setShort("indexLpmt",i, (short) hitlist.get(i).GetIndexLpmt());
			bank.setShort("indexRpmt",i, (short) hitlist.get(i).GetIndexRpmt()); 

			bank.setShort("status",i, (short) hitlist.get(i).GetStatus());
		}
		return bank;

	}
	
	public static DataBank fillBandCandidateBank(DataEvent event, ArrayList<BandHitCandidate> candidatelist) {

		if (candidatelist == null) {
			return null;
		}
		DataBank bank =  event.createBank("BAND::rawhits", candidatelist.size());

		if (bank == null) {
			System.err.println("COULD NOT CREATE A BAND::rawhits BANK in RecoBankWriter.fillBandRawhitsBank!!!!!!");
			return null;
		}

		//i just numbers the hits
		for(int i =0;  i<candidatelist.size(); i++) {
			bank.setShort("id",i, (short)(i+1));

			bank.setByte("sector",i, (byte) candidatelist.get(i).GetSector());
			bank.setByte("layer",i, (byte) candidatelist.get(i).GetLayer());
			bank.setShort("component",i, (short) candidatelist.get(i).GetComponent());
			bank.setShort("side",i, (short) candidatelist.get(i).GetSide());
			bank.setFloat("adc",i, (float) candidatelist.get(i).GetAdc());	
			bank.setFloat("ampl", i, (float) candidatelist.get(i).GetAmpl());
		
			bank.setFloat("time",i, (float) candidatelist.get(i).GetTdc());
			bank.setFloat("timeFadc",i, (float) candidatelist.get(i).GetFtdc());
			bank.setFloat("timeCorr",i, (float) candidatelist.get(i).GetTimeCorr());
			
			bank.setShort("indexTdc",i, (short) candidatelist.get(i).GetIndexTdc());
			bank.setShort("indexAdc",i, (short) candidatelist.get(i).GetIndexAdc()); 			

		}
		return bank;

	}
	
	

	public static void appendBANDBanks(DataEvent event,ArrayList<BandHitCandidate> candidatelist, ArrayList<BandHit> hitlist) {

		//check if candidatelist is not empty (just to be sure, this is also checked in BANDEngine.
		if(candidatelist.size()>0){
			DataBank rawhits = fillBandCandidateBank((DataEvent) event, candidatelist);
			if (rawhits != null) {
				event.appendBank(rawhits);
			}
			else {
				System.err.println("COULD NOT APPEND BAND::rawhits to the event in RecoBankWriter.appendBANDBanks!");
			}
		}
		//check if hitlist is not empty
		if(hitlist.size()>0){
			DataBank hits = fillBandHitBanks((DataEvent) event, hitlist); 
			if (hits != null) {
				event.appendBank(hits);
			}
			else {
				System.err.println("COULD NOT APPEND BAND::hits/laser to the event in RecoBankWriter.appendBANDBanks!");
			}
		}
	}

}


