package org.jlab.service.rtpc;

import java.io.File;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jlab.clas.reco.EngineProcessor;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.rtpc.banks.HitReader;
import org.jlab.rec.rtpc.banks.RecoBankWriter;
import org.jlab.rec.rtpc.hit.Hit;
import org.jlab.rec.rtpc.hit.HitParameters;
import org.jlab.rec.rtpc.hit.SignalSimulation;
import org.jlab.rec.rtpc.hit.TimeAverage;
import org.jlab.rec.rtpc.hit.TrackDisentangler;
import org.jlab.rec.rtpc.hit.TrackFinder;
import org.jlab.rec.rtpc.hit.TrackHitReco;
import org.jlab.rec.rtpc.hit.HelixFitTest;




public class RTPCEngine extends ReconstructionEngine{


    public RTPCEngine() {
        super("RTPC","davidp","3.0");
    }

    private boolean simulation = false;
    private boolean cosmic = false;
    private int fitToBeamline = 1;
    private boolean disentangle = true;

    @Override
    public boolean init() {
        String sim = this.getEngineConfigString("rtpcSimulation");
        String cosm = this.getEngineConfigString("rtpcCosmic");
        String beamfit = this.getEngineConfigString("rtpcBeamlineFit");
        String disentangler = this.getEngineConfigString("rtpcDisentangler");
        //System.out.println(sim + " " + cosm + " " + beamfit);

        if(sim != null){
            simulation = Boolean.valueOf(sim);
        }

        if(cosm != null){
            cosmic = Boolean.valueOf(cosm);
        }

        if(beamfit != null){
           fitToBeamline = Boolean.valueOf(beamfit)?1:0;
        }

        if(disentangler != null){
            disentangle = Boolean.valueOf(disentangler);
        }

        String[] rtpcTables = new String[]{
            "/calibration/rtpc/time_offsets",
            "/calibration/rtpc/gain_balance",
            "/calibration/rtpc/time_parms",
            "/calibration/rtpc/recon_parms"
        };

        requireConstants(Arrays.asList(rtpcTables));

        return true;
    }



    @Override
    public boolean processDataEvent(DataEvent event) {

        HitParameters params = new HitParameters();

        HitReader hitRead = new HitReader();
        hitRead.fetch_RTPCHits(event,simulation,cosmic);//boolean is for simulation

        List<Hit> hits = new ArrayList<>();

        hits = hitRead.get_RTPCHits();

        if(hits==null || hits.size()==0) {
            return true;
        }

        int runNo = 10;
        double magfield = 50.0;
        double magfieldfactor = 1;

        if(event.hasBank("RUN::config")==true){
            DataBank bank = event.getBank("RUN::config");
            runNo = bank.getInt("run", 0);
            magfieldfactor = bank.getFloat("solenoid",0);
            if (runNo<=0) {
                System.err.println("RTPCEngine:  got run <= 0 in RUN::config, skipping event.");
                return false;
            }
        }

        magfield = 50 * magfieldfactor;


        if(event.hasBank("RTPC::adc")){
            params.init(this.getConstantsManager(), runNo);

            SignalSimulation SS = new SignalSimulation(hits,params,simulation); //boolean is for simulation

            //Sort Hits into Tracks at the Readout Pads
            TrackFinder TF = new TrackFinder(params,cosmic);
            //Calculate Average Time of Hit Signals
            TimeAverage TA = new TimeAverage(this.getConstantsManager(),params,runNo);
            //Disentangle Crossed Tracks
            TrackDisentangler TD = new TrackDisentangler(params,disentangle);
            //Reconstruct Hits in Drift Region
            TrackHitReco TR = new TrackHitReco(params,hits,cosmic,magfield);
            //Helix Fit Tracks to calculate Track Parameters
            HelixFitTest HF = new HelixFitTest(params,fitToBeamline,Math.abs(magfield),cosmic);

            RecoBankWriter writer = new RecoBankWriter();
            DataBank recoBank = writer.fillRTPCHitsBank(event,params);
            DataBank trackBank = writer.fillRTPCTrackBank(event,params);

            event.appendBank(recoBank);
            event.appendBank(trackBank);


        }
        else{
            return true;
        }
        return true;
    }

    public static void main(String[] args){

        System.setProperty("CLAS12DIR", "/Users/davidpayette/Desktop/newrtpcbranch/clas12-offline-software");
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
        //String inputFile = "/Users/davidpayette/Desktop/6b.2.0/myClara/ctest.hipo";
        //String inputFile = "/Users/davidpayette/Desktop/6b.2.0/myClara/new40p.hipo";
        //String inputFile = "/Users/davidpayette/Desktop/rtpcbranch/1ep.hipo";
        //String inputFile = "/Users/davidpayette/Desktop/6b.2.0/myClara/plugins/clas12/340_40p.hipo";
        String inputFile = "/Users/davidpayette/Desktop/newrtpcbranch/input.hipo";
        String outputFile = "/Users/davidpayette/Desktop/6b.2.0/myClara/out_cosmic.hipo";

        System.err.println(" \n[PROCESSING FILE] : " + inputFile);

        RTPCEngine en = new RTPCEngine();
        en.init();


        EngineProcessor processor = new EngineProcessor();
        processor.addEngine("RTPC", en);
        processor.processFile(inputFile, outputFile);

        /*
        HipoDataSource reader = new HipoDataSource();
        HipoDataSync writer = reader.createWriter();

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
        */
        System.out.println("finished " + (System.nanoTime() - starttime)*Math.pow(10,-9));
    }
}