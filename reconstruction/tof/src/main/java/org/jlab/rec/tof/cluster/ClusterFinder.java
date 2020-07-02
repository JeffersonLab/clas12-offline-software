package org.jlab.rec.tof.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jlab.rec.ctof.Constants;
import org.jlab.rec.tof.hit.AHit;
import org.jlab.utils.groups.IndexedList;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author ziegler, ahobart, devita
 *
 */
public class ClusterFinder {

    private double minEnergy   = Constants.MINENERGY;
    private double maxDistance = 20; // default set to 20 cm
    private double deltaTime   = 10; // default set to 10 ns
    private int    maxSize     = 2 ; // default set to cluster size of 2
    private IndexedTable clusterPar = null;
    
    private boolean debug = false;
    
    public ClusterFinder() {
        // TODO Auto-generated constructor stub
    }

    public ClusterFinder(IndexedTable clusterPar) {
        this.clusterPar=clusterPar;
    }


    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    
    public ArrayList<Cluster> findClusters(List<?> hits2, int nsectors, int npanels, int[] npaddles) {
        // allocate list of hits for each sector and layer
        IndexedList<ArrayList<AHit>> HitList = new IndexedList<ArrayList<AHit>>(2); 

        // create cluster list
        ArrayList<Cluster> clusters = new ArrayList<Cluster>();
        
        // loop over list of hits
        if (hits2 != null) {

            if(debug) {
                System.out.println();
                System.out.println("Unsorted");
            }
            // initializing array lists
            for (int i = 0; i < hits2.size(); i++) {
                AHit thisHit = (AHit) hits2.get(i);
                int l = thisHit.get_Panel();
                int s = thisHit.get_Sector();
                // if list for sector s and layer l doesn't exist yet, create it
                if(!HitList.hasItem(s,l)) HitList.add(new ArrayList<AHit>(), s,l);
                // add hit to relevant list
                HitList.getItem(s,l).add(thisHit);
                if(debug) System.out.println(s + " " + l + " " + thisHit.get_Paddle() + " " + thisHit.get_Energy() + " " + thisHit.get_t());
            }
            
            // sort lists by energy
            if(debug) System.out.println("Sorted");
            for (int s = 1; s <= nsectors; s++) {
                for (int l = 1; l <= npanels; l++) {
                    if(HitList.hasItem(s,l)) {
                        if(debug) System.out.println(s + " " + l);
                        ArrayList<AHit> hitList = HitList.getItem(s,l);
                        Collections.sort(hitList, new sortByEnergy());
                        if(debug) {
                            for(int i=0; i<hitList.size(); i++) {            
                                AHit thisHit=hitList.get(i);
                                System.out.println(thisHit.get_Paddle() + " " + thisHit.get_Energy() + " " + thisHit.get_t());
                            }
                        }
                    }
                }
            }
            
            // create clusters within each sector and panel
            for (int s = 1; s <= nsectors; s++) {
                for (int l = 1; l <= npanels; l++) {
                    if(HitList.hasItem(s,l)) {
                        // use constants from CCDB if available to set cluster parameters
                        if(this.clusterPar!=null) {
                            this.minEnergy   = clusterPar.getDoubleValue("minEnergy", s, l, 0);
                            this.maxDistance = clusterPar.getDoubleValue("maxDistance", s, l, 0);
                            this.deltaTime   = clusterPar.getDoubleValue("maxTimeDifference", s, l, 0);
                            this.maxSize     = clusterPar.getIntValue("maxClusterSize", s, l, 0);
                            if(debug) System.out.println("Setting cluster parameters to " + this.minEnergy + "/" + this.maxDistance + " " + this.deltaTime + "/" + this.maxSize);
                        }
                        // get hit list for given sector and layer
                        ArrayList<AHit> hitList = HitList.getItem(s,l);
                        for(int i=0; i<hitList.size(); i++) {
                            // for each hit
                            AHit hit = hitList.get(i);
                            hit.set_AssociatedClusterID(0);
                            // reject hits below the selected threshold
                            if (hit.get_Energy() < this.minEnergy) {
                                continue;
                            }
                            // loop over all clusters 
                            addHitToCluster:
                            for(int j=0; j<clusters.size(); j++) {
                                Cluster cluster = clusters.get(j);
                                // if cluster siz is less than the max allowed
                                if(cluster.size()<this.maxSize) {
                                    for(int k=0; k<cluster.size(); k++) {
                                        // check if hit can be associated to them
                                        AHit clusterHit=cluster.get(k);
                                        if(hit.isAdjacent(clusterHit)) {
                                            if(Math.abs(hit.get_y()-clusterHit.get_y())<this.maxDistance &&
                                               Math.abs(hit.get_t()-clusterHit.get_t())<this.deltaTime) {
                                                cluster.add(hit);
                                                hit.set_AssociatedClusterID(cluster.get_Id());
                                                if(debug) System.out.println("Adding hit " + hit.get_Sector() + " " + hit.get_Panel() + " " + hit.get_Paddle() 
                                                                 + " " + hit.get_Energy() + " " + hit.get_t() + " " + hit.get_y() 
                                                                 + " to cluster " + cluster.get_Id());
                                                // as soon as hit is associated to a cluster stop looping over clusters
                                                break addHitToCluster;
                                            }
                                        }
                                    }
                                }
                            }
                            // if hit was not associated to any existing cluster, create a new one
                            if(hit.get_AssociatedClusterID()==0) {
                                Cluster cluster = new Cluster(s, l, clusters.size()+1);
                                cluster.add(hit);
                                if(debug) System.out.println("Creating new cluster with hit " + hit.get_Sector() + " " + hit.get_Panel() + " " + hit.get_Paddle() 
                                                 + " " + hit.get_Energy() + " " + hit.get_t() + " " + hit.get_y() + " " + hit.get_TrkId()
                                                 + " and id " + cluster.get_Id());
                                clusters.add(cluster);
                            }
                        }
                    }
                }
            }
            // calculate cluster parameters
            for(int i=0; i<clusters.size(); i++) {
                clusters.get(i).calc_Centroids();
                clusters.get(i).matchToTrack();
            }
        }
        
        return clusters;

    }
    
    class sortByEnergy implements Comparator<AHit> {
        @Override
        public int compare(AHit a, AHit b) {
            if(a.get_Energy()<b.get_Energy()) {
                    return 1;
            } else {
                    return -1;
            }  
        }
    }
}
