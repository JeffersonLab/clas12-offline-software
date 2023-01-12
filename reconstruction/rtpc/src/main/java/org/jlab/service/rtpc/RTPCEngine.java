package org.jlab.service.rtpc;

import java.io.File;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import org.jlab.clas.reco.EngineProcessor;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.clas.tracking.kalmanfilter.Units;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.rtpc.KalmanFilter.KalmanFitter;
import org.jlab.rec.rtpc.KalmanFilter.KalmanFitterInfo;
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
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.utils.groups.IndexedTable;

import org.jlab.clas.tracking.kalmanfilter.Material;



public class RTPCEngine extends ReconstructionEngine{


    public RTPCEngine() {
        super("RTPC","davidp","3.0");
    }

    private boolean simulation = false;
    private boolean cosmic = false;
    private int fitToBeamline = 1;
    private boolean disentangle = true;
    private boolean chi2culling = true; 
    private boolean kfStatus = true;
    private HashMap<String, Material> materialMap;

    @Override
    public boolean init() {
        String sim = this.getEngineConfigString("rtpcSimulation");
        String cosm = this.getEngineConfigString("rtpcCosmic");
        String beamfit = this.getEngineConfigString("rtpcBeamlineFit");
        String disentangler = this.getEngineConfigString("rtpcDisentangler");
	String chi2cull = this.getEngineConfigString("rtpcChi2Cull");
        //System.out.println(sim + " " + cosm + " " + beamfit);
        String kfstatus = this.getEngineConfigString("rtpcKF");

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

        if(chi2cull != null){
           chi2culling = Boolean.valueOf(chi2cull);
        }
        if (kfstatus != null) {
			kfStatus = Boolean.parseBoolean(kfstatus);
		}

        String[] rtpcTables = new String[]{
            "/calibration/rtpc/time_offsets",
            "/calibration/rtpc/gain_balance",
            "/calibration/rtpc/time_parms",
            "/calibration/rtpc/recon_parms",
            "/calibration/rtpc/global_parms",
            "/geometry/rtpc/alignment"
        };

        requireConstants(Arrays.asList(rtpcTables));

        if (materialMap == null) {
            materialMap = generateMaterials();
        }

        return true;
    }



    @Override
    public boolean processDataEvent(DataEvent event) {

        HitParameters params = new HitParameters();

        HitReader hitRead = new HitReader();
        hitRead.fetch_RTPCHits(event,simulation,cosmic);//boolean is for simulation

        List<Hit> hits = new ArrayList<>();

        hits = hitRead.get_RTPCHits();

        if(hits==null || hits.size()==0){
            return true;
        }

        int runNo = 10;
        int eventNo = 777;
        double magfield = 50.0;
        double magfieldfactor = 1;

        if(event.hasBank("RUN::config")==true){
            DataBank bank = event.getBank("RUN::config");
            runNo = bank.getInt("run", 0);
            eventNo = bank.getInt("event",0);
            magfieldfactor = bank.getFloat("solenoid",0);
        }
	
        magfield = 50 * magfieldfactor;
        IndexedTable global_parms = this.getConstantsManager().getConstants(runNo, "/calibration/rtpc/global_parms");
        int hitsbound;
        hitsbound = (int) global_parms.getDoubleValue("MaxHitsEvent",0,0,0);
        if(hits.size() > hitsbound) return true;

       // reading the central detectors z shift with respect to the FD 
       IndexedTable rtpc_alignment = this.getConstantsManager().getConstants(runNo, "/geometry/rtpc/alignment");
       float rtpc_vz_shift;
       rtpc_vz_shift = (float) rtpc_alignment.getDoubleValue("deltaZ",0,0,0);

        if(event.hasBank("RTPC::adc")){
            params.init(this.getConstantsManager(), runNo);

            SignalSimulation SS = new SignalSimulation(hits,params,simulation); //boolean is for simulation

            //Sort Hits into Tracks at the Readout Pads
            TrackFinder TF = new TrackFinder(params,cosmic);
            //Calculate Average Time of Hit Signals
            TimeAverage TA = new TimeAverage(this.getConstantsManager(),params,runNo);
            //Disentangle Crossed Tracks
            TrackDisentangler TD = new TrackDisentangler(params,disentangle,eventNo);
            //Reconstruct Hits in Drift Region
            TrackHitReco TR = new TrackHitReco(params,hits,cosmic,magfield);
            //Helix Fit Tracks to calculate Track Parameters
            HelixFitTest HF = new HelixFitTest(params,fitToBeamline,Math.abs(magfield),cosmic,chi2culling);

            RecoBankWriter writer = new RecoBankWriter();
            DataBank recoBank = writer.fillRTPCHitsBank(event,params);
            DataBank trackBank = writer.fillRTPCTrackBank(event,params,rtpc_vz_shift);

            event.appendBank(recoBank);
            event.appendBank(trackBank);

            if (kfStatus) {
                HashMap<Integer, KalmanFitterInfo> KFTrackMap = new HashMap<>();
                new KalmanFitter(params, KFTrackMap, magfield, materialMap);
                DataBank KFBank = writer.fillRTPCKFBank(event, KFTrackMap);
                event.appendBank(KFBank);
            }


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

    private static HashMap<String, Material> generateMaterials() {
        Units units = Units.CM;

        String name_De = "deuteriumGas";
        double thickness_De = 1;
        double density_De = 9.37E-4;
        double ZoverA_De = 0.496499;
        double X0_De = 0;
        double IeV_De = 19.2;
        org.jlab.clas.tracking.kalmanfilter.Material deuteriumGas =
                new org.jlab.clas.tracking.kalmanfilter.Material(
                        name_De, thickness_De, density_De, ZoverA_De, X0_De, IeV_De, units);

        String name_Bo = "BONuS12Gas";
        double thickness_Bo = 1;
        double density_Bo = 4.9778E-4;
        double ZoverA_Bo = 0.49989;
        double X0_Bo = 0;
        double IeV_Bo = 73.8871;
        org.jlab.clas.tracking.kalmanfilter.Material BONuS12 =
                new org.jlab.clas.tracking.kalmanfilter.Material(
                        name_Bo, thickness_Bo, density_Bo, ZoverA_Bo, X0_Bo, IeV_Bo, units);

        String name_My = "Mylar";
        double thickness_My = 1;
        double density_My = 1.4;
        double ZoverA_My = 0.501363;
        double X0_My = 0;
        double IeV_My = 78.7;
        org.jlab.clas.tracking.kalmanfilter.Material Mylar =
                new org.jlab.clas.tracking.kalmanfilter.Material(
                        name_My, thickness_My, density_My, ZoverA_My, X0_My, IeV_My, units);

        String name_Ka = "Kapton";
        double thickness_Ka = 1;
        double density_Ka = 1.42;
        double ZoverA_Ka = 0.500722;
        double X0_Ka = 0;
        double IeV_Ka = 79.6;
        org.jlab.clas.tracking.kalmanfilter.Material Kapton =
                new org.jlab.clas.tracking.kalmanfilter.Material(
                        name_Ka, thickness_Ka, density_Ka, ZoverA_Ka, X0_Ka, IeV_Ka, units);

        return new HashMap<>() {
            {
                put("deuteriumGas", deuteriumGas);
                put("Kapton", Kapton);
                put("Mylar", Mylar);
                put("BONuS12Gas", BONuS12);
            }
        };
    }
}
