package org.jlab.geom.detector.ftof;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractLayer;
import org.jlab.geom.component.ScintillatorPaddle;

/**
 * A Forward Time of Flight (FTOF) {@link org.jlab.geom.base.Layer Layer}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.ftof.FTOFFactory FTOFFactory}<br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.ftof.FTOFDetector FTOFDetector} → 
 * {@link org.jlab.geom.detector.ftof.FTOFSector FTOFSector} → 
 * {@link org.jlab.geom.detector.ftof.FTOFSuperlayer FTOFSuperlayer} → 
 * <b>{@link org.jlab.geom.detector.ftof.FTOFLayer FTOFLayer}</b> → 
 * {@link org.jlab.geom.component.ScintillatorPaddle ScintillatorPaddle}
 * </code>
 * 
 * @author jnhankins
 */
public class FTOFLayer extends AbstractLayer<ScintillatorPaddle> {
    
    protected FTOFLayer(int sectorId, int superlayerId, int layerId) {
        super(DetectorId.FTOF, sectorId, superlayerId, layerId, false);
    }
    
    /**
     * Returns "FTOF Layer".
     * @return "FTOF Layer"
     */
    @Override
    public String getType() {
        return "FTOF Layer";
    }
}
