package org.jlab.clas.detector.matching;

import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.clas.detector.DetectorTrack.TrajectoryPoint;

/**
 * 
 * @author baltzell
 */
public class MatchCylindrical extends AMatch {

    private final double limit_dz;
    private final double limit_dphi;
    private final double limit_dt;

    /**
     * 
     * @param dz 
     * @param dphi must be in units=degrees!!!!
     * @param dt 
     */
    public MatchCylindrical(double dz, double dphi, double dt) {
        this.limit_dz = dz;
        this.limit_dphi = Math.toRadians(dphi);
        this.limit_dt = dt;
    }

    @Override
    public boolean matches(DetectorParticle p, DetectorResponse r) {
        TrajectoryPoint tp = p.getTrack().getTrajectoryPoint(r.getDescriptor());
        if (tp == null) return false;
        final double dz = tp.getCross().origin().vectorTo(r.getPosition().toPoint3D()).z();
        final double dphi = getDeltaPhi(tp.getCross().origin(),r.getPosition().toPoint3D());
        if (Math.abs(dz) > this.limit_dz) return false;
        if (Math.abs(dphi) > this.limit_dphi) return false;
        if (this.limit_dt>0) {
            for (DetectorResponse x : p.getDetectorResponses()) {
                if (r.getDescriptor().getType() == x.getDescriptor().getType()) {
                    if (Math.abs(x.getTime() - r.getTime()) > this.limit_dt) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public double quality(DetectorParticle p, DetectorResponse r) {
        if (!this.matches(p,r)) return Double.POSITIVE_INFINITY;
        TrajectoryPoint tp = p.getTrack().getTrajectoryPoint(r.getDescriptor());
        double dz = tp.getCross().origin().vectorTo(r.getPosition().toPoint3D()).z();
        double dphi = getDeltaPhi(tp.getCross().origin(),r.getPosition().toPoint3D());
        return Math.pow(dz,2)+Math.pow(dphi,2);
    }

}
