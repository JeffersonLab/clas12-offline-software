package org.jlab.rec.cvt.fit;

import java.util.ArrayList;
import java.util.List;

import org.jlab.rec.cvt.Constants;

//import Jama.Matrix;

/**
 * Circle fit using the Karimaki algorithm. The algorithm returns Gaussian
 * parameters rho (circle curvature), d (doca to orig.) and phi (at doca).
 * Allows for non-iterative solution. Returns covariance matrix of fitted
 * parameter.
 *
 * Algorithm Reference: Nuclear Instruments and Methods in Physics Research A305
 * (1991) 187-191 Effective circle fitting for particle trajectories by V.
 * Karimaki
 *
 */
public class CircleFitter {

    private double _xref;  	// x coord. of the ref. point
    private double _yref;  	// y coord. of the ref. point

    private double _xx0;     // hit x coordinate
    private double _yy0;     // hit y coordinate

    private double Sw, Sx, Sy, Sr2, Sxx, Sxy, Syy, Sxr2, Syr2, Sr2r2; //  moments 

    // fit outputs
    private double _rho;   	// the curvature of the circle
    private double _phi;    // the value of phi at the ref. point
    private double _dca;    // the distance of closest approach to the origin
    private double _chi2;   // the chi^2 value of the fit

    private double _xpca;	// x coord. of the point of closest approach to the origin
    private double _ypca;   // y coord. of the point of closest approach to the origin

    private double[] _covr; // the elements of the symmetric 3x3 covariance matrix of the parameters rho, phi, dca

    /**
     * Constructor Sets the reference point to (0,0)
     * @param xb
     * @param yb
     */
    public CircleFitter(double xb, double yb) {
        _xref = xb;
        _yref = yb;
        _covr = new double[6];
    }
    // Now set the reference point for the fit

    public void setrefcoords(double xr, double yr) {
        _xref = xr;
        _yref = yr;
    }

    /**
     * Fits the set of data points given by arrays xm[], ym[], with
     * corresponding weights wm[].The number of points that are fitted is given
     * by NP.The fit returns a boolean flag of true if the fit was successful.
     *
     * @param xm
     * @param ym
     * @param wm
     * @param NP
     * @return 
     */
    public boolean fitStatus(List<Double> xm, List<Double> ym, List<Double> wm, int NP) {
        double xl, yl, r2l, wt, wx, wy, wr2;  // The local point positions and weight parameters

        double Cxx, Cxy, Cyy, Cxr2, Cyr2, Cr2r2; // these coefficients are the statistical covariances of the xm[], ym[], r2m[]=xm[]^2+ym[]^2 measurements
        double q1, q2;
        double phiFit, kappa, delta;
        double rhoFit, docaFit;

        // Insure that there are at least three points to perform the fit
        if (NP < 3) {
            return false;
        }

        // Initialization of the Sum of the Weight parameters
        Sw = 0;
        Sx = 0;
        Sy = 0;
        Sr2 = 0;
        Sxx = 0;
        Sxy = 0;
        Syy = 0;
        Sxr2 = 0;
        Syr2 = 0;
        Sr2r2 = 0;

        _chi2 = 0;
        // Find a point on the circle used as a local origin
        // Use the second hit by default 
        // 
        //	_xx0 = xm.get(1);
        //	_yy0 = ym.get(1);

        //	_xx0=0.0; //bug
        //	_yy0=0.0;
        _xx0 = xm.get(1);
        _yy0 = ym.get(1);

        //double x = _xx0 - xm.get(0);
        //double y = _yy0 - ym.get(0);
        double x = xm.get(1) - xm.get(0);
        double y = ym.get(1) - ym.get(0);

        // Calculate the weight-sum parameters
        for (int p = 0; p < NP; p++) {

            xl = xm.get(p) - _xx0;
            yl = ym.get(p) - _yy0;
            r2l = xl * xl + yl * yl;
            wt = wm.get(p);
            wx = wt * xl;
            wy = wt * yl;
            wr2 = wt * r2l;

            Sw += wt;

            Sx += wx;
            Sy += wy;
            Sr2 += wr2;

            Sxx += wx * xl;
            Sxy += wx * yl;
            Syy += wy * yl;
            Sxr2 += wx * r2l;
            Syr2 += wy * r2l;
            Sr2r2 += wr2 * r2l;
        }

        if (Sw <= 0.) {
            return false;   // Weights needed  
        }
        // Obtain the solutions for the fit parameters
        Sx = Sx / Sw;
        Sy = Sy / Sw;
        Sr2 = Sr2 / Sw;
        Sxx = Sxx / Sw;
        Sxy = Sxy / Sw;
        Syy = Syy / Sw;
        Sxr2 = Sxr2 / Sw;
        Syr2 = Syr2 / Sw;
        Sr2r2 = Sr2r2 / Sw;

        //xmean  = Sx/Sw;
        //ymean  = Sy/Sw;
        //r2mean = (Sxx+Syy)/Sw;
        // The statistical covariances
        Cxx = Sxx - Sx * Sx;
        Cxy = Sxy - Sx * Sy;
        Cyy = Syy - Sy * Sy;
        Cxr2 = Sxr2 - Sx * Sr2;
        Cyr2 = Syr2 - Sy * Sr2;
        Cr2r2 = Sr2r2 - Sr2 * Sr2;

        if (Cr2r2 < 1.e-9) {
            return false;   //Comes in the denumerator; should be positively defined
        }
        q1 = Cr2r2 * Cxy - Cxr2 * Cyr2;
        q2 = Cr2r2 * (Cxx - Cyy) - Cxr2 * Cxr2 + Cyr2 * Cyr2;

        phiFit = 0.5 * Math.atan2(2. * q1, q2);

        kappa = (Math.sin(phiFit) * Cxr2 - Math.cos(phiFit) * Cyr2) / Cr2r2;

        delta = -kappa * Sr2 + Math.sin(phiFit) * Sx - Math.cos(phiFit) * Sy;

        // the curvature rho= 1/R ; 
        rhoFit = 2. * kappa / Math.sqrt(1. - 4. * delta * kappa);
        // the distance of closest approach to the origin
        docaFit = 2. * delta / (1. + Math.sqrt(1. - 4. * delta * kappa));
        // The chi2	
        _chi2 = Sw * ((1. + rhoFit * docaFit) * (1. + rhoFit * docaFit)) * ((Math.sin(phiFit) * Math.sin(phiFit)) * Cxx + (Math.cos(phiFit) * Math.cos(phiFit)) * Cyy - 2. * Math.sin(phiFit) * Math.cos(phiFit) * Cxy - kappa * kappa * Cr2r2);

        // The explicit formulae for the inverse covariance matrix elements give
        // The covariance matrix
        double u = 1 + rhoFit * docaFit;
        double S_alpha = Math.sin(phiFit) * Sx - Math.cos(phiFit) * Sy;
        double S_beta = Math.cos(phiFit) * Sx + Math.sin(phiFit) * Sy;
        double S_gamma = ((Math.sin(phiFit) * Math.sin(phiFit)) - (Math.cos(phiFit) * Math.cos(phiFit))) * Sxy + Math.cos(phiFit) * Math.sin(phiFit) * (Sxx - Syy);
        double S_delta = Math.sin(phiFit) * Sxr2 - Math.cos(phiFit) * Syr2;
        double S_alphalpha = (Math.sin(phiFit) * Math.sin(phiFit)) * Sxx - 2. * Math.cos(phiFit) * Math.sin(phiFit) * Sxy + (Math.cos(phiFit) * Math.cos(phiFit)) * Syy;

        double invV_rhorho = 0.25 * (Sr2r2) - docaFit * (S_delta - docaFit * (S_alphalpha + 0.5 * Sr2 - docaFit * (S_alpha - 0.25 * docaFit * Sw)));
        double invV_rhophi = -u * (0.5 * (Math.cos(phiFit) * Sxr2 + Math.sin(phiFit) * Syr2) - docaFit * (Sy - 0.5 * docaFit * S_beta));
        double invV_phiphi = u * u * ((Math.cos(phiFit) * Math.cos(phiFit)) * Sxx + (Math.sin(phiFit) * Math.sin(phiFit)) * Syy + Math.sin(2. * phiFit) * Sxy);
        double invV_rhod = rhoFit * (-0.5 * S_delta + docaFit * S_alphalpha) + 0.5 * u * Sr2 - 0.5 * docaFit * ((2. * u + rhoFit * docaFit) * S_alpha - u * docaFit * Sw);
        double invV_phid = u * (rhoFit * S_gamma - u * S_beta);
        double invV_dd = rhoFit * (rhoFit * S_alphalpha - 2. * u * S_alpha) + u * u * Sw;

        double[][] array = {{invV_rhorho, invV_rhophi, invV_rhod}, {invV_rhophi, invV_phiphi, invV_phid}, {invV_rhod, invV_phid, invV_dd}};
        //Matrix inv_V = new Matrix(array);
        //Matrix V = inv_V.inverse();
        //Matrix result = new Matrix(V.getArray());
        // using this class methods
        
        double[][] mat3x3= invert3x3Matrix(array);
        
        double V_rhorho = mat3x3[0][0];
        double V_rhophi = mat3x3[0][1];
        double V_rhod = mat3x3[0][2];
        double V_phiphi = mat3x3[1][1];
        double V_phid = mat3x3[1][2];
        double V_dd = mat3x3[2][2];

        // Fill the covariance matrix
        _covr[0] = V_rhorho;
        _covr[1] = V_rhophi;
        _covr[2] = V_rhod;
        _covr[3] = V_phiphi;
        _covr[4] = V_phid;
        _covr[5] = V_dd;

        // chi2 derivatives
        double sigma = -rhoFit * S_delta + 2. * u * S_alphalpha - docaFit * (1. + u) * S_alpha;
        double dchi2drho = docaFit * sigma;
        double dchi2dd = rhoFit * sigma;

        // The corrections to be added:        
        double Delta_rho = -0.5 * (V_rhorho * dchi2drho + V_rhod * dchi2dd);
        double Delta_phi = -0.5 * (V_rhophi * dchi2drho + V_phid * dchi2dd);
        double Delta_doca = -0.5 * (V_rhod * dchi2drho + V_dd * dchi2dd);

        // Fit parameters output with corrections
        _rho = rhoFit + Delta_rho;
        _phi = phiFit + Delta_phi;
        _dca = docaFit + Delta_doca;
        _xpca = _xx0 + _dca * Math.sin(phiFit);
        _ypca = _yy0 - _dca * Math.cos(phiFit);
        if(_rho==0 || Double.isNaN(_rho))
            return false;
        
        // corrected chi2
        _chi2 = (1. + _rho * _dca) * (1. + _rho * _dca) * (1. - kappa * delta) * _chi2;

        // transformation to a new reference point
        propagatePars(_xref, _yref, x, y, Math.cos(_phi), Math.sin(_phi));

        return true;
    }

    private void propagatePars(double xr, double yr, double x, double y, double cosphi, double sinphi) {
        setrefcoords(xr, yr);
        // evaluate the fit parameters at the reference point
        //double X = _xpca -_xref;
        //double Y = _ypca -_yref;
        double X = _xx0 - _xref;
        double Y = _yy0 - _yref;

        double DeltaPerp = X * sinphi - Y * cosphi + _dca;
        double DeltaParl = X * cosphi + Y * sinphi;
        double u = 1. + _rho * _dca;
        double B = _rho * X + u * sinphi;
        double C = -_rho * Y + u * cosphi;
        double A = 2. * DeltaPerp + _rho * (DeltaPerp * DeltaPerp + DeltaParl * DeltaParl);
        double U = Math.sqrt(1. + _rho * A);

        // error matrix calculation
        //1. The Jacobian Matrix
        double xi = 1. / (B * B + C * C);
        double nu = 1. + _rho * DeltaPerp;
        double lambda = 0.5 * A / ((1. + U) * (1. + U) * U);
        double mu = 1. / (U * (1. + U)) + _rho * lambda;
        double zeta = DeltaPerp * DeltaPerp + DeltaParl * DeltaParl;

        // V' = JVJ^T
        double[][] Jarray = {{1, 0, 0}, {xi * DeltaParl, xi * u * nu, -xi * _rho * _rho * DeltaParl}, {mu * zeta - lambda * A, 2. * mu * u * DeltaParl, 2. * mu * nu}};
        //2. Matrix Multiplication

        double[][] Varray = {{_covr[0], _covr[1], _covr[2]}, {_covr[1], _covr[3], _covr[4]}, {_covr[2], _covr[4], _covr[5]}};

        //        Matrix J = new Matrix(Jarray);
//        Matrix JT = J.transpose();
//        Matrix V = new Matrix(Varray);
//        Matrix Vp = J.times(V.times(JT));
//
//        Matrix result = new Matrix(Vp.getArray());

        double[][] JarrayT = this.transpose3x3Matrix(Jarray);
        double[][] VJT = this.multiply3x3Matrices(Varray, JarrayT);
        double[][] JVJT = this.multiply3x3Matrices(Jarray, VJT);
        
        double Vp_rhorho = JVJT[0][0];
        double Vp_rhophi = JVJT[0][1];
        double Vp_rhod = JVJT[0][2];
        double Vp_phiphi = JVJT[1][1];
        double Vp_phid = JVJT[1][2];
        double Vp_dd = JVJT[2][2];
        //3. Fill the covariance matrix
        if(Constants.getInstance().seedBeamSpotConstraint()==false) {
            _covr[0] =  Vp_rhorho;
            _covr[1] =  Vp_rhophi;
            _covr[2] =  Vp_rhod;
            _covr[3] =  Vp_phiphi;
            _covr[4] =  Vp_phid;
            _covr[5] =  Vp_dd; 
        }
        //  new params
        _phi = Math.atan2(B, C);
        _dca = A / (1. + U);
        // get the correct signed curvature
        double s = Math.cos(_phi) * x + Math.sin(_phi) * y;
        if (s < 0) {
            _phi += Math.PI;
            _dca = -_dca;
            _rho = -_rho;
            _covr[1] = -_covr[1];
            _covr[4] = -_covr[4];
        }
        if (_phi >= 2. * Math.PI) {
            _phi -= 2. * Math.PI;
        }
        if (_phi < 0) {
            _phi += 2. * Math.PI;
        }
        // update parameters
        _xpca = _xref + _dca * Math.sin(_phi);
        _ypca = _yref - _dca * Math.cos(_phi);
    }

    public CircleFitPars getFit() {
        return new CircleFitPars(_xref, _yref, _rho, _phi, _dca, _chi2, _covr);
    }

    public CircleFitPars propagatefit(double xp, double yp) {
        double x = Math.cos(_phi);
        double y = Math.sin(_phi);
        double cosphi = Math.cos(_phi);
        double sinphi = Math.sin(_phi);
        propagatePars(xp, yp, x, y, cosphi, sinphi);

        return new CircleFitPars(_xref, _yref, _rho, _phi, _dca, _chi2, _covr);
    }

    public static void main(String[] args) {
        //List<Double> xm, List<Double> ym, List<Double> wm;
        List<Double> xm = new ArrayList<>();
        List<Double> ym = new ArrayList<>();
        List<Double> wm = new ArrayList<>();
        //	xm.add((double) 5.);
        //	ym.add((double) 5.);
        //	wm.add((double) 1.);
        xm.add((double) 59.109);
        ym.add((double) 29.104);
        wm.add((double) 1.);
        xm.add((double) 86.68);
        ym.add((double) 39.06);
        wm.add((double) 1.);
        xm.add((double) 112.275);
        ym.add((double) 46.82);
        wm.add((double) 1.);

        List<Double> P0 = new ArrayList<>(2);
        List<Double> P1 = new ArrayList<>(2);
        List<Double> P2 = new ArrayList<>(2);
        // Find the intersection of the lines joining the innermost to middle and middle to outermost point
        P0.add(xm.get(0));
        P1.add(xm.get(1));
        P2.add(xm.get(2));
        P0.add(ym.get(0));
        P1.add(ym.get(1));
        P2.add(ym.get(2));

        double ma = (P1.get(1) - P0.get(1)) / (P1.get(0) - P0.get(0));
        double mb = (P2.get(1) - P1.get(1)) / (P2.get(0) - P1.get(0));

        double xcen = 0.5 * (ma * mb * (P0.get(1) - P2.get(1)) + mb * (P0.get(0) + P1.get(0)) - ma * (P1.get(0) + P2.get(0))) / (mb - ma);
        double ycen = (-1. / mb) * (xcen - 0.5 * (P1.get(0) + P2.get(0))) + 0.5 * (P1.get(1) + P2.get(1));

        double CircRad = Math.sqrt((P0.get(0) - xcen) * (P0.get(0) - xcen) + (P0.get(1) - ycen) * (P0.get(1) - ycen));

        System.out.println("calculated radius " + CircRad + "  --> pt=" + (Constants.LIGHTVEL * 5.14 * CircRad));

        CircleFitter _circlefit = new CircleFitter(0, 0);
        boolean circlefitstatusOK = _circlefit.fitStatus(xm, ym, wm, xm.size());
    }

    
    public double[][] transpose3x3Matrix(double[][] theMatrix){
        int c1=3; int c2=3;
        double[][] transpMatrix = new double[c2][c1];
        for (int i = 0; i < c1; i++)
            for (int j = 0; j < c2; j++)
                transpMatrix[i][j] = theMatrix[j][i];

        return transpMatrix;
    }
    
    public double[][] multiply3x3Matrices(double[][] firstMatrix, double[][] secondMatrix) {
        int r1 =3; int c1=3; int c2=3;
        double[][] product = new double[r1][c2];
        for(int i = 0; i < r1; i++) {
            for (int j = 0; j < c2; j++) {
                for (int k = 0; k < c1; k++) {
                    product[i][j] += firstMatrix[i][k] * secondMatrix[k][j];
                }
            }
        }

        return product;
    }
    public double[][] invert3x3Matrix(double[][] m) {
        double[][] minv = new double[3][3];
        // computes the inverse of a matrix m
        double det = m[0][0] * (m[1][1] * m[2][2] - m[2][1] * m[1][2]) -
                     m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0]) +
                     m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0]);

        double invdet = 1 / det;
        minv[0][0] = (m[1][1] * m[2][2] - m[2][1] * m[1][2]) * invdet;
        minv[0][1] = (m[0][2] * m[2][1] - m[0][1] * m[2][2]) * invdet;
        minv[0][2] = (m[0][1] * m[1][2] - m[0][2] * m[1][1]) * invdet;
        minv[1][0] = (m[1][2] * m[2][0] - m[1][0] * m[2][2]) * invdet;
        minv[1][1] = (m[0][0] * m[2][2] - m[0][2] * m[2][0]) * invdet;
        minv[1][2] = (m[1][0] * m[0][2] - m[0][0] * m[1][2]) * invdet;
        minv[2][0] = (m[1][0] * m[2][1] - m[2][0] * m[1][1]) * invdet;
        minv[2][1] = (m[2][0] * m[0][1] - m[0][0] * m[2][1]) * invdet;
        minv[2][2] = (m[0][0] * m[1][1] - m[1][0] * m[0][1]) * invdet;
        
        return minv;
    }
}
