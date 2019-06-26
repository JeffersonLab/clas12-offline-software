package org.jlab.rec.rtpc.hit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;

import org.jlab.clas.physics.LorentzVector;
import org.jlab.clas.physics.Vector3;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;

public class MapCombine {

	public void MC(HitParameters params, boolean draw) {
		
		HashMap<Integer,Vector<HitVector>> TAMap = params.get_FinalTIDMap();
		HashMap<Integer, HashMap<Integer, Vector<Integer>>> TFMap = params.get_strkTIDMap();
		HashMap<Integer, double[]> ADCMap = params.get_R_adc();
		
		Vector<Integer> PadList = new Vector<Integer>();
		double maxvalue = 0;
		double thresh = 0;
		int TrigWindSize = params.get_TrigWindSize();
		double sumnumer = 0;
		double sumdenom = 0;
		int count = 0;
		int newtid = 0;
		
		HashMap<Integer, Double> weightave = new HashMap<Integer, Double>();
		HashMap<Integer, Double> weightaveadc = new HashMap<Integer, Double>();
		HashMap<Integer, Vector<HitVector>> trTFMap = new HashMap<Integer, Vector<HitVector>>();
		HashMap<Integer, Vector<HitVector>> alltracks = new HashMap<Integer, Vector<HitVector>>();
		HitVector vtmp = new HitVector();
		HitVector v3tmp = new HitVector();
		HitVector hitvec = new HitVector();
		Vector<Vector3> toListvec = new Vector<Vector3>();
		double larget = 0;
		int countadcvalues = 0;
		
		/*GraphErrors g = new GraphErrors();
		EmbeddedCanvas c = new EmbeddedCanvas();
		JFrame jf = new JFrame();
		jf.setSize(800,600);*/
		//System.out.println(TIDMap.size());
		//for(Hit hit : rawHits) {
		for(int TID : TFMap.keySet()){
			//System.out.println(" ");
			larget = 0;
			PadList.clear();
			weightave.clear();
			for(int time : TFMap.get(TID).keySet())
			{
				for(int pad = 0; pad < TFMap.get(TID).get(time).size(); pad++)
				{
					//System.out.println(TID + " " + time + " " + TFMap.get(TID).get(time).get(pad));
					if(!PadList.contains(TFMap.get(TID).get(time).get(pad)))
					{
						PadList.add(TFMap.get(TID).get(time).get(pad));
					}
				}
			}
			
			for(int pad = 0; pad < PadList.size(); pad++)
			{
				for(int time : TFMap.get(TID).keySet())
				{
					if(TFMap.get(TID).get(time).contains(PadList.get(pad)))
					{
						if(ADCMap.get(PadList.get(pad))[time]>maxvalue)
						{
							maxvalue = ADCMap.get(PadList.get(pad))[time];
							//System.out.println(maxvalue);
						}
					}
				}
				thresh = maxvalue/2;
				for(int time = 0; time < TrigWindSize; time+=120)
				{
					if(ADCMap.get(PadList.get(pad))[time] > thresh)
					{
						sumnumer += ADCMap.get(PadList.get(pad))[time]*time;
						sumdenom += ADCMap.get(PadList.get(pad))[time];	
						countadcvalues++;
						//System.out.println(PadList.get(pad) + " " + time);
					}				
				}
				if(sumnumer/sumdenom >= larget)
				{
					larget = sumnumer/sumdenom;
				}
				//System.out.println("weightave " + PadList.get(pad) + " " + sumnumer/sumdenom);
				weightave.put(PadList.get(pad), sumnumer/sumdenom);
				weightaveadc.put(PadList.get(pad), sumdenom);
				sumnumer = 0; 
				sumdenom = 0;
				maxvalue = 0;
				countadcvalues = 0;
				//padvec = PadCoords(PadList.get(pad));
				//gZvsT.addPoint(weightave.get(PadList.get(pad)), padvec.z(), 0, 0);
				//gPhivsT.addPoint(weightave.get(PadList.get(pad)), Math.atan2(padvec.y(),padvec.x()),0,0);
			}
			
			int pad = 0;
			double avetime = 0;
			double adc = 0;
			trTFMap.put(TID,new Vector<HitVector>());
			for(int p = 0; p < PadList.size(); p++)
			{
				pad = PadList.get(p);
				avetime = weightave.get(pad);
				adc = weightaveadc.get(pad);
				//LorentzVector v = new LorentzVector(avetime,Math.atan2(PadCoords(pad).y(),PadCoords(pad).x()),PadCoords(pad).z(),pad);
				HitVector v = new HitVector(pad,PadCoords(pad).z(),Math.atan2(PadCoords(pad).y(),PadCoords(pad).x()),avetime,adc);

				trTFMap.get(TID).add(v);			
			}
			
			
			//trTFMap.put(TID, new HashMap<Integer,Double>());
			//trTFMap.get(TID).putAll(weightave);
			
			//System.out.println(" ");
		}
		for(int choosetid : trTFMap.keySet()) {
			double smalltime = 0;
			for(int i = 0; i < trTFMap.get(choosetid).size(); i++)
			{
				smalltime = trTFMap.get(choosetid).get(i).time();
				for(int j = 0; j < trTFMap.get(choosetid).size();j++)
				{
					if(trTFMap.get(choosetid).get(j).time() < smalltime && i!=j)
					{
						trTFMap.get(choosetid).insertElementAt(trTFMap.get(choosetid).get(i), j);
						trTFMap.get(choosetid).remove(i+1);
						break;
					}
				}
			}
		}
		
		for(int i : TAMap.keySet())
		{
			//toListvec.clear();
			alltracks.put(newtid, new Vector<HitVector>());
			for(int j = 0; j < TAMap.get(i).size(); j++)
			{
				vtmp = TAMap.get(i).get(j);
				//v3tmp = new Vector3(vtmp.pad(),vtmp.time(),0);
				//System.out.println(vtmp.e() + " " + vtmp.px());
				//System.out.println(v3tmp.x() + " " + v3tmp.y());
				//toListvec.add(v3tmp);
				alltracks.get(newtid).add(vtmp);
			}	
	        //System.out.println(toListvec.size());
			//System.out.println(" ");
			//alltracks.put(newtid, toListvec);
			
			newtid++;
			
		}
		toListvec.clear();
		//System.out.println(" ");
		for(int i : trTFMap.keySet())
		{
			//System.out.println(" ");
			//toListvec.clear();
			alltracks.put(newtid, new Vector<HitVector>());			
			//for(int pad : trTFMap.get(i).keySet())
			for(int j = 0; j < trTFMap.get(i).size(); j++)
			{
				v3tmp = trTFMap.get(i).get(j);
				//System.out.println(pad);
				//v3tmp = new HitVector(pad,trTFMap.get(i).get(pad),0);
				//System.out.println(v3tmp.x() + " " + v3tmp.y());
				//toListvec.add(v3tmp);
				alltracks.get(newtid).add(v3tmp);
			}
			//System.out.println(" ");
			//System.out.println(toListvec.size());
			//alltracks.put(newtid, toListvec);
			newtid++;
		}
		HashMap<Integer, Double> largetmap = new HashMap<Integer, Double>();
		larget = 0;
		HashMap<Integer, GraphErrors> histmap = new HashMap<Integer, GraphErrors>();
		HashMap<Integer, EmbeddedCanvas> canvasmap = new HashMap<Integer, EmbeddedCanvas>();
		HashMap<Integer, JFrame> framemap = new HashMap<Integer, JFrame>();
		//H2F adcvst = new H2F("adcvst",500,3000,12000,500,0,10);
		//System.out.println(" ");
		Vector<Integer> marktid = new Vector<Integer>();
		for(int i : alltracks.keySet())
		{
			larget = 0;
			/*if(alltracks.get(i).size() < 6) {
				System.out.println("removed track with " + alltracks.get(i).size() + " hits");
				marktid.add(i);
				continue;
			}*/
			histmap.put(i, new GraphErrors());
			//System.out.println(i);
			for(int j = 0; j < alltracks.get(i).size(); j++)
			{
				//if(j < 3) {g.addPoint(alltracks.get(i).get(j).y(), y, 0, 0);
				//System.out.println(i + " " + alltracks.get(i).get(j).time() + " " + alltracks.get(i).get(j).adc());
				histmap.get(i).addPoint(alltracks.get(i).get(j).time(), alltracks.get(i).get(j).adc(),0,0);
				if(alltracks.get(i).get(j).time() > larget)
				{
					larget = alltracks.get(i).get(j).time();
					
				}
			}
			canvasmap.put(i, new EmbeddedCanvas());
			canvasmap.get(i).draw(histmap.get(i));
			framemap.put(i, new JFrame());
			framemap.get(i).setSize(800, 600);
			framemap.get(i).add(canvasmap.get(i));
			if(draw) framemap.get(i).setVisible(true);
			largetmap.put(i, larget);
			//System.out.println(" ");
		}
		/*for(int i = 0; i < marktid.size(); i++) {
			alltracks.remove(i);
		}*/
		for(int z : canvasmap.keySet()) {
			//canvasmap.get(z).save(z + ".png");
		}
		EmbeddedCanvas c_adcvst = new EmbeddedCanvas();
		c_adcvst.draw(histmap.get(0));
		if(draw) {
			JFrame j_adcvst = new JFrame();
			j_adcvst.setSize(800, 600);
			j_adcvst.add(c_adcvst);
			j_adcvst.setVisible(true);
		}
		
		//System.out.println("There are " + alltracks.size() + " tracks sorted in this event!");
		params.set_alltracks(alltracks);
		params.set_largetmap(largetmap);
	}
	
	private Vector3 PadCoords(int cellID) {
		
		double PAD_W = 2.79; // in mm
		double PAD_S = 80.0; //in mm
        double PAD_L = 4.0; // in mm
	    double RTPC_L= 384.0; // in mm	    
	    double phi_pad = 0;		    
	    //double Num_of_Rows = (2.0*(Math.PI)*PAD_S)/PAD_W;
        double Num_of_Cols = RTPC_L/PAD_L;
	    double PI=Math.PI;
	    double z0 = -(RTPC_L/2.0);
	    double phi_per_pad = PAD_W/PAD_S; // in rad		
        double chan = (double)cellID;       
        double col = chan%Num_of_Cols;
        double row=(chan-col)/Num_of_Cols;
        double z_shift = row%4;
   
        phi_pad=(row*phi_per_pad)+(phi_per_pad/2.0);
        
        if(phi_pad>= 2.0*PI) 
        {
        		phi_pad -= 2.0*PI;
        }
        if(phi_pad<0) 
        	{
        		phi_pad += 2.0*PI;
        	}
   
        double z_pad=z0+(col*PAD_L)+(PAD_L/2.0)+z_shift;
        
        Vector3 PadCoords = new Vector3(PAD_S*Math.cos(phi_pad),PAD_S*Math.sin(phi_pad),z_pad);
        return PadCoords;
		
	}
	
	
}
