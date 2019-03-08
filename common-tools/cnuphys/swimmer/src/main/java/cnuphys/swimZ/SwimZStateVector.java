package cnuphys.swimZ;

/**
 * Holds the position and track slopes. The positions (x, y, z) are in cm, while
 * the track slopes are dimensionless. Note: z is not an actual component of the
 * "true" state vector, it is the independent variable. But it rides along here
 * because we will want to know z at every step.
 * 
 * @author heddle
 *
 */
public class SwimZStateVector {

	// NOTE: not including q = Q/p as an element because it is a constant. It is
	// held in the SwimZResult object

	/** the x coordinate (cm) */
	public double x;

	/** the y coordinate (cm) */
	public double y;

	/** the z coordinate (cm) */
	public double z;

	/** the x track slope, px/pz */
	public double tx;

	/** the y track slope, py/pz */
	public double ty;

	/**
	 * Create a state vector for the SwimZ package with all NaNs for the components
	 */
	public SwimZStateVector() {
		this(Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
	}

	/**
	 * Constructor for a state vector for the SwimZ package. Note that it uses CM
	 * for distance units
	 * 
	 * @param x  the x coordinate (cm)
	 * @param y  the x coordinate (cm)
	 * @param z  the z coordinate (cm). Note: z is not an actual component of the
	 *           state vector, it is the independent variable. But it rides along
	 *           here.
	 * @param tx the x track slope, px/pz
	 * @param ty the y track slope, py/pz
	 */
	public SwimZStateVector(double x, double y, double z, double tx, double ty) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.tx = tx;
		this.ty = ty;
	}

	/**
	 * Create a state variable from an array (probably from RK integration)
	 * 
	 * @param z the value of z in cm. Note: z is not an actual component of the
	 *          state vector, it is the independent variable. But it rides along
	 *          here.
	 * @param v the array with, in order, x,y,tx,ty,q
	 */
	public SwimZStateVector(double z, double v[]) {
		this(v[0], v[1], z, v[2], v[3]);
	}

	/**
	 * Create a state variable from another (copy)
	 * 
	 * @param sv the state vector to copy
	 * @param v  the array with, in order, x,y,tx,ty,q
	 */
	public SwimZStateVector(SwimZStateVector sv) {
		this(sv.x, sv.y, sv.z, sv.tx, sv.ty);
	}

	/**
	 * Constructor
	 * 
	 * @param x     the x coordinate (cm)
	 * @param y     the y coordinate (cm)
	 * @param z     the z coordinate (cm)
	 * @param p     the magnitude of the momentum in GeV/c
	 * @param theta the initial polar angle (degrees)
	 * @param phi   the initial azimuthal angle(degrees)
	 */
	public SwimZStateVector(double x, double y, double z, double p, double theta, double phi) {
		this.x = x;
		this.y = y;
		this.z = z;
		theta = Math.toRadians(theta);
		phi = Math.toRadians(phi);
		double pz = p * Math.cos(theta);
		double pt = p * Math.sin(theta);
		double px = pt * Math.cos(phi);
		double py = pt * Math.sin(phi);
		tx = px / pz;
		ty = py / pz;
	}

	/**
	 * Copy from another state vector
	 * 
	 * @param sv the state vector to copy
	 */
	public void copy(SwimZStateVector sv) {
		x = sv.x;
		y = sv.y;
		z = sv.z;
		tx = sv.tx;
		ty = sv.ty;
	}

	/**
	 * Set the state vector
	 * 
	 * @param z the value of z in cm. Note: z is not an actual component of the
	 *          state vector, it is the independent variable. But it rides along
	 *          here.
	 * @param v the array with, in order, x,y,tx,ty,
	 */
	public void set(double z, double v[]) {
		this.z = z;
		x = v[0];
		y = v[1];
		tx = v[0];
		ty = v[1];
	}

	/**
	 * Compute the difference between this state vector's location and another state
	 * vector's location
	 * 
	 * @param zv the other state vector
	 * @param dr will hold the delta in cm
	 */
	public void dR(SwimZStateVector zv, double dr[]) {
		dr[0] = zv.x - x;
		dr[1] = zv.y - y;
		dr[2] = zv.z - z;
	}

	/**
	 * Get a string representation
	 * 
	 * @return a string representation of the state vector
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(255);
		double r = Math.sqrt(x * x + y * y + z * z);

		sb.append(String.format("R = [%8.5f, %8.5f, %8.5f] |R| = %7.4f cm", x, y, z, r));

		return sb.toString();
	}

	public String normalPrint(double p, int pzSign) {
		StringBuffer sb = new StringBuffer(255);
		sb.append(String.format("R = [%9.6f, %9.6f, %9.6f] cm", x, y, z));

//		int _pzSign = (zf < zo) ? -1 : 1;

		double txsq = tx * tx;
		double tysq = ty * ty;
		double pz = pzSign * p / Math.sqrt(txsq + tysq + 1);
		double px = pz * tx;
		double py = pz * ty;

		double theta = Math.toDegrees(Math.acos(pz / p));
		double phi = Math.toDegrees(Math.atan2(py, px));
		sb.append(String.format("\nP, theta, phi = [%9.6f, %9.6f, %9.6f] ", p, theta, phi));

		return sb.toString();
	}

}
