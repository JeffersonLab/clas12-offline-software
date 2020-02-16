package org.jlab.service.ctof;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.coda.jevio.EvioException;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.geant4.v2.CTOFGeant4Factory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geometry.prim.Line3d;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.rec.ctof.Constants;
import org.jlab.rec.tof.banks.ctof.HitReader;
import org.jlab.rec.tof.banks.ctof.RecoBankWriter;
import org.jlab.rec.tof.banks.ctof.TrackReader;
import org.jlab.rec.tof.cluster.Cluster;
import org.jlab.rec.tof.cluster.ClusterFinder;
import org.jlab.rec.tof.hit.AHit;
import org.jlab.rec.tof.hit.ctof.Hit;
import org.jlab.rec.tof.track.Track;

/**
 *
 * @author ziegler
 *
 */
public class CTOFEngine extends ReconstructionEngine {

    public CTOFEngine() {
        super("CTOFRec", "carman, ziegler", "0.5");
    }

    CTOFGeant4Factory geometry;

    int Run = 0;
    RecoBankWriter rbc;
    
    @Override
    public boolean init() {
        // Load the Constants
        // if (Constants.CSTLOADED == false) {
        Constants.Load();
        // }
        rbc = new RecoBankWriter();
        // CalibrationConstantsLoader.Load();
        // }
        String[]  ctofTables = new String[]{ 
                    "/calibration/ctof/attenuation",
                    "/calibration/ctof/effective_velocity",
                    "/calibration/ctof/time_offsets",
                    "/calibration/ctof/tdc_conv",
                    "/calibration/ctof/status",
                    "/calibration/ctof/gain_balance",
                    "/calibration/ctof/time_jitter",
                    "/calibration/ctof/fadc_offset",
                    "/calibration/ctof/hpos",
                    "/calibration/ctof/cluster"
                };
        
        requireConstants(Arrays.asList(ctofTables));
       
       // Get the constants for the correct variation
        this.getConstantsManager().setVariation("default");
        String engineVariation = Optional.ofNullable(this.getEngineConfigString("variation")).orElse("default");
        ConstantProvider cp = GeometryFactory.getConstants(DetectorType.CTOF, 11, engineVariation);
        geometry = new CTOFGeant4Factory(cp);
        
        return true;
    }

    @Override
    public boolean processDataEvent(DataEvent event) {
        //setRunConditionsParameters( event) ;
        if(event.hasBank("RUN::config")==false ) {
            System.err.println("RUN CONDITIONS NOT READ!");
            return true;
        }

        DataBank bank = event.getBank("RUN::config");
//	System.out.println();
//	System.out.println(bank.getInt("event", 0));
        
        // Load the constants
        //-------------------
        final int newRun = bank.getInt("run", 0);
        long timeStamp = bank.getLong("timestamp", 0);
        if (newRun<=0) {
            System.err.println("CTOFEngine:  got run <= 0 in RUN::config, skipping event.");
            return false;
        }
        if (timeStamp==-1) {
            System.err.println("CTOFEngine:  got 0 timestamp, skipping event");
            return false;
        }

        if (geometry == null) {
            System.err.println(" CTOF Geometry not loaded !!!");
            return false;
        }
        // Get the list of track lines which will be used for matching the CTOF
        // hit to the CVT track
        TrackReader trkRead = new TrackReader();
        ArrayList<Track> tracks = trkRead.fetch_Trks(event);
        
        List<Hit> hits = new ArrayList<Hit>(); // all hits
        List<Cluster> clusters = new ArrayList<Cluster>(); // all clusters
        // read in the hits for CTOF
        HitReader hitRead = new HitReader();
        hitRead.fetch_Hits(event, timeStamp, geometry, tracks, 
            this.getConstantsManager().getConstants(newRun, "/calibration/ctof/attenuation"),
            this.getConstantsManager().getConstants(newRun, "/calibration/ctof/effective_velocity"),
            this.getConstantsManager().getConstants(newRun, "/calibration/ctof/time_offsets"),
            this.getConstantsManager().getConstants(newRun, "/calibration/ctof/tdc_conv"),
            this.getConstantsManager().getConstants(newRun, "/calibration/ctof/status"),
            this.getConstantsManager().getConstants(newRun, "/calibration/ctof/gain_balance"),
            this.getConstantsManager().getConstants(newRun, "/calibration/ctof/time_jitter"),
            this.getConstantsManager().getConstants(newRun, "/calibration/ctof/fadc_offset"),
            this.getConstantsManager().getConstants(newRun, "/calibration/ctof/hpos"));

        // 1) get the hits
        List<Hit> CTOFHits = hitRead.get_CTOFHits();

        // 1.1) exit if hit list is empty
        if (CTOFHits != null) {
            hits.addAll(CTOFHits);
        }

        if (hits.size() == 0) {
            // System.err.println(" no hits ....");
            return true;
        }

        // 1.2) Sort the hits for clustering
        Collections.sort(hits);

        // 2) find the clusters from these hits
        ClusterFinder clusFinder = new ClusterFinder(this.getConstantsManager().getConstants(newRun, "/calibration/ctof/cluster"));
//        clusFinder.setDebug(true);
        int[] npaddles = Constants.NPAD;
        int npanels = 1;
        int nsectors = 1;
        List<Cluster> CTOFClusters = null;
        if (CTOFHits != null && CTOFHits.size() > 0) {
            CTOFClusters = clusFinder.findClusters(hits, nsectors, npanels,
                    npaddles);
        }

        if (CTOFClusters != null) {
            clusters.addAll(CTOFClusters);
            // assign cluster IDs to hits
            hitRead.setHitPointersToClusters(hits, clusters);       
        }

        // 2.1) exit if cluster list is empty but save the hits
        if (clusters.size() == 0) {
            rbc.appendCTOFBanks(event, hits, null);
            return true;
        }
        
        // continuing ... there are clusters
        if (Constants.DEBUGMODE) { // if running in DEBUG MODE print out the
            // reconstructed info about the hits and the
            // clusters
            System.out.println("=============== All Hits ===============");
            for (Hit hit : hits) {
                hit.printInfo();
            }
            System.out
                    .println("==================================================");
            for (Cluster cls : clusters) {
                cls.printInfo();
                System.out.println("contains:");
                for (AHit hit : cls) {
                    hit.printInfo();
                }
                System.out
                        .println("---------------------------------------------");
            }
        }
        rbc.appendCTOFBanks(event, hits, clusters); // json file needs clusters...

        return true;

    }

    public static void main(String[] args)  {

        String inputFile  = "/Users/ziegler/Desktop/Work/Files/GEMC/CTOF/pions1degphi80degtheta.hipo";
        String outputFile = "/Users/ziegler/Desktop/Work/Files/GEMC/CTOF/pions1degphi80degthetaRECWithNewCCDB.hipo";
        
        // String inputFile = args[0];
        // String outputFile = args[1];

        System.err.println(" \n[PROCESSING FILE] : " + inputFile);
        
        CTOFEngine en = new CTOFEngine();
        en.init();

        HipoDataSource reader = new HipoDataSource();
        HipoDataSync writer = new HipoDataSync();
        writer.open(outputFile);
        
        int counter = 0;
        reader.open(inputFile);
        long t1 = System.currentTimeMillis();
        while (reader.hasEvent()) {

            counter++;
            DataEvent event = reader.getNextEvent();
           
            en.processDataEvent(event);
            writer.writeEvent(event);
            if (counter > 3) {
                break;
            }
            // if(counter%100==0)
            System.out.println("run " + counter + " events");

        }
        double t = System.currentTimeMillis() - t1;
        System.out.println("TOTAL  PROCESSING TIME = " + t);
        writer.close();
    }

}
