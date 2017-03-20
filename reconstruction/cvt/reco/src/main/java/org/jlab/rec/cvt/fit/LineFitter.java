package org.jlab.rec.cvt.fit;


/** A least square fitting method
*   For a linear fit,f(a,b)=a+bx taking  y errors into account
*/

public class LineFitter {

	//instantiate
	private LineFitPars _linefitresult;
	
	// the constructor
	public LineFitter() {
	}
	
	// fit status
	public boolean fitStatus(double[] x, double[] y, double[] sigma_x, double[] sigma_y, int nbpoints) {
		boolean fitStat = false;
		
		if (nbpoints>=2) {  // must have enough points to do the fit
			// now do the fit
			// initialize weight-sum and moments
			double Sw, Sx, Sy, Sxx, Sxy;
			Sw = Sx = Sy = Sxx = Sxy =0.;
			
			double[] w = new double[nbpoints];
			
			for (int i = 0; i<nbpoints; i++) { 
				if((sigma_y[i]*sigma_y[i]+sigma_x[i]*sigma_x[i])==0) {
					return false;
				}
				w[i] = 1./(sigma_y[i]*sigma_y[i]+sigma_x[i]*sigma_x[i]); 
				//w[i] = 1./(sigma_x[i]*sigma_x[i]); 
				Sw  += w[i];
				// the moments
				Sx  += x[i]*w[i];
				Sy  += y[i]*w[i];
				Sxy += x[i]*y[i]*w[i];
				Sxx += x[i]*x[i]*w[i]; 
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
					chi_2 += ((y[j]-(slopeSol*x[j]+intercSol))*(y[j]-(slopeSol*x[j]+intercSol)))*w[j];
					pointchi_2[j] = ((y[j]-(slopeSol*x[j]+intercSol))*(y[j]-(slopeSol*x[j]+intercSol)))*w[j];  					
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
	public static void main (String arg[]) {
		
	      double[] X = new double[3];
	      double[] Y = new double[3];
	      double[] eY = new double[3];
	      double[] eX = new double[3];
	      for (int i = 0; i<3; i++) {
	    	  X[i]=1+i;
	    	  Y[i] = 5*X[i]+90;
	    	  eX[i]=0;
	      }

	    eY[0] = .8;
	    eY[1]=.3;
	    eY[2]=.4;
	    LineFitter _linefit = new LineFitter();

	   
	    boolean linefitstatusOK = _linefit.fitStatus(X, Y, eX, eY, 3);
	    if(linefitstatusOK) {
	    LineFitPars _linefitpars = _linefit.getFit();
	    System.out.println(_linefitpars.intercept());
	    System.err.println(_linefitpars.interceptErr());
	    System.out.println(_linefitpars.slope());
	    System.err.println(_linefitpars.slopeErr());
	    }
	}
	
}
