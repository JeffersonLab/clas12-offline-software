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
        set_helix(helix);
        set_chisq(chi2);
    }

    public Helix get_helix() {
        return _helix;
    }

    public void set_helix(Helix helix) {
        this._helix = helix;
    }

    public double[] get_chisq() {
        return _chisq;
    }

    public void set_chisq(double[] chisq) {
        this._chisq = chisq;
    }

}
