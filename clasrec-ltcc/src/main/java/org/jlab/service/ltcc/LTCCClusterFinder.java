/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.ltcc;

import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 *
 * @author Sylvester Joosten
 */
public final class LTCCClusterFinder {
    // minimum nphe requirements for a hit and cluster center
    static private final double NPHE_MIN_CLUSTER_CENTER = 0;
    // segments from cluster center where we look for additional hits
    // no side requirement needed because we always only consider hits in the
    // same sector
    static private final int DSEGMENT_SEARCH = 3;
    // timing delta in [ns]
    static private final double DTIME_SEARCH = 50.;
    // note: good cluster requirements are defined in the LTCCCluster class
    //       good hits are ensured by LTCCHit.loadHits()
    
    static public List<LTCCCluster> findClusters(List<LTCCHit> hits) {
        List<LTCCCluster> clusters = new ArrayList<>(10);
        
        // sort the hits from highest to lowest nphe
        hits.sort((LTCCHit h1, LTCCHit h2) -> Double.compare(h2.getNphe(), h1.getNphe()));
        // this is not a typo --------------------------------^-------------^
        // the default order for sort is ascending,
        // by reversing the arguments to the compare operations we get a descending list
        // ==> the first entry has the largest nphe
        
        while(!hits.isEmpty()) {
            LTCCHit center = findClusterCenter(hits);
            // no more good cluster centers available
            if (center == null) {
                break;
            }
            LTCCCluster cluster = growCluster(center, hits);
            clusters.add(cluster);
        }
        return clusters;
    }
    
    // get a new cluster center, assuming that the list hits is order in
    // descending order for number of photo-electrons
    static private LTCCHit findClusterCenter(List<LTCCHit> hits) {
        LTCCHit center = hits.remove(0);
        // sanity check
        if (center.getNphe() < NPHE_MIN_CLUSTER_CENTER) {
            center = null;
        }
        
        return center;
    }
    static private LTCCCluster growCluster(LTCCHit center, List<LTCCHit> hits){
        LTCCCluster cluster = new LTCCCluster(center);
        ListIterator<LTCCHit> hitIt = hits.listIterator();
        while (hitIt.hasNext()){
            LTCCHit hit = hitIt.next();
            if (hit.isNeighbor(center, DSEGMENT_SEARCH, DTIME_SEARCH)) {
                cluster.add(hit);
                hitIt.remove();
            }
        }
        return cluster;
    }
}
