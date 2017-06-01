package org.jlab.rec.tof.cluster;

import java.util.ArrayList;
import java.util.List;

import org.jlab.rec.ctof.Constants;
import org.jlab.rec.tof.hit.AHit;

/**
 *
 * @author ziegler
 *
 */
public class ClusterFinder {

    public ClusterFinder() {
        // TODO Auto-generated constructor stub
    }

    /**
     * int panel, int sector, int paddle paddle = strip, panel = layer,
     */
    AHit[][][] HitArray;

    public ArrayList<Cluster> findClusters(List<?> hits2, int nsectors,
            int npanels, int[] npaddles) {
        ArrayList<Cluster> clusters = new ArrayList<Cluster>();
        if (hits2 != null) {
            int maxNumPad = 0;
            for (int i = 0; i < npaddles.length; i++) {
                if (npaddles[i] > maxNumPad) {
                    maxNumPad = npaddles[i];
                }
            }

            // a Hit Array is used to identify clusters, panel1B is the one with
            // the maximum number of scintillator bars
            HitArray = new AHit[maxNumPad][npanels][nsectors];

            // initializing non-zero Hit Array entries
            // with valid hits
            for (int i = 0; i < hits2.size(); i++) {
                if (((AHit) hits2.get(i)).get_Energy() < Constants.MINENERGY) {
                    continue;
                }

                int w = ((AHit) hits2.get(i)).get_Paddle();
                int l = ((AHit) hits2.get(i)).get_Panel();
                int s = ((AHit) hits2.get(i)).get_Sector();

                if (s > 0 && s <= nsectors && l > 0 && l <= npanels && w > 0
                        && w <= npaddles[l - 1]) {
                    HitArray[w - 1][l - 1][s - 1] = (AHit) hits2.get(i);
                }

            }
            int cid = 1; // cluster id, will increment with each new good
            // cluster

            // for each panel and sector, a loop over the components
            for (int s = 0; s < nsectors; s++) {
                for (int l = 0; l < npanels; l++) {
                    int si = 0; // index in the loop
                    // looping over all bars
                    while (si < npaddles[l]) {
                        // if there's a hit, it's a cluster candidate
                        if (HitArray[si][l][s] != null) {
                            // array of hits in the cluster candidate
                            ArrayList<AHit> hits = new ArrayList<AHit>();
                            try {
                                while (HitArray[si][l][s] != null
                                        && si < npaddles[l]) {
                                    AHit clusteredHit = HitArray[si][l][s];
                                    hits.add(clusteredHit);
                                    si++;
                                }
                            } catch (ArrayIndexOutOfBoundsException exception) {
                                continue;
                            }
                            // define new cluster
                            Cluster this_cluster = new Cluster(s + 1, l + 1,
                                    cid++);
                            // add hits to the cluster
                            this_cluster.addAll(hits);
                            // make arraylist
                            for (AHit hit : this_cluster) {
                                hit.set_AssociatedClusterID(this_cluster
                                        .get_Id());
                            }
                            this_cluster.calc_Centroids();
                            this_cluster.matchToTrack();
                            clusters.add(this_cluster);

                        }
                        // if no hits, check for next
                        si++;
                    }
                }
            }
        }

        return clusters;

    }

}
