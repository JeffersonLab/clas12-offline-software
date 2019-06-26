package org.jlab.service.rtpc;

import java.io.File;
import java.io.FileNotFoundException;


import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.reco.ReconstructionEngine;
//import org.jlab.coda.hipo.HipoException;
//import org.jlab.coda.jevio.EvioException;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.rec.rtpc.banks.HitReader;
import org.jlab.rec.rtpc.banks.RecoBankWriter;
import org.jlab.rec.rtpc.banks.RecoBankWriter2;
import org.jlab.rec.rtpc.hit.Hit;
import org.jlab.rec.rtpc.hit.HitDistance;
import org.jlab.rec.rtpc.hit.HitParameters;
import org.jlab.rec.rtpc.hit.HitReconstruction;
import org.jlab.rec.rtpc.hit.MapCombine;
import org.jlab.rec.rtpc.hit.PadAve;
import org.jlab.rec.rtpc.hit.PadFit;
import org.jlab.rec.rtpc.hit.PadHit;
//import org.jlab.rec.rtpc.hit.PulseShapeAnalysis;
import org.jlab.rec.rtpc.hit.TimeAverage;
import org.jlab.rec.rtpc.hit.TimeAverage2;
//import org.jlab.rec.rtpc.hit.TrackDisentangler;
//import org.jlab.rec.rtpc.hit.TrackFinder;
import org.jlab.rec.rtpc.hit.TrackFinder2;
import org.jlab.rec.rtpc.hit.TrackFinder3;
import org.jlab.rec.rtpc.hit.TrackHitReco;
import org.jlab.rec.rtpc.hit.TrackHitReco2;
import org.jlab.rec.rtpc.hit.TrackHitReco3;




public class RTPCEngine extends ReconstructionEngine{

	int test = 0;
	int swtch = 2;
	
	
	public RTPCEngine() {
		super("RTPC","charlesg","3.0");
	}

	@Override
	public boolean init() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean processDataEvent(DataEvent event) {
                HitParameters params = new HitParameters();
		HitReader hitRead = new HitReader();
		hitRead.fetch_RTPCHits(event);
		
		List<Hit> hits = new ArrayList<Hit>();
		//I) get the hits
		hits = hitRead.get_RTPCHits();
		
		//II) process the hits
		//1) exit if hit list is empty
		if(hits==null || hits.size()==0) {
			return true;
		}
		if(event.hasBank("RTPC::pos"))
		{
			PadHit phit = new PadHit();
			phit.bonus_shaping(hits,params);
			PadFit pfit = new PadFit();
			pfit.Fitting(params);
			
//			PulseShapeAnalysis psa = new PulseShapeAnalysis();
//			psa.PSA(params);
			
			//TrackFinder TF = new TrackFinder();
			//TF.FindTrack(params);
			if(swtch == 1) //outdated build
			{
				HitDistance HD = new HitDistance();
				HD.FindDistance(params);
			}
			else if(swtch == 2) { //current build only use this one
				TrackFinder3 TF = new TrackFinder3(params,false);	
				TimeAverage2 TA2 = new TimeAverage2(params,false);
				//TrackDisentangler TD = new TrackDisentangler(params,true);
				TrackHitReco3 TR3 = new TrackHitReco3(hits,params,false);
				RecoBankWriter2 writer = new RecoBankWriter2();				
				DataBank recoBank = writer.fillRTPCHitsBank(event, params);
				event.appendBanks(recoBank);
			}
			else    //outdated build
			{
				TrackFinder2 TF = new TrackFinder2();
				TF.FindTrack2(params,false);
				TimeAverage TA = new TimeAverage();
				TA.TA(params,false);
				MapCombine MC = new MapCombine();
				MC.MC(params,false);
				/*TrackHitReco TR = new TrackHitReco();
				TR.Reco(hits,params);*/
				TrackHitReco2 TR = new TrackHitReco2();
				TR.Reco(hits,params,false);
				RecoBankWriter2 writer = new RecoBankWriter2();				
				DataBank recoBank = writer.fillRTPCHitsBank(event, params);
				event.appendBanks(recoBank);
				//recoBank.show();
			}
			
		}
		else
		{
			return true;
		}

		/*
		for(Hit h : hits) {
			System.out.println("Hit  "+h.get_Id()+" CellID "+h.get_cellID()+" ADC "+h.get_ADC()+" true Edep "+h.get_EdepTrue()+" Edep "+h.get_Edep()+" Time "+h.get_Time()+" "+
		" true X "+h.get_PosXTrue()+" X "+h.get_PosX()+" true Y "+h.get_PosYTrue()+" Y "+h.get_PosY()+" true Z "+h.get_PosZTrue()+" Z "+h.get_PosZ());
		}*/
		
		
		
		
		return true;
	}
	
	public static void main(String[] args){
		double starttime = System.nanoTime();
		//String inputFile = "/Users/davidpayette/Desktop/5c.2.3/clara/installation/plugins/clas12/2_72_516.hipo";
		//String inputFile = "/Users/davidpayette/Desktop/5c.2.3/clara/installation/plugins/clas12/1000_1_711.hipo";
		//String inputFile = "/Users/davidpayette/Desktop/5c.2.3/clara/installation/plugins/clas12/100_20_731.hipo";
		//String inputFile = "/Users/davidpayette/Desktop/5c.3.5/clara/installation/plugins/clas12/test.hipo";
		//String inputFile = "/Users/davidpayette/Desktop/5b.7.4/myClara/1212.hipo";
		//String inputFile = "/Users/davidpayette/Desktop/5b.7.4/myClara/plugins/clas12/Jantest.hipo";
		String inputFile = "/Users/davidpayette/Desktop/5b.7.4/myClara/plugins/clas12/newfeb.hipo";
		//String inputFile = "/Users/davidpayette/Desktop/Distribution/clas12-offline-software/1212again.hipo";
		//String inputFile = "/Users/davidpayette/Desktop/5c.2.3/clara/installation/plugins/clas12/100_20_802.hipo";
		//String inputFile = "/Users/davidpayette/Desktop/5c.2.3/clara/installation/plugins/clas12/10p.hipo";
		//String inputFile = args[0];
		String outputFile = "/Users/davidpayette/Desktop/5b.7.4/myClara/tout_working.hipo";
		
		System.err.println(" \n[PROCESSING FILE] : " + inputFile);

		RTPCEngine en = new RTPCEngine();
		en.init();
		
		
		
		HipoDataSource reader = new HipoDataSource();	
		HipoDataSync writer = new HipoDataSync();
		reader.open(inputFile);
		writer.open(outputFile);
		System.out.println("starting " + starttime);
		
		File f1= new File("/Users/davidpayette/Documents/FileOutput/PulseShapeAll.txt");
		File f2= new File("/Users/davidpayette/Documents/FileOutput/PulseShapeMax.txt");
		File f3= new File("/Users/davidpayette/Documents/FileOutput/PulseShapeAllOverMax.txt");
		f1.delete();
		f2.delete();
		f3.delete();
		
		while(reader.hasEvent()){	
			
			DataEvent event = reader.getNextEvent();			
			en.processDataEvent(event);
			writer.writeEvent(event);
		}
		writer.close();
		System.out.println("finished " + (System.nanoTime() - starttime)*Math.pow(10,-9));
	}
}
