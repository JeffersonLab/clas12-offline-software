package org.jlab.service.band;


import java.util.ArrayList;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
//import org.jlab.rec.band.constants.CalibrationConstantsLoader;
import org.jlab.rec.band.banks.HitReader;
import org.jlab.rec.band.banks.RecoBankWriter;
import org.jlab.rec.band.hit.BandHit;
import org.jlab.rec.band.hit.BandHitCandidate;
import org.jlab.rec.band.hit.BandHitFinder;


/**
 * Service to return reconstructed BAND Hits - the output is in Hipo format
 * 
 *
 */

public class BANDEngine extends ReconstructionEngine {


	public BANDEngine() {
		super("BAND", "hauenstein", "1.0");
	}

	int Run = -1;
	RecoBankWriter rbc;
	//test
	static int enb =0;
	static int eband=0;
	static int hcvt=0;
	static int match=0;
	static int posmatch=0;

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

		//2) find the BAND hits from thes candidates
		BandHitFinder hitFinder = new BandHitFinder();
		hits = hitFinder.findGoodHits(candidates);

		
		   			

		if(hits.size()!=0){

				//          DataBank outbank = RecoBankWriter.fillbandHitBanks(event, hits);
			//          event.appendBanks(outbank);
			// event.show();
			System.out.println("in process event ");
			rbc.appendBANDBanks(event,hits);
			
			
		
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
			/*if(Run!=newRun) {
				CalibrationConstantsLoader.Load(newRun,"default"); 
				Run = newRun;
			}*/
		}

	}

	public static void main (String arg[]) {
		BANDEngine en = new BANDEngine();
		en.init();
		//String input = "/Users/ziegler/Workdir/Files/GEMC/ForwardTracks/pi-.r100.evio";
		
		String input = "/band_2052_2053.hipo";
		
		HipoDataSource  reader = new HipoDataSource();
		reader.open(input);
		String outputFile="/projet/nucleon/hauenst/band_run2052_2053/test.hipo";
		HipoDataSync  writer = new HipoDataSync();
		writer.open(outputFile);

		
		while(reader.hasEvent()) {
			enb++;		
			DataEvent event = (DataEvent) reader.getNextEvent();
			
			//event.show();
			if (event.hasBank("band::adc") && event.hasBank("band::tdc")){
			event.getBank("band::adc").show();
			event.getBank("band::tdc").show();
			}
			en.processDataEvent(event);
			writer.writeEvent(event);
			//event.getBank("band::hits").show();
			System.out.println("event nb "+enb);
			//event.getBank("band::hits").show();
			//System.out.println();
			if(enb>1000)
				break;
		}		
		writer.close();
		
		System.out.println("enb "+enb);
		System.out.println("eband "+eband);
		System.out.println("hcvt "+hcvt);
		System.out.println("posmatch "+posmatch);
		System.out.println("match "+match);
		System.out.println("%match "+100.*match/posmatch);
		System.out.println("Done");
	}

}
