/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.dc.track.fit.basefit;

import trackfitter.fitter.utilities.ProbChi2perNDF;

/**
 * The fit parameters of a line fit returned by LineFitter
 */
public class LineFitPars {

	private double _slope;      // line slope
	private double _interc;     // line y-intercept
	private double _slopeErr;   // error on the slope
	private double _intercErr;  // error in the intercept
	private double _SlIntCov;   // covariance matrix off-diagonal term
	private double _chi2;        // fit chi^2 
	private double[] _pointchi2; // fit chi^2 for each point
	private int    _ndf;         // fit NDF
	
	// constructor
	
	public LineFitPars(double slope, double interc, double slopeErr, double intercErr, double SlIntCov, double chi2, double[] pointchi2, int ndf) {
		_slope     = slope;
		_interc    = interc;
		_slopeErr  = slopeErr;
		_intercErr = intercErr;
		_SlIntCov  = SlIntCov;
		_chi2      = chi2;
		_pointchi2 = pointchi2;
		_ndf       = ndf;
	}
	//methods
	public double slope() {
		return _slope;
	}
	public double slopeErr() {
		return _slopeErr;
	}
	public double  intercept() {
		return _interc;
	}
	public double interceptErr() {
		return _intercErr;
	}
	public double SlopeIntercCov() {
		return _SlIntCov;
	}
	public double chisq() {
		return _chi2;
	}
	public int NDF() {
		return _ndf;
	}
	public double[] get_pointchi2() {
		return _pointchi2;
	}
	public double getProb() { 
		
		double chi2=this.chisq();
		int ndf = this.NDF();
		return ProbChi2perNDF.prob(chi2, ndf);	
	}
}
