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
    
    public List<FTCALHit> selectHits(List<FTCALHit> allhits) {

        if(debugMode>=1) System.out.println("\nSelecting hits");
        ArrayList<FTCALHit> hits = new ArrayList<FTCALHit>();
        
        for(int i = 0; i < allhits.size(); i++) 
        {
                if(FTCALHit.passHitSelection(allhits.get(i))) {
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
        
        IndexedTable   clusterTable = manager.getConstants(run, "/calibration/ft/ftcal/cluster");
        
        if(debugMode>=1) System.out.println("\nBuilding clusters");
        for(int ihit=0; ihit<hits.size(); ihit++) {
            FTCALHit hit = hits.get(ihit);
            if(hit.get_ClusIndex()==0)  {                       // this hit is not yet associated with a cluster
                for(int jclus=0; jclus<clusters.size(); jclus++) {
                    FTCALCluster cluster = clusters.get(jclus);
                    if(cluster.containsHit(hit, clusterTable)) {
                        hit.set_ClusIndex(cluster.getID());     // attaching hit to previous cluster 
                        cluster.add(hit);
                        if(debugMode>=1) System.out.println("Attaching hit " + ihit + " to cluster " + cluster.getID());
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
	             hits.add(hit); 
	        }	          
            }
        }
        return hits;
    }
    
}
