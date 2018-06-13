package org.jlab.service.ftof;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.detector.geant4.v2.FTOFGeant4Factory;
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
import org.jlab.service.dc.DCHBEngine;
import org.jlab.geometry.prim.Line3d;
import org.jlab.service.dc.DCTBEngine;

/**
 *
 * @author ziegler
 *
 */
public class FTOFEngine extends ReconstructionEngine {

    public FTOFEngine() {
        super("FTOFRec", "carman, ziegler", "0.5");
    }

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
                 };
        
        requireConstants(Arrays.asList(ftofTables));
       
       // Get the constants for the correct variation
        this.getConstantsManager().setVariation("default");
        
        // Get the geometry
        DatabaseConstantProvider db = new DatabaseConstantProvider( 11, "default");
        // using
        // the
        // new
        // run
        // load the geometry tables
        db.loadTable("/geometry/ftof/panel1a/paddles");
        db.loadTable("/geometry/ftof/panel1a/panel");
        db.loadTable("/geometry/ftof/panel1b/paddles");
        db.loadTable("/geometry/ftof/panel1b/panel");
        db.loadTable("/geometry/ftof/panel2/paddles");
        db.loadTable("/geometry/ftof/panel2/panel");

        // disconncect from database. Important to do this after loading tables.
        db.disconnect();
        geometry = new FTOFGeant4Factory(db);
        // Load the Calibration Constants
        // if (CCDBConstantsLoader.CSTLOADED == false) {
        // DatabaseConstantProvider db = CCDBConstantsLoader.Load();
        // }
        // if(db!=null) {
        // Detector ftofdet = GeometryFactory.getDetector(DetectorType.FTOF);
        // ConstantProvider cp =
        // GeometryFactory.getConstants(DetectorType.FTOF);
        // geometry = new FTOFGeant4Factory(db);
        // }
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
        trkRead.fetch_Trks(event);
        List<Line3d> trkLines = trkRead.get_TrkLines();
        double[] paths = trkRead.get_Paths();
        int[] ids = trkRead.getTrkId();
        List<Hit> hits = new ArrayList<Hit>(); // all hits
        List<Cluster> clusters = new ArrayList<Cluster>(); // all clusters
        // read in the hits for FTOF
        HitReader hitRead = new HitReader();
        hitRead.fetch_Hits(event, timeStamp, geometry, trkLines, paths, ids, 
                this.getConstantsManager().getConstants(newRun, "/calibration/ftof/attenuation"),
                this.getConstantsManager().getConstants(newRun, "/calibration/ftof/effective_velocity"),
                this.getConstantsManager().getConstants(newRun, "/calibration/ftof/time_offsets"),
                this.getConstantsManager().getConstants(newRun, "/calibration/ftof/time_walk"),
                this.getConstantsManager().getConstants(newRun, "/calibration/ftof/status"),
                this.getConstantsManager().getConstants(newRun, "/calibration/ftof/gain_balance"),
                this.getConstantsManager().getConstants(newRun, "/calibration/ftof/tdc_conv"),
                this.getConstantsManager().getConstants(newRun, "/calibration/ftof/time_jitter") );

        // 1) get the hits
        List<Hit> FTOF1AHits = hitRead.get_FTOF1AHits();
        List<Hit> FTOF1BHits = hitRead.get_FTOF1BHits();
        List<Hit> FTOF2Hits = hitRead.get_FTOF2Hits();

        // 1.1) exit if hit list is empty
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

        // 1.2) Sort the hits for clustering
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
        // 2) find the clusters from these hits
        ClusterFinder clusFinder = new ClusterFinder();
        int[] npaddles = Constants.NPAD;
        int npanels = 3;
        int nsectors = 6;
        List<Cluster> FTOF1AClusters = null;
        List<Cluster> FTOF1BClusters = null;
        List<Cluster> FTOF2Clusters = null;
        if (FTOF1AHits != null && FTOF1AHits.size() > 0) {
            FTOF1AClusters = clusFinder.findClusters(FTOF1AHits, nsectors,
                    npanels, npaddles);
        }
        if (FTOF1BHits != null && FTOF1BHits.size() > 0) {
            FTOF1BClusters = clusFinder.findClusters(FTOF1BHits, nsectors,
                    npanels, npaddles);
        }
        if (FTOF2Hits != null && FTOF2Hits.size() > 0) {
            FTOF2Clusters = clusFinder.findClusters(FTOF2Hits, nsectors,
                    npanels, npaddles);
        }

        // next write results to banks
        if (FTOF1AClusters != null) {
            clusters.addAll(FTOF1AClusters);
        }
        if (FTOF1BClusters != null) {
            clusters.addAll(FTOF1BClusters);
        }
        if (FTOF2Clusters != null) {
            clusters.addAll(FTOF2Clusters);
        }
        // 2.1) exit if cluster list is empty but save the hits
        if (clusters.size() == 0) {
            rbc.appendFTOFBanks(event, hits, null, null);
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

        // matching ... not used at this stage...
        ClusterMatcher clsMatch = new ClusterMatcher();
        ArrayList<ArrayList<Cluster>> matchedClusters = clsMatch
                .MatchedClusters(clusters);
        if (matchedClusters.size() == 0) {
            rbc.appendFTOFBanks(event, hits, clusters, null);
            return true;
        }

        rbc.appendFTOFBanks(event, hits, clusters, matchedClusters);

        return true;
    }

    

    public static void main(String arg[]) {

        DCHBEngine en0 = new DCHBEngine();
        en0.init();
        DCTBEngine en1 = new DCTBEngine();
        en1.init();
        FTOFEngine en = new FTOFEngine();
        en.init();

        int counter = 0;
        String inputFile = "/Users/ziegler/Workdir/Distribution/CLARA/CLARA_INSTALL/data/output/out_pion_smearz_gen_1.hipo";
        // String inputFile = args[0];
        // String outputFile = args[1];

        System.err.println(" \n[PROCESSING FILE] : " + inputFile);

        HipoDataSource reader = new HipoDataSource();
        reader.open(inputFile);

        HipoDataSync writer = new HipoDataSync();
        // Writer
        String outputFile = "/Users/ziegler/Workdir/Files/GEMC/TestFTOFSchemaRec2.hipo";
        writer.open(outputFile);

        long t1 = 0;
        while (reader.hasEvent()) {

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
            //System.out.println("  EVENT " + counter);
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
