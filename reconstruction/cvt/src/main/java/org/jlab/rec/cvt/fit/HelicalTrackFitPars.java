package org.jlab.rec.cvt.fit;

import org.jlab.rec.cvt.trajectory.Helix;

/**
 * The fit parameters of a line fit returned by HelicalTrackFitter
 */
public class HelicalTrackFitPars {

    private Helix _helix;    // fit helix
    private double[] _chisq;  // fit chi-squared

    // The constructor
    public HelicalTrackFitPars(Helix helix, double[] chi2) {
        setHelix(helix);
        setChi2(chi2);
    }

    public Helix getHelix() {
        return _helix;
    }

    public final void setHelix(Helix helix) {
        this._helix = helix;
    }

    public double[] getChi2() {
        return _chisq;
    }

    public final void setChi2(double[] chisq) {
        this._chisq = chisq;
    }

}
