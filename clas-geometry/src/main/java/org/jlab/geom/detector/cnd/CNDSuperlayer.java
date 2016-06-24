package org.jlab.geom.detector.cnd;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractSuperlayer;


/**
 * A Central Neutron Detector (CND) {@link org.jlab.geom.base.Superlayer Superlayer}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.cnd.CNDFactory CNDFactory}<br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.cnd.CNDDetector CNDDetector} → 
 * {@link org.jlab.geom.detector.cnd.CNDSector CNDSector} → 
 * <b>{@link org.jlab.geom.detector.cnd.CNDSuperlayer CNDSuperlayer}</b> → 
 * {@link org.jlab.geom.detector.cnd.CNDLayer CNDLayer} → 
 * {@link org.jlab.geom.component.ScintillatorPaddle ScintillatorPaddle}
 * </code>
 * 
 * @author jnhankins
 */
public class CNDSuperlayer extends AbstractSuperlayer<CNDLayer> {

    protected CNDSuperlayer(int sectorId, int superlayerId) {
        super(DetectorId.CND, sectorId, superlayerId);
    }
    
    /**
     * Returns "CND Superlayer".
     * @return "CND Superlayer"
     */
    @Override
    public String getType() {
        return "CND Superlayer";
    }
}
