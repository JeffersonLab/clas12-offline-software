package org.jlab.geom.detector.fmt;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractLayer;
import org.jlab.geom.component.TrackerStrip;

/**
 * A Forward Micromegas Tracker  (FMT) {@link org.jlab.geom.base.Layer Layer}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.fmt.FMTFactory FMTFactory}<br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.fmt.FMTDetector FMTDetector} → 
 * {@link org.jlab.geom.detector.fmt.FMTSector FMTSector} → 
 * {@link org.jlab.geom.detector.fmt.FMTSuperlayer FMTSuperlayer} → 
 * <b>{@link org.jlab.geom.detector.fmt.FMTLayer FMTLayer}</b> → 
 * {@link org.jlab.geom.component.TrackerStrip TrackerStrip}
 * </code>
 * 
 * @author devita
 */
public class FMTLayer extends AbstractLayer<TrackerStrip> {
    
    protected FMTLayer(int sectorId, int superlayerId, int layerId) {
        super(DetectorId.FMT, sectorId, superlayerId, layerId, false);
    }

    /**
     * Returns "FMT Layer".
     * @return "FMT Layer"
     */
    @Override
    public String getType() {
        return "FMT Layer";
    }

}
