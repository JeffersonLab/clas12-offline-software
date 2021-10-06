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

    private IndexedTable clusterPar = null;
    private IndexedTable timeRes    = null;
    private IndexedTable vEff       = null;
    
    private boolean debug = false;
    
    public ClusterFinder() {
        // TODO Auto-generated constructor stub
    }

    public ClusterFinder(IndexedTable clusterPar) {
        this.clusterPar=clusterPar;
    }

    public ClusterFinder(IndexedTable clusterPar, IndexedTable timeRes, IndexedTable vEff) {
        this.clusterPar=clusterPar;
        this.timeRes=timeRes;
        this.vEff=vEff;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    private double getMinEnergy(int sector, int layer) {
        double eMin = Constants.MINENERGY;
        // if db tables are supplied get cluster parameters from there
        if(this.clusterPar!=null) {
            eMin  = clusterPar.getDoubleValue("minEnergy", sector, layer, 0);
        }
        if(debug) System.out.println("Setting cluster hit minimum energy to " + eMin);
        return eMin;
    }
    
    private double getMaxSize(int sector, int layer) {
        // default cluster max size is 2
        double size = 2;
        // if db tables are supplied get cluster parameters from there
        if(this.clusterPar!=null) {
            size   = clusterPar.getDoubleValue("maxClusterSize", sector, layer, 0);
        }
        if(debug) System.out.println("Setting cluster max size to " + size);
        return size;
    }
    
    private double getMaxDistance(int sector, int layer, int component1, int component2) {
        double deltaY = 20; // 20 cm
        // if db tables are supplied get cluster parameters from there
        if(this.clusterPar!=null && (this.timeRes==null || this.vEff==null)) {
           deltaY = clusterPar.getDoubleValue("maxDistance", sector, layer, 0);
        }
        // if the time resolutions and effective velocity are supplied, recalculte cluster parameters
        else if(this.timeRes!=null && this.vEff!=null) {
           double tres1 = this.timeRes.getDoubleValue("tres", sector, layer, component1);
           double tres2 = this.timeRes.getDoubleValue("tres", sector, layer, component2);
           double veff1 = (this.vEff.getDoubleValue("veff_left",  sector, layer, component1)+
                           this.vEff.getDoubleValue("veff_right", sector, layer, component1))/2;
           double veff2 = (this.vEff.getDoubleValue("veff_left",  sector, layer, component2)+
                           this.vEff.getDoubleValue("veff_right", sector, layer, component2))/2;
           double yres1 = veff1*tres1;        
           double yres2 = veff2*tres2;        
           double deltaYRes = Math.sqrt(yres1*yres1+yres2*yres2);
           deltaY = deltaYRes*clusterPar.getDoubleValue("maxDistance", sector, layer, 0);
        }
        if(debug) System.out.println("Setting cluster deltaY to " + deltaY);
        return deltaY;                                   
    }
    
    private double getDeltaTime(int sector, int layer, int component1, int component2) {
        double deltaT = 10; // 10 ns
        // if db tables are supplied get cluster parameters from there
        if(this.clusterPar!=null && this.timeRes==null) {
           deltaT = clusterPar.getDoubleValue("maxTimeDifference", sector, layer, 0);
        }
        // if the time resolutions and effective velocity are supplied, recalculte cluster parameters
        else if(this.timeRes!=null) {
           double tres1 = this.timeRes.getDoubleValue("tres", sector, layer, component1);
           double tres2 = this.timeRes.getDoubleValue("tres", sector, layer, component2);
           double deltaTRes = Math.sqrt(tres1*tres1+tres2*tres2);
           deltaT = deltaTRes*clusterPar.getDoubleValue("maxTimeDifference", sector, layer, 0);
        }
        if(debug) System.out.println("Setting cluster deltaT to " + deltaT);
        return deltaT;                                   
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
                        // get hit list for given sector and layer
                        ArrayList<AHit> hitList = HitList.getItem(s,l);
                        for(int i=0; i<hitList.size(); i++) {
                            // for each hit
                            AHit hit = hitList.get(i);
                            hit.set_AssociatedClusterID(0);
                            // reject hits below the selected threshold
                            if (hit.get_Energy() < this.getMinEnergy(s, l)) {
                                continue;
                            }
                            // loop over all clusters 
                            addHitToCluster:
                            for(int j=0; j<clusters.size(); j++) {
                                Cluster cluster = clusters.get(j);
                                // if cluster siz is less than the max allowed
                                if(cluster.size()<this.getMaxSize(s, l)) {
                                    for(int k=0; k<cluster.size(); k++) {
                                        // check if hit can be associated to them
                                        AHit clusterHit=cluster.get(k);
                                        if(hit.isAdjacent(clusterHit)) {
                                            if(Math.abs(hit.get_y()-clusterHit.get_y())<this.getMaxDistance(s, l, hit.get_Paddle(), clusterHit.get_Paddle()) &&
                                               Math.abs(hit.get_t()-clusterHit.get_t())<this.getDeltaTime(s, l, hit.get_Paddle(), clusterHit.get_Paddle())) {
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
