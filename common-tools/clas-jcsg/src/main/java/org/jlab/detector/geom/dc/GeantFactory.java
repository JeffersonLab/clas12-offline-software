/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geom.dc;

import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.detector.dc.DCDetector;

/**
 *
 * @author gavalian
 */
public class GeantFactory {
    public static void main(String[] args){
        ConstantProvider cp = GeometryFactory.getConstants(DetectorType.DC, 4013, "default");
        
        DCGeantFactory factory = new DCGeantFactory();
        
        DCDetector detector = factory.createDetectorCLAS(cp);
        
        detector.show();
    }
}
