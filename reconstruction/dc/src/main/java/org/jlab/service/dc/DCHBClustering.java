package org.jlab.service.dc;

import cnuphys.snr.NoiseReductionParameters;
import cnuphys.snr.clas12.Clas12NoiseAnalysis;
import cnuphys.snr.clas12.Clas12NoiseResult;

import java.util.List;
import org.jlab.clas.swimtools.Swim;
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
 * @author ziegler
 * 
 */
public class DCHBClustering extends DCEngine {
    //some identifier for the type of clustering,
    //ability to plug in more than once

    public DCHBClustering() {
        super("DCCR");
    }
    
    @Override
    public void setDropBanks() {        
        super.registerOutputBank(this.getBanks().getHitsBank());
        super.registerOutputBank(this.getBanks().getClustersBank());
    }
     
    
    @Override
    public boolean processDataEvent(DataEvent event) {
        
        int run = this.getRun(event);
        if(run==0) return true;
        
        double triggerPhase = 0;
        this.getTriggerPhase(event);

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
        RecoBankWriter rbc = new RecoBankWriter(this.getBanks());
        /* 8 */
        HitReader hitRead = new HitReader(this.getBanks());
        /* 9 */
        hitRead.fetch_DCHits(event,
                noiseAnalysis,
                parameters,
                results,
                super.getConstantsManager().getConstants(run, Constants.TIME2DIST),
                super.getConstantsManager().getConstants(run, Constants.TDCTCUTS),
                super.getConstantsManager().getConstants(run, Constants.WIRESTAT),
                Constants.getInstance().dcDetector,
                triggerPhase);
        /* 10 */
        //I) get the hits
        List<Hit> hits = hitRead.get_DCHits(Constants.getInstance().SECTORSELECT);
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
                Constants.getInstance().dcDetector);
        if (clusters.isEmpty()) {
            return true;
        } else {
            List<FittedHit> fhits = rbc.createRawHitList(hits);
            /* 13 */
            rbc.updateListsWithClusterInfo(fhits, clusters);
            rbc.fillAllHBBanks(event,
                    fhits,
                    clusters,
                    null,
                    null,
                    null);
        }
        
        return true;
    }

}
