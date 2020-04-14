package org.jlab.rec.cnd.banks;

import java.util.ArrayList;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cnd.constants.CalibrationConstantsLoader;
import org.jlab.rec.cnd.constants.Parameters;
import org.jlab.rec.cnd.hit.HalfHit;

public class HitReader {

	// this method retrieves the half hits ie the adc/tdc signal from all the pmt for a given event
	public static  ArrayList<HalfHit> getCndHalfHits(DataEvent event, CalibrationConstantsLoader ccdb) {

		if(event==null)
			return new ArrayList<HalfHit>();

                // get CAEN TDC jitter correction based on event timestamp
                double triggerPhase = 0;
                if(event.hasBank("RUN::config")) {
                    DataBank  bank = event.getBank("RUN::config");                       
                    long timeStamp = bank.getLong("timestamp", 0);
                    if(ccdb.JITTER_CYCLES>0 && timeStamp!=-1) 
                        triggerPhase=ccdb.JITTER_PERIOD*((timeStamp+ccdb.JITTER_PHASE)%ccdb.JITTER_CYCLES);
                }

            // Check that the file has the dgtz bank for CND.	 
		if(event.hasBank("CND::adc")==false || event.hasBank("CND::tdc")==false) {
			//System.err.println("there is no CND bank :-(");
			return new ArrayList<HalfHit>();
		} 


		DataBank bankADC = event.getBank("CND::adc");
		DataBank bankTDC = event.getBank("CND::tdc");

		//if(bankADC.rows() != bankTDC.rows()) return new ArrayList<HalfHit>();

		ArrayList<HalfHit> halfhits = new ArrayList<HalfHit>();

		int nadc = bankADC.rows();   // number of adc values
		int ntdc = bankTDC.rows();   // number of tdc values
		int tdc=0;
		int adc=0;
		
		
		//Sort the TDC bank using index. This allows to reproduce double hits in simulation.
		// The TDCs are sorted in descending order.
		
		int sortedIndTDC[];
		sortedIndTDC = new int[ntdc]; 
		int AlreadysortedIndTDC[];
		AlreadysortedIndTDC = new int[ntdc]; 
		for(int i=0; i<ntdc; i++){//index in the sorted array
			int index=-1000;
			int TDC= -50000;
			for(int j=0; j<ntdc; j++){//index in the bank
				int tdcTemp = bankTDC.getInt("TDC",j);
				if(tdcTemp>TDC & AlreadysortedIndTDC[j]==0) {
					TDC=tdcTemp;
					index=j;
				}
			}
			sortedIndTDC[i]=index;
			AlreadysortedIndTDC[index]=1;
		}
		
		/*System.out.println("Sorted TDC");
		bankTDC.show();
		System.out.println("Sorted TDC");
		for(int i=0; i<ntdc; i++){//index in the sorted array
			System.out.println(i+"   "+sortedIndTDC[i]+" "+bankTDC.getInt("TDC",sortedIndTDC[i]));
		}*/
		
		// starts looking at the adc bank
		for(int i = 0; i<nadc; i++)
		{ 
			
			
			int sector    = bankADC.getByte("sector",i);  // one of the 24 "blocks"
			int layer     = bankADC.getByte("layer",i);  
			int order     = bankADC.getByte("order",i);
			int component = order + 1; // get the component 1 is left 2 is right
			int indextdc = -1;
			
			
			adc = bankADC.getInt("ADC",i); 
			boolean ignorePaddle = false; // whether to ignore or not the processing paddle (as there is already a half it in the paddle)
			
			for (int p=0; p<halfhits.size();p++){
				if (sector==halfhits.get(p).Sector() && layer==halfhits.get(p).Layer() && component==halfhits.get(p).Component()){
					ignorePaddle=true;
					break;
					//break as there should only be one half hit per paddle in a event. Otherwise we don't know which tdc to associate with adc.
				}
			}
			
			//now look for a tdc that is matching the adc. Starts from the beggining of the tdc list so only the first tdc is taken.
			if(ignorePaddle==false){
				for(int j=0; j<ntdc; j++){
					int s = bankTDC.getByte("sector", sortedIndTDC[j]);
                   			int l = bankTDC.getByte("layer", sortedIndTDC[j]);
                 			int o = bankTDC.getByte("order", sortedIndTDC[j]);

					if(s==sector && l == layer  && o == order+2 ){
					//System.out.println("s "+ s+" sector "+sector+" l "+l+" layer "+layer+" o "+o+" order "+order);
					tdc = bankTDC.getInt("TDC",sortedIndTDC[j]);
					indextdc=sortedIndTDC[j];
					break;
					}
				}
			}
			

			HalfHit newhit = null;

			// First, carry out checks on the quality of the signals:	    	  
			if (adc == 0 || tdc == 0 || tdc == Parameters.NullTDC || indextdc==-1) continue; // require good ADC and TDC values
			if(ccdb.Status_LR[sector-1][layer-1][component-1]==1)continue; //if status is one then ignore halfhit and pass to the next one in the list

			
			newhit = new HalfHit(sector, layer, component, triggerPhase, adc, tdc, i,indextdc, ccdb); 

			halfhits.add(newhit);
		}


		return  halfhits;  

	}	

}
