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

    public int debugMode = 0;

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
    	// cluster finding algorithm
	// the loop is done over sectors 

	int Nlayers = FTTRKConstantsLoader.Nlayers;
	int Nstrips = FTTRKConstantsLoader.Nstrips;
        /*
        int logStripsBottom[] = FTTRKConstantsLoader.longStripsDown;
        int longStripsUp[] =  FTTRKConstantsLoader.longStripsUp;
        int shortStripsLeft[] = FTTRKConstantsLoader.shortStripsLeft;
        int shortStripsRight[] = FTTRKConstantsLoader.shortStripsRight;
        */
	boolean[][] checked;
	FTTRKHit[][] HitArray;        
        ArrayList<FTTRKCluster> clusters = new ArrayList<FTTRKCluster>();

        // 
        
        
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

            if(w>0 && w<=Nstrips)	{						
                    HitArray[w-1][l-1] = hit;
                    if(debugMode>=1) System.out.println(w + " " + l + " " + HitArray[w-1][l-1].printInfo());
            }

        }
        int cid = 1;  // cluster id, will increment with each new good cluster
        int cidLim = 1;

        // for each layer and sector, a loop over the strips
        // is done to define clusters in that module's layer
        // clusters are delimited by strips with no hits
        
        // divide the layer geometry in two parts: U part with contiguous numbering, I part is the insert on the other side coresponding to the hole section
        int nUStrips = 2*FTTRKConstantsLoader.Longstrips + FTTRKConstantsLoader.SideHalfstrips;
        int nIStrips = FTTRKConstantsLoader.SideHalfstrips;
        
        for(int il=0; il<Nlayers; il++) {		
//            int is  = 0;  // strip index in the loop
            // looping over strip regions. Start with U strips, then I strips, then check contiguity for extremal strips
            boolean isUstripAtLimit = false, isIstripAtLimit = false;
            for(int is = 0; is < nUStrips; is++){
                // iss: physical strip number (not consecutive)
                // is: strip ordering number
                // loop on U stips (1-384 + 641-768)
                int iss = FTTRKConstantsLoader.UStrips[is];
                if(HitArray[iss][il] != null && !checked[is][il]){
                    // how many strips in the cluster
                    int nst = 1;
                    // if there is a strip start to store it as a cluster seed
                    ArrayList<FTTRKHit> clusterHits = new ArrayList<FTTRKHit>();
                    
                    // adding all hits in this and all the subsequent
                    // strip until there's a strip with no hit
                    int issnext = iss+1;
                    int isnext = is+1;
                    // check if the next strip is hit (but only if it's not the last one on the layer)
                    while(HitArray[issnext][il] != null  && issnext<Nstrips) {
                        nst++;
                        checked[issnext][il]=true;
                        clusterHits.add(new FTTRKHit(HitArray[issnext][il].get_Sector(),HitArray[issnext][il].get_Layer(),
                                HitArray[issnext][il].get_Strip(),HitArray[issnext][il].get_Edep()));
                        // check if the hit strip is at the border of U/I regions
                        if((issnext==FTTRKConstantsLoader.Longstrips-1) || 
                                (issnext==(FTTRKConstantsLoader.Longstrips+FTTRKConstantsLoader.SideHalfstrips-1))) 
                            isUstripAtLimit=true;
                        if(debugMode>=1) System.out.println(issnext);
                        issnext++; 
                        isnext++;
//                        if(issnext == )
//                        if(issnext!=Nstrips){issnext++;}else{System.out.println("dove vai ? " + is + " " + iss); break;}
                    }
                    // define new cluster
                    if(isUstripAtLimit){
                        FTTRKCluster clusterAtLimit = new FTTRKCluster(1, il+1, cidLim++); 
                    }else{
                        // add hits to the cluster
                        FTTRKCluster this_cluster = new FTTRKCluster(1, il+1, cid++);  
                        this_cluster.addAll(clusterHits);
                        this_cluster.calc_CentroidParams();
                        //make arraylist
                        clusters.add(this_cluster);
                        if(debugMode>=1) System.out.println((iss-1) + " cluster identifier in FTTRKReco, UStrip section " + cid + " layer " + il);
                    }        
                }   
            }
            
            /* 
            // loop on Istrips (half strips on one side of the hole: 385-640)
            for(int is=0; is < nIStrips; is++){
            int iss = FTTRKConstantsLoader.IStrips[is];
                if(HitArray[iss][il] != null && !checked[is][il]){
                    // how many strips in the cluster
                    int nst = 1;
                    // if there is a strip start to store it as a cluster seed
                    ArrayList<FTTRKHit> clusterHits = new ArrayList<FTTRKHit>();
                    
                    // adding all hits in this and all the subsequent
                    // strip until there's a strip with no hit
                    int issnext = iss+1;
                    int isnext = is+1;
                    // check if the next strip is hit (but only if it's not the last one on the layer)
                    while(HitArray[issnext][il] != null  && isnext<nIStrips) {
                        nst++;
                        checked[issnext][il]=true;
                        clusterHits.add(new FTTRKHit(HitArray[issnext][il].get_Sector(),HitArray[issnext][il].get_Layer(),
                                HitArray[issnext][il].get_Strip(),HitArray[issnext][il].get_Edep()));
                        // check if the hit strip is at the border of I regions
                        if((issnext==FTTRKConstantsLoader.Longstrips+FTTRKConstantsLoader.SideHalfstrips) || 
                                    (issnext==(FTTRKConstantsLoader.Longstrips+2*FTTRKConstantsLoader.SideHalfstrips-1)))
                                isIstripAtLimit=true;
                        issnext++; 
                        isnext++;
                  // define new cluster
                    if(isIstripAtLimit){
                        FTTRKCluster clusterAtLimit = new FTTRKCluster(1, il+1, cidLim++); 
                    }else{
                        // add hits to the cluster
                        FTTRKCluster this_cluster = new FTTRKCluster(1, il+1, cid++);  
                        this_cluster.addAll(clusterHits);
                        this_cluster.calc_CentroidParams();
                        //make arraylist
                        clusters.add(this_cluster);
                        System.out.println((iss-1) + " cluster identifier in FTTRKReco , IStrip section" + cid);
                    }        
                }   
            }
            */
            // if there is any extremal strip check whether there is a contiguity
            if(isUstripAtLimit){;  // do something - check if there is a limiting strip and join it to the next cluster (if any)
            }       
            if(isIstripAtLimit){;   // do something
            }
        
         
 
/*  old stuff to be deleted      
            // looping over all strips
            while(is<nstrip) {
                // if there's a hit, it's a cluster candidate
                if(HitArray[is][il] != null&&!checked[is][il])
                {
                    // vector of hits in the cluster candidate
                    ArrayList<FTTRKHit> clusterHits = new ArrayList<FTTRKHit>();

                    // adding all hits in this and all the subsequent
                    // strip until there's a strip with no hit
                    // Strip 1 and 513 needs a particular loop
                    // strips in bottom layer and in top layer have different numberings according to regions, beware of contiguity
                    // between different regions
   
                    nstripRight
                    if(il==1 || il==3){
                        // all strips
                        while(HitArray[is][il] != null  && is<nstrip) {
                            checked[is][il]=true;
                            clusterHits.add(new FTTRKHit(HitArray[is][il].get_Sector(),HitArray[is][il].get_Layer(),HitArray[is][il].get_Strip(),HitArray[is][il].get_Edep()));
                            if (is!=nstrip) is++; //Since strip 512 is on a edge
                            else break;
                        }        
                        // if there is a limiting strip check on the other side

                        
                    }else if(il==2 || il ==4){  
                    }
                    
*/                    
                    /*
                    if (is==0){
                        int js=832;
                        while(HitArray[js][il] != null  && js<nstrip) {
                            checked[js][il]=true;
                            clusterHits.add(new FTTRKHit(HitArray[js][il].get_Sector(),HitArray[js][il].get_Layer(),HitArray[js][il].get_Strip(),HitArray[js][il].get_Edep()));
                            js++;
                        }
                    }

                    if (is==512){
                        int sj=320;
                        while(HitArray[sj][il] != null  && sj<512) {
                            checked[sj][il]=true;
                            clusterHits.add(new FTTRKHit(HitArray[sj][il].get_Sector(),HitArray[sj][il].get_Layer(),HitArray[sj][il].get_Strip(),HitArray[sj][il].get_Edep()));
                            sj++;
                        }
                    }
*/
                    //For all strips
                    

             // if no hits, check for next wire coordinate
//                is++;
        

        }   // end loop on layers
        return clusters;
        
    }
    
    public ArrayList<FTTRKCross> findCrosses(List<FTTRKCluster> clusters) {

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
                    crosses.add(this_cross);
                    if(debugMode>=1) System.out.println(this_cross.printInfo() + " " + crosses.size());
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
