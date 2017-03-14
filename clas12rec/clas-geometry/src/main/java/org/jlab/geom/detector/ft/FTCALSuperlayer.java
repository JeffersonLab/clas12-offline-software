package org.jlab.geom.detector.ft;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractSuperlayer;


/**
 * A Forward Tagger Calorimeter (FTCAL) {@link org.jlab.geom.base.Superlayer Superlayer}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.ft.FTCALFactory FTCALFactory}<br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.ft.FTCALDetector FTCALDetector} → 
 * {@link org.jlab.geom.detector.ft.FTCALSector FTCALSector} → 
 * <b>{@link org.jlab.geom.detector.ft.FTCALSuperlayer FTCALSuperlayer}</b> → 
 * {@link org.jlab.geom.detector.ft.FTCALLayer FTCALLayer} → 
 * {@link org.jlab.geom.component.ScintillatorPaddle ScintillatorPaddle}
 * </code>
 * 
 * @author jnhankins
 */
public class FTCALSuperlayer extends AbstractSuperlayer<FTCALLayer> {

    protected FTCALSuperlayer(int sectorId, int superlayerId) {
        super(DetectorId.FTCAL, sectorId, superlayerId);
    }
    
    /**
     * Returns "FTCAL Superlayer".
     * @return "FTCAL Superlayer"
     */
    @Override
    public String getType() {
        return "FTCAL Superlayer";
    }
}
