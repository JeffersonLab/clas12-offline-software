/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.detector.ftof;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractLayer;
import org.jlab.geom.component.ScintillatorMesh;
import org.jlab.geom.component.ScintillatorPaddle;

/**
 *
 * @author gavalian
 */
public class FTOFLayerMesh extends AbstractLayer<ScintillatorMesh>{
    
    protected FTOFLayerMesh(int sectorId, int superlayerId, int layerId) {
        super(DetectorId.FTOF, sectorId, superlayerId, layerId, false);
    }
    
    @Override
    public String getType() {
        return "FTOF Layer";
    }
    
}
