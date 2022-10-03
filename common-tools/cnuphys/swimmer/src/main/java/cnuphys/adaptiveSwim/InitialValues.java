package cnuphys.adaptiveSwim;

/**
 * Hold the initial values of a swim
 * 
 * @author heddle
 *
 */
public class InitialValues {

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

	public String toStringRaw() {
		return String.format("%-7.4f  %-7.4f  %-7.4f %-6.3f %-6.3f %-6.3f", xo, yo, zo, p, theta, phi);
	}

	/**
	 * Store the initial conditions of a swim
	 * 
	 * @param charge The integer charge
	 * @param xo     The x coordinate of the vertex in meters
	 * @param yo     The y coordinate of the vertex in meters
	 * @param zo     The z coordinate of the vertex in meters
	 * @param p      The momentum in GeV/c
	 * @param theta  The polar angle in degrees
	 * @param phi    The azimuthal angle in degrees
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
	 * 
	 * @param src the source initial values
	 */
	public InitialValues(InitialValues src) {
		this(src.charge, src.xo, src.yo, src.zo, src.p, src.theta, src.phi);
	}

	@Override
	public String toString() {
		return String.format("Q: %d\n", charge) + String.format("xo: %10.7e m\n", xo)
				+ String.format("yo: %10.7e m\n", yo) + String.format("zo: %10.7e m\n", zo)
				+ String.format("p: %10.7e GeV/c\n", p) + String.format("theta: %10.7f deg\n", theta)
				+ String.format("phi: %10.7f deg", phi);
	}

}
