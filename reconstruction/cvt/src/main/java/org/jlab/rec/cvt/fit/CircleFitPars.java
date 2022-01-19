package org.jlab.rec.cvt.fit;

/**
 * Returns the parameters of a fit with a circle to hits in the the CLAS12 where
 * the solenoid field bends the particle path into a helix. These hits are
 * represented as 3-dimensional points, that map to to 2-D points in the XY
 * plane in the detector's Global coordinate system.
 *
 */
public class CircleFitPars {

    private double _xref;    	// x coord. of the reference point
    private double _yref;    	// y coord. of the reference point
    private double _rho;     	// curvature
    private double _phi;	  	// azymuth angle at the reference point
    private double _dca; 		// distance of closest approach to the reference point
    private double _chisq;   	// fit chi-squared

    private double[] _covcircle; // covariance matrix of the circle fit

    // The constructor
    public CircleFitPars(double xr, double yr, double rho, double phir, double docar, double chi2, double[] covmat) {
        _xref = xr;
        _yref = yr;
        _rho = rho;
        _phi = phir;
        _dca = docar;
        _chisq = chi2;
        _covcircle = covmat;
    }
    // The methods

    /**
     * Returns the x coordinate of the reference point
     * @return 
     */
    public double xref() {
        return _xref;
    }

    /**
     * Returns the y coordinate of the reference point
     * @return 
     */
    public double yref() {
        return _yref;
    }

    /**
     * Returns the radius of curvature
     * @return 
     */
    public double rho() {
        return _rho;
    }

    /**
     * Returns the phi at the reference point
     * @return 
     */
    public double phi() {
        return _phi;
    }

    /**
     * Returns the doca to the reference point
     * @return 
     */
    public double doca() {
        return _dca;
    }

    /**
     * Returns the chi^2 of the fit
     * @return 
     */
    public double chisq() {
        return _chisq;
    }

    /**
     * Returns the covariance matrix of the fit
     * @return 
     */
    public double[] cov() {
        double[] tmpmat = new double[6];
        System.arraycopy(_covcircle, 0, tmpmat, 0, 6);
        return tmpmat;
    }

}
