package org.jlab.rec.cvt.fit;

import org.jlab.rec.cvt.trajectory.Helix;

import Jama.Matrix;

	
/** A fitter which does sequential fit (for r, phi coordinates) to a circle using CircleFitter 
*	 and then (for r, z coordinates) to a line using LineFitter.
*	 Uses CircleCalculator for corroboration of the results obtained from CircleCalculator
*/

public class HelicalTrackFitter {


		private Helix    _helix;  // fit helix
		private double[] _chisq = new double[2];  // fit chi-squared [0]: circle [1] line

	
		private CircleCalculator _circlecalc = new CircleCalculator();
		private CircleFitter _circlefit = new CircleFitter();
	    private LineFitter _linefit  = new LineFitter();
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
        
	    /**
	     * Does fit to 2 sets of inputs : circle fit to array X[i] Y[i] and line fit to array Rho[j] Z[j]
	     * @param X  array i
	     * @param Y  array i
	     * @param Z  array j
	     * @param Rho array j
	     * @param errRt error on XY
	     * @param errRho error on Rho
	     * @param ErrZ array j
	     * @return
	     */    
	    public FitStatus fit(double[] X, double[] Y, double[] Z, double Rho[], double[] errRt, double[] errRho, double[] ErrZ) {    
	    	
	        //  Initialize the various fitter outputs
	        _circlefitpars = null;
	        _linefitpars = null;
	        _helicalfitoutput = null;
 
   		    double[] W = new double[X.length]; //the weight on the point for circle fit 
	   		 X[0]=0;
	   		 Y[0]=0;
	   		 Z[0]=0;
	   		
	   		 for(int j=0; j<X.length;j++){
	   			 
	   			 if(errRt[j]==0) {
	   				 System.err.println("Point errors ill-defined -- helical fit exiting");
	   				 return FitStatus.CircleFitFailed;	   				
	   			 }
	   			 W[j] = 1./(errRt[j]*errRt[j]); //the weight is the 1./error^2
	   			 
	   		 }
	    
	   			 
	   		 // fit the points 
	   		 // check the status
	   		 
	 		 _circlefit = new CircleFitter();
	   		 boolean circlefitstatusOK = _circlefit.fitStatus(X, Y, W, X.length); 
	   		

	        if (!circlefitstatusOK) {
	            return FitStatus.CircleFitFailed;
	        }

            //  Corroborate using the simple circle calculator
	        // take each consecutive set of three points in the trackcand point list and calculate the circle going thru them
	        // return the rms of the curvatures so-obtained and compare to the fit value
	        // Points P_i P_i[0]=x, P_i[1]=y
	        double[] P0 = new double[2];
	        double[] P1 = new double[2];
	        double[] P2 = new double[2];
	        
	        
	        int passed_fits = 0;
	        double averageChi2 = 0;
	        double chi2 = 0;
	        
	            int index0=0;
	            int index1=1;
	            int index2=2;
		        for(int j=0; j<X.length-2;j++){
		        	P1[0]=X[index0];
			       	P1[1]=Y[index0];
			       	P1[0]=X[index1];
			       	P1[1]=Y[index1];
			       	P2[0]=X[index2];
			       	P2[1]=Y[index2];
			       	
			       	_circlecalc = new CircleCalculator();
		   	   		 boolean circlecalcstatusOK = _circlecalc.status(P0, P1, P2); 
		   	   		 //get average of residuals from calcvalues to fit
		   	   		 if(circlecalcstatusOK && _circlecalc.getCalc().radius()!=0) {
		   	   			 passed_fits++;
		   	   			 double calcrhovalue = (1./_circlecalc.getCalc().radius());
		   	   		     chi2 += (_circlefit.getFit().rho()-calcrhovalue)*(_circlefit.getFit().rho()-calcrhovalue);
		   	   		 }
		   	   		 index0++;
		   	   		 index1++;
		   	   		 index2++;
		        }
		        if(passed_fits==0) {
		            return FitStatus.AllCircleCalcsFailed;
		        }
                
		        double avechi2Max = 1.; 
		        if(passed_fits>0) { 
		        	averageChi2 = chi2/passed_fits;
		        	if(averageChi2>avechi2Max) { 
		            return FitStatus.CircleCalcInconsistentWithFit; 
		        	}
		        }

		     //Line fit
		     _linefit = new LineFitter();
		   	
		   	boolean linefitstatusOK = _linefit.fitStatus(Rho, Z, errRho, ErrZ, Z.length);
		   		 	
	        if (!linefitstatusOK) {
	            return FitStatus.LineFitFailed;
	        }
	        //  Get the results of the fits
	        _linefitpars = _linefit.getFit();
	        
	        _circlefitpars = _circlefit.getFit();
	        
	       
	        // get the parameters of the helix representation of the track
	        double fit_dca = _circlefitpars.doca(); //check sign convention
	        double fit_phi_at_dca  = _circlefitpars.phi();   
	        double fit_curvature = _circlefitpars.rho();
	        double fit_tandip = _linefitpars.slope();
	        
	        //double fit_Z0 = _linefitpars.intercept();
	        double fit_Z0 = _linefitpars.intercept();
	        
	        // get the error matrix
	    	 Matrix fit_covmatrix = new Matrix(5,5);
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
	        fit_covmatrix.set(0, 0, _circlefitpars.cov()[5]);
	        fit_covmatrix.set(1, 0, _circlefitpars.cov()[4]);
	        fit_covmatrix.set(2, 0, _circlefitpars.cov()[2]);
	        fit_covmatrix.set(0, 1, _circlefitpars.cov()[4]);
	        fit_covmatrix.set(1, 1, _circlefitpars.cov()[3]);
	        fit_covmatrix.set(2, 1, _circlefitpars.cov()[1]);
	        fit_covmatrix.set(0, 2, _circlefitpars.cov()[2]);
	        fit_covmatrix.set(1, 2, _circlefitpars.cov()[1]);
	        fit_covmatrix.set(2, 2, _circlefitpars.cov()[0]);
	        fit_covmatrix.set(3, 3, _linefitpars.interceptErr()*_linefitpars.interceptErr());
	        fit_covmatrix.set(4, 4, _linefitpars.slopeErr()*_linefitpars.slopeErr());
	        
	        
             Helix helixresult = new Helix(fit_dca, fit_phi_at_dca, fit_curvature, fit_Z0, fit_tandip, fit_covmatrix);

            set_helix(helixresult);
            _chisq[0]=_circlefitpars.chisq();
            _chisq[1]=_linefitpars.chisq();          
            
            //System.out.println("chi2 "+_chisq[0]+" " + _chisq[1]);
	        //  Create the HelicalTrackFit for this helix
            _helicalfitoutput = new HelicalTrackFitPars(helixresult, _chisq);
           
	        return FitStatus.Successful;
	    }

	    /**
	     * Return the results of the most recent helix fit.  Returns null if the fit
	     * was not successful.
	     * @return HelicalTrackFitPars from the most recent helix fit
	     */
	    public HelicalTrackFitPars getFit() {
	        return _helicalfitoutput;
	    }

	    /**
	     * Return the circle fit for the most recent helix fit.  Returns null if the
	     * circle fit was not successful.
	     * @return circle fit from most recent helix fit
	     */
	    public CircleFitPars getCircleFit() {
	        return _circlefitpars;
	    }

	    public CircleCalcPars get_circlecalcpars() {
			return _circlecalcpars;
		}

		public void set_circlecalcpars(CircleCalcPars _circlecalcpars) {
			this._circlecalcpars = _circlecalcpars;
		}

		/**
	     * Return the s-z line fit for the most recent helix fit.  If the line fit failed
	     * or was not performed due to not having enough 3D hits, null is returned.
	     * @return line fit for most recent helix fit
	     */
	    public LineFitPars getLineFit() {
	        return _linefitpars;
	    }


	    public void setReferencePoint(double x, double y) {
	        _circlefit.setrefcoords(x, y);
	        return;
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


	

	
	
}


