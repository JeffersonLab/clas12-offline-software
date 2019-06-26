// Author: Gabriel Charles
// Language Conversion: David Payette

//This code uses the map created in PadHit.java to access the signals for a given pad (cellID).
//The code is run once per event and loops over all Pads stored in the PadNum vector.
//For a given pad, the map returns an array which is looped over and integrated bin by bin.
//This integral by time bin is then plotted and fit. The fit function should be user-defined.

package org.jlab.rec.rtpc.hit;
import javax.swing.JFrame;
import org.jlab.groot.data.*;
import org.jlab.groot.fitter.*;
import org.jlab.groot.graphics.*;
import org.jlab.groot.math.F1D;
import org.jlab.groot.ui.TCanvas;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class PadFit {
	public void Fitting(HitParameters params){
		
		
		int StepSize = params.get_StepSize(); // step size of the signal before integration (arbitrary value)
		int BinSize = params.get_BinSize(); // electronics integrates the signal over 40 ns
		int NBinKept = params.get_NBinKept(); // only 1 bin over 3 is kept by the daq
		int TrigWindSize = params.get_TrigWindSize(); // Trigger window should be 10 micro
		int NTrigSampl = TrigWindSize/BinSize; // number of time samples
		double inte=0;
		double inte_tot; // integral of the signal in BinSize
		double max_inte=0; // maximum of the integral to help the fit
		double max_t=0; 
		HashMap<Integer, double[]> R_adc = params.get_R_adc();
		HashMap<Integer, double[]> Adcmap = new HashMap<Integer, double[]>();
		Vector<Integer> PadNum = params.get_PadNum();
		Vector<Integer> PadN = params.get_PadN();
		Vector<Integer> Pad = params.get_Pad();
		Vector<Double> ADC = params.get_ADC();
		Vector<Double> Time_o = params.get_Time_o();
		boolean flag_event = false; 
		int eventnum = params.get_eventnum();
		
		/*JFrame j1 = new JFrame();
		GraphErrors g1 = new GraphErrors();   
		g1.setMarkerSize(0);
		g1.setTitleX("Time(ns)");
		g1.setTitleY("ADC");
		g1.setMarkerSize(3);
		EmbeddedCanvas c1 = new EmbeddedCanvas();
		j1.setSize(800, 600);
		 */
		 
		 
		//F1D f1 = new F1D("f1", "[amp]*gaus(x,[mean],[sigma])",0,1);
				    	 		
		inte=0;
		for(int p=0;p<PadNum.size();p++){ 
			//System.out.println(PadNum.size() + " " + eventnum);	    	
			inte_tot = 0;
                        
                        Adcmap.put(PadNum.get(p),new double[TrigWindSize]);
                        
			for(int t=0;t<TrigWindSize;t+=StepSize){  
                                
                                if(t>0) inte+=0.5*(R_adc.get(PadNum.get(p))[t-StepSize]+R_adc.get(PadNum.get(p))[t])*StepSize;	         	
                                
                                inte_tot+=inte;	         	
				if(t%BinSize==0 && t>0){ // integration over BinSize
					if(t%(BinSize*NBinKept)==0){ // one BinSize over NBinKept is read out, hence added to the histogram
						
                                                Adcmap.get(PadNum.get(p))[t] = inte;
                                                
						//g1.addPoint(t,inte,0,0);							
						if(max_inte<inte){max_inte=inte; max_t=t;}       
					}	             
					inte=0;
				}
			}
 
			/*
	        f1.setRange(max_t-StepSize*100,max_t+StepSize*100);
	        f1.setParameter(0,1.5*max_inte);
	        f1.setParameter(1,max_t);
	        f1.setParameter(2,155);
       
	        DataFitter.fit(f1, g1, "QER");*/
	         

/*
	        if(0<f1.getParameter(1) && f1.getParameter(1)<10000){ // the fit is not robust for now
	          Pad.add(PadNum.get(p));
	           ADC.add(inte_tot);  // use the signal integral for now
	           Time_o.add(f1.getParameter(1));
	           flag_event=true;
	         }*/
			/*
			c1.draw(g1);
			j1.add(c1);
			j1.setVisible(true);
	        	File dire = new File("/Users/dpaye001/Desktop/PlotOutput/event" + eventnum);
	       	 	dire.mkdir();
			try{ Thread.sleep(1000); } //the sleep time can be modified as needed. Will throw ConcurrentModificationException without
			catch(InterruptedException e){System.out.println("main thread interrupted");}
	        	c1.save("/Users/dpaye001/Desktop/PlotOutput/event" + eventnum + "/pad" + PadNum.get(p) + ".png");
			try{ Thread.sleep(1000); }
			catch(InterruptedException e){System.out.println("main thread interrupted");}
			max_inte=0;
			max_t=0;
			g1.reset();
			c1.clear();
			*/
			
		} 
		params.set_R_adc(Adcmap);
	}				
}
