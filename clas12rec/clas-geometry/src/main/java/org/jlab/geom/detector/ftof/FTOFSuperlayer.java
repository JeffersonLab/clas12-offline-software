package org.jlab.geom.detector.ftof;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractSuperlayer;

/**
 * A Forward Time of Flight (FTOF) {@link org.jlab.geom.base.Superlayer Superlayer}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.ftof.FTOFFactory FTOFFactory}<br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.ftof.FTOFDetector FTOFDetector} → 
 * {@link org.jlab.geom.detector.ftof.FTOFSector FTOFSector} → 
 * <b>{@link org.jlab.geom.detector.ftof.FTOFSuperlayer FTOFSuperlayer}</b> → 
 * {@link org.jlab.geom.detector.ftof.FTOFLayer FTOFLayer} → 
 * {@link org.jlab.geom.component.ScintillatorPaddle ScintillatorPaddle}
 * </code>
 * 
 * @author jnhankins
 */
public class FTOFSuperlayer extends AbstractSuperlayer<FTOFLayer> {

    protected FTOFSuperlayer(int sectorId, int superlayerId) {
        super(DetectorId.FTOF, sectorId, superlayerId);
    }
    
    /**
     * Returns "FTOF Superlayer".
     * @return "FTOF Superlayer"
     */
    @Override
    public String getType() {
        return "FTOF Superlayer";
    }
}
