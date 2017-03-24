/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.hits;

import static org.jlab.detector.hits.DetId.FTOFID;

/**
 *
 * @author kenjo
 */
public final class FTOFDetHit extends DetHit {

    public FTOFDetHit(DetHit hit) {
        super(hit.origin(), hit.end(), hit.detId);
        detectorComponent = hit.detectorComponent;

        if (hit.detId.length != 4 || hit.getId()[0] != FTOFID) {
            throw new IllegalArgumentException("Hit is not FTOF Detector Hit!");
        }
    }

    public int getSector() {
        return detId[1];
    }

    public int getLayer() {
        return detId[2];
    }

    public int getPaddle() {
        return detId[3];
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(String.format("FTOF detector,"));
        str.append(String.format("sector#: %d, ", detId[1]));
        str.append(String.format("layer#: %d ", detId[2]));
        str.append(String.format("paddle#: %d", detId[3]));

        return str.toString();
    }
}
