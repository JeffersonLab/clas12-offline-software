package cnuphys.adaptiveSwim.swimZ;

import cnuphys.adaptiveSwim.InitialValues;

public class SwimZInitialValues {

	
	/** The integer charge */
	public int charge;
	
	/** The coordinate x of the vertex in cm */
	public double xo;

	/** The y coordinate of the vertex in cm */
    public double yo;

	/** The z coordinate of the vertex in cm */
    public double zo;
    
    /** The momentum in GeV/c */
	public double p;
	
	/** The polar angle in degrees */
	public double theta;
	
	/** The azimuthal angle in degrees */
	public double phi;
	
	/** The inital 4D state vector (x, y, tx, ty) */
	public double[] uo = new double[4];
	
	
	public String toStringRaw() {
		return String.format("%-7.4f  %-7.4f  %-7.4f %-6.3f %-6.3f %-6.3f", xo, yo, zo, p, theta, phi);
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
	public SwimZInitialValues(int charge, double xo, double yo, double zo, double p, double theta, double phi) {
		this.charge = charge;
		this.xo = xo;
		this.yo = yo;
		this.zo = zo;
		this.p = p;
		this.theta = theta;
		this.phi = phi;
		SwimZUtil.swimZToSwim(xo, yo, p, theta, phi, uo);;
	}
	

	
}
