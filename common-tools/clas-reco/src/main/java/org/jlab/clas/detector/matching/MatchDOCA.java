package org.jlab.clas.detector.matching;

import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;

/**
 * Matching based only on distance of closest approach between the last cross
 * and the detector hit position.
 * 
 * @author baltzell
 */
public class MatchDOCA extends AMatch {

    private final double docaLimit;

    public MatchDOCA(double docaLimit) {
        this.docaLimit = docaLimit;
    }

    @Override
    public boolean matches(DetectorParticle p, DetectorResponse r) {
        return this.quality(p,r) < this.docaLimit;
    }

    @Override
    public double quality(DetectorParticle p, DetectorResponse r) {
        return p.getLastCross().distance(r.getPosition().toPoint3D()).length();
    }

}
