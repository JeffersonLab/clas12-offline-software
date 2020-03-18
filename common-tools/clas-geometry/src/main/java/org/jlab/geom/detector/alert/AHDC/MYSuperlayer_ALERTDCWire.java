/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.detector.alert.AHDC;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractSuperlayer;
/**
 *
 * @author sergeyeva
 */
public class MYSuperlayer_ALERTDCWire extends AbstractSuperlayer<MYLayer_ALERTDCWire> {
    protected MYSuperlayer_ALERTDCWire(int sectorId, int superlayerId) {
        super(DetectorId.DC, sectorId, superlayerId);
    }
    
    /**
     * Returns "ALERT DC Superlayer".
     * @return "ALERT DC Superlayer"
     */
    @Override
    public String getType() {
        return "ALERT DC Superlayer";
    }  
}
