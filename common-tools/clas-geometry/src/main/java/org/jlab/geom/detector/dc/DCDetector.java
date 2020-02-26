package org.jlab.geom.detector.dc;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractDetector;

/**
 * A Drift Chamber (DC) {@link org.jlab.geom.base.Detector Detector}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.dc.DCFactory DCFactory}<br> 
 * Hierarchy: 
 * <code>
 * <b>{@link org.jlab.geom.detector.dc.DCDetector DCDetector}</b> → 
 * {@link org.jlab.geom.detector.dc.DCSector DCSector} → 
 * {@link org.jlab.geom.detector.dc.DCSuperlayer DCSuperlayer} → 
 * {@link org.jlab.geom.detector.dc.DCLayer DCLayer} → 
 * {@link org.jlab.geom.component.DriftChamberWire DriftChamberWire}
 * </code>
 * 
 * @author jnhankins
 */
public class DCDetector extends AbstractDetector<DCSector> {
    
    protected DCDetector() {
        super(DetectorId.DC);
    }
    
    public DCDetector(DetectorId id){
        super(id);
    }
    /**
     * Returns "DC Detector".
     * @return "DC Detector"
     */
    @Override
    public String getType() {
        return "DC Detector";
    }
}
