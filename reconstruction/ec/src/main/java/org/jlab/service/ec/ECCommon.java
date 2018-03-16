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
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataEvent;
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
    public static String      variation = "default";

    private static double[] AtoE  = {15,10,10};    // SCALED ADC to Energy in MeV
    private static double[] AtoE5 = {15,5,5};     // For Sector 5 ECAL
    
    //public static DetectorCollection<H1F> H1_ecEng = new DetectorCollection<H1F>();
    
    static int ind[]  = {0,0,0,1,1,1,2,2,2}; 
        
    public static void initHistos() {
        /*
        for (int is=1; is<7; is++){
            for (int il=1; il<4; il++) {             
                H1_ecEng.add(is,il,0, new H1F("Cluster Errors",55,-10.,100.));
                H1_ecEng.add(is,il,1, new H1F("Cluster Errors",55,-10.,100.));
            }
        }*/
    }
    
    public static void resetHistos() {
        /*
        for (int is=1; is<7; is++){
            for (int il=1; il<4; il++) {             
                H1_ecEng.get(is,il,0).reset();
                H1_ecEng.get(is,il,1).reset();
            }
        } */      
    }
    
    public static List<ECStrip>  initEC(DataEvent event, Detector detector, ConstantsManager manager, int run){
        if (singleEvent) resetHistos();        
        
        List<ECStrip>  ecStrips = null;
        
        if(event instanceof EvioDataEvent) {
            ecStrips = ECCommon.readStripsEvio(event);
        }
        
        if(event instanceof HipoDataEvent) {
            ecStrips = ECCommon.readStripsHipo(event);
        }
        
        if(ecStrips==null) return new ArrayList<ECStrip>();
        
        Collections.sort(ecStrips);
        
        manager.setVariation(variation);
        
        IndexedTable   atten  = manager.getConstants(run, "/calibration/ec/attenuation");
        IndexedTable    gain  = manager.getConstants(run, "/calibration/ec/gain");
        
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
            ScintillatorPaddle firstPaddle = (ScintillatorPaddle) detLayer.getComponent(0);
            
            strip.getLine().copy(paddle.getLine());
            double distance = paddle.getLine().origin().distance(firstPaddle.getLine().origin());
            strip.setDistanceEdge(distance);
            strip.setAttenuation( atten.getDoubleValue("A", sector,layer,component),
                                  atten.getDoubleValue("B", sector,layer,component),
                                  atten.getDoubleValue("C", sector,layer,component));
            strip.setGain(gain.getDoubleValue("gain", sector,layer,component));            
        }
        return ecStrips;
    }
        
    public static List<ECStrip>  readStripsHipo(DataEvent event){        
        List<ECStrip>  strips = new ArrayList<ECStrip>();
        if(event.hasBank("ECAL::adc")==true){
            DataBank bank = event.getBank("ECAL::adc");
            int rows = bank.rows();
            for(int loop = 0; loop < rows; loop++){
                int sector    = bank.getByte("sector", loop);
                int layer     = bank.getByte("layer", loop);
                int component = bank.getShort("component", loop);
                ECStrip  strip = new ECStrip(sector, layer, component);
                strip.setADC(bank.getInt("ADC", loop));
                strip.setTDC(0);
                double sca = (sector==5)?AtoE5[ind[layer-1]]:AtoE[ind[layer-1]]; 
                if (variation=="clas6") sca = 1.0;
                if(strip.getADC()>sca*ECCommon.stripThreshold[ind[layer-1]]) strips.add(strip);
            }
        }
        return strips;
    }
    /**
     * Read Strips from PCAL and EC and return all strips
     * @param event
     * @return 
     */
    public static List<ECStrip> readStripsEvio(DataEvent event){
        List<ECStrip>  strips = new ArrayList<ECStrip>();
        strips.addAll(ECCommon.readEvioEvent("PCAL",event));
        strips.addAll(ECCommon.readEvioEvent("EC",event));
        return strips;
    }
    
    /**
     * Read event data from calorimeters
     * @param det
     * @param event
     * @return 
     */
    public static List<ECStrip>  readEvioEvent(String det, DataEvent event){
        List<ECStrip>  strips = new ArrayList<ECStrip>();
         if(event.hasBank(det+"::dgtz")==true){
            EvioDataBank bank = (EvioDataBank) event.getBank(det+"::dgtz");
            int nrows = bank.rows();
            for(int row = 0; row < nrows; row++){
                int     sector = bank.getInt("sector", row);
                int      stack = det.equals("PCAL") ? 0:bank.getInt("stack", row);
                int       view = bank.getInt("view", row);
                int  component = bank.getInt("strip", row);
                int      layer = stack*3 + view;
                ECStrip  strip = new ECStrip(sector, layer, component);
                strip.setADC(bank.getInt("ADC", row));
                strip.setTDC(0);
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
            int sec = p.getDescriptor().getSector();
            double sca = (sec==5)?AtoE5[ind[lay-1]]:AtoE[ind[lay-1]]; 
            if (variation=="clas6") sca = 1.0;
            if(adc>sca*ECCommon.peakThreshold[ind[lay-1]]) ecPeaks.add(p);
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
                            //H1_ecEng.get(sector,ind[startLayer-1]+1,0).fill(cluster.getHitPositionError());
                            if(cluster.getHitPositionError()<ECCommon.clusterError[ind[startLayer-1]]) {
                                //H1_ecEng.get(sector,ind[startLayer-1]+1,1).fill(cluster.getHitPositionError());
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
