package org.jlab.service.band;


import java.util.ArrayList;
import java.util.Arrays;

import javax.naming.event.NamingEvent;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.rec.band.constants.CalibrationConstantsLoader;
import org.jlab.rec.band.constants.Parameters;
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
		super("BAND", "hauensteinsegarra", "1.0");
	}

	int Run = -1;


	@Override
		public boolean processDataEvent(DataEvent event) {
			//System.out.println("**** NEW EVENT ****");
			// update calibration constants based on run number if changed
			setRunConditionsParameters(event);

			ArrayList<BandHitCandidate> candidates = new ArrayList<BandHitCandidate>();   
			ArrayList<BandHit> hits = new ArrayList<BandHit>();

			//1) Search for valid PMT hits based on FADC/TDC for each PMT
			candidates = HitReader.getBandCandidates(event)	;	
			// exit if candidates list is empty, neither BAND::rawhits nor BAND::hits is filled in this case
			if(candidates.size()==0 )
				return true;

			//2) Find the BAND bar hits from the candidates
			BandHitFinder hitFinder = new BandHitFinder();
			hits = hitFinder.findGoodHits(candidates);

			//3) Write candidates and hits to the banks. 
			RecoBankWriter.appendBANDBanks(event,candidates,hits);


			return true;
		}

	@Override
		public boolean init() {
			
			String[]  bandTables = new String[]{
				"/calibration/band/time_jitter",
				"/calibration/band/lr_offsets",
				"/calibration/band/effective_velocity",
				"/calibration/band/paddle_offsets",
				"/calibration/band/layer_offsets",
				"/calibration/band/paddle_offsets_tdc",
				"/calibration/band/layer_offsets_tdc",
				"/calibration/band/attenuation_lengths",
				"/calibration/band/time_walk_amp_left",
				"/calibration/band/time_walk_amp_right",
				"/calibration/band/global_offsets",
				"/calibration/band/cuts",
				"/calibration/band/energy_conversion"
				//"/calibration/band/time_walk_corr_left",
				//"/calibration/band/time_walk_corr_right",
    		};
    
			requireConstants(Arrays.asList(bandTables));
    		

		
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
				CalibrationConstantsLoader.Load(newRun,"default",this.getConstantsManager()); 
				Run = newRun;
				Parameters.CreateGeometry(); // loading BAND params
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
		
		int nevent = 0;

		while(reader.hasEvent() && nevent<2) {
			DataEvent event = (DataEvent) reader.getNextEvent();
			//System.out.println("***********  NEXT EVENT ************");
			//event.show();
			//if (event.hasBank("band::adc") && event.hasBank("band::tdc")){
			//	event.getBank("band::adc").show();
			//	event.getBank("band::tdc").show();
			//}
			en.processDataEvent(event);
			writer.writeEvent(event);
			//event.getBank("band::hits").show();
			nevent++;

		}		
		writer.close();


	}

}
