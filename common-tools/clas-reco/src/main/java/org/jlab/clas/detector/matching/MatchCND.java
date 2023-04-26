package org.jlab.clas.detector.matching;

import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.clas.detector.DetectorTrack.TrajectoryPoint;

/**
 * 
 * @author baltzell
 */
public class MatchCND extends AMatch {

    private final double limit_dz;
    private final double limit_dphi;
    private final double limit_dt;

    /**
     * 
     * @param dz 
     * @param dphi must be in units=degrees!!!!
     * @param dt 
     */
    public MatchCND(double dz, double dphi, double dt) {
        this.limit_dz = dz;
        this.limit_dphi = Math.toRadians(dphi);
        this.limit_dt = dt;
    }
    
    @Override
    public boolean matches(DetectorParticle p, DetectorResponse r) {
        return matches(p,r,false);
    }
    
    public boolean matches(DetectorParticle p, DetectorResponse r, boolean uniqueLayer) {
        return p.getCharge() == 0 ? matchesNeutral(p,r,uniqueLayer) : matchesCharged(p,r,uniqueLayer);
    }

    public boolean matchesNeutral(DetectorParticle p, DetectorResponse r, boolean uniqueLayer) {
        final double dphi = getDeltaPhi(r.getPosition().phi(), p.getTrack().getVector().phi());
        final double dz = p.getDetectorResponses().get(0).getPosition().z() - r.getPosition().z();
        if (Math.abs(dphi) > this.limit_dphi) return false;
        if (Math.abs(dz) > this.limit_dz) return false;
        for (DetectorResponse x : p.getDetectorResponses()) {
            if (uniqueLayer && r.getDescriptor().getLayer() == x.getDescriptor().getLayer()) {
                return false;
            }
            if (r.getDescriptor().getType() == x.getDescriptor().getType()) {
                if (this.limit_dt>0 && Math.abs(x.getTime() - r.getTime()) > this.limit_dt) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean matchesCharged(DetectorParticle p, DetectorResponse r, boolean uniqueLayer) {
        TrajectoryPoint tp = p.getTrack().getTrajectoryPoint(r.getDescriptor());
        if (tp == null) return false;
        final double dz = tp.getCross().origin().vectorTo(r.getPosition().toPoint3D()).z();
        final double dphi = getDeltaPhi(tp.getCross().origin(),r.getPosition().toPoint3D());
        if (Math.abs(dz) > this.limit_dz) return false;
        if (Math.abs(dphi) > this.limit_dphi) return false;
        for (DetectorResponse x : p.getDetectorResponses()) {
            if (uniqueLayer && r.getDescriptor().getLayer() == x.getDescriptor().getLayer()) {
                return false;
            }
            if (r.getDescriptor().getType() == x.getDescriptor().getType()) {
                if (this.limit_dt>0 && Math.abs(x.getTime() - r.getTime()) > this.limit_dt) {
                    return false;
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
