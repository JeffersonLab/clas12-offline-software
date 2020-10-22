package org.jlab.rec.cvt.fit;

import java.util.ArrayList;
import java.util.List;

import org.jlab.rec.cvt.svt.Constants;
import org.jlab.rec.cvt.trajectory.Helix;

import Jama.Matrix;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;

/**
 * A fitter which does sequential fit (for r, phi coordinates) to a circle using
 * CircleFitter and then (for r, z coordinates) to a line using LineFitter. Uses
 * CircleCalculator for corroboration of the results obtained from
 * CircleCalculator
 */
public class StraightTrackFitter {

    private Helix _helix;  // fit helix
    private double[] _chisq = new double[2];  // fit chi-squared [0]: circle [1] line

    private LineFitter _xyfit = new LineFitter();
    private LineFitter _linefit = new LineFitter();
    private LineFitPars _linefitpars;
    private HelicalTrackFitPars _helicalfitoutput;
    private double Xb;
    private double Yb;
    /**
     * Status of the HelicalTrackFit
     */
    public enum FitStatus {

        /**
         * Successful Fit.
         */
        Successful,
        /**
         * CircleFit failed.
         */
        CircleFitFailed,
        /**
         * rho-z line fit failed.
         */
        LineFitFailed,
        /**
         * cross-check using simple 3-point circle calculator
         */
        AllCircleCalcsFailed,
        CircleCalcInconsistentWithFit
    };

    
    public StraightTrackFitter() {
    }

    private List<Double> W = new ArrayList<Double>();

    public FitStatus fit(List<Double> X, List<Double> Y, List<Double> Z, List<Double> Rho, List<Double> errRt, List<Double> errRho, List<Double> ErrZ) {
        //  Initialize the various fitter outputs
        
        _linefitpars = null;
        _helicalfitoutput = null;
        _chisq[0] = 0;
        _chisq[1] = 0;
        W.clear();
        ((ArrayList<Double>) W).ensureCapacity(X.size());

        for (int j = 0; j < X.size(); j++) {
            W.add(j, errRt.get(j) ); 

        }
       
        // fit the points 
        // check the status
        _xyfit = new LineFitter();
        boolean xyfitstatusOK = _xyfit.fitStatus(X, Y, W, new ArrayList<Double>(X.size()), X.size());
        double slope = _xyfit.getFit().slope();
        double intercept = _xyfit.getFit().intercept();
        Line3D line = new Line3D(new Point3D(X.get(0),slope*X.get(0)+intercept,0), new Point3D(X.get(1),slope*X.get(1)+intercept,0));
        double fit_dca = 0;
        Point3D xydoca = line.distance(new Point3D(this.getXb(), this.getYb(),0)).origin();
        
        double fit_phi_at_dca = line.direction().phi();
        
        if(Math.abs(Math.atan2(Y.get(0),X.get(0))-fit_phi_at_dca)>Math.PI/2) {
            fit_phi_at_dca+=Math.PI;
        }
        double x = xydoca.x();
        double y = xydoca.y();
        fit_dca = Math.atan2(-x,y);
        if(Math.cos(fit_phi_at_dca)>0.1) {
            fit_dca = y/Math.cos(fit_phi_at_dca);
        } else {
            fit_dca = -x/Math.sin(fit_phi_at_dca);
        }
        
        //Line fit
        _linefit = new LineFitter();

        boolean linefitstatusOK = _linefit.fitStatus(Rho, Z, errRho, ErrZ, Z.size());

        if (!linefitstatusOK) {
            return FitStatus.LineFitFailed; 
        }
        //  Get the results of the fits
        _linefitpars = _linefit.getFit();

        // get the parameters of the helix representation of the track
        double fit_curvature = 1.e-12;
        double fit_tandip = _linefitpars.slope();

        //double fit_Z0 = _linefitpars.intercept();
        double fit_Z0 = _linefitpars.intercept();
        //fit_Z0 = (Math.abs(fit_dca)-_linefitpars.intercept())/ _linefitpars.slope() ; //reset for displaced vertex
        //require vertex position inside of the inner barrel
            if (Math.abs(fit_dca) > Constants.MODULERADIUS[0][0] || Math.abs(fit_Z0) > 100) {
            return null;
        }

        // get the error matrix
        Matrix fit_covmatrix = new Matrix(5, 5);
        //error matrix (assuming that the circle fit and line fit parameters are uncorrelated)
        // | d_dca*d_dca                   d_dca*d_phi_at_dca            d_dca*d_curvature        0            0             |
        // | d_phi_at_dca*d_dca     d_phi_at_dca*d_phi_at_dca     d_phi_at_dca*d_curvature        0            0             |
        // | d_curvature*d_dca	    d_curvature*d_phi_at_dca      d_curvature*d_curvature         0            0             |
        // | 0                              0                             0                    d_Z0*d_Z0                     |
        // | 0                              0                             0                       0        d_tandip*d_tandip |
        // 

        // the circle covariance matrix
        //covr[0] =  delta_rho.delta_rho;
        //covr[1] =  delta_rho.delta_phi;
        //covr[2] =  delta_rho.delta_dca;
        //covr[3] =  delta_phi.delta_phi;
        //covr[4] =  delta_phi.delta_dca;
        //covr[5] =  delta_dca.delta_dca;
        
        fit_covmatrix.set(0, 0, _xyfit.getFit().interceptErr() * _xyfit.getFit().interceptErr());
        fit_covmatrix.set(1, 1, _xyfit.getFit().slopeErr() * _xyfit.getFit().slopeErr());
        fit_covmatrix.set(2, 2, 1.e-08);
        fit_covmatrix.set(3, 3, _linefitpars.interceptErr() * _linefitpars.interceptErr());
        fit_covmatrix.set(4, 4, _linefitpars.slopeErr() * _linefitpars.slopeErr());

        Helix helixresult = new Helix(fit_dca, fit_phi_at_dca, fit_curvature, fit_Z0, fit_tandip, fit_covmatrix);

        set_helix(helixresult);
        _chisq[0] = _linefitpars.chisq();
        _chisq[1] = _linefitpars.chisq();

        //System.out.println("chi2 "+_chisq[0]+" " + _chisq[1]);
        //  Create the HelicalTrackFit for this helix
        _helicalfitoutput = new HelicalTrackFitPars(helixresult, _chisq);

        return FitStatus.Successful;
    }

    /**
     * Return the results of the most recent helix fit. Returns null if the fit
     * was not successful.
     *
     * @return HelicalTrackFitPars from the most recent helix fit
     */
    public HelicalTrackFitPars getFit() {
        return _helicalfitoutput;
    }

    
    

    /**
     * Return the s-z line fit for the most recent helix fit. If the line fit
     * failed or was not performed due to not having enough 3D hits, null is
     * returned.
     *
     * @return line fit for most recent helix fit
     */
    public LineFitPars getLineFit() {
        return _linefitpars;
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

    public void set_covmat(double[] chisq) {
        this._chisq = chisq;
    }

    /**
     * @return the Xb
     */
    public double getXb() {
        return Xb;
    }

    /**
     * @param Xb the Xb to set
     */
    public void setXb(double Xb) {
        this.Xb = Xb;
    }

    /**
     * @return the Yb
     */
    public double getYb() {
        return Yb;
    }

    /**
     * @param Yb the Yb to set
     */
    public void setYb(double Yb) {
        this.Yb = Yb;
    }

}
