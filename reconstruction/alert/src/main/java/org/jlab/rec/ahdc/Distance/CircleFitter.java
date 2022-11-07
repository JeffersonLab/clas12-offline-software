package org.jlab.rec.ahdc.Distance;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.List;

/**
 * Circle fit using the Karimaki algorithm. The algorithm returns Gaussian
 * parameters' rho (circle curvature), d (doca to orig.) and phi (at doca).
 * Allows for non-iterative solution. Returns covariance matrix of fitted
 * parameter.
 * <p>
 * Algorithm Reference: Nuclear Instruments and Methods in Physics Research A305
 * (1991) 187-191 Effective circle fitting for particle trajectories by V.
 * Karimaki
 */
public class CircleFitter {

	private double _xref;    // x coord. of the ref. point
	private double _yref;    // y coord. of the ref. point

	private double _xx0;     // hit x coordinate
	private double _yy0;     // hit y coordinate

	// fit outputs
	private double _rho;    // the curvature of the circle
	private double _phi;    // the value of phi at the ref. point
	private double _dca;    // the distance of the closest approach to the origin
	private double _chi2;   // the chi^2 value of the fit

	private final double[] _covr; // the elements of the symmetric 3x3 covariance matrix of the parameters' rho, phi, dca

	/**
	 * Constructor Sets the reference point to (0,0)
	 */
	public CircleFitter() {
		_xref = 0;
		_yref = 0;
		_covr = new double[6];
	}
	// Now set the reference point for the fit

	public void setrefcoords(double xr, double yr) {
		_xref = xr;
		_yref = yr;
	}

	/**
	 * Fits the set of data points given by arrays xm[], ym[], with
	 * corresponding weights wm[]. The number of points that are fitted is given
	 * by NP. The fit returns a boolean flag of true if the fit was successful.
	 */
	public boolean fitStatus(List<Double> xm, List<Double> ym, List<Double> wm, int NP) {
		double xl, yl, r2l, wt, wx, wy, wr2;  // The local point positions and weight parameters

		double Cxx, Cxy, Cyy, Cxr2, Cyr2, Cr2r2; // these coefficients are the statistical covariances of the xm[], ym[], r2m[]=xm[]^2+ym[]^2 measurements
		double q1, q2;
		double phiFit, kappa, delta;
		double rhoFit, docaFit;

		// Ensure that there are at least three points to perform the fit
		if (NP < 3) {
			return false;
		}

		// Initialization of the Sum of the Weight parameters
		double sw   = 0;
		double sx   = 0;
		double sy   = 0;
		double sr2  = 0;
		double sxx  = 0;
		double sxy  = 0;
		double syy  = 0;
		double sxr2 = 0;
		double syr2 = 0;
		//  moments
		double sr2r2 = 0;

		_chi2 = 0;
		// Find a point on the circle used as a local origin
		// Use the second hit by default
		_xx0 = xm.get(1);
		_yy0 = ym.get(1);

		double x = xm.get(1) - xm.get(0);
		double y = ym.get(1) - ym.get(0);

		// Calculate the weight-sum parameters
		for (int p = 0; p < NP; p++) {

			xl  = xm.get(p) - _xx0;
			yl  = ym.get(p) - _yy0;
			r2l = xl * xl + yl * yl;
			wt  = wm.get(p);
			wx  = wt * xl;
			wy  = wt * yl;
			wr2 = wt * r2l;

			sw += wt;

			sx += wx;
			sy += wy;
			sr2 += wr2;

			sxx += wx * xl;
			sxy += wx * yl;
			syy += wy * yl;
			sxr2 += wx * r2l;
			syr2 += wy * r2l;
			sr2r2 += wr2 * r2l;
		}

		if (sw <= 0.) {
			return false;   // Weights needed
		}
		// Obtain the solutions for the fit parameters
		sx    = sx / sw;
		sy    = sy / sw;
		sr2   = sr2 / sw;
		sxx   = sxx / sw;
		sxy   = sxy / sw;
		syy   = syy / sw;
		sxr2  = sxr2 / sw;
		syr2  = syr2 / sw;
		sr2r2 = sr2r2 / sw;

		//xmean  = Sx/Sw;
		//ymean  = Sy/Sw;
		//r2mean = (Sxx+Syy)/Sw;
		// The statistical covariances
		Cxx   = sxx - sx * sx;
		Cxy   = sxy - sx * sy;
		Cyy   = syy - sy * sy;
		Cxr2  = sxr2 - sx * sr2;
		Cyr2  = syr2 - sy * sr2;
		Cr2r2 = sr2r2 - sr2 * sr2;

		if (Cr2r2 < 1.e-9) {
			return false;   //Comes in the denumerator; should be positively defined
		}
		q1 = Cr2r2 * Cxy - Cxr2 * Cyr2;
		q2 = Cr2r2 * (Cxx - Cyy) - Cxr2 * Cxr2 + Cyr2 * Cyr2;

		phiFit = 0.5 * Math.atan2(2. * q1, q2);

		kappa = (Math.sin(phiFit) * Cxr2 - Math.cos(phiFit) * Cyr2) / Cr2r2;

		delta = -kappa * sr2 + Math.sin(phiFit) * sx - Math.cos(phiFit) * sy;

		// the curvature rho= 1/R ;
		rhoFit = 2. * kappa / Math.sqrt(1. - 4. * delta * kappa);
		// the distance of the closest approach to the origin
		docaFit = 2. * delta / (1. + Math.sqrt(1. - 4. * delta * kappa));
		// The chi2
		double sinphiFit = Math.sin(phiFit) * Math.sin(phiFit);
		double cosphiFit = Math.cos(phiFit) * Math.cos(phiFit);
		_chi2 = sw * ((1. + rhoFit * docaFit) * (1. + rhoFit * docaFit)) * (sinphiFit * Cxx + cosphiFit * Cyy - 2. * Math.sin(phiFit) * Math.cos(phiFit) * Cxy - kappa * kappa * Cr2r2);

		// The explicit formulae for the inverse covariance matrix elements give
		// The covariance matrix
		double u           = 1 + rhoFit * docaFit;
		double S_alpha     = Math.sin(phiFit) * sx - Math.cos(phiFit) * sy;
		double S_beta      = Math.cos(phiFit) * sx + Math.sin(phiFit) * sy;
		double S_gamma     = (sinphiFit - cosphiFit) * sxy + Math.cos(phiFit) * Math.sin(phiFit) * (sxx - syy);
		double S_delta     = Math.sin(phiFit) * sxr2 - Math.cos(phiFit) * syr2;
		double S_alphalpha = sinphiFit * sxx - 2. * Math.cos(phiFit) * Math.sin(phiFit) * sxy + cosphiFit * syy;

		double invV_rhorho = 0.25 * (sr2r2) - docaFit * (S_delta - docaFit * (S_alphalpha + 0.5 * sr2 - docaFit * (S_alpha - 0.25 * docaFit * sw)));
		double invV_rhophi = -u * (0.5 * (Math.cos(phiFit) * sxr2 + Math.sin(phiFit) * syr2) - docaFit * (sy - 0.5 * docaFit * S_beta));
		double invV_phiphi = u * u * (cosphiFit * sxx + sinphiFit * syy + Math.sin(2. * phiFit) * sxy);
		double invV_rhod   = rhoFit * (-0.5 * S_delta + docaFit * S_alphalpha) + 0.5 * u * sr2 - 0.5 * docaFit * ((2. * u + rhoFit * docaFit) * S_alpha - u * docaFit * sw);
		double invV_phid   = u * (rhoFit * S_gamma - u * S_beta);
		double invV_dd     = rhoFit * (rhoFit * S_alphalpha - 2. * u * S_alpha) + u * u * sw;

		double[][] array  = {{invV_rhorho, invV_rhophi, invV_rhod}, {invV_rhophi, invV_phiphi, invV_phid}, {invV_rhod, invV_phid, invV_dd}};
		RealMatrix inv_V  = MatrixUtils.createRealMatrix(array);
		RealMatrix result = MatrixUtils.inverse(inv_V);


		double V_rhorho = result.getEntry(0, 0);
		double V_rhophi = result.getEntry(0, 1);
		double V_rhod   = result.getEntry(0, 2);
		double V_phiphi = result.getEntry(1, 1);
		double V_phid   = result.getEntry(1, 2);
		double V_dd     = result.getEntry(2, 2);

		// Fill the covariance matrix
		_covr[0] = V_rhorho;
		_covr[1] = V_rhophi;
		_covr[2] = V_rhod;
		_covr[3] = V_phiphi;
		_covr[4] = V_phid;
		_covr[5] = V_dd;

		// chi2 derivatives
		double sigma     = -rhoFit * S_delta + 2. * u * S_alphalpha - docaFit * (1. + u) * S_alpha;
		double dchi2drho = docaFit * sigma;
		double dchi2dd   = rhoFit * sigma;

		// The corrections to be added:
		double Delta_rho  = -0.5 * (V_rhorho * dchi2drho + V_rhod * dchi2dd);
		double Delta_phi  = -0.5 * (V_rhophi * dchi2drho + V_phid * dchi2dd);
		double Delta_doca = -0.5 * (V_rhod * dchi2drho + V_dd * dchi2dd);

		// Fit parameters output with corrections
		_rho = rhoFit + Delta_rho;
		_phi = phiFit + Delta_phi;
		_dca = docaFit + Delta_doca;

		// corrected chi2
		_chi2 = (1. + _rho * _dca) * (1. + _rho * _dca) * (1. - kappa * delta) * _chi2;

		// transformation to a new reference point
		propagatePars(_xref, _yref, x, y, Math.cos(_phi), Math.sin(_phi));

		return true;
	}

	void propagatePars(double xr, double yr, double x, double y, double cosphi, double sinphi) {
		setrefcoords(xr, yr);
		// evaluate the fit parameters at the reference point
		double X = _xx0 - _xref;
		double Y = _yy0 - _yref;

		double DeltaPerp = X * sinphi - Y * cosphi + _dca;
		double DeltaParl = X * cosphi + Y * sinphi;
		double u         = 1. + _rho * _dca;
		double B         = _rho * X + u * sinphi;
		double C         = -_rho * Y + u * cosphi;
		double A         = 2. * DeltaPerp + _rho * (DeltaPerp * DeltaPerp + DeltaParl * DeltaParl);
		double U         = Math.sqrt(1. + _rho * A);

		//  new params
		_phi = Math.atan2(B, C);
		_dca = A / (1. + U);
		// get the correct signed curvature
		double s = Math.cos(_phi) * x + Math.sin(_phi) * y;
		if (s < 0) {
			_phi += Math.PI;
			_dca     = -_dca;
			_rho     = -_rho;
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
	}

	public CircleFitPars getFit() {
		return new CircleFitPars(_xref, _yref, _rho, _phi, _dca, _chi2, _covr);
	}
}
