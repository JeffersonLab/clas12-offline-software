package org.jlab.geom.detector.alert.ATOF;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractSector;


/**
 * A Central Neutron Detector (CND) {@link org.jlab.geom.base.Sector Sector}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.cnd.CNDFactory CNDFactory}<br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.cnd.CNDDetector CNDDetector} → 
 * <b>{@link org.jlab.geom.detector.cnd.CNDSector CNDSector}</b> → 
 * {@link org.jlab.geom.detector.cnd.CNDSuperlayer CNDSuperlayer} → 
 * {@link org.jlab.geom.detector.cnd.CNDLayer CNDLayer} → 
 * {@link org.jlab.geom.component.ScintillatorPaddle ScintillatorPaddle}
 * </code>
 * 
 * @author jnhankins
 */
public class MYSector extends AbstractSector<MYSuperlayer> {
    
    protected MYSector(int sectorId) {
        super(DetectorId.CND, sectorId);
    }
    
    /**
     * Returns "CND Sector".
     * @return "CND Sector"
     */
    @Override
    public String getType() {
        return "CND Sector";
    }
}
