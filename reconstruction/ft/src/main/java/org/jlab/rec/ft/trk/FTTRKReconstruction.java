/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.ft.trk;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorLayer;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.rec.ft.FTConstants;


/**
 *
 * @author devita
 * @author filippi
 */


public class FTTRKReconstruction {

    public static int debugMode = 0;  // 1 for verbose, set it here (better be set in the steering Engine) PROVISIONAL
    public static int[] sectorLimits = 
        {0, 64, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448, 480, 512, 544, 576, 608, 640, 704, 768};
    public static int NStripsSectors = 20;
    public static int stripsInLongSector = 64;
    public static int stripsInShortSector = 32;
    public static int sideShortStrips;      // 256
    public static int sequentialStrips;     // 512
    public static int stripDiscontinuity1;  // 128   CHECK DISCONTINUITIES +- 1 strip
    public static int stripDiscontinuity2;  // 385
    public static int stripDiscontinuity3;  // 640
    public static int stripDiscontinuity4;  // 641
    
    public FTTRKReconstruction() {
    }
    public List<FTTRKHit> initFTTRK(DataEvent event, ConstantsManager manager, int run) {

        /*  tables are not needed for FTTRK reco at this point
        IndexedTable charge2Energy = manager.getConstants(run, "/calibration/ft/fthodo/charge_to_energy");
        IndexedTable timeOffsets   = manager.getConstants(run, "/calibration/ft/fthodo/time_offsets");
        IndexedTable geometry      = manager.getConstants(run, "/geometry/ft/fthodo");
        IndexedTable trkGeo        = manager.getConstants(run, "/geometry/ft/fttrk");
        */
        
        sideShortStrips = 2*FTTRKConstantsLoader.SideHalfstrips;   // 256
        sequentialStrips = FTTRKConstantsLoader.Longstrips + FTTRKConstantsLoader.SideHalfstrips + sideShortStrips; // 512
        stripDiscontinuity1 = FTTRKConstantsLoader.Longstrips; // 128   CHECK DISCONTINUITIES +- 1 strip
        stripDiscontinuity2 = FTTRKConstantsLoader.Longstrips + sideShortStrips + 1;  // 385
        stripDiscontinuity3 = sequentialStrips + FTTRKConstantsLoader.SideHalfstrips; // 640
        stripDiscontinuity4 = stripDiscontinuity3+1;    
        
        if(debugMode>=1) System.out.println("\nAnalyzing new event");
        List<FTTRKHit> allhits = null;
        
//      allhits = this.readRawHits(event,charge2Energy,timeOffsets,geometry,trkGeo);
        allhits = this.readRawHits(event, run);
        
        if(debugMode>=1) {
            System.out.println("Found " + allhits.size() + " hits");
            for(int i = 0; i < allhits.size(); i++) {
                System.out.print(i + "\t");
                System.out.println(allhits.get(i).printInfo());
            }
        }
        return allhits;
    }


    public ArrayList<FTTRKCluster> findClusters(List<FTTRKHit> hits)
    {
        int debugMode=FTTRKReconstruction.debugMode;
    	// cluster finding algorithm
	// the loop is done over sectors 

	int Nlayers = FTTRKConstantsLoader.Nlayers;
	int Nstrips = FTTRKConstantsLoader.Nstrips;
 	boolean[][] checked;
	FTTRKHit[][] HitArray;        
        ArrayList<FTTRKCluster> clusters = new ArrayList<FTTRKCluster>();
        
        // a boolean array to avoid double counting at the numbering discontinuities
        checked = new boolean[Nstrips][Nlayers] ;
        for(int l=0; l<Nlayers; l++){
            for(int s=0; s<Nstrips; s++) {  // init all strips to false
                    checked[s][l]=false;
            }
        }

        // a Hit Array is used to identify clusters
        HitArray = new FTTRKHit[Nstrips][Nlayers] ;

        // initializing non-zero Hit Array entries
        // fill with valid hits
        for(FTTRKHit hit : hits) {
            if(hit.get_Strip()==-1) continue;
            int w = hit.get_Strip();
            int l = hit.get_Layer();

            if(w>0 && w<=Nstrips){						
                    HitArray[w-1][l-1] = hit;
                    if(debugMode>=1) System.out.println(w + " " + l + " " + HitArray[w-1][l-1].printInfo());
            }

        }
        int cid = -1;  // cluster id, will increment with each new good cluster

        // for each layer and sector, loop over the strips:
        // once one is selected for the first time, loop over the remainder
        // clusters are delimited by strips with no hits
        int[] indR = new int[sequentialStrips]; 
        int[] indL = new int[sequentialStrips];
        // strip index: from 0 to 512 (full right hand side, left hand side fired only in the interval 384-639)
        // strip number: from 1 to 768
        for(int i=0; i<sequentialStrips; i++){
            if(i<FTTRKConstantsLoader.Longstrips){
                indR[i] = indL[i] = i;
            }else if(i >=  FTTRKConstantsLoader.Longstrips && i < (FTTRKConstantsLoader.Longstrips + sideShortStrips)){ //384
                indR[i] = i;
                indL[i] = i + sideShortStrips; //256
            }else if(i >= (FTTRKConstantsLoader.Longstrips + sideShortStrips)){ // 384
                indR[i] = indL[i] = i + sideShortStrips; // 256    
            }
        }
            
        boolean needsReordering = false;
        // loop on layers
        for(int il=0; il<Nlayers; il++) {
            int is = 0;
            // first loop on RHS strips (complete, including top and bottom long strips)
            while(is < sequentialStrips){
                int nst = 0;
                int ris = indR[is];
                if(HitArray[ris][il] != null && !checked[ris][il]){
                    ArrayList<FTTRKHit> clusterHits = new ArrayList<FTTRKHit>();
                    checked[ris][il] = true;
                    // if fired this is the first strip of the cluster
                    clusterHits.add(new FTTRKHit(HitArray[ris][il].get_Sector(), HitArray[ris][il].get_Layer(), HitArray[ris][il].get_Strip(), 
                            HitArray[ris][il].get_Edep(), HitArray[ris][il].get_Time(), HitArray[ris][il].get_Id()));
                    // look for a consecutive strip in the stack and stick it to the cluster
                    int isnext = is+1; 
                    while(isnext < sequentialStrips && HitArray[indR[isnext]][il] != null && !checked[indR[isnext]][il]){
                        int nris = indR[isnext];
                        checked[nris][il] = true;
                        clusterHits.add(new FTTRKHit(HitArray[nris][il].get_Sector(), HitArray[nris][il].get_Layer(), HitArray[nris][il].get_Strip(), 
                            HitArray[nris][il].get_Edep(), HitArray[ris][il].get_Time(), HitArray[nris][il].get_Id()));
                        nst++;
                        isnext++;
                    }
                    is = isnext-1;  
                    FTTRKCluster this_cluster = new FTTRKCluster(1, il+1, ++cid);                  
                    // modify clusterIndex for all the hits belonging to the cluster
                    ArrayList<FTTRKHit> cloneHitsWNewID = new ArrayList<FTTRKHit>();
                    for(FTTRKHit clHit: clusterHits){
                        clHit.set_DGTZIndex(clHit.get_Id());
                        clHit.set_ClusterIndex(cid);
                        cloneHitsWNewID.add(clHit);
                    }
                    this_cluster.addAll(cloneHitsWNewID);
                    this_cluster.calc_CentroidParams();
                    clusters.add(this_cluster);
                    
                    if(debugMode>=1){
                        System.out.println("xxxxxxxxxxxxxxx cluster properties " + this_cluster.get_CId() + " cluster size " + this_cluster.size());
                        for(int k=0; k<this_cluster.size(); k++){
                            System.out.println("hit n " + k + " strip " + this_cluster.get(k).get_Strip() + " stripId " + this_cluster.get(k).get_Id());
                        }
                    }
                }
                is++;
            }
            // second loop on LHS strips (only lateral strips)
            is = FTTRKConstantsLoader.Longstrips; 
            while(is < (FTTRKConstantsLoader.Longstrips + sideShortStrips)){  //384
                int nst = 0;
                int lis = indL[is];
                if(HitArray[lis][il] != null && !checked[lis][il]){
                    ArrayList<FTTRKHit> clusterHits = new ArrayList<FTTRKHit>();
                    checked[lis][il] = true;
                    // if fired this is the first strip of the cluster
                    clusterHits.add(new FTTRKHit(HitArray[lis][il].get_Sector(), HitArray[lis][il].get_Layer(), HitArray[lis][il].get_Strip(), 
                            HitArray[lis][il].get_Edep(), HitArray[lis][il].get_Time(), HitArray[lis][il].get_Id()));
                    // look for a consecutive strip in the stack and stick it to the cluster
                    int isnext = is+1; 
                    while(isnext < (sequentialStrips + FTTRKConstantsLoader.SideHalfstrips) &&   // 640 
                            HitArray[indL[isnext]][il] != null && !checked[indL[isnext]][il]){  
                        int nlis = indL[isnext];
                        checked[nlis][il] = true;
                        clusterHits.add(new FTTRKHit(HitArray[nlis][il].get_Sector(), HitArray[nlis][il].get_Layer(), HitArray[nlis][il].get_Strip(), 
                            HitArray[nlis][il].get_Edep(), HitArray[lis][il].get_Time(), HitArray[nlis][il].get_Id()));
                        nst++;
                        isnext++;
                    }
                    is = isnext-1;  
                    FTTRKCluster this_cluster = new FTTRKCluster(1, il+1, ++cid);
                    ArrayList<FTTRKHit> cloneHitsWNewID = new ArrayList<FTTRKHit>();
                    for(FTTRKHit clHit: clusterHits){
                        clHit.set_DGTZIndex(clHit.get_Id());
                        clHit.set_ClusterIndex(cid);
                        cloneHitsWNewID.add(clHit);
                    }
                    this_cluster.addAll(cloneHitsWNewID);                  
                    this_cluster.calc_CentroidParams();
                    clusters.add(this_cluster);
                }
                is++;
            }
            // check if in any cluster there is a limiting strip. If two consecutive limiting strips are found, merge the clusters and
            // delete the second one
            int clusterId11 = -1; 
            int clusterId12 = -1; 
            int clusterId21 = -1; 
            int clusterId22 = -1;
            for(FTTRKCluster clust : clusters){
                if(clust.get_Layer() == il+1){
                    int nbhits = clust.size();
                    int control = clust.get_CId();
                    for(int i=0; i < nbhits; i++){
                        FTTRKHit hit = clust.get(i);
                        int nstrip = hit.get_Strip();
                                                     // 641
                        if(nstrip==stripDiscontinuity1 || nstrip==stripDiscontinuity2 || nstrip==stripDiscontinuity3 || nstrip==stripDiscontinuity4){
                            needsReordering = true;
                            if(nstrip==stripDiscontinuity1) clusterId11 = clust.get_CId();  //cut 127-384
                            if(nstrip==stripDiscontinuity2) clusterId12 = clust.get_CId();
                            if(nstrip==stripDiscontinuity3) clusterId21 = clust.get_CId();  // cut 640-641
                            if(nstrip==stripDiscontinuity4) clusterId22 = clust.get_CId();
                        }   
                    }
                }
            }
            int clustersize = clusters.size();
            // join clusters if there are consecutive limiting hits
            if(clusterId11>=0 && clusterId12>=0 && clusterId11 != clusterId12){
                int newIndex = Math.min(clusterId11, clusterId12);
                ArrayList<FTTRKHit> twoClusterHits = new ArrayList<FTTRKHit>();
                FTTRKCluster firstCluster = clusters.get(clusterId11);
                FTTRKCluster secondCluster = clusters.get(clusterId12);
                for(int i=0; i< firstCluster.size(); i++){
                    FTTRKHit hit = firstCluster.get(i);
                    twoClusterHits.add(hit);
                }
                for(int i=0; i< secondCluster.size(); i++){
                    FTTRKHit hit = secondCluster.get(i);
                    twoClusterHits.add(hit);
                }
                FTTRKCluster joinedClusters = new FTTRKCluster(1, il+1, ++clustersize);
                // update cluster and hit ID
                ArrayList<FTTRKHit> cloneHitsWNewID = new ArrayList<FTTRKHit>();
                for(FTTRKHit clHit: twoClusterHits){
                    clHit.set_DGTZIndex(clHit.get_Id());
                    cloneHitsWNewID.add(clHit);
                }   
                joinedClusters.addAll(cloneHitsWNewID);
                joinedClusters.calc_CentroidParams();
                clusters.add(joinedClusters);
            }   
            if(clusterId21>=0 && clusterId22>=0 && clusterId21 != clusterId22){
                int newIndex = Math.min(clusterId21, clusterId22);
                ArrayList<FTTRKHit> twoClusterHits = new ArrayList<FTTRKHit>();
                FTTRKCluster firstCluster = clusters.get(clusterId21);
                FTTRKCluster secondCluster = clusters.get(clusterId22);
                for(int i=0; i< firstCluster.size(); i++){
                    FTTRKHit hit = firstCluster.get(i);
                    twoClusterHits.add(hit);
                }
                for(int i=0; i< secondCluster.size(); i++){
                    FTTRKHit hit = secondCluster.get(i);
                    twoClusterHits.add(hit);
                }
                FTTRKCluster joinedClusters = new FTTRKCluster(1, il+1, ++clustersize);
                ArrayList<FTTRKHit> cloneHitsWNewID = new ArrayList<FTTRKHit>();
                for(FTTRKHit clHit: twoClusterHits){
                    clHit.set_DGTZIndex(clHit.get_Id());
                    clHit.set_ClusterIndex(newIndex);
                    cloneHitsWNewID.add(clHit);
                }   
                joinedClusters.addAll(cloneHitsWNewID);
                joinedClusters.calc_CentroidParams();
                clusters.add(joinedClusters);
            }
            // remove joined clusters from the final list
            if(clusterId11>=0 && clusterId12>=0) {
                if(clusterId11<clusterId12){
                    clusters.remove(clusterId12);
                    clusters.remove(clusterId11);
                }else{
                    clusters.remove(clusterId11);
                    clusters.remove(clusterId12);
                }
                cid--; 
            }
            if(clusterId21>=0 && clusterId22>=0){
                if(clusterId21<clusterId22){
                    clusters.remove(clusterId22);
                    clusters.remove(clusterId21);
                }else{
                    clusters.remove(clusterId21);
                    clusters.remove(clusterId22);
                }
                cid--;
            }
        }    // end loop on layers
        
        
        // before returning cluster list update centroid and its error, if the cluster list was modified
        if(needsReordering){
            int newClusterID = -1;
            for(FTTRKCluster aCluster : clusters){
                aCluster.calc_CentroidParams();
                aCluster.set_CId(++newClusterID);  // ClusterID relabelling to match the ordinal cluster number
            }
        }
        
        return clusters;
    }
    
    public ArrayList<FTTRKCross> findCrosses(List<FTTRKCluster> clusters) {

        int debugMode=FTTRKReconstruction.debugMode;
        // first separate the segments according to layers
        ArrayList<FTTRKCluster> allinnerlayrclus = new ArrayList<FTTRKCluster>();
        ArrayList<FTTRKCluster> allouterlayrclus = new ArrayList<FTTRKCluster>();

        // Sorting by layer first:
        for (FTTRKCluster theclus : clusters){
            if(theclus.get_Layer()%2==0) { 
                    allouterlayrclus.add(theclus); 
            } 
            if(theclus.get_Layer()%2==1) { 
                    allinnerlayrclus.add(theclus);
            }
        }

        ArrayList<FTTRKCross> crosses = new ArrayList<FTTRKCross>();

        int rid =-1;
        for(FTTRKCluster inlayerclus : allinnerlayrclus){
            if(inlayerclus.size()<FTConstants.TRK_MIN_CLUS_SIZE) continue;
            for(FTTRKCluster outlayerclus : allouterlayrclus){
                if(outlayerclus.size()<FTConstants.TRK_MIN_CLUS_SIZE) continue;
                if(outlayerclus.get_Layer()-inlayerclus.get_Layer()!=1) continue;
                if(outlayerclus.get_Sector()!=inlayerclus.get_Sector()) continue;
                if(debugMode>=1) System.out.println(inlayerclus.printInfo() +  " " + outlayerclus.printInfo());
                if( (inlayerclus.get_MinStrip()+outlayerclus.get_MinStrip() > 0) 
                            && (inlayerclus.get_MaxStrip()+outlayerclus.get_MaxStrip() < FTTRKConstantsLoader.Nstrips*2) ) {
                    // define new cross 
                    FTTRKCross this_cross = new FTTRKCross(inlayerclus.get_Sector(), inlayerclus.get_Region(),++rid);
                    this_cross.set_Cluster1(inlayerclus);
                    this_cross.set_Cluster2(outlayerclus);
                    int dummy = this_cross.get_Cluster1().get_CId();
                    int dummy2 = this_cross.get_Cluster2().get_CId();

                    this_cross.set_CrossParams();
                    //make arraylist and check whether the cross center is in a physical position
                    double radXCenter = Math.sqrt(this_cross.get_Point().x()*this_cross.get_Point().x() + 
                            this_cross.get_Point().y()*this_cross.get_Point().y());
                    if(debugMode>=1) System.out.println("cross radius =============" + radXCenter);
                    if(radXCenter > FTTRKConstantsLoader.InnerHole && radXCenter < FTTRKConstantsLoader.Rmax) crosses.add(this_cross);
                    if(debugMode>=1) System.out.println("cross info :" + this_cross.printInfo() + " " + crosses.size());
                    
                }
            }
        }
        
        return crosses;
    }

        public List<FTTRKHit> readRawHits(DataEvent event, int run) {
        // getting raw data bank
	if(debugMode>=1) System.out.println("Getting raw hits from FTTRK:adc bank");

        List<FTTRKHit>  hits = new ArrayList<FTTRKHit>();
	if(event.hasBank("FTTRK::adc")==true) {
            DataBank bankDGTZ = event.getBank("FTTRK::adc");
            int nrows = bankDGTZ.rows();
            if(nrows>FTConstants.TRK_MAXNUMBEROFHITS) return hits;   // if too many hits skip the event
            int hitId = -1;
            for(int row = 0; row < nrows; row++){
                int isector     = bankDGTZ.getInt("sector",row);
                int ilayer      = bankDGTZ.getInt("layer",row);
                int icomponent  = bankDGTZ.getInt("component",row);
                int iorder      = bankDGTZ.getInt("order",row);
                int adc         = bankDGTZ.getInt("ADC",row);
                float time      = bankDGTZ.getFloat("time", row);
                
                
/////////////////////////////////////////////////////// insert here operations modifying strip number 
/////////////////////////////////////////////////////// IN RECO: STRIP NUMBERS: 0-767, layer numbers: 0-3 
/////////////////////////////////////////////////////// IN FEE: STRIP NUMBERS: 1-768, layer numbers: 1-4
                 
                // read just layer sectors only for real data (no montecarlo)
                // PROVISIONAL: include correct run range for fall18 RGA runs & same FTTRK FEEE configuration
                if(run >100) icomponent = renumberFEE2RECRotatedAndAdjust_Fall18RGA(run, ilayer, icomponent);  

                if(adc>FTConstants.FTTRKMinAdcThreshold && adc<FTConstants.FTTRKMaxAdcThreshold && time!=-1 && icomponent!=-1){
                    FTTRKHit hit = new FTTRKHit(isector, ilayer, icomponent, (double)adc, (double)time, ++hitId);

//////////////////////////////////////////////////////// insert here possible operations selecting strips or groups of strips

                    // select only one sector at a time, for instance
                    // if(icomponent>64 && icomponent<=128) hits.add(hit);
                      
                    boolean isHitAccepted = true;
                    // exclude here some layers or sectors, for instance
                    // if((ilayer==4) && icomponent>=1 && icomponent>64) isHitAccepted = false;
                                    
                    // selection on strip time
                    if(time < FTConstants.TRK_STRIP_MIN_TIME || time > FTConstants.TRK_STRIP_MAX_TIME) isHitAccepted = false;
                    // icomponent = -1: strip is off
                    if(icomponent<0) isHitAccepted = false;
                      
                    if(isHitAccepted) hits.add(hit);                                        
	        }	          
            }
        }
        // order hits list by component
        List<FTTRKHit>  hitsOrderedByLay = new ArrayList<FTTRKHit>();

        // order by layers
        int Nlayers = FTTRKConstantsLoader.Nlayers;
        int Nstrips = FTTRKConstantsLoader.Nstrips;
        for(int nlayer=1; nlayer<=Nlayers; nlayer++){
            for(FTTRKHit h:hits){
                if(h.get_Layer()==nlayer) hitsOrderedByLay.add(h);
            }
        }

        // order by strip
        List<FTTRKHit>  hitsOrderedByStrip = new ArrayList<FTTRKHit>();
        for(int nlayer=1; nlayer<=Nlayers; nlayer++){
            for(int nstrip=1; nstrip<=Nstrips; nstrip++){
                for(FTTRKHit h: hitsOrderedByLay){
                    if(nlayer == h.get_Layer() && nstrip == h.get_Strip()) hitsOrderedByStrip.add(h);
                }
            }   
        }
          
        return hitsOrderedByStrip;
    }    
    
    public void writeBanks(DataEvent event, List<FTTRKHit> hits, List<FTTRKCluster> clusters, List<FTTRKCross> crosses){
        if(event instanceof EvioDataEvent) {
            writeEvioBanks(event, hits, clusters, crosses);
        }
        else if(event instanceof HipoDataEvent) {
            writeHipoBanks(event, hits, clusters, crosses);
        }
    }
        
        
    public void writeHipoBanks(DataEvent event, List<FTTRKHit> hits, List<FTTRKCluster> clusters, List<FTTRKCross> crosses){
        
        // hits banks
        if(hits.size()!=0) {
            DataBank bankHits = event.createBank("FTTRK::hits", hits.size());    
            if(bankHits==null){
                System.out.println("ERROR CREATING BANK : FTTRK::hits");
                return;
            }
            for(int i = 0; i < hits.size(); i++){
                bankHits.setByte("sector",i,(byte) hits.get(i).get_Sector());
                bankHits.setByte("layer",i,(byte) hits.get(i).get_Layer());
                bankHits.setShort("component",i,(short) hits.get(i).get_Strip());
//                bankHits.setFloat("x",i,(float) (hits.get(i).get_Dx()/10.0));
//                bankHits.setFloat("y",i,(float) (hits.get(i).get_Dy()/10.0));
//                bankHits.setFloat("z",i,(float) (hits.get(i).get_Dz()/10.0));
                bankHits.setFloat("energy",i,(float) hits.get(i).get_Edep());
                bankHits.setFloat("time",i,(float) hits.get(i).get_Time());
                bankHits.setShort("hitID",i,(short) hits.get(i).get_DGTZIndex());
                bankHits.setShort("clusterID",i,(short) hits.get(i).get_ClusterIndex());				
            }
            event.appendBanks(bankHits);
        }
        // cluster bank
        if(debugMode>=1) System.out.println("cluster bank size " + clusters.size());
        if(clusters.size()!=0){
            DataBank bankCluster = event.createBank("FTTRK::clusters", clusters.size());    
            if(bankCluster==null){
                System.err.println("ERROR CREATING BANK : FTTRK::clusters");
                return;
            }
            for(int i = 0; i < clusters.size(); i++){
                bankCluster.setShort("size",      i,(short) clusters.get(i).size());
                bankCluster.setShort("id",        i,(short) clusters.get(i).get_CId());
                bankCluster.setByte("sector",     i,(byte)  clusters.get(i).get_Sector());
                bankCluster.setByte("layer",      i,(byte)  clusters.get(i).get_Layer());
                bankCluster.setFloat("energy",    i,(float) clusters.get(i).get_TotalEnergy());
                bankCluster.setFloat("maxEnergy", i,(float) clusters.get(i).get_SeedEnergy());
                bankCluster.setShort("seed",      i,(short) clusters.get(i).get_SeedStrip());
                bankCluster.setFloat("centroid",  i,(float) clusters.get(i).get_Centroid());
            }
            event.appendBanks(bankCluster);
        }
        // cross bank
        if(debugMode>=1) System.out.println("crosses bank size " + crosses.size());
        if(crosses.size()!=0){        
            DataBank bankCross = event.createBank("FTTRK::crosses", crosses.size());
            if(bankCross==null){
                System.err.println("ERROR CREATING BANK : FTTRK::crosses");
                return;
            }        
            for (int j = 0; j < crosses.size(); j++){     
                bankCross.setShort("size",       j, (short) crosses.size());
                bankCross.setShort("id",         j, (short) crosses.get(j).get_trkId());
                bankCross.setByte("sector",      j, (byte)  crosses.get(j).get_Sector());
                bankCross.setByte("detector",    j, (byte)  (crosses.get(j).get_Region()-1));  // detector: 0 or 1, region 1 or 2
                bankCross.setFloat("x",          j, (float) crosses.get(j).get_Point().x());
                bankCross.setFloat("y",          j, (float) crosses.get(j).get_Point().y());
                bankCross.setFloat("z",          j, (float) crosses.get(j).get_Point().z());
                if(debugMode>=1) System.out.println("energy and time to be stored in banks " + crosses.get(j).get_Energy() + " " + crosses.get(j).get_Time());              
                bankCross.setFloat("energy", j, (float) crosses.get(j).get_Energy());
                bankCross.setFloat("time", j, (float) crosses.get(j).get_Time());
                bankCross.setShort("Cluster1ID", j, (short) crosses.get(j).get_Cluster1().get_CId());
                bankCross.setShort("Cluster2ID", j, (short) crosses.get(j).get_Cluster2().get_CId());
            }
            event.appendBanks(bankCross);
        }
    }
  

    
 public void writeEvioBanks(DataEvent event, List<FTTRKHit> hits, List<FTTRKCluster> clusters, List<FTTRKCross> crosses){
        
        EvioDataBank bankHits  = null;
        EvioDataBank bankCluster = null;
        EvioDataBank bankCross = null;
        
        // hits banks
        if(hits.size()!=0) {
            bankHits = (EvioDataBank) event.getDictionary().createBank("FTTRK::hits", hits.size());    
            if(bankHits==null){
                System.err.println("ERROR CREATING BANK : FTTRK::hits");
                return;
            }
            
            for(int i = 0; i < hits.size(); i++){
                bankHits.setByte("sector",i,(byte) hits.get(i).get_Sector());
                bankHits.setByte("layer",i,(byte) hits.get(i).get_Layer());
                bankHits.setShort("component",i,(short) hits.get(i).get_Strip());
//                bankHits.setFloat("x",i,(float) (hits.get(i).get_Dx()/10.0));
//                bankHits.setFloat("y",i,(float) (hits.get(i).get_Dy()/10.0));
//                bankHits.setFloat("z",i,(float) (hits.get(i).get_Dz()/10.0));
                bankHits.setFloat("energy",i,(float) hits.get(i).get_Edep());
                bankHits.setFloat("time",i,(float) hits.get(i).get_Time());
                bankHits.setShort("hitID",i,(short) hits.get(i).get_DGTZIndex());
                bankHits.setShort("clusterID",i,(short) hits.get(i).get_ClusterIndex());				
            }
            event.appendBanks(bankHits);
        }
        // cluster bank
        if(debugMode>=1) System.out.println("cluster bank size " + clusters.size());
        if(clusters.size()!=0){
            bankCluster = (EvioDataBank) event.getDictionary().createBank("FTTRK::clusters", clusters.size());    
            if(bankCluster==null){
                System.err.println("ERROR CREATING BANK : FTTRK::clusters");
                return;
            }
            for(int i = 0; i < clusters.size(); i++){
                bankCluster.setShort("size",      i,(short) clusters.get(i).size());
                bankCluster.setShort("id",        i,(short) clusters.get(i).get_CId());
                bankCluster.setByte("sector",     i,(byte)  clusters.get(i).get_Sector());
                bankCluster.setByte("layer",      i,(byte)  clusters.get(i).get_Layer());
                bankCluster.setFloat("energy",    i,(float) clusters.get(i).get_TotalEnergy());
                bankCluster.setFloat("maxEnergy", i,(float) clusters.get(i).get_SeedEnergy());
                bankCluster.setShort("seed",      i,(short) clusters.get(i).get_SeedStrip());
                bankCluster.setFloat("centroid",  i,(float) clusters.get(i).get_Centroid());
            }
            event.appendBanks(bankCluster);
        }
        // cross bank
        if(debugMode>=1) System.out.println("crosses bank size " + crosses.size());
        if(crosses.size()!=0){        
            bankCross = (EvioDataBank) event.getDictionary().createBank("FTTRK::crosses", crosses.size());
            if(bankCross==null){
                System.err.println("ERROR CREATING BANK : FTTRK::crosses");
                return;
            }        
            for (int j = 0; j < crosses.size(); j++) {
                bankCross.setShort("size",       j, (short) crosses.size());
                bankCross.setShort("id",         j, (short) crosses.get(j).get_Id());
                bankCross.setByte("sector",      j, (byte)  crosses.get(j).get_Sector());
                bankCross.setByte("detector",    j, (byte)  (crosses.get(j).get_Region()-1));  // detector: 0 or 1, region 1 or 2
                bankCross.setFloat("x",          j, (float) crosses.get(j).get_Point().x());
                bankCross.setFloat("y",          j, (float) crosses.get(j).get_Point().y());
                bankCross.setFloat("z",          j, (float) crosses.get(j).get_Point().z());
                bankCross.setFloat("energy",     j, (float) crosses.get(j).get_Energy());
                bankCross.setFloat("time",       j, (float) crosses.get(j).get_Time());
                bankCross.setShort("Cluster1ID", j, (short) crosses.get(j).get_Cluster1().get_CId());
                bankCross.setShort("Cluster2ID", j, (short) crosses.get(j).get_Cluster2().get_CId());
            }
            event.appendBanks(bankCross);
        }
 }
    

 public void updateAllHitsWithAssociatedIDs(List<FTTRKHit> hits, List<FTTRKCluster> clusters){
    // update clusterIndex and crossIndex for hits belonging to a cross
    for(FTTRKCluster aCluster: clusters){
        for(FTTRKHit ahitInCluster: aCluster){
            for(FTTRKHit aHit: hits){     
               if(aHit.get_Id()==ahitInCluster.get_Id()){
                  aHit.set_DGTZIndex(ahitInCluster.get_DGTZIndex());
                  aHit.set_ClusterIndex(ahitInCluster.get_ClusterIndex());
                  aHit.set_CrossIndex(ahitInCluster.get_CrossIndex());
               }
            }
        }
    }
 }
 
  
 public int flipStripVertical(int ilayer, int icomponent){
    // flip the layer vertically with respect to y axis (left/right flip)
    // strips numbered 1-768
    if(ilayer==DetectorLayer.FTTRK_LAYER2 || ilayer==DetectorLayer.FTTRK_LAYER3){  // RECO vertical strips        
        if(icomponent>=(stripDiscontinuity1+1) && icomponent<=(stripDiscontinuity2-1)){   // [129,384]
            icomponent += sideShortStrips;  // 256
        }else if(icomponent>=stripDiscontinuity2 && icomponent<=stripDiscontinuity3){ // [385,640]
            icomponent -= sideShortStrips;  // 256
        }
    }else if(ilayer==DetectorLayer.FTTRK_LAYER1 || ilayer==DetectorLayer.FTTRK_LAYER4){  // RECO horizontal strips
        if(icomponent>=129 && icomponent<=384){ // [129,384]
            icomponent = 513 - icomponent;   // 513
        }else if(icomponent>=stripDiscontinuity2 && icomponent<=stripDiscontinuity3){ // [385,640]
            icomponent = 2*sequentialStrips+1 - icomponent;     // 1025
        }else{ // for horizontal strips flip also long strips
            icomponent = FTTRKConstantsLoader.Nstrips+1 - icomponent;      // 769
        }
    }
    return icomponent;
 }
 
 public int flipStripHorizontal(int ilayer, int icomponent){
    // flip the layer horizontally with respect to the x axis (top/bottom flip)
    // strips 1-128
    if(ilayer==DetectorLayer.FTTRK_LAYER1 || ilayer==DetectorLayer.FTTRK_LAYER4){  // RECO vertical strips        
        if(icomponent>=(stripDiscontinuity1+1) && icomponent<=(stripDiscontinuity2-1)){   // [129, 384]
            icomponent += sideShortStrips;                    // 256
        }else if(icomponent>=stripDiscontinuity2 && icomponent<=stripDiscontinuity3){   // [385,640]
            icomponent -= sideShortStrips;                    // 256
        }
    }else if(ilayer==DetectorLayer.FTTRK_LAYER2 || ilayer==DetectorLayer.FTTRK_LAYER3){ // RECO horizontal strips
        if(icomponent>=(stripDiscontinuity1+1) && icomponent<=(stripDiscontinuity2-1)){   // [129,384]
            icomponent = sequentialStrips+1 - icomponent;        // 513
        }else if(icomponent>=stripDiscontinuity2 && icomponent<=stripDiscontinuity3){   // [385,640]
            icomponent = 2*sequentialStrips+1 - icomponent;     // 1025
        }else{
            icomponent = FTTRKConstantsLoader.Nstrips+1 - icomponent;      // 769
        }
    }
    
    return icomponent;
 }
 
public int overturnModule(int ilayer, int icomponent){
     // rotate of 180 degrees: this incluse a flip with respect to the x axis followed by a flip with respect to y 
     int icompNew = flipStripHorizontal(ilayer, icomponent);
     icompNew = flipStripVertical(ilayer, icompNew); 
     return icompNew;
 }
  
public int reverseStripsInSector(int icomponent){
    // reverse the number of strips inside a sector (strips are numbered 1-768, the [i] component of sectorLimit vector is
    // excluded in the sector, [i+1]is included
    // 20 sectors of 32 strips each, except long strips sectors (0, 1, 18, 19) which have 64 strips
    int nsector = findSector(icomponent);
    if(nsector>NStripsSectors) System.out.println("wrong sector number, check code");
    int offset = sectorLimits[nsector+1] - icomponent;
    icomponent = sectorLimits[nsector]+1 + offset;
      
    return icomponent; 
 } 

public int reverseStripInFirstHalf(int icomponent){
    // reverse the number of strips inside a sector (strips are numbered 1-768, the [i] component of sectorLimit vector is
    // excluded in the sector, [i+1]is included
    // 20 sectors of 32 strips each, except long strips sectors (0, 1, 18, 19) which have 64 strips
    int nsector = findSector(icomponent);
    if(nsector>NStripsSectors) System.out.println("wrong sector number, check code");
    int halfStrip = (sectorLimits[nsector+1]-sectorLimits[nsector])/2 + sectorLimits[nsector];
    if(icomponent <= halfStrip){
        int offset = halfStrip - icomponent;
        icomponent = sectorLimits[nsector]+1 + offset;
    }
    return icomponent; 
 }


public int reverseStripInSecondHalf(int icomponent){
    // reverse the number of strips inside a sector (strips are numbered 1-768, the [i] component of sectorLimit vector is
    // excluded in the sector, [i+1]is included
    // 20 sectors of 32 strips each, except long strips sectors (0, 1, 18, 19) which have 64 strips
    int nsector = findSector(icomponent);
    if(nsector>NStripsSectors) System.err.println("wrong FTTRK strip sector number, check code");
    int halfStrip = (sectorLimits[nsector+1]-sectorLimits[nsector])/2 + sectorLimits[nsector];
    if(icomponent > halfStrip){
        int offset = sectorLimits[nsector+1] - icomponent;
        icomponent = halfStrip + offset;
    }  
    return icomponent; 
 } 

public int swapHalves(int icomponent){
    // swap half the module to the opposite half, rigid translation
    // the strips are numbered 1-768, the [i] component of sectorLimit vector is
    // excluded in the sector, [i+1]is included
    // 20 sectors of 32 strips each, except long strips sectors (0, 1, 18, 19) which have 64 strips
    int nsector = findSector(icomponent);
    if(nsector>NStripsSectors) System.err.println("wrong FTTRK strip sector number, check code");
    int halfWid = (sectorLimits[nsector+1]-sectorLimits[nsector])/2;
    int halfStrip =  halfWid + sectorLimits[nsector];
    if(icomponent >= halfStrip+1){
        icomponent -= halfWid;
    }else{
        icomponent += (halfWid-1);
    }
    return icomponent; 
 }

public int switchStripOff(){return -1;}


public int swapSectors(int icomponent, int nsector2){
    // get the new strip number of the icomponent strip in nsector1 once the sector is swapped to nsector2
    // icomponent strips are numbered 1-768 so the vector sectorLimits contains the number of the starting 
    // strip of a sector: [i] is excluded from sector i, [i+1] is included
    // 20 sectors of 32 strips each, except long strips sectors (0, 1, 18, 19) which have 64 strips
    int nsector1 = findSector(icomponent);
    int offset = -sectorLimits[nsector1] + icomponent;
    int newicomp = sectorLimits[nsector2] + offset;
           
    return newicomp;
}


public static int findSector(int icomponent){
    // returns the sector number, corresponding to the component of the lower extreme of the interval
    // sectors are numbered 0-20; icomponent strips are numbered 1-768 so the vector sectorLimits contains
    // the number of the starting strip of a sector: [i] is excluded from sector i, [i+1] is included
    // 20 sectors of 32 strips each, except long strips sectors (0, 1, 18, 19) which have 64 strips
    int nsector = -1;
    for(int i=0; i<NStripsSectors; i++){
        if(icomponent>sectorLimits[i] && icomponent<=sectorLimits[i+1]){
            nsector = i;
            break;
        } 
    }
    if(nsector>NStripsSectors) System.err.println("wrong FTTRK strip sector number, check code");
    return nsector;
}

public boolean isInSector(int iSector, int icomponent){
    // returns ture is the strip icomponent is in the iSector sector
    // 20 sectors of 32 strips each, except long strips sectors (0, 1, 18, 19) which have 64 strips
    if(icomponent>sectorLimits[iSector] && icomponent<=sectorLimits[iSector+1]){
        return true;
    }else{
        return false;
    }
} 
    

public int renumberStrip(int ilayer, int icomponent){
    // renumber strips from FEE number (to RECO numbering
    // strips numbering 1-768
    int newStripNumber = -1;
    if(ilayer==DetectorLayer.FTTRK_LAYER1){
        if((icomponent>=1 && icomponent <=stripDiscontinuity1) || (icomponent>=641 && icomponent<=768)){    //[1,128], [641,768]
            newStripNumber = icomponent;
        }else if(icomponent>=(stripDiscontinuity1+1) && icomponent<=sideShortStrips){   // [129, 256]
            newStripNumber = icomponent+sideShortStrips;            // 256
        }else if(icomponent>=(sideShortStrips+1) && icomponent<=sequentialStrips){   // [257, 512] 
            newStripNumber = icomponent-FTTRKConstantsLoader.Longstrips;            // 128
        }else if(icomponent>=(sequentialStrips+1) && icomponent<=stripDiscontinuity3){   // [513, 640]
            newStripNumber = icomponent;
        }
    }else if(ilayer==DetectorLayer.FTTRK_LAYER2 || ilayer==DetectorLayer.FTTRK_LAYER4 || ilayer==DetectorLayer.FTTRK_LAYER3){
        if(icomponent>=(sideShortStrips+1) && icomponent <=sequentialStrips){        // [257, 512]
            newStripNumber = icomponent+FTTRKConstantsLoader.Longstrips;            // 128
        }else if(icomponent>=(sequentialStrips+1) && icomponent <=stripDiscontinuity3){  // [513, 640]
            newStripNumber = icomponent-sideShortStrips;            // 256
        }else{  
            newStripNumber = icomponent;
        }
    }
    return (newStripNumber);
}

public int renumberFEE2REC(int run, int ilayer, int icomponent){
//  apply strip renumbering  only
    if(run>10) icomponent = renumberStrip(ilayer, icomponent); 
    return icomponent;     
}


public int renumberFEE2RECRotatedAndAdjust_Fall18RGA(int run, int ilayer, int icomponent){
//  apply the renumbering schema - method 2 
    if(run>0){   
        icomponent = renumberStrip(ilayer, icomponent);
        // overturn layer 1+4
        if(ilayer==DetectorLayer.FTTRK_LAYER1 || ilayer==DetectorLayer.FTTRK_LAYER4) icomponent = overturnModule(ilayer, icomponent);
        
        int isec1 = -1;
        if(ilayer==DetectorLayer.FTTRK_LAYER1){
            isec1 = findSector(icomponent);
            if(isec1 == 1){
                icomponent = reverseStripInSecondHalf(icomponent);
            }else if(isec1==6){
                icomponent = swapSectors(icomponent, 7);
                icomponent = reverseStripsInSector(icomponent); 
            }else if(isec1==7){
                icomponent = swapSectors(icomponent, 6);
                icomponent = reverseStripsInSector(icomponent);    
            }else if(isec1==13){
                icomponent = reverseStripsInSector(icomponent);
            }else if(isec1==14){
                icomponent = reverseStripsInSector(icomponent);
            }else if(isec1==18){
                icomponent = reverseStripInFirstHalf(icomponent); 
            }   
            
        }else if(ilayer==DetectorLayer.FTTRK_LAYER2){
            isec1 = findSector(icomponent);
            if(isec1==1){
                icomponent = reverseStripsInSector(icomponent);
                if(icomponent>(stripsInLongSector+stripsInShortSector)/2) icomponent -= 8;        // 96
            }else if(isec1==3){
                icomponent = reverseStripsInSector(icomponent);
            }
        
        }else if(ilayer==DetectorLayer.FTTRK_LAYER3){     
            isec1 = findSector(icomponent);
            if(isec1==11){
                icomponent = swapSectors(icomponent, 10);
            }else if(isec1==10){
                icomponent = swapSectors(icomponent, 11);
            }
            // reverse sectors 10-11 + flip horizontal
            int newsec = findSector(icomponent);
            if(newsec==11 || newsec==10) icomponent = reverseStripsInSector(icomponent);
            icomponent = flipStripHorizontal(ilayer, icomponent);
            
            isec1 = findSector(icomponent);

            if(isec1==8){
                icomponent = reverseStripsInSector(icomponent);  
            }else if(isec1==14){
                icomponent = swapSectors(icomponent, 15);
                icomponent = reverseStripsInSector(icomponent);
            }else if(isec1==15){
                icomponent = swapSectors(icomponent, 14);
                icomponent = reverseStripsInSector(icomponent);
            }
        
        }else if(ilayer==DetectorLayer.FTTRK_LAYER4){            
            isec1 = findSector(icomponent);
            if(isec1 == 1){
                icomponent = reverseStripInSecondHalf(icomponent);
            }else if(isec1 == 5){
                icomponent = reverseStripsInSector(icomponent);
            }else if(isec1 == 6){
                icomponent = reverseStripsInSector(icomponent);
            }else if(isec1==18){
                icomponent = swapHalves(icomponent);
                icomponent = reverseStripInSecondHalf(icomponent);
            }
        }
    }

    return icomponent;     
}


}



