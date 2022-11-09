package org.jlab.geom.detector.fmt;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractLayer;
import org.jlab.geom.component.TrackerStrip;
import org.jlab.geom.prim.Sector3D;
import org.jlab.geom.prim.Transformation3D;

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
    
    private Sector3D arcSector = new Sector3D();
    
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

    /**
     * Returns the minimum radius
     * @return
     */
    public double getRmin() {
        return arcSector.innerRadius();
    }

    /**
     * Returns the maximum radius
     * @return
     */
    public double getRmax() {
        return arcSector.outerRadius();
    }

    /**
     * Returns the Sector3D describing the layer surface
     * @return
     */
    public Sector3D getTrajectorySurface() {
        return this.arcSector;
    }
    
    @Override
    protected void onSetTransformation(Transformation3D transform) {
        transform.apply(arcSector);
    }
}
