package org.jlab.geom.detector.ec;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractSuperlayer;
import org.jlab.geom.prim.Triangle3D;


/**
 * An Electromagnetic Calorimeter (EC) {@link org.jlab.geom.base.Superlayer Superlayer}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.ec.ECFactory ECFactory}<br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.ec.ECDetector ECDetector} → 
 * {@link org.jlab.geom.detector.ec.ECSector ECSector} → 
 * <b>{@link org.jlab.geom.detector.ec.ECSuperlayer ECSuperlayer}</b> → 
 * {@link org.jlab.geom.detector.ec.ECLayer ECLayer} → 
 * {@link org.jlab.geom.component.ScintillatorPaddle ScintillatorPaddle}
 * </code>
 * 
 * @author jnhankins
 */
public class ECSuperlayer extends AbstractSuperlayer<ECLayer> {

    protected ECSuperlayer(int sectorId, int superlayerId) {
        super(DetectorId.EC, sectorId, superlayerId);
    }
    
    /**
     * Returns "EC Superlayer".
     * @return "EC Superlayer"
     */
    @Override
    public String getType() {
        return "EC Superlayer";
    }
    
    /**
     * Returns the superlayer thickness
     * @return thickness
     */
    public double getThickness() {
        double thickness = 0;
        for(ECLayer layer : this.getAllLayers()) {
            thickness += layer.getThickness();
        }
        return thickness;
    }
}
