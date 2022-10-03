package cnuphys.adaptiveSwim.swimZ;

import cnuphys.adaptiveSwim.AdaptiveSwimException;
import cnuphys.adaptiveSwim.AdaptiveSwimmer;
import cnuphys.adaptiveSwim.ButcherAdvance;
import cnuphys.lund.GeneratedParticleRecord;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.MagneticField;
import cnuphys.rk4.ButcherTableau;

/**
 * This class holds the parameters and static methods for the swimZ
 * integration. The swimZ integration follows the method described for the
 * HERA-B magnet here: http://arxiv.org/pdf/physics/0511177v1.pdf<br>
 * <p>
 * In this swim, z is the independent variable. It is only good for swims
 * where z is monotonic. It can handle z increasing or z decreasing (backwars
 * swim) but not where z changes direction.
 * <p>
 * The state vector has five elements: <br>
 * (x, y, tx, ty, q) <br>
 * Where x and y are the transverse coordinates, tx = px/pz, ty = py/pz,
 * and q = Q/|p| where Q is the integer charge (e.g. -1 for an electron)
 * * <p>
 * Note q is constant, so this is only a 4D problem, unlike the regular swimmer which is 6D.
 * That's why we go through all the trouble.
 * <p>
 * UNITS  (NOT THE SAME AS THE REGULAR 6D SWIMMER!!!!)
 * <ul>
 * <li>x, y, and z are in cm
 * <li>p is in GeV/c
 * <li>B (mag field) is in kGauss
 * </ul>
 * <p>
 * 
 * @author heddle
 *
 */
public class SwimZ {
	
	/** The speed of light in these units: (GeV/c)(1/kG)(1/cm) */
	public static final double C = 2.99792458e-04;
	
	//small number test
	private static final double TINY = 1e-12;

	//the Cash-Karp advancer
	private static ButcherAdvance _cashKarpAdvancer;
	
	// The magnetic field probe.
	// NOTE: the method of interest in FieldProbe takes a position in cm
	// and returns a field in kG.
	private FieldProbe _probe;
	
	
	/**
	 * Create a swimmer using the current active field
	 */
	public SwimZ() {
		// make a probe using the current active field
		_probe = FieldProbe.factory();
	}

	/**
	 * Create a swimmer specific to a magnetic field
	 *
	 * @param magneticField the magnetic field
	 */
	public SwimZ(MagneticField magneticField) {
		_probe = FieldProbe.factory(magneticField);
	}


	/**
	 * The basic swim used by the swim Z swimmer
	 * @param charge   in integer units of e
	 * @param xo       the x vertex position in cm
	 * @param yo       the y vertex position in cm
	 * @param zo       the z vertex position in cm
	 * @param p        the momentum in GeV/c
	 * @param theta    the initial polar angle in degrees
	 * @param phi      the initial azimuthal angle in degrees
	 * @param zf       the final (max) value of the independent variable z in cm
	 * @param accuracy the requested accuracy for the target z in cm
	 * @param h        the initial stepsize in cm
	 * @param eps      the overall fractional tolerance (e.g., 1.0e-5)
	 * @param result   the container for some of the swimming results
	 * @throws AdaptiveSwimException
	 */
	private void swimZ(int charge, double xo, double yo, double zo, double p, double theta, double phi, double zf,
			double accuracy, double h, double eps, SwimZResult result) throws AdaptiveSwimException {

		//initialization; the initial state vector will  be placed in the result object by init
		init(charge, xo, yo, zo, p, theta, phi, zf, new SwimZStopper(zf, accuracy, result));
		
		//we may already have failed if pz is in wrong direction
		if (result.getStatus() == AdaptiveSwimmer.SWIM_TARGET_MISSED)  {
			return;
		}

		//neutral? Just return a line
		if (charge == 0) {
			straightLine(xo, yo, zo, p, theta, phi, zf, result);
			return;
		}

		//tiny momentum? No point to swim
		if (p < AdaptiveSwimmer.MINMOMENTUM) {
			result.setStatus(AdaptiveSwimmer.SWIM_BELOW_MIN_P);
			return;
		}

		// create the derivative object
		SwimZDerivative deriv = new SwimZDerivative(charge, p, _probe);


		//no promise to be thread safe so can create just one
		if (_cashKarpAdvancer == null) {
			_cashKarpAdvancer = new ButcherAdvance(ButcherTableau.CASH_KARP);
		}

		// swim
		int count = 0;
		boolean done = false;
		while (!done) {

//			int ns = AdaptiveSwimUtilities.driver(h, deriv, stopper, _cashKarpAdvancer, eps);
//			result.setNStep(result.getNStep() + ns);
//
//			switch (result.getStatus()) {
//
//			case AdaptiveSwimmer.SWIM_SUCCESS:
//				done = true;
//				break;
//
//			case AdaptiveSwimmer.SWIM_CROSSED_BOUNDARY:
//				h = stopper.getNewStepSize(h);
//				//go back to swimming
//				result.setStatus(AdaptiveSwimmer.SWIM_SWIMMING);
//				break;
//
//			case AdaptiveSwimmer.SWIM_TARGET_MISSED:
//				done = true;
//				break;
//
//			case AdaptiveSwimmer.SWIM_SWIMMING:
//				done = true;
//				System.err.println("baseSwimZ should not encounter a status of SWIM_SWIMMING");
//				break;
//
//			case AdaptiveSwimmer.SWIM_BELOW_MIN_P:
//				done = true;
//				System.err.println("baseSwimZ should not encounter a status of SWIM_BELOW_MIN_P");
//				break;
//
//			case AdaptiveSwimmer.SWIM_EXCEED_MAX_TRIES:
//				done = true;
//				System.err.println("baseSwimZ should not encounter a status of SWIM_EXCEED_MAX_TRIES");
//				break;
//
//			case AdaptiveSwimmer.SWIM_NEUTRAL_PARTICLE:
//				done = true;
//				System.err.println("baseSwimZ should not encounter a status of SWIM_NEUTRAL_PARTICLE");
//				break;
//
//			default:
//				done = true;
//				System.err.println("Unknown status encountered in baseSwimZ: " + result.getStatus());
//				break;
//
//			} // end case
//
//			if (++count > MAXTRIES) {
//				result.setStatus(SWIM_EXCEED_MAX_TRIES);
//				throw new AdaptiveSwimException("Exceeded MAXTRIES in baseSwim");
//			}

		}

	}
	
	/**
	 * 
	 * @param charge
	 * @param xo
	 * @param yo
	 * @param zo
	 * @param p
	 * @param theta
	 * @param phi
	 * @param zf
	 * @param stopper
	 */
	protected void init(int charge, double xo, double yo, double zo, double p,
			double theta, double phi, double zf, SwimZStopper stopper) {

		
		
//		SwimZResult result = stopper.getResult();
//		// clear old trajectory
//		if (result.getTrajectory() != null) {
//			result.getTrajectory().clear();
//
//			result.getTrajectory()
//					.setGeneratedParticleRecord(new GeneratedParticleRecord(charge, xo, yo, zo, momentum, theta, phi));
//		}
//
//		// store initial values
//		result.setInitialValues(charge, xo, yo, zo, momentum, theta, phi);
//
//		stuffU(result.getU(), xo, yo, zo, momentum, theta, phi);
//
//		
//		
//		
//		
//		super.init(charge, xo, yo, zo, p, theta, phi, stopper);
//		SwimZResult result = (SwimZResult) stopper.getResult();
//
//		result.setZf(zf);
//		result.setSign(sign(zo, zf));
//		
//		//quick sign check that pz can get us from zo to zf
//		//given that pz can't change signs for SwimZ swimming
//		
//		double pz = p * Math.cos(Math.toDegrees(theta));
//		int signpz = ((pz < 0) ? 1 : -1);
//		
//		if (result.getSign() != signpz) {
//			result.setStatus(AdaptiveSwimmer.SWIM_TARGET_MISSED);
//		}

	}
	
	/**
	 * Get the z sign
	 * @param zo the initial z
	 * @param zf the final z
	 * @return 1 if zf < zo, else -1
	 */
	private int sign(double zo, double zf) {
		return (zf < zo) ? 1 : -1;
	}
	
	
	
	// create a straight line for neutral particles
	//the starting point will already be set as the result current statevector
	protected void straightLine(double xo, double yo, double zo, double momentum, double theta,
			double phi, double zf, SwimZResult result) {
		
		//if  here it passed the sign test
		// probably unnecessary check for vertical track
		if (Math.abs(theta-90) < TINY) {
			result.setStatus(AdaptiveSwimmer.SWIM_TARGET_MISSED);
			return;
		}
		
//		//pathlength
//		double s = Math.abs(zf - zo)/Math.cos(Math.toDegrees(theta));
//		super.straightLine(xo, yo, zo, momentum, theta, phi, s, result);
//		result.setStatus(SWIM_NEUTRAL_PARTICLE);
	}


}
