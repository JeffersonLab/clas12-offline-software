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
	 * Create some random data within a sector for a swim test
	 * @param n the number of points
	 * @param seed random number seed
	 * @param sector the sector [1..6]
	 */
	public RandomData(int n, long seed, int sector) {

		this(n, seed, -0.01, 0.02, -0.01, 0.02, -0.01, 0.02,
				0.9, 5.0, 25, 20, -30 + (sector-1)*60 + 10, 40);
	}


	/**
	 * Create some random data for a swim test
	 * @param n the number of points
	 * @param seed random number seed
	 */
	public RandomData(int n, long seed) {

		count = n;
	    rand = new Random(seed);

		//random sector


	    charge = new int[n];
	    xo = new double[n];
	    yo = new double[n];
	    zo = new double[n];
	    p = new double[n];
	    theta = new double[n];
	    phi = new double[n];

	    for (int i = 0; i < n; i++) {
			int sector = rand.nextInt(5) + 1;
			double phiMin = (sector-1)*60;


	    	charge[i] = (rand.nextDouble() < 0.5) ? -1 : 1;
	    	p[i] = dval(0.9, 5.0); //Gev
	    	theta[i] = dval(25, 20);
	    	phi[i] = dval(phiMin, 40);

	    	xo[i] = dval( -0.01, 0.02); //meters
	    	yo[i] = dval( -0.01, 0.02); //meters
	    	zo[i] = dval( -0.01, 0.02); //meters

	    }

	}


	public String toStringRaw(int index) {
		return String.format("%2d %7.4f  %7.4f  %7.4f   %6.3f   %6.3f  %7.3f",
				charge[index], xo[index], yo[index], zo[index], p[index], theta[index], phi[index]);
	}

	/**
	 * Create some random data for a swim test
	 * @param n the number to generate
	 * @param seed the random number seed
	 * @param xmin the x vertex minimum meters
	 * @param dx the spread in x meters
	 * @param ymin the y vertex minimum meters
	 * @param dy the spread in y meters
	 * @param zmin the z vertex minimum meters
	 * @param dz the spread in z meters
	 * @param pmin the minimum momentum p in Gev/c
	 * @param dp the spread in momentum p
	 * @param thetamin the minimum theta in degrees
	 * @param dtheta the spread in theta
	 * @param phimin the minimum phi in degrees
	 * @param dphi the spread in phi
	 */
	public RandomData(int n, long seed,
			double xmin, double dx,
			double ymin, double dy,
			double zmin, double dz,
			double pmin, double dp,
			double thetamin, double dtheta,
			double phimin, double dphi) {

		System.out.println("phimin: " + phimin);
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

	//get a random value in a range
	private double dval(double min, double dv) {
		return min + dv*rand.nextDouble();
	}
}
