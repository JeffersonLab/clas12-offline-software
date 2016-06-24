package org.jlab.geom.detector.ec;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractSector;

/**
 * An Electromagnetic Calorimeter (EC) {@link org.jlab.geom.base.Sector Sector}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.ec.ECFactory ECFactory}<br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.ec.ECDetector ECDetector} → 
 * <b>{@link org.jlab.geom.detector.ec.ECSector ECSector}</b> → 
 * {@link org.jlab.geom.detector.ec.ECSuperlayer ECSuperlayer} → 
 * {@link org.jlab.geom.detector.ec.ECLayer ECLayer} → 
 * {@link org.jlab.geom.component.ScintillatorPaddle ScintillatorPaddle}
 * </code>
 * 
 * @author jnhankins
 */
public class ECSector extends AbstractSector<ECSuperlayer> {
    
    protected ECSector(int sectorId) {
        super(DetectorId.EC, sectorId);
    }
    
    /**
     * Returns "EC Sector".
     * @return "EC Sector"
     */
    @Override
    public String getType() {
        return "EC Sector";
    }
}
