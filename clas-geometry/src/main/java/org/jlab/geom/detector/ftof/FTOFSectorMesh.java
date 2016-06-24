/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.detector.ftof;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractSector;

/**
 *
 * @author gavalian
 */
public class FTOFSectorMesh extends AbstractSector<FTOFSuperlayerMesh>{

    protected FTOFSectorMesh(int sectorId) {
        super(DetectorId.FTOF, sectorId);
    }
    
    @Override
    public String getType() {
        return "FTOF Sector Mesh";
    }
    
}
