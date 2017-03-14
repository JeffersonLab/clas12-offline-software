package org.jlab.geom.detector.ec;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractLayer;
import org.jlab.geom.component.ScintillatorPaddle;

/**
 * An Electromagnetic Calorimeter (EC) {@link org.jlab.geom.base.Layer Layer}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.ec.ECFactory ECFactory}<br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.ec.ECDetector ECDetector} → 
 * {@link org.jlab.geom.detector.ec.ECSector ECSector} → 
 * {@link org.jlab.geom.detector.ec.ECSuperlayer ECSuperlayer} → 
 * <b>{@link org.jlab.geom.detector.ec.ECLayer ECLayer}</b> → 
 * {@link org.jlab.geom.component.ScintillatorPaddle ScintillatorPaddle}
 * </code>
 * 
 * @author jnhankins
 */
public class ECLayer extends AbstractLayer<ScintillatorPaddle> {
    
    protected ECLayer(int sectorId, int superlayerId, int layerId) {
        super(DetectorId.EC, sectorId, superlayerId, layerId, true);
    }
    
    /**
     * Returns "EC Layer".
     * @return "EC Layer"
     */
    @Override
    public String getType() {
        return "EC Layer";
    }
}
