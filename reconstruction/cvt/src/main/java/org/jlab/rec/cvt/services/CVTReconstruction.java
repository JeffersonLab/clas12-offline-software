package org.jlab.rec.cvt.services;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.banks.HitReader;
import org.jlab.rec.cvt.bmt.BMTConstants;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cluster.ClusterFinder;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.cross.CrossMaker;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.svt.SVTParameters;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author ziegler
 * @author devita
 */
public class CVTReconstruction {
        
    private List<ArrayList<Hit>>     CVThits     = new ArrayList<>();
    private List<ArrayList<Cluster>> CVTclusters = new ArrayList<>();
    private List<ArrayList<Cross>>   CVTcrosses  = new ArrayList<>();

    private Swim swimmer;

    public CVTReconstruction() {
    }

    public CVTReconstruction(Swim swimmer) {
        this.swimmer = swimmer;
    }


    public List<ArrayList<Hit>> readHits(DataEvent event, IndexedTable svtStatus, IndexedTable bmtStatus, IndexedTable bmtTime) {
        
        HitReader hitRead = new HitReader();
        hitRead.fetch_SVTHits(event, -1, -1, svtStatus);
        if(Constants.getInstance().svtOnly==false)
          hitRead.fetch_BMTHits(event, swimmer, bmtStatus, bmtTime);

        //I) get the hits
        List<Hit> SVThits = hitRead.getSVTHits();
        if(SVThits == null || SVThits.size()>SVTParameters.MAXSVTHITS) {
            CVThits.add(new ArrayList<>());
        }
        else {
            CVThits.add((ArrayList<Hit>) SVThits);
        }
        List<Hit> BMThits = hitRead.getBMTHits();
        if(BMThits == null || BMThits.size()>BMTConstants.MAXBMTHITS) {
            CVThits.add(new ArrayList<>());
        }
        else {
            CVThits.add((ArrayList<Hit>) BMThits);
        }
        return CVThits;
    }
    
    public List<ArrayList<Cluster>> findClusters() {
        
        List<Cluster> clusters = new ArrayList<>();
        //2) find the clusters from these hits
        ClusterFinder clusFinder = new ClusterFinder();
        if(this.getSVThits().size()>0) {
            clusters.addAll(clusFinder.findClusters(this.getSVThits()));
        }     
        if(this.getBMThits().size() > 0) {
            clusters.addAll(clusFinder.findClusters(this.getBMThits())); 
        }
        
        CVTclusters.add(new ArrayList<>());
        CVTclusters.add(new ArrayList<>());
        for (int i = 0; i < clusters.size(); i++) {
            if (clusters.get(i).getDetector() == DetectorType.BST) {
                CVTclusters.get(0).add(clusters.get(i));
            }
            if (clusters.get(i).getDetector() == DetectorType.BMT) {
                CVTclusters.get(1).add(clusters.get(i));
            }
        }
        return CVTclusters; 
    }
    
    
    public List<ArrayList<Cross>> findCrosses() {
        
        List<Cluster> clusters = new ArrayList<>();
        clusters.addAll(CVTclusters.get(0));
        clusters.addAll(CVTclusters.get(1));
        
        CrossMaker crossMake = new CrossMaker();
        List<ArrayList<Cross>> crosses = crossMake.findCrosses(clusters);
        if(crosses.get(0).size() > SVTParameters.MAXSVTCROSSES ) {
            crosses.get(0).clear();
        }
        return crosses;
    }
        
    
    public int getRun(DataEvent event) {
                
        if (event.hasBank("RUN::config") == false) {
            System.err.println("RUN CONDITIONS NOT READ!");
            return 0;
        }

        DataBank bank = event.getBank("RUN::config");
        int run = bank.getInt("run", 0); 
                
        return run;
    }

    public List<ArrayList<Hit>> getCVThits() {
        return CVThits;
    }

    public List<Hit> getSVThits() {
        return CVThits.get(0);
    }

    public List<Hit> getBMThits() {
        return CVThits.get(1);
    }

    public List<ArrayList<Cluster>> getCVTclusters() {
        return CVTclusters;
    }

    public List<Cluster> getSVTclusters() {
        return CVTclusters.get(0);
    }

    public List<Cluster> getBMTclusters() {
        return CVTclusters.get(1);
    }

    public List<ArrayList<Cross>> getCVTcrosses() {
        return CVTcrosses;
    }

        public List<Cross> getSVTcrosses() {
        return CVTcrosses.get(0);
    }

    public List<Cross> getBMTcrosses() {
        return CVTcrosses.get(1);
    }


    
}
