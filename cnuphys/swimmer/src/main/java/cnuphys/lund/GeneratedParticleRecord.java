package cnuphys.lund;

/**
 * Holds the Lund ID, vertex, momentum, and the initial angles giving the
 * direction of the particle's momentum.
 * 
 * @author heddle
 *
 */
public class GeneratedParticleRecord {

    // the charge (-1 for electron, etc.)
    int _charge;

    // the vertex position in meters.
    private double _xo, _yo, _zo;

    // intial momentum in GeV/c
    private double _momentum;

    // the initial polar angle in degrees
    private double _theta;

    // the initial azimuthal angle in degrees
    private double _phi;

    /**
     * @param charge
     *            the charge: -1 for electron, 1 for proton, etc
     * @param xo
     *            the x vertex position in m
     * @param yo
     *            the y vertex position in m
     * @param zo
     *            the z vertex position in m
     * @param momentum
     *            initial momentum in GeV/c
     * @param theta
     *            initial polar angle in degrees
     * @param phi
     *            initial azimuthal angle in degrees
     */
    public GeneratedParticleRecord(int charge, double xo, double yo, double zo,
	    double momentum, double theta, double phi) {
	_charge = charge;
	_xo = xo;
	_yo = yo;
	_zo = zo;
	_momentum = momentum;
	_theta = theta;
	_phi = phi;
    }

    /**
     * Get the the X vertex position in m
     * 
     * @return the X vertex position in m
     */
    public double getVertexX() {
	return _xo;
    }

    /**
     * Get the the Y vertex position in m
     * 
     * @return the Y vertex position in m
     */
    public double getVertexY() {
	return _yo;
    }

    /**
     * Get the the Z vertex position in m
     * 
     * @return the Z vertex position in m
     */
    public double getVertexZ() {
	return _zo;
    }

    /**
     * Get the momentum in GeV/c
     * 
     * @return the momentum in GeV/c
     */
    public double getMomentum() {
	return _momentum;
    }

    /**
     * Get the initial polar angle in degrees.
     * 
     * @return the initial polar angle in degrees.
     */
    public double getTheta() {
	return _theta;
    }

    /**
     * Get the initial azimuthal angle in degrees.
     * 
     * @return the initial azimuthal angle in degrees.
     */
    public double getPhi() {
	return _phi;
    }

    /**
     * The total energy in GeV. This requires that we specify what particle this
     * is.
     * 
     * @param lundid
     *            the lundid of the particle
     * @return the total energy in GeV
     */
    public double getTotalEnergy(LundId lundId) {
	double m = lundId.getMass();
	return Math.sqrt(_momentum * _momentum + m * m);
    }

    /**
     * Get the kinetic energy in GeV. This requires that we specify what
     * particle this is.
     * 
     * @param lundid
     *            the lundid of the particle
     * 
     * @return the kinetic energy in GeV
     */
    public double getKineticEnergy(LundId lundId) {
	return getTotalEnergy(lundId) - lundId.getMass();
    }

}