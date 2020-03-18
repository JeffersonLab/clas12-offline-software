/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.detector.alert.AHDC;
import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractDetector;
/**
 *
 * @author sergeyeva
 */
public class MYDetector_ALERTDCWire extends AbstractDetector<MYSector_ALERTDCWire> {
    protected MYDetector_ALERTDCWire() {
        super(DetectorId.DC);
    }
    
    /**
     * Returns "ALERT DC Detector".
     * @return "ALERT DC Detector"
     */
    @Override
    public String getType() {
        return "ALERT DC Detector";
    }
    
}
