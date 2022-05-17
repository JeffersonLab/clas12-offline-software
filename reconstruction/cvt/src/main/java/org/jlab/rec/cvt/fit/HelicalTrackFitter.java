package org.jlab.rec.cvt.fit;

import java.util.ArrayList;
import java.util.List;
import org.jlab.rec.cvt.Constants;

import org.jlab.rec.cvt.trajectory.Helix;
import org.jlab.rec.cvt.svt.SVTGeometry;

/**
 * A fitter which does sequential fit (for r, phi coordinates) to a circle using
 * CircleFitter and then (for r, z coordinates) to a line using LineFitter. Uses
 * CircleCalculator for corroboration of the results obtained from
 * CircleCalculator
 */
public class HelicalTrackFitter {

    private Helix _helix;  // fit helix
    private double[] _chisq = new double[2];  // fit chi-squared [0]: circle [1] line

    private CircleCalculator _circlecalc;
    private CircleFitter _circlefit;
    private LineFitter _linefit = new LineFitter();
    private CircleCalcPars _circlecalcpars;
    private CircleFitPars _circlefitpars;
    private LineFitPars _linefitpars;
    private HelicalTrackFitPars _helicalfitoutput;

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

    /**
     * Creates a new instance of HelicalTrackFitter.
     */
    public HelicalTrackFitter() {
    }

    private final List<Double> W = new ArrayList<>();
    private final List<Double> P0 = new ArrayList<>(2);
    private final List<Double> P1 = new ArrayList<>(2);
    private final List<Double> P2 = new ArrayList<>(2);

    public FitStatus fit(List<Double> X, List<Double> Y, List<Double> Z, List<Double> Rho, List<Double> errRt, List<Double> errRho, List<Double> ErrZ, double xb, double yb) {
        //  Initialize the various fitter outputs
        _circlefitpars = null;
        _linefitpars = null;
        _helicalfitoutput = null;
        _chisq[0] = 0;
        _chisq[1] = 0;
        W.clear();
        ((ArrayList<Double>) W).ensureCapacity(X.size());

        for (int j = 0; j < X.size(); j++) {
            
            if (errRt.get(j) == 0) {
                System.err.println("Point errors ill-defined -- helical fit exiting");
                return FitStatus.CircleFitFailed;
            }
            W.add(j, 1. / (errRt.get(j) * errRt.get(j))); //the weight is the 1./error^2

        }

        // fit the points 
        // check the status
        _circlefit = new CircleFitter(xb, yb);
        boolean circlefitstatusOK = _circlefit.fitStatus(X, Y, W, X.size());

        if (!circlefitstatusOK) { 
            return FitStatus.CircleFitFailed;
        }

        //  Corroborate using the simple circle calculator
        // take each consecutive set of three points in the trackcand point list and calculate the circle going thru them
        // return the rms of the curvatures so-obtained and compare to the fit value
        // Points P_i P_i[0]=x, P_i[1]=y
        P0.clear();
        P1.clear();
        P2.clear();

        int passed_fits = 0;
        double averageChi2 = 0;
        double chi2 = 0;

        int index0 = 0;
        int index1 = 1;
        int index2 = 2;
        for (int j = 0; j < X.size() - 2; j++) {
            P0.add(0, X.get(index0));
            P0.add(1, Y.get(index0));
            P1.add(0, X.get(index1));
            P1.add(1, Y.get(index1));
            P2.add(0, X.get(index2));
            P2.add(1, Y.get(index2));

            _circlecalc = new CircleCalculator();
            boolean circlecalcstatusOK = _circlecalc.status(P0, P1, P2);
            //get average of residuals from calcvalues to fit
            if (circlecalcstatusOK && _circlecalc.getCalc().radius() != 0) {
                passed_fits++;
                double calcrhovalue = (1. / _circlecalc.getCalc().radius());
                chi2 += (_circlefit.getFit().rho() - calcrhovalue) * (_circlefit.getFit().rho() - calcrhovalue);
            }
            index0++;
            index1++;
            index2++;
        }

        if (passed_fits == 0) {
            return FitStatus.AllCircleCalcsFailed;
        }

        double avechi2Max = 1.;
        if (passed_fits > 0) {
            averageChi2 = chi2 / passed_fits;
            if (averageChi2 > avechi2Max) {
                return FitStatus.CircleCalcInconsistentWithFit;
            }
        }

        //Line fit
        _linefit = new LineFitter();

        boolean linefitstatusOK = _linefit.fitStatus(Rho, Z, errRho, ErrZ, Z.size());

        if (!linefitstatusOK) {
            return FitStatus.LineFitFailed; 
        }
        //  Get the results of the fits
        _linefitpars = _linefit.getFit();

        _circlefitpars = _circlefit.getFit();

        // get the parameters of the helix representation of the track
        double fit_dca = _circlefitpars.doca(); //check sign convention
        double fit_phi_at_dca = _circlefitpars.phi();
        double fit_curvature = _circlefitpars.rho();
        double fit_tandip = _linefitpars.slope();
        double fit_Z0 = _linefitpars.intercept() - _circlefitpars.arcLength(xb, yb, 0, 0)*fit_tandip;

        //require vertex position inside of the inner barrel
        if (Math.abs(fit_dca) > SVTGeometry.getLayerRadius(1)) {
//            if (Math.abs(fit_dca) > Constants.MODULERADIUS[0][0] || Math.abs(fit_Z0) > 100) {
            return null;
        }

        // get the error matrix
       
        //error matrix (assuming that the circle fit and line fit parameters are uncorrelated)
        // | d_dca*d_dca                   d_dca*d_phi_at_dca            d_dca*d_curvature        0            0             |
        // | d_phi_at_dca*d_dca     d_phi_at_dca*d_phi_at_dca     d_phi_at_dca*d_curvature        0            0             |
        // | d_curvature*d_dca	    d_curvature*d_phi_at_dca      d_curvature*d_curvature         0            0             |
        // | 0                              0                             0                    d_Z0*d_Z0                     |
        // | 0                              0                             0                       0        d_tandip*d_tandip |
        // 
        double[][] fit_covmatrix = new double[5][5];
        // the circle covariance matrix
        //covr[0] =  delta_rho.delta_rho;
        //covr[1] =  delta_rho.delta_phi;
        //covr[2] =  delta_rho.delta_dca;
        //covr[3] =  delta_phi.delta_phi;
        //covr[4] =  delta_phi.delta_dca;
        //covr[5] =  delta_dca.delta_dca;
        
        fit_covmatrix[0][0] = _circlefitpars.cov()[5];
        fit_covmatrix[1][0] = _circlefitpars.cov()[4];
        fit_covmatrix[2][0] = _circlefitpars.cov()[2];
        fit_covmatrix[0][1] = _circlefitpars.cov()[4];
        fit_covmatrix[1][1] = _circlefitpars.cov()[3];
        fit_covmatrix[2][1] = _circlefitpars.cov()[1];
        fit_covmatrix[0][2] = _circlefitpars.cov()[2];
        fit_covmatrix[1][2] = _circlefitpars.cov()[1];
        fit_covmatrix[2][2] = _circlefitpars.cov()[0];
        fit_covmatrix[3][3] = _linefitpars.interceptErr() * _linefitpars.interceptErr();
        fit_covmatrix[4][4] = _linefitpars.slopeErr() * _linefitpars.slopeErr();
        fit_covmatrix[3][4] = _linefitpars.slopeIntercCov();
        fit_covmatrix[4][3] = _linefitpars.slopeIntercCov();
        
        if(fit_curvature==0) {
            return FitStatus.CircleFitFailed;
        }
        Helix helixresult = new Helix(fit_dca, fit_phi_at_dca, fit_curvature, fit_Z0, fit_tandip, xb, yb, fit_covmatrix);
        
        sethelix(helixresult);
        _chisq[0] = _circlefitpars.chisq();
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
     * Return the circle fit for the most recent helix fit. Returns null if the
     * circle fit was not successful.
     *
     * @return circle fit from most recent helix fit
     */
    public CircleFitPars getCircleFit() {
        return _circlefitpars;
    }

    public CircleCalcPars getcirclecalcpars() {
        return _circlecalcpars;
    }

    public void setcirclecalcpars(CircleCalcPars _circlecalcpars) {
        this._circlecalcpars = _circlecalcpars;
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

    public void setReferencePoint(double x, double y) {
        _circlefit.setrefcoords(x, y);
    }

    public Helix gethelix() {
        return _helix;
    }

    public void sethelix(Helix helix) {
        this._helix = helix;
    }

    public double[] getchisq() {
        return _chisq;
    }

    public void setcovmat(double[] chisq) {
        this._chisq = chisq;
    }

}
