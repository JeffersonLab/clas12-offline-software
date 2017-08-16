package org.jlab.rec.cnd.banks;

import java.util.ArrayList;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cnd.costants.Parameters;
import org.jlab.rec.cnd.hit.HalfHit;

public class HitReader {

	// this method retrieves the half hits ie the adc/tdc signal from all the pmt for a given event
	public static  ArrayList<HalfHit> getCndHalfHits(DataEvent event) {

		if(event==null)
			return new ArrayList<HalfHit>();

		// Check that the file has the dgtz bank for CND.	 
		if(event.hasBank("CND::adc")==false || event.hasBank("CND::tdc")==false) {
			//System.err.println("there is no CND bank :-(");
			return new ArrayList<HalfHit>();
		} 


		DataBank bankADC = event.getBank("CND::adc");
		DataBank bankTDC = event.getBank("CND::tdc");

		if(bankADC.rows() != bankTDC.rows()) return new ArrayList<HalfHit>();

		ArrayList<HalfHit> halfhits = new ArrayList<HalfHit>();

		int nhits = bankADC.rows();   // number of hits in the event

		for(int i = 0; i<nhits; i++)
		{ 
			int sector    = bankADC.getByte("sector",i);  // one of the 24 "blocks"
			int layer     = bankADC.getByte("layer",i);  
			int order     = bankADC.getByte("order",i);
			int component = order + 1; // get the component 1 is left 2 is right

			//assume that ADC and TDC have then same index in both adc and tdc raw list
			int adc = bankADC.getInt("ADC",i);   
			int tdc = bankTDC.getInt("TDC",i);

			HalfHit newhit = null;

			// First, carry out checks on the quality of the signals:	    	  
			if (adc == 0 || tdc == 0 || tdc == Parameters.NullTDC) continue; // require good ADC and TDC values

			newhit = new HalfHit(sector, layer, component, adc, tdc, i); 

			halfhits.add(newhit);
		}


		return  halfhits;  

	}	

}
