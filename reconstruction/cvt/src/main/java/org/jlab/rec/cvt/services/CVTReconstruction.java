package org.jlab.rec.cvt.services;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.coda.jevio.EvioException;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.detector.geant4.v2.SVT.SVTConstants;
import org.jlab.detector.geant4.v2.SVT.SVTStripFactory;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.banks.HitReader;
import org.jlab.rec.cvt.banks.RecoBankWriter;
import org.jlab.rec.cvt.bmt.CCDBConstantsLoader;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cluster.ClusterFinder;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.cross.CrossMaker;
import org.jlab.rec.cvt.hit.ADCConvertor;
import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.track.Seed;
import org.jlab.rec.cvt.track.Track;
import org.jlab.rec.cvt.track.TrackListFinder;
import org.jlab.rec.cvt.track.TrackSeeder;
import org.jlab.rec.cvt.track.fit.KFitter;
import org.jlab.rec.cvt.trajectory.TrkSwimmer;
import org.jlab.service.eb.EBHBEngine;
import org.jlab.service.eb.EBTBEngine;
//import org.jlab.service.eb.EBHBEngine;

/**
 * Service to return reconstructed BST track candidates- the output is in Evio
 * format
 *
 * @author ziegler
 *
 */
public class CVTReconstruction extends ReconstructionEngine {

    org.jlab.rec.cvt.svt.Geometry SVTGeom;
    org.jlab.rec.cvt.bmt.Geometry BMTGeom;
    SVTStripFactory svtIdealStripFactory;
    
    public CVTReconstruction() {
        super("CVTTracks", "ziegler", "4.0");

        SVTGeom = new org.jlab.rec.cvt.svt.Geometry();
        BMTGeom = new org.jlab.rec.cvt.bmt.Geometry();

    }

    String FieldsConfig = "";
    int Run = -1;
  
    public void setRunConditionsParameters(DataEvent event, String FieldsConfig, int iRun, boolean addMisAlignmts, String misAlgnFile) {
        if (event.hasBank("RUN::config") == false) {
            System.err.println("RUN CONDITIONS NOT READ!");
            return;
        }

        int Run = iRun;

        boolean isMC = false;
        boolean isCosmics = false;
        DataBank bank = event.getBank("RUN::config");
        //System.out.println(bank.getInt("Event")[0]);
        if (bank.getByte("type", 0) == 0) {
            isMC = true;
        }
        if (bank.getByte("mode", 0) == 1) {
            isCosmics = true;
        }

        boolean isSVTonly = false;

        // Load the fields
        //-----------------
        String newConfig = "SOLENOID" + bank.getFloat("solenoid", 0);

        if (FieldsConfig.equals(newConfig) == false) {
            // Load the Constants
            System.out.println(" ........................................ trying to connect to db ");
            CCDBConstantsLoader.Load(bank.getInt("run", 0));
            DatabaseConstantProvider cp = new DatabaseConstantProvider(bank.getInt("run", 0), "default");
            
            cp = SVTConstants.connect( cp );
            SVTConstants.loadAlignmentShifts( cp );
            cp.disconnect();
            // create the factory
            //SVTStripFactory svtShiftedStripFactory = new SVTStripFactory( cp, true );
            SVTStripFactory svtIdealStripFactory = new SVTStripFactory( cp, false );
            SVTGeom.setSvtIdealStripFactory(svtIdealStripFactory);
            
            System.out.println("  CHECK CONFIGS..............................." + FieldsConfig + " = ? " + newConfig);
            Constants.Load(isCosmics, isSVTonly, (double) bank.getFloat("solenoid", 0));
            // Load the Fields
            System.out.println("************************************************************SETTING FIELD SCALE *****************************************************");
            TrkSwimmer.setMagneticFieldScale(bank.getFloat("solenoid", 0)); // something changed in the configuration
            this.setFieldsConfig(newConfig);
        }
        FieldsConfig = newConfig;

        // Load the constants
        //-------------------
        int newRun = bank.getInt("run", 0);

        if (Run != newRun) {
            
            this.setRun(newRun);

        }
        Run = newRun;
        this.setRun(Run);
    }

    public int getRun() {
        return Run;
    }

    public void setRun(int run) {
        Run = run;
    }

    public String getFieldsConfig() {
        return FieldsConfig;
    }

    public void setFieldsConfig(String fieldsConfig) {
        FieldsConfig = fieldsConfig;
    }
    @Override
    public boolean processDataEvent(DataEvent event) {
        this.setRunConditionsParameters(event, FieldsConfig, Run, false, "");

        this.FieldsConfig = this.getFieldsConfig();
        this.Run = this.getRun();

        ADCConvertor adcConv = new ADCConvertor();

        RecoBankWriter rbc = new RecoBankWriter();

        HitReader hitRead = new HitReader();
        hitRead.fetch_SVTHits(event, adcConv, -1, -1, SVTGeom);
        hitRead.fetch_BMTHits(event, adcConv, BMTGeom);

        List<Hit> hits = new ArrayList<Hit>();
        //I) get the hits
        List<Hit> svt_hits = hitRead.get_SVTHits();
        if (svt_hits != null && svt_hits.size() > 0) {
            hits.addAll(svt_hits);
        }

        List<Hit> bmt_hits = hitRead.get_BMTHits();
        if (bmt_hits != null && bmt_hits.size() > 0) {
            hits.addAll(bmt_hits);
        }

        //II) process the hits		
        List<FittedHit> SVThits = new ArrayList<FittedHit>();
        List<FittedHit> BMThits = new ArrayList<FittedHit>();
        //1) exit if hit list is empty
        if (hits.size() == 0) {
            return true;
        }

        List<Cluster> clusters = new ArrayList<Cluster>();
        List<Cluster> SVTclusters = new ArrayList<Cluster>();
        List<Cluster> BMTclusters = new ArrayList<Cluster>();

        //2) find the clusters from these hits
        ClusterFinder clusFinder = new ClusterFinder();
        clusters.addAll(clusFinder.findClusters(svt_hits));
        
        clusters.addAll(clusFinder.findClusters(bmt_hits)); 
        
        if (clusters.size() == 0) {
            return true;
        }

        // fill the fitted hits list.
        if (clusters.size() != 0) {
            for (int i = 0; i < clusters.size(); i++) {
                if (clusters.get(i).get_Detector() == 0) {
                    SVTclusters.add(clusters.get(i));
                    SVThits.addAll(clusters.get(i));
                }
                if (clusters.get(i).get_Detector() == 1) {
                    BMTclusters.add(clusters.get(i));
                    BMThits.addAll(clusters.get(i));
                }
            }
        }

        List<ArrayList<Cross>> crosses = new ArrayList<ArrayList<Cross>>();
        CrossMaker crossMake = new CrossMaker();
        crosses = crossMake.findCrosses(clusters, SVTGeom);
        
        TrackSeeder trseed = new TrackSeeder();
        KFitter kf;
        List<Track> trkcands = new ArrayList<Track>();
        List<Seed> seeds = trseed.findSeed(SVTclusters, SVTGeom, crosses.get(1), BMTGeom);
        for (Seed seed : seeds) { 
            
            kf = new KFitter(seed, SVTGeom, event);
            kf.runFitter(SVTGeom, BMTGeom);

            trkcands.add(kf.OutputTrack(seed, SVTGeom));
            if (kf.setFitFailed == false) {
                trkcands.get(trkcands.size() - 1).set_TrackingStatus(2);
           } else {
                trkcands.get(trkcands.size() - 1).set_TrackingStatus(1);
           }
        }

        if (trkcands.size() == 0) {
            rbc.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null);
            return true;
        }

        //This last part does ELoss C
        TrackListFinder trkFinder = new TrackListFinder();
        List<Track> trks = new ArrayList<Track>();
        trks = trkFinder.getTracks(trkcands, SVTGeom, BMTGeom);

        for (int c = 0; c < trkcands.size(); c++) {
            trkcands.get(c).set_Id(c + 1);
            for (int ci = 0; ci < trkcands.get(c).size(); ci++) {

                if (crosses.get(0).size() > 0) {
                    for (Cross crsSVT : crosses.get(0)) {
                        if (crsSVT.get_Sector() == trkcands.get(c).get(ci).get_Sector() && crsSVT.get_Cluster1()!=null && crsSVT.get_Cluster2()!=null 
                                && trkcands.get(c).get(ci).get_Cluster1()!=null && trkcands.get(c).get(ci).get_Cluster2()!=null
                                && crsSVT.get_Cluster1().get_Id() == trkcands.get(c).get(ci).get_Cluster1().get_Id()
                                && crsSVT.get_Cluster2().get_Id() == trkcands.get(c).get(ci).get_Cluster2().get_Id()) {  
                            crsSVT.set_Point(trkcands.get(c).get(ci).get_Point());
                            trkcands.get(c).get(ci).set_Id(crsSVT.get_Id());
                            crsSVT.set_PointErr(trkcands.get(c).get(ci).get_PointErr());
                            crsSVT.set_Dir(trkcands.get(c).get(ci).get_Dir());
                            crsSVT.set_DirErr(trkcands.get(c).get(ci).get_DirErr());
                            crsSVT.set_AssociatedTrackID(c + 1);
                            crsSVT.get_Cluster1().set_AssociatedTrackID(c + 1);
                            for (FittedHit h : crsSVT.get_Cluster1()) {
                                h.set_AssociatedTrackID(c + 1);
                            }
                            for (FittedHit h : crsSVT.get_Cluster2()) {
                                h.set_AssociatedTrackID(c + 1);
                            }
                            crsSVT.get_Cluster2().set_AssociatedTrackID(c + 1);

                        }
                    }
                }
                if (crosses.get(1).size() > 0) {
                    for (Cross crsBMT : crosses.get(1)) {
                        if (crsBMT.get_Id() == trkcands.get(c).get(ci).get_Id()) {
                            crsBMT.set_Point(trkcands.get(c).get(ci).get_Point());
                            crsBMT.set_PointErr(trkcands.get(c).get(ci).get_PointErr());
                            crsBMT.set_Dir(trkcands.get(c).get(ci).get_Dir());
                            crsBMT.set_DirErr(trkcands.get(c).get(ci).get_DirErr());
                            crsBMT.set_AssociatedTrackID(c + 1);
                            crsBMT.get_Cluster1().set_AssociatedTrackID(c + 1);
                            for (FittedHit h : crsBMT.get_Cluster1()) {
                                h.set_AssociatedTrackID(c + 1);
                            }
                        }
                    }
                }
            }
        }

        //crosses.get(0).removeAll(crosses.get(0));
        //crosses.get(0).addAll(crossesOntrk);
        //REMOVE THIS
        //crosses.get(0).addAll(crosses.get(1));
        //------------------------
        // set index associations
        if (trks.size() > 0) {

            rbc.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, trks);
        }

        return true;

    }

    public boolean init() {

        TrkSwimmer.getMagneticFields();
        return true;
    }

    public static void main(String[] args) throws FileNotFoundException, EvioException {
        /*
       //String inputFile = "/Users/ziegler/Desktop/Work/Files/Data/DecodedData/CVT/1537.0.skim.positive.sol.hipo";
       String inputFile = "/Users/ziegler/Desktop/Work/Files/GEMC/CVT/CVTtracks.hipo";

        System.err.println(" \n[PROCESSING FILE] : " + inputFile);

        CVTReconstruction en = new CVTReconstruction();
        en.init();
       //EBHBEngine eb = new EBHBEngine();
       //eb.init();
        int counter = 0;

        HipoDataSource reader = new HipoDataSource();
        reader.open(inputFile);

        HipoDataSync writer = new HipoDataSync();
        //Writer
        //String outputFile = "/Users/ziegler/Desktop/Work/Files/Data/DecodedData/CVT/1537.0.skim.positive.sol.rec.hipo";
        String outputFile = "/Users/ziegler/Desktop/Work/Files/GEMC/CVT/CVTtracks.rec.hipo";
        writer.open(outputFile);

        long t1 = 0;
        while (reader.hasEvent()) {
            

            DataEvent event = reader.getNextEvent();
            if (counter > 0) {
                t1 = System.currentTimeMillis();
            }

            // Processing    
            en.processDataEvent(event);
            //eb.processDataEvent(event);
            
            if(event.hasBank("CVTRec::Tracks")) {
                counter++;
                writer.writeEvent(event); 
            }
            
            System.out.println("  EVENT " + event.getBank("RUN::config").getInt("event",0));
            
            //if(event.getBank("RUN::config").getInt("event",0)>=1461) break;
            //event.show();
            //if(counter%100==0)
            //System.out.println("run "+counter+" events");

        }
        writer.close();
        double t = System.currentTimeMillis() - t1;
        System.out.println(t1 + " TOTAL  PROCESSING TIME = " + (t / (float) counter));
        */
        DataEvent testEvent = getCVTTestEvent();

        CVTReconstruction CVTengine = new CVTReconstruction();
        CVTengine.init();
        CVTengine.processDataEvent(testEvent);
        testEvent.show();
        if(testEvent.hasBank("CVTRec::Tracks")) {
            testEvent.getBank("CVTRec::Tracks").show();
        }
        EBHBEngine EBHBengine = new EBHBEngine();
        EBHBengine.init();
        EBHBengine.processDataEvent(testEvent);

        EBTBEngine EBTBengine = new EBTBEngine();
        EBTBengine.init();
        EBTBengine.processDataEvent(testEvent);

        System.out.println(isWithinXPercent(10.0, testEvent.getBank("REC::Particle").getFloat("px", 0), -0.375)+" "+
		isWithinXPercent(10.0, testEvent.getBank("REC::Particle").getFloat("py", 0), 0.483)
		+" "+isWithinXPercent(10.0, testEvent.getBank("REC::Particle").getFloat("pz", 0), 0.674)
		+" "+isWithinXPercent(30.0, testEvent.getBank("REC::Particle").getFloat("vz", 0), -13.9));
    }
    public static boolean isWithinXPercent(double X, double val, double standard) {
        if(standard >= 0 && val > (1.0 - (X/100.0))*standard && val < (1.0 + (X/100.0))*standard) return true;
        else if(standard < 0 && val < (1.0 - (X/100.0))*standard && val > (1.0 + (X/100.0))*standard) return true;
        return false;
	}
    public static HipoDataEvent getCVTTestEvent() {
		HipoDataSync writer = new HipoDataSync();
		HipoDataEvent testEvent = (HipoDataEvent) writer.createEvent();
		DataBank config = testEvent.createBank("RUN::config", 1);
		DataBank SVTadc = testEvent.createBank("BST::adc", 7);
		DataBank mc = testEvent.createBank("MC::Particle", 1);
		// this event is based on a gemc (4a.1.1 aka 4a.2.0) event with
		// torus = -1.0 , solenoid = 1.0
		//	<option name="BEAM_P"   value="proton, 0.91*GeV, 42.2*deg, 127.8*deg"/>
		// <option name="SPREAD_P" value="0*GeV, 0*deg, 0*deg"/>
		// <option name="BEAM_V" value="(0, 0, -1.39)cm"/>
		// <option name="SPREAD_V" value="(0.0, 0.0)cm"/>

		config.setInt("run", 0, (int) 11);
		config.setInt("event", 0, (int) 1);
		config.setInt("trigger", 0, (int) 0);
		config.setLong("timestamp", 0, (long) 0);
		config.setByte("type", 0, (byte) 0);
		config.setByte("mode", 0, (byte) 0);
		config.setFloat("torus", 0, (float) -1.0);
		config.setFloat("solenoid", 0, (float) 1.0);
		config.setFloat("rf", 0, (float) 0.0);
		config.setFloat("startTime", 0, (float) 0.0);
		
		for(int i = 0; i < 7; i++) {
			SVTadc.setByte("order", i, (byte) 0);
			SVTadc.setShort("ped", i, (short) 0);
			SVTadc.setLong("timestamp", i, (long) 0);
		}

		SVTadc.setByte("sector", 0, (byte) 5);
		SVTadc.setByte("sector", 1, (byte) 5);
		SVTadc.setByte("sector", 2, (byte) 7);
		SVTadc.setByte("sector", 3, (byte) 7);
		SVTadc.setByte("sector", 4, (byte) 7);
		SVTadc.setByte("sector", 5, (byte) 9);
		SVTadc.setByte("sector", 6, (byte) 9);
		
		SVTadc.setByte("layer", 0, (byte) 1);
		SVTadc.setByte("layer", 1, (byte) 2);
		SVTadc.setByte("layer", 2, (byte) 3);
		SVTadc.setByte("layer", 3, (byte) 4);
		SVTadc.setByte("layer", 4, (byte) 4);
		SVTadc.setByte("layer", 5, (byte) 5);
		SVTadc.setByte("layer", 6, (byte) 6);
		
		SVTadc.setShort("component", 0, (short) 109);
		SVTadc.setShort("component", 1, (short) 77);
		SVTadc.setShort("component", 2, (short) 52);
		SVTadc.setShort("component", 3, (short) 137);
		SVTadc.setShort("component", 4, (short) 138);
		SVTadc.setShort("component", 5, (short) 1);
		SVTadc.setShort("component", 6, (short) 190);
		
		SVTadc.setInt("ADC", 0, (int) 7);
		SVTadc.setInt("ADC", 1, (int) 7);
		SVTadc.setInt("ADC", 2, (int) 7);
		SVTadc.setInt("ADC", 3, (int) 5);
		SVTadc.setInt("ADC", 4, (int) 5);
		SVTadc.setInt("ADC", 5, (int) 7);
		SVTadc.setInt("ADC", 6, (int) 7);
		
		SVTadc.setFloat("time", 0, (float) 97.0);
		SVTadc.setFloat("time", 1, (float) 201.0);
		SVTadc.setFloat("time", 2, (float) 78.0);
		SVTadc.setFloat("time", 3, (float) 102.0);
		SVTadc.setFloat("time", 4, (float) 81.0);
		SVTadc.setFloat("time", 5, (float) 91.0);
		SVTadc.setFloat("time", 6, (float) 205.0);

		testEvent.appendBank(config);
                testEvent.appendBank(mc);
		testEvent.appendBank(SVTadc);

		return testEvent;
	}

}
