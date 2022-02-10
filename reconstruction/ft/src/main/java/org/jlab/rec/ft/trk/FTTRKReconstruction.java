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
import org.jlab.geom.prim.Point3D;
import org.jlab.rec.ft.FTEBEngine;

/**
 *
 * @author devita
 * @author filippi
 */


public class FTTRKReconstruction {

    public static int debugMode = 0;  // 1 for verbose, set it here (better be set in the steering Engine)
    ////////////////////// provisional
    public static float[] crEnergy;
    public static float[] crTime;
    //////////////////////////////////
    
    public FTTRKReconstruction() {
    }
    public List<FTTRKHit> initFTTRK(DataEvent event, ConstantsManager manager, int run) {

        /*  tables are not needed for FTTRK reco at this point
        IndexedTable charge2Energy = manager.getConstants(run, "/calibration/ft/fthodo/charge_to_energy");
        IndexedTable timeOffsets   = manager.getConstants(run, "/calibration/ft/fthodo/time_offsets");
        IndexedTable geometry      = manager.getConstants(run, "/geometry/ft/fthodo");
        IndexedTable trkGeo        = manager.getConstants(run, "/geometry/ft/fttrk");
        */
        
        if(debugMode>=1) System.out.println("\nAnalyzing new event");
        List<FTTRKHit> allhits = null;
        
//        allhits = this.readRawHits(event,charge2Energy,timeOffsets,geometry,trkGeo);
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
            
        boolean needsReordering = false;
        // loop on layers
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
                            HitArray[ris][il].get_Edep(), HitArray[ris][il].get_Time(), HitArray[ris][il].get_Id()));
//                    System.out.println(" clusterHits iteration " + is + " strip " + ris + " " + HitArray[ris][il].get_Strip());
                    // look for a consecutive strip in the stack and stick it to the cluster
                    int isnext = is+1; 
                    while(isnext < 512 && HitArray[indR[isnext]][il] != null && !checked[indR[isnext]][il]){
                        int nris = indR[isnext];
                        checked[nris][il] = true;
                        clusterHits.add(new FTTRKHit(HitArray[nris][il].get_Sector(), HitArray[nris][il].get_Layer(), HitArray[nris][il].get_Strip(), 
                            HitArray[nris][il].get_Edep(), HitArray[ris][il].get_Time(), HitArray[nris][il].get_Id()));
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
                            HitArray[lis][il].get_Edep(), HitArray[lis][il].get_Time(), HitArray[lis][il].get_Id()));
                    // look for a consecutive strip in the stack and stick it to the cluster
                    int isnext = is+1; 
                    while(isnext < 640 && HitArray[indL[isnext]][il] != null && !checked[indL[isnext]][il]){
                        int nlis = indL[isnext];
                        checked[nlis][il] = true;
                        clusterHits.add(new FTTRKHit(HitArray[nlis][il].get_Sector(), HitArray[nlis][il].get_Layer(), HitArray[nlis][il].get_Strip(), 
                            HitArray[nlis][il].get_Edep(), HitArray[lis][il].get_Time(), HitArray[nlis][il].get_Id()));
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
                    int control = clust.get_CId();
                    for(int i=0; i < nbhits; i++){
                        FTTRKHit hit = clust.get(i);
                        int nstrip = hit.get_Strip();
//                        System.out.println("strip " + nstrip + " clusterId " + clust.get_CId() + " layer " + clust.get_Layer());
                        if(nstrip==128 || nstrip==385 || nstrip==640 || nstrip==641){
                            needsReordering = true;
                            if(nstrip==128) clusterId11 = clust.get_CId();  //cut 127-384
                            if(nstrip==385) clusterId12 = clust.get_CId();
                            if(nstrip==640) clusterId21 = clust.get_CId(); // cut 640-641
                            if(nstrip==641) clusterId22 = clust.get_CId();
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
                //FTTRKCluster joinedClusters = new FTTRKCluster(1, il+1, clusters.size()+1);
                // new list for joined clusters
//                FTTRKCluster joinedClusters = new FTTRKCluster(1, il+1, clusters.size()-2);
                FTTRKCluster joinedClusters = new FTTRKCluster(1, il+1, ++clustersize);
                // update cluster and hit ID
                ArrayList<FTTRKHit> cloneHitsWNewID = new ArrayList<FTTRKHit>();
                for(FTTRKHit clHit: twoClusterHits){
                    clHit.set_DGTZIndex(clHit.get_Id());
                    //clHit.set_ClusterIndex(clusters.size()-2);  // was +1
//                    clHit.set_ClusterIndex(newIndex); //gets the index of the first of the joined clusters (both will be removed)
                    cloneHitsWNewID.add(clHit);
                }   
                joinedClusters.addAll(cloneHitsWNewID);
//                joinedClusters.addAll(twoClusterHits);
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
//                FTTRKCluster joinedClusters = new FTTRKCluster(1, il+1, clusters.size()-2);
                FTTRKCluster joinedClusters = new FTTRKCluster(1, il+1, ++clustersize);
//                joinedClusters.addAll(twoClusterHits);
                // update cluster and hit ID
                ArrayList<FTTRKHit> cloneHitsWNewID = new ArrayList<FTTRKHit>();
                for(FTTRKHit clHit: twoClusterHits){
                    clHit.set_DGTZIndex(clHit.get_Id());
                    //clHit.set_ClusterIndex(clusters.size()-2);
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
        
        
        // before returning cluster list re-calculate centroid and its error, if the cluster list was modified
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
                            && (inlayerclus.get_MaxStrip()+outlayerclus.get_MaxStrip() < FTTRKConstantsLoader.Nstrips*2) ) { // put correct numbers to make sure the intersection is valid

                    // define new cross 
                    FTTRKCross this_cross = new FTTRKCross(inlayerclus.get_Sector(), inlayerclus.get_Region(),++rid);
                    this_cross.set_Cluster1(inlayerclus);
                    this_cross.set_Cluster2(outlayerclus);
                    int dummy = this_cross.get_Cluster1().get_CId();
                    int dummy2 = this_cross.get_Cluster2().get_CId();

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
        
        double diffRadTolerance = FTConstants.TRK0_TRK1_RADTOL;
        double diffPhiTolerance = FTConstants.TRK0_TRK1_PHITOL;
        double diffThetaTolerance = FTConstants.TRK0_TRK1_THETATOL;
        
// make a geometric match of the two crosses 
        if(validatedCrosses.size()== FTTRKConstantsLoader.NSupLayers){            
            Point3D cross0 = validatedCrosses.get(0).get_Point();
            Point3D cross1 = validatedCrosses.get(1).get_Point();
            double r02d = Math.sqrt(cross0.x()*cross0.x() + cross0.y()*cross0.y());
            double r12d = Math.sqrt(cross1.x()*cross1.x() + cross1.y()*cross1.y());
            double r0 = Math.sqrt(r02d*r02d + cross0.z()*cross0.z());
            double r1 = Math.sqrt(r12d*r12d + cross1.z()*cross1.z());
            double diffRadii =  r02d-r12d;
            double diffTheta = Math.acos(cross0.z()/r0) - Math.acos(cross1.z()/r1);
            double diffPhi = Math.atan2(cross0.y(), cross0.x()) - Math.atan2(cross1.y(), cross1.x());
            if(!(Math.abs(diffPhi) < diffPhiTolerance && Math.abs(diffRadii)< diffRadTolerance && Math.abs(diffTheta) < diffThetaTolerance)) validatedCrosses.clear();
        }
        
        //return validatedCrosses;
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
            int nsec1 = -1, nsec2 = -1;
            for(int row = 0; row < nrows; row++){
                int isector     = bankDGTZ.getInt("sector",row);
                int ilayer      = bankDGTZ.getInt("layer",row);
                int icomponent  = bankDGTZ.getInt("component",row);
                int iorder      = bankDGTZ.getInt("order",row);
                int adc         = bankDGTZ.getInt("ADC",row);
//                float time      = bankDGTZ.getLong("timestamp",row); // entry not avalable in mc banks yet
                float time      = bankDGTZ.getFloat("time", row);
                // set threshold on FTTRK ADCs (in FTConstants.java
                
/////////////////////////////////////////////////////// insert here operations modifying strip number 
/////////////////////////////////////////////////////// IN RECO: STRIP NUMBERS: 0-767, layer numbers: 0-3 
/////////////////////////////////////////////////////// IN FEE: STRIP NUMBERS: 1-768, layer numbers: 1-4
                
                // swap layer numbers
                /*
                if(ilayer==1){
                    ilayer = 3;
                }else if(ilayer==2){
                    ilayer = 4;
                }else if(ilayer==3){
                    ilayer = 1;
                }else if(ilayer==4){
                    ilayer = 2;
                }
                */
                // swap left/right short strips sectors for layer 2 and 4  (it means a reflection wrt y axis)
                
                /*
                if(ilayer==4 || ilayer==2){        
                    if(icomponent>=129 && icomponent<=384){
                        icomponent += 256;
                    }else if(icomponent>=385 && icomponent<=640){
                        icomponent -= 256;
                    }
                }
                */
                
                /*
                if(ilayer==2 || ilayer==3) icomponent = flipStripVertical(ilayer, icomponent); 
                if(ilayer==4) icomponent = flipStripHorizontal(ilayer, icomponent);
                */
                
                // read just layer sectors only for real data (no montecarlo)
              //icomponent = adjustStripNumberingTest11(run, ilayer, icomponent);
              //icomponent = renumberStrip(ilayer, icomponent);
              //icomponent = renumberFEE2RECRotatedAndAdjustVanilla(run, ilayer, icomponent);
              //icomponent = renumberFEE2RECRotatedAndAdjustHazel(run, ilayer, icomponent);
              if(run != 10) icomponent = renumberFEE2RECRotatedAndAdjustWalnut(run, ilayer, icomponent);
                
              
/*                
// layer 3: swap 6-7 e inversione                
                if(ilayer==3){
                   int isec1 = findSector(icomponent);
                   if(isec1 == 6){
                       icomponent = swapSectors(icomponent, 7);
                   }else if(isec1 == 7){
                       icomponent = swapSectors(icomponent, 6);
                   } 
                   int newSector = findSector(icomponent);
                   if(newSector == 6 || newSector==7) icomponent = reverseStripsInSector(icomponent);
                }         
 
// swap central sectors (6->9, 7->10, 8->11) for layers 1,4  - large sectors (16) numbering
                if(ilayer==1 || ilayer == 4 || ilayer==3 || ilayer==2){
                    int isec1 = findSector(icomponent);
                    if(isec1==6){     
                        icomponent = swapSectors(icomponent, 10);
                    }else if(isec1==7){
                        icomponent = swapSectors(icomponent, 11);
                    }else if(isec1==8){
                        icomponent = swapSectors(icomponent, 12);
                    }else if(isec1==9){
                        icomponent = swapSectors(icomponent, 13);
                    }else if(isec1==10){
                        icomponent = swapSectors(icomponent, 6);
                    }else if(isec1==11){
                        icomponent = swapSectors(icomponent, 7);
                    }else if(isec1==12){
                        icomponent = swapSectors(icomponent, 8);
                    }else if(isec1==13){
                        icomponent = swapSectors(icomponent, 9);
                    }
                    
//                    }else if(isec1==2){
//                        icomponent = swapSectors(icomponent, 14);
//                    }else if(isec1==3){
//                        icomponent = swapSectors(icomponent, 15);
//                    }else if(isec1==4){
//                        icomponent = swapSectors(icomponent, 16);
//                    }else if(isec1==5){
//                        icomponent = swapSectors(icomponent, 17);
//                    }else if(isec1==14){
//                        icomponent = swapSectors(icomponent, 2);
//                    }else if(isec1==15){
//                        icomponent = swapSectors(icomponent, 3);
//                    }else if(isec1==16){
//                        icomponent = swapSectors(icomponent, 4);
//                    }else if(isec1==17){
//                        icomponent = swapSectors(icomponent, 5);
//                    }                  
                }
            
*/
                            
                
/////////////////////////////////////////////////////////////////////////////////////////////////

                if(adc>FTConstants.FTTRKMinAdcThreshold && adc<FTConstants.FTTRKMaxAdcThreshold && time!=-1 && icomponent!=-1){
//                    System.out.println("~~~~~~~~~~ adc dell'hit accettato " + adc + " component " + icomponent + " layer " + ilayer);
                    FTTRKHit hit = new FTTRKHit(isector, ilayer, icomponent, (double)adc, (double)time, ++hitId);

                    
//////////////////////////////////////////////////////// insert here operations selecting strips or groups of strips

                    // select only one sector at a time
                    /*
                    if(ilayer == 1){
                        if(icomponent>0 && icomponent<=64) hits.add(hit);
                    }else if(ilayer == 3){
                        if(icomponent>705&& icomponent<=768) hits.add(hit);
                    }else{
                        hits.add(hit);
                    }
*/

//                    if(icomponent>64 && icomponent<=128) hits.add(hit);
//                    if(icomponent>128 && icomponent<=160) hits.add(hit);
//                    if(icomponent>160 && icomponent<=192) hits.add(hit);
//                    if(icomponent>192 && icomponent<=256) hits.add(hit);
//                    if(icomponent>256 && icomponent<=320) hits.add(hit);
//                    if(icomponent>320 && icomponent<=352) hits.add(hit);
//                    if(icomponent>352 && icomponent<=384) hits.add(hit);
//                    if(icomponent>384 && icomponent<=416) hits.add(hit);
//                    if(icomponent>416 && icomponent<=448) hits.add(hit);
//                    if(icomponent>448 && icomponent<=512) hits.add(hit);
//                    if(icomponent>513 && icomponent<=577) hits.add(hit);
//                    if(icomponent>577 && icomponent<=610) hits.add(hit);
//                    if(icomponent>610 && icomponent<=640) hits.add(hit);
//                    if(icomponent>640 && icomponent<=705) hits.add(hit);
//                    if(icomponent>705 && icomponent<=768) hits.add(hit);
//                      if(icomponent>512)


/*
                      if(ilayer==1){
                            if(isInSector(0, icomponent)) hits.add(hit);
                      }else if(ilayer==2){
                            if(isInSector(7, icomponent)) hits.add(hit);
                      }else if(ilayer==3){
                            if(isInSector(19, icomponent)) hits.add(hit);        
                      }else if(ilayer==4){
                            if(isInSector(3, icomponent)) hits.add(hit);
}
*/
/*
                      if(isStripInConnector(3, ilayer, icomponent)) {
                          hits.add(hit);
                      }
*/

// select strips belonging to a crate (1-2-3)
///                      if(isStripInCrate(1, ilayer, icomponent)) hits.add(hit);
// select strips belonging to a given physical connector in a layer
//                      int iconn = findPhysicalConnector(icomponent);  
//                      if(ilayer==2 && iconn==6) hits.add(hit);           // bad connector
//                      if(ilayer==4 && iconn==12) hits.add(hit);          // bad connector


                      /*
                      if(ilayer==1 && icomponent>=1 && icomponent<=64) hits.add(hit);
                      if(ilayer==2 && icomponent>=193 && icomponent<=258) hits.add(hit);
                      if(ilayer==3 && icomponent>=705 && icomponent<=768) hits.add(hit);
                      if(ilayer==4 && icomponent>=119 && icomponent<=384) hits.add(hit);
                      */
                      
                      boolean isHitAccepted = true;
//                      if(ilayer == 2 || ilayer ==3) isHitAccepted = false;
//                      if((ilayer==2) && icomponent<=641) isHitAccepted = false; 
//                      if((ilayer==4) && icomponent>=1 && icomponent>64) isHitAccepted = false;

// strip sector selection in layer 1-2
/*
                      if(ilayer==1){
                          nsec1 = findIn12Sectors(icomponent);
                          if(nsec1!=10 && nsec1!=11) isHitAccepted = false;
                      }
                      if(ilayer==2){
                          nsec2 = findIn12Sectors(icomponent);
                          if(nsec2 ==6){
                              System.out.println("layer 2 sector 6 "  + icomponent);
//                              if(icomponent-64*6 > 32.){icomponent = -1;}else{icomponent = 200;}
                          }else{
                              icomponent = -1;
                          }
                      }                  
//                      if(nsec1 !=5) isHitAccepted = false;
*/                     
                      
//  selection on strip time
                     if(time < FTConstants.TRK_STRIP_MIN_TIME || time > FTConstants.TRK_STRIP_MAX_TIME) isHitAccepted = false;
                      if(icomponent<0) isHitAccepted = false;
                      
                      if(isHitAccepted) hits.add(hit);                  
////////////////////////////////////////////////////////////////////////////////////////                      
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
//                bankCluster.setShort("id"  ,      i,(short) i);
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
            crEnergy = new float[crosses.size()];
            crTime = new float[crosses.size()];
            for (int j = 0; j < crosses.size(); j++){
                // put here cut on time and energy of crosses
                //if(crosses.get(j).get_Energy()>1200.) continue;
                // these are probaly outoftimers, no needo to cut
                //if(crosses.get(j).get_Time()<150. || crosses.get(j).get_Time()>250.) continue;
                ///////////////////////////////////////////////////////////////////////////////////////        
                bankCross.setShort("size",       j, (short) crosses.size());
//                bankCross.setShort("id",         j, (short) crosses.get(j).get_Id());
                bankCross.setShort("id",         j, (short) crosses.get(j).get_trkId());
                bankCross.setByte("sector",      j, (byte)  crosses.get(j).get_Sector());
                bankCross.setByte("detector",    j, (byte)  (crosses.get(j).get_Region()-1));  // detector: 0 or 1, region 1 or 2
                bankCross.setFloat("x",          j, (float) crosses.get(j).get_Point().x());
                bankCross.setFloat("y",          j, (float) crosses.get(j).get_Point().y());
                bankCross.setFloat("z",          j, (float) crosses.get(j).get_Point().z());
                //        System.out.println("energy and time to be stored in banks " + crosses.get(j).get_Energy() + " " + crosses.get(j).get_Time());
                FTEBEngine.h507.fill(crosses.get(j).get_Time(), crosses.get(j).get_Energy());   
                if(crosses.get(j).get_Id()==0){
                    FTEBEngine.h503.fill(crosses.get(j).get_Time());
                    FTEBEngine.h501.fill(crosses.get(j).get_Energy());
                    FTEBEngine.h505.fill(crosses.get(j).get_Time(), crosses.get(j).get_Energy());
                    FTTRKCluster cl1 = crosses.get(j).get_Cluster1();
                    FTTRKCluster cl2 = crosses.get(j).get_Cluster2();
                    FTEBEngine.h510.fill(cl1.get_TotalEnergy(), cl2.get_TotalEnergy());
                    FTEBEngine.h512.fill(cl1.get_TotalEnergy());
                    FTEBEngine.h512.fill(cl2.get_TotalEnergy());
                    for(int k=0; k<cl1.size(); k++) FTEBEngine.h520.fill(cl1.get(k).get_Time());
                    for(int k=0; k<cl2.size(); k++) FTEBEngine.h521.fill(cl2.get(k).get_Time());         
                }
                if(crosses.get(j).get_Id()==1){
                    FTEBEngine.h504.fill(crosses.get(j).get_Time());
                    FTEBEngine.h502.fill(crosses.get(j).get_Energy());
                    FTEBEngine.h506.fill(crosses.get(j).get_Time(), crosses.get(j).get_Energy());
                    FTTRKCluster cl1 = crosses.get(j).get_Cluster1();
                    FTTRKCluster cl2 = crosses.get(j).get_Cluster2();
                    FTEBEngine.h511.fill(crosses.get(j).get_Cluster1().get_TotalEnergy(), crosses.get(j).get_Cluster2().get_TotalEnergy());
                    FTEBEngine.h513.fill(crosses.get(j).get_Cluster1().get_TotalEnergy());
                    FTEBEngine.h513.fill(crosses.get(j).get_Cluster2().get_TotalEnergy());
                    for(int k=0; k<cl1.size(); k++) FTEBEngine.h522.fill(cl1.get(k).get_Time());
                    for(int k=0; k<cl2.size(); k++) FTEBEngine.h523.fill(cl2.get(k).get_Time());    
                }
                FTTRKReconstruction.crEnergy[j] = crosses.get(j).get_Energy();
                FTTRKReconstruction.crTime[j] = crosses.get(j).get_Time();
               
                bankCross.setFloat("energy", j, (float) crosses.get(j).get_Energy());
                bankCross.setFloat("time", j, (float) crosses.get(j).get_Time());
                int dummy = crosses.get(j).get_Cluster1().get_CId();
                int dummy2 = crosses.get(j).get_Cluster2().get_CId();
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
 
 
 
public int adjustStripNumberingTest11(int run, int ilayer, int icomponent){
    // adjustment of strip numbering valid for fall18 data
    if(run>0){    // provisional
        if(ilayer==1){
            //icomponent = overturnModule(ilayer, icomponent);
            icomponent = flipStripVertical(ilayer, icomponent);
            int isec1 = findSector(icomponent);
            
            // test 9
            /*
            if(isec1==2){      // era 2-5, 5-13, 13-12, 12-2 (test8_2)
                icomponent = swapSectors(icomponent, 12);
            }else if(isec1==3){
                icomponent = swapSectors(icomponent, 2);
            }else if(isec1==5){
                icomponent = swapSectors(icomponent, 13);
            }else if(isec1==12){
                icomponent = swapSectors(icomponent, 3); 
            }else if(isec1==13){
                icomponent = swapSectors(icomponent, 5); 
            */
            // test10
            if(isec1==2){      
                icomponent = swapSectors(icomponent, 12);
            }else if(isec1==12){
                icomponent = swapSectors(icomponent, 2);
            }else if(isec1==5){
                icomponent = swapSectors(icomponent, 13); 
            }else if(isec1==13){
                icomponent = swapSectors(icomponent, 5); 
            
            //}else if(isec1==12){
            //    icomponent = swapSectors(icomponent, 10);
            //}else if(isec1==10){
            //    icomponent = swapSectors(icomponent, 12);
                
            /*
            if(isec1==12){
                icomponent = swapSectors(icomponent, 10);
            }else if(isec1==10){
                icomponent = swapSectors(icomponent, 12);
            }else if(isec1==5){
                icomponent = swapSectors(icomponent, 2);
            }else if(isec1==2){
                icomponent = swapSectors(icomponent, 5);
            }else if(isec1==13){
                icomponent = swapSectors(icomponent, 11);
            }else if(isec1==11){
                icomponent = swapSectors(icomponent, 13);    
            */
            
            /*
            if(isec1==13){
                icomponent = swapSectors(icomponent, 12);
            }else if(isec1==12){
                icomponent = swapSectors(icomponent, 10);
            }else if(isec1==10){
                icomponent = swapSectors(icomponent, 13);
            }else if(isec1==5){
                icomponent = swapSectors(icomponent, 2);
            }else if(isec1==2){
                icomponent = swapSectors(icomponent, 5);    
            */
            
            }else if(isec1==14){
                icomponent = swapSectors(icomponent, 17);
            }else if(isec1==15){
                icomponent = swapSectors(icomponent, 16);
            }else if(isec1==16){
                icomponent = swapSectors(icomponent, 15);
            }else if(isec1==17){
                icomponent = swapSectors(icomponent, 14);
            }       
            // reverse sectors
            int newsec = findSector(icomponent);
            if(newsec==16 || newsec==17 || newsec==2) icomponent = reverseStripsInSector(icomponent);
            
// test 12
//            if(newsec==13){
//                icomponent = swapSectors(icomponent, 10);        
//            }else if(newsec==10){
//                icomponent = swapSectors(icomponent, 13);
//            }    
        
            
            /*
            newsec = findSector(icomponent);
            if(newsec==3){
                icomponent = swapSectors(icomponent, 12);
            }else if(newsec==12){
                icomponent = swapSectors(icomponent, 3);   
            }
            if(newsec==2 || newsec==4) icomponent = reverseStripsInSector(icomponent);
            */

        }else if(ilayer==2){
            icomponent = flipStripVertical(ilayer, icomponent);
            int isec1 = findSector(icomponent);
            
            if(isec1==2){      
                icomponent = swapSectors(icomponent, 12);
            }else if(isec1==12){
                icomponent = swapSectors(icomponent, 2);
            }else if(isec1==5){
                icomponent = swapSectors(icomponent, 13);
            }else if(isec1==13){
                icomponent = swapSectors(icomponent, 5);
                
            }else if(isec1==14){
                icomponent = swapSectors(icomponent, 17);
            }else if(isec1==15){
                icomponent = swapSectors(icomponent, 16);
            }else if(isec1==16){
                icomponent = swapSectors(icomponent, 15);
            }else if(isec1==17){
                icomponent = swapSectors(icomponent, 14);
            }
            // reverse sectors
            int newsec = findSector(icomponent);
            if(newsec==16 || newsec==17) icomponent = reverseStripsInSector(icomponent);
            
        }else if(ilayer==3){
            icomponent = flipStripVertical(ilayer, icomponent);
            int isec1 = findSector(icomponent);
            if(isec1==2){
                icomponent = swapSectors(icomponent, 4);
            }else if(isec1==3){
                icomponent = swapSectors(icomponent, 5);
            }else if(isec1==4){
                icomponent = swapSectors(icomponent, 2);
            }else if(isec1==5){
                icomponent = swapSectors(icomponent, 3);
                        
            }else if(isec1==14){
                icomponent = swapSectors(icomponent, 17);
            }else if(isec1==15){
                icomponent = swapSectors(icomponent, 16);
            }else if(isec1==16){
                icomponent = swapSectors(icomponent, 15);
            }else if(isec1==17){
                icomponent = swapSectors(icomponent, 14);
            }
            // reverse sectors
            int newsec = findSector(icomponent);
            if(newsec==16 || newsec==17) icomponent = reverseStripsInSector(icomponent);
        }else if(ilayer==4){
            icomponent = flipStripHorizontal(ilayer, icomponent);
            int isec1 = findSector(icomponent);
            if(isec1==2){
                icomponent = swapSectors(icomponent, 5);
            }else if(isec1==5){
                icomponent = swapSectors(icomponent, 2);
            }else if(isec1==3){
                icomponent = swapSectors(icomponent, 4);
            }else if(isec1==4){
                icomponent = swapSectors(icomponent, 3);                    
            }else if(isec1==14){
                icomponent = swapSectors(icomponent, 17);
            }else if(isec1==15){
                icomponent = swapSectors(icomponent, 16);
            }else if(isec1==16){
                icomponent = swapSectors(icomponent, 15);
            }else if(isec1==17){
                icomponent = swapSectors(icomponent, 14);
            }
            int newsec = findSector(icomponent);
            if(newsec==16 || newsec==17 || newsec==2 || newsec==3) icomponent = reverseStripsInSector(icomponent);
           
        }
    }
    return icomponent;
}
 
  
 public int flipStripVertical(int ilayer, int icomponent){
    // flip the layer vertically with respect to y axis (left/right flip)
    // strips numbered 1-768
    /*
    if(ilayer==1 || ilayer==3){ // vertical strips         
        if(icomponent>=129 && icomponent<=384){
            icomponent = 512-icomponent;
        }else if(icomponent>384 && icomponent<=640){
            icomponent = 1025 - icomponent;
        }else{
            icomponent = 769 - icomponent;
        }
    }else if(ilayer==2 || ilayer==4){
        if(icomponent>=129 && icomponent<=384){
            icomponent = 513 - icomponent;   
        }else if(icomponent>384 && icomponent<=640){
            icomponent = 1025 - icomponent;
        }else{
            //icomponent = 769 - icomponent;
        }
    }
    */
    
    // strips numbers 1-768
    if(ilayer==2 || ilayer==3){  // RECO vertical strips        
        if(icomponent>=129 && icomponent<=384){
            icomponent += 256;
        }else if(icomponent>=385 && icomponent<=640){
            icomponent -= 256;
        }
    }else if(ilayer==1 || ilayer==4){  // RECO horizontal strips
        if(icomponent>=129 && icomponent<=384){
            icomponent = 513 - icomponent;   
        }else if(icomponent>=385 && icomponent<=640){
            icomponent = 1025 - icomponent;
        }else{ // for horizontal strips flip also long strips
            icomponent = 769 - icomponent;
        }
    }
    
    
    return icomponent;
 }
 
 public int flipStripHorizontal(int ilayer, int icomponent){
    // flip the layer horizontally with respect to the x axis (top/bottom flip)
    // strips 1-128
    /*
    if(ilayer==1 || ilayer==3){
        if(icomponent>=129 && icomponent<=384){
            icomponent += 256;
        }else if(icomponent>=385 && icomponent<=640){
            icomponent -= 256;
        }else{
            icomponent = 769 - icomponent;
        } 
    }else if(ilayer==2 || ilayer ==4){
        if(icomponent>=129 && icomponent<=384){
            icomponent = 513 - icomponent;   
        }else if(icomponent>=385 && icomponent<=640){
            icomponent = 1025 - icomponent;
        }else{
         //   icomponent = 769 - icomponent;
        }
    }
    */
    if(ilayer==1 || ilayer==4){  // RECO vertical strips        
        if(icomponent>=129 && icomponent<=384){
            icomponent += 256;
        }else if(icomponent>=385 && icomponent<=640){
            icomponent -= 256;
        }
    }else if(ilayer==2 || ilayer==3){ // RECO horizontal strips
        if(icomponent>=129 && icomponent<=384){
            icomponent = 513 - icomponent;   
        }else if(icomponent>=385 && icomponent<=640){
            icomponent = 1025 - icomponent;
        }else{
            icomponent = 769 - icomponent;
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
    // excluded in the sector, [i+1]is inluded
    int[] sectorLimit = {0, 64, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448, 480, 512, 544, 576, 608, 640, 704, 768};
    int nsector = findSector(icomponent);
    if(nsector>20) System.out.println("wrong sector number, check code");
    int offset = sectorLimit[nsector+1] - icomponent;
    icomponent = sectorLimit[nsector]+1 + offset;
      
    return icomponent; 
 } 

public int reverseStripInFirstHalf(int icomponent){
    // reverse the number of strips inside a sector (strips are numbered 1-768, the [i] component of sectorLimit vector is
    // excluded in the sector, [i+1]is inluded
    int[] sectorLimit = {0, 64, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448, 480, 512, 544, 576, 608, 640, 704, 768};
    int nsector = findSector(icomponent);
    if(nsector>20) System.out.println("wrong sector number, check code");
    int halfStrip = (sectorLimit[nsector+1]-sectorLimit[nsector])/2 + sectorLimit[nsector];
    if(icomponent <= halfStrip){
        int offset = halfStrip - icomponent;
        icomponent = sectorLimit[nsector]+1 + offset;
    }
    return icomponent; 
 }


public int reverseStripInSecondHalf(int icomponent){
    // reverse the number of strips inside a sector (strips are numbered 1-768, the [i] component of sectorLimit vector is
    // excluded in the sector, [i+1]is included
    int[] sectorLimit = {0, 64, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448, 480, 512, 544, 576, 608, 640, 704, 768};
    int nsector = findSector(icomponent);
    if(nsector>20) System.out.println("wrong sector number, check code");
    int halfStrip = (sectorLimit[nsector+1]-sectorLimit[nsector])/2 + sectorLimit[nsector];
    if(icomponent > halfStrip){
        int offset = sectorLimit[nsector+1] - icomponent;
        icomponent = halfStrip + offset;
    }  
    return icomponent; 
 } 

public int swapHalves(int icomponent){
    // swap half the module to the opposite half, rigid translation
    // the strips are numbered 1-768, the [i] component of sectorLimit vector is
    // excluded in the sector, [i+1]is included
    int[] sectorLimit = {0, 64, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448, 480, 512, 544, 576, 608, 640, 704, 768};
    int nsector = findSector(icomponent);
    if(nsector>20) System.out.println("wrong sector number, check code");
    int halfWid = (sectorLimit[nsector+1]-sectorLimit[nsector])/2;
    int halfStrip =  halfWid + sectorLimit[nsector];
    if(icomponent >= halfStrip+1){
        icomponent -= halfWid;
    }else{
        icomponent += (halfWid-1);
    }
    return icomponent; 
 }

public int switchStripOff(){return -1;}

public int reverseStripsIn12Sectors(int icomponent){
    int[] sectorLimit = {0, 64, 128, 192, 256, 320, 384, 448, 512, 576, 640, 704, 768};
    int nsector = findIn12Sectors(icomponent);
    int offset = sectorLimit[nsector+1] - icomponent;
    icomponent = sectorLimit[nsector] + offset;
      
    return icomponent; 
}


public int swapSectors(int icomponent, int nsector2){
    // get the new strip number of the icomponent strip in nsector1 once the sector is swapped to nsector2
    // icomponent strips are numbered 1-768 so the vector sectorLimits contains the number of the starting 
    // strip of a sector: [i] is excluded from sector i, [i+1] is included
    int[] sectorLimit = {0, 64, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448, 480, 512, 544, 576, 608, 640, 704, 768};
    int nsector1 = findSector(icomponent);
    int offset = -sectorLimit[nsector1] + icomponent;
    int newicomp = sectorLimit[nsector2] + offset;
           
    return newicomp;
}

public int swap12Sectors(int icomponent, int nsector2){
    // get the new strip number of the icomponent strip in nsector1 once the sector is swapped to nsector2
    //int[] sectorLimits = {0, 64, 128, 192, 256, 320, 384, 498, 512, 576, 640, 704, 768};
    int[] sectorNumber =   {0,  1,   2,   3,   8,   9,   4,   5,   6,   7,   10,   11 };
    int[] sectorLimits = {0, 64, 128, 192, 256, 448, 512, 576, 640, 320, 384, 704, 768};
    int[] sectorLimitsSequential = {0, 64, 128, 192, 256, 320, 384, 448, 512, 576, 640, 704, 768};
    int[] sectorNumberSequential =   {0,  1,   2,   3,   4,   5,   6,   7,   8,   9,   10,   11 };    
    int nsector1 = findIn12Sectors(icomponent);
    int secidx1 = -1; 
    int secidx2 = -1;
    /*
    for(int i=0; i<11; i++){
        if(sectorNumberSequential[i] == nsector1) secidx1 = i;
        if(sectorNumberSequential[i] == nsector2) secidx2 = i;
    }
    */
    int offset = -sectorLimitsSequential[nsector1] + icomponent;
    int newicomp = sectorLimitsSequential[nsector2] + offset;
           
    return newicomp;
}




public static int findSector(int icomponent){
    // returns the sector number, corresponding to the component of the lower extreme of the interval
    // sectors are numbered 0-20; icomponent strips are numbered 1-768 so the vector sectorLimits contains
    // the number of the starting strip of a sector: [i] is excluded from sector i, [i+1] is included
    int[] sectorLimits = {0, 64, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448, 480, 512, 544, 576, 608, 640, 704, 768};
    int nsector = -1;
    for(int i=0; i<20; i++){
        if(icomponent>sectorLimits[i] && icomponent<=sectorLimits[i+1]){
//        if(icomponent>=sectorLimits[i] && icomponent<sectorLimits[i+1]){
            nsector = i;
            break;
        } 
    }
    if(nsector>20) System.out.println("wrong sector number, check code");
    return nsector;
}

public static int findIn12Sectors(int icomponent){
    // returns the sector number, corresponding to the component of the lower extreme of the interval
    // sectors are numbered 0-11
    //int[] sectorLimits = {0, 64, 128, 192, 256, 320, 384, 498, 512, 576, 640, 704, 768};
    int[] sectorLimits = {0, 64, 128, 192, 256, 320, 384, 448, 512, 576, 640, 704, 768};
    int[] sectorNumber =   {0,  1,   2,   3,   8,   9,   4,   5,   6,   7,   10,   11 };
    int nsector = -1;
    for(int i=0; i<12; i++){
        if(icomponent>sectorLimits[i] && icomponent<=sectorLimits[i+1]){
//            nsector = sectorNumber[i];
            nsector = i;
            break;
        } 
    }
    return nsector;
}


public static int findIn16Sectors(int icomponent){
    // returns the sector number, corresponding to the component of the lower extreme of the interval
    // sectors are numbered 0-16
    int[] sectorLimits = {0, 64, 128, 160, 192, 224, 256, 320, 352, 384, 416, 448, 512, 576, 608, 640, 704, 768};
    int nsector = -1;
    for(int i=0; i<16; i++){
        if(icomponent>sectorLimits[i] && icomponent<=sectorLimits[i+1]){
            nsector = i;
            break;
        } 
    }
    return nsector;
}


public boolean isInSector(int iSector, int icomponent){
    // returns ture is the strip icomponent is in the iSector sector
    int[] sectorLimits = {0, 64, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448, 480, 512, 544, 576, 608, 640, 704, 768};
    if(icomponent>sectorLimits[iSector] && icomponent<=sectorLimits[iSector+1]){
        return true;
    }else{
        return false;
    }
} 
    
public int adjustStripNumbering(int run, int ilayer, int icomponent){
    // adjustment of strip numbering valid for fall18 data
    if(run>0){    // provisional
        if(ilayer==1){
           ;
        }else if(ilayer==2){
           //icomponent = flipStripVertical(ilayer, icomponent);
            
        }else if(ilayer==3){
           //icomponent = flipStripVertical(ilayer, icomponent);

        }else if(ilayer==4){
           ;
        }
    }
    return icomponent;
}    
    

public boolean isStripInSlot(int slot, int icomponent){
    // exits true if the strip is located in the group corresponding to one of the 12 fee slots (64 strips each)
    // strips numeration in slots start from 1 to 768
    int[] slotLimits = {1, 64, 128, 192, 256, 320, 384, 448, 512, 576, 640, 704, 768};
    // 12 slots, from 0 to 11, ordered according to the sequence: 0,1,2,3, 6,7,8,9, 4,5,10,11
    icomponent++;
    boolean isInSlot = false;
    switch(slot){
        case 0:
            if(icomponent>=slotLimits[0] && icomponent<=slotLimits[1]) isInSlot = true;
            break;
        case 1:
            if(icomponent>slotLimits[1] && icomponent<=slotLimits[2]) isInSlot = true;
            break;
        case 2:
            if(icomponent>slotLimits[2] && icomponent<=slotLimits[3]) isInSlot = true;
            break;
        case 3:
            if(icomponent>slotLimits[3] && icomponent<=slotLimits[4]) isInSlot = true;
            break;
        case 6:
            if(icomponent>slotLimits[4] && icomponent<=slotLimits[5]) isInSlot = true;
            break;
        case 7:
            if(icomponent>slotLimits[5] && icomponent<=slotLimits[6]) isInSlot = true;
            break;
        case 8:
            if(icomponent>slotLimits[6] && icomponent<=slotLimits[7]) isInSlot = true;
            break;
        case 9:
            if(icomponent>slotLimits[7] && icomponent<=slotLimits[8]) isInSlot = true;
            break;
        case 4:
            if(icomponent>slotLimits[8] && icomponent<=slotLimits[9]) isInSlot = true;
            break;
        case 5:
            if(icomponent>slotLimits[9] && icomponent<=slotLimits[10]) isInSlot = true;
            break;
        case 10:
            if(icomponent>slotLimits[10] && icomponent<=slotLimits[11]) isInSlot = true;
            break;
        case 11:
            if(icomponent>slotLimits[11] && icomponent<=slotLimits[12]) isInSlot = true;
            break;   
        default:
            break;
    }    
    return isInSlot;        
}


public boolean isStripInConnector(int iFEUConn, int ilayer, int icomponent)
{
    boolean isStripConnected = false;
    // there are 12 connectors per layer, layers are numbered 1-4, FEUConnectors 0 to 11
    switch(iFEUConn){
        case 0:
            if(ilayer==1){if(isStripInSlot(4, icomponent)) isStripConnected = true;}
            else if(ilayer==2){if(isStripInSlot(6, icomponent)) isStripConnected = true;}
            else if(ilayer==3){if(isStripInSlot(1, icomponent)) isStripConnected = true;}
            else if(ilayer==4){if(isStripInSlot(2, icomponent)) isStripConnected = true;}
            break;
        case 1:
            if(ilayer==1){if(isStripInSlot(7, icomponent)) isStripConnected = true;}
            else if(ilayer==2){if(isStripInSlot(2, icomponent)) isStripConnected = true;}
            else if(ilayer==3){if(isStripInSlot(1, icomponent)) isStripConnected = true;}
            else if(ilayer==4){if(isStripInSlot(2, icomponent)) isStripConnected = true;}
            break;
        case 2:
            if(ilayer==1){if(isStripInSlot(7, icomponent)) isStripConnected = true;}
            else if(ilayer==2){if(isStripInSlot(7, icomponent)) isStripConnected = true;}
            else if(ilayer==3){if(isStripInSlot(0, icomponent)) isStripConnected = true;}
            else if(ilayer==4){if(isStripInSlot(1, icomponent)) isStripConnected = true;}
            break;
        case 3:
            if(ilayer==1){if(isStripInSlot(0, icomponent)) isStripConnected = true;}
            else if(ilayer==2){if(isStripInSlot(3, icomponent)) isStripConnected = true;}
            else if(ilayer==3){if(isStripInSlot(5, icomponent)) isStripConnected = true;}
            else if(ilayer==4){if(isStripInSlot(6, icomponent)) isStripConnected = true;}
            break;
        case 4:
            if(ilayer==1){if(isStripInSlot(0, icomponent)) isStripConnected = true;}
            else if(ilayer==2){if(isStripInSlot(6, icomponent)) isStripConnected = true;}
            else if(ilayer==3){if(isStripInSlot(5, icomponent)) isStripConnected = true;}
            else if(ilayer==4){if(isStripInSlot(6, icomponent)) isStripConnected = true;}
            break;
        case 5:
            if(ilayer==1){if(isStripInSlot(4, icomponent)) isStripConnected = true;}
            else if(ilayer==2){if(isStripInSlot(5, icomponent)) isStripConnected = true;}
            else if(ilayer==3){if(isStripInSlot(3, icomponent)) isStripConnected = true;}
            else if(ilayer==4){if(isStripInSlot(7, icomponent)) isStripConnected = true;}
            break;
        case 6:
            if(ilayer==1){if(isStripInSlot(1, icomponent)) isStripConnected = true;}
            else if(ilayer==2){if(isStripInSlot(0, icomponent)) isStripConnected = true;}
            else if(ilayer==3){if(isStripInSlot(3, icomponent)) isStripConnected = true;}
            else if(ilayer==4){if(isStripInSlot(5, icomponent)) isStripConnected = true;}
            break;
        case 7:
            if(ilayer==1){if(isStripInSlot(3, icomponent)) isStripConnected = true;}
            else if(ilayer==2){if(isStripInSlot(4, icomponent)) isStripConnected = true;}
            else if(ilayer==3){if(isStripInSlot(1, icomponent)) isStripConnected = true;}
            else if(ilayer==4){if(isStripInSlot(3, icomponent)) isStripConnected = true;}
            break;
        case 8:
            if(ilayer==1){if(isStripInSlot(4, icomponent)) isStripConnected = true;}
            else if(ilayer==2){if(isStripInSlot(7, icomponent)) isStripConnected = true;}
            else if(ilayer==3){if(isStripInSlot(2, icomponent)) isStripConnected = true;}
            else if(ilayer==4){if(isStripInSlot(5, icomponent)) isStripConnected = true;}
            break;
        case 9:
            if(ilayer==1){if(isStripInSlot(0, icomponent)) isStripConnected = true;}
            else if(ilayer==2){if(isStripInSlot(4, icomponent)) isStripConnected = true;}
            else if(ilayer==3){if(isStripInSlot(1, icomponent)) isStripConnected = true;}
            else if(ilayer==4){if(isStripInSlot(7, icomponent)) isStripConnected = true;}
            break;
        case 10:
            if(ilayer==1){if(isStripInSlot(6, icomponent)) isStripConnected = true;}
            else if(ilayer==2){if(isStripInSlot(2, icomponent)) isStripConnected = true;}
            else if(ilayer==3){if(isStripInSlot(5, icomponent)) isStripConnected = true;}
            else if(ilayer==4){if(isStripInSlot(4, icomponent)) isStripConnected = true;}
            break;
        case 11:
            if(ilayer==1){if(isStripInSlot(3, icomponent)) isStripConnected = true;}
            else if(ilayer==2){if(isStripInSlot(2, icomponent)) isStripConnected = true;}
            else if(ilayer==3){if(isStripInSlot(1, icomponent)) isStripConnected = true;}
            else if(ilayer==4){if(isStripInSlot(6, icomponent)) isStripConnected = true;}
            break;
        default:
            break;
        }
    return isStripConnected;
}
    
public boolean isStripInCrate(int nCrate, int ilayer, int icomponent){
    boolean isInCrate = false;
    // check if the strip belongs to a given connector - connectors are numbered 1-12
    // icomponent is numberered 0-767
    int iConnector = findPhysicalConnector(icomponent);
    // check if the connector belongs to the chosen crate
    if(nCrate==1){
       if(ilayer==1){
           if(iConnector==1 || iConnector==2 || iConnector==3 || iConnector==4) isInCrate = true;  
       }else if(ilayer==2){
           if(iConnector==5 || iConnector==6 || iConnector==7 || iConnector==8) isInCrate = true; 
       }else if(ilayer==3){
           if(iConnector==5 || iConnector==6 || iConnector==7 || iConnector==8) isInCrate = true;
       }else if(ilayer==4){
           if(iConnector==1 || iConnector==2 || iConnector==3 || iConnector==4) isInCrate = true;
       }
    }else if(nCrate==2){
       if(ilayer==1){
           if(iConnector==5 || iConnector==6 || iConnector==7 || iConnector==8) isInCrate = true;  
       }else if(ilayer==2){
           if(iConnector==1 || iConnector==2 || iConnector==3 || iConnector==4) isInCrate = true; 
       }else if(ilayer==3){
           if(iConnector==9 || iConnector==10 || iConnector==11 || iConnector==12) isInCrate = true;
       }else if(ilayer==4){
           if(iConnector==9 || iConnector==10 || iConnector==11 || iConnector==12) isInCrate = true;
       }
    }else if(nCrate==3){
       if(ilayer==1){
           if(iConnector==9 || iConnector==10 || iConnector==11 || iConnector==12) isInCrate = true;  
       }else if(ilayer==2){
           if(iConnector==9 || iConnector==10 || iConnector==11 || iConnector==12) isInCrate = true; 
       }else if(ilayer==3){
           if(iConnector==1 || iConnector==2 || iConnector==3 || iConnector==4) isInCrate = true;
       }else if(ilayer==4){
           if(iConnector==5 || iConnector==6 || iConnector==7 || iConnector==8) isInCrate = true;
       }
   }else{
       System.out.println("wrong crate number selected in FTTRKReconstruction");
   } 
   return isInCrate;
}    
    
public int findPhysicalConnector(int icomponent){
    // returns the number of the physical connector one strip belongs to
    // physical connectors are numbered 1-12
    int iConnector = -1;
    if(icomponent>=0 && icomponent <64){
        iConnector = 1;
    }else if(icomponent>=64 && icomponent <128){
        iConnector = 2;
    }else if(icomponent>=128 && icomponent <192){
        iConnector = 3;
    }else if(icomponent>=192 && icomponent <256){
        iConnector = 4;
    }else if(icomponent>=384 && icomponent <448){
        iConnector = 5;
    }else if(icomponent>=448 && icomponent <512){
        iConnector = 6;
    }else if(icomponent>=512 && icomponent <576){
        iConnector = 7;
    }else if(icomponent>=576 && icomponent <640){
        iConnector = 8;
    }else if(icomponent>=256 && icomponent <320){
        iConnector = 9;
    }else if(icomponent>=320 && icomponent <384){
        iConnector = 10;
    }else if(icomponent>=640 && icomponent <704){
        iConnector = 11;
    }else if(icomponent>=704 && icomponent <768){
        iConnector = 12;
    }   
    return iConnector;
}   

public int renumberStrip(int ilayer, int icomponent){
    // renumber strips from FEE number (to RECO numbering
    // strips numbering 1-768
    int newStripNumber = -1;
    if(ilayer==1){
            if((icomponent>=1 && icomponent <=128) || (icomponent>=641 && icomponent<=768)){
                newStripNumber = icomponent;
            }else if(icomponent>=129 && icomponent<=256){
                newStripNumber = icomponent+256;
            }else if(icomponent>=257 && icomponent<=512){
                newStripNumber = icomponent-128;
            }else if(icomponent>=513 && icomponent<=640){
                newStripNumber = icomponent;
            }
    }else if(ilayer==2 || ilayer==4 || ilayer==3){
            if(icomponent>=257 && icomponent <=512){
                newStripNumber = icomponent+128;
            }else if(icomponent>=513 && icomponent <=640){
                newStripNumber = icomponent-256;
            }else{  
                newStripNumber = icomponent;
            }
    }
    return (newStripNumber);
}

public int renumberFEE2RECAndAdjust(int run, int ilayer, int icomponent){
//  apply the renumbering schema - method 2 
    if(run>0){
        //System.out.println("icomponent in bank " + icomponent + " ilayer " + ilayer);    
        icomponent = renumberStrip(ilayer, icomponent);
        if(ilayer==1 || ilayer==2 || ilayer==4) icomponent = flipStripVertical(ilayer, icomponent);
        if(ilayer==3){
            int isec1 = findSector(icomponent);
            if(isec1==11){
                icomponent = swapSectors(icomponent, 10);
            }else if(isec1==10){
                icomponent = swapSectors(icomponent, 11);
            }
            // reverse sectors 10-11 + flip horizontal
            int newsec = findSector(icomponent);
            if(newsec==11 || newsec==10) icomponent = reverseStripsInSector(icomponent);
            icomponent = flipStripHorizontal(ilayer, icomponent);
        }
        
    }
    return icomponent;     
}

public int renumberFEE2RECRotatedAndAdjust(int run, int ilayer, int icomponent){
//  apply the renumbering schema - method 2 
    if(run>0){
        //System.out.println("icomponent in bank " + icomponent + " ilayer " + ilayer);    
        icomponent = renumberStrip(ilayer, icomponent);
        
//        if(ilayer==2) icomponent = flipStripHorizontal(ilayer, icomponent);
//        if(ilayer==1) icomponent = overturnModule(ilayer, icomponent);
        if(ilayer==1 || ilayer==4) icomponent = overturnModule(ilayer, icomponent);
//        if(ilayer==2) icomponent = overturnModule(ilayer, icomponent);

        if(ilayer==3){
            int isec1 = findSector(icomponent);
            if(isec1==11){
                icomponent = swapSectors(icomponent, 10);
            }else if(isec1==10){
                icomponent = swapSectors(icomponent, 11);
            }
            // reverse sectors 10-11 + flip horizontal
            int newsec = findSector(icomponent);
            if(newsec==11 || newsec==10) icomponent = reverseStripsInSector(icomponent);
            icomponent = flipStripHorizontal(ilayer, icomponent);
        }
 
        
        if(ilayer==2){
            int isec1 = findIn12Sectors(icomponent);
//            if(isec1 == 1 || isec1==6 || isec1 == 10){
//                 icomponent = -1;
//            }
        }
        

/*
            if(isec1 == 6) icomponent = -1;
            if(isec1 == 0){
                icomponent = swap12Sectors(icomponent, 11);
            }else if(isec1 == 1){
                icomponent = swap12Sectors(icomponent, 10);
            }else if(isec1 == 11){
                icomponent = swap12Sectors(icomponent, 0);
            }else if(isec1 == 10){
                icomponent = swap12Sectors(icomponent, 1);
            }
            int newsec = findIn12Sectors(icomponent);
            if( newsec==0 || newsec==1 || newsec ==10 || newsec==11) icomponent = reverseStripsIn12Sectors(icomponent);
            if(newsec==1 || newsec==10) icomponent = -1;
*/            
        //}
        
        
        if(ilayer==1){
            
            int isec1 = findIn12Sectors(icomponent);
            /* 
            if(isec1==2){
               icomponent = swap12Sectors(icomponent, 9);
//                 icomponent = -1;
            }else if(isec1==9){
               icomponent = swap12Sectors(icomponent, 2);
            }else if(isec1==3){
                icomponent = swap12Sectors(icomponent, 6);
                //icomponent = -1;
            }else if(isec1==6){
                icomponent = swap12Sectors(icomponent, 3);
            }
            int newsec = findIn12Sectors(icomponent);
            if(newsec == 2 || newsec == 9) icomponent = reverseStripsIn12Sectors(icomponent);
            if(newsec==3){
               icomponent = swap12Sectors(icomponent, 2);
//                 icomponent = -1;
            }else if(newsec==2){
               icomponent = swap12Sectors(icomponent, 3);
            }else if(newsec==9){
                icomponent = swap12Sectors(icomponent, 6);
            }
            */
            
            // good
            
            if(isec1==3){
//                icomponent = reverseStripsInSector(icomponent);
//                icomponent = swap12Sectors(icomponent, 2); 
//                 icomponent = -1;
            }else if(isec1==6){
               icomponent = swap12Sectors(icomponent, 10);
            }else if(isec1==8){
               icomponent = swap12Sectors(icomponent, 11);
//                 icomponent = -1;
            }else if(isec1==4){
//                 icomponent = -1;
//               icomponent = swap12Sectors(icomponent, 6); 
            }else if(isec1==2){
//               icomponent = reverseStripsIn12Sectors(icomponent); 
               //icomponent = swap12Sectors(icomponent, 3);  
            }
            
            /*
            int isec2 = findIn12Sectors(icomponent);
            if(isec2==3){
               icomponent = swap12Sectors(icomponent, 6);
               icomponent = reverseStripsIn12Sectors(icomponent);
            }
            */
            
//            int newsec1 = findIn12Sectors(icomponent);   
//            if(newsec1!=2) icomponent = -1;
            
            
            /*
            if(isec1==4){
                icomponent = swapSectors(icomponent, 2);
            }else if(isec1==5){
               icomponent = swapSectors(icomponent, 3);
            }else if(isec1==3){
                icomponent = swapSectors(icomponent, 4);
            }else if(isec1==9){
                icomponent = swapSectors(icomponent, 17);
            }else if(isec1==17){
                icomponent = swapSectors(icomponent, 9);
            }
            
            int newsec = findSector(icomponent);
            if(newsec == 2) icomponent = reverseStripsInSector(icomponent);
            */
            /*
            if(isec1==10){
                icomponent = swapSectors(icomponent, 2);
            }else if(isec1==3){
                icomponent = swapSectors(icomponent, 10);
            }
            int newsec = findSector(icomponent);
            if(newsec == 2) icomponent = reverseStripsInSector(icomponent);
            */
            /*
            if(isec1==9){
                icomponent = swapSectors(icomponent, 3);
            }else if(isec1==8){
                icomponent = swapSectors(icomponent, 4);
            }else if(isec1==6){
                icomponent = swapSectors(icomponent, 5);
            }else if(isec1==7){
                icomponent = swapSectors(icomponent, 6);
            }else if(isec1==5){
                icomponent = swapSectors(icomponent, 7);
            }else if(isec1==3){
                icomponent = swapSectors(icomponent, 8);
            }else if(isec1==4){
                icomponent = swapSectors(icomponent, 9);
            }
            int newsec = findSector(icomponent);
            if(newsec == 3 || newsec==7 || newsec==4) icomponent = reverseStripsInSector(icomponent);
            */
            /*
            if(isec1==18){ 
                icomponent = swapSectors(icomponent, 17);
            }else if(isec1==17){
                icomponent = swapSectors(icomponent, 18);
            }
            */
            // prova Livio l2
            /*
            if(isec1==3){
                icomponent = swapSectors(icomponent, 5);
            }else if(isec1==4){
                icomponent = swapSectors(icomponent, 6);
            }else if(isec1==5){
                icomponent = swapSectors(icomponent, 7);
            }else if(isec1==6){
                icomponent = swapSectors(icomponent, 11);
            }else if(isec1==7){
                icomponent = swapSectors(icomponent, 10);
            }else if(isec1==8){
                icomponent = swapSectors(icomponent, 3);
            }else if(isec1==9){
                icomponent = swapSectors(icomponent, 4);
            }else if(isec1==10){
                icomponent = swapSectors(icomponent, 9);
            }else if(isec1==11){
                icomponent = swapSectors(icomponent, 8);
            }
            int newsec = findSector(icomponent);
            if( newsec==2 || newsec==9) icomponent = reverseStripsInSector(icomponent);
            */
            /*
            if(isec1==5){
                icomponent = swapSectors(icomponent, 9);
            }else if(isec1==4){
                icomponent = swapSectors(icomponent, 8);
            }else if(isec1==3){
                icomponent = swapSectors(icomponent, 7);
            }else if(isec1==2){
                icomponent = swapSectors(icomponent, 6);
            }else if(isec1==7){
                icomponent = reverseStripsInSector(icomponent);
                icomponent = swapSectors(icomponent, 4);
            }else if(isec1==8){
                icomponent = reverseStripsInSector(icomponent);
                icomponent = swapSectors(icomponent, 3);
            }else if(isec1==9){
                icomponent = reverseStripsInSector(icomponent);
                icomponent = swapSectors(icomponent, 2);
            }
            */
            /*
            if(isec1==5){
                icomponent = reverseStripsInSector(icomponent);
                icomponent = swapSectors(icomponent, 7);
            }else if(isec1==4){
                icomponent = swapSectors(icomponent, 8);
            }else if(isec1==10){
                icomponent = swapSectors(icomponent, 9);
            }else if(isec1==3){
                icomponent = swapSectors(icomponent, 10);
            }else if(isec1==7){
                icomponent = reverseStripsInSector(icomponent);
                icomponent = swapSectors(icomponent, 5);
            }else if(isec1==8){
                icomponent = reverseStripsInSector(icomponent);
                icomponent = swapSectors(icomponent, 4);
            }else if(isec1==9){
                icomponent = reverseStripsInSector(icomponent);
                icomponent = swapSectors(icomponent, 3);
            }else if(isec1==5){
                icomponent = reverseStripsInSector(icomponent);
                icomponent = swapSectors(icomponent, 7);
            }
            */
            
        }
    } 
    
    return icomponent;     
}


public int renumberFEE2RECRotatedAndAdjustVanilla(int run, int ilayer, int icomponent){
//  apply the renumbering schema - method 2 
    if(run>0){
        //System.out.println("icomponent in bank " + icomponent + " ilayer " + ilayer);    
        icomponent = renumberStrip(ilayer, icomponent);
        
        /*
        if(ilayer==1 || ilayer==4) icomponent = overturnModule(ilayer, icomponent);
        if(ilayer==3){
            int isec1 = findSector(icomponent);
            if(isec1==11){
                icomponent = swapSectors(icomponent, 10);
            }else if(isec1==10){
                icomponent = swapSectors(icomponent, 11);
            }
            // reverse sectors 10-11 + flip horizontal
            int newsec = findSector(icomponent);
            if(newsec==11 || newsec==10) icomponent = reverseStripsInSector(icomponent);
            icomponent = flipStripHorizontal(ilayer, icomponent);
        }
        */
    } 
    
    return icomponent;     
}


public int renumberFEE2RECRotatedAndAdjustHazel(int run, int ilayer, int icomponent){
//  apply the renumbering schema - method 2 
    if(run>0){
        //System.out.println("icomponent in bank " + icomponent + " ilayer " + ilayer);    
        icomponent = renumberStrip(ilayer, icomponent);
        
        if(ilayer==1 || ilayer==4) icomponent = overturnModule(ilayer, icomponent);
        if(ilayer==3){
            int isec1 = findSector(icomponent);
            if(isec1==11){
                icomponent = swapSectors(icomponent, 10);
            }else if(isec1==10){
                icomponent = swapSectors(icomponent, 11);
            }
            // reverse sectors 10-11 + flip horizontal
            int newsec = findSector(icomponent);
            if(newsec==11 || newsec==10) icomponent = reverseStripsInSector(icomponent);
            icomponent = flipStripHorizontal(ilayer, icomponent);
        }
 
        if(ilayer==2){
            int isec1 = findIn12Sectors(icomponent);
//            if(isec1 == 1 || isec1==6 || isec1 == 10) icomponent = -1;
//            if(isec1 != 10){icomponent = -1;} 
        }
        
        
        if(ilayer==1){            
            int isec1 = findIn12Sectors(icomponent);

            // good
            /*
            if(isec1==3){
//                icomponent = reverseStripsInSector(icomponent);
//                icomponent = swap12Sectors(icomponent, 2); 
//                 icomponent = -1;
            }else if(isec1==6){
               icomponent = swap12Sectors(icomponent, 10);
            }else if(isec1==8){
               icomponent = swap12Sectors(icomponent, 11);
//                 icomponent = -1;
            }else if(isec1==4){
//                 icomponent = -1;
//               icomponent = swap12Sectors(icomponent, 6); 
            }else if(isec1==2){
//               icomponent = reverseStripsIn12Sectors(icomponent); 
               //icomponent = swap12Sectors(icomponent, 3);  
            }
            */
            
            //if(isec1 != 10 && isec1 != 11) icomponent = -1; 
///            if(isec1 != 4) icomponent = -1;
        }
        
        if(ilayer==3){
//            int isec1 = findIn12Sectors(icomponent);
            int isec1 = findIn12Sectors(icomponent);
//            if(isec1 == 1 || isec1==6 || isec1 == 10) icomponent = -1;
///              if(isec1 != 8) icomponent = -1; 
        }
        
    } 
    
    return icomponent;     
}

public int renumberFEE2RECRotatedAndAdjustWalnut2(int run, int ilayer, int icomponent){
//  apply the renumbering schema - method 2 
    if(run>0){
        //System.out.println("icomponent in bank " + icomponent + " ilayer " + ilayer);    
        icomponent = renumberStrip(ilayer, icomponent);
        
        if(ilayer==1 || ilayer==4) icomponent = overturnModule(ilayer, icomponent);
        if(ilayer==3){
            int isec1 = findSector(icomponent);
            if(isec1==11){
                icomponent = swapSectors(icomponent, 10);
            }else if(isec1==10){
                icomponent = swapSectors(icomponent, 11);
            }
            // reverse sectors 10-11 + flip horizontal
            int newsec = findSector(icomponent);
            if(newsec==11 || newsec==10) icomponent = reverseStripsInSector(icomponent);
            icomponent = flipStripHorizontal(ilayer, icomponent);
        }
 
        if(ilayer==1){
            int isec1 = findSector(icomponent);
            if(isec1==18){
                if(icomponent > 672) icomponent = 1377-icomponent;
            }else if(isec1==7){
                icomponent = swapSectors(icomponent, 6);
                icomponent = reverseStripsInSector(icomponent);
            }else if(isec1==6){
                icomponent = swapSectors(icomponent, 7);
            } 
        }
        
        if(ilayer==2){
            int isec1 = findSector(icomponent);
            if(isec1==10){
                icomponent = swapSectors(icomponent, 11);
                icomponent = reverseStripsInSector(icomponent);
            }else if(isec1==11){
                icomponent = swapSectors(icomponent, 10);
                icomponent = reverseStripsInSector(icomponent);
            }
        }
        
       
        if(ilayer==4){            
            int isec1 = findSector(icomponent);
             if(isec1==18){
                if(icomponent>= 673){
                    icomponent -= 32;
                }else{
                    icomponent += 31;
                }
            }
        }
              

        if(ilayer==3){
            int isec1 = findSector(icomponent);
            if(isec1==15){
                icomponent = swapSectors(icomponent, 13);
            }else if(isec1==13){
                icomponent = swapSectors(icomponent, 15);
            }
        }
    } 
    
    return icomponent;     
}

public int maskFaultySectors(int ilay, int isec){
    int isMasked = 1;
    // bad sectors, need masking
    if(
//       (ilay==2 && (isec==12 || isec==13) ) ||
//       (ilay==3 && (isec==7 ) )  ||
        isec<0 ) 
            isMasked = 0;
    return isMasked;
}


public int renumberFEE2RECRotatedAndAdjustWalnut4(int run, int ilayer, int icomponent){
//  apply the renumbering schema - method 2 
    if(run>0){
        //System.out.println("icomponent in bank " + icomponent + " ilayer " + ilayer);    
        icomponent = renumberStrip(ilayer, icomponent);
        
        if(ilayer==1 || ilayer==4) icomponent = overturnModule(ilayer, icomponent);
        if(ilayer==3){
            int isec1 = findSector(icomponent);
            if(isec1==11){
                icomponent = swapSectors(icomponent, 10);
            }else if(isec1==10){
                icomponent = swapSectors(icomponent, 11);
            }
            // reverse sectors 10-11 + flip horizontal
            int newsec = findSector(icomponent);
            if(newsec==11 || newsec==10) icomponent = reverseStripsInSector(icomponent);
            icomponent = flipStripHorizontal(ilayer, icomponent);
        }
 
        int isec1 = -1;
        if(ilayer==1){
            isec1 = findSector(icomponent);
            if(isec1==18){
                if(icomponent > 672){ 
                    icomponent = 1377-icomponent;
                }else{
                    icomponent = 1312-icomponent;
                }
            }else if(isec1==7){
                icomponent = swapSectors(icomponent, 6);
                icomponent = reverseStripsInSector(icomponent);
            }else if(isec1==6){
                icomponent = swapSectors(icomponent, 7);
            }else if(isec1==13){
                icomponent = reverseStripsInSector(icomponent);
            }else if(isec1 == 1){
                if(icomponent >= 128){
                    icomponent = 224 - icomponent;
                }
            }
        }
        
        if(ilayer==2){
            isec1 = findSector(icomponent);
            if(isec1==10){
                icomponent = swapSectors(icomponent, 11);
                icomponent = reverseStripsInSector(icomponent);
            }else if(isec1==11){
                icomponent = swapSectors(icomponent, 10);
                icomponent = reverseStripsInSector(icomponent);
            }else if(isec1==1){
                icomponent = reverseStripsInSector(icomponent);
            }
        }
        
       
        if(ilayer==4){            
            isec1 = findSector(icomponent);
            if(isec1==18){
                //icomponent = reverseStripsInSector(icomponent);
                if(icomponent>= 673){
                    icomponent -= 32;
                }else{
                    icomponent += 31;
                }
            }else if(isec1 == 5){
                icomponent = reverseStripsInSector(icomponent);
            }else if(isec1 == 13){
                //icomponent = reverseStripsInSector(icomponent);
            }else if(isec1 == 1){
                if(icomponent >= 128){
                    icomponent = 224 - icomponent;
                }
            }
        }
              

        if(ilayer==3){
            isec1 = findSector(icomponent);
             if(isec1==15){
                //icomponent = swapSectors(icomponent, 13);
            //    icomponent = swapSectors(icomponent, 15);
            }else if(isec1==14){
                icomponent = swapSectors(icomponent, 15);
                icomponent = reverseStripsInSector(icomponent);
            }else if(isec1==8){ 
                icomponent = reverseStripsInSector(icomponent);
            }
        }
        isec1 = findSector(icomponent);  // rehash  
        //icomponent = flipStripHorizontal(ilayer, icomponent);
        
        if(maskFaultySectors(ilayer, isec1) == 0) icomponent = -1;
    } 
    return icomponent;     
}

  
public int renumberFEE2RECRotatedAndAdjustWalnut(int run, int ilayer, int icomponent){
//  apply the renumbering schema - method 2 
    if(run>0){
        //System.out.println("icomponent in bank " + icomponent + " ilayer " + ilayer);    
        icomponent = renumberStrip(ilayer, icomponent);
        
        if(ilayer==1 || ilayer==4) icomponent = overturnModule(ilayer, icomponent);
        
        int isec1 = -1;
        if(ilayer==1){
            isec1 = findSector(icomponent);
            if(isec1==18){
                icomponent = reverseStripInFirstHalf(icomponent);
                //icomponent = reverseStripInSecondHalf(icomponent);   
            }else if(isec1==7){
                icomponent = swapSectors(icomponent, 6);
                icomponent = reverseStripsInSector(icomponent); //ok
            }else if(isec1==6){
                //icomponent = swapSectors(icomponent, 8);
                icomponent = swapSectors(icomponent, 7);
                icomponent = reverseStripsInSector(icomponent);
            }else if(isec1==8){
                //icomponent = swapSectors(icomponent, 7);    
            }else if(isec1==13){
                icomponent = reverseStripsInSector(icomponent);
            }else if(isec1==14){
                icomponent = reverseStripsInSector(icomponent);
            }else if(isec1 == 1){
                ///if(icomponent >= 96){
                ///    icomponent = 224 - icomponent;
                ///}//else{
                //    icomponent = 160 - icomponent;
                //}
                icomponent = reverseStripInSecondHalf(icomponent);
            }
            //if(isec1==18){
             //   icomponent = swapSectors(icomponent, 19);
                  //icomponent = reverseStripsInSector(icomponent);
                  //if(icomponent>672){
                  //     icomponent -= 32;
                  //}else{
                  //     icomponent += 32;
                  //}
            //}else if(isec1==19){
            //    icomponent = swapSectors(icomponent, 18);
            //}
        }
        
        if(ilayer==2){
            isec1 = findSector(icomponent);
            if(isec1==10){
                //icomponent = swapSectors(icomponent, 11);
                //icomponent = reverseStripsInSector(icomponent);
            }else if(isec1==11){
                //icomponent = swapSectors(icomponent, 10);
                //icomponent = reverseStripsInSector(icomponent);
            }else if(isec1==1){
                icomponent = reverseStripsInSector(icomponent);
                //if(icomponent<=96) icomponent += 32;
                //icomponent = reverseStripInSecondHalf(icomponent);
                if(icomponent>96) icomponent -= 8;
            }else if(isec1==3){
                icomponent = reverseStripsInSector(icomponent);
                //if(icomponent<=96) icomponent += 32;
                //icomponent = reverseStripInSecondHalf(icomponent);
            }else if(isec1==18){
                //icomponent = switchStripOff();
                //if(icomponent > 672) icomponent = swapSectors(icomponent, 19);
                ///if(icomponent>= 673){
                ///    icomponent -= 32;
                ///}else{
                ///    icomponent += 31;
                ///}
                //if(icomponent > 672){ 
                //    icomponent = 1377-icomponent;
                //}else{
                //    icomponent = 1312-icomponent;
                //}
                //    icomponent = reverseStripsInSector(icomponent);
            }else if(isec1==19){
                //icomponent = switchStripOff();
                //if(icomponent <= 736) icomponent -= 32;
            }else if(isec1==0){
                //icomponent = swapSectors(icomponent, 1);
                //if(icomponent > 48 && icomponent <= 64) icomponent += 16;
                //if(icomponent > 32) icomponent = -1;
                //icomponent = reverseStripInSecondHalf(icomponent);
            }else if(isec1==12){ // strip 476 only is fired
                //icomponent += 16;
            }
        }
        
       
        if(ilayer==3){     
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
//            if(isec1 == 1 || isec1==6 || isec1 == 10) icomponent = -1;
///              if(isec1 != 8) icomponent = -1; 
            if(isec1==15){
                //icomponent = swapSectors(icomponent, 13);
                //    icomponent = swapSectors(icomponent, 15);
                icomponent = swapSectors(icomponent, 14);
                icomponent = reverseStripsInSector(icomponent);
            }else if(isec1==14){
                icomponent = swapSectors(icomponent, 15);
                icomponent = reverseStripsInSector(icomponent);
            }else if(isec1==7){
                //icomponent = swapSectors(icomponent, 8);
                //icomponent = reverseStripsInSector(icomponent);
            }else if(isec1==8){
                icomponent = reverseStripsInSector(icomponent);  
            }else if(isec1==0){ //swap half of sector 0
                //if(icomponent>=33){
                //    icomponent -= 32;
                //}else{
                //    icomponent += 31;
            //}else if(isec1==18){
            //    if(icomponent < 673) icomponent = swapSectors(icomponent, 17);
            //}else if(isec1==17){
            //    icomponent = swapSectors(icomponent, 18);
            }else if(isec1==2){
            //    icomponent = swapSectors(icomponent, 3);
            //}else if(isec1==3){
            //    icomponent = swapSectors(icomponent, 2);
            }else if(isec1==10){
                  //icomponent = swapSectors(icomponent, 9);
            }else if(isec1==11){
                //icomponent = switchStripOff();
                //icomponent = swapSectors(icomponent, 9);
            }
             /*
            isec1 = findSector(icomponent);  // rehash
            if(isec1==13){
                icomponent = swapSectors(icomponent, 15);
            }
             */
        }
        
        if(ilayer==4){            
            isec1 = findSector(icomponent);
            if(isec1==18){
                ////icomponent = reverseStripsInSector(icomponent);
                //if(icomponent>= 673){   // qui sembra che faccia swap tra prima e seconda meta'
                //    icomponent -= 32;
                //}else{
                //    icomponent += 31;
                //}
                icomponent = swapHalves(icomponent);
                icomponent = reverseStripInSecondHalf(icomponent);
            }else if(isec1 == 15){
                //component = swapSectors(icomponent, 14);
                //icomponent = reverseStripsInSector(icomponent);
            }else if(isec1 == 5){
                icomponent = reverseStripsInSector(icomponent);
            }else if(isec1 == 13){
                //icomponent = reverseStripsInSector(icomponent);
            }else if(isec1 == 1){
                icomponent = reverseStripInSecondHalf(icomponent);
            }else if(isec1 == 6){
                icomponent = reverseStripsInSector(icomponent);
            }
        }
 

        isec1 = findSector(icomponent);  // rehash  
        //icomponent = flipStripHorizontal(ilayer, icomponent);
        
        //if(maskFaultySectors(ilayer, isec1) == 0) icomponent = -1;
        /*
        // select bad crosses rejecting all other strips
        int newsec = findSector(icomponent);
        if(ilayer==1){
            if(newsec != 3) icomponent = -1;
        }else if(ilayer==2){
            if(newsec != 14); icomponent = -1;
        }else{
            icomponent = -1;
        }
        */
    }

    return icomponent;     
}


}



