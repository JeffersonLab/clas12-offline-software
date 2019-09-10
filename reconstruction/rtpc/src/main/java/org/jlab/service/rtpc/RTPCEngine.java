package org.jlab.service.rtpc;

import java.io.File;
import java.io.FileNotFoundException;


import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.rec.rtpc.banks.HitReader;
import org.jlab.rec.rtpc.banks.RecoBankWriter;
import org.jlab.rec.rtpc.hit.Hit;
import org.jlab.rec.rtpc.hit.HitParameters;
import org.jlab.rec.rtpc.hit.SignalSimulation;
import org.jlab.rec.rtpc.hit.TimeAverage;
//import org.jlab.rec.rtpc.hit.TrackDisentangler;
import org.jlab.rec.rtpc.hit.TrackFinder;
import org.jlab.rec.rtpc.hit.TrackHitReco;
import org.jlab.rec.rtpc.hit.HelixFitTest;




public class RTPCEngine extends ReconstructionEngine{


    public RTPCEngine() {
        super("RTPC","davidp","3.0");
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

        List<Hit> hits = new ArrayList<>();
        //I) get the hits
        hits = hitRead.get_RTPCHits();

        //II) process the hits
        //1) exit if hit list is empty
        if(hits==null || hits.size()==0) {
            return true;
        }

        if(event.hasBank("RTPC::pos")){
            //to be removed, signals should be simulated in GEMC
            SignalSimulation SS = new SignalSimulation(hits,params); 
            //
            
            //Sort Hits into Tracks at the Readout Pads
            TrackFinder TF = new TrackFinder(params);	
            //Calculate Average Time of Hit Signals
            TimeAverage TA = new TimeAverage(params);
            //Reconstruct Hits in Drift Region
            TrackHitReco TR = new TrackHitReco(params);
            //Helix Fit Tracks to calculate Track Parameters
            HelixFitTest HF = new HelixFitTest(params);
            
            RecoBankWriter writer = new RecoBankWriter();	                               
            DataBank recoBank = writer.fillRTPCHitsBank(event,params);
            DataBank trackBank = writer.fillRTPCTrackBank(event,params);
            event.appendBanks(recoBank);
            event.appendBanks(trackBank);
        }
        else{
            return true;
        }
        return true;
    }

    public static void main(String[] args){
        double starttime = System.nanoTime();
        
        String inputFile = "/Users/davidpayette/Desktop/6b.2.0/myClara/plugins/clas12/5000_40_12Jul.hipo";
        String outputFile = "/Users/davidpayette/Desktop/5b.7.4/myClara/tout_working.hipo";

        System.err.println(" \n[PROCESSING FILE] : " + inputFile);

        RTPCEngine en = new RTPCEngine();
        en.init();

        HipoDataSource reader = new HipoDataSource();	
        HipoDataSync writer = new HipoDataSync();
        reader.open(inputFile);
        writer.open(outputFile);
        System.out.println("starting " + starttime);

        while(reader.hasEvent()){	
            DataEvent event = reader.getNextEvent();			
            en.processDataEvent(event);
            writer.writeEvent(event);
      
        }
        
        writer.close();
        
        System.out.println("finished " + (System.nanoTime() - starttime)*Math.pow(10,-9));
    }
}
