package org.jlab.service.ftof;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.detector.geant4.v2.FTOFGeant4Factory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.rec.ftof.Constants;
import org.jlab.rec.tof.banks.ftof.HitReader;
import org.jlab.rec.tof.banks.ftof.RecoBankWriter;
import org.jlab.rec.tof.banks.ftof.TrackReader;
import org.jlab.rec.tof.cluster.Cluster;
import org.jlab.rec.tof.cluster.ClusterFinder;
import org.jlab.rec.tof.cluster.ftof.ClusterMatcher;
import org.jlab.rec.tof.hit.AHit;
import org.jlab.rec.tof.hit.ftof.Hit;
import org.jlab.geometry.prim.Line3d;
import org.jlab.rec.tof.track.Track;

/**
 *
 * @author ziegler
 *
 */
public class FTOFEngine extends ReconstructionEngine {

    public FTOFEngine(String name) {
        super(name, "carman, ziegler", "1.0");
        TrkType = name;
    }
    private final String TrkType;
    
    FTOFGeant4Factory geometry;
    int Run = 0;
    RecoBankWriter rbc;
    
    @Override
    public boolean init() {

        // Load the Constants
        // if (Constants.CSTLOADED == false) {
        Constants.Load();
        rbc = new RecoBankWriter();

        String[]  ftofTables = new String[]{ 
                    "/calibration/ftof/attenuation",
                    "/calibration/ftof/effective_velocity",
                    "/calibration/ftof/time_offsets",
                    "/calibration/ftof/time_walk",
                    "/calibration/ftof/status",
                    "/calibration/ftof/gain_balance",
                    "/calibration/ftof/tdc_conv",
                    "/calibration/ftof/time_jitter",
                    "/calibration/ftof/time_walk_pos",
                    "/calibration/ftof/time_walk_exp",
                    "/calibration/ftof/fadc_offset",
                    "/calibration/ftof/cluster"
                 };
        
        requireConstants(Arrays.asList(ftofTables));
       
       // Get the constants for the correct variation
        this.getConstantsManager().setVariation("default");
        
        // Get geometry database provider, load the geometry tables and create geometry
        String engineVariation = Optional.ofNullable(this.getEngineConfigString("variation")).orElse("default");
        ConstantProvider db = GeometryFactory.getConstants(DetectorType.FTOF, 11, engineVariation);
        geometry = new FTOFGeant4Factory(db);

        return true;
    }

    @Override
    public boolean processDataEvent(DataEvent event) {
        // System.out.println(" PROCESSING EVENT ....");
        // Constants.DEBUGMODE = true;
        //setRunConditionsParameters( event) ;
        if(event.hasBank("RUN::config")==false ) {
		System.err.println("RUN CONDITIONS NOT READ!");
		return true;
	}
		
        DataBank bank = event.getBank("RUN::config");
		
        // Load the constants
        //-------------------
        int  newRun = bank.getInt("run", 0);
        long timeStamp = bank.getLong("timestamp", 0);
        if (newRun<=0) {
            System.err.println("FTOFEngine:  got run <= 0 in RUN::config, skipping event.");
            return false;
        }
        if (timeStamp==-1) {
            System.err.println("FTOFEngine:  got 0 timestamp, skipping event");
            return false;
        }
        
        if (geometry == null) {
            System.err.println(" FTOF Geometry not loaded !!!");
            return false;
        }
        // Get the list of track lines which will be used for matching the FTOF
        // hit to the DC hit
        TrackReader trkRead = new TrackReader();
        ArrayList<Track> tracks = trkRead.fetch_Trks(event);

        List<Hit> hits = new ArrayList<Hit>(); // all hits
        List<Cluster> clusters = new ArrayList<Cluster>(); // all clusters
        // read in the hits for FTOF
        HitReader hitRead = new HitReader();
        hitRead.fetch_Hits(event, timeStamp, geometry, tracks, 
                this.getConstantsManager().getConstants(newRun, "/calibration/ftof/attenuation"),
                this.getConstantsManager().getConstants(newRun, "/calibration/ftof/effective_velocity"),
                this.getConstantsManager().getConstants(newRun, "/calibration/ftof/time_offsets"),
                this.getConstantsManager().getConstants(newRun, "/calibration/ftof/time_walk"),
                this.getConstantsManager().getConstants(newRun, "/calibration/ftof/status"),
                this.getConstantsManager().getConstants(newRun, "/calibration/ftof/gain_balance"),
                this.getConstantsManager().getConstants(newRun, "/calibration/ftof/tdc_conv"),
                this.getConstantsManager().getConstants(newRun, "/calibration/ftof/time_jitter"),
                this.getConstantsManager().getConstants(newRun, "/calibration/ftof/time_walk_pos"),
                this.getConstantsManager().getConstants(newRun, "/calibration/ftof/time_walk_exp"),
                this.getConstantsManager().getConstants(newRun, "/calibration/ftof/fadc_offset"));

        // 1) get the hits
        List<Hit> FTOF1AHits = hitRead.get_FTOF1AHits();
        List<Hit> FTOF1BHits = hitRead.get_FTOF1BHits();
        List<Hit> FTOF2Hits  = hitRead.get_FTOF2Hits();

        // 2) exit if hit list is empty
        if (FTOF1AHits != null) {
            hits.addAll(FTOF1AHits);
        }
        if (FTOF1BHits != null) {
            hits.addAll(FTOF1BHits);
        }
        if (FTOF2Hits != null) {
            hits.addAll(FTOF2Hits);
        }

        if (hits.size() == 0) {
            return true;
        }
        // 2.1) Sort the hits according to sector/layer/component
        Collections.sort(hits);       
        if (Constants.DEBUGMODE) { // if running in DEBUG MODE print out the
            // reconstructed info about the hits and the
            // clusters
            System.out
                    .println("=============== All Hits Before clustering ===============");
            for (Hit hit : hits) {
                hit.printInfo();
            }
        }

        // 3) find the clusters from these hits
        ClusterFinder clusFinder = new ClusterFinder(this.getConstantsManager().getConstants(newRun, "/calibration/ftof/cluster"));
        int[] npaddles = Constants.NPAD;
        int npanels = 3;
        int nsectors = 6;
        if (hits != null && hits.size() > 0) {
            clusters = clusFinder.findClusters(hits, nsectors,
                    npanels, npaddles);
        }
        // 3.1) assign cluster IDs to hits
        if (clusters != null && clusters.size()>0) {
            hitRead.setHitPointersToClusters(hits, clusters);       
        }
        
        // 3.2) exit if cluster list is empty but save the hits
        if (clusters.size() == 0) {
            rbc.appendFTOFBanks(event, hits, null, null, TrkType);
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

        // 4) matching clusters ... not used at this stage...
        ClusterMatcher clsMatch = new ClusterMatcher();
        ArrayList<ArrayList<Cluster>> matchedClusters = clsMatch
                .MatchedClusters(clusters, event);
        if (matchedClusters.size() == 0) {
            rbc.appendFTOFBanks(event, hits, clusters, null, TrkType);
            return true;
        }

        
        // 3.4) exit if cluster list is empty but save the hits
        rbc.appendFTOFBanks(event, hits, clusters, matchedClusters, TrkType);
//            if (event.hasBank("FTOF::adc")) {
//                if (event.hasBank("FTOF::adc")) {
//                    event.getBank("FTOF::adc").show();
//                }
//                if (event.hasBank("FTOF::tdc")) {
//                    event.getBank("FTOF::tdc").show();
//                }
//                if (event.hasBank("FTOF::hits")) {
//                    event.getBank("FTOF::hits").show();
//                }
//            }


        return true;
    }

    

    public static void main(String arg[]) {
        FTOFHBEngine en = new FTOFHBEngine();
        en.init();

        int counter = 0;
        String inputFile = "/Users/ziegler/Desktop/Work/Files/GEMC/out_gemc_orig.hipo";

        // String inputFile = args[0];
        // String outputFile = args[1];

        System.err.println(" \n[PROCESSING FILE] : " + inputFile);

        HipoDataSource reader = new HipoDataSource();
        reader.open(inputFile);

        HipoDataSync writer = new HipoDataSync();
        // Writer
        String outputFile = "/Users/ziegler/Desktop/Work/Files/GEMC/out_gemc_orig_rec.hipo";
        writer.open(outputFile);

        long t1 = 0;
        while (reader.hasEvent() && counter<10) {

            counter++;

            DataEvent event = reader.getNextEvent();
            if (counter > 0) {
                t1 = System.currentTimeMillis();
            }

            //en0.processDataEvent(event);
            //	if (counter > 3062)
            //en0.processDataEvent(event);
            //en1.processDataEvent(event);
            en.processDataEvent(event);
            System.out.println("  EVENT " + counter);
            //if (counter > 3066)
            //	break;
            // event.show();
            // if(counter%100==0)
            //System.out.println("run " + counter + " events");
            //if (event.hasBank("HitBasedTrkg::HBTracks")) {
            //    
            //}
            writer.writeEvent(event);
        }
        writer.close();
        double t = System.currentTimeMillis() - t1;
        System.out.println(t1 + " TOTAL  PROCESSING TIME = "
                + (t / (float) counter));
    }
}
