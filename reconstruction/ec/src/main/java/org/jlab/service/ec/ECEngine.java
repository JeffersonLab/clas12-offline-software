package org.jlab.service.ec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.detector.base.DetectorCollection;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.geom.base.Detector;
import org.jlab.groot.data.H1F;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author gavalian
 */
public class ECEngine extends ReconstructionEngine {
    
    Detector              ecDetector = null;
    public Boolean             debug = false;
    public Boolean  isSingleThreaded = false;
    public Boolean       singleEvent = false;
    public Boolean              isMC = false;
    int                       calrun = 2;
    
    public ECEngine(){
        super("EC","gavalian","1.0");
    }
    
    @Override
    public boolean processDataEvent(DataEvent de) {
           
        ECCommon.setDebug(debug);
        ECCommon.setisSingleThreaded(isSingleThreaded);
        ECCommon.setSingleEvent(singleEvent);

        int runNo = 10;
        
        if(de.hasBank("RUN::config")==true){
            DataBank bank = de.getBank("RUN::config");
            runNo = bank.getInt("run", 0);
            if (runNo<=0) {
                System.err.println("ECEngine:  got run <= 0 in RUN::config, skipping event.");
                return false;
            }
        }
                
        List<ECStrip>     ecStrips = ECCommon.initEC(de,  ecDetector, this.getConstantsManager(), runNo); // thresholds, ADC/TDC match        
        List<ECPeak>      ecPeaks  = ECCommon.processPeaks(ECCommon.createPeaks(ecStrips)); // thresholds, split peaks -> update peak-lines          
        List<ECCluster> ecClusters = new ArrayList<ECCluster>();
        ecClusters.addAll(ECCommon.createClusters(ecPeaks,1)); //PCAL
        ecClusters.addAll(ECCommon.createClusters(ecPeaks,4)); //ECinner 
        ecClusters.addAll(ECCommon.createClusters(ecPeaks,7)); //ECouter
        
        ECCommon.shareClustersEnergy(ecClusters);  // Repair 2 clusters which share the same peaks
       
        if (debug) {
            System.out.println(" STRIPS SIZE = " + ecStrips.size());
            for(ECStrip strip : ecStrips) System.out.println(strip);
            System.out.println(" PEAKS  SIZE = " + ecPeaks.size());
            for(ECPeak p : ecPeaks) System.out.println(p);
            System.out.println("\n\n\n\n\nEC CLUSTERS SIZE = " + ecClusters.size());
            if(ecClusters.size()==2) {for(ECCluster c : ecClusters) System.out.println(c);}
        }
	    
        this.writeHipoBanks(de,ecStrips,ecPeaks,ecClusters);  
        
        if (isSingleThreaded) {
        	ECCommon.clearMyStructures();
        	getStrips().addAll(ecStrips);
        	getPeaks().addAll(ecPeaks);
        	getClusters().addAll(ecClusters);
        }
        
        return true;
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
            bankS.setByte("sector",  h,  (byte) strips.get(h).getDescriptor().getSector());
            bankS.setByte("layer",   h,  (byte) strips.get(h).getDescriptor().getLayer());
            bankS.setByte("strip",   h,  (byte) strips.get(h).getDescriptor().getComponent());
            bankS.setByte("peakid",  h,  (byte) strips.get(h).getPeakId());
            bankS.setFloat("energy", h, (float) strips.get(h).getEnergy());
            bankS.setFloat("time",   h, (float) strips.get(h).getTime());                
        }
       
        DataBank  bankP =  de.createBank("ECAL::peaks", peaks.size());
        for(int p = 0; p < peaks.size(); p++){
            bankP.setByte("sector",  p,  (byte) peaks.get(p).getDescriptor().getSector());
            bankP.setByte("layer",   p,  (byte) peaks.get(p).getDescriptor().getLayer());
            bankP.setFloat("xo",     p, (float) peaks.get(p).getLine().origin().x());
            bankP.setFloat("yo",     p, (float) peaks.get(p).getLine().origin().y());
            bankP.setFloat("zo",     p, (float) peaks.get(p).getLine().origin().z());
            bankP.setFloat("xe",     p, (float) peaks.get(p).getLine().end().x());
            bankP.setFloat("ye",     p, (float) peaks.get(p).getLine().end().y());
            bankP.setFloat("ze",     p, (float) peaks.get(p).getLine().end().z());
            bankP.setFloat("energy", p, (float) peaks.get(p).getEnergy());
            bankP.setFloat("time",   p, (float) peaks.get(p).getTime());
        }   
        
        DataBank bankC = de.createBank("ECAL::clusters", clusters.size());        
        for(int c = 0; c < clusters.size(); c++){
            bankC.setByte("sector",  c,  (byte) clusters.get(c).clusterPeaks.get(0).getDescriptor().getSector());
            bankC.setByte("layer",   c,  (byte) clusters.get(c).clusterPeaks.get(0).getDescriptor().getLayer());
            bankC.setFloat("energy", c, (float) clusters.get(c).getEnergy());
            bankC.setFloat("time",   c, (float) clusters.get(c).getRawADCTime());
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
            bankM.setFloat("m1u", c,   (float) clusters.get(c).clusterPeaks.get(0).getMoment());
            bankM.setFloat("m1v", c,   (float) clusters.get(c).clusterPeaks.get(1).getMoment());
            bankM.setFloat("m1w", c,   (float) clusters.get(c).clusterPeaks.get(2).getMoment());
            bankM.setFloat("m2u", c,   (float) clusters.get(c).clusterPeaks.get(0).getMoment2());
            bankM.setFloat("m2v", c,   (float) clusters.get(c).clusterPeaks.get(1).getMoment2());
            bankM.setFloat("m2w", c,   (float) clusters.get(c).clusterPeaks.get(2).getMoment2());
            bankM.setFloat("m3u", c,   (float) clusters.get(c).clusterPeaks.get(0).getMoment3());
            bankM.setFloat("m3v", c,   (float) clusters.get(c).clusterPeaks.get(1).getMoment3());
            bankM.setFloat("m3w", c,   (float) clusters.get(c).clusterPeaks.get(2).getMoment3());
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
         
         de.appendBanks(bankS,bankP,bankC,bankD,bankM);
//         de.appendBanks(bankS,bankP,bankC,bankD);

    }
   
    public void setCalRun(int runno) {
        System.out.println("ECEngine: Calibration Run Number = "+runno);
        this.calrun = runno;
    }
    
    public void setVariation(String variation) {
        System.out.println("ECEngine: Variation = "+variation);
        ECCommon.variation = variation;
    } 
    
    public void setVeff(float veff) {
        System.out.println("ECEngine: Veff = "+veff);
    	    ECCommon.veff = veff;
    }
    
    public void setNewTimeCal(boolean val) {
        System.out.println("ECEngine: useNewTimeCal = "+val);
    	ECCommon.useNewTimeCal = val;
    }
    
    public void setStripThresholds(int thr0, int thr1, int thr2) {
        System.out.println("ECEngine: Strip ADC thresholds = "+thr0+" "+thr1+" "+thr2);
        ECCommon.stripThreshold[0] = thr0;
        ECCommon.stripThreshold[1] = thr1;
        ECCommon.stripThreshold[2] = thr2;
    }
    
    public void setPeakThresholds(int thr0, int thr1, int thr2) {
        System.out.println("ECEngine: Peak ADC thresholds = "+thr0+" "+thr1+" "+thr2);
        ECCommon.peakThreshold[0] = thr0;
        ECCommon.peakThreshold[1] = thr1;
        ECCommon.peakThreshold[2] = thr2;
    }   
    
    public void setClusterCuts(float err0, float err1, float err2) {
        System.out.println("ECEngine: Cluster Dalitz Cuts = "+err0+" "+err1+" "+err2);
        ECCommon.clusterError[0] = err0;
        ECCommon.clusterError[1] = err1;
        ECCommon.clusterError[2] = err2;
    }
    
    public DetectorCollection<H1F>  getHist() {
        return ECCommon.H1_ecEng;
    }
    
    @Override
    public boolean init() {
    	
        String[]  ecTables = new String[]{
            "/calibration/ec/attenuation", 
            "/calibration/ec/gain", 
            "/calibration/ec/timing",
            "/calibration/ec/time_jitter",
            "/calibration/ec/fadc_offset",
            "/calibration/ec/fadc_global_offset",
            "/calibration/ec/tdc_global_offset",
            "/calibration/ec/global_gain_shift",
            "/calibration/ec/global_time_walk",
            "/calibration/ec/effective_velocity",
            "/calibration/ec/tmf_offset",
            "/calibration/ec/tmf_window"
        };
        
        
        requireConstants(Arrays.asList(ecTables));
        getConstantsManager().setVariation(ECCommon.variation);
        String variationName = Optional.ofNullable(this.getEngineConfigString("variation")).orElse("default");
        ecDetector =  GeometryFactory.getDetector(DetectorType.ECAL,11,variationName);

        setCalRun(2);
        setStripThresholds(10,9,8);
        setPeakThresholds(18,20,15);
        setClusterCuts(7,15,20);
        
        if (isSingleThreaded) ECCommon.initHistos();
        return true;
    }
    
}
