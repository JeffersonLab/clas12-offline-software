package org.jlab.rec.rtpc.hit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;

import org.jlab.clas.physics.LorentzVector;
import org.jlab.clas.physics.Vector3;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.math.F1D;

public class TimeAverage {

	public void TA(HitParameters params, boolean draw) {
		
		HashMap<Integer, double[]> ADCMap = params.get_R_adc();
		HashMap<Integer, HashMap<Integer, Vector<Integer>>> TIDMap = params.get_TIDMap();
		HashMap<Integer, Vector<HitVector>> trTIDMap = new HashMap<Integer,Vector<HitVector>>();
		HashMap<Integer, Vector<HitVector>> FinalTIDMap = new HashMap<Integer,Vector<HitVector>>();
		Vector<Integer> PadList = new Vector<Integer>();
		HashMap<Integer, Vector<Integer>> hitlist = new HashMap<Integer, Vector<Integer>>();
		int TrigWindSize = params.get_TrigWindSize();
		//int choosetid = 1;
		int StepSize = 120;
		double thresh = 0;
		Vector3 padvec = new Vector3();
		LorentzVector test4vec = new LorentzVector();
		double sumnumer = 0;
		double sumdenom = 0;
		double maxvalue = 0;
		int countadc = 0;
		int newtidcounter = 1;
		int tidsum = 0;
		int min_pads = 3;
		double phithresh = 0.6;
		double zthresh = 24; 
		double timethresh = 1500;
		
		HashMap<Integer,Double> weightave = new HashMap<Integer,Double>();
		HashMap<Integer,Double> weightaveadc = new HashMap<Integer,Double>();
		
		HashMap<Integer, HashMap<Integer,GraphErrors>> gZvsT = new HashMap<Integer, HashMap<Integer, GraphErrors>>();
		HashMap<Integer, HashMap<Integer,GraphErrors>> gPhivsT = new HashMap<Integer, HashMap<Integer, GraphErrors>>();

		EmbeddedCanvas c = new EmbeddedCanvas();
		c.divide(5, 5);
		EmbeddedCanvas c2 = new EmbeddedCanvas();
		c2.divide(5, 5);
		int plotcounter = 0;
		
		JFrame jfr = new JFrame();
		jfr.setSize(1200,1200);
		JFrame jfr2 = new JFrame();
		jfr2.setSize(1200,1200);
		
		
		boolean drawellipse = false;
		
		
		F1D ellipse1a = new F1D("ellipse1a","sqrt([b]*[b](1-((x-[h])*(x-[h])/([a]*[a]))))+[k]",2500,4508);
		F1D ellipse1b = new F1D("ellipse1b","-sqrt([b]*[b](1-((x-[h])*(x-[h])/([a]*[a]))))+[k]",2500,4508);
		F1D ellipse2a = new F1D("ellipse2a","sqrt([b]*[b](1-((x-[h])*(x-[h])/([a]*[a]))))+[k]",2800,4210);
		F1D ellipse2b = new F1D("ellipse2b","-sqrt([b]*[b](1-((x-[h])*(x-[h])/([a]*[a]))))+[k]",2800,4210);
		ellipse1a.setParameter(0, 50);
		ellipse1a.setParameter(1, 3500);
		ellipse1a.setParameter(2, 1000);
		ellipse1a.setParameter(3, -75);
		ellipse1b.setParameter(0, 50);
		ellipse1b.setParameter(1, 3500);
		ellipse1b.setParameter(2, 1000);
		ellipse1b.setParameter(3, -75);
		
		ellipse2a.setParameter(0, 75);
		ellipse2a.setParameter(1, 3500);
		ellipse2a.setParameter(2, 1400);
		ellipse2a.setParameter(3, -75);
		ellipse2b.setParameter(0, 75);
		ellipse2b.setParameter(1, 3500);
		ellipse2b.setParameter(2, 1400);
		ellipse2b.setParameter(3, -75);
		
		for(int choosetid : TIDMap.keySet())
		{
			
			for(int time : TIDMap.get(choosetid).keySet())
			{
				//System.out.println(time);
				for(int pad = 0; pad < TIDMap.get(choosetid).get(time).size(); pad++)
				{
					if(!PadList.contains(TIDMap.get(choosetid).get(time).get(pad)))
					{
						PadList.add(TIDMap.get(choosetid).get(time).get(pad));
					}
				}
			}
			
			for(int pad = 0; pad < PadList.size(); pad++)
			{
				for(int time : TIDMap.get(choosetid).keySet())
				{
					if(TIDMap.get(choosetid).get(time).contains(PadList.get(pad)))
					{
						if(ADCMap.get(PadList.get(pad))[time]>maxvalue)
						{
							maxvalue = ADCMap.get(PadList.get(pad))[time];
						}
					}
				}
				thresh = maxvalue/2;
				countadc = 0;
				for(int time = 0; time < TrigWindSize; time+=120)
				{
					if(ADCMap.get(PadList.get(pad))[time] > thresh)
					{
						sumnumer += ADCMap.get(PadList.get(pad))[time]*time;
						sumdenom += ADCMap.get(PadList.get(pad))[time];	
						countadc++;
						//System.out.println(PadList.get(pad) + " " + time);
					}				
				}
				//System.out.println("weightave " + PadList.get(pad) + " " + sumnumer/sumdenom);
				weightave.put(PadList.get(pad), sumnumer/sumdenom);
				weightaveadc.put(PadList.get(pad), sumdenom);
				sumnumer = 0; 
				sumdenom = 0;
				maxvalue = 0;
				padvec = PadCoords(PadList.get(pad));
				//gZvsT.addPoint(weightave.get(PadList.get(pad)), padvec.z(), 0, 0);
				//gPhivsT.addPoint(weightave.get(PadList.get(pad)), Math.atan2(padvec.y(),padvec.x()),0,0);
			}
			int pad = 0;
			double avetime = 0;
			double adc = 0;
			trTIDMap.put(choosetid,new Vector<HitVector>());
			for(int p = 0; p < PadList.size(); p++)
			{
				pad = PadList.get(p);
				avetime = weightave.get(pad);
				adc = weightaveadc.get(pad);
				//LorentzVector v = new LorentzVector(avetime,Math.atan2(PadCoords(pad).y(),PadCoords(pad).x()),PadCoords(pad).z(),pad);
				HitVector v = new HitVector(pad,PadCoords(pad).z(),Math.atan2(PadCoords(pad).y(),PadCoords(pad).x()),avetime,adc);

				trTIDMap.get(choosetid).add(v);			
			}
			double smalltime = 0;
			for(int i = 0; i < trTIDMap.get(choosetid).size(); i++)
			{
				smalltime = trTIDMap.get(choosetid).get(i).time();
				for(int j = 0; j < trTIDMap.get(choosetid).size();j++)
				{
					if(trTIDMap.get(choosetid).get(j).time() < smalltime && i!=j)
					{
						trTIDMap.get(choosetid).insertElementAt(trTIDMap.get(choosetid).get(i), j);
						trTIDMap.get(choosetid).remove(i+1);
						break;
					}
				}
			}
			int newtid = 150;
			
			
			HitVector padcoords = new HitVector();
			HitVector checkpadcoords = new HitVector();
			
			while(trTIDMap.get(choosetid).size()!=0)
			{		
				hitlist.put(choosetid, new Vector<Integer>());
				hitlist.get(choosetid).add(0);
				padcoords = trTIDMap.get(choosetid).get(0);
				for(int i = 1; i < trTIDMap.get(choosetid).size(); i++)
				{
					checkpadcoords = trTIDMap.get(choosetid).get(i);
					if(Math.abs(checkpadcoords.time() - padcoords.time()) < timethresh && (Math.abs(checkpadcoords.phi()-padcoords.phi())<phithresh || (Math.abs(checkpadcoords.phi()-padcoords.phi()-2*Math.PI) < phithresh )) && Math.abs(checkpadcoords.z()-padcoords.z())<zthresh)
					{
						hitlist.get(choosetid).add(i);
						padcoords = trTIDMap.get(choosetid).get(i);
					}
				}
				trTIDMap.put(newtid,new Vector<HitVector>());
				for(int i = 0; i < hitlist.get(choosetid).size(); i++)
				{
					trTIDMap.get(newtid).add(trTIDMap.get(choosetid).get(hitlist.get(choosetid).get(i)));
					trTIDMap.get(choosetid).set(hitlist.get(choosetid).get(i),new HitVector(0,1e10,0));
				}
				for(int i = 0; i < trTIDMap.get(choosetid).size(); i ++)
				{
					if(trTIDMap.get(choosetid).get(i).time() == 1e10)
					{
						trTIDMap.get(choosetid).remove(i);
						i--;
					}
				}
				hitlist.clear();
				newtid++;
			}
			trTIDMap.remove(choosetid);
			HashMap<Integer, Vector<Integer>> mergelist = new HashMap<Integer, Vector<Integer>>();
			Vector<Integer> mergelistvec = new Vector<Integer>();
			int mergelistindex = 0;
			HitVector testvec1 = new HitVector();
			HitVector testvec2 = new HitVector();
			for(int i : trTIDMap.keySet())
			{
				mergelist.put(mergelistindex, new Vector<Integer>());
				for(int f = trTIDMap.get(i).size() - 1; f >= (trTIDMap.get(i).size() - 5) && f > 0; f--)
				{	
					testvec1 = trTIDMap.get(i).get(f);				
					for(int j : trTIDMap.keySet())
					{
						if(i!=j)
						{
							for(int g = trTIDMap.get(j).size() - 1; g >= (trTIDMap.get(j).size() - 5) && g > 0; g--)
							{
								//System.out.println(f + " " + g);
								testvec2 = trTIDMap.get(j).get(g);
								if((Math.abs(testvec1.time() - testvec2.time()) < timethresh) && (Math.abs(testvec1.phi()-testvec2.phi())<phithresh || (Math.abs(testvec1.phi()-testvec2.phi()-2*Math.PI) < phithresh )) && (Math.abs(testvec1.z()-testvec2.z())<zthresh))
								{
									if(!mergelistvec.contains(i) && !mergelistvec.contains(j))
									{
										mergelistvec.add(i);
										mergelistvec.add(j);
										mergelist.get(mergelistindex).add(i);
										mergelist.get(mergelistindex).add(j);
									}						
								}
							}
						}
					}
				}
				mergelistindex++;
			}
			
			for(int i : trTIDMap.keySet())
			{
				mergelist.put(mergelistindex, new Vector<Integer>());
				for(int f = 0; f <= 5; f++)
				{	
					if(f < trTIDMap.get(i).size()) {
						testvec1 = trTIDMap.get(i).get(f);				
						for(int j : trTIDMap.keySet())
						{
							if(i!=j)
							{
								for(int g = trTIDMap.get(j).size() - 1; g >= (trTIDMap.get(j).size() - 5) && g > 0; g--)
								{
									//System.out.println(f + " " + g);
									testvec2 = trTIDMap.get(j).get(g);
									if((Math.abs(testvec1.time() - testvec2.time()) < timethresh) && (Math.abs(testvec1.phi()-testvec2.phi())<phithresh || (Math.abs(testvec1.phi()-testvec2.phi()-2*Math.PI) < phithresh )) && (Math.abs(testvec1.z()-testvec2.z())<zthresh))
									{
										if(!mergelistvec.contains(i) && !mergelistvec.contains(j))
										{
											mergelistvec.add(i);
											mergelistvec.add(j);
											mergelist.get(mergelistindex).add(i);
											mergelist.get(mergelistindex).add(j);
										}						
									}
								}
							}
						}
					}
				}
				mergelistindex++;
			}
			
			int TIDtokeep = 0; 
			int TIDtomerge = 0;
			for(int i : mergelist.keySet())
			{
				 if(mergelist.get(i).size()>1)
				 {
					 TIDtokeep = mergelist.get(i).get(0);
					 for(int l = 1; l < mergelist.get(i).size(); l++)
					 {
						 TIDtomerge = mergelist.get(i).get(l);
						 trTIDMap.get(TIDtokeep).addAll(trTIDMap.get(TIDtomerge));
						 trTIDMap.remove(TIDtomerge);
					 }
				 }
			}
			Vector<Integer> toremove = new Vector<Integer>();
			for(int i : trTIDMap.keySet())
			{
				if(trTIDMap.get(i).size() < min_pads)
				{
					toremove.add(i);
				}
			}
			for(int i = 0; i < toremove.size(); i++)
			{
				trTIDMap.remove(toremove.get(i));
			}
			gZvsT.put(choosetid, new HashMap<Integer, GraphErrors>());
			c.cd(plotcounter);
			gPhivsT.put(choosetid, new HashMap<Integer, GraphErrors>());
			c2.cd(plotcounter);
			for(int i : trTIDMap.keySet())
			{
				FinalTIDMap.put(newtidcounter,new Vector<HitVector>());
				gZvsT.get(choosetid).put(i, new GraphErrors());
				gZvsT.get(choosetid).get(i).setTitle("Z vs T");
				gPhivsT.get(choosetid).put(i, new GraphErrors());
				gPhivsT.get(choosetid).get(i).setTitle("Phi vs T");
				for(int j = 0; j < trTIDMap.get(i).size(); j++)
				{
					gZvsT.get(choosetid).get(i).addPoint(trTIDMap.get(i).get(j).time(), trTIDMap.get(i).get(j).z(), 0, 0);
					gPhivsT.get(choosetid).get(i).addPoint(trTIDMap.get(i).get(j).time(), trTIDMap.get(i).get(j).phi(), 0, 0);
					FinalTIDMap.get(newtidcounter).add(trTIDMap.get(i).get(j));
				}
				gZvsT.get(choosetid).get(i).setMarkerSize(2);
				gZvsT.get(choosetid).get(i).setMarkerColor(i-149);
				gPhivsT.get(choosetid).get(i).setMarkerSize(2);
				gPhivsT.get(choosetid).get(i).setMarkerColor(i-149);
				c.draw(gZvsT.get(choosetid).get(i),"same");
				c2.draw(gPhivsT.get(choosetid).get(i),"same");
				newtidcounter++;
			}	
			tidsum+= trTIDMap.size();
			PadList.clear();
			weightave.clear();
			trTIDMap.clear();
			hitlist.clear();
			mergelist.clear();
			plotcounter++;
		}
		//System.out.println(plotcounter);
		//System.out.println("tidsum " + tidsum + "final map size " + FinalTIDMap.size());
		/*
		for(int i : gZvsT.keySet())
		{
			c.cd(plotcounter);
			for(int j : gZvsT.get(i).keySet())
			{
				c.draw(gZvsT.get(i).get(j),"same");
			}
			plotcounter++;
		}*/
		if(draw) {
			jfr.add(c);
			jfr.setTitle("Time Averaged ZvsT");
			jfr.setVisible(true);
			jfr2.add(c2);
			jfr2.setTitle("Time Averaged PhivsT");
			jfr2.setVisible(true);
		}
		params.set_FinalTimeMap(FinalTIDMap);
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
