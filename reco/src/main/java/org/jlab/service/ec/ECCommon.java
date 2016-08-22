/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.ec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.geom.base.Detector;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.service.ec.ECCluster.ECClusterIndex;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author gavalian
 */
public class ECCommon {
    
    /**
     * Returns array of strips for EC given the EC bank, EC detector
     * and constants manager.
     * @param event 
     * @param detector
     * @param manager
     * @param run
     * @return 
     */    
    public static List<ECStrip>  initStrips(DataEvent event, 
            Detector detector, ConstantsManager manager, int run){
                
        List<ECStrip>  strips = new ArrayList<ECStrip>();
        IndexedTable   atten  = manager.getConstants(run, "/calibration/ec/attenuation");
        
        if(event.hasBank("EC::dgtz")==true){
            EvioDataBank ecBank = (EvioDataBank) event.getBank("EC::dgtz");
            int nrows = ecBank.rows();
            //System.out.println(" BANK EC loaded with ROWS = " + nrows);
            for(int row = 0; row < nrows; row++){
                /*
                int sector    = (int) ecBank.getByte("sector", row);
                int layer     = (int) ecBank.getByte("layer", row);
                int component = (int) ecBank.getByte("component", row);
                */
                int sector = ecBank.getInt("sector", row);
                int stack  = ecBank.getInt("stack", row);
                int view   = ecBank.getInt("view", row);
                int component  = ecBank.getInt("strip", row);
                int layer  = stack*3 + view;
                
                ECStrip strip = new ECStrip(
                        sector,layer,component
                );
                
                strip.setADC(ecBank.getInt("ADC", row));
                strip.setTDC(ecBank.getInt("TDC", row));
                if(atten!=null){
                    //atten.show();
                    strip.setAttenuation(
                            atten.getDoubleValue("A", sector,layer,component),
                            atten.getDoubleValue("B", sector,layer,component),
                            atten.getDoubleValue("C", sector,layer,component)
                    );
                } else {                    
                    System.out.println(manager.toString());
                }
                if(strip.getADC()>10){
                    strips.add(strip);
                }
            }
        }
        Collections.sort(strips);
        return strips;
    }
    
    public static List<ECPeak>  createPeaks(List<ECStrip> stripList){
        List<ECPeak>  peakList = new ArrayList<ECPeak>();
        if(stripList.size()>1){
            ECPeak  firstPeak = new ECPeak(stripList.get(0));
            peakList.add(firstPeak);
            for(int loop = 1; loop < stripList.size(); loop++){
                boolean stripAdded = false;                
                for(ECPeak  peak : peakList){
                    if(peak.addStrip(stripList.get(loop))==true){
                        stripAdded = true;
                    }
                }
                if(stripAdded==false){
                    ECPeak  newPeak = new ECPeak(stripList.get(loop));
                    peakList.add(newPeak);
                }
            }
        }
        for(int loop = 0; loop < peakList.size(); loop++){
            peakList.get(loop).setPeakId(loop+1);
        }
        return peakList;
    }
    
    
    
    public static List<ECPeak>   getPeaks(int sector, int layer, List<ECPeak> peaks){
        List<ECPeak>  selected = new ArrayList<ECPeak>();
        for(ECPeak peak : peaks){
            if(peak.getDescriptor().getSector()==sector&&peak.getDescriptor().getLayer()==layer){
                selected.add(peak);
            }
        }
        return selected;
    }
    
    public static List<ECCluster>   createClusters(List<ECPeak>  peaks){

        List<ECCluster>   clusters = new ArrayList<ECCluster>();
        
        for(int p = 0; p < peaks.size(); p++){
            peaks.get(p).setOrder(p+1);
        }


        for(int sector = 1; sector <= 6; sector++){

            List<ECPeak>  pU = ECCommon.getPeaks(sector, 1, peaks);
            List<ECPeak>  pV = ECCommon.getPeaks(sector, 2, peaks);
            List<ECPeak>  pW = ECCommon.getPeaks(sector, 3, peaks);

            if(pU.size()>0&&pV.size()>0&&pW.size()>0){
                for(int bU = 0; bU < pU.size();bU++){
                    for(int bV = 0; bV < pV.size();bV++){
                        for(int bW = 0; bW < pW.size();bW++){
                            //ECCluster cluster = new ECCluster(
                            //        pU.get(bU),pV.get(bV),pW.get(bW));
                            //if(cluster.getHitPositionError()<10.0)
                            //    clusters.add(cluster);
                        }
                    }
                }
            }
        }
        return clusters;
    }
    
}
