package org.jlab.rec.cvt.fit;

import org.jlab.rec.cvt.trajectory.Ray;

/**
 * The fit parameters of a line fit returned by HelicalTrackFitter
 */
public class CosmicFitPars {

    private Ray _ray;    // ray describing the straight track
    private double[] _chisq;  // fit chi-squared

    // The constructor
    public CosmicFitPars(Ray ray, double[] chi2) {
        set_ray(ray);
        set_chisq(chi2);
    }

    public double[] get_chisq() {
        return _chisq;
    }

    public void set_chisq(double[] chisq) {
        this._chisq = chisq;
    }

    public Ray get_ray() {
        return _ray;
    }

    public void set_ray(Ray _ray) {
        this._ray = _ray;
    }

}
