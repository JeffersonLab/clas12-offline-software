package org.jlab.rec.band.banks;

import java.util.ArrayList;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
//import org.jlab.rec.band.constants.CalibrationConstantsLoader;
//import org.jlab.rec.band.constants.Parameters;
import org.jlab.rec.band.hit.BandHitCandidate;


public class HitReader {

	// this method retrieves the candidate hits ie the adc/tdc signal from all the pmt for a given event
	public static  ArrayList<BandHitCandidate> getBandCandidates(DataEvent event) {

		if(event==null)
			return new ArrayList<BandHitCandidate>();

		double triggerPhase = 0;
            // NOT implemented yet 
		 /*get CAEN TDC jitter correction based on event timestamp
           double triggerPhase = 0;
                if(event.hasBank("RUN::config")) {
                    DataBank  bank = event.getBank("RUN::config");                       
                    long timeStamp = bank.getLong("timestamp", 0);
                    if(CalibrationConstantsLoader.JITTER_CYCLES>0 && timeStamp!=-1) 
                        triggerPhase=CalibrationConstantsLoader.JITTER_PERIOD*((timeStamp+CalibrationConstantsLoader.JITTER_PHASE)%CalibrationConstantsLoader.JITTER_CYCLES);
                }
		 	*/
		
            // Check that the file has the dgtz bank for BAND.	 
		if(event.hasBank("BAND::adc")==false || event.hasBank("BAND::tdc")==false) {
			//System.err.println("there is no BAND bank :-(");
			return new ArrayList<BandHitCandidate>();
		} 


		DataBank bankADC = event.getBank("BAND::adc");
		DataBank bankTDC = event.getBank("BAND::tdc");

		ArrayList<BandHitCandidate> candidates = new ArrayList<BandHitCandidate>();

		int nadc = bankADC.rows();   // number of adc values
		int ntdc = bankTDC.rows();   // number of tdc values
		int tdc=0;
		int adc=0;
		
		// starts looking at the adc bank
		for(int i = 0; i<nadc; i++)
		{ 
			int sector    = bankADC.getByte("sector",i);  // one of the 5 sectors
			int layer     = bankADC.getByte("layer",i);  // one of the 6 layers
			int component = bankADC.getByte("component", i);
			int order     = bankADC.getByte("order",i);
		
			int side = -1;
			
			adc = bankADC.getInt("ADC",i); 
			
			
			//know look for a tdc that is matching the adc. 
			//Starts from the beginning of the tdc list so only the first tdc is taken.
			for(int j=0; j<ntdc; j++){
				int s = bankTDC.getByte("sector", j);
                int l = bankTDC.getByte("layer", j);
                int o = bankTDC.getByte("order", j);

				if(s==sector && l == layer  && o == order+2 ){
			//System.out.println("s "+ s+" sector "+sector+" l "+l+" layer "+layer+" o "+o+" order "+order);
				 tdc = bankTDC.getInt("TDC",j);
				 break;
				}
			}
			
			BandHitCandidate newhit = null;

			// First, carry out checks on the quality of the signals:	    	  
			if (adc == 0 || tdc == 0) continue; // require good ADC and TDC values

			newhit = new BandHitCandidate(sector, layer, component, order, triggerPhase, adc, tdc); 

			candidates.add(newhit);
		}


		return  candidates;  

	}	

}
