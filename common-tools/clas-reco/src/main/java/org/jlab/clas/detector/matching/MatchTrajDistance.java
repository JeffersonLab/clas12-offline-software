package org.jlab.clas.detector.matching;

import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Point3D;

/**
 *
 * @author baltzell
 */
public class MatchTrajDistance extends AMatch {

    private final double distanceLimit;

    public MatchTrajDistance(double distanceLimit) {
        this.distanceLimit = distanceLimit;
    }

    @Override
    public boolean matches(DetectorParticle p, DetectorResponse r) {
        return this.quality(p,r) < this.distanceLimit;
    }

    @Override
    public double quality(DetectorParticle p, DetectorResponse r) {
        if (p.getTrack().getDetectorID() == DetectorType.DC.getDetectorId()) {
            if (p.getTrack().getSector() != r.getSector()) {
                return Double.POSITIVE_INFINITY;
            }
        }
        if (p.getTrackTrajectory().contains(r.getDescriptor())) { 
            Point3D traj = p.getTrackTrajectory().get(r.getDescriptor()).getCross().origin();
            return traj.distance(r.getPosition().toPoint3D());
        }
        return Double.POSITIVE_INFINITY;
    }
 
}
