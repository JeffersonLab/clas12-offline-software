package cnuphys.swimtest;

import java.util.Random;

public class RandomData {
	public int count;
	public int charge[];
	public double xo[];
	public double yo[];
	public double zo[];
	public double p[];
	public double theta[];
	public double phi[];
	
	private Random rand;
	
	/**
	 * Create some random data for a swim test
	 * @param n the number of points
	 * @param seed random number seed
	 */
	public RandomData(int n, long seed) {
		
		this(n, seed, -0.01, 0.02, -0.01, 0.02, -0.01, 0.02,
				0.9, 5.0, 25, 20, 0, 360);
	}
	
	public String toStringRaw(int index) {
		return String.format("%2d %7.4f  %7.4f  %7.4f   %6.3f   %6.3f  %7.3f", 
				charge[index], xo[index], yo[index], zo[index], p[index], theta[index], phi[index]);		
	}
	
	/**
	 * Create some random data for a swim test
	 * @param n
	 * @param seed
	 * @param xmin
	 * @param dx
	 * @param ymin
	 * @param dy
	 * @param zmin
	 * @param dz
	 * @param pmin
	 * @param dp
	 * @param thetamin
	 * @param dtheta
	 * @param phimin
	 * @param dphi
	 */
	public RandomData(int n, long seed,
			double xmin, double dx,
			double ymin, double dy,
			double zmin, double dz,
			double pmin, double dp,
			double thetamin, double dtheta,
			double phimin, double dphi) {
		
		
		count = n;
	    rand = new Random(seed);
		
	    charge = new int[n];
	    xo = new double[n];
	    yo = new double[n];
	    zo = new double[n];
	    p = new double[n];
	    theta = new double[n];
	    phi = new double[n];
	    
	    for (int i = 0; i < n; i++) {
	    	charge[i] = (rand.nextDouble() < 0.5) ? -1 : 1;
	    	p[i] = dval(pmin, dp); //Gev
	    	theta[i] = dval(thetamin, dtheta);
	    	phi[i] = dval(phimin, dphi);
	    	
	    	xo[i] = dval(xmin, dx); //meters
	    	yo[i] = dval(ymin, dy); //meters
	    	zo[i] = dval(zmin, dz); //meters
	    	
	    }


	}
	
	
	private double dval(double min, double dv) {
		return min + dv*rand.nextDouble();
	}
}
