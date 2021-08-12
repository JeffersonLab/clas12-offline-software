package org.jlab.rec.ft.hodo;

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

public class FTHODOReconstruction {


    public int debugMode = 0;

    public FTHODOReconstruction() {
    }
	
    public List<FTHODOHit> initFTHODO(DataEvent event, ConstantsManager manager, int run) {

        IndexedTable charge2Energy = manager.getConstants(run, "/calibration/ft/fthodo/charge_to_energy");
        IndexedTable timeOffsets   = manager.getConstants(run, "/calibration/ft/fthodo/time_offsets");
        IndexedTable status        = manager.getConstants(run, "/calibration/ft/fthodo/status");
        IndexedTable geometry      = manager.getConstants(run, "/geometry/ft/fthodo");
        
        if(debugMode>=1) System.out.println("\nAnalyzing new event");
        List<FTHODOHit> allhits = null;
        
        if(event instanceof EvioDataEvent) {
            allhits = this.readRawHits(event,charge2Energy,timeOffsets,geometry);
        }
        
        if(event instanceof HipoDataEvent) {
            allhits = this.readRawHitsHipo(event,charge2Energy,timeOffsets,status,geometry);
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
    
    public List<FTHODOHit> selectHits(List<FTHODOHit> allhits) {

        if(debugMode>=1) System.out.println("\nSelecting hits");
        ArrayList<FTHODOHit> hits = new ArrayList<FTHODOHit>();
        
        for(int i = 0; i < allhits.size(); i++) 
        {
                if(FTHODOHit.passHitSelection(allhits.get(i))) {
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

    public List<FTHODOCluster> findClusters(List<FTHODOHit> hits) {

        List<FTHODOCluster> clusters = new ArrayList();
        
        if(debugMode>=1) System.out.println("\nBuilding clusters");
        for(int ihit=0; ihit<hits.size(); ihit++) {
            FTHODOHit hit = hits.get(ihit);
            if(hit.get_ClusterIndex()==0)  {                       // this hit is not yet associated with a cluster
                for(int jclus=0; jclus<clusters.size(); jclus++) {
                    FTHODOCluster cluster = clusters.get(jclus);
                    if(cluster.containsHit(hit)) {
                        hit.set_ClusterIndex(cluster.getID());     // attaching hit to previous cluster 
                        cluster.add(hit);
                        if(debugMode>=1) System.out.println("Attaching hit " + ihit + " to cluster " + cluster.getID());
                    }
                }
            }
            if(hit.get_ClusterIndex()==0)  {                       // new cluster found
                FTHODOCluster cluster = new FTHODOCluster(clusters.size()+1);
                hit.set_ClusterIndex(cluster.getID());
                cluster.add(hit);
                clusters.add(cluster);
                if(debugMode>=1) System.out.println("Creating new cluster with ID " + cluster.getID());
            }
        }
        return clusters;
    }
    
    public void writeBanks(DataEvent event, List<FTHODOHit> hits, List<FTHODOCluster> clusters){
        if(event instanceof EvioDataEvent) {
            writeEvioBanks(event, hits, clusters);
        }
        else if(event instanceof HipoDataEvent) {
            writeHipoBanks(event, hits, clusters);
        }
    }
    
    private void writeHipoBanks(DataEvent event, List<FTHODOHit> hits, List<FTHODOCluster> clusters){
        
        // hits banks
        if(hits.size()!=0) {
            DataBank bankHits = event.createBank("FTHODO::hits", hits.size());    
            if(bankHits==null){
                System.out.println("ERROR CREATING BANK : FTHODO::hits");
                return;
            }
            for(int i = 0; i < hits.size(); i++){
                bankHits.setByte("sector",i,(byte) hits.get(i).get_Sector());
                bankHits.setByte("layer",i,(byte) hits.get(i).get_Layer());
                bankHits.setShort("component",i,(short) hits.get(i).get_ID());
                bankHits.setFloat("x",i,(float) (hits.get(i).get_Dx()/10.0));
                bankHits.setFloat("y",i,(float) (hits.get(i).get_Dy()/10.0));
                bankHits.setFloat("z",i,(float) (hits.get(i).get_Dz()/10.0));
                bankHits.setFloat("energy",i,(float) hits.get(i).get_Edep());
                bankHits.setFloat("time",i,(float) hits.get(i).get_Time());
                bankHits.setShort("hitID",i,(short) hits.get(i).get_DGTZIndex());
                bankHits.setShort("clusterID",i,(short) hits.get(i).get_ClusterIndex());				
            }
            event.appendBanks(bankHits);
        }
        // cluster bank
        if(clusters.size()!=0){
            DataBank bankCluster = event.createBank("FTHODO::clusters", clusters.size());    
            if(bankCluster==null){
                System.out.println("ERROR CREATING BANK : FTHODO::clusters");
                return;
            }
            for(int i = 0; i < clusters.size(); i++){
                            bankCluster.setShort("id", i,(short) clusters.get(i).getID());
                            bankCluster.setShort("size", i,(short) clusters.get(i).getSize());
                            bankCluster.setFloat("x",i,(float) (clusters.get(i).getX()/10.0));
                            bankCluster.setFloat("y",i,(float) (clusters.get(i).getY()/10.0));
                            bankCluster.setFloat("z",i,(float) (clusters.get(i).getZ()/10.0));
                            bankCluster.setFloat("widthX",i,(float) (clusters.get(i).getWidthX()/10.0));
                            bankCluster.setFloat("widthY",i,(float) (clusters.get(i).getWidthY()/10.0));
                            bankCluster.setFloat("radius",i,(float) (clusters.get(i).getRadius()/10.0));
                            bankCluster.setFloat("time",i,(float) clusters.get(i).getTime());
                            bankCluster.setFloat("energy",i,(float) clusters.get(i).getEnergy());
            }
            event.appendBanks(bankCluster);
        }
    }
    


    private void writeEvioBanks(DataEvent event, List<FTHODOHit> hits, List<FTHODOCluster> clusters) {
                          
        EvioDataBank bankhits  = null;
        EvioDataBank bankclust = null;
		
        // hits banks
        if(hits.size()!=0) {
                bankhits = (EvioDataBank) event.getDictionary().createBank("FTHODORec::hits",hits.size());
                for(int i=0; i<hits.size(); i++) {
                    bankhits.setInt("id",i,hits.get(i).get_ID());
                    bankhits.setInt("sector",i,hits.get(i).get_Sector());
                    bankhits.setInt("layer",i,hits.get(i).get_Layer());
                    bankhits.setDouble("hitX",i,hits.get(i).get_Dx()/10.0);
                    bankhits.setDouble("hitY",i,hits.get(i).get_Dy()/10.0);
                    bankhits.setDouble("hitEnergy",i,hits.get(i).get_Edep());
                    bankhits.setDouble("hitTime",i,hits.get(i).get_Time());
                    bankhits.setInt("hitDGTZIndex",i,hits.get(i).get_DGTZIndex());
                    bankhits.setInt("hitClusterIndex",i,hits.get(i).get_ClusterIndex());
                }				
        }	
        // cluster bank
        if(clusters.size()!=0){
                bankclust = (EvioDataBank) event.getDictionary().createBank("FTHODORec::clusters",clusters.size());
                for(int i =0; i< clusters.size(); i++) {
                        if(debugMode>=1) clusters.get(i).showCluster();
                            bankclust.setInt("clusterID", i,clusters.get(i).getID());
                            bankclust.setInt("clusterSize", i,clusters.get(i).size());
                            bankclust.setDouble("clusterX",i,clusters.get(i).getX()/10.0);
                            bankclust.setDouble("clusterY",i,clusters.get(i).getY()/10.0);
                            bankclust.setDouble("clusterDX",i,clusters.get(i).getWidthX()/10.0);
                            bankclust.setDouble("clusterDY",i,clusters.get(i).getWidthY()/10.0);
                            bankclust.setDouble("clusterTime",i,clusters.get(i).getTime());
                            bankclust.setDouble("clusterEnergy",i,clusters.get(i).getEnergy());
                            bankclust.setDouble("clusterTheta",i,clusters.get(i).getTheta());
                            bankclust.setDouble("clusterPhi",i,clusters.get(i).getPhi());
                }
        }

        // If there are no clusters, punt here but save the reconstructed hits 
        if(bankclust!=null) {
                event.appendBanks(bankhits,bankclust);
        }
        else if (bankhits!=null) {
                event.appendBank(bankhits);
        }
        
    }
    public List<FTHODOHit> readRawHits(DataEvent event, IndexedTable charge2Energy, IndexedTable timeOffsets, IndexedTable geometry) {
        // getting raw data bank
	if(debugMode>=1) System.out.println("Getting raw hits from FTHODO:dgtz bank");

        List<FTHODOHit>  hits = new ArrayList<FTHODOHit>();
	if(event.hasBank("FTHODO::dgtz")==true) {
            EvioDataBank bankDGTZ = (EvioDataBank) event.getBank("FTHODO::dgtz");
            int nrows = bankDGTZ.rows();
            for(int row = 0; row < nrows; row++){
                int isector     = bankDGTZ.getInt("sector",row);
                int ilayer      = bankDGTZ.getInt("layer",row);
                int icomponent  = bankDGTZ.getInt("component",row);
                int adc         = bankDGTZ.getInt("ADC",row);
                int tdc         = bankDGTZ.getInt("TDC",row);
                if(adc!=-1 && tdc!=-1){
                    FTHODOHit hit = new FTHODOHit(row,isector,ilayer,icomponent, adc, tdc,charge2Energy,timeOffsets,geometry);
	            hits.add(hit); 
	        }	          
            }
        }
        return hits;
    }
    
    public List<FTHODOHit> readRawHitsHipo(DataEvent event, IndexedTable charge2Energy, IndexedTable timeOffsets, IndexedTable status, IndexedTable geometry) {
        // getting raw data bank
	if(debugMode>=1) System.out.println("Getting raw hits from FTHODO:adc bank");

        List<FTHODOHit>  hits = new ArrayList<FTHODOHit>();
	if(event.hasBank("FTHODO::adc")==true) {
            DataBank bankDGTZ = event.getBank("FTHODO::adc");
            int nrows = bankDGTZ.rows();
            for(int row = 0; row < nrows; row++){
                int isector     = bankDGTZ.getByte("sector",row);
                int ilayer      = bankDGTZ.getByte("layer",row);
                int icomponent  = bankDGTZ.getShort("component",row);
                int iorder      = bankDGTZ.getByte("order",row);
                int adc         = bankDGTZ.getInt("ADC",row);
                float time      = bankDGTZ.getFloat("time",row);
                if(adc!=-1 && time!=-1 && status.getIntValue("status", isector, ilayer, icomponent)==0){
                    FTHODOHit hit = new FTHODOHit(row,isector,ilayer,icomponent, adc, time, charge2Energy,timeOffsets,geometry);
	             hits.add(hit); 
	        }	          
            }
        }
        return hits;
    }
}
