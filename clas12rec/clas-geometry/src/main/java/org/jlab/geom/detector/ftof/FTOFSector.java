package org.jlab.geom.detector.ftof;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractSector;


/**
 * A Forward Time of Flight (FTOF) {@link org.jlab.geom.base.Sector Sector}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.ftof.FTOFFactory FTOFFactory}<br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.ftof.FTOFDetector FTOFDetector} → 
 * <b>{@link org.jlab.geom.detector.ftof.FTOFSector FTOFSector}</b> → 
 * {@link org.jlab.geom.detector.ftof.FTOFSuperlayer FTOFSuperlayer} → 
 * {@link org.jlab.geom.detector.ftof.FTOFLayer FTOFLayer} → 
 * {@link org.jlab.geom.component.ScintillatorPaddle ScintillatorPaddle}
 * </code>
 * 
 * @author jnhankins
 */
public class FTOFSector extends AbstractSector<FTOFSuperlayer> {
    
    protected FTOFSector(int sectorId) {
        super(DetectorId.FTOF, sectorId);
    }
    
    /**
     * Returns "FTOF Sector".
     * @return "FTOF Sector"
     */
    @Override
    public String getType() {
        return "FTOF Sector";
    }
}
