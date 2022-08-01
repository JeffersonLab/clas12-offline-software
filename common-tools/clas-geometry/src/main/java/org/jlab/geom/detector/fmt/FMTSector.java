package org.jlab.geom.detector.fmt;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractSector;


/**
 * A Forward Micromegas Tracker  (FMT) {@link org.jlab.geom.base.Sector Sector}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.fmt.FMTFactory FMTFactory}<br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.fmt.FMTDetector FMTDetector} → 
 * <b>{@link org.jlab.geom.detector.fmt.FMTSector FMTSector}</b> → 
 * {@link org.jlab.geom.detector.fmt.FMTSuperlayer FMTSuperlayer} → 
 * {@link org.jlab.geom.detector.fmt.FMTLayer FMTLayer} → 
 * {@link org.jlab.geom.component.TrackerStrip TrackerStrip}
 * </code>
 * 
 * @author devita
 */
public class FMTSector extends AbstractSector<FMTSuperlayer> {
    
    protected FMTSector(int sectorId) {
        super(DetectorId.FMT, sectorId);
    }
    
    /**
     * Returns "FMT Sector".
     * @return "FMT Sector"
     */
    @Override
    public String getType() {
        return "FMT Sector";
    }
}
