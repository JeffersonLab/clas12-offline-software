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
        IndexedTable status        = manager.getConstants(run, "/calibration/ft/ftcal/status");

        if(this.debugMode>=1) System.out.println("\nAnalyzing new event");
        List<FTCALHit> allhits = null;
        
        if(event instanceof EvioDataEvent) {
            allhits = this.readRawHits(event,charge2Energy,timeOffsets,timeWalk,cluster);
        }
        
        if(event instanceof HipoDataEvent) {
            allhits = this.readRawHitsHipo(event,charge2Energy,timeOffsets,timeWalk,cluster,status);
        }
        if(debugMode>=1) {
            System.out.println("Found " + allhits.size() + " hits");
            for(int i = 0; i < allhits.size(); i++) {
                System.out.print(i + "\t");
                allhits.get(i).show();
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
                hits.get(i).show();
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
            if(hit.get_ClusID()==0)  {                       // this hit is not yet associated with a cluster
                for(int jclus=0; jclus<clusters.size(); jclus++) {
                    FTCALCluster cluster = clusters.get(jclus);
                    if(cluster.containsHit(hit, thresholds, clusterTable)) {
                        hit.set_ClusID(cluster.getID());     // attaching hit to previous cluster 
                        cluster.add(hit);
                        if(debugMode>=1) System.out.println("Attaching hit " + ihit + " to cluster " + cluster.getID());
                        break;
                    }
                }
            }
            if(hit.get_ClusID()==0)  {                       // new cluster found
                FTCALCluster cluster = new FTCALCluster(clusters.size()+1);
                hit.set_ClusID(cluster.getID());
                cluster.add(hit);
                clusters.add(cluster);
                if(debugMode>=1) System.out.println("Creating new cluster with ID " + cluster.getID());
            }
        }
        return clusters;
    }
       
    public void selectClusters(List<FTCALCluster> clusters, ConstantsManager manager, int run) {

        IndexedTable   clusterTable = manager.getConstants(run, "/calibration/ft/ftcal/cluster");
        
        for(int i=0; i<clusters.size(); i++) {
            clusters.get(i).setStatus(clusterTable);
            if(debugMode>=1) System.out.println("Setting status for cluster " + i + " " + clusters.get(i).toString());
        }
        return;
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
                if(clusters.size()>0 && clusters.get(hits.get(i).get_ClusID()-1).getStatus()) {
                    bankHits.setShort("clusterID",i,(short) hits.get(i).get_ClusID());
                }
                else {
                    bankHits.setShort("clusterID",i,(short) 0);
                }
            }	
            if(debugMode>=1) bankHits.show();
            event.appendBanks(bankHits);
        }
        // cluster bank
        if(clusters.size()!=0) {
            List<FTCALCluster> selectedClusters  = new ArrayList();
            for(int i =0; i< clusters.size(); i++) {
                if(clusters.get(i).getStatus()) selectedClusters.add(clusters.get(i));
            }
            if(selectedClusters.size()>0) {                
                DataBank bankCluster = event.createBank("FTCAL::clusters", selectedClusters.size());    
                if(bankCluster==null){
                    System.out.println("ERROR CREATING BANK : FTCAL::clusters");
                    return;
                }
                for(int i = 0; i < selectedClusters.size(); i++){
                    bankCluster.setShort("id", i,(short) selectedClusters.get(i).getID());
                    bankCluster.setShort("size", i,(short) selectedClusters.get(i).getSize());
                    bankCluster.setFloat("x",i,(float) (selectedClusters.get(i).getX()/10.0));
                    bankCluster.setFloat("y",i, (float) (selectedClusters.get(i).getY()/10.0));
                    bankCluster.setFloat("z",i, (float) (selectedClusters.get(i).getZ()/10.0));
                    bankCluster.setFloat("widthX",i, (float) (selectedClusters.get(i).getWidthX()/10.0));
                    bankCluster.setFloat("widthY",i, (float) (selectedClusters.get(i).getWidthY()/10.0));
                    bankCluster.setFloat("radius",i, (float) (selectedClusters.get(i).getRadius()/10.0));
                    bankCluster.setFloat("time",i, (float) selectedClusters.get(i).getTime());
                    bankCluster.setFloat("energy",i, (float) selectedClusters.get(i).getFullEnergy(energyTable));
                    bankCluster.setFloat("recEnergy",i, (float) selectedClusters.get(i).getEnergy());
                    bankCluster.setFloat("maxEnergy",i, (float) selectedClusters.get(i).getSeedEnergy());                   
                }
                if(debugMode>=1) bankCluster.show();
                event.appendBanks(bankCluster);
            }
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
                        bankhits.setInt("hitClusterIndex",i,hits.get(i).get_ClusID());	
                }				
        }	
        // cluster bank
        if(clusters.size()!=0){
                bankclust = (EvioDataBank) event.getDictionary().createBank("FTCALRec::clusters",clusters.size());
                for(int i =0; i< clusters.size(); i++) {
                        if(debugMode>=1) clusters.get(i).show();
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
    
    public List<FTCALHit> readRawHitsHipo(DataEvent event, IndexedTable charge2Energy, IndexedTable timeOffsets, IndexedTable timeWalk, IndexedTable cluster, IndexedTable status) {
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
                if(adc!=-1 && time!=-1 && status.getIntValue("status", isector, ilayer, icomponent)==0){
                    FTCALHit hit = new FTCALHit(row,icomponent, adc, time, charge2Energy, timeOffsets, timeWalk, cluster);
                    
                    ////////////////////////////////////////////////////////////////////////////////////////////////////
                    // select here single/groups of crystals for debugging purposes
                    // examples:
	            /// if(icomponent>=90 && icomponent<=98) hits.add(hit); // select only crystals in a given region
                    // vertical strip of crystals, right side
                    /// if(icomponent == 61 || icomponent == 83 || icomponent == 105 || icomponent == 127 || 
                    ///    icomponent == 149 || icomponent == 193 || icomponent == 215 || icomponent == 237 || 
                    ///    icomponent == 259 || icomponent == 281 || icomponent == 303 || icomponent == 325 || 
                    ///    icomponent == 347 || icomponent == 369 || icomponent == 391 || icomponent == 413 || icomponent == 435)  
                    // vertical strip of crystals, left side
                    ///if(icomponent == 27 || icomponent == 49 || icomponent == 71 || icomponent == 93 || 
                    ///    icomponent == 115 || icomponent == 137 || icomponent == 159 || icomponent == 181 || 
                    ///    icomponent == 203 || icomponent == 225 || icomponent == 247 || icomponent == 269 || 
                    ///    icomponent == 291 || icomponent == 313 || icomponent == 335 || icomponent == 357 || 
                    ///    icomponent == 379 || icomponent == 401 || icomponent == 423 || icomponent == 445 )  
                    // vertical strip of crystals, left side central
                    ///if(icomponent == 10 || icomponent == 32 || icomponent == 54 || icomponent == 76 || 
                    ///    icomponent == 98 || icomponent == 120 || icomponent == 142 || icomponent == 340 || 
                    ///    icomponent == 362 || icomponent == 384 || icomponent == 406 || icomponent == 428 || 
                    ///    icomponent == 450 || icomponent == 472 )  
                    // vertical strip of crystals, right side central
                    ///if(icomponent == 11 || icomponent == 33 || icomponent == 55 || icomponent == 77 || 
                    ///    icomponent == 99 || icomponent == 121 || icomponent == 143 || icomponent == 341 || 
                    ///    icomponent == 363 || icomponent == 385 || icomponent == 407 || icomponent == 429 || 
                    ///    icomponent == 451 || icomponent == 473 )  
                    // top half 
                    /// if(icomponent >= 242)
                    // bottom half
                    ///  if(icomponent <= 241)
                    
                    hits.add(hit);
	        }	          
            }
        }
        return hits;
    }
    
}
