package org.jlab.geom.detector.dc;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractSuperlayer;


/**
 * A Drift Chamber (DC) {@link org.jlab.geom.base.Superlayer Superlayer}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.dc.DCFactory DCFactory}<br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.dc.DCDetector DCDetector} → 
 * {@link org.jlab.geom.detector.dc.DCSector DCSector} → 
 * <b>{@link org.jlab.geom.detector.dc.DCSuperlayer DCSuperlayer}</b> → 
 * {@link org.jlab.geom.detector.dc.DCLayer DCLayer} → 
 * {@link org.jlab.geom.component.DriftChamberWire DriftChamberWire}
 * </code>
 * 
 * @author jnhankins
 */
public class DCSuperlayer extends AbstractSuperlayer<DCLayer> {

    protected DCSuperlayer(int sectorId, int superlayerId) {
        super(DetectorId.DC, sectorId, superlayerId);
    }
    
    public DCSuperlayer(DetectorId id, int sectorId, int superlayerId) {
        super(id, sectorId, superlayerId);
    }
    /**
     * Returns "DC Superlayer".
     * @return "DC Superlayer"
     */
    @Override
    public String getType() {
        return "DC Superlayer";
    }
}
