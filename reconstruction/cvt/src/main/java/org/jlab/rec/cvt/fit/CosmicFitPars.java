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
        setRay(ray);
        setChi2(chi2);
    }

    public double[] getChi2() {
        return _chisq;
    }

    public final void setChi2(double[] chisq) {
        this._chisq = chisq;
    }

    public Ray getRay() {
        return _ray;
    }

    public final void setRay(Ray _ray) {
        this._ray = _ray;
    }

}
