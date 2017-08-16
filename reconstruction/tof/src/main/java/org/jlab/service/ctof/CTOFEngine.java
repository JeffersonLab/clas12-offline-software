package org.jlab.service.ctof;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.coda.jevio.EvioException;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.detector.geant4.v2.CTOFGeant4Factory;
import org.jlab.geometry.prim.Line3d;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.rec.ctof.Constants;
import org.jlab.rec.ctof.CCDBConstantsLoader;
import org.jlab.rec.tof.banks.ctof.HitReader;
import org.jlab.rec.tof.banks.ctof.RecoBankWriter;
import org.jlab.rec.tof.banks.ctof.TrackReader;
import org.jlab.rec.tof.cluster.Cluster;
import org.jlab.rec.tof.cluster.ClusterFinder;
import org.jlab.rec.tof.hit.AHit;
import org.jlab.rec.tof.hit.ctof.Hit;

/**
 *
 * @author ziegler
 *
 */
public class CTOFEngine extends ReconstructionEngine {

    public CTOFEngine() {
        super("CTOFRec", "carman, ziegler", "0.4");
    }

    CTOFGeant4Factory geometry;

    int Run = -1;
    RecoBankWriter rbc;

    @Override
    public boolean init() {
        // Load the Constants
        // if (Constants.CSTLOADED == false) {
        Constants.Load();
        // }
        rbc = new RecoBankWriter();
        geometry = new CTOFGeant4Factory();
        // CalibrationConstantsLoader.Load();
        // }
        return true;
    }

    @Override
    public boolean processDataEvent(DataEvent event) {
        setRunConditionsParameters(event);

        if (geometry == null) {
            System.err.println(" CTOF Geometry not loaded !!!");
            return false;
        }
        // Get the list of track lines which will be used for matching the CTOF
        // hit to the CVT track
        TrackReader trkRead = new TrackReader();
        trkRead.fetch_Trks(event);
        List<Line3d> trkLines = trkRead.get_TrkLines();
        double[] paths = trkRead.get_Paths();

        List<Hit> hits = new ArrayList<Hit>(); // all hits
        List<Cluster> clusters = new ArrayList<Cluster>(); // all clusters
        // read in the hits for CTOF
        HitReader hitRead = new HitReader();
        hitRead.fetch_Hits(event, geometry, trkLines, paths);

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
        ClusterFinder clusFinder = new ClusterFinder();
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
        //rbc.appendCTOFBanks( event, hits, clusters);
        rbc.appendCTOFBanks(event, hits, null); // json file needs clusters...
        return true;

    }

    public void setRunConditionsParameters(DataEvent event) {

        if (event.hasBank("RUN::config") == false) {
            System.err.println("RUN CONDITIONS NOT READ!");
            return;
        }
        boolean isMC = false;
        boolean isCosmics = false;
        DataBank bank = event.getBank("RUN::config");

        if (bank.getByte("type", 0) == 0) {
            isMC = true;
        }
        if (bank.getByte("mode", 0) == 1) {
            isCosmics = true;
        }
        // force cosmics
        //isCosmics = true;
        //System.out.println(bank.getInt("Event")[0]);
        boolean isCalib = isCosmics;  // all cosmics runs are for calibration right now
        //

        // Load the constants
        //-------------------
        int newRun = bank.getInt("run", 0);

        if (Run != newRun) {
            CCDBConstantsLoader.Load(newRun);
            DatabaseConstantProvider db = CCDBConstantsLoader.getDB();

            geometry = new CTOFGeant4Factory();
        }
        Run = newRun;

    }

    public static void main(String[] args) throws FileNotFoundException,
            EvioException {

        String inputFile = "/Users/ziegler/Workdir/Files/test/piminus_cd_0.5-2.0GeV.hipo";
        // String inputFile = args[0];
        // String outputFile = args[1];

        System.err.println(" \n[PROCESSING FILE] : " + inputFile);

        CTOFEngine en = new CTOFEngine();
        en.init();

        HipoDataSource reader = new HipoDataSource();

        int counter = 0;

        reader.open(inputFile);
        long t1 = System.currentTimeMillis();
        while (reader.hasEvent()) {

            counter++;
            DataEvent event = reader.getNextEvent();
            en.processDataEvent(event);

            if (counter > 50) {
                break;
            }
            // if(counter%100==0)
            System.out.println("run " + counter + " events");

        }
        double t = System.currentTimeMillis() - t1;
        System.out.println("TOTAL  PROCESSING TIME = " + t);
    }

}
