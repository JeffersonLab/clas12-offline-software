/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.ft.trk;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author devita
 * @author filippi
 */
public class FTTRKReconstruction {

    public static int debugMode = 0;

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
        FTTRKHit[] clusterHitAtBorder = new FTTRKHit[3];
        int[] limitingStrip = {127, 383, 639};  // strip numbering from 0 to 767
        
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
        int cid = 0;  // cluster id, will increment with each new good cluster

        // for each layer and sector, loop over the strips:
        // once one is selected for the first time, loop over the remainder
        // clusters are delimited by strips with no hits
        
        for(int il=0; il<Nlayers; il++) {
            // loop on first strip		
            for(int is = 0; is < Nstrips; is++){
                ArrayList<FTTRKHit> clusterHits = new ArrayList<FTTRKHit>();
                int nst=0;
                if(HitArray[is][il] != null && !checked[is][il]){
                    // select one fired strip 
                    checked[is][il]=true;
                    // if it's fired it is the seed of the cluster (which may be formed by one stip only
                    clusterHits.add(new FTTRKHit(HitArray[is][il].get_Sector(),HitArray[is][il].get_Layer(),
                                HitArray[is][il].get_Strip(),HitArray[is][il].get_Edep()));
                    if(debugMode>=1) System.out.println("hitArray " + HitArray[is][il].printInfo());
                    // now check if there are tother strips to be added to the cluster
                    int isnext = is;
                    // is it a limiting strip?
                    if(is==limitingStrip[0]){ // strip 127: next one can be 384 or 128
                        if(HitArray[limitingStrip[0]+1][il] != null){
                            isnext++;   
                        }else if(HitArray[limitingStrip[1]+1][il] != null){
                            isnext = limitingStrip[1]+1;
                        }
                        while(HitArray[isnext][il] != null && isnext<Nstrips){
                            nst++;
                            checked[isnext][il]=true;
                            clusterHits.add(new FTTRKHit(HitArray[isnext][il].get_Sector(),HitArray[isnext][il].get_Layer(),
                                HitArray[isnext][il].get_Strip(),HitArray[isnext][il].get_Edep()));   
                            isnext++;
                        }                        
                    }else if(is==limitingStrip[1]){
                        isnext = limitingStrip[2]+1;
                        while(HitArray[isnext][il] != null && isnext<Nstrips){
                            nst++;
                            checked[isnext][il]=true;
                            clusterHits.add(new FTTRKHit(HitArray[isnext][il].get_Sector(),HitArray[isnext][il].get_Layer(),
                                HitArray[isnext][il].get_Strip(),HitArray[isnext][il].get_Edep()));   
                            isnext++;
                        }
                    }else if(is==limitingStrip[2]){ // strip 383||639: next strip is 640
                        isnext = limitingStrip[2]+1;
                        while(HitArray[isnext][il] != null && isnext<Nstrips){
                            nst++;
                            checked[isnext][il]=true;
                            clusterHits.add(new FTTRKHit(HitArray[isnext][il].get_Sector(),HitArray[isnext][il].get_Layer(),
                                HitArray[isnext][il].get_Strip(),HitArray[isnext][il].get_Edep()));   
                            isnext++;
                        }
                    }else{ 
                    // the first strip is not at the border of any zone, check is other strips can be added to the cluster
                        while(HitArray[isnext][il] != null && isnext<Nstrips){
                            nst++;
                            checked[isnext][il]=true;
                            clusterHits.add(new FTTRKHit(HitArray[isnext][il].get_Sector(),HitArray[isnext][il].get_Layer(),
                                HitArray[isnext][il].get_Strip(),HitArray[isnext][il].get_Edep()));
                            // is the next strip at the borders?
                            if(isnext==limitingStrip[0] && !checked[isnext][il]){
                                if(HitArray[limitingStrip[0]+1] != null){
                                    isnext++;
                                }else if(HitArray[limitingStrip[1]+1] != null && !checked[isnext][il]){
                                    isnext = limitingStrip[1]+1;
                                }
                            }else if(isnext==limitingStrip[1]){
                                isnext = limitingStrip[2]+1; 
                            }else{  // covers the case of limitingStrip[2]
                                isnext++;
                            }
                        }
                    }
                    FTTRKCluster this_cluster = new FTTRKCluster(1, il+1, cid++);
                    this_cluster.addAll(clusterHits);
                    this_cluster.calc_CentroidParams();
                    clusters.add(this_cluster);
                    if(debugMode>=1) System.out.println("cluster number " + cid + " size " + clusterHits.size() + " layer " + this_cluster.get_Layer() + 
                            " " + this_cluster.get_MaxStrip() + "-" + this_cluster.get_MinStrip() + " " + this_cluster.get_Centroid()) ;
                    
                } // check on seed                   
            }   // end loop on strips
        }   // end loop on layers
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

        int rid =0;
        for(FTTRKCluster inlayerclus : allinnerlayrclus){
            for(FTTRKCluster outlayerclus : allouterlayrclus){
                if(outlayerclus.get_Layer()-inlayerclus.get_Layer()!=1)
                        continue;
                if(outlayerclus.get_Sector()!=inlayerclus.get_Sector())
                        continue;
                if(debugMode>=1) System.out.println(inlayerclus.printInfo() +  " " + outlayerclus.printInfo());
                if( (inlayerclus.get_MinStrip()+outlayerclus.get_MinStrip() > 1) 
                            && (inlayerclus.get_MaxStrip()+outlayerclus.get_MaxStrip() < FTTRKConstantsLoader.Nstrips*2) ) { // put correct numbers to make sure the intersection is valid

                    // define new cross 
                    FTTRKCross this_cross = new FTTRKCross(inlayerclus.get_Sector(), inlayerclus.get_Region(),rid++);
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
        return crosses;
    }

        public List<FTTRKHit> readRawHits(DataEvent event, IndexedTable charge2Energy, IndexedTable timeOffsets, IndexedTable geometry) {
        // getting raw data bank
	if(debugMode>=1) System.out.println("Getting raw hits from FTTRK:adc bank");

        List<FTTRKHit>  hits = new ArrayList<FTTRKHit>();
	if(event.hasBank("FTTRK::adc")==true) {
            DataBank bankDGTZ = event.getBank("FTTRK::adc");
            int nrows = bankDGTZ.rows();
            for(int row = 0; row < nrows; row++){
                int isector     = bankDGTZ.getInt("sector",row);
                int ilayer      = bankDGTZ.getInt("layer",row);
                int icomponent  = bankDGTZ.getInt("component",row);
                int iorder      = bankDGTZ.getInt("order",row);
                int adc         = bankDGTZ.getInt("ADC",row);
                float time      = bankDGTZ.getFloat("time",row);
                if(adc>0 && time!=-1 && icomponent!=-1){
                    FTTRKHit hit = new FTTRKHit(isector,ilayer,icomponent, (double) adc);
	            hits.add(hit); 
	        }	          
            }
        }
        return hits;
    }    
    
    
    public void writeBanks(DataEvent event, List<FTTRKHit> hits, List<FTTRKCluster> clusters, List<FTTRKCross> crosses){
        
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
                bankCluster.setShort("id",        i,(short) clusters.get(i).get_Id());
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
                bankCross.setShort("id",         j, (short) crosses.get(j).get_Id());
                bankCross.setByte("sector",      j, (byte)  crosses.get(j).get_Sector());
                bankCross.setByte("detector",    j, (byte)  crosses.get(j).get_Region());
                bankCross.setFloat("x",          j, (float) crosses.get(j).get_Point().x());
                bankCross.setFloat("y",          j, (float) crosses.get(j).get_Point().y());
                bankCross.setFloat("z",          j, (float) crosses.get(j).get_Point().z());
                bankCross.setShort("Cluster1ID", j, (short) crosses.get(j).get_Cluster1().get_Id());
                bankCross.setShort("Cluster2ID", j, (short) crosses.get(j).get_Cluster2().get_Id());
            }
            event.appendBanks(bankCross);
        }
    }
  

}
