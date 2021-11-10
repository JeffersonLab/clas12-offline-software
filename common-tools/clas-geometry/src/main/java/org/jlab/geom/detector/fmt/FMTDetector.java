package org.jlab.geom.detector.fmt;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractDetector;


/**
 * A Forward Micromegas Tracker  (FMT) {@link org.jlab.geom.base.Detector Detector}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.fmt.FMTFactory FMTFactory}<br> 
 * Hierarchy: 
 * <code>
 * <b>{@link org.jlab.geom.detector.fmt.FMTDetector FMTDetector}</b> → 
 * {@link org.jlab.geom.detector.fmt.FMTSector FMTSector} → 
 * {@link org.jlab.geom.detector.fmt.FMTSuperlayer FMTSuperlayer} → 
 * {@link org.jlab.geom.detector.fmt.FMTLayer FMTLayer} → 
 * {@link org.jlab.geom.component.TrackerStrip TrackerStrip}
 * </code>
 * 
 * @author devita
 */
public class FMTDetector extends AbstractDetector<FMTSector> {
    
    protected FMTDetector() {
        super(DetectorId.FMT);
    }
    
    /**
     * Returns "FMT Detector".
     * @return "FMT Detector"
     */
    @Override
    public String getType() {
        return "FMT Detector";
    }
}
