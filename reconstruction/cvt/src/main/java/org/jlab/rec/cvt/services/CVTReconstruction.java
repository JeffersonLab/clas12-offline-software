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
            SVTStripFactory svtIdealStripFactory = new SVTStripFactory( cp, true );
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
                        if (crsSVT.get_Id() == trkcands.get(c).get(ci).get_Id()) {
                            crsSVT.set_Point(trkcands.get(c).get(ci).get_Point());
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

        //String inputFile = "/Users/ziegler/Workdir/Files/GEMC/CVT/YurisTest.hipo";
        String inputFile = "/Users/ziegler/Workdir/Files/cvt/CVTmuons.hipo";

        System.err.println(" \n[PROCESSING FILE] : " + inputFile);

        CVTReconstruction en = new CVTReconstruction();
        en.init();

        int counter = 0;

        HipoDataSource reader = new HipoDataSource();
        reader.open(inputFile);

        HipoDataSync writer = new HipoDataSync();
        //Writer
        String outputFile = "/Users/ziegler/Workdir/Files/cvt/pos_muons.rec.newGeom.hipo";
        writer.open(outputFile);

        long t1 = 0;
        while (reader.hasEvent()) {
            counter++;

            DataEvent event = reader.getNextEvent();
            if (counter > 0) {
                t1 = System.currentTimeMillis();
            }

            // Processing    
            en.processDataEvent(event);
            
            //if(event.hasBank("CVTRec::Tracks")) {
                writer.writeEvent(event);
            //}
            
            System.out.println("  EVENT " + counter);
            /*
			 * event.show();
			if(event.hasBank("CVTRec::Tracks")) {
				 HipoDataEvent de = (HipoDataEvent) event;
				 HipoEvent dde = de.getHipoEvent();
				 HipoGroup group = dde.getGroup("CVTRec::Tracks");
				 dde.show();
				 dde.removeGroup("CVTRec::Tracks");
				 dde.show();
				 dde.writeGroup(group);
				 dde.show();
			}
             */
            if(counter>304) break;
            //event.show();
            //if(counter%100==0)
            //System.out.println("run "+counter+" events");

        }
        writer.close();
        double t = System.currentTimeMillis() - t1;
        System.out.println(t1 + " TOTAL  PROCESSING TIME = " + (t / (float) counter));
    }

}
