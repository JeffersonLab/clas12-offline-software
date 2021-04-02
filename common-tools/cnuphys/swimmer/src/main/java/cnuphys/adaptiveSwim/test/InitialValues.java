package cnuphys.adaptiveSwim.test;

import java.util.Random;

/**
 * Hold the initial values of a swim
 * @author heddle
 *
 */
public class InitialValues {
	
	//usual small number cutoff
	private static final double SMALL = 1.0e-8;


	/** The integer charge */
	public int charge;
	
	/** The coordinate x of the vertex in meters */
	public double xo;

	/** The y coordinate of the vertex in meters */
    public double yo;

	/** The z coordinate of the vertex in meters */
    public double zo;
    
    /** The momentum in GeV/c */
	public double p;
	
	/** The polar angle in degrees */
	public double theta;
	
	/** The azimuthal angle in degrees */
	public double phi;
	
	public InitialValues() {
		
	}
	
	/**
	 * Store the initial conditions of a swim
	 * @param charge The integer charge
	 * @param xo The x coordinate of the vertex in meters
	 * @param yo The y coordinate of the vertex in meters
	 * @param zo The z coordinate of the vertex in meters
	 * @param p The momentum in GeV/c
	 * @param theta The polar angle in degrees
	 * @param phi The azimuthal angle in degrees
	 */
	public InitialValues(int charge, double xo, double yo, double zo, double p, double theta, double phi) {
		this.charge = charge;
		this.xo = xo;
		this.yo = yo;
		this.zo = zo;
		this.p = p;
		this.theta = theta;
		this.phi = phi;
	}
	
	/**
	 * Copy constructor
	 * @param src the source initial values
	 */
	public InitialValues(InitialValues src) {
		this(src.charge, src.xo, src.yo, src.zo, src.p, src.theta, src.phi);
	}
	
	@Override
	public String toString() {
		return 
		String.format("Q: %d\n", charge) +
		String.format("xo: %10.7e cm\n", xo) +
		String.format("yo: %10.7e cm\n", yo) +
		String.format("zo: %10.7e cm\n", zo) +
		String.format("p: %10.7e GeV/c\n", p) +
		String.format("theta: %10.7f deg\n", theta) +
		String.format("phi: %10.7f deg", phi);
	}
	

	/**
	 * For setting up an array of initial values for testing. In cases where the difference between
	 * a min val and a max val is < SMALL, the min val is used and the variable is no
	 * randomized.
	 * @param rand and random number generator
	 * @param num the number to create
	 * @param charge the integer charge
	 * @param randCharge if <code>true</code> the charge will be 1 or -1 randomly
	 * @param xmin minimum value of the x coordinate in meters
	 * @param xmax maximum value of the x coordinate in meters
	 * @param ymin minimum value of the y coordinate in meters
	 * @param ymax maximum value of the y coordinate in meters
	 * @param zmin minimum value of the z coordinate in meters
	 * @param zmax maximum value of the z coordinate in meters
	 * @param pmin minimum value of the momentum in GeV/c
	 * @param pmax maximum value of the momentum in GeV/c
	 * @param thetamin minimum value of the polar angle in degrees
	 * @param thetamax maximum value of the polar angle in degrees
	 * @param phimin minimum value of the azimuthal angle in degrees
	 * @param phimax maximum value of the azimuthal angle in degrees
	 * @return an array of initial values
	 */
	public static InitialValues[] getInitialValues(Random rand, int num, int charge, boolean randCharge, double xmin, double xmax,
			double ymin, double ymax, double zmin, double zmax, double pmin, double pmax, double thetamin,
			double thetamax, double phimin, double phimax) {

		InitialValues[] initVals = new InitialValues[num];

		for (int i = 0; i < num; i++) {
			initVals[i] = new InitialValues();
			randomInitVal(rand, initVals[i], charge, randCharge, xmin, xmax, ymin, ymax, zmin, zmax, pmin, pmax,
					thetamin, thetamax, phimin, phimax);
		}

		return initVals;
	}

	/**
	 * For setting up an array of initial values for testing. In cases where the difference between
	 * a min val and a max val is < SMALL, the min val is used and the variable is no
	 * randomized.
	 * @param rand and random number generator
	 * @param initVal the object to fill with random values
	 * @param charge the integer charge
	 * @param randCharge if <code>true</code> the charge will be 1 or -1 randomly
	 * @param xmin minimum value of the x coordinate in meters
	 * @param xmax maximum value of the x coordinate in meters
	 * @param ymin minimum value of the y coordinate in meters
	 * @param ymax maximum value of the y coordinate in meters
	 * @param zmin minimum value of the z coordinate in meters
	 * @param zmax maximum value of the z coordinate in meters
	 * @param pmin minimum value of the momentum in GeV/c
	 * @param pmax maximum value of the momentum in GeV/c
	 * @param thetamin minimum value of the polar angle in degrees
	 * @param thetamax maximum value of the polar angle in degrees
	 * @param phimin minimum value of the azimuthal angle in degrees
	 * @param phimax maximum value of the azimuthal angle in degrees
	 */
	public static void randomInitVal(Random rand, InitialValues initVal, int charge, boolean randCharge, double xmin,
			double xmax, double ymin, double ymax, double zmin, double zmax, double pmin, double pmax, double thetamin,
			double thetamax, double phimin, double phimax) {

		if (randCharge) {
			initVal.charge = (rand.nextBoolean() ? -1 : 1);
		} else {
			initVal.charge = charge;
		}

		initVal.xo = randVal(rand, xmin, xmax);
		initVal.yo = randVal(rand, ymin, ymax);
		initVal.zo = randVal(rand, zmin, zmax);
		initVal.p = randVal(rand, pmin, pmax);
		initVal.theta = randVal(rand, thetamin, thetamax);
		initVal.phi = randVal(rand, phimin, phimax);
	}

	//used to generate a random number in a range
	private static double randVal(Random rand, double vmin, double vmax) {
		double del = vmax - vmin;

		if (Math.abs(del) < SMALL) {
			return vmin;
		} else {
			return vmin + del * rand.nextDouble();
		}
	}

}
