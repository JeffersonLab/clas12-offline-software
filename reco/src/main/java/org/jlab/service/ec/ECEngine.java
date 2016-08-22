/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.ec;

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
        
        List<ECStrip>  ecStrips = ECCommon.initStrips(de, ecDetector, this.getConstantsManager(), 10);
        
        for(ECStrip strip : ecStrips){
            //System.out.println(strip);
        }
        
        
        
        List<ECPeak> ecPeaks = ECCommon.createPeaks(ecStrips);
        int peaksOriginal = ecPeaks.size();
        //for(ECPeak p : ecPeaks){ System.out.println(p);}
        
        
        ECPeakAnalysis.splitPeaks(ecPeaks);
        int peaksOriginalSplit = ecPeaks.size();
        System.out.println(String.format("SPLIT PROCEDURE %8d %8d",peaksOriginal,
                peaksOriginalSplit));
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
