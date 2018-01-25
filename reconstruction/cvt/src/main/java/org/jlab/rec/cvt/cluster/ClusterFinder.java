package org.jlab.rec.cvt.cluster;

import java.util.ArrayList;
import java.util.List;

import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.hit.Hit;

/**
 *
 *
 *
 */
public class ClusterFinder {

    public ClusterFinder() {

    }

    // cluster finding algorithm
    // the loop is done over sectors 
    Hit[][][] HitArray;
    int nstrip = 1200; // max number of strips
    int nlayr = 6;
    int nsec = 18;
    public ArrayList<Cluster> findClusters(List<Hit> hits2,org.jlab.rec.cvt.bmt.Geometry geo_bmt) // the number of strips depends on the layer 
    {
        ArrayList<Cluster> clusters = new ArrayList<Cluster>();

        // a Hit Array is used to identify clusters		
        HitArray = new Hit[nstrip][nlayr][nsec];

        // initializing non-zero Hit Array entries
        // with valid hits
        for (Hit hit : hits2) {

            if (hit.get_Strip().get_Strip() == -1) {
                continue;
            }

            int w = hit.get_Strip().get_Strip();
            int l = hit.get_Layer();
            int s = hit.get_Sector();
            
            if (w > 0 && w < nstrip) {
                HitArray[w - 1][l - 1][s - 1] = hit; 
            }

        }
        int cid = 1;  // cluster id, will increment with each new good cluster

        // for each layer and sector, a loop over the strips
        // is done to define clusters in that module's layer
        // clusters are delimited by strips with no hits 
        for (int s = 0; s < nsec; s++) {
            for (int l = 0; l < nlayr; l++) {
                int si = 0;  // strip index in the loop

                // looping over all strips
                while (si < nstrip) {
                    // if there's a hit, it's a cluster candidate
                    if (HitArray[si][l][s] != null || (si < nstrip - 1 && HitArray[si + 1][l][s] != null)) { 
                        // vector of hits in the cluster candidate
                        ArrayList<FittedHit> hits = new ArrayList<FittedHit>();

                        // adding all hits in this and all the subsequent
                        // strip until there's a strip with no hit
                        while ((si < nstrip - 1 && HitArray[si + 1][l][s] != null) || (HitArray[si][l][s] != null && si < nstrip)) {
                            if (HitArray[si][l][s] != null) { // continue clustering skipping over bad hit
                                FittedHit hitInCls = new FittedHit(HitArray[si][l][s].get_Detector(), HitArray[si][l][s].get_DetectorType(), HitArray[si][l][s].get_Sector(), HitArray[si][l][s].get_Layer(), HitArray[si][l][s].get_Strip());
                                hitInCls.set_Id(HitArray[si][l][s].get_Id());

                                hits.add(hitInCls);
                            }
                            si++;
                        }

                        // define new cluster 
                        Cluster this_cluster = new Cluster(hits.get(0).get_Detector(), hits.get(0).get_DetectorType(), hits.get(0).get_Sector(), l + 1, cid++);
                        this_cluster.set_Id(clusters.size() + 1);
                        // add hits to the cluster
                        this_cluster.addAll(hits); 
                        for (FittedHit h : hits) { 
                            h.set_AssociatedClusterID(this_cluster.get_Id());
                        }

                        this_cluster.calc_CentroidParams(geo_bmt);
                        //make arraylist
                        clusters.add(this_cluster);

                    }
                    // if no hits, check for next wire coordinate
                    si++;
                }
            }
        }
        return clusters;

    }

}
