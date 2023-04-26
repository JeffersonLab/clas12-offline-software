package org.jlab.clas.detector.matching;

import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.clas.detector.DetectorTrack.TrajectoryPoint;

/**
 * 
 * @author baltzell
 */
public class MatchThetaPhi extends AMatch {

    private final double limit_dtheta;
    private final double limit_dphi;
    private final double limit_dt;

    /**
     * @param dtheta must be in units=degrees!!!! 
     * @param dphi must be in units=degrees!!!!
     * @param dt 
     */
    public MatchThetaPhi(double dtheta, double dphi, double dt) {
        this.limit_dtheta = Math.toRadians(dtheta);
        this.limit_dphi = Math.toRadians(dphi);
        this.limit_dt = dt;
    }

    /**
     * @param dtheta must be in units=degrees!!!! 
     * @param dphi must be in units=degrees!!!!
     */
    public MatchThetaPhi(double dtheta, double dphi) {
        this.limit_dtheta = Math.toRadians(dtheta);
        this.limit_dphi = Math.toRadians(dphi);
        this.limit_dt = -1;
    }

    @Override
    public boolean matches(DetectorParticle p, DetectorResponse r) {
        TrajectoryPoint tp = p.getTrack().getTrajectoryPoint(r.getDescriptor());
        if (tp == null) return false;
        final double theta1 = tp.getCross().origin().toVector3D().theta();
        final double theta2 = r.getPosition().theta();
        final double dtheta = theta1-theta2;
        final double dphi = getDeltaPhi(tp.getCross().origin(),r.getPosition().toPoint3D());
        if (Math.abs(dtheta) > this.limit_dtheta) return false;
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
