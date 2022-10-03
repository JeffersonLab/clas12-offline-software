package cnuphys.adaptiveSwim;

import java.util.Hashtable;

import cnuphys.adaptiveSwim.geometry.Cylinder;
import cnuphys.adaptiveSwim.geometry.Plane;
import cnuphys.adaptiveSwim.geometry.Point;
import cnuphys.adaptiveSwim.geometry.Sphere;
import cnuphys.lund.GeneratedParticleRecord;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.IMagField;
import cnuphys.magfield.MagneticField;
import cnuphys.magfield.RotatedCompositeProbe;
import cnuphys.rk4.ButcherTableau;
import cnuphys.swim.SwimTrajectory;

/**
 * A swimmer for adaptive stepsize integrators. These swimmers are not thread
 * safe. Every thread that needs an AdaptiveSwimmer should create its own.
 *
 * @author heddle
 *
 */
public class AdaptiveSwimmer {

	//dimensionality of our swimming
	public static final int DIM = 6;


	/** currently swimming */
	public static final int SWIM_SWIMMING = 88;


	/** The swim was a success */
	public static final int SWIM_SUCCESS = 0;
	
	// Speed of light in m/s
	public static final double C = 299792458.0; // m/s

	/**
	 * A target, such as a target rho or z, was not reached before the swim was
	 * stopped for some other reason
	 */
	public static final int SWIM_TARGET_MISSED = -1;

	/** A swim was requested for a particle with extremely low momentum */
	public static final int SWIM_BELOW_MIN_P = -2;

	/** A swim was requested exceeded the max number of tries */
	public static final int SWIM_EXCEED_MAX_TRIES = -3;

	/** A swim crossed a boundary, need to back up and reduce h */
	public static final int SWIM_CROSSED_BOUNDARY = -4;

	/** A swim was requested for a neutral particle */
	public static final int SWIM_NEUTRAL_PARTICLE = 10;
	
	public static final Hashtable<Integer, String> resultNames = new Hashtable<Integer, String>();
	static {
		resultNames.put(SWIM_SWIMMING, "SWIMMING");
		resultNames.put(SWIM_SUCCESS, "SWIM_SUCCESS");
		resultNames.put(SWIM_TARGET_MISSED, "SWIM_TARGET_MISSED");
		resultNames.put(SWIM_BELOW_MIN_P, "SWIM_BELOW_MIN_P");
		resultNames.put(SWIM_EXCEED_MAX_TRIES, "SWIM_EXCEED_MAX_TRIES");
		resultNames.put(SWIM_NEUTRAL_PARTICLE, "SWIM_NEUTRAL_PARTICLE");
	}


	//the Cash-Karp advancer
	private static ButcherAdvance _cashKarpAdvancer;


	// Min momentum to swim in GeV/c
	public static final double MINMOMENTUM = 5e-05;

	// max tries to reset if swam passed a target
	private static final int MAXTRIES = 60;

	// The magnetic field probe.
	// NOTE: the method of interest in FieldProbe takes a position in cm
	// and returns a field in kG.This swim package works in SI (meters and
	// Tesla.) So care has to be taken when using the field object

	private FieldProbe _probe;

	/**
	 * Create a swimmer using the current active field
	 */
	public AdaptiveSwimmer() {
		// make a probe using the current active field
		_probe = FieldProbe.factory();
	}

	/**
	 * Create a swimmer specific to a magnetic field
	 *
	 * @param magneticField the magnetic field
	 */
	public AdaptiveSwimmer(MagneticField magneticField) {
		_probe = FieldProbe.factory(magneticField);
	}

	/**
	 * Create a swimmer specific to a magnetic field
	 *
	 * @param magneticField the magnetic field
	 */
	public AdaptiveSwimmer(IMagField magneticField) {
		_probe = FieldProbe.factory(magneticField);
	}

	/**
	 * Get the probe being used to swim
	 *
	 * @return the probe
	 */
	public FieldProbe getProbe() {
		return _probe;
	}


	/**
	 * The basic swim used by the specific swimmers
	 * @param charge   in integer units of e
	 * @param xo       the x vertex position in meters
	 * @param yo       the y vertex position in meters
	 * @param zo       the z vertex position in meters
	 * @param momentum the momentum in GeV/c
	 * @param theta    the initial polar angle in degrees
	 * @param phi      the initial azimuthal angle in degrees
	 * @param sMax       the final (max) value of the independent variable
	 *                 (pathlength) unless the integration is terminated by the
	 *                 stopper
	 * @param h        the initial stepsize in meters
	 * @param eps      the overall fractional tolerance (e.g., 1.0e-5)
	 * @param result   the container for some of the swimming results
	 * @param stopper the object that knows when to stop the swimming
	 * @throws AdaptiveSwimException
	 */
	private void baseSwim(int charge, double xo, double yo, double zo, double momentum,
			double theta, double phi, double sMax, double h, double eps, AdaptiveSwimResult result,
			IAdaptiveStopper stopper) throws AdaptiveSwimException {

		//initialization; the initial state vector will be placed in the result object by init
		init(charge, xo, yo, zo, momentum, theta, phi, stopper);

		//neutral? Just return a line
		if (charge == 0) {
			result.setStatus(SWIM_NEUTRAL_PARTICLE);
			straightLine(xo, yo, zo, momentum, theta, phi, sMax, result);
			return;
		}

		//tiny momentum? No point to swim
		if (momentum < MINMOMENTUM) {
			result.setStatus(SWIM_BELOW_MIN_P);
			return;
		}

		// create the derivative object
		Derivative deriv = new Derivative(charge, momentum, _probe);


		//no promise to be thread safe so can create just one
		if (_cashKarpAdvancer == null) {
			_cashKarpAdvancer = new ButcherAdvance(ButcherTableau.CASH_KARP);
		}

		// swim
		int count = 0;
		boolean done = false;
		while (!done) {

			int ns = AdaptiveSwimUtilities.driver(h, deriv, stopper, _cashKarpAdvancer, eps);
			result.setNStep(result.getNStep() + ns);

			switch (result.getStatus()) {

			case SWIM_SUCCESS:
				done = true;
				break;

			case SWIM_CROSSED_BOUNDARY:
				h = stopper.getNewStepSize(h);
				//go back to swimming
				result.setStatus(SWIM_SWIMMING);
				break;

			case SWIM_TARGET_MISSED:
				done = true;
				break;

			case SWIM_SWIMMING:
				done = true;
				System.err.println("baseSwim should not encounter a status of SWIM_SWIMMING");
				break;

			case SWIM_BELOW_MIN_P:
				done = true;
				System.err.println("baseSwim should not encounter a status of SWIM_BELOW_MIN_P");
				break;

			case SWIM_EXCEED_MAX_TRIES:
				done = true;
				System.err.println("baseSwim should not encounter a status of SWIM_EXCEED_MAX_TRIES");
				break;

			case SWIM_NEUTRAL_PARTICLE:
				done = true;
				System.err.println("baseSwim should not encounter a status of SWIM_NEUTRAL_PARTICLE");
				break;

			default:
				done = true;
				System.err.println("Unknown status encountered in baseSwim: " + result.getStatus());
				break;

			} // end case

			if (++count > MAXTRIES) {
				result.setStatus(SWIM_EXCEED_MAX_TRIES);
				throw new AdaptiveSwimException("Exceeded MAXTRIES in baseSwim");
			}

		}

	}

	/**
	 * Swim using the current active field. This swimmer has no target. It will stop
	 * when the pathlength exceeds the maximum. For a more precise pathlength swim,
	 * use swimS.
	 *
	 * @param charge   in integer units of e
	 * @param xo       the x vertex position in meters
	 * @param yo       the y vertex position in meters
	 * @param zo       the z vertex position in meters
	 * @param momentum the momentum in GeV/c
	 * @param theta    the initial polar angle in degrees
	 * @param phi      the initial azimuthal angle in degrees
	 * @param sMax       the final (max) value of the independent variable
	 *                 (pathlength) unless the integration is terminated by the
	 *                 stopper
	 * @param h        the initial stepsize in meters
	 * @param eps      the overall fractional tolerance (e.g., 1.0e-5)
	 * @param result   the container for some of the swimming results
	 * @throws AdaptiveSwimException
	 */
	public void swim(int charge, double xo, double yo, double zo, double momentum,
			double theta, double phi, double sMax, double h, double eps, AdaptiveSwimResult result)
			throws AdaptiveSwimException {

			baseSwim(charge, xo, yo, zo, momentum, theta, phi, sMax, h, eps, result,
					new AdaptiveDefaultStopper(sMax, result));
	}


	/**
	 * Swim to a fixed z using the current active field
	 *
	 * @param charge   in integer units of e
	 * @param xo       the x vertex position in meters
	 * @param yo       the y vertex position in meters
	 * @param zo       the z vertex position in meters
	 * @param momentum the momentum in GeV/c
	 * @param theta    the initial polar angle in degrees
	 * @param phi      the initial azimuthal angle in degrees
	 * @param targetZ  the target z in meters
	 * @param accuracy the requested accuracy for the target z in meters
	 * @param sMax       the final (max) value of the independent variable
	 *                 (pathlength) unless the integration is terminated by the
	 *                 stopper
	 * @param h        the initial stepsize in meters
	 * @param eps      the overall fractional tolerance (e.g., 1.0e-5)
	 * @param result   the container for some of the swimming results
	 * @throws AdaptiveSwimException
	 */
	public void swimZ(int charge, double xo, double yo, double zo, double momentum,
			double theta, double phi, double targetZ, double accuracy, double sMax, double h,
			double eps, AdaptiveSwimResult result) throws AdaptiveSwimException {


		baseSwim(charge, xo, yo, zo, momentum, theta, phi, sMax, h, eps, result,
				new AdaptiveZStopper(sMax, targetZ, accuracy, result));
		

	}

	/**
	 * Swim to a fixed rho using the current active field
	 *
	 * @param charge    in integer units of e
	 * @param xo        the x vertex position in meters
	 * @param yo        the y vertex position in meters
	 * @param zo        the z vertex position in meters
	 * @param momentum  the momentum in GeV/c
	 * @param theta     the initial polar angle in degrees
	 * @param phi       the initial azimuthal angle in degrees
	 * @param targetRho the target rho in meters
	 * @param accuracy  the requested accuracy for the target rho in meters
	 * @param sf        the final (max) value of the independent variable
	 *                  (pathlength) unless the integration is terminated by the
	 *                  stopper
	 * @param h         the initial stepsize in meters
	 * @param eps       the overall fractional tolerance (e.g., 1.0e-5)
	 * @param result    the container for some of the swimming results
	 * @throws AdaptiveSwimException
	 */
	public void swimRho(int charge, double xo, double yo, double zo, double momentum,
			double theta, double phi, double targetRho, double accuracy, double sf, double h,
			double eps, AdaptiveSwimResult result) throws AdaptiveSwimException {

		baseSwim(charge, xo, yo, zo, momentum, theta, phi, sf, h, eps, result,
				new AdaptiveRhoStopper(sf, targetRho, accuracy, result));

	}

	/**
	 * Swim to a plane using the current active field
	 *
	 * @param charge      in integer units of e
	 * @param xo          the x vertex position in meters
	 * @param yo          the y vertex position in meters
	 * @param zo          the z vertex position in meters
	 * @param momentum    the momentum in GeV/c
	 * @param theta       the initial polar angle in degrees
	 * @param phi         the initial azimuthal angle in degrees
	 * @param targetPlane the target plane
	 * @param accuracy    the requested accuracy for the final distance to the plane
	 *                    in meters
	 * @param sMax          the final (max) value of the independent variable
	 *                    (pathlength) unless the integration is terminated by the
	 *                    stopper
	 * @param h           the initial stepsize in meters
	 * @param eps         the overall fractional tolerance (e.g., 1.0e-5)
	 * @param result      the container for some of the swimming results
	 * @throws AdaptiveSwimException
	 */
	public void swimPlane(int charge, double xo, double yo, double zo, double momentum,
			double theta, double phi, Plane targetPlane, double accuracy, double sMax, double h,
			double eps, AdaptiveSwimResult result) throws AdaptiveSwimException {

		baseSwim(charge, xo, yo, zo, momentum, theta, phi, sMax, h, eps, result,
				new AdaptivePlaneStopper(sMax, targetPlane, accuracy, result));

		}

	/**
	 * A private swimmer that stops when the stopper evaluates a function that changes sign or
	 * when the pathlength max is exceeded
	 * @param u0 the initial state vector
	 * @param s0 the initial pathlength, in case this is a continuation swim
	 * @param sf the final or maximum path length
	 * @param h0 the initial step size
	 * @param result the swim result
	 * @throws AdaptiveSwimException
	 */
	private void swimSignChange(double[] u0, double so, double sf, double h0,
			double eps, final Derivative deriv, AAdaptiveStopper stopper, AdaptiveSwimResult result) throws AdaptiveSwimException {


		ButcherAdvance advancer = new ButcherAdvance(ButcherTableau.CASH_KARP);

		try {
			AdaptiveSwimUtilities.driver(h0, deriv, stopper, advancer, eps);
		} catch (AdaptiveSwimException e) {
			// put in a message that allows us to reproduce the track
		}

		double currentS = stopper.getS();
		if (currentS > stopper.getSmax()) {
			result.setStatus(SWIM_TARGET_MISSED);
			return;
		}

		result.setStatus(SWIM_SUCCESS);

	}

	/**
	 * Swim to a plane using the current active field. In this case, interpolate the
	 * last two points (one on each side) so that the final point is right on the
	 * plane.
	 *
	 * @param charge      in integer units of e
	 * @param xo          the x vertex position in meters
	 * @param yo          the y vertex position in meters
	 * @param zo          the z vertex position in meters
	 * @param momentum    the momentum in GeV/c
	 * @param theta       the initial polar angle in degrees
	 * @param phi         the initial azimuthal angle in degrees
	 * @param targetPlane the target plane
	 * @param accuracy    the requested accuracy for the final distance to the plane
	 *                    in meters
	 * @param sf          the final (max) value of the independent variable
	 *                    (pathlength) unless the integration is terminated by the
	 *                    stopper
	 * @param h0          the initial stepsize in meters
	 * @param eps         the overall fractional tolerance (e.g., 1.0e-5)
	 * @param result      the container for some of the swimming results
	 * @throws AdaptiveSwimException
	 */
	public void swimPlaneInterp(int charge, double xo, double yo, double zo,
			double momentum, double theta, double phi, Plane targetPlane, double accuracy, double sf,
			double h0, double eps, AdaptiveSwimResult result) throws AdaptiveSwimException {

		AdaptiveSwimIntersection intersection = result.getIntersection();
		intersection.reset();

		//basic algorithm:
		// 1) Swim to the plane from the start side, using a stopper that rejects crossing
		// 2) Swim across the plane, using "delta" to the plane as a step size
		// 3) Interpolate the two end points

		//Step 1
		swimPlane(charge, xo, yo, zo, momentum, theta, phi, targetPlane, accuracy, sf, h0, eps, result);
		
		if (result.getStatus() != SWIM_SUCCESS) {
			return;
		}
		
		double[] u = result.getU();
		double s = result.getS();
		double signedDist = targetPlane.signedDistance(u);

		intersection.checkSetLeft(u, s, Math.abs(signedDist));

		double del = Math.abs(signedDist); //should be less than accuracy

		//Step 2
		Derivative deriv = new Derivative(charge, momentum, _probe);
		result.setStatus(SWIM_SWIMMING);

		PlaneSignChangeStopper pscStopper = new PlaneSignChangeStopper(sf, targetPlane, result);
		pscStopper.initialize();

		swimSignChange(u, s, sf, Math.max(del, accuracy), eps, deriv, pscStopper, result);
		s = result.getS();

		if (result.getStatus() != SWIM_SUCCESS) {
			return;
		}

		
		double[] uf = result.getU();
		signedDist = targetPlane.signedDistance(uf);
		s = result.getS();

		//get the interpolated intersection. Note this  will NOT be the last trajectory point.
		//the last point on the trajectory will be a point on the start side of the intersection that is within the
		//requested accuracy.

		intersection.checkSetRight(uf, s, Math.abs(signedDist));
		result.computeIntersection(targetPlane);
		
		
		result.setS(intersection.getS());
		intersection.setU(result.getU());
	}

	/**
	 * Swim to an arbitrary infinitely long cylinder using the current active field
	 *
	 * @param charge         in integer units of e
	 * @param xo             the x vertex position in meters
	 * @param yo             the y vertex position in meters
	 * @param zo             the z vertex position in meters
	 * @param momentum       the momentum in GeV/c
	 * @param theta          the initial polar angle in degrees
	 * @param phi            the initial azimuthal angle in degrees
	 * @param centerLineP1   one point [array: (x, y, z)] on the infinite center line (meters)
	 * @param centerLineP2   another point [array: (x, y, z)] on the infinite center line (meters)
	 * @param radius         the radius of the cylinder in meters
	 * @param accuracy       the requested accuracy for the target rho in meters
	 * @param sMax           the final (max) value of the independent variable
	 *                       (pathlength) unless the integration is terminated by
	 *                       the stopper
	 * @param h              the initial stepsize in meters
	 * @param eps            the overall fractional tolerance (e.g., 1.0e-5)
	 * @param result         the container for some of the swimming results
	 * @throws AdaptiveSwimException
	 */
	public void swimCylinder(int charge, double xo, double yo, double zo, double momentum,
			double theta, double phi, 
			double centerLineP1[], double centerLineP2[], double radius,
			double accuracy, double sMax, double h, double eps, AdaptiveSwimResult result)
			throws AdaptiveSwimException {

		if ((centerLineP1[0] == 0) && (centerLineP1[1] == 0) && (centerLineP2[0] == 0) && (centerLineP2[1] == 0)) {
			baseSwim(charge, xo, yo, zo, momentum, theta, phi, sMax, h, eps, result,
					new AdaptiveRhoStopper(sMax, radius, accuracy, result));
			return;
		}

		Cylinder targetCylinder = new Cylinder(centerLineP1, centerLineP2, radius);
		baseSwim(charge, xo, yo, zo, momentum, theta, phi, sMax, h, eps, result,
				new AdaptiveCylinderStopper(sMax, targetCylinder, accuracy, result));
	}
	
	/**
	 * Swim to an arbitrary infinitely long cylinder using the current active field
	 *
	 * @param charge         in integer units of e
	 * @param xo             the x vertex position in meters
	 * @param yo             the y vertex position in meters
	 * @param zo             the z vertex position in meters
	 * @param momentum       the momentum in GeV/c
	 * @param theta          the initial polar angle in degrees
	 * @param phi            the initial azimuthal angle in degrees
	 * @param targetCylinder the target cylinder
	 * @param accuracy       the requested accuracy for the target rho in meters
	 * @param sMax           the final (max) value of the independent variable
	 *                       (pathlength) unless the integration is terminated by
	 *                       the stopper
	 * @param h              the initial stepsize in meters
	 * @param eps            the overall fractional tolerance (e.g., 1.0e-5)
	 * @param result         the container for some of the swimming results
	 * @throws AdaptiveSwimException
	 */
	public void swimCylinder(int charge, double xo, double yo, double zo, double momentum,
			double theta, double phi, 
			Cylinder targetCylinder, double accuracy, double sMax, double h, double eps, AdaptiveSwimResult result)
			throws AdaptiveSwimException {
		baseSwim(charge, xo, yo, zo, momentum, theta, phi, sMax, h, eps, result,
				new AdaptiveCylinderStopper(sMax, targetCylinder, accuracy, result));
	}


	/**
	 * Swim to an arbitrary infinitely long cylinder using the current active field
	 *
	 * @param charge         in integer units of e
	 * @param xo             the x vertex position in meters
	 * @param yo             the y vertex position in meters
	 * @param zo             the z vertex position in meters
	 * @param momentum       the momentum in GeV/c
	 * @param theta          the initial polar angle in degrees
	 * @param phi            the initial azimuthal angle in degrees
	 * @param targetSphere   the target sphere
	 * @param accuracy       the requested accuracy for the target rho in meters
	 * @param sMax           the final (max) value of the independent variable
	 *                       (pathlength) unless the integration is terminated by
	 *                       the stopper
	 * @param h              the initial stepsize in meters
	 * @param eps            the overall fractional tolerance (e.g., 1.0e-5)
	 * @param result         the container for some of the swimming results
	 * @throws AdaptiveSwimException
	 */
	public void swimSphere(int charge, double xo, double yo, double zo, double momentum,
			double theta, double phi, 
			Sphere targetSphere, double accuracy, double sMax, double h, double eps, AdaptiveSwimResult result)
			throws AdaptiveSwimException {
		baseSwim(charge, xo, yo, zo, momentum, theta, phi, sMax, h, eps, result,
				new AdaptiveSphereStopper(sMax, targetSphere, accuracy, result));
	}
	
	/**
	 * Swim to an arbitrary infinitely long cylinder using the current active field
	 *
	 * @param charge         in integer units of e
	 * @param xo             the x vertex position in meters
	 * @param yo             the y vertex position in meters
	 * @param zo             the z vertex position in meters
	 * @param momentum       the momentum in GeV/c
	 * @param theta          the initial polar angle in degrees
	 * @param phi            the initial azimuthal angle in degrees
	 * @param r              the (centered on origin) target sphere radius
	 * @param accuracy       the requested accuracy for the target rho in meters
	 * @param sMax           the final (max) value of the independent variable
	 *                       (pathlength) unless the integration is terminated by
	 *                       the stopper
	 * @param h              the initial stepsize in meters
	 * @param eps            the overall fractional tolerance (e.g., 1.0e-5)
	 * @param result         the container for some of the swimming results
	 * @throws AdaptiveSwimException
	 */
	public void swimSphere(int charge, double xo, double yo, double zo, double momentum,
			double theta, double phi, 
			double r, double accuracy, double sMax, double h, double eps, AdaptiveSwimResult result)
			throws AdaptiveSwimException {
		baseSwim(charge, xo, yo, zo, momentum, theta, phi, sMax, h, eps, result,
				new AdaptiveSphereStopper(sMax, r, accuracy, result));
	}



	/**
	 * Swims a charged particle in a sector coordinate system. This swims to a fixed
	 * z value. THIS IS ONLY VALID IF THE FIELD IS A RotatedComnpositeField or
	 * RotatedCompositeProbe
	 *
	 * @param sector   the sector [1..6]
	 * @param charge   in integer units of e
	 * @param xo       the x vertex position in meters
	 * @param yo       the y vertex position in meters
	 * @param zo       the z vertex position in meters
	 * @param momentum the momentum in GeV/c
	 * @param theta    the initial polar angle in degrees
	 * @param phi      the initial azimuthal angle in degrees
	 * @param targetZ  the target z in meters
	 * @param accuracy the requested accuracy for the target z in meters
	 * @param sMax     the final (max) value of the independent variable
	 *                 (pathlength) unless the integration is terminated by the
	 *                 stopper
	 * @param h        the initial stepsize in meters
	 * @param eps      the overall fractional tolerance (e.g., 1.0e-5)
	 * @param result   the container for some of the swimming results
	 * @throws AdaptiveSwimException
	 */
	public void sectorSwimZ(int sector, int charge, double xo, double yo, double zo,
			double momentum, double theta, double phi, double targetZ, double accuracy,
			double sMax, double h, double eps, AdaptiveSwimResult result)
			throws AdaptiveSwimException {

		if (!(_probe instanceof RotatedCompositeProbe)) {
			System.err.println("Can only call sectorSwim with a RotatedComposite Probe");
			throw new AdaptiveSwimException("Bad Probe: Must call a sectorSwim method with a RotatedCompositeProbe");
		}

		baseSwim(charge, xo, yo, zo, momentum, theta, phi, sMax, h, eps, result,
				new AdaptiveZStopper(sMax, targetZ, accuracy, result));


	}
	
	/**
	 * Stores the initial variables in the result object, and then
	 * sets the result object state vector to the corresponding starting state vector
	 * @param charge the integer charge
	 * @param xo the initial x position in meters
	 * @param yo the initial y position in meters
	 * @param zo the initial z position in meters
	 * @param momentum the momentum in GeV/c
	 * @param theta the polar angle in degrees
	 * @param phi the azimuthal angle in degrees
	 * @param stopper the adaptive stopper
	 */
	private void init(int charge, double xo, double yo, double zo, double momentum,
			double theta, double phi, IAdaptiveStopper stopper) {

		//create a swim result
		AdaptiveSwimResult result = stopper.getResult();
		
		// clear old trajectory
		if (result.getTrajectory() != null) {
			result.getTrajectory().clear();

			result.getTrajectory()
					.setGeneratedParticleRecord(new GeneratedParticleRecord(charge, xo, yo, zo, momentum, theta, phi));
		}

		// store initial values
		result.setInitialValues(charge, xo, yo, zo, momentum, theta, phi);

		stuffU(result.getU(), xo, yo, zo, momentum, theta, phi);
		stopper.initialize();

	}

	//convert the "normal" variables into a state vector
	private void stuffU(double[] u, double xo, double yo, double zo, double momentum,
			double theta, double phi) {
		double thetaRad = Math.toRadians(theta);
		double phiRad = Math.toRadians(phi);
		double sinTheta = Math.sin(thetaRad);

		double tx = sinTheta * Math.cos(phiRad); // px/p
		double ty = sinTheta * Math.sin(phiRad); // py/p
		double tz = Math.cos(thetaRad); // pz/p

		// set uf (in the result container) to the starting state vector
		u[0] = xo;
		u[1] = yo;
		u[2] = zo;
		u[3] = tx;
		u[4] = ty;
		u[5] = tz;
	}

	// create a straight line for neutral particles
	//the starting point will already be set as the result current statevector
	protected void straightLine(double xo, double yo, double zo, double momentum, double theta,
			double phi, double sf, AdaptiveSwimResult result) {
		result.setS(sf);
		result.setNStep(2);

		double uo[] = new double[6];
		double uf[] = result.getU();

		double sintheta = Math.sin(Math.toRadians(theta));
		double costheta = Math.cos(Math.toRadians(theta));
		double sinphi = Math.sin(Math.toRadians(phi));
		double cosphi = Math.cos(Math.toRadians(phi));

		double xf = xo + sf * sintheta * cosphi;
		double yf = yo + sf * sintheta * sinphi;
		double zf = zo + sf * costheta;

		stuffU(uo, xo, yo, zo, momentum, theta, phi);
		stuffU(uf, xf, yf, zf, momentum, theta, phi);

		if (result.hasTrajectory()) {
			SwimTrajectory traj = result.getTrajectory();
			traj.clear();
			traj.add(uo, 0);
			traj.add(uf, sf);
		}
	}


}
