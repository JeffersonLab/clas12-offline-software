/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.detector.ftof;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractDetector;

/**
 *
 * @author gavalian
 */
public class FTOFDetectorMesh extends AbstractDetector<FTOFSectorMesh> {
    
    protected FTOFDetectorMesh() {
        super(DetectorId.FTOF);
    }
    
    @Override
    public String getType() {
        return "FTOF Detector Mesh";
    }
    
}
