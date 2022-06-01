package org.jlab.clas.detector.matching;

import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

/**
 * Matching based on point of closest approach, 3-d distance between the
 * last cross and the detector hit position.
 * 
 * @author baltzell
 */
public class MatchPOCA extends AMatch {

    private final Point3D limit;

    public MatchPOCA(Point3D limit) {
        this.limit = limit;
    }

    @Override
    public boolean matches(DetectorParticle p, DetectorResponse r) {
        Vector3D res = p.getLastCross().distance(r.getPosition().toPoint3D()).toVector();
        return Math.abs(res.x()) < this.limit.x() &&
               Math.abs(res.y()) < this.limit.y() &&
               Math.abs(res.z()) < this.limit.z();
    }

    @Override
    public double quality(DetectorParticle p, DetectorResponse r) {
        return p.getLastCross().distance(r.getPosition().toPoint3D()).length();
    }

}
