package org.jlab.rec.cvt.track;

import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.svt.Geometry;
import org.jlab.rec.cvt.trajectory.Ray;
import org.jlab.rec.cvt.trajectory.Trajectory;

public class StraightTrack extends Trajectory {

    public StraightTrack(Ray ray) {
        super(ray);

    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private double _ndf;
    private double _chi2;

    /**
     * updates the crosses in the track position based on the track straight
     * line fit parameters
     *
     * @param fit_yxslope
     * @param fit_yzslope
     * @param geo
     */
    public void update_Crosses(double fit_yxslope, double fit_yzslope, Geometry geo) {
        for (Cross c : this) {
            if (c.get_Detector().equalsIgnoreCase("SVT")) //only update for the svt
            {
                update_Cross(c, fit_yxslope, fit_yzslope, geo);
            }

        }
    }

    /**
     * updates the cross position based on the track straight line fit
     * parameters
     *
     * @param cross
     * @param fit_yxslope
     * @param fit_yzslope
     * @param geo
     */
    public void update_Cross(Cross cross, double fit_yxslope, double fit_yzslope, Geometry geo) {

        double x = fit_yxslope / Math.sqrt(fit_yxslope * fit_yxslope + fit_yzslope * fit_yzslope + 1);
        double y = 1. / Math.sqrt(fit_yxslope * fit_yxslope + fit_yzslope * fit_yzslope + 1);
        double z = fit_yzslope / Math.sqrt(fit_yxslope * fit_yxslope + fit_yzslope * fit_yzslope + 1);

        Vector3D trkDir = new Vector3D(x, y, z);

        if (trkDir != null) {
            cross.set_CrossParamsSVT(trkDir, geo);
        }

    }

    public double get_ndf() {
        return _ndf;
    }

    public void set_ndf(double _ndf) {
        this._ndf = _ndf;
    }

    public double get_chi2() {
        return _chi2;
    }

    public void set_chi2(double _chi2) {
        this._chi2 = _chi2;
    }

    public boolean containsCross(Cross cross) {
        StraightTrack cand = this;
        boolean isInTrack = false;

        for (int i = 0; i < cand.size(); i++) {
            if (cand.get(i).get_Id() == cross.get_Id()) {
                isInTrack = true;
            }

        }

        return isInTrack;
    }

    /**
     *
     * @return the chi^2 for the straight track fit
     */
    public double calc_straightTrkChi2() {

        double chi2 = 0;

        double yxSl = this.get_ray().get_yxslope();
        double yzSl = this.get_ray().get_yzslope();
        double yxIt = this.get_ray().get_yxinterc();
        double yzIt = this.get_ray().get_yzinterc();

        for (Cross c : this) {
            double errSq = c.get_PointErr().x() * c.get_PointErr().x() + c.get_PointErr().z() * c.get_PointErr().z();
            double y = c.get_Point().y();
            double x = c.get_Point().x();
            double z = c.get_Point().z();

            double x_fit = yxSl * y + yxIt;
            double z_fit = yzSl * y + yzIt;

            double delta = (x - x_fit) * (x - x_fit) + (z - z_fit) * (z - z_fit);

            chi2 += delta / errSq;
        }
        return chi2;
    }

}
