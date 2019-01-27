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
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.rec.ft.hodo.FTHODOCluster;
import org.jlab.rec.ft.hodo.FTHODOHit;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author devita
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
                if(adc!=-1 && time!=-1){
                    FTTRKHit hit = new FTTRKHit(isector,ilayer,icomponent, (double) adc);
	            hits.add(hit); 
	        }	          
            }
        }
        return hits;
    }    
    
    
    public void writeBanks(DataEvent event, List<FTTRKHit> hits, List<FTTRKCluster> clusters){
        
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
//        // cluster bank
//        if(clusters.size()!=0){
//            DataBank bankCluster = event.createBank("FTHODO::clusters", clusters.size());    
//            if(bankCluster==null){
//                System.out.println("ERROR CREATING BANK : FTHODO::clusters");
//                return;
//            }
//            for(int i = 0; i < clusters.size(); i++){
//                            bankCluster.setShort("id", i,(short) clusters.get(i).getID());
//                            bankCluster.setShort("size", i,(short) clusters.get(i).getSize());
//                            bankCluster.setFloat("x",i,(float) (clusters.get(i).getX()/10.0));
//                            bankCluster.setFloat("y",i,(float) (clusters.get(i).getY()/10.0));
//                            bankCluster.setFloat("z",i,(float) (clusters.get(i).getZ()/10.0));
//                            bankCluster.setFloat("widthX",i,(float) (clusters.get(i).getWidthX()/10.0));
//                            bankCluster.setFloat("widthY",i,(float) (clusters.get(i).getWidthY()/10.0));
//                            bankCluster.setFloat("radius",i,(float) (clusters.get(i).getRadius()/10.0));
//                            bankCluster.setFloat("time",i,(float) clusters.get(i).getTime());
//                            bankCluster.setFloat("energy",i,(float) clusters.get(i).getEnergy());
//            }
//            event.appendBanks(bankCluster);
//        }
    }
    

}
