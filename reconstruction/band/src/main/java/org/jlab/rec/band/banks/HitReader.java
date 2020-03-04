package org.jlab.rec.band.banks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.band.constants.CalibrationConstantsLoader;
//import org.jlab.rec.band.constants.Parameters;
import org.jlab.rec.band.hit.BandHitCandidate;
import org.jlab.utils.groups.IndexedList;
import org.jlab.utils.groups.IndexedTable;

/**
 * @author Efrain Segarra, Florian Hauenstein
 * Inspiration: Herbie
 */


public class HitReader {


	// this method retrieves the candidate hits ie the adc/tdc signal from all the pmt for a given event
	public static  ArrayList<BandHitCandidate> getBandCandidates(DataEvent event) {

		Map<Integer,Integer>	fadcInt 	= new HashMap<Integer,Integer>();
		Map<Integer,Integer>	fadcAmpl    = new HashMap<Integer,Integer>();
		Map<Integer,Float>	fadcTimes	= new HashMap<Integer,Float>();
		Map<Integer,Double>	tdcTimes	= new HashMap<Integer,Double>();
		Map<Integer,Integer> fadcIndex  = new HashMap<Integer,Integer>();
		Map<Integer,Integer> tdcIndex  = new HashMap<Integer,Integer>();


		if(event==null) return new ArrayList<BandHitCandidate>();


		// Grab trigger phase for TDC vs FADC matching
		double triggerPhase = getTriggerPhase(event);

		// Check that the file has the dgtz bank for BAND.	 
		if(event.hasBank("BAND::adc")==false || event.hasBank("BAND::tdc")==false) {
			return new ArrayList<BandHitCandidate>();
		} 

		DataBank bankADC = event.getBank("BAND::adc");
		DataBank bankTDC = event.getBank("BAND::tdc");

		ArrayList<BandHitCandidate> candidates = new ArrayList<BandHitCandidate>();

		int nadc 	= bankADC.rows();   // number of adc values
		int ntdc 	= bankTDC.rows();   // number of tdc values

		//System.out.println("PROCESSING ALL ADCs");
		//System.out.flush();
		// Loop over the event and add all the FADC information to arrays for processing
		for( int i = 0 ; i < nadc ; i++ ){ 
			int s	= bankADC.getByte("sector",i);  // one of the 5 sectors
			int l	= bankADC.getByte("layer",i);  // one of the 6 layers
			int c 	= bankADC.getShort("component", i);
			int o	= bankADC.getByte("order",i);

			int adc = bankADC.getInt("ADC",i); 
			int ampl= bankADC.getInt("amplitude", i);
			float ftdc = bankADC.getFloat("time",i);
			int ped = bankADC.getShort("ped",i);
			//System.out.println("ampl, ped = "+ampl+" "+ped);
			ampl = ampl - ped;
	
			if( adc <= 0 || ftdc <= 0 ) continue;

			int key = s*1000 + l*100 + c*10 + o;

			//System.out.println("s,l,c,o: "+key+" adc,ftdc: "+adc+" "+ftdc+" amp,index: "+ampl+" " +i+"\n");
			//System.out.flush();


			// Check if this PMT has been stored before, and if it has, then
			// replace it only if it has a larger ADC value. Otherwise, add to map
			//NOTE F.H. Feb 17 2020: Should we do some update to also check Amplitude here?
			if (fadcInt.containsKey( Integer.valueOf(key) ) ) { 
				//System.out.println("\tPMT has prev save.");
				//System.out.flush();
				if( fadcInt.get( Integer.valueOf(key) ) < adc ) {
					//System.out.println("\t\treplacing PMT information for: "+key+" adc,ftdc: "+adc+" "+ftdc);
					//System.out.flush();
					fadcInt.put( Integer.valueOf(key) , Integer.valueOf(adc) );
					fadcAmpl.put(Integer.valueOf(key),  Integer.valueOf(ampl));
					fadcTimes.put( Integer.valueOf(key),  Float.valueOf(ftdc) );
					fadcIndex.put( Integer.valueOf(key), Integer.valueOf(i));
				}
			}
			else {
				//System.out.println("\tPMT doesnt have prev save. saving as s,l,c,o: "+key+" adc,ftdc: "+adc+" "+ftdc);
				//System.out.flush();
				fadcInt.put( Integer.valueOf(key) , Integer.valueOf(adc) );
				fadcAmpl.put(Integer.valueOf(key),  Integer.valueOf(ampl));
				fadcTimes.put( Integer.valueOf(key),  Float.valueOf(ftdc) );
				fadcIndex.put( Integer.valueOf(key), Integer.valueOf(i));
			}	
		} // end fadc loop

		//System.out.println("PROCESSING ALL TDCs");
		//System.out.flush();
		// Now loop over the TDC information and try to find best match
		for( int j = 0 ; j < ntdc ; j++ ){
			int s = bankTDC.getByte("sector", j);
			int l = bankTDC.getByte("layer", j);
			int c = bankTDC.getShort("component", j);
			int o = bankTDC.getByte("order", j);

			int key = s*1000 + l*100 + c*10 + (o-2);

			double tdc = ((double)bankTDC.getInt("TDC",j) * 0.02345);

			if( tdc <= 0 ) continue;

			//System.out.println("s,l,c,o: "+key+" tdc, index: "+tdc+" "+j);
			//System.out.flush();


			// Make sure that we have FADC information for this PMT -- if not, skip it
			if( !fadcInt.containsKey(key) || !fadcTimes.containsKey(key) ) continue;
			//System.out.println("\t*Found match for this PMT! Saving TDC information*");
			//System.out.flush();

			// If we have already stored a TDC for this PMT, need to compare with
			// FADC time and take the smallest tdiff one.
			if (tdcTimes.containsKey( Integer.valueOf(key) ) ) { 
				double thisDiff = fadcTimes.get( Integer.valueOf(key) ) - tdc;
				double prevDiff = fadcTimes.get( Integer.valueOf(key) ) - tdcTimes.get( Integer.valueOf(key) );

				//System.out.println("\tPMT TDC has prev save.");
				//System.out.flush();
				if( Math.abs(thisDiff) < Math.abs(prevDiff) ) {
					//System.out.println("\t\treplacing PMT TDC information for: "+key+" tdc: "+tdc);
					//System.out.flush();
					tdcTimes.put( Integer.valueOf(key),  Double.valueOf(tdc) );
					tdcIndex.put( Integer.valueOf(key), Integer.valueOf(j));
				}
			}
			else {
				//System.out.println("\tPMT TDC doesnt have prev save. saving as s,l,c,o: "+key+" tdc: "+tdc);
				//System.out.flush();
				tdcTimes.put( Integer.valueOf(key),  Double.valueOf(tdc) );
				tdcIndex.put( Integer.valueOf(key), Integer.valueOf(j));
			}
		} // end tdc loop


		// Now we can loop over keys in our map and just ask that each map has that key in order
		// to save it as a hit candidate. The FADC is likely to have LESS info than the TDC, so 
		// we will loop over the keys in FADC to save time (since we are looking for match of all)
		for (Integer keys : fadcTimes.keySet()){
			if( !tdcTimes.containsKey(keys) || !fadcInt.containsKey(keys) ) continue;
			String id = Integer.toString(keys);
			int sector 		= Integer.parseInt( id.substring(0,1) );
			int layer 		= Integer.parseInt( id.substring(1,2) );
			int component 		= Integer.parseInt( id.substring(2,3) );
			int order		= Integer.parseInt( id.substring(3,4) );

			int adc = fadcInt.get(keys);
			int ampl= fadcAmpl.get(keys);
			float ftdc = fadcTimes.get(keys);
			double tdc = tdcTimes.get(keys);
			int indexadc = fadcIndex.get(keys);
			int indextdc = tdcIndex.get(keys);
			//System.out.println("Found a candidate PMT hit! slco: "+sector+" "+layer+" "+component+" "+order+" adc,ftdc "+adc+" "+ftdc+" tdc: "+tdc+" adcInd,tdcInd: "+indexadc+" "+indextdc);
			//System.out.flush();

			BandHitCandidate newHit = new BandHitCandidate( sector,layer,component,order,
					adc, ampl,tdc, ftdc ,triggerPhase, indexadc, indextdc);
			candidates.add(newHit);

		}

		return  candidates;  

	}	


	private static double getTriggerPhase( DataEvent ev) {
		double tPh = 0.;
		if(ev.hasBank("RUN::config")) {
			DataBank  bank = ev.getBank("RUN::config");                       
			long timeStamp = bank.getLong("timestamp", 0);
			if( CalibrationConstantsLoader.JITTER_CYCLES > 0 && timeStamp != -1 ) {
				tPh = CalibrationConstantsLoader.JITTER_PERIOD *
					( ( timeStamp + CalibrationConstantsLoader.JITTER_PHASE ) % 
					  CalibrationConstantsLoader.JITTER_CYCLES);
			}
		}
		return tPh;

	}
}
