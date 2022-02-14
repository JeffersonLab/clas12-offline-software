package cnuphys.lund;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * Holds the Lund ID, vertex, momentum, and the initial angles giving the
 * direction of the particle's momentum.
 * 
 * @author heddle
 *
 */
public class GeneratedParticleRecord implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2460631848401695481L;
	// for use in hask keys
	private static final String HASH_DELIM = "$";
	private static final int HASHRADIX = 36;
	private static final double HASHFACT = 100.;


	// the charge (-1 for electron, etc.)
	private int _charge;

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
	public GeneratedParticleRecord(int charge, double xo, double yo, double zo, double momentum, double theta,
			double phi) {
		_charge = charge;
		_xo = xo;
		_yo = yo;
		_zo = zo;
		_momentum = momentum;
		_theta = theta;
		_phi = phi;
	}
	
	/**
	 * Get the integer charge
	 * @return the integer charge
	 */
	public int getCharge() {
		return _charge;
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

	@Override
	public String toString() {
		return String.format(
				"Q: %d Vertex: (%-8.5f, %-8.5f, %-8.5f) m  P: %-8.5f GeV/c Theta: %-8.5f deg  Phi: %-8.5f deg", _charge,
				_xo, _yo, _zo, _momentum, _theta, _phi);
	}
	
	/**
	 * Records a reduced precision String version as a hash key
	 * @param rpr the GeneratedParticleRecord
	 * @return a reduced precision String version
	 */
	public String hashKey() {
		return hashKey(this);
	}
	
	/**
	 * Records a reduced precision String version as a hash key
	 * @param rpr the GeneratedParticleRecord
	 * @return a reduced precision String version
	 */
	public static String hashKey(GeneratedParticleRecord rpr) {
		
		StringBuilder sb = new StringBuilder(128);
		sb.append(rpr._charge);
		sb.append(HASH_DELIM);
		sb.append(valStr(rpr._xo));
		sb.append(HASH_DELIM);
		sb.append(valStr(rpr._yo));
		sb.append(HASH_DELIM);
		sb.append(valStr(rpr._zo));
		sb.append(HASH_DELIM);
		sb.append(valStr(rpr._momentum));
		sb.append(HASH_DELIM);
		sb.append(valStr(rpr._theta));
		sb.append(HASH_DELIM);
		sb.append(valStr(rpr._phi));
		
		return sb.toString();
	}
	
	private static final double TINY = 1.0e-4;
	public static String valStr(double f) {
		if (Math.abs(f) < TINY) {
			return "0";
		}
		else {
			long s = Math.round(HASHFACT*f);
			String hs = Long.toString(s, HASHRADIX);
			return hs;
		}
	}
		
	public static GeneratedParticleRecord fromHash(String hash) {
		StringTokenizer t = new StringTokenizer(hash, HASH_DELIM);
		int charge = Integer.parseInt(t.nextToken());
		double xo = ((double)(Long.valueOf(t.nextToken(), HASHRADIX)))/HASHFACT;
		double yo = ((double)(Long.valueOf(t.nextToken(), HASHRADIX)))/HASHFACT;
		double zo = ((double)(Long.valueOf(t.nextToken(), HASHRADIX)))/HASHFACT;
		double p = ((double)(Long.valueOf(t.nextToken(), HASHRADIX)))/HASHFACT;
		double theta = ((double)(Long.valueOf(t.nextToken(), HASHRADIX)))/HASHFACT;
		
		String pstr = t.nextToken();
		double phi = ((double)(Long.valueOf(pstr, HASHRADIX)))/HASHFACT;
		
		return new GeneratedParticleRecord(charge, xo, yo, zo, p, theta, phi);
	}
	
	public static void main(String[] arg) {
		GeneratedParticleRecord gpr = new GeneratedParticleRecord(-1, 1.0, 2.0, 3.00001, 5.6789, -45.6789, 359.99);
		
		String hash = gpr.hashKey();
		System.err.println("HASH [" + hash + "]");
		
		GeneratedParticleRecord gprp = fromHash(hash);
		
		System.err.println("HASH [" + gprp.hashKey() + "]");
		System.err.println("done");
	}

}