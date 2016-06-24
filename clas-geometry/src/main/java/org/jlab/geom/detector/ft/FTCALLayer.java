package org.jlab.geom.detector.ft;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractLayer;
import org.jlab.geom.component.ScintillatorPaddle;

/**
 * A Forward Tagger Calorimeter (FTCAL) {@link org.jlab.geom.base.Layer Layer}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.ft.FTCALFactory FTCALFactory}<br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.ft.FTCALDetector FTCALDetector} → 
 * {@link org.jlab.geom.detector.ft.FTCALSector FTCALSector} → 
 * {@link org.jlab.geom.detector.ft.FTCALSuperlayer FTCALSuperlayer} → 
 * <b>{@link org.jlab.geom.detector.ft.FTCALLayer FTCALLayer}</b> → 
 * {@link org.jlab.geom.component.ScintillatorPaddle ScintillatorPaddle}
 * </code>
 * 
 * @author jnhankins
 */
public class FTCALLayer extends AbstractLayer<ScintillatorPaddle> {
    
    protected FTCALLayer(int sectorId, int superlayerId, int layerId) {
        super(DetectorId.FTCAL, sectorId, superlayerId, layerId, true);
    }
    
    /**
     * Returns "FTCAL Layer".
     * @return "FTCAL Layer"
     */
    @Override
    public String getType() {
        return "FTCAL Layer";
    }
}
