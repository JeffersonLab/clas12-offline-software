package org.jlab.service.dc;

import cnuphys.snr.NoiseReductionParameters;
import cnuphys.snr.clas12.Clas12NoiseAnalysis;
import cnuphys.snr.clas12.Clas12NoiseResult;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jlab.clas.swimtools.Swim;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.banks.HitReader;
import org.jlab.rec.dc.banks.RecoBankWriter;
import org.jlab.rec.dc.cluster.ClusterCleanerUtilities;
import org.jlab.rec.dc.cluster.ClusterFinder;
import org.jlab.rec.dc.cluster.ClusterFitter;
import org.jlab.rec.dc.cluster.FittedCluster;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.hit.Hit;
/**
 * @author zigler
 * 
 * Reads either TDC or NN hit bank
 * Loads the Tables for the rest of the tracking
 * Creates regular banks or banks storing AI assisted output
 */
public class DCHBClustering extends DCEngine {
    //some identifier for the type of clustering,
    //ability to plug in more than once
    private AtomicInteger Run = new AtomicInteger(0);
    private double triggerPhase;
    private int newRun = 0;

    public DCHBClustering() {
        super("DCHB");
    }
    
    @Override
    public boolean init() {
        super.LoadTables();
        return true;
    }
    public static boolean aiAssist;
    
    @Override
    public boolean processDataEvent(DataEvent event) {
        if (!event.hasBank("RUN::config")) {
            return true;
        }

        /* 1 */
        // get Field
        Swim dcSwim = new Swim();
        /* 2 */
        // init SNR
        Clas12NoiseResult results = new Clas12NoiseResult();
        /* 3 */
        Clas12NoiseAnalysis noiseAnalysis = new Clas12NoiseAnalysis();
        /* 4 */
        NoiseReductionParameters parameters =
                new NoiseReductionParameters(
                        2,
                        Constants.SNR_LEFTSHIFTS,
                        Constants.SNR_RIGHTSHIFTS);
        /* 5 */
        ClusterFitter cf = new ClusterFitter();
        /* 6 */
        ClusterCleanerUtilities ct = new ClusterCleanerUtilities();
        /* 7 */
        RecoBankWriter rbc = new RecoBankWriter();
        /* 8 */
        HitReader hitRead = new HitReader();
        /* 9 */
        hitRead.fetch_DCHits(event,
                noiseAnalysis,
                parameters,
                results,
                super.getConstantsManager().getConstants(newRun, Constants.TIME2DIST),
                super.getConstantsManager().getConstants(newRun, Constants.TDCTCUTS),
                super.getConstantsManager().getConstants(newRun, Constants.WIRESTAT),
                dcDetector,
                triggerPhase);
        /* 10 */
        //I) get the hits
        List<Hit> hits = hitRead.get_DCHits();
        //II) process the hits
        //1) exit if hit list is empty
        if (hits.isEmpty()) {
            return true;
        }
        /* 11 */
        //2) find the clusters from these hits
        ClusterFinder clusFinder = new ClusterFinder();
        List<FittedCluster> clusters = clusFinder.FindHitBasedClusters(hits,
                ct,
                cf,
                dcDetector);
        if (clusters.isEmpty()) {
            return true;
        } else {
            List<FittedHit> fhits = rbc.createRawHitList(hits);
            /* 13 */
            rbc.updateListsListWithClusterInfo(fhits, clusters);
            rbc.fillAllHBBanks(event,
                    rbc,
                    fhits,
                    clusters,
                    null,
                    null,
                    null);
        }
        
        return true;
    }

}
