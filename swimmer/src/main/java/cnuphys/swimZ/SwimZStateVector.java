package cnuphys.swimZ;

public class SwimZStateVector {

    // NOTE: not including q = Q/p as an element because it is a constant. It is
    // held in the
    // SwimZResult object

    /** the x coordinate (cm) */
    public final double x;

    /** the y coordinate (cm) */
    public final double y;

    /** the z coordinate (cm) */
    public final double z;

    /** the x track slope, px/pz */
    public final double tx;

    /** the y track slope, py/pz */
    public final double ty;

    /**
     * Constructor
     * 
     * @param x
     *            the x coordinate (cm)
     * @param y
     *            the x coordinate (cm)
     * @param z
     *            the z coordinate (cm). Note: z is not an actual component of
     *            the state vector, it is the independent variable. But it rides
     *            along here.
     * @param tx
     *            the x track slope, px/pz
     * @param ty
     *            the y track slope, py/pz
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
     * @param z
     *            the value of z
     * @param v
     *            the array with, in order, x,y,tx,ty,q
     */
    public SwimZStateVector(double z, double v[]) {
	this(v[0], v[1], z, v[2], v[3]);
    }

    /**
     * Constructor
     * 
     * @param x
     *            the x coordinate (cm)
     * @param y
     *            the y coordinate (cm)
     * @param z
     *            the z coordinate (cm)
     * @param p
     *            the magnitude of the momentum in GeV/c
     * @param theta
     *            the initial polar angle (degrees)
     * @param phi
     *            the initial azimuthal angle(degrees)
     */
    public SwimZStateVector(double x, double y, double z, double p,
	    double theta, double phi) {
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
     * Get a string representation
     * 
     * @return a string representation of the state vector
     */
    @Override
    public String toString() {
	StringBuffer sb = new StringBuffer(255);
	double r = Math.sqrt(x * x + y * y + z * z);

	sb.append(String.format("R = [%8.5f, %8.5f, %8.5f] |R| = %7.4f cm", x,
		y, z, r));

	return sb.toString();
    }

}
