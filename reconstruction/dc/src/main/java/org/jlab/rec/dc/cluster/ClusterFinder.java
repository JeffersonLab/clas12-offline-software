package org.jlab.rec.dc.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.io.base.DataEvent;

import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.hit.Hit;
import org.jlab.rec.dc.timetodistance.TimeToDistanceEstimator;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.utils.groups.IndexedTable;

/**
 * A hit pruning algorithm to reject noise that gives a pattern of hits that are
 * continguous in the same layer The algorithm first puts the hits in arrays
 * according to their layer and wire number. Each such array contains all the
 * hits in the same layer. The algorithm then collects groups of contiguous hits
 * into a list of hits. The n-first and n-last hits in the list are kept, and
 * all other hits inbetween pruned. The value of n depends on the size of the
 * list. A loose clustering algorithm loops over all superlayers, in a sector
 * and finds groups of hits with contiguous wire index numbers. These clusters
 * (called clumps of hits) are delimited by layers with no hits at a particular
 * wire coordinate. These clusters are then refined using fits to their
 * respective wire indexes as a function of layer number to identify parallel
 * tracks or overlapping track candidates.
 *
 *
 */
public class ClusterFinder {

    public ClusterFinder() {

    }
    private static final Logger LOGGER = Logger.getLogger(ClusterFinder.class.getName());

    // cluster finding algorithm
    // the loop is done over sector and superlayers
    // idx        = superlayer*sector + superlayer
    // sector     = idx/nsect + 1 (starting at 1)
    // superlayer = idx%nsect + 1 (     "      )
    int nsect = Constants.NSECT;
    int nslay = Constants.NSLAY;
    int nlayr = Constants.NLAYR;
    int nwire = Constants.NWIRE;

    private Hit[][][] HitArray = new Hit[nsect * nslay][nwire][nlayr];

    /**
     *
     * @return gets 3-dimentional array of hits as
     * Array[total_nb_sectors*total_nb_superlayers][total_nb_wires][total_nb_layers]
     */
    public Hit[][][] getHitArray() {
        return HitArray;
    }

    /**
     * Sets the hit array
     * Array[total_nb_sectors*total_nb_superlayers][total_nb_wires][total_nb_layers]
     *
     * @param hitArray
     */
    public void setHitArray(Hit[][][] hitArray) {
        HitArray = hitArray;
    }

    /**
     * Fills 3-dimentional array of hits from input hits
     *
     * @param hits the unfitted hit
     * @param rejectLayer
     */
    public void fillHitArray(List<Hit> hits, int rejectLayer) {

        // a Hit Array is used to identify clusters
        Hit[][][] hitArray = new Hit[nsect * nslay][nwire][nlayr];

        // initializing non-zero Hit Array entries
        // with valid hits
        for (Hit hit : hits) {
            if (passHitSelection(hit) && hit.get_Layer() != rejectLayer) {
                int ssl = (hit.get_Sector() - 1) * nsect + (hit.get_Superlayer() - 1);
                int wi = hit.get_Wire() - 1;
                int la = hit.get_Layer() - 1;

                if (wi >= 0 && wi < nwire) {
                    hitArray[ssl][wi][la] = hit;
                }
            }
        }
        this.setHitArray(hitArray);

    }

    /**
     * @param allhits the list of unfitted hits
     * @param ct
     * @return List of clusters
     */
    public List<Cluster> findClumps(List<Hit> allhits, ClusterCleanerUtilities ct) { // a clump is a cluster that is not filtered for noise
        Collections.sort(allhits);

        List<Cluster> clumps = new ArrayList<>();

        // looping over each superlayer in each sector
        // each superlayer is treated independently
        int cid = 1;  // cluster id, will increment with each new good cluster

        for (int ssl = 0; ssl < nsect * nslay; ssl++) {
            // for each ssl, a loop over the wires
            // is done to define clusters
            // clusters are delimited by layers with
            // no hits at a particular wire coordinate

            int wi = 0;  // wire number in the loop
            // looping over all physical wires
            while (wi < nwire) {
                // if there's a hit in at least one layer, it's a cluster candidate
                if (ct.count_nlayers_hit(HitArray[ssl][wi]) != 0) {
                    List<Hit> hits = new ArrayList<>();

                    // adding all hits in this and all the subsequent
                    // wires until there's a wire with no layers hit
                    while (ct.count_nlayers_hit(HitArray[ssl][wi]) > 0 && wi < nwire) {
                        // looping over all physical wires

                        for (int la = 0; la < nlayr; la++) {

                            if (HitArray[ssl][wi][la] != null) {

                                hits.add(HitArray[ssl][wi][la]);
                                //LOGGER.log(Level.FINER, " adding hit "+HitArray[ssl][wi][la].printInfo()+" to cid "+cid);
                            }
                        }
                        wi++;

                    }

                    // Need at least MIN_NLAYERS
                    if (ct.count_nlayers_in_cluster(hits) >= Constants.DC_MIN_NLAYERS) {

                        // cluster constructor DCCluster(hit.sector,hit.superlayer, cid)
                        Cluster this_cluster = new Cluster((int) (ssl / nsect) + 1, (int) (ssl % nsect) + 1, cid++);
                        //LOGGER.log(Level.FINER, " created cluster "+this_cluster.printInfo());
                        this_cluster.addAll(hits);

                        clumps.add(this_cluster);

                    }
                }

                // if no hits, check for next wire coordinate
                wi++;

            }
        }
        return clumps;
    }

    /**
     * @param allhits the list of unfitted hits
     * @param ct
     * @param cf
     * @param DcDetector
     * @return clusters of hits. Hit-based tracking linear fits to the wires are
     * done to determine the clusters. The result is a fitted cluster
     */
    public List<FittedCluster> FindHitBasedClusters(List<Hit> allhits, ClusterCleanerUtilities ct, ClusterFitter cf, DCGeant4Factory DcDetector) {

        //fill array of hit
        this.fillHitArray(allhits, 0);
        //prune noise
        //ct.HitListPruner(allhits, HitArray);
        //find clumps of hits init
        List<Cluster> clusters = this.findClumps(allhits, ct);
       
        allhits.clear();
        
        for (Cluster clus : clusters) {
            Collections.sort(clus);
            allhits.addAll(ct.HitListPruner(clus));
        }
        
        this.fillHitArray(allhits, 0);
        clusters.clear();
        clusters = this.findClumps(allhits, ct);
        
        // create cluster list to be fitted
        List<FittedCluster> selectedClusList = new ArrayList<>();

        for (Cluster clus : clusters) {
            if(clus.size()<Constants.DC_MIN_NLAYERS)
                continue;
            //LOGGER.log(Level.FINER, " I passed this cluster "+clus.printInfo());
            FittedCluster fClus = new FittedCluster(clus);
            //FittedCluster fClus = ct.IsolatedHitsPruner(fclus);
            // Flag out-of-timers
            //if(Constants.isSimulation==true) {
            ct.outOfTimersRemover(fClus, true); // remove outoftimers
            //} else {
            //	ct.outOfTimersRemover(fClus, false); // correct outoftimers
            //}
            // add cluster
            if(fClus.size()<Constants.DC_MIN_NLAYERS)
                continue;
            selectedClusList.add(fClus); 
        }
        
        //LOGGER.log(Level.FINER, " Clusters Step 2");
        // for(FittedCluster c : selectedClusList)
        //	for(FittedHit h : c)
        //		LOGGER.log(Level.FINER, h.printInfo());
        // create list of fitted clusters
        List<FittedCluster> fittedClusList = new ArrayList<>();
        List<FittedCluster> refittedClusList = new ArrayList<>();

        for (FittedCluster clus : selectedClusList) {

            cf.SetFitArray(clus, "LC"); 
            cf.Fit(clus, true);
            if(clus.get_fitProb()<Constants.HITBASEDTRKGMINFITHI2PROB) { 
                ct.IsolatedHitsPruner(clus);
                //Refit
                cf.SetFitArray(clus, "LC"); 
                cf.Fit(clus, true);
            }
            if (clus.get_fitProb() > Constants.HITBASEDTRKGMINFITHI2PROB  ){
                //    || 
                //    (clus.size() < Constants.HITBASEDTRKGNONSPLITTABLECLSSIZE && clus.get_fitProb()!=0) ){            
                fittedClusList.add(clus); //if the chi2 prob is good enough, then just add the cluster, or if the cluster is not split-able because it has too few hits                
            } else {  
                
                List<FittedCluster> splitClus = ct.ClusterSplitter(clus, selectedClusList.size(), cf);
                fittedClusList.addAll(splitClus);              
            }
        }
        
        ArrayList rmHits = new ArrayList<FittedHit>();
        for (FittedCluster clus : fittedClusList) {
            if (clus != null && clus.size() > 3 && clus.get_fitProb()>Constants.HITBASEDTRKGMINFITHI2PROB) {
                
                // update the hits
                for (FittedHit fhit : clus) {
                    fhit.set_TrkgStatus(0);
                    fhit.updateHitPosition(DcDetector); 
                    //fhit.set_AssociatedClusterID(clus.get_Id());
                }
                
                cf.SetFitArray(clus, "TSC"); 
                cf.Fit(clus, true); 
                cf.SetResidualDerivedParams(clus, false, false, DcDetector); //calcTimeResidual=false, resetLRAmbig=false, local= false
                
                clus = ct.ClusterCleaner(clus, cf, DcDetector);
                // update the hits
                for (FittedHit fhit : clus) {
                    fhit.set_AssociatedClusterID(clus.get_Id());
                }
                cf.SetFitArray(clus, "TSC");
                cf.Fit(clus, false);
                cf.SetSegmentLineParameters(clus.get(0).get_Z(), clus);
               
                if (clus != null ) {
                    refittedClusList.add(clus);
                }

            }

        }

        //LOGGER.log(Level.FINER, " Clusters Step 4");
        //for(FittedCluster c : refittedClusList)
        //	for(FittedHit h : c)
        //		LOGGER.log(Level.FINER, h.printInfo());
        return refittedClusList;

    }

    public List<FittedCluster> RecomposeClusters(Map<Integer, ArrayList<FittedHit>> grpHits, 
            DCGeant4Factory dcDetector, ClusterFitter cf) {
        cf.reset();
        List<FittedCluster> clusters = new ArrayList<>();
        
        // using iterators 
        Iterator<Map.Entry<Integer, ArrayList<FittedHit>>> itr = grpHits.entrySet().iterator(); 
          
        while(itr.hasNext()) {
            Map.Entry<Integer, ArrayList<FittedHit>> entry = itr.next(); 
             
            if(entry.getValue().size()>3) {
                Cluster cluster = new Cluster(entry.getValue().get(0).get_Sector(), 
                        entry.getValue().get(0).get_Superlayer(), entry.getValue().get(0).get_AssociatedClusterID());
                FittedCluster fcluster = new FittedCluster(cluster);
                for (FittedHit hit : entry.getValue()) {
                    hit.updateHitPosition(dcDetector); 
                }
                fcluster.addAll(entry.getValue());
                clusters.add(fcluster);
            }
        }
    

        for (FittedCluster clus : clusters) {
            if (clus != null) {
                cf.SetFitArray(clus, "TSC");
                cf.Fit(clus, true);
                cf.SetResidualDerivedParams(clus, true, false, dcDetector); //calcTimeResidual=false, resetLRAmbig=false 
                cf.Fit(clus, false);
                cf.SetSegmentLineParameters(clus.get(0).get_Z(), clus);
                cf.reset();
               
                // update the hits
                for (FittedHit fhit : clus) {
                    fhit.set_TrkgStatus(0);
                    fhit.set_AssociatedClusterID(clus.get_Id());
                }
            }
            
        }

        return clusters;
    }
    
    private List<FittedCluster> RecomposeTrackClusters(DataEvent event, List<FittedHit> fhits, IndexedTable tab, DCGeant4Factory DcDetector, TimeToDistanceEstimator tde) {
        Map<Integer, ArrayList<FittedHit>> grpHits = new HashMap<>();
        List<FittedCluster> clusters = new ArrayList<>();
        
        for (FittedHit hit : fhits) {
            
            if (hit.get_AssociatedClusterID() == -1 || hit.get_AssociatedHBTrackID() == -1) {
                continue;
            }
            if (hit.get_AssociatedClusterID() != -1 &&
                    hit.get_AssociatedHBTrackID() != -1) {
                int index = hit.get_AssociatedHBTrackID()*10000+hit.get_AssociatedClusterID();
                if(grpHits.get(index)==null) { // if the list not yet created make it
                    grpHits.put(index, new ArrayList<>()); 
                    grpHits.get(index).add(hit); // append hit
                } else {
                    grpHits.get(index).add(hit); // append hit
                }
            }
        }
        
        // using iterators 
        Iterator<Map.Entry<Integer, ArrayList<FittedHit>>> itr = grpHits.entrySet().iterator(); 
          
        while(itr.hasNext()) {
            Map.Entry<Integer, ArrayList<FittedHit>> entry = itr.next(); 
             
            if(entry.getValue().size()>3) {
                Cluster cluster = new Cluster(entry.getValue().get(0).get_Sector(), 
                        entry.getValue().get(0).get_Superlayer(), entry.getValue().get(0).get_AssociatedClusterID());
                FittedCluster fcluster = new FittedCluster(cluster);
                for (FittedHit hit : entry.getValue()) {
                    hit.updateHitPosition(DcDetector); 
                }
                fcluster.addAll(entry.getValue());
                clusters.add(fcluster);
            }
        }
    

        for (FittedCluster clus : clusters) {
            if (clus != null) {
                // update the hits
                for (FittedHit fhit : clus) {
                    fhit.set_TrkgStatus(0);
                    fhit.updateHitPositionWithTime(event, 1, fhit.getB(), tab, DcDetector, tde);
                    fhit.set_AssociatedClusterID(clus.get_Id());
                    fhit.set_AssociatedHBTrackID(clus.get(0).get_AssociatedHBTrackID());
                }
            }
        }

        return clusters;
    }

    public List<FittedCluster> FindTimeBasedClusters(DataEvent event, 
            List<FittedHit> fhits, ClusterFitter cf, ClusterCleanerUtilities ct, 
            IndexedTable tab, DCGeant4Factory DcDetector, TimeToDistanceEstimator tde) {

        List<FittedCluster> clusters = new ArrayList<>();

        List<FittedCluster> rclusters = RecomposeTrackClusters(event, fhits, tab, DcDetector, tde);
        //LOGGER.log(Level.FINER, " Clusters TimeBased Step 1");
        //     for(FittedCluster c : rclusters)
        //    	for(FittedHit h : c)
        //    		LOGGER.log(Level.FINER, h.printInfo());

        for (FittedCluster clus : rclusters) {
            // clean them up
            //if(Constants.isSimulation) {  // at this stage only remove secondaries in MC
            FittedCluster cleanClus = ct.SecondariesRemover(event, clus, cf, tab, DcDetector, tde);
            clus = cleanClus;
            //}

            if (clus == null) {
                continue;
            }

           // 	LOGGER.log(Level.FINER, " Clusters TimeBased Step 2ndaries rem");
           // 	for(FittedHit h : clus)
           // 		LOGGER.log(Level.FINER, h.printInfo());
            FittedCluster LRresolvClus = ct.LRAmbiguityResolver(event, clus, cf, tab, DcDetector, tde);
            clus = LRresolvClus;
            if (clus == null) {
                continue;
            }

            //	LOGGER.log(Level.FINER, " Clusters TimeBased Step LR res");
           // 	for(FittedHit h : clus)
             //   	LOGGER.log(Level.FINER, h.printInfo());
            // resolves segments where there are only single hits in layers thereby resulting in a two-fold LR ambiguity
            // hence there are 2 solutions to the segments
            int[] SumLn = new int[6];
            for (FittedHit fhit : clus) {
                SumLn[fhit.get_Layer() - 1]++;
            }
            boolean tryOtherClus = true;
            for (int l = 0; l < 6; l++) {
                if (SumLn[l] > 1) {
                    tryOtherClus = false;
                }
            }

            if (tryOtherClus) {
                FittedCluster Clus2 = new FittedCluster(clus.getBaseCluster());
                for (FittedHit hit : clus) {

                    if (hit.get_LeftRightAmb() != 0) {
                        FittedHit newhit = new FittedHit(hit.get_Sector(), hit.get_Superlayer(), hit.get_Layer(), hit.get_Wire(),
                                hit.get_TDC(), hit.get_Id());
                        newhit.set_Doca(hit.get_Doca());
                        newhit.set_DocaErr(hit.get_DocaErr());
                        newhit.setT0(hit.getT0()); 
                        newhit.set_Beta(hit.get_Beta());
                        newhit.setB(hit.getB());
                        newhit.set_DeltaTimeBeta(hit.get_DeltaTimeBeta());
                        newhit.setTStart(hit.getTStart());
                        newhit.setTProp(hit.getTProp());
                        newhit.betaFlag= hit.betaFlag;
                        newhit.setTFlight(hit.getTFlight());
                        newhit.set_Time(hit.get_Time());
                        newhit.set_Id(hit.get_Id());
                        newhit.set_TrkgStatus(hit.get_TrkgStatus());
                        newhit.set_LeftRightAmb(-hit.get_LeftRightAmb());
                        newhit.calc_CellSize(DcDetector);
                        newhit.set_XWire(hit.get_XWire());
                        newhit.set_Z(hit.get_Z());
                        newhit.set_WireLength(hit.get_WireLength());
                        newhit.set_WireMaxSag(hit.get_WireMaxSag());
                        newhit.set_WireLine(hit.get_WireLine());
                        newhit.updateHitPositionWithTime(event, 1, hit.getB(), tab, DcDetector, tde); // assume the track angle is // to the layer						
                        newhit.set_AssociatedClusterID(hit.get_AssociatedClusterID());
                        newhit.set_AssociatedHBTrackID(hit.get_AssociatedHBTrackID());
                        Clus2.add(newhit);
                    }

                }
                cf.SetFitArray(Clus2, "TSC");
                cf.Fit(Clus2, true);

                if (Math.abs(clus.get_Chisq() - Clus2.get_Chisq()) < 1) {
                    clusters.add(Clus2);
                }
            }
            clusters.add(clus);
        }

        for (FittedCluster clus : clusters) {

            cf.SetFitArray(clus, "TSC");
            cf.Fit(clus, true);

            // update the hits
            for (FittedHit fhit : clus) {
                fhit.updateHitPositionWithTime(event, clus.get_clusterLineFitSlope(), fhit.getB(), tab, DcDetector, tde);
            }
            // iterate till convergence of trkAngle
            double Chi2Diff = 1;
            double prevChi2 = 999999999;
            double trkAngleFinal = 0;
            while (Chi2Diff > 0) {
                cf.SetFitArray(clus, "TSC");
                cf.Fit(clus, true);
                Chi2Diff = prevChi2 - clus.get_Chisq();
                if (Chi2Diff > 0) {
                    // update the hits
                    for (FittedHit fhit : clus) {
                        fhit.updateHitPositionWithTime(event, clus.get_clusterLineFitSlope(), fhit.getB(), tab, DcDetector, tde);
                    }
                    trkAngleFinal = clus.get_clusterLineFitSlope();
                }
                prevChi2 = clus.get_Chisq();
            }

            // update to MP
            cf.SetResidualDerivedParams(clus, false, false, DcDetector); //calcTimeResidual=false, resetLRAmbig=false 

            for (FittedHit fhit : clus) {
                fhit.updateHitPositionWithTime(event, trkAngleFinal, fhit.getB(), tab, DcDetector, tde);
            }
            cf.SetFitArray(clus, "TSC");
            cf.Fit(clus, true);
            cf.SetResidualDerivedParams(clus, true, false, DcDetector); //calcTimeResidual=false, resetLRAmbig=false 

            cf.SetFitArray(clus, "TSC");
            cf.Fit(clus, false);

            cf.SetSegmentLineParameters(clus.get(0).get_Z(), clus);

        }

        return clusters;

    }

    /**
     *
     * @param hit the hit
     * @return a selection cut to pass the hit (for now pass all hits)
     */
    public boolean passHitSelection(Hit hit) {

        return true;
    }

    public EvioDataBank getLayerEfficiencies(List<FittedCluster> fclusters, List<Hit> allhits, ClusterCleanerUtilities ct, ClusterFitter cf, EvioDataEvent event) {

        ArrayList<Hit> clusteredHits = new ArrayList<>();
        for (FittedCluster fclus : fclusters) {
            for (int k = 0; k < fclus.size(); k++) {
                clusteredHits.add(fclus.get(k));
            }
        }
        int[][][] EffArray = new int[6][6][6]; //6 sectors,  6 superlayers, 6 layers
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                for (int k = 0; k < 6; k++) {
                    EffArray[i][j][k] = -1;
                }
            }
        }

        for (int rejLy = 1; rejLy <= 6; rejLy++) {

            //fill array of hit
            this.fillHitArray(clusteredHits, rejLy);
            //find clumps of hits
            List<Cluster> clusters = this.findClumps(clusteredHits, ct);
            // create cluster list to be fitted
            List<FittedCluster> selectedClusList = new ArrayList<>();

            for (Cluster clus : clusters) {
                //LOGGER.log(Level.FINER, " I passed this cluster "+clus.printInfo());
                FittedCluster fclus = new FittedCluster(clus);
                selectedClusList.add(fclus);

            }

            for (FittedCluster clus : selectedClusList) {
                if (clus != null) {

                    int status = 0;
                    //fit
                    cf.SetFitArray(clus, "LC");
                    cf.Fit(clus, true);

                    for (Hit hit : allhits) {

                        if (hit.get_Sector() != clus.get_Sector() || hit.get_Superlayer() != clus.get_Superlayer() || hit.get_Layer() != rejLy) {
                            continue;
                        }

                        double locX = hit.calcLocY(hit.get_Layer(), hit.get_Wire());
                        double locZ = hit.get_Layer();

                        double calc_doca = Math.abs(locX - clus.get_clusterLineFitSlope() * locZ - clus.get_clusterLineFitIntercept());

                        if (calc_doca < 2 * Math.tan(Math.PI / 6.)) {
                            status = 1; //found a hit close enough to the track to assume that the layer is live
                        }
                        int sec = clus.get_Sector() - 1;
                        int slay = clus.get_Superlayer() - 1;
                        int lay = rejLy - 1;

                        EffArray[sec][slay][lay] = status;

                    }
                }
            }
        }
        // now fill the bank
        int bankSize = 6 * 6 * 6;
        EvioDataBank bank = (EvioDataBank) event.getDictionary().createBank("HitBasedTrkg::LayerEffs", bankSize);
        int bankEntry = 0;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                for (int k = 0; k < 6; k++) {
                    bank.setInt("sector", bankEntry, i + 1);
                    bank.setInt("superlayer", bankEntry, j + 1);
                    bank.setInt("layer", bankEntry, k + 1);
                    bank.setInt("status", bankEntry, EffArray[i][j][k]);
                    bankEntry++;
                }
            }
        }
        return bank;

    }
}
