package org.jlab.geom.detector.cnd;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractDetector;


/**
 * A Central Neutron Detector (CND) {@link org.jlab.geom.base.Detector Detector}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.cnd.CNDFactory CNDFactory}<br> 
 * Hierarchy: 
 * <code>
 * <b>{@link org.jlab.geom.detector.cnd.CNDDetector CNDDetector}</b> → 
 * {@link org.jlab.geom.detector.cnd.CNDSector CNDSector} → 
 * {@link org.jlab.geom.detector.cnd.CNDSuperlayer CNDSuperlayer} → 
 * {@link org.jlab.geom.detector.cnd.CNDLayer CNDLayer} → 
 * {@link org.jlab.geom.component.ScintillatorPaddle ScintillatorPaddle}
 * </code>
 * 
 * @author jnhankins
 */
public class CNDDetector extends AbstractDetector<CNDSector> {
    
    protected CNDDetector() {
        super(DetectorId.CND);
    }
    
    /**
     * Returns "CND Detector".
     * @return "CND Detector"
     */
    @Override
    public String getType() {
        return "CND Detector";
    }
}
