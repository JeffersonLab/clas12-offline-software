package org.jlab.service.ec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jlab.detector.base.DetectorCollection;
import org.jlab.detector.base.DetectorLayer;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.geom.base.Detector;
import org.jlab.geom.base.Layer;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.utils.groups.IndexedList;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author gavalian
 */

public class ECCommon {
    
    public static List<ECStrip>     myStrips = new ArrayList<ECStrip>();
    public static List<ECPeak>       myPeaks = new ArrayList<ECPeak>();
    public static List<ECCluster> myClusters = new ArrayList<ECCluster>();
    
    public static int[]   stripThreshold = new int[3];
    public static int[]    peakThreshold = new int[3]; 
    public static int[] clusterThreshold = new int[3];
    public static float[]   clusterError = new float[3];
    public static float[]  clusterDeltaT = new float[3];    
    
    public static Boolean             debug = false;
    public static Boolean        debugSplit = false;
    public static Boolean  isSingleThreaded = false;
    public static Boolean       singleEvent = false;
    public static Boolean     useNewTimeCal = true;
    public static Boolean   useUnsharedTime = true;
    public static Boolean      useLogWeight = true;
    public static Boolean       useCCDBGain = true;
    public static double           logParam = 4.0;
    public static String          variation = "default";
    public static String      geomVariation = "default";
    
    private static double[] AtoE  = {15,10,10};   // SCALED ADC to Energy in MeV
    private static double[] AtoE5 = {15,5,5};     // For Sector 5 ECAL
    
    public static DetectorCollection<H1F> H1_ecEng = new DetectorCollection<H1F>();
    public static DetectorCollection<H2F> H2_ecEng = new DetectorCollection<H2F>();
    
    static int ind[]  = {0,0,0,1,1,1,2,2,2}; 
    static float               tps = 0.02345f;
    public static float       veff = 18.1f;
    
    public  static void initHistos() {
    	int[] bins = {480,240,120};       
        for (int is=1; is<7; is++){
            for (int il=1; il<4; il++) {             
                H1_ecEng.add(is,il,0, new H1F("Cluster Errors",bins[il-1],-10,100));
                H1_ecEng.add(is,il,1, new H1F("Cluster Errors",bins[il-1],-10,100));
                H2_ecEng.add(is,il,1, new H2F("Cluster Errors",12,0,12,40,0,20));
            }
        }
    }
    
    public static void resetHistos() {
        
        for (int is=1; is<7; is++){
            for (int il=1; il<4; il++) {             
                H1_ecEng.get(is,il,0).reset();
                H1_ecEng.get(is,il,1).reset();
                H2_ecEng.get(is,il,1).reset();
            }
        }       
    }
    
    public static void setDebug(boolean val) {
    	debug = val;
    }
    
    public static void setisSingleThreaded(boolean val) {
        isSingleThreaded = val;
    }
    
    public static void setSingleEvent(boolean val) {
    	singleEvent = val;
    }
    
    public static void clearMyStructures() {
    	getMyStrips().clear();
    	getMyPeaks().clear();
    	getMyClusters().clear();
    }
    
    public static List<ECStrip> getMyStrips() {
    	return myStrips;
    }
    
    public static List<ECPeak> getMyPeaks() {
    	return myPeaks;
    }
    
    public static List<ECCluster> getMyClusters() {
    	return myClusters;
    }
    
    public static List<ECStrip>  initEC(DataEvent event, Detector detector, ConstantsManager manager, int run){
    	
        manager.setVariation(variation);

        IndexedTable    atten = manager.getConstants(run, "/calibration/ec/attenuation");
        IndexedTable     gain = manager.getConstants(run, "/calibration/ec/gain");
		IndexedTable     time = manager.getConstants(run, "/calibration/ec/timing");
		IndexedTable      ggs = manager.getConstants(run, "/calibration/ec/global_gain_shift");
		IndexedTable      gtw = manager.getConstants(run, "/calibration/ec/global_time_walk");
		IndexedTable       ev = manager.getConstants(run, "/calibration/ec/effective_velocity");
		IndexedTable      tgo = manager.getConstants(run, "/calibration/ec/tdc_global_offset");
        IndexedTable   r2gain = manager.getConstants(2,   "/calibration/ec/gain");
   
        if (singleEvent) resetHistos();        
        
        List<ECStrip>  ecStrips = null;
        
        ecStrips = ECCommon.readStripsHipo(event, run, manager);  
        
        if(ecStrips==null) return new ArrayList<ECStrip>();
        
        Collections.sort(ecStrips);
        
        for(ECStrip strip : ecStrips){
            int sector    = strip.getDescriptor().getSector();
            int layer     = strip.getDescriptor().getLayer();      //1,2,3=PCAL 4,5,6=ECIN 7,8,9=ECOU
            int component = strip.getDescriptor().getComponent();
            int superlayer = (int) ((layer-1)/3);                  //0=PCAL 1=ECIN 2=ECOU
            int localLayer = (layer-1)%3;                          //0=U 1=V 2=W

            int off = superlayer==0 ? DetectorLayer.PCAL_Z : (superlayer==1 ? DetectorLayer.EC_INNER_Z : DetectorLayer.EC_OUTER_Z);
            
            Layer detLayer = detector.getSector(sector-1).getSuperlayer(superlayer).getLayer(localLayer+off);
            ScintillatorPaddle      paddle = (ScintillatorPaddle) detLayer.getComponent(component-1);
            ScintillatorPaddle firstPaddle = (ScintillatorPaddle) detLayer.getComponent(0);
            ScintillatorPaddle  lastPaddle = (ScintillatorPaddle) detLayer.getComponent(detLayer.getNumComponents()-1);
                       
            strip.getLine().copy(paddle.getLine());
            
            double distance = paddle.getLine().origin().distance(firstPaddle.getLine().origin());

            strip.setDistanceEdge(distance);
            // Modified on May 1st 2019. This is to account for
            // the fact that the distance from the edge on PCAL
            // for layers V and W are calculated from the wider
            // strips edge.
            if(layer==2||layer==3){
                distance = paddle.getLine().origin().distance(lastPaddle.getLine().origin());
                double hL = 394.2*0.5;
                double hyp = Math.sqrt(hL*hL + 385.2*385.2);
                double theta = Math.acos(hL/hyp);
                double proj  = 4.5*Math.cos(theta);
                strip.setDistanceEdge(distance + proj);
            }
            // End of the edit.
            strip.setAttenuation(atten.getDoubleValue("A", sector,layer,component),
                                 atten.getDoubleValue("B", sector,layer,component),
                                 atten.getDoubleValue("C", sector,layer,component));
            double ccdbGain =   gain.getDoubleValue("gain", sector,layer,component)*ggs.getDoubleValue("gain_shift",sector,layer,0);
            double run2Gain = r2gain.getDoubleValue("gain", sector,layer,component);
            strip.setGain(useCCDBGain?ccdbGain:run2Gain); 
            strip.setGlobalTimeWalk(gtw.getDoubleValue("time_walk",sector,layer,0)); 
            strip.setVeff(ev.getDoubleValue("veff",sector,layer,component));
            strip.setTiming(time.getDoubleValue("a0", sector, layer, component),
                            time.getDoubleValue("a1", sector, layer, component),
                            time.getDoubleValue("a2", sector, layer, component),
                            time.getDoubleValue("a3", sector, layer, component),
                            time.getDoubleValue("a4", sector, layer, component));
            strip.setGlobalTimingOffset(tgo.getDoubleValue("offset",0,0,0)); //global shift of TDC acceptance window
        }
            
        return ecStrips;
    }
        
    public static List<ECStrip>  readStripsHipo(DataEvent event, int run, ConstantsManager manager){ 
    	
      	List<ECStrip>  strips = new ArrayList<ECStrip>();
        IndexedList<List<Integer>>  tdcs = new IndexedList<List<Integer>>(3);  
        
		IndexedTable    jitter = manager.getConstants(run, "/calibration/ec/time_jitter");
		IndexedTable        fo = manager.getConstants(run, "/calibration/ec/fadc_offset");
		IndexedTable       tmf = manager.getConstants(run, "/calibration/ec/tmf_offset"); 
		IndexedTable       fgo = manager.getConstants(run, "/calibration/ec/fadc_global_offset");
		IndexedTable       gtw = manager.getConstants(run, "/calibration/ec/global_time_walk");
		IndexedTable    tmfcut = manager.getConstants(run,  "/calibration/ec/tmf_window");
	    IndexedTable    status = manager.getConstants(run,  "/calibration/ec/status");
        
        double PERIOD  = jitter.getDoubleValue("period",0,0,0);
        int    PHASE   = jitter.getIntValue("phase",0,0,0); 
        int    CYCLES  = jitter.getIntValue("cycles",0,0,0);        
        float FTOFFSET = (float) fgo.getDoubleValue("global_offset",0,0,0); //global shift of trigger time
        float  TMFCUT  = (float) tmfcut.getDoubleValue("window", 0,0,0); //acceptance window for TDC-FADC times
        
	    int triggerPhase = 0;
    	
        if(CYCLES>0&&event.hasBank("RUN::config")==true){
            DataBank bank = event.getBank("RUN::config");
            long timestamp = bank.getLong("timestamp", 0);
            triggerPhase = (int) (PERIOD*((timestamp+PHASE)%CYCLES));
        }

        if(event.hasBank("ECAL::tdc")==true){
            DataBank  bank = event.getBank("ECAL::tdc");            
            for(int i = 0; i < bank.rows(); i++){
                int  is = bank.getByte("sector",i);
                int  il = bank.getByte("layer",i);
                int  ip = bank.getShort("component",i);    
                int tdc = bank.getInt("TDC",i);
		
		        if(status.getIntValue("status",is,il,ip)==2) continue;
		    
                if(tdc>0) {                       
                    if(!tdcs.hasItem(is,il,ip)) tdcs.add(new ArrayList<Integer>(),is,il,ip);
                        tdcs.getItem(is,il,ip).add(tdc);       
                }
            }
        }        
        
        if(event.hasBank("ECAL::adc")==true){
            DataBank bank = event.getBank("ECAL::adc");
            for(int i = 0; i < bank.rows(); i++){
                int  is = bank.getByte("sector", i);
                int  il = bank.getByte("layer", i); 
                int  ip = bank.getShort("component", i);
                int adc = bank.getInt("ADC", i);
                float t = bank.getFloat("time", i) + (float) tmf.getDoubleValue("offset",is,il,ip) // FADC-TDC offset (sector, layer, PMT)
                                                   + (float)  fo.getDoubleValue("offset",is,il,0); // FADC-TDC offset (sector, layer) 
                
		        if (status.getIntValue("status",is,il,ip)==3) continue;    
		    
                ECStrip  strip = new ECStrip(is, il, ip); 
                
                strip.setADC(adc);
                strip.setTriggerPhase(triggerPhase);
                strip.setID(i+1);
                
                double sca = (is==5)?AtoE5[ind[il-1]]:AtoE[ind[il-1]]; 
                if (variation=="clas6") sca = 1.0;  
                
                if(strip.getADC()>sca*ECCommon.stripThreshold[ind[il-1]]) strips.add(strip); 
                
                float  tmax = 1000; int tdc = 0;
                
                if (tdcs.hasItem(is,il,ip)) {
                    float radc = (float)Math.sqrt(adc);
                    for (float tdcc : tdcs.getItem(is,il,ip)) {
                         float tdif = tps*tdcc - (float)gtw.getDoubleValue("time_walk",is,il,0)/radc - triggerPhase - FTOFFSET - t; 
                        if (Math.abs(tdif)<TMFCUT&&tdif<tmax) {tmax = tdif; tdc = (int)tdcc;}
                    }
                    strip.setTDC(tdc); 
                }              
            }
        }  
        
        return strips;     
    }
    
    public static List<ECPeak>  createPeaks(List<ECStrip> stripList){
    	
        List<ECPeak>  peakList = new ArrayList<ECPeak>();
        
        if(stripList.size()>1){ //Require minimum of 2 strips/event to reject uncorrelated hot channels
            ECPeak  firstPeak = new ECPeak(stripList.get(0)); //Seed the first peak with the first strip
            peakList.add(firstPeak); 
            for(int loop = 1; loop < stripList.size(); loop++){ //Loop over all strips 
                boolean stripAdded = false;                
                for(ECPeak  peak : peakList) {
                    if(peak.addStrip(stripList.get(loop))){ //Add adjacent strip to newly seeded peak
                        stripAdded = true;
                    }
                }
                if(!stripAdded){
                    ECPeak  newPeak = new ECPeak(stripList.get(loop)); //Non-adjacent strip seeds new peak
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
    	
        List<ECPeak> peakList = new ArrayList<ECPeak>();
        
        for(ECPeak p : peaks){
            int adc = p.getADC();
            int lay = p.getDescriptor().getLayer();
            int sec = p.getDescriptor().getSector();
            double sca = (sec==5)?AtoE5[ind[lay-1]]:AtoE[ind[lay-1]]; 
            if (variation=="clas6") sca = 1.0;
            if(adc>sca*ECCommon.peakThreshold[ind[lay-1]]) peakList.add(p); //adc threshold (uncorrected energy MeV*10)
        }
        
        ECPeakAnalysis.splitPeaks(peakList);       //Split peak if strip members have an adc valley
        for(ECPeak p : peakList) p.redoPeakLine(); //Find new peak lines after splitPeaks
                
        return peakList;
    }
    
    public static List<ECPeak>  getPeaks(int sector, int layer, List<ECPeak> peaks){
        List<ECPeak>  selected = new ArrayList<ECPeak>();
        for(ECPeak peak : peaks){
            if(peak.getDescriptor().getSector()==sector&&peak.getDescriptor().getLayer()==layer){
                selected.add(peak);
            }
        }
        return selected;
    }
    
    public static void shareClustersEnergy(List<ECCluster> clusters){
        
        for(int i = 0; i < clusters.size() - 1; i++){
            for(int k = i+1 ; k < clusters.size(); k++){
                byte sharedView = (byte) clusters.get(i).sharedView(clusters.get(k)); // 0,1,2,3,4,5 <=> U,V,W,UV,UW,VW
                if(sharedView>=0&&sharedView<6){
                	clusters.get(i).setSharedCluster(k); clusters.get(i).setSharedView(sharedView+1);
                	clusters.get(k).setSharedCluster(i); clusters.get(k).setSharedView(sharedView+1);                  
                    ECCluster.shareEnergy(clusters.get(i), clusters.get(k), sharedView+1);
                }
            }
        }        
    }
    
    public static boolean isGoodCluster(ECCluster cluster) {
    	
    	for (int i=0; i<3; i++) {
    		int lay = cluster.getPeak(i).getDescriptor().getLayer();
    		if(ECCommon.clusterThreshold[ind[lay-1]]==0) return true;
    		double thr = 0.1*ECCommon.clusterThreshold[ind[lay-1]]*ECCommon.peakThreshold[ind[lay-1]]; //cluster thrsh. fraction of peak
    		if(cluster.getEnergy(i)*1e3<thr) return false;  
    	}       
    	return true;
    }  
    
    public static List<ECCluster>  createClusters(List<ECPeak>  peaks, int startLayer){

        List<ECCluster>   clusters = new ArrayList<ECCluster>();
        
        for(int p = 0; p < peaks.size(); p++){
            peaks.get(p).setOrder(p+1);
        }

        for(int sector = 1; sector <= 6; sector++){

            List<ECPeak>  pU = ECCommon.getPeaks(sector, startLayer,   peaks);
            List<ECPeak>  pV = ECCommon.getPeaks(sector, startLayer+1, peaks);
            List<ECPeak>  pW = ECCommon.getPeaks(sector, startLayer+2, peaks);
            /*System.out.println("-------->  peaks are found " +
                    " U " + pU.size() +
                    " V " + pV.size() +
                    " W " + pW.size()
            );*/
            
           int nclus=0; float maxerr=0;
           if(pU.size()>0&&pV.size()>0&&pW.size()>0){  //U,V,W peaks required for cluster
                for(int bU = 0; bU < pU.size();bU++){
                    pU.get(bU).redoPeakLine();
                    for(int bV = 0; bV < pV.size();bV++){
                        if(bU==0) pV.get(bV).redoPeakLine();
                        for(int bW = 0; bW < pW.size();bW++){
                            if(bU==0 && bV==0) pW.get(bW).redoPeakLine();
                            ECCluster cluster = new ECCluster(pU.get(bU),pV.get(bV),pW.get(bW));
                            float err = (float) cluster.getHitPositionError();
                            if(isSingleThreaded)H1_ecEng.get(sector,ind[startLayer-1]+1,0).fill(err);                           
                            if(err<ECCommon.clusterError[ind[startLayer-1]]) {
                            	if(err>maxerr) maxerr=err;
                                if(isSingleThreaded)H1_ecEng.get(sector,ind[startLayer-1]+1,1).fill(err);                               
								if(isGoodCluster(cluster)) {clusters.add(cluster);nclus++;}
                            }
                        }
                    }
                }
            }           
            if(isSingleThreaded) H2_ecEng.get(sector,ind[startLayer-1]+1,1).fill(nclus,maxerr);             
        }

        
        for(int i = 0 ; i < clusters.size(); i++){
            clusters.get(i).setEnergy(
            clusters.get(i).getEnergy(0) + 
            clusters.get(i).getEnergy(1) +
            clusters.get(i).getEnergy(2));      
        }  
        
        return clusters;
    }    
}
