package org.jlab.geom.detector.cnd;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractLayer;
import org.jlab.geom.component.ScintillatorPaddle;

/**
 * A Central Neutron Detector (CND) {@link org.jlab.geom.base.Layer Layer}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.cnd.CNDFactory CNDFactory}<br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.cnd.CNDDetector CNDDetector} → 
 * {@link org.jlab.geom.detector.cnd.CNDSector CNDSector} → 
 * {@link org.jlab.geom.detector.cnd.CNDSuperlayer CNDSuperlayer} → 
 * <b>{@link org.jlab.geom.detector.cnd.CNDLayer CNDLayer}</b> → 
 * {@link org.jlab.geom.component.ScintillatorPaddle ScintillatorPaddle}
 * </code>
 * 
 * @author jnhankins
 */
public class CNDLayer extends AbstractLayer<ScintillatorPaddle> {
    
    protected CNDLayer(int sectorId, int superlayerId, int layerId) {
        super(DetectorId.CND, sectorId, superlayerId, layerId, false);
    }

    /**
     * Returns "CND Layer".
     * @return "CND Layer"
     */
    @Override
    public String getType() {
        return "CND Layer";
    }
}
