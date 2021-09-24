package org.jlab.geom.detector.fmt;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractSuperlayer;


/**
 * A Forward Micromegas Tracker  (FMT) {@link org.jlab.geom.base.Superlayer Superlayer}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.cnd.FMTFactory FMTFactory}<br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.cnd.FMTDetector FMTDetector} → 
 * {@link org.jlab.geom.detector.cnd.FMTSector FMTSector} → 
 * <b>{@link org.jlab.geom.detector.cnd.FMTSuperlayer FMTSuperlayer}</b> → 
 * {@link org.jlab.geom.detector.cnd.FMTLayer FMTLayer} → 
 * {@link org.jlab.geom.component.TrackerStrip TrackerStrip}
 * </code>
 * 
 * @author devita
 */
public class FMTSuperlayer extends AbstractSuperlayer<FMTLayer> {

    protected FMTSuperlayer(int sectorId, int superlayerId) {
        super(DetectorId.FMT, sectorId, superlayerId);
    }
    
    /**
     * Returns "FMT Superlayer".
     * @return "FMT Superlayer"
     */
    @Override
    public String getType() {
        return "FMT Superlayer";
    }
}
