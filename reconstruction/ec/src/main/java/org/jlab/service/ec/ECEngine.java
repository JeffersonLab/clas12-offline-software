package org.jlab.service.ec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.detector.base.DetectorCollection;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author gavalian
 */

public class ECEngine extends ReconstructionEngine {

    public static Logger LOGGER = Logger.getLogger(ECEngine.class.getName());
    
    public ECEngine(){
        super("EC","gavalian","1.0");
    }
    
    @Override
    public boolean processDataEvent(DataEvent de) {
                           
        List<ECStrip>     ecStrips = ECCommon.initEC(de, this.getConstantsManager()); // thresholds, ADC/TDC match        
        List<ECPeak>       ecPeaks = ECCommon.processPeaks(ECCommon.createPeaks(ecStrips)); // thresholds, split peaks -> update peak-lines          
        List<ECCluster> ecClusters = new ArrayList<ECCluster>();  
        
        ecClusters.addAll(ECCommon.createClusters(ecPeaks,1)); //PCAL
        ecClusters.addAll(ECCommon.createClusters(ecPeaks,4)); //ECinner 
        ecClusters.addAll(ECCommon.createClusters(ecPeaks,7)); //ECouter
        
        ECCommon.shareClustersEnergy(ecClusters);  // Repair 2 clusters which share the same peaks
       
        for (int iCl = 0; iCl < ecClusters.size(); iCl++) {
            // As clusters are already defined at this point, we can fill the clusterID of ECStrips belonging to the given cluster
            // === U strips ===
            for (ECStrip curStrip : ecClusters.get(iCl).getPeak(0).getStrips()) {
                curStrip.setClusterId(iCl + 1);
            }
            // === V strips ===
            for (ECStrip curStrip : ecClusters.get(iCl).getPeak(1).getStrips()) {
                curStrip.setClusterId(iCl + 1);
            }
            // === W strips ===
            for (ECStrip curStrip : ecClusters.get(iCl).getPeak(2).getStrips()) {
                curStrip.setClusterId(iCl + 1);
            }
        }
	    
        this.writeHipoBanks(de,ecStrips,ecPeaks,ecClusters);  
        
        if (ECCommon.debug) printDebug(ecStrips,ecPeaks,ecClusters);  
        
        if (ECCommon.isSingleThreaded) {
        	ECCommon.clearMyStructures();
        	getStrips().addAll(ecStrips);
        	getPeaks().addAll(ecPeaks);
        	getClusters().addAll(ecClusters);
        }
        
        return true;
    }
    
    public void printDebug(List<ECStrip> ecStrips, List<ECPeak> ecPeaks, List<ECCluster> ecClusters) {
    	if (ecClusters.size()<5) {
    	for(ECCluster c : ecClusters) {
    		if (c.getStatus()==0) {
    			System.out.println("\nSTRIPS SIZE = " + ecStrips.size());
    			for(ECStrip strip : ecStrips) System.out.println(strip);
    			System.out.println("\nPEAKS  SIZE = " + ecPeaks.size());
    			for(ECPeak p : ecPeaks) System.out.println(p);
    			System.out.println("\nEC CLUSTERS SIZE = " + ecClusters.size());
    			System.out.println(c);	
    		}
    	
    	}
        System.out.println("\nEND\n");
    	}    	
    }
        
    public List<ECStrip> getStrips() {
	    return ECCommon.getMyStrips();    		
    }
    
    public List<ECPeak> getPeaks() {
	    return ECCommon.getMyPeaks();    
    }
    
    public List<ECCluster> getClusters() {
	    return ECCommon.getMyClusters();    
    }    
        
    private void writeHipoBanks(DataEvent de, 
                                List<ECStrip>   strips, 
                                List<ECPeak>    peaks, 
                                List<ECCluster> clusters){
	    
        DataBank bankS = de.createBank("ECAL::hits", strips.size());
        for(int h = 0; h < strips.size(); h++){
            bankS.setByte("sector",     h,  (byte) strips.get(h).getDescriptor().getSector());
            bankS.setByte("layer",      h,  (byte) strips.get(h).getDescriptor().getLayer());
            bankS.setByte("strip",      h,  (byte) strips.get(h).getDescriptor().getComponent());
            bankS.setByte("peakid",     h,  (byte) strips.get(h).getPeakId());
            bankS.setShort("id",        h, (short) strips.get(h).getID());
            bankS.setShort("clusterId", h, (short) strips.get(h).getClusterId());
            bankS.setFloat("energy",    h, (float) strips.get(h).getEnergy());
            bankS.setFloat("time",      h, (float) strips.get(h).getTime());                
        }
       
        DataBank  bankP =  de.createBank("ECAL::peaks", peaks.size());
        for(int p = 0; p < peaks.size(); p++){
            bankP.setByte("sector",  p,  (byte) peaks.get(p).getDescriptor().getSector());
            bankP.setShort("status", p, (short) peaks.get(p).getStatus());
            bankP.setByte("layer",   p,  (byte) peaks.get(p).getDescriptor().getLayer());
            bankP.setFloat("energy", p, (float) peaks.get(p).getEnergy());
            bankP.setFloat("time",   p, (float) peaks.get(p).getTime());
            bankP.setFloat("xo",     p, (float) peaks.get(p).getLine().origin().x());
            bankP.setFloat("yo",     p, (float) peaks.get(p).getLine().origin().y());
            bankP.setFloat("zo",     p, (float) peaks.get(p).getLine().origin().z());
            bankP.setFloat("xe",     p, (float) peaks.get(p).getLine().end().x());
            bankP.setFloat("ye",     p, (float) peaks.get(p).getLine().end().y());
            bankP.setFloat("ze",     p, (float) peaks.get(p).getLine().end().z());
       }   
        
        DataBank bankC = de.createBank("ECAL::clusters", clusters.size());        
        for(int c = 0; c < clusters.size(); c++){
            bankC.setByte("sector",  c,  (byte) clusters.get(c).clusterPeaks.get(0).getDescriptor().getSector());
            bankC.setShort("status", c, (short) clusters.get(c).getStatus());
            bankC.setByte("layer",   c,  (byte) clusters.get(c).clusterPeaks.get(0).getDescriptor().getLayer());
            bankC.setFloat("energy", c, (float) clusters.get(c).getEnergy());           
            bankC.setFloat("time",   c, (float) clusters.get(c).getTime(false)); //true=use FADC timing
            bankC.setByte("idU",     c,  (byte) clusters.get(c).UVIEW_ID);
            bankC.setByte("idV",     c,  (byte) clusters.get(c).VVIEW_ID);
            bankC.setByte("idW",     c,  (byte) clusters.get(c).WVIEW_ID);
            bankC.setFloat("x",      c, (float) clusters.get(c).getHitPosition().x());
            bankC.setFloat("y",      c, (float) clusters.get(c).getHitPosition().y());
            bankC.setFloat("z",      c, (float) clusters.get(c).getHitPosition().z());
            bankC.setFloat("widthU", c,         clusters.get(c).getPeak(0).getMultiplicity());
            bankC.setFloat("widthV", c,         clusters.get(c).getPeak(1).getMultiplicity());
            bankC.setFloat("widthW", c,         clusters.get(c).getPeak(2).getMultiplicity());
            bankC.setInt("coordU",   c,         clusters.get(c).getPeak(0).getCoord());
            bankC.setInt("coordV",   c,         clusters.get(c).getPeak(1).getCoord());
            bankC.setInt("coordW",   c,         clusters.get(c).getPeak(2).getCoord());  
        }
     
        DataBank bankM = de.createBank("ECAL::moments", clusters.size());
        for(int c = 0; c < clusters.size(); c++){
            bankM.setFloat("distU", c, (float) clusters.get(c).clusterPeaks.get(0).getDistanceEdge());
            bankM.setFloat("distV", c, (float) clusters.get(c).clusterPeaks.get(1).getDistanceEdge());
            bankM.setFloat("distW", c, (float) clusters.get(c).clusterPeaks.get(2).getDistanceEdge());
            bankM.setFloat("m1u",   c, (float) clusters.get(c).clusterPeaks.get(0).getMoment());
            bankM.setFloat("m1v",   c, (float) clusters.get(c).clusterPeaks.get(1).getMoment());
            bankM.setFloat("m1w",   c, (float) clusters.get(c).clusterPeaks.get(2).getMoment());
            bankM.setFloat("m2u",   c, (float) clusters.get(c).clusterPeaks.get(0).getMoment2());
            bankM.setFloat("m2v",   c, (float) clusters.get(c).clusterPeaks.get(1).getMoment2());
            bankM.setFloat("m2w",   c, (float) clusters.get(c).clusterPeaks.get(2).getMoment2());
            bankM.setFloat("m3u",   c, (float) clusters.get(c).clusterPeaks.get(0).getMoment3());
            bankM.setFloat("m3v",   c, (float) clusters.get(c).clusterPeaks.get(1).getMoment3());
            bankM.setFloat("m3w",   c, (float) clusters.get(c).clusterPeaks.get(2).getMoment3());
        }
               
        DataBank  bankD =  de.createBank("ECAL::calib", clusters.size());
         for(int c = 0; c < clusters.size(); c++){
            bankD.setByte("sector",  c,  (byte) clusters.get(c).clusterPeaks.get(0).getDescriptor().getSector());
            bankD.setByte("layer",   c,  (byte) clusters.get(c).clusterPeaks.get(0).getDescriptor().getLayer());
            bankD.setFloat("energy", c, (float) clusters.get(c).getEnergy());
            bankD.setFloat("rawEU",  c, (float) clusters.get(c).getRawEnergy(0));
            bankD.setFloat("rawEV",  c, (float) clusters.get(c).getRawEnergy(1));
            bankD.setFloat("rawEW",  c, (float) clusters.get(c).getRawEnergy(2));
            bankD.setFloat("recEU",  c, (float) clusters.get(c).getEnergy(0));
            bankD.setFloat("recEV",  c, (float) clusters.get(c).getEnergy(1));
            bankD.setFloat("recEW",  c, (float) clusters.get(c).getEnergy(2));            
        }
         
         DataBank  bankD2 =  de.createBank("ECAL::calibpass2", clusters.size());
         for(int c = 0; c < clusters.size(); c++){
            bankD2.setByte("sector",  c,  (byte) clusters.get(c).clusterPeaks.get(0).getDescriptor().getSector());
            bankD2.setByte("layer",   c,  (byte) clusters.get(c).clusterPeaks.get(0).getDescriptor().getLayer());
            bankD2.setShort("dbstU",  c, (short) clusters.get(c).clusterPeaks.get(0).getDBStatus());
            bankD2.setShort("dbstV",  c, (short) clusters.get(c).clusterPeaks.get(1).getDBStatus());
            bankD2.setShort("dbstW",  c, (short) clusters.get(c).clusterPeaks.get(2).getDBStatus());
            bankD2.setFloat("energy", c, (float) clusters.get(c).getEnergy());
            bankD2.setFloat("time",   c, (float) clusters.get(c).getTime(false));
            bankD2.setFloat("ftime",  c, (float) clusters.get(c).getTime(true));
            bankD2.setFloat("rawEU",  c, (float) clusters.get(c).getRawEnergy(0));
            bankD2.setFloat("rawEV",  c, (float) clusters.get(c).getRawEnergy(1));
            bankD2.setFloat("rawEW",  c, (float) clusters.get(c).getRawEnergy(2));
            bankD2.setFloat("recEU",  c, (float) clusters.get(c).getEnergy(0));
            bankD2.setFloat("recEV",  c, (float) clusters.get(c).getEnergy(1));
            bankD2.setFloat("recEW",  c, (float) clusters.get(c).getEnergy(2));  
            bankD2.setFloat("recDTU", c, (float) clusters.get(c).getDTime(0));
            bankD2.setFloat("recDTV", c, (float) clusters.get(c).getDTime(1));
            bankD2.setFloat("recDTW", c, (float) clusters.get(c).getDTime(2));  
            bankD2.setFloat("recFTU", c, (float) clusters.get(c).getFTime(0));
            bankD2.setFloat("recFTV", c, (float) clusters.get(c).getFTime(1));
            bankD2.setFloat("recFTW", c, (float) clusters.get(c).getFTime(2));              
        }
         
        de.appendBanks(bankS,bankP,bankC,bankD,bankD2,bankM);

    }
    
    public void setEventNumber(int val) {
    	ECCommon.eventNumber = val;
    } 
    
    public void setDebug(boolean val) {
    	ECCommon.debug = val;
    }
    
    public void setDebugSplit(boolean val) {
    	ECCommon.debugSplit = val;
    }
    
    public void setIsSingleThreaded(boolean val) {
    	ECCommon.isSingleThreaded = val;
    }
    
    public void setSingleEvent(boolean val) {
    	ECCommon.setSingleEvent(val);
    }
        
    public void setIsMC(boolean val) {
    	ECCommon.isMC = val;
    }
    
    public void setConfig(String val) {
    	LOGGER.log(Level.INFO,"ECEngine: Configuration = "+val);
        ECCommon.config = val;    	
    } 
    
    public void setVariation(String val) {
        LOGGER.log(Level.INFO,"ECEngine: Calibration Variation = "+val);
        ECCommon.variation = val;
    } 
    
    public void setGeomVariation(String val) {
        LOGGER.log(Level.INFO,"ECEngine: Geometry Variation = "+val);
        ECCommon.geomVariation = val;
    }  
    
    public void setVeff(float val) {
        LOGGER.log(Level.INFO,"ECEngine: Veff = "+val+" CM/NS");
        ECCommon.veff = val;
    }
    
    public void setPCTrackingPlane(int val) {
    	LOGGER.log(Level.INFO,"ECEngine: PC tracking plane = "+val);
    	ECCommon.pcTrackingPlane = val;
    }
    
    public void setECTrackingPlane(int val) {
    	LOGGER.log(Level.INFO,"ECEngine: EC tracking plane = "+val);
    	ECCommon.ecTrackingPlane = val;
    } 
    
    public void setNewTimeCal(boolean val) {
        LOGGER.log(Level.INFO,"ECEngine: useNewTimeCal = "+val);
    	ECCommon.useNewTimeCal = val;
    }
    
    public void setUseUnsharedEnergy(boolean val) {
    	LOGGER.log(Level.INFO,"ECengine: UseUnsharedEnergy = "+val);   	
    	ECCommon.useUnsharedEnergy = val;
    } 
    
    public void setUnsharedEnergyCut(int val) {
    	LOGGER.log(Level.INFO,"ECengine: UnsharedEnergyCut = "+val);   	
    	ECCommon.UnsharedEnergyCut = val;
    } 
    
    public void setUseUnsharedTime(boolean val) {
    	LOGGER.log(Level.INFO,"ECengine: useUnsharedTime = "+val);
    	ECCommon.useUnsharedTime = val;
    } 
    
    public void setTWCorrections(boolean val) {
    	LOGGER.log(Level.INFO,"ECengine: useTWCorrections = "+val);
    	ECCommon.useTWCorrections = val;
    }
    
    public void setUsePass2Timing(boolean val) {
    	LOGGER.log(Level.INFO,"ECengine: usePass2Timing = "+val);
    	ECCommon.usePass2Timing = val;
    }
    
    public void setUsePass2Recon(boolean val) {
    	LOGGER.log(Level.INFO,"ECengine: usePass2Recon = "+val);
    	ECCommon.usePass2Recon = val;
    }
    
    public void setUseFADCTime(boolean val) {
    	LOGGER.log(Level.INFO,"ECengine: UseFADCTime = "+val);   	
    	ECCommon.useFADCTime = val;
    } 
    
    public void setCCDBGain(boolean val) {
        LOGGER.log(Level.INFO,"ECEngine: useCCDBGain = "+val);
        ECCommon.useCCDBGain = val;    	
    }  
    
    public void setLogParam(double val) {
        LOGGER.log(Level.INFO,"ECEngine: logParam = "+val);
    	ECCommon.logParam = val;
    }
    
    public void setSplitMethod(int val) {
    	LOGGER.log(Level.INFO,"ECEngine: splitMethod = "+val);
        ECCommon.splitMethod = val;    	
    }
    
    public void setSplitThresh(int thr0, int thr1, int thr2) {
    	LOGGER.log(Level.INFO,"ECEngine: Peak Split thresholds = "+thr0+" "+thr1+" "+thr2);
        ECCommon.splitThresh[0] = thr0;
        ECCommon.splitThresh[1] = thr1;
        ECCommon.splitThresh[2] = thr2;   	
    }
    
    public void setTouchID(int val) {
    	LOGGER.log(Level.INFO,"ECEngine: touchID = "+val);
        ECCommon.touchID = val;    	
    } 
    
    public void setStripThresholds(int thr0, int thr1, int thr2) {
        LOGGER.log(Level.INFO,"ECEngine: Strip ADC thresholds = "+thr0+" "+thr1+" "+thr2+" MeV*10");
        ECCommon.stripThreshold[0] = thr0;
        ECCommon.stripThreshold[1] = thr1;
        ECCommon.stripThreshold[2] = thr2;
    }
    
    public void setPeakThresholds(int thr0, int thr1, int thr2) {
        LOGGER.log(Level.INFO,"ECEngine: Peak ADC thresholds = "+thr0+" "+thr1+" "+thr2+" MeV*10");
        ECCommon.peakThreshold[0] = thr0;
        ECCommon.peakThreshold[1] = thr1;
        ECCommon.peakThreshold[2] = thr2;
    }   
    
    public void setClusterCuts(float err0, float err1, float err2) {
        LOGGER.log(Level.INFO,"ECEngine: Cluster Size Cuts = "+err0+" "+err1+" "+err2+" CM");
        ECCommon.clusterSize[0] = err0;
        ECCommon.clusterSize[1] = err1;
        ECCommon.clusterSize[2] = err2;
    }
    
    public void setClusterThresholds(int thr0, int thr1, int thr2) {
        LOGGER.log(Level.INFO,"ECEngine: Cluster peak energy threshold scale factors = "+thr0+" "+thr1+" "+thr2);  
        ECCommon.clusterThreshold[0] = thr0;
        ECCommon.clusterThreshold[1] = thr1;
        ECCommon.clusterThreshold[2] = thr2;    	
    }
    
    public DetectorCollection<H1F>  getHist() {
        return ECCommon.H1_ecEng;
    }
    
    public DetectorCollection<H2F>  getHist2() {
        return ECCommon.H2_ecEng;
    }
    
    @Override
    public boolean init() {
    	
        String[]  ecTables = new String[]{
                "/calibration/ec/attenuation", 
                "/calibration/ec/gain", 
                "/calibration/ec/timing",
                "/calibration/ec/ftime",
                "/calibration/ec/dtime",
                "/calibration/ec/fdjitter",
                "/calibration/ec/time_jitter",
                "/calibration/ec/fadc_offset",
                "/calibration/ec/fadc_global_offset",
                "/calibration/ec/tdc_global_offset",
                "/calibration/ec/global_gain_shift",
                "/calibration/ec/torus_gain_shift",
                "/calibration/ec/global_time_walk",
                "/calibration/ec/effective_velocity",
                "/calibration/ec/fveff",
                "/calibration/ec/dveff",
                "/calibration/ec/tmf_offset",
                "/calibration/ec/tmf_window",
                "/calibration/ec/status"
        };
        
        
        requireConstants(Arrays.asList(ecTables));
        
        getConstantsManager().setVariation(ECCommon.variation);
        String variationName = Optional.ofNullable(this.getEngineConfigString("variation")).orElse("default");
        if(!(ECCommon.geomVariation.equals("default"))) variationName = ECCommon.geomVariation;
        LOGGER.log(Level.INFO,"GEOMETRY VARIATION IS "+variationName);
        ECCommon.ecDetector =  GeometryFactory.getDetector(DetectorType.ECAL,11,variationName);

        setConfig("test");
        
        if(ECCommon.usePass2Recon) {  //for testing pass2 recon peak splitting methods
        setStripThresholds(10,9,8);   //pass1 10,9,8
        setPeakThresholds(18,20,15);  //pass1 18,20,15
        setClusterThresholds(0,0,0);
        setClusterCuts(4.5f,11f,13f); //pass1 7,15,20
        setSplitMethod(3);            //pass1 0=gagik method
        setSplitThresh(3,3,3);        //pass1 3,3,3
        setTouchID(2);                //pass1 1
        }
        
        if(!ECCommon.usePass2Recon) { //use pass1 recon but pass2 timing 
        setStripThresholds(10,9,8);   //pass1 10,9,8
        setPeakThresholds(18,20,15);  //pass1 18,20,15
        setClusterThresholds(0,0,0);
        setClusterCuts(7,15,20);      //pass1 7,15,20
        setUsePass2Timing(true);
        setSplitMethod(0);            //pass1 0=gagik method
        setSplitThresh(3,3,3);        //pass1 3,3,3
        setTouchID(1);                //pass1 1
        }
        
        this.registerOutputBank("ECAL::hits");
        this.registerOutputBank("ECAL::peaks");
        this.registerOutputBank("ECAL::clusters");
        this.registerOutputBank("ECAL::calib");
        this.registerOutputBank("ECAL::calibpass2");
        this.registerOutputBank("ECAL::moments"); 

        if (ECCommon.isSingleThreaded) ECCommon.initHistos();
        
        return true;
    }
    
}
