package org.jlab.service.ec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jlab.detector.banks.RawDataBank;

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
	
    public static Detector        ecDetector = null; 
        
    public static List<ECStrip>     myStrips = new ArrayList<ECStrip>();
    public static List<ECPeak>       myPeaks = new ArrayList<ECPeak>();
    public static List<ECCluster> myClusters = new ArrayList<ECCluster>();
    
    public static int[]      stripThreshold = new int[3];
    public static int[]       peakThreshold = new int[3]; 
    public static int[]    clusterThreshold = new int[3];
    public static float[]       clusterSize = new float[3];
    public static float[]     clusterDeltaT = new float[3];
    
    public static int               touchID = 1;
    public static int           splitMethod = 0;
    public static int       stripSortMethod = 0;
    public static int[]         splitThresh = new int[3];
    
    public static Boolean              isMC = false;
    public static Boolean             debug = false;
    public static Boolean        debugSplit = false;
    public static Boolean  isSingleThreaded = false;
    public static Boolean       singleEvent = false;
    public static Boolean     useNewTimeCal = true;
    public static Boolean useUnsharedEnergy = true;
    public static Boolean  useTWCorrections = true;
    public static Boolean  useDTCorrections = true;
    
    public static Boolean     usePass2Recon = false;
    public static Boolean    usePass2Timing = true;
    public static Boolean    usePass2Energy = true;
    public static int     UnsharedEnergyCut = 6;
    public static Boolean   useUnsharedTime = true;
    public static Boolean       useFADCTime = false;
    public static Boolean         useFTpcal = true;
    public static Boolean       useCCDBGain = true;
    public static double           logParam = 3.0;
    public static String             config = "";
    public static String          variation = "default";
    public static String      geomVariation = "default";
    public static int       pcTrackingPlane = -1;
    public static int       ecTrackingPlane = -1;
    
    public static int           eventNumber = 0;
   
    private static double[] AtoE  = {15,10,10};   // SCALED ADC to Energy in MeV
    private static double[] AtoE5 = {15,5,5};     // For Sector 5 ECAL
    
    public static DetectorCollection<H1F> H1_ecEng = new DetectorCollection<H1F>();
    public static DetectorCollection<H2F> H2_ecEng = new DetectorCollection<H2F>();
    
    static int ind[]  = {0,0,0,1,1,1,2,2,2}; 
    static float               tps = 0.02345f;
    public static float       veff = 18.1f;
    static int nclus, slast;
    static float maxerr;
    
    public  static void initHistos() {
    	int[] bins = {480,240,120};       
        for (int is=1; is<7; is++){
            for (int il=1; il<4; il++) {             
                H1_ecEng.add(is,il, 0, new H1F("Cluster Errors",bins[il-1],-2,30));
                H1_ecEng.add(is,il, 1, new H1F("Cluster Errors",bins[il-1],-2,30));
                if(il==1) {
                H1_ecEng.add(is,il,10, new H1F("Cluster Errors",bins[il-1],-2,30));
                H1_ecEng.add(is,il,11, new H1F("Cluster Errors",bins[il-1],-2,30));
                H1_ecEng.add(is,il,12, new H1F("Cluster Errors",bins[il-1],-2,30));
                H1_ecEng.add(is,il,13, new H1F("Cluster Errors",bins[il-1],-2,30));
                }
                H2_ecEng.add(is,il, 1, new H2F("Cluster Errors",11,1,12,40,0,20));
            }
//            for (int il=1; il<10; il++) {
//                H1_ecEng.add(is,il,14, new H1F("Split Ratio",100,0,10));           	
//            }
        }
    }
    
    public static void resetHistos() {       
        for (int is=1; is<7; is++){
            for (int il=1; il<4; il++) {             
                H1_ecEng.get(is,il,0).reset();
                H1_ecEng.get(is,il,1).reset();
                H2_ecEng.get(is,il,1).reset();
                if(il==1) for (int i=10; i<14; i++) H1_ecEng.get(is,il,i).reset();
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
    
    public static int  getRunNumber(DataEvent de) {
    	return (de.hasBank("RUN::config") ? (int) de.getBank("RUN::config").getInt("run", 0) : 10);
    }
    
    public static List<ECStrip>  initEC(DataEvent event,  ConstantsManager manager){
    	
        int run = getRunNumber(event);
        
        isMC = run<=100;
        
        if(isMC) {usePass2Timing = false; useDTCorrections = false; useFTpcal = false;}
        
        manager.setVariation(variation);

        IndexedTable   atten1 = manager.getConstants(run, "/calibration/ec/attenuation");
        IndexedTable   atten2 = manager.getConstants(run, "/calibration/ec/atten");    //pass2
        IndexedTable     gain = manager.getConstants(run, "/calibration/ec/gain");
        IndexedTable    itime = manager.getConstants(run, "/calibration/ec/timing"); 
        IndexedTable    ftime = manager.getConstants(run, "/calibration/ec/ftime");    //pass2
        IndexedTable    dtime = manager.getConstants(run, "/calibration/ec/dtime");    //pass2
        IndexedTable     veff = manager.getConstants(run, "/calibration/ec/effective_velocity");
        IndexedTable      fev = manager.getConstants(run, "/calibration/ec/fveff");    //pass2
        IndexedTable      dev = manager.getConstants(run, "/calibration/ec/dveff");    //pass2
        IndexedTable      fdj = manager.getConstants(run, "/calibration/ec/fdjitter"); //pass2		
        IndexedTable      ggs = manager.getConstants(run, "/calibration/ec/global_gain_shift");
        IndexedTable      gtw = manager.getConstants(run, "/calibration/ec/global_time_walk");
        IndexedTable      tgo = manager.getConstants(run, "/calibration/ec/tdc_global_offset");		
        IndexedTable   r2gain = manager.getConstants(2,   "/calibration/ec/gain");
    
        if(singleEvent) resetHistos();        
        
        List<ECStrip>  ecStrips = null;
        
        ecStrips = ECCommon.readStripsHipo(event, run, manager);  
        
        if(ecStrips==null) return new ArrayList<ECStrip>();
        
        stripSortMethod=0; Collections.sort(ecStrips); //sort by sector, layer, component
        
        for(ECStrip strip : ecStrips){
            int sector    = strip.getDescriptor().getSector();
            int layer     = strip.getDescriptor().getLayer();     //1,2,3=PCAL 4,5,6=ECIN 7,8,9=ECOU
            int component = strip.getDescriptor().getComponent();
            int superlayer = (int) ((layer-1)/3);                 //0=PCAL 1=ECIN 2=ECOU
            int localLayer = (layer-1)%3;                         //0=U 1=V 2=W
           
            int pcalz = pcTrackingPlane!=-1 ? pcTrackingPlane:DetectorLayer.PCAL_Z;
            int ecinz = ecTrackingPlane!=-1 ? ecTrackingPlane:DetectorLayer.EC_INNER_Z;
            int ecouz = ecTrackingPlane!=-1 ? ecTrackingPlane:DetectorLayer.EC_OUTER_Z;
            
            int off = superlayer==0 ? pcalz : (superlayer==1 ? ecinz : ecouz);
            
            Layer detLayer = ecDetector.getSector(sector-1).getSuperlayer(superlayer).getLayer(localLayer+off); //localLayer+off=9,10,11 for U,V,W planes
            
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
            
            if(!usePass2Energy) { 
            strip.setAttenuation(atten1.getDoubleValue("A", sector,layer,component),
                                 atten1.getDoubleValue("B", sector,layer,component),
                                 atten1.getDoubleValue("C", sector,layer,component),
                                 0,
                               100);
            }
            
            if(usePass2Energy) { 
            strip.setAttenuation(atten2.getDoubleValue("A", sector,layer,component),
                                 atten2.getDoubleValue("B", sector,layer,component),
                                 atten2.getDoubleValue("C", sector,layer,component),
                                 atten2.getDoubleValue("D", sector,layer,component),
                                 atten2.getDoubleValue("E", sector,layer,component));
            }
            
            double ccdbGain =   gain.getDoubleValue("gain", sector,layer,component)*ggs.getDoubleValue("gain_shift",sector,layer,0);
            double run2Gain = r2gain.getDoubleValue("gain", sector,layer,component);  
            
            strip.setGain(useCCDBGain ? ccdbGain : run2Gain);             
            strip.setDtimeGlobalTimeWalk(gtw.getDoubleValue("time_walk",sector,layer,0)); 
            
            strip.setVeff(veff.getDoubleValue("veff",sector,layer,component));            
            strip.setDVeff(dev.getDoubleValue("veff",sector,layer,component));
            strip.setFVeff(fev.getDoubleValue("veff",sector,layer,component));

            if(!useTWCorrections) { //For TWC calibration must start from scratch as corrections cannot be iterated
                  strip.setITime(itime.getDoubleValue("a0", sector, layer, component),
                		         itime.getDoubleValue("a1", sector, layer, component),
                                 0,
                                 0,
                                 0);

                  strip.setDTime(dtime.getDoubleValue("a0", sector, layer, component),
		                         dtime.getDoubleValue("a1", sector, layer, component),
                                 0,
                                 0,
                                 0, 
                                 0, 
                                 0, 
                                 0, 
                                 0); 
                
                  strip.setFTime(ftime.getDoubleValue("a0", sector, layer, component),
                                 1,
                		         0,
                		         0,
                		         0,
                		         0,
                		         0);
            }
            
            if(useTWCorrections) {    
                  strip.setITime(itime.getDoubleValue("a0", sector, layer, component),
                		         itime.getDoubleValue("a1", sector, layer, component),
                		         itime.getDoubleValue("a2", sector, layer, component),
                		         itime.getDoubleValue("a3", sector, layer, component),
                		         itime.getDoubleValue("a4", sector, layer, component)); 
                
                  strip.setDTime(dtime.getDoubleValue("a0", sector, layer, component),
       		                     dtime.getDoubleValue("a1", sector, layer, component),
                                 dtime.getDoubleValue("a2", sector, layer, component),
                                 dtime.getDoubleValue("a3", sector, layer, component),
                                 dtime.getDoubleValue("a4", sector, layer, component), 
                                 dtime.getDoubleValue("a5", sector, layer, component), 
                                 dtime.getDoubleValue("a6", sector, layer, component), 
                                 dtime.getDoubleValue("a7", sector, layer, component), 
                                 dtime.getDoubleValue("a8", sector, layer, component));
                  
                  strip.setFTime(ftime.getDoubleValue("a0", sector, layer, component),
                		         1,
                		         ftime.getDoubleValue("a2", sector, layer, component),
                                 ftime.getDoubleValue("a3", sector, layer, component),
                                 ftime.getDoubleValue("a4", sector, layer, component),
                                 ftime.getDoubleValue("a5", sector, layer, component),
                                 ftime.getDoubleValue("a6", sector, layer, component));
            }

            
            strip.setDtimeGlobalTimingOffset(tgo.getDoubleValue("offset",0,0,0)); //global shift of TDC acceptance window
            strip.setFtimeGlobalTimingOffset(tgo.getDoubleValue("offset",0,0,0)+  //global shift of TDC acceptance window
            		                (double) fdj.getDoubleValue("offset",0,0,0)); //jitter correction (usually +/- 2ns)
            
        }  
        
        return ecStrips;
    }
        
    public static List<ECStrip>  readStripsHipo(DataEvent event, int run, ConstantsManager manager){ 
    	
        IndexedList<List<Integer>>  tdcs = new IndexedList<List<Integer>>(3);          

    	List<ECStrip>  strips = new ArrayList<ECStrip>();
      	
        IndexedTable    jitter = manager.getConstants(run, "/calibration/ec/time_jitter");
        IndexedTable        fo = manager.getConstants(run, "/calibration/ec/fadc_offset");        // TDC-FADC offset (sector, layer) 
        IndexedTable       tmf = manager.getConstants(run, "/calibration/ec/tmf_offset");         // TDC-FADC offset (sector, layer, PMT)
        IndexedTable       fgo = manager.getConstants(run, "/calibration/ec/fadc_global_offset"); // TDC-FADC global offset (trigger)
        IndexedTable    tmfcut = manager.getConstants(run, "/calibration/ec/tmf_window");         // TDC-FADC cut
        IndexedTable       gtw = manager.getConstants(run, "/calibration/ec/global_time_walk");
        IndexedTable    status = manager.getConstants(run, "/calibration/ec/status");
        
        double PERIOD  = jitter.getDoubleValue("period",0,0,0);
        int    PHASE   = jitter.getIntValue("phase",0,0,0); 
        int    CYCLES  = jitter.getIntValue("cycles",0,0,0);        
        float FTOFFSET = (float) fgo.getDoubleValue("global_offset",0,0,0); //global shift of trigger time
        float  TMFCUT  = (float) tmfcut.getDoubleValue("window", 0,0,0); //acceptance window for TDC-FADC cut
        
        int triggerPhase = 0;
    	
        if(CYCLES>0&&event.hasBank("RUN::config")==true){
            DataBank bank = event.getBank("RUN::config");
            long timestamp = bank.getLong("timestamp", 0);
            triggerPhase = (int) (PERIOD*((timestamp+PHASE)%CYCLES));
        }

        if(event.hasBank("ECAL::tdc")==true){
            RawDataBank  bank = new RawDataBank("ECAL::tdc");
            bank.read(event);
            //DataBank  bank = event.getBank("ECAL::tdc");
            for(int i = 0; i < bank.rows(); i++){
                int  is = bank.getByte("sector",i);
                int  il = bank.getByte("layer",i);
                int  ip = bank.getShort("component",i);    
                int tdc = bank.getInt("TDC",i);
                
                if(status.getIntValue("status",is,il,ip)==2) continue; //for MC use only
                
                if(tdc>0) {                       
                    if(!tdcs.hasItem(is,il,ip)) tdcs.add(new ArrayList<Integer>(),is,il,ip);
                        tdcs.getItem(is,il,ip).add(tdc);       
                }
            }
        }        
        
        if(event.hasBank("ECAL::adc")==true){
            RawDataBank bank = new RawDataBank("ECAL::adc");
            bank.read(event);
            //DataBank bank = event.getBank("ECAL::adc");
            for(int i = 0; i < bank.rows(); i++){
                int  is = bank.getByte("sector", i);
                int  il = bank.getByte("layer", i); 
                int  ip = bank.getShort("component", i);
                int adc = bank.getInt("ADC", i);
                float t = bank.getFloat("time", i) + (float) tmf.getDoubleValue("offset",is,il,ip) // TDC-FADC offset (sector, layer, PMT)
                                                   + (float)  fo.getDoubleValue("offset",is,il,0); // TDC-FADC offset (sector, layer) 
                
                if (status.getIntValue("status",is,il,ip)==3) continue; //for MC use only
                
                ECStrip  strip = new ECStrip(is, il, ip); 
                
                strip.setStatus(status.getIntValue("status",is,il,ip));                
                strip.setADC(adc);
                strip.setTriggerPhase(triggerPhase);
                strip.setID(i+1);

                if(!isGoodStrip(strip)) continue;
                
                strips.add(strip); 
                
                float ftdc_corr = t+FTOFFSET;
                strip.setTADC(ftdc_corr);
                
                float  tmax = 1000; int tdc = 0;
                               
                if (tdcs.hasItem(is,il,ip)) {
                    float radc = (float)Math.sqrt(adc);
                    for (float tdcc : tdcs.getItem(is,il,ip)) {
                         float tdif = tps*tdcc - triggerPhase - (float)gtw.getDoubleValue("time_walk",is,il,0)/radc - ftdc_corr; 
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
        
        if(!(stripList.size()>1)) return peakList;  //Require minimum of 2 strips/event to reject uncorrelated hot channels 
        
        peakList.add(new ECPeak(stripList.get(0))); //Seed the first peak with the first strip
        
        int n=1;
        for(int loop = 1; loop < stripList.size(); loop++){ //Loop over remaining strips 
        	ECStrip s = stripList.get(loop); boolean stripAdded = false;                
            for(ECPeak peak : peakList)  {
            	if(peak.addStrip(s)) { //Add adjacent strip to newly seeded peak
            		stripAdded = true; s.setStripID(n); peak.setimap(n++, s.getADC()); //Set indices to assist peak splitting
            	} 
            }
            if(!stripAdded) {peakList.add(new ECPeak(s)); n=1;} //Non-adjacent strip seeds new peak
        }

        for(int loop = 0; loop < peakList.size(); loop++) peakList.get(loop).setPeakId(loop+1);
 
        return peakList;
    } 
       
    public static List<ECPeak>  processPeaks(List<ECPeak> peaks){
    	
        //System.out.println("processing peaks");
        
        List<ECPeak> peakList = new ArrayList<ECPeak>();
        
        for(ECPeak p : peaks) if(isGoodPeak(p)) peakList.add(p);
        //ECPeakAnalysis.splitPeaks(peakList);       //Split peak if strip members have an adc valley   
        ECPeakAnalysis.splitPeaksAlternative5(peakList);    // new Way of splitting the peaks as of 2/20/2023 
                                                //Split peak if strip members have an adc valley
                                                
        //ECPeakAnalysis.doPeakCleanup(peaks);
        for(ECPeak p : peakList) p.redoPeakLine(); //Find new peak lines after splitPeaks
                
        return peakList;
    }
    
    public static List<ECPeak>  getPeaks(int sector, int layer, List<ECPeak> peaks){
    	
        List<ECPeak> peakList = new ArrayList<ECPeak>();
        
        for(ECPeak peak : peaks) if(peak.getDescriptor().getSector()==sector && peak.getDescriptor().getLayer()==layer) peakList.add(peak);

        return peakList;
    }
    
    public static List<ECCluster>  createClusters(List<ECPeak>  peaks, int layer){ 
       return filterClusters(processClusters(getClusters(peaks,layer)));       
    }
    
    public static boolean goodPeaks(int sector, int layer, List<ECPeak> peaks) {
    	List<ECPeak>  pU = ECCommon.getPeaks(sector, layer,   peaks);
        List<ECPeak>  pV = ECCommon.getPeaks(sector, layer+1, peaks);
        List<ECPeak>  pW = ECCommon.getPeaks(sector, layer+2, peaks); 
        return pU.size()>0 && pV.size()>0 && pW.size()>0;
    }
    
    public static List<ECCluster> getClusters(List<ECPeak> peaks, int layer) {
    	
        List<ECCluster> clusters = new ArrayList<ECCluster>();
        
        for(int p = 0; p < peaks.size(); p++) peaks.get(p).setOrder(p+1);
        
        for(int sector = 1; sector <= 6; sector++){ 
        	if(!goodPeaks(sector,layer,peaks)) continue;
            nclus=0; maxerr=0;
            for (ECPeak pu : getPeaks(sector,layer,peaks)) {
                for (ECPeak pv : getPeaks(sector,layer+1,peaks)) {
                    for (ECPeak pw : getPeaks(sector,layer+2,peaks)) {
                    	ECCluster c = new ECCluster(pu,pv,pw);
                        clusters.add(c); if(isSingleThreaded) processSingleThreaded(c);
                    }
                }
            }
            if(isSingleThreaded) H2_ecEng.get(sector,ind[layer-1]+1,1).fill(nclus,maxerr);
        }        
        return clusters;        
    }
            
    public static List<ECCluster>  processClusters(List<ECCluster> clusters) { 
    	
    	for (ECCluster c : clusters) {
    		int l = c.getDescriptor().getLayer();
    		c.setError(c.getClusterSize()>clusterSize[ind[l-1]]); //flag clusters that exceed the size limit
    	}

    	return clusters;   	
    }
    
    public static List<ECCluster>  filterClusters(List<ECCluster> clusters) {
    	
        List<ECCluster> filtClusters = new ArrayList<ECCluster>();

    	for (ECCluster c : clusters) if(!c.getError() && isGoodCluster(c)) filtClusters.add(c);            	
        for (ECCluster c : filtClusters) c.setEnergy();

        return filtClusters;   
    }
    
    public static void processSingleThreaded(ECCluster c) {  //not used in clara  
    	int s = c.getDescriptor().getSector(); 
    	int l = c.getDescriptor().getLayer();
    	List<ECPeak> p = c.getPeaks();
    	float err = (float) c.getClusterSize();
    	boolean gc = err < clusterSize[ind[l-1]];
        int zone = getZone(ind[l-1],p.get(0).getMaxStrip(),p.get(1).getMaxStrip(),p.get(2).getMaxStrip());
    	if(l==1 && zone<2) H1_ecEng.get(s,1,10+zone).fill(err);
    	if(l==1 && zone>1) H1_ecEng.get(s,1,12).fill(err); 
    	                          H1_ecEng.get(s,ind[l-1]+1,0).fill(err); 
    	if(gc&&isGoodCluster(c)) {H1_ecEng.get(s,ind[l-1]+1,1).fill(err); nclus++; if(err>maxerr) maxerr=err;} 
    }    

    public static void shareClustersEnergy(List<ECCluster> clusters){
        
        for(int i = 0; i < clusters.size() - 1; i++){
            for(int k = i+1 ; k < clusters.size(); k++){
                byte sharedView = (byte) clusters.get(i).sharedView(clusters.get(k)); // 0,1,2,3,4,5 <=> U,V,W,UV,UW,VW
                if(sharedView>=0 && sharedView<UnsharedEnergyCut){
                	clusters.get(i).setSharedCluster(k); clusters.get(i).setSharedView(sharedView+1);
                	clusters.get(k).setSharedCluster(i); clusters.get(k).setSharedView(sharedView+1);                  
                	if(useUnsharedEnergy) ECCluster.shareEnergy(clusters.get(i), clusters.get(k), sharedView+1);
                }
            }
        }        
    }
    
    public static boolean isGoodStrip(ECStrip s) {
        int adc = s.getADC();
        int lay = s.getDescriptor().getLayer(); 
        int sec = s.getDescriptor().getSector();
        double sca = (sec==5)?AtoE5[ind[lay-1]]:AtoE[ind[lay-1]];
        return adc>sca*stripThreshold[ind[lay-1]];	
    }
       
    public static boolean isGoodPeak(ECPeak p) {
        int adc = p.getADC();
        int lay = p.getDescriptor().getLayer();
        int sec = p.getDescriptor().getSector();
        double sca = (sec==5)?AtoE5[ind[lay-1]]:AtoE[ind[lay-1]];
    	return adc>sca*peakThreshold[ind[lay-1]]; //adc threshold (uncorrected energy MeV*10)
    }  
    
    public static boolean isGoodCluster(ECCluster c) {    	
    	int l = c.getDescriptor().getLayer();    	     	
    	for (int i=0; i<3; i++) {   		
    		if(clusterThreshold[ind[l-1]]==0) return true;
    		double thr = 0.1*clusterThreshold[ind[l-1]]*peakThreshold[ind[l-1]]; //cluster thrsh. fraction of peak
    		if(c.getEnergy(i)*1e3<thr) return false;  
    	}       
    	return true;
    }
    
    public static int getZone(int layer, int u, int v, int w){
    	if (layer>0)          return 0;
        if (u<53&&v>15&&w>15) return 0;
        if (u>52&&v>15&&w>15) return 1;
        if (v<16)             return 2;
        if (w<16)             return 3;
        return 0;
    }
    
    public static List<ECCluster>  OldcreateClusters(List<ECPeak>  peaks, int startLayer){

        List<ECCluster>   clusters = new ArrayList<ECCluster>();
        
        for(int p = 0; p < peaks.size(); p++) peaks.get(p).setOrder(p+1);

        for(int sector = 1; sector <= 6; sector++){

            List<ECPeak>  pU = ECCommon.getPeaks(sector, startLayer,   peaks);
            List<ECPeak>  pV = ECCommon.getPeaks(sector, startLayer+1, peaks);
            List<ECPeak>  pW = ECCommon.getPeaks(sector, startLayer+2, peaks);
            /*System.out.println("-------->  peaks are found " +
                    " U " + pU.size() +
                    " V " + pV.size() +
                    " W " + pW.size()
            );*/
            
           nclus=0; maxerr=0;
           
           if(pU.size()>0&&pV.size()>0&&pW.size()>0){  //U,V,W peaks required for cluster
                for(int bU = 0; bU < pU.size();bU++){
                    pU.get(bU).redoPeakLine();
                    for(int bV = 0; bV < pV.size();bV++){
                        if(bU==0) pV.get(bV).redoPeakLine();
                        for(int bW = 0; bW < pW.size();bW++){
                            if(bU==0 && bV==0) pW.get(bW).redoPeakLine();
                            ECCluster cluster = new ECCluster(pU.get(bU),pV.get(bV),pW.get(bW));
                            float err = (float) cluster.getClusterSize();
                            if(isSingleThreaded) {                           
//                            	if (pU.get(bU).getSplitRatio()>0) H1_ecEng.get(sector,startLayer,  14).fill((pU.get(bU).getSplitRatio()));
//                            	if (pV.get(bV).getSplitRatio()>0) H1_ecEng.get(sector,startLayer+1,14).fill((pV.get(bV).getSplitRatio()));
//                            	if (pW.get(bW).getSplitRatio()>0) H1_ecEng.get(sector,startLayer+2,14).fill((pW.get(bW).getSplitRatio()));
                                int zone = getZone(ind[startLayer-1],pU.get(bU).getMaxStrip(),pV.get(bV).getMaxStrip(),pW.get(bW).getMaxStrip());
                            	H1_ecEng.get(sector,ind[startLayer-1]+1,0).fill(err); 
                            	if(startLayer==1 && zone<2) H1_ecEng.get(sector,1,10+zone).fill(err);
                            	if(startLayer==1 && zone>1) H1_ecEng.get(sector,1,12).fill(err);
                            }
                            if(err<clusterSize[ind[startLayer-1]]) {
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

        for (ECCluster c : clusters) c.setEnergy();
        
        return clusters;
    }    
}
