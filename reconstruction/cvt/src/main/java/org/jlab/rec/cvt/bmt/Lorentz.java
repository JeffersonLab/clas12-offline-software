package org.jlab.rec.cvt.bmt;

/**
 * 
 * @author defurne
 */

public class Lorentz {
	
	public Lorentz() {
		
	}
	
	public static double GetLorentzAngle(double xe, double xb) {
		if (xe==0||xb==0) return 0;
		double de = (org.jlab.rec.cvt.bmt.Constants.emax-org.jlab.rec.cvt.bmt.Constants.emin)/(org.jlab.rec.cvt.bmt.Constants.Ne-1);
		double db = (org.jlab.rec.cvt.bmt.Constants.bmax-org.jlab.rec.cvt.bmt.Constants.bmin)/(org.jlab.rec.cvt.bmt.Constants.Nb-1);	
		
		if (xe<org.jlab.rec.cvt.bmt.Constants.emin) {
		    xe=org.jlab.rec.cvt.bmt.Constants.emin;
		    System.err.println("Warning: E out of grid... setting it to Emin");
		  }
		  if (xe>=org.jlab.rec.cvt.bmt.Constants.emax) {
		    xe=org.jlab.rec.cvt.bmt.Constants.emax*0.99;
		    System.err.println("Warning: E out of grid... setting it to Emax");
		  }
		  if (xb>org.jlab.rec.cvt.bmt.Constants.bmax) {
		    xb=org.jlab.rec.cvt.bmt.Constants.bmax*0.99;
		    System.err.println("Warning: B field out of grid... setting it to Bmax");
		  }
		  
		  int i11 = getBin( xe, xb);
		  int i12 = getBin( xe, xb+db);
		  int i21 = getBin( xe+de, xb);
		  int i22 = getBin( xe+de, xb+db);
		 
		  double Q11 = 0; double Q12 = 0; double Q21 = 0;   double Q22 = 0;
		  double e1 = org.jlab.rec.cvt.bmt.Constants.emin; double e2 = org.jlab.rec.cvt.bmt.Constants.emax; double b1 = 0; double b2 = org.jlab.rec.cvt.bmt.Constants.bmax; 
		  if (i11>=0) {
		    Q11=org.jlab.rec.cvt.bmt.Constants.ThetaL_grid[i11]; e1 = org.jlab.rec.cvt.bmt.Constants.E_grid[i11];  b1 = org.jlab.rec.cvt.bmt.Constants.B_grid[i11];
		  }
		  if (i12>=0) Q12 = org.jlab.rec.cvt.bmt.Constants.ThetaL_grid[i12];
		  if (xb>=org.jlab.rec.cvt.bmt.Constants.bmin) Q21 = org.jlab.rec.cvt.bmt.Constants.ThetaL_grid[i21];
		  if (xb<org.jlab.rec.cvt.bmt.Constants.bmin) Q21 = 0;
		  if (i22>=0) {
		    Q22 = org.jlab.rec.cvt.bmt.Constants.ThetaL_grid[i22]; e2 = org.jlab.rec.cvt.bmt.Constants.E_grid[i22];  b2 = org.jlab.rec.cvt.bmt.Constants.B_grid[i22];
		  }
		 
		  double R1 = linInterp( xe, e1,e2,Q11,Q21);
		  double R2 = linInterp( xe, e1,e2,Q12,Q22);
		  
		  double P =  linInterp( xb, b1,b2,R1, R2);
		  
		  return P;
	}

	
	public static double linInterp(double x, double x1, double x2, double y1, double y2) {
		// linear interpolation
		  // return y = f(x), given x1, y1=f(x1) and x2, y2=f(x2) 
		  // y = m * ( x - x1 ) + y1
		  // m = ( y2 - y1)/(x2 - x1)
		  
		  // compute m
		  double m = (y2 - y1)/(x2 - x1);
		  
		  // return
		  return m * ( x - x1 ) + y1;
	}
	
	public static int getBin( double e, double b){
		double de = (org.jlab.rec.cvt.bmt.Constants.emax-org.jlab.rec.cvt.bmt.Constants.emin)/(org.jlab.rec.cvt.bmt.Constants.Ne-1);
		double db = (org.jlab.rec.cvt.bmt.Constants.bmax-org.jlab.rec.cvt.bmt.Constants.bmin)/(org.jlab.rec.cvt.bmt.Constants.Nb-1);
		
		int ie = (int) Math.floor( (e - org.jlab.rec.cvt.bmt.Constants.emin)/de );
		int ib = (int) Math.floor( (b - org.jlab.rec.cvt.bmt.Constants.bmin)/db );
		
			//   std::cout << ie << "  " << ib << "\n";
		  	
		  return ib + org.jlab.rec.cvt.bmt.Constants.Nb * ie ;
		}	
}