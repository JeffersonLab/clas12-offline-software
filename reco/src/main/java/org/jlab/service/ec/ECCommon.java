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
import org.jlab.geom.base.Layer;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.prim.Line3D;
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
                
                int superlayer = (layer-1)/3;
                int clayer     = (layer-1)%3;
                
                Layer detLayer = detector.getSector(sector-1).getSuperlayer(superlayer).getLayer(clayer);
                ScintillatorPaddle paddle = (ScintillatorPaddle) detLayer.getComponent(component-1);
                strip.getLine().copy(paddle.getLine());
                //= detector.getSector(sector).getSuperlayer(stack).getLayer(view).getComponent(component);
                
                if(strip.getADC()>20){
                    strips.add(strip);
                }
            }
        }
        Collections.sort(strips);
        return strips;
    }
    
    public static List<ECStrip>  initEC(DataEvent event, 
            Detector detector, ConstantsManager manager, int run){
        List<ECStrip>  ecStrips = ECCommon.readStrips(event);
        Collections.sort(ecStrips);
        IndexedTable   atten  = manager.getConstants(run, "/calibration/ec/attenuation");
        for(ECStrip strip : ecStrips){
            int sector    = strip.getDescriptor().getSector();
            int layer     = strip.getDescriptor().getLayer();
            int component = strip.getDescriptor().getComponent();
            int superlayer = (int) ((layer-1)/3);
            int localLayer = (layer-1)%3;
            /*System.out.println(" SUPERLAYER = " + superlayer 
                    + "  LOCAL LAYER = " + localLayer
                    + "  LAYER = " + layer);
            */
            Layer detLayer = detector.getSector(sector-1).getSuperlayer(superlayer).getLayer(localLayer);
            ScintillatorPaddle paddle = (ScintillatorPaddle) detLayer.getComponent(component-1);
            strip.getLine().copy(paddle.getLine());
            strip.setAttenuation( atten.getDoubleValue("A", sector,layer,component),
                            atten.getDoubleValue("B", sector,layer,component),
                            atten.getDoubleValue("C", sector,layer,component));
        }
        return ecStrips;
    }
    
    /**
     * Read strips from PCAL
     * @param event
     * @return 
     */
    public static List<ECStrip>  readPCAL(DataEvent event){
        List<ECStrip>  strips = new ArrayList<ECStrip>();
         if(event.hasBank("PCAL::dgtz")==true){
            EvioDataBank ecBank = (EvioDataBank) event.getBank("PCAL::dgtz");
            int nrows = ecBank.rows();
            for(int row = 0; row < nrows; row++){
                int sector = ecBank.getInt("sector", row);
                int stack  = ecBank.getInt("stack", row);
                int view   = ecBank.getInt("view", row);
                int component  = ecBank.getInt("strip", row);
                ECStrip  strip = new ECStrip(sector, view, component);
                strip.setADC(ecBank.getInt("ADC", row));
                strip.setTDC(ecBank.getInt("TDC", row));
                if(strip.getADC()>20) strips.add(strip);
            }
         }
        return strips;
    }
    /**
     * Read EC Bank and
     * @param event
     * @return 
     */
    public static List<ECStrip>  readEC(DataEvent event){
        List<ECStrip>  strips = new ArrayList<ECStrip>();
         if(event.hasBank("EC::dgtz")==true){
            EvioDataBank ecBank = (EvioDataBank) event.getBank("EC::dgtz");
            int nrows = ecBank.rows();
            for(int row = 0; row < nrows; row++){
                int sector = ecBank.getInt("sector", row);
                int stack  = ecBank.getInt("stack", row);
                int view   = ecBank.getInt("view", row);
                int component  = ecBank.getInt("strip", row);
                int layer      = (stack)*3 + view;
                ECStrip  strip = new ECStrip(sector, layer, component);
                strip.setADC(ecBank.getInt("ADC", row));
                strip.setTDC(ecBank.getInt("TDC", row));
                if(strip.getADC()>5)
                    strips.add(strip);
            }
         }
        return strips;
    }
    /**
     * Read Strips from PCAL and EC and return all
     * @param event
     * @return 
     */
    public static List<ECStrip> readStrips(DataEvent event){
        List<ECStrip>  strips = new ArrayList<ECStrip>();
        List<ECStrip>  pcal   = ECCommon.readPCAL(event);
        List<ECStrip>  ec     = ECCommon.readEC(event);
        strips.addAll(pcal);
        strips.addAll(ec);
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
            peakList.get(loop).setOrder(loop+1);
        }
        
        return peakList;
    }
    
    public static List<ECPeak>  processPeaks(List<ECPeak> peaks){
        List<ECPeak> ecPeaks = new ArrayList<ECPeak>();
        for(ECPeak p : peaks){
            int adc = p.getADC();
            //if(adc>300){
                ecPeaks.add(p);
            //}
        }
        return ecPeaks;
    }
    
    /**
     * returns a list of peaks for given sector and layer
     * @param sector
     * @param layer
     * @param peaks
     * @return 
     */
    public static List<ECPeak>   getPeaks(int sector, int layer, List<ECPeak> peaks){
        List<ECPeak>  selected = new ArrayList<ECPeak>();
        for(ECPeak peak : peaks){
            if(peak.getDescriptor().getSector()==sector&&peak.getDescriptor().getLayer()==layer){
                selected.add(peak);
            }
        }
        return selected;
    }
    
    
    public static void shareClustersEnergy(List<ECCluster> clusters){
        
        int nclusters = clusters.size();
        
        for(int i = 0; i < nclusters - 1; i++){
            for(int k = i+1 ; k < nclusters; k++){
                int sharedView = clusters.get(i).sharedView(clusters.get(k));
                if(sharedView>=0){
                    
                    //System.out.println(" CLUSTERS SHARE VIEW : " + i + " " + k 
                    //+ "  energy " + clusters.get(i).getEnergy() + "  " + clusters.get(k).getEnergy());
                    
                    //System.out.println(clusters.get(i));
                    //System.out.println(clusters.get(k));
                    
                    ECCluster.shareEnergy(clusters.get(i), clusters.get(k), sharedView);
                    //System.out.println("\t -->  " 
                    //+ " corrected energy " + clusters.get(i).getEnergy() + "  " + clusters.get(k).getEnergy());
                }
            }
        }
    }
    
    public static List<ECCluster>   createClusters(List<ECPeak>  peaks, int startLayer){

        List<ECCluster>   clusters = new ArrayList<ECCluster>();
        
        for(int p = 0; p < peaks.size(); p++){
            peaks.get(p).setOrder(p+1);
        }


        for(int sector = 1; sector <= 6; sector++){

            List<ECPeak>  pU = ECCommon.getPeaks(sector, startLayer, peaks);
            List<ECPeak>  pV = ECCommon.getPeaks(sector, startLayer+1, peaks);
            List<ECPeak>  pW = ECCommon.getPeaks(sector, startLayer+2, peaks);
            /*System.out.println("-------->  peaks are found " +
                    " U " + pU.size() +
                    " V " + pV.size() +
                    " W " + pW.size()
            );*/
            
            if(pU.size()>0&&pV.size()>0&&pW.size()>0){
                
                for(int bU = 0; bU < pU.size();bU++){
                    for(int bV = 0; bV < pV.size();bV++){
                        for(int bW = 0; bW < pW.size();bW++){
                            pU.get(bU).redoPeakLine();
                            pV.get(bV).redoPeakLine();
                            pW.get(bW).redoPeakLine();
                            ECCluster cluster = new ECCluster(
                                    pU.get(bU),pV.get(bV),pW.get(bW));
                            if(cluster.getHitPositionError()<9.5)
                            //System.out.println(" POSITION ERROR - > " + cluster.getHitPositionError());
                                clusters.add(cluster);
                        }
                    }
                }
            }
        }
        
        for(int i = 0 ; i < clusters.size(); i++){
            clusters.get(i).setEnergy(
                    clusters.get(i).getEnergy(0) + 
                            clusters.get(i).getEnergy(1) +
                            clusters.get(i).getEnergy(2)
            );
        }
        
        return clusters;
    }    
}
