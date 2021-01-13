/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.ft.trk;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.rec.ft.FTConstants;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author devita
 * @author filippi
 */
public class FTTRKReconstruction {

    public static int debugMode = 0;  // 1 for verbose, set it here (better be set in the steering Engine)

    public FTTRKReconstruction() {
    }
    public List<FTTRKHit> initFTTRK(DataEvent event, ConstantsManager manager, int run) {

        IndexedTable charge2Energy = manager.getConstants(run, "/calibration/ft/fthodo/charge_to_energy");
        IndexedTable timeOffsets   = manager.getConstants(run, "/calibration/ft/fthodo/time_offsets");
        IndexedTable geometry      = manager.getConstants(run, "/geometry/ft/fthodo");
        
        if(debugMode>=1) System.out.println("\nAnalyzing new event");
        List<FTTRKHit> allhits = null;
        
        allhits = this.readRawHits(event,charge2Energy,timeOffsets,geometry);
        
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
        
        int[] indR = new int[512]; 
        int[] indL = new int[512];
        // strip index: from 0 to 512 (full right hand side, left hand side fired only in the interval 384-639)
        // strip number: from 1 to 768
        for(int i=0; i<512; i++){
            if(i<128){
                indR[i] = indL[i] = i;
            }else if(i >= 128 && i < 384){
                indR[i] = i;
                indL[i] = i+256;
            }else if(i >= 384){
                indR[i] = indL[i] = i+256;    
            }
        }
            
        // loop on layers
//        for(int il=0; il<2; il++){
        for(int il=0; il<Nlayers; il++) {
            int is = 0;
        // first loop on RHS strips (complete, including top and bottom long strips)
            while(is < 512){
                int nst = 0;
                int ris = indR[is];
                if(HitArray[ris][il] != null && !checked[ris][il]){
                    ArrayList<FTTRKHit> clusterHits = new ArrayList<FTTRKHit>();
                    checked[ris][il] = true;
                    // if fired this is the first strip of the cluster
                    clusterHits.add(new FTTRKHit(HitArray[ris][il].get_Sector(), HitArray[ris][il].get_Layer(), HitArray[ris][il].get_Strip(), 
                            HitArray[ris][il].get_Edep(), HitArray[ris][il].get_Id()));
//                    System.out.println(" clusterHits iteration " + is + " strip " + ris + " " + HitArray[ris][il].get_Strip());
                    // look for a consecutive strip in the stack and stick it to the cluster
                    int isnext = is+1; 
                    while(isnext < 512 && HitArray[indR[isnext]][il] != null && !checked[indR[isnext]][il]){
                        int nris = indR[isnext];
                        checked[nris][il] = true;
                        clusterHits.add(new FTTRKHit(HitArray[nris][il].get_Sector(), HitArray[nris][il].get_Layer(), HitArray[nris][il].get_Strip(), 
                            HitArray[nris][il].get_Edep(), HitArray[nris][il].get_Id()));
//                        System.out.println(" clusterHits iteration " + isnext + " strip " + nris + " " + HitArray[nris][il].get_Strip());
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
            is = 128; //384
            while(is < 384){  //639
                int nst = 0;
                int lis = indL[is];
                if(HitArray[lis][il] != null && !checked[lis][il]){
                    ArrayList<FTTRKHit> clusterHits = new ArrayList<FTTRKHit>();
                    checked[lis][il] = true;
                    // if fired this is the first strip of the cluster
                    clusterHits.add(new FTTRKHit(HitArray[lis][il].get_Sector(), HitArray[lis][il].get_Layer(), HitArray[lis][il].get_Strip(), 
                            HitArray[lis][il].get_Edep(), HitArray[lis][il].get_Id()));
                    // look for a consecutive strip in the stack and stick it to the cluster
                    int isnext = is+1; 
                    while(isnext < 640 && HitArray[indL[isnext]][il] != null && !checked[indL[isnext]][il]){
                        int nlis = indL[isnext];
                        checked[nlis][il] = true;
                        clusterHits.add(new FTTRKHit(HitArray[nlis][il].get_Sector(), HitArray[nlis][il].get_Layer(), HitArray[nlis][il].get_Strip(), 
                            HitArray[nlis][il].get_Edep(), HitArray[nlis][il].get_Id()));
                        nst++;
                        isnext++;
                    }
                    is = isnext-1;  
                    FTTRKCluster this_cluster = new FTTRKCluster(1, il+1, ++cid);
//                    this_cluster.addAll(clusterHits);
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
                    for(int i=0; i < nbhits; i++){
                        FTTRKHit hit = clust.get(i);
                        int nstrip = hit.get_Strip();
//                        System.out.println("strip " + nstrip + " clusterId " + clust.get_CId() + " layer " + clust.get_Layer());
                        if(nstrip==128 || nstrip==385 || nstrip==640 || nstrip==641){
                            if(nstrip==128) clusterId11 = clust.get_CId();  //cut 127-384
                            if(nstrip==385) clusterId12 = clust.get_CId();
                            if(nstrip==640) clusterId21 = clust.get_CId(); // cut 640-641
                            if(nstrip==641) clusterId22 = clust.get_CId();
                        }   
                    }
                }
            }
        // join clusters if there are consecutive limiting hits
            if(clusterId11>=0 && clusterId12>=0 && clusterId11 != clusterId12){
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
                //FTTRKCluster joinedClusters = new FTTRKCluster(1, il+1, clusters.size()+1);
                FTTRKCluster joinedClusters = new FTTRKCluster(1, il+1, clusters.size()-2);
                // update cluster and hit ID
                ArrayList<FTTRKHit> cloneHitsWNewID = new ArrayList<FTTRKHit>();
                for(FTTRKHit clHit: twoClusterHits){
                        clHit.set_DGTZIndex(clHit.get_Id());
                        clHit.set_ClusterIndex(clusters.size()-2);  // was +1
                        cloneHitsWNewID.add(clHit);
                }   
                joinedClusters.addAll(cloneHitsWNewID);
//                joinedClusters.addAll(twoClusterHits);
                joinedClusters.calc_CentroidParams();
                clusters.add(joinedClusters);
            }   
            if(clusterId21>=0 && clusterId22>=0 && clusterId21 != clusterId22){
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
                FTTRKCluster joinedClusters = new FTTRKCluster(1, il+1, clusters.size()-2);
//                joinedClusters.addAll(twoClusterHits);
                // update cluster and hit ID
                ArrayList<FTTRKHit> cloneHitsWNewID = new ArrayList<FTTRKHit>();
                for(FTTRKHit clHit: twoClusterHits){
                        clHit.set_DGTZIndex(clHit.get_Id());
                        clHit.set_ClusterIndex(clusters.size()-2);
                        cloneHitsWNewID.add(clHit);
                }   
                joinedClusters.addAll(cloneHitsWNewID);
                joinedClusters.calc_CentroidParams();
                clusters.add(joinedClusters);
            }
        // remove second clusters from the final list
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
            if(inlayerclus.size()<FTConstants.TRK_MIN_CLUS_SIZE){continue;}
            for(FTTRKCluster outlayerclus : allouterlayrclus){
                if(outlayerclus.size()<FTConstants.TRK_MIN_CLUS_SIZE){continue;}
                if(outlayerclus.get_Layer()-inlayerclus.get_Layer()!=1)
                        continue;
                if(outlayerclus.get_Sector()!=inlayerclus.get_Sector())
                        continue;
                if(debugMode>=1) System.out.println(inlayerclus.printInfo() +  " " + outlayerclus.printInfo());
                if( (inlayerclus.get_MinStrip()+outlayerclus.get_MinStrip() > 1) 
                            && (inlayerclus.get_MaxStrip()+outlayerclus.get_MaxStrip() < FTTRKConstantsLoader.Nstrips*2) ) { // put correct numbers to make sure the intersection is valid

                    // define new cross 
                    FTTRKCross this_cross = new FTTRKCross(inlayerclus.get_Sector(), inlayerclus.get_Region(),++rid);
                    this_cross.set_Cluster1(inlayerclus);
                    this_cross.set_Cluster2(outlayerclus);

                    this_cross.set_CrossParams();
                    //make arraylist
                    // check if the cross center is in a physical position
                    double radXCenter = Math.sqrt(this_cross.get_Point().x()*this_cross.get_Point().x() + 
                            this_cross.get_Point().y()*this_cross.get_Point().y());
                    if(debugMode>=1) System.out.println("cross radius =============" + radXCenter);
                    if(radXCenter > FTTRKConstantsLoader.InnerHole && radXCenter < FTTRKConstantsLoader.Rmax) crosses.add(this_cross);
                    if(debugMode>=1) System.out.println("cross info :" + this_cross.printInfo() + " " + crosses.size());
                    
                }
            }
        }
        /*
        // validate crosses in two detectors: their distance must be within a reasonable tolerance 
        ArrayList<FTTRKCross> validatedCrosses = new ArrayList<FTTRKCross>();
        for(int ic=0; ic<crosses.size(); ic++){
            for(int jc=crosses.size()-1; jc>ic; jc--){
                FTTRKCross cross1 = crosses.get(ic);
                FTTRKCross cross2 = crosses.get(jc);
                if(cross1.get_Id()!=cross2.get_Id() && cross1.get_Region()!=cross2.get_Region()){
                    double rad1 = Math.sqrt(cross1.get_Point().x()*cross1.get_Point().x() + 
                            cross1.get_Point().y()*cross1.get_Point().y());
                    double rad2 = Math.sqrt(cross2.get_Point().x()*cross2.get_Point().x() + 
                            cross2.get_Point().y()*cross2.get_Point().y());
                    double diffPhi = Math.abs(Math.atan2(cross1.get_Point().y(),cross1.get_Point().x()) - 
                            Math.atan2(cross2.get_Point().y(),cross2.get_Point().x()));
                    double diffTheta = Math.abs(Math.atan2(rad1,cross1.get_Point().z())- Math.atan2(rad2,cross2.get_Point().z()));
                    double phiTolerance = 0.01;
                    double thetaTolerance = 0.01;
                    if(Math.abs(rad2-rad1)< FTConstants.TOLERANCE_ON_CROSSES_TWO_DETECTORS && diffPhi<phiTolerance && diffTheta<thetaTolerance){
                        validatedCrosses.add(cross1);
                        validatedCrosses.add(cross2);
                    } 
                }
            }
        }
        */
        
        // validate crosses: for every detector choose the cross with larger deposited energy: only two crosses are saved
        ArrayList<FTTRKCross> validatedCrosses = new ArrayList<FTTRKCross>();
        double maxEn1 =-100.;
        double maxEn2 =-100.;
        int idMax1=-1, idMax2=-1;
        for(int ic=0; ic<crosses.size(); ic++){
            FTTRKCross aCross = crosses.get((ic));
            double crossEnergy = Math.sqrt(aCross.get_Cluster1().get_TotalEnergy() * aCross.get_Cluster2().get_TotalEnergy());
            if(aCross.get_Region()==1){   
                if(crossEnergy>maxEn1){
                    maxEn1 = crossEnergy;
                    idMax1 = ic;
                }
            }else if(aCross.get_Region()==2){
                if(crossEnergy>maxEn2){
                    maxEn2 = crossEnergy;
                    idMax2 = ic;
                }
            }
        }
        if(idMax1>=0) validatedCrosses.add(crosses.get(idMax1));
        if(idMax2>=0) validatedCrosses.add(crosses.get(idMax2)); 
        
        return validatedCrosses;
        //return crosses;
    }

        public List<FTTRKHit> readRawHits(DataEvent event, IndexedTable charge2Energy, IndexedTable timeOffsets, IndexedTable geometry) {
        // getting raw data bank
	if(debugMode>=1) System.out.println("Getting raw hits from FTTRK:adc bank");

        List<FTTRKHit>  hits = new ArrayList<FTTRKHit>();
	if(event.hasBank("FTTRK::adc")==true) {
            DataBank bankDGTZ = event.getBank("FTTRK::adc");
            int nrows = bankDGTZ.rows();
            int hitId = -1;
            for(int row = 0; row < nrows; row++){
                int isector     = bankDGTZ.getInt("sector",row);
                int ilayer      = bankDGTZ.getInt("layer",row);
                int icomponent  = bankDGTZ.getInt("component",row);
                int iorder      = bankDGTZ.getInt("order",row);
                int adc         = bankDGTZ.getInt("ADC",row);
                float time      = bankDGTZ.getLong("timestamp",row); // entry not avalable in mc banks yet
                // set threshold on FTTRK ADCs (in FTConstants.java
                if(adc>FTConstants.FTTRKAdcThreshold && time!=-1 && icomponent!=-1){
//                    System.out.println("~~~~~~~~~~ adc dell'hit accettato " + adc + " component " + icomponent + " layer " + ilayer);
                    FTTRKHit hit = new FTTRKHit(isector,ilayer,icomponent, (double) adc, ++hitId);
	            hits.add(hit); 
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
                System.out.println("ERROR CREATING BANK : FTTRK::clusters");
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
                System.out.println("ERROR CREATING BANK : FTTRK::crosses");
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
            bankCluster = (EvioDataBank) event.getDictionary().createBank("FTTRK::clusters", clusters.size());    
            if(bankCluster==null){
                System.out.println("ERROR CREATING BANK : FTTRK::clusters");
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
                System.out.println("ERROR CREATING BANK : FTTRK::crosses");
                return;
            }        
            for (int j = 0; j < crosses.size(); j++) {
//                bankCross.setShort("size",       j, (short) crosses.get(j).size());
                bankCross.setShort("size",       j, (short) crosses.size());
                bankCross.setShort("id",         j, (short) crosses.get(j).get_Id());
                bankCross.setByte("sector",      j, (byte)  crosses.get(j).get_Sector());
                bankCross.setByte("detector",    j, (byte)  (crosses.get(j).get_Region()-1));  // detector: 0 or 1, region 1 or 2
                bankCross.setFloat("x",          j, (float) crosses.get(j).get_Point().x());
                bankCross.setFloat("y",          j, (float) crosses.get(j).get_Point().y());
                bankCross.setFloat("z",          j, (float) crosses.get(j).get_Point().z());
                bankCross.setShort("Cluster1ID", j, (short) crosses.get(j).get_Cluster1().get_CId());
                bankCross.setShort("Cluster2ID", j, (short) crosses.get(j).get_Cluster2().get_CId());
            }
            event.appendBanks(bankCross);
        }
 }
    

 public void updateAllHitsWithAssociatedIDs(List<FTTRKHit> hits, List<FTTRKCluster> clusters){
    // update clusterIndex and crossIndex for hits belonging to a cross
    for(FTTRKCluster aCluster: clusters){
        int i=-1;
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
 
 
}
