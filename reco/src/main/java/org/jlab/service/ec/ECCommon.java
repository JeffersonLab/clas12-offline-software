/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.ec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jlab.clas.detector.DetectorCollection;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.geom.base.Detector;
import org.jlab.geom.base.Layer;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.prim.Line3D;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.service.ec.ECCluster.ECClusterIndex;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author gavalian
 */
public class ECCommon {
    
    public static int[]  stripThreshold = new int[3];
    public static int[]   peakThreshold = new int[3]; 
    public static float[]  clusterError = new float[3];
    public static Boolean         debug = false;
    public static Boolean   singleEvent = false;
    public static DetectorCollection<H1F> H1_ecEng = new DetectorCollection<H1F>();
    
    static int ind[]  = {0,0,0,1,1,1,2,2,2}; 
        
    public static void initHistos() {
        for (int is=1; is<7; is++){
            for (int il=1; il<4; il++) {             
                H1_ecEng.add(is,il,0, new H1F("Cluster Errors",55,-10.,100.));
                H1_ecEng.add(is,il,1, new H1F("Cluster Errors",55,-10.,100.));
            }
        }
    }
    
    public static void resetHistos() {
        for (int is=1; is<7; is++){
            for (int il=1; il<4; il++) {             
                H1_ecEng.get(is,il,0).reset();
                H1_ecEng.get(is,il,1).reset();
            }
        }       
    }
    
    public static List<ECStrip>  initEC(DataEvent event, Detector detector, ConstantsManager manager, int run){
        if (singleEvent) resetHistos();
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
     * Read Strips from PCAL and EC and return all strips
     * @param event
     * @return 
     */
    public static List<ECStrip> readStrips(DataEvent event){
        List<ECStrip>  strips = new ArrayList<ECStrip>();
        strips.addAll(ECCommon.readEvent("PCAL",event));
        strips.addAll(ECCommon.readEvent("EC",event));
        return strips;
    }
    
    /**
     * Read event data from calorimeters
     * @param det
     * @param event
     * @return 
     */
    public static List<ECStrip>  readEvent(String det, DataEvent event){
        List<ECStrip>  strips = new ArrayList<ECStrip>();
         if(event.hasBank(det+"::dgtz")==true){
            EvioDataBank ecBank = (EvioDataBank) event.getBank(det+"::dgtz");
            int nrows = ecBank.rows();
            for(int row = 0; row < nrows; row++){
                int     sector = ecBank.getInt("sector", row);
                int      stack = det.equals("PCAL") ? 0:ecBank.getInt("stack", row);
                int       view = ecBank.getInt("view", row);
                int  component = ecBank.getInt("strip", row);
                int      layer = stack*3 + view;
                ECStrip  strip = new ECStrip(sector, layer, component);
                strip.setADC(ecBank.getInt("ADC", row));
                strip.setTDC(ecBank.getInt("TDC", row));
                if(strip.getADC()>ECCommon.stripThreshold[ind[layer-1]]) strips.add(strip);                       
            }
         }
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
    
    public static List<ECPeak>  processPeaks(List<ECPeak> peaks){
        List<ECPeak> ecPeaks = new ArrayList<ECPeak>();
        for(ECPeak p : peaks){
            int adc = p.getADC();
            int lay = p.getDescriptor().getLayer();
            if(adc>ECCommon.peakThreshold[ind[lay-1]]){
                ecPeaks.add(p);
            }
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
                            ECCluster cluster = new ECCluster(pU.get(bU),pV.get(bV),pW.get(bW));
                            H1_ecEng.get(sector,ind[startLayer-1]+1,0).fill(cluster.getHitPositionError());
                            if(cluster.getHitPositionError()<ECCommon.clusterError[ind[startLayer-1]]) {
                                H1_ecEng.get(sector,ind[startLayer-1]+1,1).fill(cluster.getHitPositionError());
                                clusters.add(cluster);
                            }
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
