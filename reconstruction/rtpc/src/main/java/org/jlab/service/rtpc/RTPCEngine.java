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

    int eventnum = -1;
    
    @Override
    public boolean processDataEvent(DataEvent event) {
        eventnum++;
        HitParameters params = new HitParameters();
        HitReader hitRead = new HitReader();
        hitRead.fetch_RTPCHits(event,true);

        List<Hit> hits = new ArrayList<>();
        //I) get the hits
        hits = hitRead.get_RTPCHits();

        //II) process the hits
        //1) exit if hit list is empty
        if(hits==null || hits.size()==0) {
            return true;
        }
        

        if(event.hasBank("RTPC::adc")){
            //to be removed, signals should be simulated in GEMC
            SignalSimulation SS = new SignalSimulation(hits,params,true); 
            //
            
            //Sort Hits into Tracks at the Readout Pads
            TrackFinder TF = new TrackFinder(params,false);	
            //Calculate Average Time of Hit Signals
            TimeAverage TA = new TimeAverage(params);
            //Reconstruct Hits in Drift Region
            TrackHitReco TR = new TrackHitReco(params,hits,true,eventnum);
            
            //Helix Fit Tracks to calculate Track Parameters
            HelixFitTest HF = new HelixFitTest(params);
            
            RecoBankWriter writer = new RecoBankWriter();	                               
            DataBank recoBank = writer.fillRTPCHitsBank(event,params);
            DataBank trackBank = writer.fillRTPCTrackBank(event,params);
            //if(recoBank == null || trackBank == null) return true;
            event.appendBank(recoBank);
            event.appendBank(trackBank);
            
            
        }
        else{
            return true;
        }
        return true;
    }

    public static void main(String[] args){
        double starttime = System.nanoTime();
        
        File f = new File("/Users/davidpayette/Desktop/SignalStudies/sig.txt");
        f.delete();
        f = new File("/Users/davidpayette/Desktop/SignalStudies/trackenergy.txt");
        f.delete();
        f = new File("/Users/davidpayette/Desktop/SignalStudies/timespectra.txt");
        f.delete();
        f = new File("/Users/davidpayette/Desktop/SignalStudies/sigafter.txt");
        f.delete();
        f = new File("/Users/davidpayette/Desktop/SignalStudies/sigTF.txt");
        f.delete();
        f = new File("/Users/davidpayette/Desktop/SignalStudies/timeenergy.txt");
        f.delete();
        f = new File("/Users/davidpayette/Desktop/SignalStudies/signalbins.txt");
        f.delete();
        
        
        //String inputFile = "/Users/davidpayette/Desktop/6b.2.0/myClara/good.hipo";
        //String inputFile = "/Users/davidpayette/Desktop/6b.2.0/myClara/cosmics.hipo";
        String inputFile = "/Users/davidpayette/Desktop/6b.2.0/myClara/ctest.hipo";
        //String inputFile = "/Users/davidpayette/Desktop/6b.2.0/myClara/new40p.hipo";
        //String inputFile = "/Users/davidpayette/Desktop/rtpcbranch/1ep.hipo";
        //String inputFile = "/Users/davidpayette/Desktop/6b.2.0/myClara/plugins/clas12/340_40p.hipo";
        String outputFile = "/Users/davidpayette/Desktop/6b.2.0/myClara/out_cosmic.hipo";

        System.err.println(" \n[PROCESSING FILE] : " + inputFile);

        RTPCEngine en = new RTPCEngine();
        en.init();

        HipoDataSource reader = new HipoDataSource();	
        HipoDataSync writer = new HipoDataSync();
        reader.open(inputFile);
        writer.open(outputFile);
        //System.out.println("starting " + starttime);
        int eventcount = 0;
        int eventselect = 144; //144
        while(reader.hasEvent()){	           
            DataEvent event = reader.getNextEvent();
            //if(eventcount == eventselect){
            en.processDataEvent(event);
            writer.writeEvent(event);
            //}else if(eventcount > eventselect) break;
            eventcount ++;
        }
        
        writer.close();
        
        System.out.println("finished " + (System.nanoTime() - starttime)*Math.pow(10,-9));
    }
}
