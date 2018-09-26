/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.base;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.Detector;
import org.jlab.geom.detector.dc.DCFactory;
import org.jlab.geom.detector.ec.ECFactory;
import org.jlab.geom.detector.ftof.FTOFFactory;

/**
 *
 * @author gavalian
 */
public class GeometryFactory {
    
    public static int SYSTEM_LOCAL  = 1;
    public static int SYSTEM_TILTED = 2;
    public static int SYSTEM_CLAS   = 3;
    
    //private volatile 
    
    public GeometryFactory(){
        
    }
    
    
    /**
     * Load constants for given detector, with RUN and VARIATION specified
     * @param type detector type
     * @param run run number
     * @param variation ccdb variation
     * @return 
     */
    public static ConstantProvider getConstants(DetectorType type, int run, String variation){
        DatabaseConstantProvider  provider = new DatabaseConstantProvider(run,variation);
        if(type==DetectorType.DC){
            provider.loadTable("/geometry/dc/dc");
            provider.loadTable("/geometry/dc/region");
            provider.loadTable("/geometry/dc/superlayer");
            provider.loadTable("/geometry/dc/layer");
            provider.loadTable("/geometry/dc/alignment");
        }
        
        if(type==DetectorType.ECAL){
            provider.loadTable("/geometry/pcal/pcal");
            provider.loadTable("/geometry/pcal/Uview");
            provider.loadTable("/geometry/pcal/Vview");
            provider.loadTable("/geometry/pcal/Wview");
            provider.loadTable("/geometry/ec/ec");
            provider.loadTable("/geometry/ec/uview");
            provider.loadTable("/geometry/ec/vview");
            provider.loadTable("/geometry/ec/wview");
        }
        
        if(type==DetectorType.FTOF){
            provider.loadTable("/geometry/ftof/panel1a/paddles");        
            provider.loadTable("/geometry/ftof/panel1a/panel");
            provider.loadTable("/geometry/ftof/panel1b/paddles");
            provider.loadTable("/geometry/ftof/panel1b/panel");
            provider.loadTable("/geometry/ftof/panel2/paddles");
            provider.loadTable("/geometry/ftof/panel2/panel");
        }
        
        if(type==DetectorType.BST){
            provider.loadTable("/geometry/bst/region");
            provider.loadTable("/geometry/bst/sector");
            provider.loadTable("/geometry/bst/bst");
        }
        
        if(type==DetectorType.CND){
            provider.loadTable("/geometry/cnd/cnd");
            provider.loadTable("/geometry/cnd/layer");
        }
        
        if(type==DetectorType.FTCAL){
            provider.loadTable("/geometry/ft/ftcal");
        }
        
        if(type==DetectorType.BST){
            provider.loadTable("/geometry/cvt/svt/svt");
            provider.loadTable("/geometry/cvt/svt/region");
            provider.loadTable("/geometry/cvt/svt/support");
            provider.loadTable("/geometry/cvt/svt/fiducial");
            provider.loadTable("/geometry/cvt/svt/material/box");
            provider.loadTable("/geometry/cvt/svt/material/tube");
            provider.loadTable("/geometry/cvt/svt/alignment");

        }
        
        provider.disconnect();
        return provider;
    }
    /**
     * Load constants for given detector, for default RUN=10 and VARIATION=default
     * @param type detector type
     * @return 
     */
    public static ConstantProvider getConstants(DetectorType type){
        return GeometryFactory.getConstants(type, 10, "default");
    }
    
    /**
     * 
     * @param type
     * @return 
     */    
    public static Detector getDetector(DetectorType type){
        return GeometryFactory.getDetector(type, 10, "default");
    }
    /**
     * Load a detector in CLAS coordinate system, for given RUN and VARIATION
     * @param type detector type
     * @param run run number
     * @param variation ccdb variation
     * @return 
     */
    public static Detector getDetector(DetectorType type, int run, String variation){
        ConstantProvider  provider = GeometryFactory.getConstants(type, run, variation);
        if(type==DetectorType.DC){
            DCFactory factory = new DCFactory();
            Detector dc = factory.createDetectorCLAS(provider);
            return dc;
        }
        
        if(type==DetectorType.ECAL){
            ECFactory factory = new ECFactory();
            Detector ec = factory.createDetectorCLAS(provider);
            return ec;
        }
        
        if(type==DetectorType.FTOF){
            FTOFFactory factory = new FTOFFactory();
            Detector ftof = factory.getDetectorGeant4(provider);
            return   ftof;
        }
        
        System.out.println("[GeometryFactory] --->  detector construction for " 
                + type.getName() + "  is not implemented");
        return null;
    }        
    
}
