package org.jlab.clas.detector.matching;

import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

/**
 * Matching as specified by CND group.
 * 
 * @author baltzell
 */
public class MatchCND extends AMatch {

    private final Point3D limit;
    private final double dt;

    public MatchCND(Point3D limit, double dt) {
        this.limit = limit;
        this.dt = dt;
    }

    public MatchCND(Vector3D limit, double dt) {
        this.limit = limit.toPoint3D();
        this.dt = dt;
    }

    public MatchCND(double dx, double dy, double dz, double dt) {
        this.limit = new Point3D(dx,dy,dz);
        this.dt =dt;
    }

    @Override
    public boolean matches(DetectorParticle p, DetectorResponse r) {
        Vector3D res = p.getLastCross().distance(r.getPosition().toPoint3D()).toVector();
        if (Math.abs(res.x()) > this.limit.x()) return false;
        if (Math.abs(res.y()) > this.limit.y()) return false;
        if (Math.abs(res.z()) > this.limit.z()) return false;
        for (DetectorResponse x : p.getDetectorResponses()) {
            if (r.getDescriptor().getType() == DetectorType.CND) {
                if (Math.abs(x.getTime() - r.getTime()) > this.dt) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public double quality(DetectorParticle p, DetectorResponse r) {
        return p.getLastCross().distance(r.getPosition().toPoint3D()).length();
    }

}
