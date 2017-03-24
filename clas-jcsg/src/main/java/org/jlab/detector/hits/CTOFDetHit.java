/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.hits;

import static org.jlab.detector.hits.DetId.CTOFID;

/**
 *
 * @author kenjo
 */
public final class CTOFDetHit extends DetHit {

    public CTOFDetHit(DetHit hit) {
        super(hit.origin(), hit.end(), hit.detId);
        detectorComponent = hit.detectorComponent;

        if (hit.detId.length != 2 || hit.getId()[0] != CTOFID) {
            throw new IllegalArgumentException("Hit is not CTOF Detector Hit!");
        }
    }

    public int getPaddle() {
        return detId[1];
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(String.format("CTOF detector,"));
        str.append(String.format("paddle#: %d", detId[1]));

        return str.toString();
    }
}
