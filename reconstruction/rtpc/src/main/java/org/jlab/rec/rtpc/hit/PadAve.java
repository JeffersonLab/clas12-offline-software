package org.jlab.rec.rtpc.hit;
/*
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class PadAve {
	public void TimeAverage(HitParameters params){
		
		int SignalStepSize = params.get_SignalStepSize(); // step size of the signal before integration (arbitrary value)
		int BinSize = params.get_BinSize(); // electronics integrates the signal over 40 ns
		int NBinKept = params.get_NBinKept(); // only 1 bin over 3 is kept by the daq
		int TrigWindSize = params.get_TrigWindSize(); // Trigger window should be 10 micro
		int NTrigSampl = TrigWindSize/BinSize; // number of time samples
		double inte=0;
		double inte_tot; // integral of the signal in BinSize
		double max_inte=0; // maximum of the integral to help the fit
		double max_t=0; 
		double sumnumer = 0; 
		double sumdenom = 0;
		double weightave = 0;
		double timesum = 0;
		HashMap<Integer, double[]> ADCMap = params.get_ADCMap();
		HashMap<Integer, List<Double>> TimeMap = params.get_TimeMap();
		List<Integer> PadList = params.get_PadList();
		List<Integer> PadN = params.get_PadN();
		List<Integer> Pad = params.get_Pad();
		List<Double> ADC = params.get_ADC();
		List<Double> Time_o = params.get_Time_o();
		List<Double> weightavevec = new ArrayList<Double>();
		List<Double> maxinte = new ArrayList<Double>();
		boolean flag_event = false; 
		int eventnum = params.get_eventnum();
		//try{
			//FileWriter write2 = new FileWriter("/Users/davidpayette/Documents/FileOutput/Output.txt",true);
			//write2.write("Event Number" + "\t" + "Pad Number" + "\t" + "Time Value for each bin" + "\t" + "ADC value for each bin" + "\r\n");

		
		inte=0;
		for(int p=0;p<PadList.size();p++){ 	    	
			inte_tot = 0;
			max_inte = 0;
			for(int t=0;t<TrigWindSize;t+=SignalStepSize){  	         		         	
				if(t>0) inte+=0.5*(ADCMap.get(PadList.get(p))[t-SignalStepSize]+ADCMap.get(PadList.get(p))[t])*SignalStepSize;	         	
				inte_tot+=inte;	         	
				if(t%BinSize==0 && t>0){ // integration over BinSize
					if(t%(BinSize*NBinKept)==0){ // one BinSize over NBinKept is read out, hence added to the histogram							
						if(max_inte<inte){max_inte=inte; max_t=t;}  
						sumnumer+=inte*t;
						sumdenom+=inte;
						
							//write2.write(eventnum + "\t" + PadList.get(p) + "\t" + t + "\t" + inte + "\r\n");
							//System.out.println(eventnum + "\t" + PadList.get(p) + "\t" + t + "\t" + inte + "\r\n");
						
					}	             
					inte=0;
				}
			}
			
			weightave = sumnumer/sumdenom;
			for(int t=0; t<TimeMap.get(PadList.get(p)).size(); t++)
			{	
				timesum += TimeMap.get(PadList.get(p)).get(t);
			}
*/
/*

				

			//System.out.println(TimeMap.get(PadList.get(p)).size());
			weightavevec.add(weightave);
			maxinte.add(max_inte);
			sumnumer = 0;
			sumdenom = 0;
			weightave = 0;
			timesum = 0;
			//write2.close();
		}
		params.set_weightave(weightavevec);
		params.set_maxinte(maxinte);
		//} catch (IOException e1) {
			// TODO Auto-generated catch block
		//	e1.printStackTrace();
		//}
	}

}
*/