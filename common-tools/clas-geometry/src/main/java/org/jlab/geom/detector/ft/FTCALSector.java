package org.jlab.geom.detector.ft;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractSector;

/**
 * A Forward Tagger Calorimeter (FTCAL) {@link org.jlab.geom.base.Sector Sector}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.ft.FTCALFactory FTCALFactory}<br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.ft.FTCALDetector FTCALDetector} → 
 * <b>{@link org.jlab.geom.detector.ft.FTCALSector FTCALSector}</b> → 
 * {@link org.jlab.geom.detector.ft.FTCALSuperlayer FTCALSuperlayer} → 
 * {@link org.jlab.geom.detector.ft.FTCALLayer FTCALLayer} → 
 * {@link org.jlab.geom.component.ScintillatorPaddle ScintillatorPaddle}
 * </code>
 * 
 * @author jnhankins
 */
public class FTCALSector extends AbstractSector<FTCALSuperlayer> {
    
    protected FTCALSector(int sectorId) {
        super(DetectorId.FTCAL, sectorId);
    }
    
    /**
     * Returns "FTCAL Sector".
     * @return "FTCAL Sector"
     */
    @Override
    public String getType() {
        return "FTCAL Sector";
    }
}
