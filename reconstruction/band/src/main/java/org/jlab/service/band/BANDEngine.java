package org.jlab.service.band;


import java.util.ArrayList;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.rec.band.constants.CalibrationConstantsLoader;
import org.jlab.rec.band.banks.HitReader;
import org.jlab.rec.band.banks.RecoBankWriter;
import org.jlab.rec.band.hit.BandHit;
import org.jlab.rec.band.hit.BandHitCandidate;
import org.jlab.rec.band.hit.BandHitFinder;


/** @author Florian Hauenstein, Efrain Segarra
 * Service to return reconstructed BAND Hits - the output is in Hipo format
 */

public class BANDEngine extends ReconstructionEngine {


	public BANDEngine() {
		super("BAND", "hauenstein", "1.0");
	}

	int Run = -1;
	
	

	@Override
	public boolean processDataEvent(DataEvent event) {
		// update calibration constants based on run number if changed
		setRunConditionsParameters(event);
		
		ArrayList<BandHitCandidate> candidates = new ArrayList<BandHitCandidate>();   
		ArrayList<BandHit> hits = new ArrayList<BandHit>();
	    
		candidates = HitReader.getBandCandidates(event)	;	
		//1) exit if candidates list is empty
		if(candidates.size()==0 )
			return true;

		//2) find the BAND hits from the candidates
		BandHitFinder hitFinder = new BandHitFinder();
		hits = hitFinder.findGoodHits(candidates);

		
    	if(hits.size()>0){

			for (int i = 0; i < (hits.size()); i++) {
				System.out.println("Hit "+i+" : sector "+ hits.get(i).GetSector()+ " layer "+ hits.get(i).GetLayer()+" component " + hits.get(i).GetComponent());
			}
			//event.show();
			System.out.println("in process event ");
			RecoBankWriter.appendBANDBanks(event,hits);
				
		}


		return true;
	}

	@Override
	public boolean init() {
		// TODO Auto-generated method stub
		return true;
	}

	public void setRunConditionsParameters(DataEvent event) {
		if(event.hasBank("RUN::config")==false) {
			System.err.println("RUN CONDITIONS NOT READ!");
		}
		else {
			int newRun = Run;        

			DataBank bank = event.getBank("RUN::config");
			newRun = bank.getInt("run", 0);  
			// Load the constants
			//-------------------
			if(Run!=newRun) {
				CalibrationConstantsLoader.Load(newRun,"default"); 
				Run = newRun;
			}
		}

	}

	public static void main (String arg[]) {
		
		BANDEngine en = new BANDEngine();
		en.init();
		
		
		String input = "bandtest.hipo";
		
		HipoDataSource  reader = new HipoDataSource();
		reader.open(input);
		String outputFile="test.hipo";
		HipoDataSync  writer = new HipoDataSync();
		writer.open(outputFile);
        int nofevents=0;
		
		while(reader.hasEvent() && nofevents<1000) {
			
			DataEvent event = (DataEvent) reader.getNextEvent();
			//System.out.println("***********  NEXT EVENT ************");
			//event.show();
			if (event.hasBank("band::adc") && event.hasBank("band::tdc")){
			event.getBank("band::adc").show();
			event.getBank("band::tdc").show();
			}
			en.processDataEvent(event);
			writer.writeEvent(event);
			nofevents++;
			//event.getBank("band::hits").show();
			
		}		
		writer.close();
		
		
	}

}
