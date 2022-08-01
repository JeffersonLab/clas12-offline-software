package org.jlab.rec.dc.track.fit.basefit;

import java.util.ArrayList;
import java.util.List;

/** A least square fitting method
*   For a linear fit,f(a,b)=a+bx taking  y errors into account
*/

public class LineFitter {

	//instantiate
	private LineFitPars _linefitresult;
	
	// the constructor
	public LineFitter() {
	}
	private final List<Double> w = new ArrayList<Double>();
	// fit status
	public boolean fitStatus(List<Double> x, List<Double> y, List<Double> sigma_x, List<Double> sigma_y, int nbpoints) {
		boolean fitStat = false;
		
		if (nbpoints>=2) {  // must have enough points to do the fit
			// now do the fit
			// initialize weight-sum and moments
			double Sw, Sx, Sy, Sxx, Sxy;
			Sw = Sx = Sy = Sxx = Sxy =0.;
			
			w.clear();
			((ArrayList<Double>) w).ensureCapacity(nbpoints);
			
			for (int i = 0; i<nbpoints; i++) { 
				if((sigma_y.get(i)*sigma_y.get(i)+sigma_x.get(i)*sigma_x.get(i))==0) {
					return false;
				}
				w.add(i, 1./(sigma_y.get(i)*sigma_y.get(i)+sigma_x.get(i)*sigma_x.get(i)) ); 
				//w.get(i) = 1./(sigma_x.get(i)*sigma_x.get(i)); 
				Sw  += w.get(i);
				// the moments
				Sx  += x.get(i)*w.get(i);
				Sy  += y.get(i)*w.get(i);
				Sxy += x.get(i)*y.get(i)*w.get(i);
				Sxx += x.get(i)*x.get(i)*w.get(i); 
			}
			// the determinant
			double determ = Sw*Sxx - Sx*Sx;  // the determinant; must be >0
			
			if(determ<1e-19) 
				determ=1e-19; //straight track approximation
			
			double slopeSol  = (Sw*Sxy - Sx*Sy)/determ;
			double intercSol = (Sy*Sxx - Sx*Sxy)/determ;
			// the errors on these parameters
			double slopeEr  = Math.sqrt(Sw/determ);
			double intercEr = Math.sqrt(Sxx/determ);
			double SlInCov   = -Sx/determ; 
			
			if(Math.abs(slopeSol)>=0 && Math.abs(intercSol)>=0) {
				
				// calculate the chi^2
				double chi_2 = 0.; 
				double pointchi_2[] = new double[nbpoints]; //individual chi2 for each fitted point
				for (int j = 0; j<nbpoints; j++) { 
					chi_2 += ((y.get(j)-(slopeSol*x.get(j)+intercSol))*(y.get(j)-(slopeSol*x.get(j)+intercSol)))*w.get(j);
					pointchi_2[j] = ((y.get(j)-(slopeSol*x.get(j)+intercSol))*(y.get(j)-(slopeSol*x.get(j)+intercSol)))*w.get(j);  					
				}
				// the number of degrees of freedom
				int Ndf = nbpoints - 2;
				
				// instantiate fit object to be returned;
				_linefitresult = new LineFitPars(slopeSol, intercSol, slopeEr, intercEr, SlInCov, chi_2, pointchi_2, Ndf);
				
				fitStat = true;
				}
		}
		
		// if there is a fit return true
		return fitStat;
	}
	// return the fit result
	public LineFitPars getFit() {
		return _linefitresult;
	}
	
	
}
