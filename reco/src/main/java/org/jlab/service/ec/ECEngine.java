/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.ec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.Detector;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author gavalian
 */
public class ECEngine extends ReconstructionEngine {

    Detector ecDetector = null;
    
    public ECEngine(){
        super("EC","gavalian","1.0");
    }
        
    @Override
    public boolean processDataEvent(DataEvent de) {
        
        //List<ECStrip>  ecStrips = ECCommon.initStrips(de, ecDetector, this.getConstantsManager(), 10);
        //List<ECStrip>  ecStrips = ECCommon.readStrips(de);//, ecDetector, this.getConstantsManager(), 10);

        List<ECStrip>  ecStrips = ECCommon.initEC(de, ecDetector, this.getConstantsManager(), 10);
        
        //System.out.println(" STRIPS SIZE = " + ecStrips.size());
        
        for(ECStrip strip : ecStrips){
            //System.out.println(strip);
        }
        
        List<ECPeak> ecPeaksALL = ECCommon.createPeaks(ecStrips);
        List<ECPeak> ecPeaks    = ECCommon.processPeaks(ecPeaksALL);
        int peaksOriginal = ecPeaks.size();
        
        //System.out.println(" PEAKS  SIZE = " + ecPeaks.size());
        //for(ECPeak p : ecPeaks){ System.out.println(p);}
        
        /*
        ECPeakAnalysis.splitPeaks(ecPeaks);
        int peaksOriginalSplit = ecPeaks.size();
        System.out.println(String.format("SPLIT PROCEDURE %8d %8d",peaksOriginal,
                peaksOriginalSplit));
               
        for(ECPeak p : ecPeaks){
            //p.redoPeakLine();
            System.out.println(p);
        }
        */
        
        List<ECCluster> cPCAL  = ECCommon.createClusters(ecPeaks,1);
        List<ECCluster> cECIN  = ECCommon.createClusters(ecPeaks,4);
        List<ECCluster> cECOUT = ECCommon.createClusters(ecPeaks,7);
        
        List<ECCluster> cEC   = new ArrayList<ECCluster>();
        
        cEC.addAll(cPCAL);
        //cEC.addAll(cECIN);
        //cEC.addAll(cECOUT);
        
        System.out.println("\n\n\n\n\nEC CLUSTERS SIZE = " + cEC.size());
        if(cEC.size()==2){
            for(ECCluster c : cEC){            
                 System.out.println(c);
            }
        }
        
        //for(ECPeak p : ecPeaks){ System.out.println(p);}
        /*
        for(ECPeak peak : ecPeaks){
            //peak.redoPeakLine();
            if(peak.getMultiplicity()==4){
                System.out.println(peak);
                int stripSplit = peak.getSplitStrip();                
                if(stripSplit>0){
                    List<ECPeak>  twoPeaks = peak.splitPeak(stripSplit);
                    for(ECPeak p : twoPeaks){
                        System.out.println("\t SPLIT PEAK  = " + p);                        
                    }
                }
            }
        }*/
        return true;
    }

    @Override
    public boolean init() {
        String[]  ecTables = new String[]{
            "/calibration/ec/attenuation", 
            "/calibration/ec/gain", 
        };
               
        requireConstants(Arrays.asList(ecTables));
        
        ecDetector =  GeometryFactory.getDetector(DetectorType.EC);
        
        return true;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
