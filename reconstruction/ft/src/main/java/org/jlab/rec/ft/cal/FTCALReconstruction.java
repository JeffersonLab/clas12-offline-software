package org.jlab.rec.ft.cal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.utils.groups.IndexedTable;


public class FTCALReconstruction {

	
    public int debugMode = 0;

    public FTCALReconstruction() {
    }
	
    public List<FTCALHit> initFTCAL(DataEvent event, ConstantsManager manager, int run) {

        IndexedTable charge2Energy = manager.getConstants(run, "/calibration/ft/ftcal/charge_to_energy");
        IndexedTable timeOffsets   = manager.getConstants(run, "/calibration/ft/ftcal/time_offsets");
        IndexedTable timeWalk      = manager.getConstants(run, "/calibration/ft/ftcal/time_walk");
        IndexedTable cluster       = manager.getConstants(run, "/calibration/ft/ftcal/cluster");

        if(this.debugMode>=1) System.out.println("\nAnalyzing new event");
        List<FTCALHit> allhits = null;
        
        if(event instanceof EvioDataEvent) {
            allhits = this.readRawHits(event,charge2Energy,timeOffsets,timeWalk,cluster);
        }
        
        if(event instanceof HipoDataEvent) {
            allhits = this.readRawHitsHipo(event,charge2Energy,timeOffsets,timeWalk,cluster);
        }
        if(debugMode>=1) {
            System.out.println("Found " + allhits.size() + " hits");
            for(int i = 0; i < allhits.size(); i++) {
                System.out.print(i + "\t");
                allhits.get(i).showHit();
            }
        }
        return allhits;
    }
    
    public List<FTCALHit> selectHits(List<FTCALHit> allhits, ConstantsManager manager, int run) {

        if(debugMode>=1) System.out.println("\nSelecting hits");
        ArrayList<FTCALHit> hits = new ArrayList<FTCALHit>();
        
        IndexedTable thresholds = manager.getConstants(run, "/calibration/ft/ftcal/thresholds");

        for(int i = 0; i < allhits.size(); i++) 
        {
                if(FTCALHit.passHitSelection(allhits.get(i), thresholds)) {
                        hits.add(allhits.get(i));	
                }
        }	
        Collections.sort(hits);
        if(debugMode>=1) {
            System.out.println("List of selected hits");
            for(int i = 0; i < hits.size(); i++) 
            {	
                System.out.print(i + "\t");
                hits.get(i).showHit();
            }
        }
        return hits;
    }
			
    public List<FTCALCluster> findClusters(List<FTCALHit> hits, ConstantsManager manager, int run) {

        List<FTCALCluster> clusters = new ArrayList();
        
        IndexedTable   thresholds   = manager.getConstants(run, "/calibration/ft/ftcal/thresholds");
        IndexedTable   clusterTable = manager.getConstants(run, "/calibration/ft/ftcal/cluster");
        
        if(debugMode>=1) System.out.println("\nBuilding clusters");
        for(int ihit=0; ihit<hits.size(); ihit++) {
            FTCALHit hit = hits.get(ihit);
            if(hit.get_ClusIndex()==0)  {                       // this hit is not yet associated with a cluster
                for(int jclus=0; jclus<clusters.size(); jclus++) {
                    FTCALCluster cluster = clusters.get(jclus);
                    if(cluster.containsHit(hit, thresholds, clusterTable)) {
                        hit.set_ClusIndex(cluster.getID());     // attaching hit to previous cluster 
                        cluster.add(hit);
                        if(debugMode>=1) System.out.println("Attaching hit " + ihit + " to cluster " + cluster.getID());
                        break;
                    }
                }
            }
            if(hit.get_ClusIndex()==0)  {                       // new cluster found
                FTCALCluster cluster = new FTCALCluster(clusters.size()+1);
                hit.set_ClusIndex(cluster.getID());
                cluster.add(hit);
                clusters.add(cluster);
                if(debugMode>=1) System.out.println("Creating new cluster with ID " + cluster.getID());
            }
        }
        return clusters;
    }
       
    public List<FTCALCluster> selectClusters(List<FTCALCluster> allclusters, ConstantsManager manager, int run) {

        IndexedTable   clusterTable = manager.getConstants(run, "/calibration/ft/ftcal/cluster");
        
        List<FTCALCluster> clusters = new ArrayList();
        for(int i=0; i<allclusters.size(); i++) {
            if(allclusters.get(i).isgoodCluster(clusterTable)) clusters.add(allclusters.get(i));
        }
        return clusters;
    }
        
    
    public void writeBanks(DataEvent event, List<FTCALHit> hits, List<FTCALCluster> clusters, ConstantsManager manager, int run){

        IndexedTable   energyTable = manager.getConstants(run, "/calibration/ft/ftcal/energycorr");

        if(event instanceof EvioDataEvent) {
            writeEvioBanks(event, hits, clusters, energyTable);
        }
        else if(event instanceof HipoDataEvent) {
            writeHipoBanks(event, hits, clusters, energyTable);
        }
    }
    
    private void writeHipoBanks(DataEvent event, List<FTCALHit> hits, List<FTCALCluster> clusters, IndexedTable energyTable){
        
        // hits banks
        if(hits.size()!=0) {
            DataBank bankHits = event.createBank("FTCAL::hits", hits.size());    
            if(bankHits==null){
                System.out.println("ERROR CREATING BANK : FTCAL::hits");
                return;
            }
            for(int i = 0; i < hits.size(); i++){
                bankHits.setByte("idx",i,(byte) hits.get(i).get_IDX());
                bankHits.setByte("idy",i,(byte) hits.get(i).get_IDY());
                bankHits.setFloat("x",i,(float) (hits.get(i).get_Dx()/10.0));
                bankHits.setFloat("y",i,(float) (hits.get(i).get_Dy()/10.0));
                bankHits.setFloat("z",i,(float) (hits.get(i).get_Dz()/10.0));
                bankHits.setFloat("energy",i,(float) hits.get(i).get_Edep());
                bankHits.setFloat("time",i,(float) hits.get(i).get_Time());
                bankHits.setShort("hitID",i,(short) hits.get(i).get_DGTZIndex());
                bankHits.setShort("clusterID",i,(short) hits.get(i).get_ClusIndex());				
            }	
            event.appendBanks(bankHits);
        }
        // cluster bank
        if(clusters.size()!=0) {
            DataBank bankCluster = event.createBank("FTCAL::clusters", clusters.size());    
            if(bankCluster==null){
                System.out.println("ERROR CREATING BANK : FTCAL::clusters");
                return;
            }
            for(int i = 0; i < clusters.size(); i++){
                            bankCluster.setShort("id", i,(short) clusters.get(i).getID());
                            bankCluster.setShort("size", i,(short) clusters.get(i).getSize());
                            bankCluster.setFloat("x",i,(float) (clusters.get(i).getX()/10.0));
                            bankCluster.setFloat("y",i, (float) (clusters.get(i).getY()/10.0));
                            bankCluster.setFloat("z",i, (float) (clusters.get(i).getZ()/10.0));
                            bankCluster.setFloat("widthX",i, (float) (clusters.get(i).getWidthX()/10.0));
                            bankCluster.setFloat("widthY",i, (float) (clusters.get(i).getWidthY()/10.0));
                            bankCluster.setFloat("radius",i, (float) (clusters.get(i).getRadius()/10.0));
                            bankCluster.setFloat("time",i, (float) clusters.get(i).getTime());
                            bankCluster.setFloat("energy",i, (float) clusters.get(i).getFullEnergy(energyTable));
                            bankCluster.setFloat("recEnergy",i, (float) clusters.get(i).getEnergy());
                            bankCluster.setFloat("maxEnergy",i, (float) clusters.get(i).getSeedEnergy());
            }
            event.appendBanks(bankCluster);
        }
    }
    


    private void writeEvioBanks(DataEvent event, List<FTCALHit> hits, List<FTCALCluster> clusters, IndexedTable energyTable) {
                          
        EvioDataBank bankhits  = null;
        EvioDataBank bankclust = null;
		
        // hits banks
        if(hits.size()!=0) {
                bankhits = (EvioDataBank) event.getDictionary().createBank("FTCALRec::hits",hits.size());
                for(int i=0; i<hits.size(); i++) {
                        bankhits.setInt("idx",i,hits.get(i).get_IDX());
                        bankhits.setInt("idy",i,hits.get(i).get_IDY());
                        bankhits.setDouble("hitX",i,hits.get(i).get_Dx()/10.0);
                        bankhits.setDouble("hitY",i,hits.get(i).get_Dy()/10.0);
                        bankhits.setDouble("hitEnergy",i,hits.get(i).get_Edep());
                        bankhits.setDouble("hitTime",i,hits.get(i).get_Time());
                        bankhits.setInt("hitDGTZIndex",i,hits.get(i).get_DGTZIndex());
                        bankhits.setInt("hitClusterIndex",i,hits.get(i).get_ClusIndex());	
                }				
        }	
        // cluster bank
        if(clusters.size()!=0){
                bankclust = (EvioDataBank) event.getDictionary().createBank("FTCALRec::clusters",clusters.size());
                for(int i =0; i< clusters.size(); i++) {
                        if(debugMode>=1) clusters.get(i).showCluster();
                        bankclust.setInt("clusID", i,clusters.get(i).getID());
                        bankclust.setInt("clusSize", i,clusters.get(i).getSize());
                        bankclust.setDouble("clusX",i,clusters.get(i).getX()/10.0);
                        bankclust.setDouble("clusY",i,clusters.get(i).getY()/10.0);
                        bankclust.setDouble("clusXX",i,clusters.get(i).getX2()/100.0);
                        bankclust.setDouble("clusYY",i,clusters.get(i).getY2()/100.0);
                        bankclust.setDouble("clusSigmaX",i,clusters.get(i).getWidthX()/10.0);
                        bankclust.setDouble("clusSigmaY",i,clusters.get(i).getWidthY()/10.0);
                        bankclust.setDouble("clusRadius",i,clusters.get(i).getRadius()/10.0);
                        bankclust.setDouble("clusTime",i,clusters.get(i).getTime());
                        bankclust.setDouble("clusEnergy",i,clusters.get(i).getFullEnergy(energyTable));
                        bankclust.setDouble("clusRecEnergy",i,clusters.get(i).getEnergy());
                        bankclust.setDouble("clusMaxEnergy",i,clusters.get(i).getSeedEnergy());
                        bankclust.setDouble("clusTheta",i,clusters.get(i).getTheta());
                        bankclust.setDouble("clusPhi",i,clusters.get(i).getPhi());				
                }

                // If there are no clusters, punt here but save the reconstructed hits 
                if(bankclust!=null) {
                        event.appendBanks(bankhits,bankclust);
                }
                else if (bankhits!=null) {
                        event.appendBank(bankhits);
                }
        }
    }

    public List<FTCALHit> readRawHits(DataEvent event, IndexedTable charge2Energy, IndexedTable timeOffsets, IndexedTable timeWalk, IndexedTable cluster) {
        // getting raw data bank
	if(debugMode>=1) System.out.println("Getting raw hits from FTCAL:dgtz bank");

        List<FTCALHit>  hits = new ArrayList<FTCALHit>();
	if(event.hasBank("FTCAL::dgtz")==true) {
            EvioDataBank bankDGTZ = (EvioDataBank) event.getBank("FTCAL::dgtz");
            int nrows = bankDGTZ.rows();
            for(int row = 0; row < nrows; row++){
                int isector     = bankDGTZ.getInt("sector",row);
                int ilayer      = bankDGTZ.getInt("layer",row);
                int icomponent  = bankDGTZ.getInt("component",row);
                int adc         = bankDGTZ.getInt("ADC",row);
                int tdc         = bankDGTZ.getInt("TDC",row);
                if(adc!=-1 && tdc!=-1){
                    FTCALHit hit = new FTCALHit(row,icomponent, adc, tdc, charge2Energy, timeOffsets, timeWalk, cluster);
	             hits.add(hit); 
	        }	          
            }
        }
        return hits;
    }
    
    public List<FTCALHit> readRawHitsHipo(DataEvent event, IndexedTable charge2Energy, IndexedTable timeOffsets, IndexedTable timeWalk, IndexedTable cluster) {
        // getting raw data bank
	if(debugMode>=1) System.out.println("Getting raw hits from FTCAL:adc bank");

        List<FTCALHit>  hits = new ArrayList<FTCALHit>();
	if(event.hasBank("FTCAL::adc")==true) {
            DataBank bankDGTZ = event.getBank("FTCAL::adc");
            int nrows = bankDGTZ.rows();
            for(int row = 0; row < nrows; row++){
                int isector     = bankDGTZ.getByte("sector",row);
                int ilayer      = bankDGTZ.getByte("layer",row);
                int icomponent  = bankDGTZ.getShort("component",row);
                int iorder      = bankDGTZ.getByte("order",row);
                int adc         = bankDGTZ.getInt("ADC",row);
                float time      = bankDGTZ.getFloat("time",row);
                if(adc!=-1 && time!=-1){
                    FTCALHit hit = new FTCALHit(row,icomponent, adc, time, charge2Energy, timeOffsets, timeWalk, cluster);
//	            if(icomponent>=90 && icomponent<=98) hits.add(hit); // select only crystals in a given region
//                    if(icomponent>= 69 && icomponent<=84)
// vertical strip of crystals, right side
/*
                     if(icomponent == 61 || icomponent == 83 || icomponent == 105 || icomponent == 127 || 
                        icomponent == 149 || icomponent == 193 || icomponent == 215 || icomponent == 237 || 
                        icomponent == 259 || icomponent == 281 || icomponent == 303 || icomponent == 325 || 
                        icomponent == 347 || icomponent == 369 || icomponent == 391 || icomponent == 413 || icomponent == 435)  
*/
// vertical strip of crystals, left side
/*
                    if(icomponent == 27 || icomponent == 49 || icomponent == 71 || icomponent == 93 || 
                        icomponent == 115 || icomponent == 137 || icomponent == 159 || icomponent == 181 || 
                        icomponent == 203 || icomponent == 225 || icomponent == 247 || icomponent == 269 || 
                        icomponent == 291 || icomponent == 313 || icomponent == 335 || icomponent == 357 || 
                        icomponent == 379 || icomponent == 401 || icomponent == 423 || icomponent == 445 )  
*/
// vertical strip of crystals, left side central
/*
                    if(icomponent == 10 || icomponent == 32 || icomponent == 54 || icomponent == 76 || 
                        icomponent == 98 || icomponent == 120 || icomponent == 142 || icomponent == 340 || 
                        icomponent == 362 || icomponent == 384 || icomponent == 406 || icomponent == 428 || 
                        icomponent == 450 || icomponent == 472 )  
 */                       
// vertical strip of crystals, right side central
/*
                    if(icomponent == 11 || icomponent == 33 || icomponent == 55 || icomponent == 77 || 
                        icomponent == 99 || icomponent == 121 || icomponent == 143 || icomponent == 341 || 
                        icomponent == 363 || icomponent == 385 || icomponent == 407 || icomponent == 429 || 
                        icomponent == 451 || icomponent == 473 )  
*/
// top half 
//                    if(icomponent >= 242)
// bottom half
 //                   if(icomponent <= 241)

//                   if(icomponent == 92)
//                   if(icomponent == 105)
//                   if(icomponent == 400)
//                   if(icomponent == 412)
// select one vertical stip of crystals left of the hole
//                     if(icomponent == 472 || icomponent == 450 || icomponent == 428 || icomponent == 406 || icomponent == 384 ||
//                        icomponent == 362 || icomponent == 340)
// select one vertical strip of crystals right of the hole
//                    if(icomponent == 473 || icomponent == 451 || icomponent == 429 || icomponent == 407 || icomponent == 385 ||
//                        icomponent == 363 || icomponent == 341)
//                     if(icomponent == 454 || icomponent == 432 || icomponent == 410 || icomponent == 388 || icomponent == 366 ||
//                        icomponent == 344 || icomponent == 322 || icomponent == 300)
//                    if(icomponent == 402 || icomponent == 401 || icomponent == 400 || icomponent == 380 || icomponent == 379 ||
//                        icomponent == 378 || icomponent == 358 || icomponent == 357 || icomponent == 356)
//                   if(icomponent == 381 || icomponent == 380 || icomponent == 379 || icomponent == 359 || icomponent == 358 ||
//                        icomponent == 357 || icomponent == 337 || icomponent == 336 || icomponent == 335)  
//                    if(icomponent == 340 || icomponent == 339 || icomponent == 316 || icomponent == 293 || icomponent == 270 ||
//                        icomponent == 248)  
//                    if(icomponent == 341 || icomponent == 342 || icomponent == 321 || icomponent == 300 || icomponent == 279 ||
//                        icomponent == 257)  
//                    if(icomponent == 226 || icomponent == 204 || icomponent == 183 || icomponent == 162 || icomponent == 141  ||
//                        icomponent == 142) 
//                    if(icomponent == 235 || icomponent == 213 || icomponent == 190 || icomponent == 167 || icomponent == 144  ||
//                        icomponent == 143) 
//                      if(icomponent == 241 || icomponent == 218 || icomponent == 195 || icomponent == 172 || icomponent == 149  ||
//                        icomponent == 126 || icomponent == 103 || icomponent == 80 || icomponent == 57 || icomponent == 34 || 
//                        icomponent == 11)
//                     if(icomponent >=341 && icomponent<=350)
 //                     if(icomponent==347) 
                   hits.add(hit);
	        }	          
            }
        }
        return hits;
    }
    
}
