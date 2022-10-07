package cnuphys.swim;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import cnuphys.adaptiveSwim.AdaptiveSwimException;
import cnuphys.adaptiveSwim.AdaptiveSwimResult;
import cnuphys.adaptiveSwim.AdaptiveSwimmer;
import cnuphys.adaptiveSwim.geometry.Cylinder;
import cnuphys.adaptiveSwim.geometry.Point;
import cnuphys.adaptiveSwim.geometry.Vector;
import cnuphys.lund.GeneratedParticleRecord;
import cnuphys.magfield.FastMath;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.IMagField;
import cnuphys.magfield.MagneticField;
import cnuphys.magfield.RotatedCompositeProbe;
import cnuphys.rk4.ButcherTableau;
import cnuphys.rk4.IRkListener;
import cnuphys.rk4.IStopper;
import cnuphys.rk4.RungeKutta;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.swim.util.Plane;
/**
 * Handles the swimming of a particle through a magnetic field.
 *
 * @author heddle
 *
 */
public final class Swimmer {
	public static Logger LOGGER = Logger.getLogger(Swimmer.class.getName());

	// Speed of light in m/s
	public static final double C = 299792458.0; // m/s

	// Min momentum to swim in GeV/c
	public static final double MINMOMENTUM = 5e-05;

	//tolerance when swimmimg to a max path length
	public static final double SMAX_TOLERANCE = 1.0e-4;  //meters

	// We have different tableaus we can use for RK integration
	public static final ButcherTableau _defaultTableau = ButcherTableau.DORMAND_PRINCE;
	
    private AdaptiveSwimmer _newSwimmer;


	/**
	 * In swimming routines that require a tolerance vector, this is a
	 * reasonable one to use for CLAS. These represent absolute errors in the
	 * adaptive stepsize algorithms
	 */
	// private static double _eps = 1.0e-6;
	private static double _eps = 1.0e-5;
	// private static double _eps = 1.0e-4;
	public static double CLAS_Tolerance[];

	// Field getter.
	// NOTE: the method of interest in FieldProbe takes a position in cm
	// and returns a field in kG.This swim package works in SI (meters and
	// Tesla)
	// so care has to be taken when using the field object
	private FieldProbe _probe;

	public static final String VERSION = "1.08";

	static {
		setCLASTolerance(_eps);
		LOGGER.log(Level.FINE,"\n***********************************");
		LOGGER.log(Level.FINE,"* Swimmer package version: " + VERSION);
		LOGGER.log(Level.FINE,"* contact: david.heddle@cnu.edu");
		LOGGER.log(Level.FINE,"***********************************\n");
	}


	/**
	 * Create a swimmer using the current active field
	 */
	public Swimmer() {
		//make a probe using the current active field
		_probe = FieldProbe.factory();
		_newSwimmer = new AdaptiveSwimmer();
	}

	/**
	 * Create a swimmer specific to a magnetic field
	 * @param magneticField the magnetic field
	 */
	public Swimmer(MagneticField magneticField) {
		_probe = FieldProbe.factory(magneticField);
		_newSwimmer = new AdaptiveSwimmer(magneticField);

	}

	/**
	 * Create a swimmer specific to a magnetic field
	 * @param magneticField the magnetic field
	 */
	public Swimmer(IMagField magneticField) {
		_probe = FieldProbe.factory(magneticField);
		_newSwimmer = new AdaptiveSwimmer(magneticField);
	}

	/**
	 * Return the version string
	 *
	 * @return the version string
	 */
	public static final String getVersion() {
		return VERSION;
	}

	/**
	 * Get the underlying field probe
	 * @return the probe
	 */
	public FieldProbe getProbe() {
		return _probe;
	}

	/**
	 * Compute the radius of curvature in cm
	 *
	 * @param Q
	 *            the charge in units of e
	 * @param p
	 *            the momentum in GeV/c
	 * @param B
	 *            the field in kG
	 * @return the radius in cm.
	 */
	public static double radiusOfCurvature(int Q, double p, double B) {

		if (Q == 0) {
			return Double.POSITIVE_INFINITY;
		}
		double c = C / 1.0e9;
		p = 1000 * p; // MeV
		return Math.abs(p / (c * Q * B));
	}

	/**
	 * Set the tolerance used by the CLAS_Tolerance array
	 *
	 * @param eps
	 *            the baseline tolerance. The default is 1.0e-5. Probably should
	 *            stay in the range 1e-10 (accurate but slow) to 1e-4
	 *            (inaccurate but fast)
	 */
	public static void setCLASTolerance(double eps) {
		_eps = eps;
		double xscale = 1.0; // position scale order of meters
		double pscale = 1.0; // direct cosine px/P etc scale order of 1
		double xTol = eps * xscale;
		double pTol = eps * pscale;
		CLAS_Tolerance = new double[6];
		for (int i = 0; i < 3; i++) {
			CLAS_Tolerance[i] = xTol;
			CLAS_Tolerance[i + 3] = pTol;
		}
	}

	/**
	 * Swims a particle with a built in stopper at the boundary of an
	 * arbitrary cylinder. Uses an adaptive stepsize algorithm.

	 *
	 * @param charge       the charge: -1 for electron, 1 for proton, etc
	 * @param xo           the x vertex position in meters
	 * @param yo           the y vertex position in meters
	 * @param zo           the z vertex position in meters
	 * @param momentum     initial momentum in GeV/c
	 * @param theta        initial polar angle in degrees
	 * @param phi          initial azimuthal angle in degrees
	 * @param centerLineP1 one point [array: (x, y, z)] on the infinite center line (meters)
	 * @param centerLineP2 another point [array: (x, y, z)] on the infinite center line (meters)
	 * @param radius       the radius of the cylinder in meters
	 * @param accuracy     the accuracy of the fixed rho termination, in meters
	 * @param sMax         Max path length in meters. This determines the max number
	 *                     of steps based on the step size. If a stopper is used,
	 *                     the integration might terminate before all the steps are
	 *                     taken. A reasonable value for CLAS is 8. meters
	 * @param stepSize     the initial step size in meters.
	 * @param relTolerance the error tolerance as fractional diffs. Note it is a
	 *                     vector, the same dimension of the problem, e.g., 6 for
	 *                     [x,y,z,vx,vy,vz]. It might be something like {1.0e-10,
	 *                     1.0e-10, 1.0e-10, 1.0e-8, 1.0e-8, 1.0e-8}
	 * @param result upon return, results from the swim including the final state vector [x, y, z, px/p, py/p, pz/p]
	 */
	public void swimCylinder(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
			double centerLineP1[],
			double centerLineP2[],
			double radius,
			double accuracy, double sMax, double stepSize, double relTolerance[], AdaptiveSwimResult result)
			throws RungeKuttaException {


//		String s = String.format("%2d, %9.6f, %9.6f, %9.6f, %9.6f, %9.6f, %9.6f, %9.6f, %9.6f, %9.6f, %9.6f, %9.6f, %9.6f, %9.6f, %9.6f, %9.6f, %9.6f",
//				charge, xo, yo, zo, momentum, theta, phi,
//				centerLineP1[0], centerLineP1[1], centerLineP1[2],
//				centerLineP2[0], centerLineP2[1], centerLineP2[2],
//				radius, accuracy, sMax, stepSize);
//		System.err.println(s);


		Cylinder targetCylinder = new Cylinder(centerLineP1, centerLineP2, radius);

		result.setInitialValues(charge, xo, yo, zo, momentum, theta, phi);

		// set u to the starting state vector
		double thetaRad = Math.toRadians(theta);
		double phiRad = Math.toRadians(phi);
		double sinTheta = Math.sin(thetaRad);

		double px = sinTheta*Math.cos(phiRad); //px/p
		double py = sinTheta*Math.sin(phiRad); //py/p
		double pz = Math.cos(thetaRad); //pz/p

		double uf[] = result.getUf();
		uf[0] = xo;
		uf[1] = yo;
		uf[2] = zo;
		uf[3] = px;
		uf[4] = py;
		uf[5] = pz;

		if (momentum < MINMOMENTUM) {
			result.setNStep(0);
			result.setFinalS(0);
			result.setStatus(AdaptiveSwimmer.SWIM_BELOW_MIN_P);
			return;
		}

		double del = Double.POSITIVE_INFINITY;
		int maxtry = 25;
		int count = 0;
		double sFinal = 0;
		int ns = 0;


		while ((count < maxtry) && (del > accuracy)) {


			uf = result.getUf();
			if (count > 0) {
				px = uf[3];
				py = uf[4];
				pz = uf[5];
				theta = FastMath.acos2Deg(pz);
				phi = FastMath.atan2Deg(py, px);
			}


			DefaultCylinderStopper stopper = new DefaultCylinderStopper(uf, sFinal, sMax, targetCylinder, accuracy);

			ns += swim(charge, uf[0], uf[1], uf[2], momentum, theta, phi, stopper, null, sMax-sFinal, stepSize, relTolerance, null);

			System.arraycopy(stopper.getFinalU(), 0, result.getUf(), 0, result.getUf().length);

			sFinal = stopper.getFinalT();

			del = Math.abs(targetCylinder.distance(stopper.getFinalU()[0], stopper.getFinalU()[1], stopper.getFinalU()[2]));

			// succeed?
			if (del < accuracy) {
				result.setStatus(AdaptiveSwimmer.SWIM_SUCCESS);
				break;
			}

			// passed max path length?
			if (stopper.passedSmax()) {
				result.setStatus(AdaptiveSwimmer.SWIM_TARGET_MISSED);
				break;
			}

			if (++count >= maxtry) {
				result.setStatus(AdaptiveSwimmer.SWIM_EXCEED_MAX_TRIES);
				break;
			}

			if (stopper.crossedBoundary()) {
				stepSize = Math.max(stepSize / 2, del / 5);
			}

		} // while

		result.setNStep(ns);
		result.setFinalS(sFinal);
	}


	/**
	 * Swims a particle with a built in stopper for the rho coordinate.
     * Uses an adaptive stepsize algorithm.
	 *
	 * @param charge       the charge: -1 for electron, 1 for proton, etc
	 * @param xo           the x vertex position in meters
	 * @param yo           the y vertex position in meters
	 * @param zo           the z vertex position in meters
	 * @param momentum     initial momentum in GeV/c
	 * @param theta        initial polar angle in degrees
	 * @param phi          initial azimuthal angle in degrees
	 * @param fixedRho     the fixed rho value (meters) that terminates (or
	 *                     maxPathLength if reached first)
	 * @param accuracy     the accuracy of the fixed rho termination, in meters
	 * @param sMax         Max path length in meters. This determines the max number
	 *                     of steps based on the step size. If a stopper is used,
	 *                     the integration might terminate before all the steps are
	 *                     taken. A reasonable value for CLAS is 8. meters
	 * @param stepSize     the initial step size in meters.
	 * @param relTolerance the error tolerance as fractional diffs. Note it is a
	 *                     vector, the same dimension of the problem, e.g., 6 for
	 *                     [x,y,z,vx,vy,vz]. It might be something like {1.0e-10,
	 *                     1.0e-10, 1.0e-10, 1.0e-8, 1.0e-8, 1.0e-8}
	 * @param result upon return, results from the swim including the final state vector [x, y, z, px/p, py/p, pz/p]
	 * @throws RungeKuttaException
	 */

	public void swimRho(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
			final double fixedRho, double accuracy, double sMax, double stepSize, double relTolerance[], AdaptiveSwimResult result)
			throws RungeKuttaException {

		result.setInitialValues(charge, xo, yo, zo, momentum, theta, phi);

		// set u to the starting state vector
		double thetaRad = Math.toRadians(theta);
		double phiRad = Math.toRadians(phi);
		double sinTheta = Math.sin(thetaRad);

		double px = sinTheta*Math.cos(phiRad); //px/p
		double py = sinTheta*Math.sin(phiRad); //py/p
		double pz = Math.cos(thetaRad); //pz/p

		double uf[] = result.getUf();
		uf[0] = xo;
		uf[1] = yo;
		uf[2] = zo;
		uf[3] = px;
		uf[4] = py;
		uf[5] = pz;

		if (momentum < MINMOMENTUM) {
			result.setNStep(0);
			result.setFinalS(0);
			result.setStatus(AdaptiveSwimmer.SWIM_BELOW_MIN_P);
			return;
		}

		double del = Double.POSITIVE_INFINITY;
		int maxtry = 25;
		int count = 0;
		double sFinal = 0;
		int ns = 0;


		while ((count < maxtry) && (del > accuracy)) {

			uf = result.getUf();
			if (count > 0) {
				px = uf[3];
				py = uf[4];
				pz = uf[5];
				theta = FastMath.acos2Deg(pz);
				phi = FastMath.atan2Deg(py, px);
			}

			DefaultRhoStopper stopper = new DefaultRhoStopper(uf, sFinal, sMax, Math.hypot(uf[0], uf[1]), fixedRho, accuracy);

			ns += swim(charge, uf[0], uf[1], uf[2], momentum, theta, phi, stopper, null, sMax-sFinal, stepSize, relTolerance, null);

			System.arraycopy(stopper.getFinalU(), 0, result.getUf(), 0, result.getUf().length);

			sFinal = stopper.getFinalT();

			del = Math.abs(stopper.getRho() - fixedRho);

			// succeed?
			if (del < accuracy) {
				result.setStatus(AdaptiveSwimmer.SWIM_SUCCESS);
				break;
			}

			// passed max path length?
			if (stopper.passedSmax()) {
				result.setStatus(AdaptiveSwimmer.SWIM_TARGET_MISSED);
				break;
			}

			if (++count >= maxtry) {
				result.setStatus(AdaptiveSwimmer.SWIM_EXCEED_MAX_TRIES);
				break;
			}

			if (stopper.crossedBoundary()) {
				stepSize = Math.max(stepSize / 2, del / 5);
			}



		} // while

		result.setNStep(ns);
		result.setFinalS(sFinal);
	}

	/**
     * 
     * @param charge               the charge: -1 for electron, 1 for proton, etc
     * @param xo                   the x vertex position in meters
     * @param yo                   the y vertex position in meters
     * @param zo                   the z vertex position in meters
     * @param momentum             initial momentum in GeV/c
     * @param theta                initial polar angle in degrees
     * @param phi                  initial azimuthal angle in degrees
     * @param normX                x component of the normal to the plane
     * @param normY                y component of the normal to the plane
     * @param normZ                z component of the normal to the plane
     * @param pointX               x coordinate of point in plane
     * @param pointY               y coordinate of point in plane
     * @param pointZ               z coordinate of point in plane
     * @param accuracy             the accuracy of the fixed rho termination, in
     *                             meters
     * @param sMax                 maximum path length (m)
     * @param stepSize             initial stepsize (m)
 	 * @param eps                  the overall fractional tolerance (e.g., 1.0e-6)
     * @param result               holds the result
     * @throws AdaptiveSwimException
     */
    public void swimPlaneInterp(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
            double normX, double normY, double normZ, double pointX, double pointY, double pointZ,
            double accuracy, double sMax, double stepSize, double eps, AdaptiveSwimResult result)
            throws AdaptiveSwimException {
        cnuphys.adaptiveSwim.geometry.Plane targetPlane = new cnuphys.adaptiveSwim.geometry.Plane(new Vector(normX,normY,normZ), new Point(pointX,pointY,pointZ));

        swimPlaneInterp(charge, xo, yo, zo, momentum, theta, phi, targetPlane, 
                accuracy, sMax, stepSize, eps, result);
    }
    
    /**
     * 
     * @param charge               the charge: -1 for electron, 1 for proton, etc
     * @param xo                   the x vertex position in meters
     * @param yo                   the y vertex position in meters
     * @param zo                   the z vertex position in meters
     * @param momentum             initial momentum in GeV/c
     * @param theta                initial polar angle in degrees
     * @param phi                  initial azimuthal angle in degrees
     * @param targetPlane
     * @param accuracy
     * @param sMax
     * @param stepSize
     * @param relTolerance
     * @param result
     * @throws AdaptiveSwimException
     */
    public void swimPlaneInterp(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
            cnuphys.adaptiveSwim.geometry.Plane targetPlane,
            double accuracy, double sf, double h0, double eps, AdaptiveSwimResult result)
            throws AdaptiveSwimException {
        
        _newSwimmer.swimPlaneInterp(charge, xo, yo, zo,
                momentum, theta, phi, targetPlane, accuracy, sf, h0, eps, result);
    
    }
    

	/**
     * 
     * @param charge               the charge: -1 for electron, 1 for proton, etc
     * @param xo                   the x vertex position in meters
     * @param yo                   the y vertex position in meters
     * @param zo                   the z vertex position in meters
     * @param momentum             initial momentum in GeV/c
     * @param theta                initial polar angle in degrees
     * @param phi                  initial azimuthal angle in degrees
     * @param normX                x component of the normal to the plane
     * @param normY                y component of the normal to the plane
     * @param normZ                z component of the normal to the plane
     * @param pointX               x coordinate of point in plane
     * @param pointY               y coordinate of point in plane
     * @param pointZ               z coordinate of point in plane
     * @param sMax                 maximum path length (m)
     * @param stepSize             initial stepsize (m)
 	 * @param eps                  the overall fractional tolerance (e.g., 1.0e-6)
     * @param result               holds the result
     * @throws AdaptiveSwimException
     */
    public void swimPlaneInterp(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
            double normX, double normY, double normZ, double pointX, double pointY, double pointZ,
            double sMax, double stepSize, double eps, AdaptiveSwimResult result)
            throws AdaptiveSwimException {
        cnuphys.adaptiveSwim.geometry.Plane targetPlane = new cnuphys.adaptiveSwim.geometry.Plane(new Vector(normX,normY,normZ), new Point(pointX,pointY,pointZ));

        swimPlaneInterp(charge, xo, yo, zo, momentum, theta, phi, targetPlane, 
                sMax, stepSize, eps, result);
    }
    
    /**
     * 
     * @param charge               the charge: -1 for electron, 1 for proton, etc
     * @param xo                   the x vertex position in meters
     * @param yo                   the y vertex position in meters
     * @param zo                   the z vertex position in meters
     * @param momentum             initial momentum in GeV/c
     * @param theta                initial polar angle in degrees
     * @param phi                  initial azimuthal angle in degrees
     * @param targetPlane
     * @param sMax
     * @param stepSize
     * @param relTolerance
     * @param result
     * @throws AdaptiveSwimException
     */
    public void swimPlaneInterp(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
            cnuphys.adaptiveSwim.geometry.Plane targetPlane,
            double sf, double h0, double eps, AdaptiveSwimResult result)
            throws AdaptiveSwimException {
        
        _newSwimmer.swimPlaneInterp(charge, xo, yo, zo,
                momentum, theta, phi, targetPlane, sf, h0, eps, result);
    
    }
    


	public void swimPlane(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
			double normX, double normY, double normZ, double pointX, double pointY, double pointZ,
			double accuracy, double sMax, double stepSize, double relTolerance[], AdaptiveSwimResult result)
			throws RungeKuttaException {

		cnuphys.adaptiveSwim.geometry.Plane targetPlane = new cnuphys.adaptiveSwim.geometry.Plane(new Vector(normX,normY,normZ), new Point(pointX,pointY,pointZ));
		result.setInitialValues(charge, xo, yo, zo, momentum, theta, phi);

		// set u to the starting state vector
		double thetaRad = Math.toRadians(theta);
		double phiRad = Math.toRadians(phi);
		double sinTheta = Math.sin(thetaRad);

		double px = sinTheta*Math.cos(phiRad); //px/p
		double py = sinTheta*Math.sin(phiRad); //py/p
		double pz = Math.cos(thetaRad); //pz/p

		double uf[] = result.getUf();
		uf[0] = xo;
		uf[1] = yo;
		uf[2] = zo;
		uf[3] = px;
		uf[4] = py;
		uf[5] = pz;

		if (momentum < MINMOMENTUM) {
			result.setNStep(0);
			result.setFinalS(0);
			result.setStatus(-2);
			result.setStatus(AdaptiveSwimmer.SWIM_BELOW_MIN_P);
			return;
		}

		double del = Double.POSITIVE_INFINITY;
		int maxtry = 25;
		int count = 0;
		double sFinal = 0;
		int ns = 0;


		while ((count < maxtry) && (del > accuracy)) {


			uf = result.getUf();
			if (count > 0) {
				px = uf[3];
				py = uf[4];
				pz = uf[5];
				theta = FastMath.acos2Deg(pz);
				phi = FastMath.atan2Deg(py, px);
			}


			NewPlaneStopper stopper = new NewPlaneStopper(uf, sFinal, sMax, targetPlane, accuracy);

			ns += swim(charge, uf[0], uf[1], uf[2], momentum, theta, phi, stopper, null, sMax-sFinal, stepSize, relTolerance, null);

			System.arraycopy(stopper.getFinalU(), 0, result.getUf(), 0, result.getUf().length);

			sFinal = stopper.getFinalT();

			del = Math.abs(targetPlane.signedDistance(stopper.getFinalU()[0], stopper.getFinalU()[1], stopper.getFinalU()[2]));

			// succeed?
			if (del < accuracy) {
				result.setStatus(AdaptiveSwimmer.SWIM_SUCCESS);
				break;
			}

			// passed max path length?
			if (stopper.passedSmax()) {
				result.setStatus(AdaptiveSwimmer.SWIM_TARGET_MISSED);
				break;
			}

			if (++count >= maxtry) {
				result.setStatus(AdaptiveSwimmer.SWIM_EXCEED_MAX_TRIES);
				break;
			}

			if (stopper.crossedBoundary()) {
				stepSize = Math.max(stepSize / 2, del / 5);
			}



		} // while


		result.setNStep(ns);
		result.setFinalS(sFinal);

	}

	/**
	 * Get the tolerance used by the CLAS_Toleance array
	 *
	 * @return the tolerance used by the CLAS_Toleance array
	 */
	public static double getEps() {
		return _eps;
	}

	/**
	 * Swims a Lund particle with a built it stopper for the maximum value of
	 * the radial coordinate. This is for the trajectory mode, where you want to
	 * cache steps along the path. Uses a fixed stepsize algorithm.
	 *
	 * @param charge
	 *            the charge: -1 for electron, 1 for proton, etc
	 * @param xo
	 *            the x vertex position in meters
	 * @param yo
	 *            the y vertex position in meters
	 * @param zo
	 *            the z vertex position in meters
	 * @param momentum
	 *            initial momentum in GeV/c
	 * @param theta
	 *            initial polar angle in degrees
	 * @param phi
	 *            initial azimuthal angle in degrees
	 * @param stopper
	 *            an optional object that can terminate the swimming based on
	 *            some condition
	 * @param maxPathLength
	 *            in meters. This determines the max number of steps based on
	 *            the step size. If a stopper is used, the integration might
	 *            terminate before all the steps are taken. A reasonable value
	 *            for CLAS is 8. meters
	 * @param stepSize
	 *            the uniform step size in meters.
	 * @param distanceBetweenSaves
	 *            this distance is in meters. It should be bigger than stepSize.
	 *            It is approximately the distance between "saves" where the
	 *            point is saved in a trajectory for later drawing.
	 * @return the trajectory of the particle
	 */
	public SwimTrajectory swim(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
			IStopper stopper, double maxPathLength, double stepSize, double distanceBetweenSaves) {

	//	System.err.println("  !!!!!!!!!   CALLED SWIMMER Swim  AA UNIFORM STEP");

//		String str = String.format("%2d,  %9.6f, %9.6f, %9.6f, %9.6f, %9.6f, %9.6f, %9.6f, %9.6f, %9.6f, %s",
//		charge, xo, yo, zo, momentum, theta, phi,
//		maxPathLength, stepSize, distanceBetweenSaves, stopper.getClass().getName());
//        System.err.println(str);

		// if no magnetic field or no charge, then simple straight line tracks.
		// the path will consist of just two points
		if ((_probe == null) || (charge == 0)) {
			GeneratedParticleRecord genPartRec = new GeneratedParticleRecord(charge, xo, yo, zo, momentum, theta, phi);
			return straightLineTrajectory(genPartRec, maxPathLength);
		}

		if (momentum < MINMOMENTUM) {
			return new SwimTrajectory(charge, xo, yo, zo, momentum, theta, phi);
		}

		// cycle is the number of advances per save
		int cycle = (int) (distanceBetweenSaves / stepSize);
		cycle = Math.max(2, cycle);

		// max number of possible steps--may not use all of them
		int ntotal = (int) (maxPathLength / stepSize); // number steps
		int nsave = ntotal / cycle; // aprox number saves

		// the the initial six vector
		double uo[] = initialState(xo, yo, zo, theta, phi);

		// storage for time and state
		double s[] = new double[ntotal];
		double u[][] = new double[6][ntotal];

		// create the trajectory container
		SwimTrajectory trajectory = new SwimTrajectory(charge, xo, yo, zo, momentum, theta, phi, nsave);

		// Integrate
		DefaultDerivative deriv = new DefaultDerivative(charge, momentum, _probe);
		ntotal = (new RungeKutta()).uniformStep(uo, 0, maxPathLength, u, s, deriv, stopper);

		// now cycle through and get the save points
		for (int i = 0; i < ntotal; i++) {
			if (((i % cycle) == 0) || (i == (ntotal - 1))) {
				double v[] = makeVector(u[0][i], u[1][i], u[2][i], u[3][i], u[4][i], u[5][i]);
				trajectory.add(v);
			}
		}

		return trajectory;
	}


	/**
	 * Swims a charged particle. This swims to a fixed plane. This is for the
	 * trajectory mode, where you want to cache steps along the path. Uses an
	 * adaptive stepsize algorithm.
	 *
	 * @param charge
	 *            the charge: -1 for electron, 1 for proton, etc
	 * @param xo
	 *            the x vertex position in meters
	 * @param yo
	 *            the y vertex position in meters
	 * @param zo
	 *            the z vertex position in meters
	 * @param momentum
	 *            initial momentum in GeV/c
	 * @param theta
	 *            initial polar angle in degrees
	 * @param phi
	 *            initial azimuthal angle in degrees
	 * @param normX
	 *            the x component of the normal vector
	 * @param normY
	 *            the y component of the normal vector
	 * @param normZ
	 *            the z component of the normal vector
	 * @param d the distance from the origin to the plane
	 * @param accuracy
	 *            the accuracy of the fixed z termination, in meters
	 * @param sMax
	 *            Max path length in meters. This determines the max number of
	 *            steps based on the step size. If a stopper is used, the
	 *            integration might terminate before all the steps are taken. A
	 *            reasonable value for CLAS is 8. meters
	 * @param stepSize
	 *            the initial step size in meters.
	 * @param relTolerance
	 *            the error tolerance as fractional diffs. Note it is a vector,
	 *            the same dimension of the problem, e.g., 6 for
	 *                     [x,y,z,vx,vy,vz]. It might be something like {1.0e-10,
	 *                     1.0e-10, 1.0e-10, 1.0e-8, 1.0e-8, 1.0e-8}
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] is the
	 *            min stepsize used (m), hdata[1] is the average stepsize used
	 *            (m), and hdata[2] is the max stepsize (m) used
	 * @return the trajectory of the particle
	 * @throws RungeKuttaException
	 */
	public SwimTrajectory swim(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
			final double normX, final double normY, final double normZ, final double d, double accuracy, double sMax, double stepSize, double relTolerance[], double hdata[])
			throws RungeKuttaException {

		Plane plane = Plane.createPlane(normX, normY, normZ, d);
		return swim(charge, xo, yo, zo, momentum, theta, phi, plane, accuracy, sMax, stepSize, relTolerance, hdata);

	}

	/**
	 * Swims a charged particle. This swims to a fixed plane. This is for the
	 * trajectory mode, where you want to cache steps along the path. Uses an
	 * adaptive stepsize algorithm.
	 *
	 * @param charge
	 *            the charge: -1 for electron, 1 for proton, etc
	 * @param xo
	 *            the x vertex position in meters
	 * @param yo
	 *            the y vertex position in meters
	 * @param zo
	 *            the z vertex position in meters
	 * @param momentum
	 *            initial momentum in GeV/c
	 * @param theta
	 *            initial polar angle in degrees
	 * @param phi
	 *            initial azimuthal angle in degrees
	 * @param plane
	 *            the plane to swim to
	 * @param accuracy
	 *            the accuracy of the fixed z termination, in meters
	 * @param sMax
	 *            Max path length in meters. This determines the max number of
	 *            steps based on the step size. If a stopper is used, the
	 *            integration might terminate before all the steps are taken. A
	 *            reasonable value for CLAS is 8. meters
	 * @param stepSize
	 *            the initial step size in meters.
	 * @param relTolerance
	 *            the error tolerance as fractional diffs. Note it is a vector,
	 *            the same dimension of the problem, e.g., 6 for
	 *                     [x,y,z,vx,vy,vz]. It might be something like {1.0e-10,
	 *                     1.0e-10, 1.0e-10, 1.0e-8, 1.0e-8, 1.0e-8}
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] is the
	 *            min stepsize used (m), hdata[1] is the average stepsize used
	 *            (m), and hdata[2] is the max stepsize (m) used
	 * @return the trajectory of the particle
	 * @throws RungeKuttaException
	 */
	public SwimTrajectory swim(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
			Plane plane, double accuracy, double sMax, double stepSize, double relTolerance[], double hdata[])
			throws RungeKuttaException {
		if (momentum < MINMOMENTUM) {
			return new SwimTrajectory(charge, xo, yo, zo, momentum, theta, phi);
		}

		//no field?
		if ((charge == 0) || (getProbe().isZeroField())) {
			// System.err.println("Skipping neutral or no field swim (D)");
			// just has to be proportional to velocity
			SwimTrajectory traj = new SwimTrajectory(charge, xo, yo, zo, momentum, theta, phi);
			double vz = momentum * Math.cos(Math.toRadians(theta));
			double vp = momentum * Math.sin(Math.toRadians(theta));
			double vx = vp * Math.cos(Math.toRadians(phi));
			double vy = vp * Math.sin(Math.toRadians(phi));
			double time = plane.timeToPlane(xo, yo, zo, vx, vy, vz);

			double xf = xo + vx*time;
			double yf = yo + vy*time;
			double zf = zo + vz*time;

			traj.add(xf, yf, zf, momentum, theta, phi);

			return traj;
		}


		DefaultPlaneStopper stopper = new DefaultPlaneStopper(0, sMax, plane, accuracy, 999);

		SwimTrajectory traj = null;
		// First try

		traj = swim(charge, xo, yo, zo, momentum, theta, phi, stopper, 0, sMax, stepSize, relTolerance, hdata);

		// if stopped because of max radius, we are done (never reached plane)
		double finalPathLength = stopper.getFinalT();

		// if stopped because of max s, we are done (never reached plane)
		if (finalPathLength > sMax) {
			return traj;
		}

		// are we there yet?
		double lastY[] = traj.lastElement();

		double del = plane.distanceToPlane(lastY[0], lastY[1], lastY[2]);
		int maxtry = 20;
		int count = 0;
//
//		// set the step size to half the accuracy
//		stepSize = accuracy / 2;

		//set the reverse stepsize to about 1/10 of distance to cover
		//stepSize = Math.max(accuracy, Math.abs(del/10));


		// have to deal with the fact that the hdata array will reset so save
		// current values
		double oldHdata[] = new double[3];
		oldHdata[0] = hdata[0];
		oldHdata[1] = hdata[1] * traj.size(); // back to sum, not avg
		oldHdata[2] = hdata[2];

		while ((count < maxtry) && (del > accuracy)) {



			// last element had beyond plane cutoff
			int lastIndex = traj.size() - 1;
			traj.remove(lastIndex);
			lastY = traj.lastElement();
			xo = lastY[0];
			yo = lastY[1];
			zo = lastY[2];
			double px = lastY[3];
			double py = lastY[4];
			double pz = lastY[5];

			del = plane.distanceToPlane(lastY[0], lastY[1], lastY[2]);
			stepSize = Math.max(accuracy, Math.abs(del/10));
			// System.out.print("COUNT: " + count + " StepSize: " + stepSize);

			// System.err.println("New start state = " + String.format("(%9.6f,
			// %9.6f, %9.6f) (%9.6f, %9.6f, %9.6f)", xo, yo, zo, px, py, pz));

			stopper = new DefaultPlaneStopper(finalPathLength, sMax, plane, accuracy, -stopper.getSide());

			// momentum = traj.getFinalMomentum();
			theta = FastMath.acos2Deg(pz);
			phi = FastMath.atan2Deg(py, px);

			SwimTrajectory addTraj = swim(charge, xo, yo, zo, momentum, theta, phi, stopper, finalPathLength, sMax,
					stepSize, relTolerance, hdata);

			finalPathLength = stopper.getFinalT();
			// System.err.println("** STOP PLEN (B) = " + finalPathLength);

			hdata[0] = Math.min(oldHdata[0], hdata[0]);
			hdata[1] = hdata[1] * addTraj.size();
			hdata[1] = oldHdata[1] + hdata[1];
			hdata[2] = Math.max(oldHdata[2], hdata[2]);
			oldHdata[0] = hdata[0];
			oldHdata[1] = hdata[1];
			oldHdata[2] = hdata[2];

			// merge the trajectories
			traj.addAll(addTraj);
			lastY = traj.lastElement();

			del = plane.distanceToPlane(lastY[0], lastY[1], lastY[2]);

			// System.out.println(" del: " + del);
			count++;
			// stepSize /= 2;
			// stepSize = Math.max(accuracy, Math.abs(del/10));
		} // while

		// now can get overall avg stepsize
		hdata[1] = hdata[1] / traj.size();
		return traj;
	}



	/**
	 * Swims a charged particle. This swims to a fixed z value. This is for the
	 * trajectory mode, where you want to cache steps along the path. Uses an
	 * adaptive stepsize algorithm.
	 *
	 * @param charge
	 *            the charge: -1 for electron, 1 for proton, etc
	 * @param xo
	 *            the x vertex position in meters
	 * @param yo
	 *            the y vertex position in meters
	 * @param zo
	 *            the z vertex position in meters
	 * @param momentum
	 *            initial momentum in GeV/c
	 * @param theta
	 *            initial polar angle in degrees
	 * @param phi
	 *            initial azimuthal angle in degrees
	 * @param fixedZ
	 *            the fixed z value (meters) that terminates (or maxPathLength
	 *            if reached first)
	 * @param accuracy
	 *            the accuracy of the fixed z termination, in meters
	 * @param maxRad
	 *            the max radial coordinate NOTE: NO LONGER USED (here for
	 *                     backwards compatibility)
	 * @param sMax
	 *            Max path length in meters. This determines the max number of
	 *            steps based on the step size. If a stopper is used, the
	 *            integration might terminate before all the steps are taken. A
	 *            reasonable value for CLAS is 8. meters
	 * @param stepSize
	 *            the initial step size in meters.
	 * @param relTolerance
	 *            the error tolerance as fractional diffs. Note it is a vector,
	 *            the same dimension of the problem, e.g., 6 for
	 *                     [x,y,z,vx,vy,vz]. It might be something like {1.0e-10,
	 *                     1.0e-10, 1.0e-10, 1.0e-8, 1.0e-8, 1.0e-8}
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] is the
	 *            min stepsize used (m), hdata[1] is the average stepsize used
	 *            (m), and hdata[2] is the max stepsize (m) used
	 * @return the trajectory of the particle
	 * @throws RungeKuttaException
	 */
	public SwimTrajectory swim(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
			final double fixedZ, double accuracy, double maxRad, double sMax, double stepSize, double relTolerance[],
			double hdata[]) throws RungeKuttaException {
		return swim(charge, xo, yo, zo, momentum, theta, phi, fixedZ, accuracy, sMax, stepSize, relTolerance, hdata);
	}
	
	/**
	 * Swims a charged particle. This swims to a fixed z value. This is for the
	 * trajectory mode, where you want to cache steps along the path. Uses an
	 * adaptive stepsize algorithm.
	 *
	 * @param charge
	 *            the charge: -1 for electron, 1 for proton, etc
	 * @param xo
	 *            the x vertex position in meters
	 * @param yo
	 *            the y vertex position in meters
	 * @param zo
	 *            the z vertex position in meters
	 * @param momentum
	 *            initial momentum in GeV/c
	 * @param theta
	 *            initial polar angle in degrees
	 * @param phi
	 *            initial azimuthal angle in degrees
	 * @param fixedZ
	 *            the fixed z value (meters) that terminates (or maxPathLength
	 *            if reached first)
	 * @param accuracy
	 *            the accuracy of the fixed z termination, in meters
	 * @param sMax
	 *            Max path length in meters. This determines the max number of
	 *            steps based on the step size. If a stopper is used, the
	 *            integration might terminate before all the steps are taken. A
	 *            reasonable value for CLAS is 8. meters
	 * @param stepSize
	 *            the initial step size in meters.
	 * @param relTolerance
	 *            the error tolerance as fractional diffs. Note it is a vector,
	 *            the same dimension of the problem, e.g., 6 for
	 *                     [x,y,z,vx,vy,vz]. It might be something like {1.0e-10,
	 *                     1.0e-10, 1.0e-10, 1.0e-8, 1.0e-8, 1.0e-8}
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] is the
	 *            min stepsize used (m), hdata[1] is the average stepsize used
	 *            (m), and hdata[2] is the max stepsize (m) used
	 * @return the trajectory of the particle
	 * @throws RungeKuttaException
	 */
	public SwimTrajectory swim(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
			final double fixedZ, double accuracy, double sMax, double stepSize, double relTolerance[], double hdata[])
			throws RungeKuttaException {
		if (momentum < MINMOMENTUM) {
//			System.err.println("Skipping low momentum swim (D)");
			return new SwimTrajectory(charge, xo, yo, zo, momentum, theta, phi);
		}

		//no field?
		if ((charge == 0) || (getProbe().isZeroField())) {
			// System.err.println("Skipping neutral or no field swim (D)");
			// just has to be proportional to velocity
			SwimTrajectory traj = new SwimTrajectory(charge, xo, yo, zo, momentum, theta, phi);
			double vz = momentum * Math.cos(Math.toRadians(theta));

			if (Math.abs(vz) > 1.0e-10) {
				double vp = momentum * Math.sin(Math.toRadians(theta));
				double vx = vp * Math.cos(Math.toRadians(phi));
				double vy = vp * Math.sin(Math.toRadians(phi));

				double time = (fixedZ - zo)/vz;
				double xf = xo + vx*time;
				double yf = xo + vy*time;

				traj.add(xf, yf, fixedZ, momentum, theta, phi);


			}
			return traj;
		}


		// normally we swim from small z to a larger z cutoff.
		// but we can handle either
		final boolean normalDirection = (fixedZ > zo);
		IStopper stopper = new DefaultZStopper(0, sMax, fixedZ, accuracy, normalDirection);

		SwimTrajectory traj = null;
		// First try

		traj = swim(charge, xo, yo, zo, momentum, theta, phi, stopper, 0, sMax, stepSize, relTolerance, hdata);

		// if we stopped because of max radius, we are done (never reached
		// target z)
		double finalPathLength = stopper.getFinalT();

		// System.err.println("** STOP PLEN (A) = " + finalPathLength);
		if (finalPathLength > sMax) {
			return traj;
		}

		// are we there yet?
		double lastY[] = traj.lastElement();
		double zlast = lastY[2];
		double del = Math.abs(zlast - fixedZ);
		int maxtry = 10;
		int count = 0;

		// set the step size to half the accuracy
		stepSize = accuracy / 2;

		//set the reverse stepsize to about 1/10 of distance to cover
//		int size = traj.size();
//		double zn = traj.get(size-2)[2];
//		double znp1 = traj.get(size-1)[2];
//		stepSize = Math.max(accuracy, Math.abs((znp1-zn)/10));


		// have to deal with the fact that the hdata array will reset so save
		// current values
		double oldHdata[] = new double[3];
		oldHdata[0] = hdata[0];
		oldHdata[1] = hdata[1] * traj.size(); // back to sum, not avg
		oldHdata[2] = hdata[2];

		while ((count < maxtry) && (del > accuracy)) {
			// last element had z beyond cutoff
			int lastIndex = traj.size() - 1;
			traj.remove(lastIndex);
			lastY = traj.lastElement();
			xo = lastY[0];
			yo = lastY[1];
			zo = lastY[2];
			double px = lastY[3];
			double py = lastY[4];
			double pz = lastY[5];

			// stepSize = Math.max(accuracy, Math.abs((fixedZ-zo)/10));

			// System.err.println("New start state = " + String.format("(%9.6f,
			// %9.6f, %9.6f) (%9.6f, %9.6f, %9.6f)", xo, yo, zo, px, py, pz));
			stopper = new DefaultZStopper(finalPathLength, sMax, fixedZ, accuracy, normalDirection);

			// momentum = traj.getFinalMomentum();
			theta = FastMath.acos2Deg(pz);
			phi = FastMath.atan2Deg(py, px);

			SwimTrajectory addTraj = swim(charge, xo, yo, zo, momentum, theta, phi, stopper, finalPathLength, sMax,
					stepSize, relTolerance, hdata);

			finalPathLength = stopper.getFinalT();
			// System.err.println("** STOP PLEN (B) = " + finalPathLength);

			hdata[0] = Math.min(oldHdata[0], hdata[0]);
			hdata[1] = hdata[1] * addTraj.size();
			hdata[1] = oldHdata[1] + hdata[1];
			hdata[2] = Math.max(oldHdata[2], hdata[2]);
			oldHdata[0] = hdata[0];
			oldHdata[1] = hdata[1];
			oldHdata[2] = hdata[2];

			// merge the trajectories
			traj.addAll(addTraj);
			lastY = traj.lastElement();
			zlast = lastY[2];
			del = Math.abs(zlast - fixedZ);
			count++;
			stepSize /= 2;
		} // while

		// now can get overall avg stepsize
		hdata[1] = hdata[1] / traj.size();
		return traj;
	}

	/**
	 * Swims a charged particle in a sector coordinate system. This swims to a
	 * fixed z value. This is for the trajectory mode, where you want to cache
	 * steps along the path. Uses an adaptive stepsize algorithm. THIS IS ONLY
	 * VALID IF THE FIELD IS A RotatedComnpositeField or RotatedCompositeProbe
	 *
	 * @param sector
	 *            the sector [1..6]
	 * @param charge
	 *            the charge: -1 for electron, 1 for proton, etc
	 * @param xo
	 *            the x vertex position in meters
	 * @param yo
	 *            the y vertex position in meters
	 * @param zo
	 *            the z vertex position in meters
	 * @param momentum
	 *            initial momentum in GeV/c
	 * @param theta
	 *            initial polar angle in degrees
	 * @param phi
	 *            initial azimuthal angle in degrees
	 * @param fixedZ
	 *            the fixed z value (meters) that terminates (or maxPathLength
	 *            if reached first)
	 * @param accuracy
	 *            the accuracy of the fixed z termination, in meters
	 * @param sMax
	 *            Max path length in meters. This determines the max number of
	 *            steps based on the step size. If a stopper is used, the
	 *            integration might terminate before all the steps are taken. A
	 *            reasonable value for CLAS is 8. meters
	 * @param stepSize
	 *            the initial step size in meters.
	 * @param relTolerance
	 *            the error tolerance as fractional diffs. Note it is a vector,
	 *            the same dimension of the problem, e.g., 6 for
	 *                     [x,y,z,vx,vy,vz]. It might be something like {1.0e-10,
	 *                     1.0e-10, 1.0e-10, 1.0e-8, 1.0e-8, 1.0e-8}
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] is the
	 *            min stepsize used (m), hdata[1] is the average stepsize used
	 *            (m), and hdata[2] is the max stepsize (m) used
	 * @return the trajectory of the particle
	 * @throws RungeKuttaException
	 */
	public SwimTrajectory sectorSwim(int sector, int charge, double xo, double yo, double zo, double momentum,
			double theta, double phi, final double fixedZ, double accuracy, double sMax, double stepSize,
			double relTolerance[], double hdata[]) throws RungeKuttaException {

		return sectorSwim(sector, charge, xo, yo, zo, momentum, theta, phi, fixedZ, accuracy, Double.POSITIVE_INFINITY, sMax, stepSize,
				relTolerance, hdata);
	}

	/**
	 * Swims a charged particle in a sector coordinate system. This swims to a
	 * fixed z value. This is for the trajectory mode, where you want to cache
	 * steps along the path. Uses an adaptive stepsize algorithm. THIS IS ONLY
	 * VALID IF THE FIELD IS A RotatedComnpositeField or RotatedCompositeProbe
	 *
	 * @param sector
	 *            the sector [1..6]
	 * @param charge
	 *            the charge: -1 for electron, 1 for proton, etc
	 * @param xo
	 *            the x vertex position in meters
	 * @param yo
	 *            the y vertex position in meters
	 * @param zo
	 *            the z vertex position in meters
	 * @param momentum
	 *            initial momentum in GeV/c
	 * @param theta
	 *            initial polar angle in degrees
	 * @param phi
	 *            initial azimuthal angle in degrees
	 * @param fixedZ
	 *            the fixed z value (meters) that terminates (or maxPathLength
	 *            if reached first)
	 * @param accuracy
	 *            the accuracy of the fixed z termination, in meters
	 * @param maxRad
	 *            the max radial coordinate NOTE: NO LONGER USED (here for
	 *                     backwards compatibility)
	 * @param sMax
	 *            Max path length in meters. This determines the max number of
	 *            steps based on the step size. If a stopper is used, the
	 *            integration might terminate before all the steps are taken. A
	 *            reasonable value for CLAS is 8. meters
	 * @param stepSize
	 *            the initial step size in meters.
	 * @param relTolerance
	 *            the error tolerance as fractional diffs. Note it is a vector,
	 *            the same dimension of the problem, e.g., 6 for
	 *                     [x,y,z,vx,vy,vz]. It might be something like {1.0e-10,
	 *                     1.0e-10, 1.0e-10, 1.0e-8, 1.0e-8, 1.0e-8}
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] is the
	 *            min stepsize used (m), hdata[1] is the average stepsize used
	 *            (m), and hdata[2] is the max stepsize (m) used
	 * @return the trajectory of the particle
	 * @throws RungeKuttaException
	 */
	public SwimTrajectory sectorSwim(int sector, int charge, double xo, double yo, double zo, double momentum,
			double theta, double phi, final double fixedZ, double accuracy, double maxRad, double sMax, double stepSize,
			double relTolerance[], double hdata[]) throws RungeKuttaException {


		// can only work for rotated composite fields or probes
		if (_probe instanceof RotatedCompositeProbe) {
			SwimTrajectory traj = null;
			try {
				traj = sectorSwimB(sector, charge, xo, yo, zo, momentum, theta, phi, fixedZ, accuracy, maxRad, sMax, stepSize,
						relTolerance, hdata);

			} catch (Exception e) {
				e.printStackTrace();
			}

			return traj;		}
		System.err.println("Can only call sectorSwim with a RotatedComposite Probe");
		System.exit(1);
		return null;
	}

	/**
	 * Swims a charged particle in a sector coordinate system. This swims to a
	 * fixed z value. This is for the trajectory mode, where you want to cache
	 * steps along the path. Uses an adaptive stepsize algorithm. THIS IS ONLY
	 * VALID IF THE FIELD IS A RotatedComnpositeField or RotatedCompositeProbe
	 *
	 * @param sector
	 *            the sector [1..6]
	 * @param charge
	 *            the charge: -1 for electron, 1 for proton, etc
	 * @param xo
	 *            the x vertex position in meters
	 * @param yo
	 *            the y vertex position in meters
	 * @param zo
	 *            the z vertex position in meters
	 * @param momentum
	 *            initial momentum in GeV/c
	 * @param theta
	 *            initial polar angle in degrees
	 * @param phi
	 *            initial azimuthal angle in degrees
	 * @param fixedZ
	 *            the fixed z value (meters) that terminates (or maxPathLength
	 *            if reached first)
	 * @param accuracy
	 *            the accuracy of the fixed z termination, in meters
	 * @param maxRad       maximum radial coordinate in meters
	 * @param sMax
	 *            Max path length in meters. This determines the max number of
	 *            steps based on the step size. If a stopper is used, the
	 *            integration might terminate before all the steps are taken. A
	 *            reasonable value for CLAS is 8. meters
	 * @param stepSize
	 *            the initial step size in meters.
	 * @param relTolerance
	 *            the error tolerance as fractional diffs. Note it is a vector,
	 *            the same dimension of the problem, e.g., 6 for
	 *                     [x,y,z,vx,vy,vz]. It might be something like {1.0e-10,
	 *                     1.0e-10, 1.0e-10, 1.0e-8, 1.0e-8, 1.0e-8}
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] is the
	 *            min stepsize used (m), hdata[1] is the average stepsize used
	 *            (m), and hdata[2] is the max stepsize (m) used
	 * @return the trajectory of the particle
	 * @throws RungeKuttaException
	 */
	//can only work for rotated composite fields or probes
	private SwimTrajectory sectorSwimB(int sector, int charge, double xo, double yo, double zo, double momentum,
			double theta, double phi, final double fixedZ, double accuracy, double maxRad, double sMax, double stepSize,
			double relTolerance[], double hdata[]) throws RungeKuttaException {


		if (!(_probe instanceof RotatedCompositeProbe)) {
			System.err.println("Can only call sectorSwim with a RotatedComposite Probe");
			System.exit(1);
			return null;
		}


		if (momentum < MINMOMENTUM) {
			return new SwimTrajectory(charge, xo, yo, zo, momentum, theta, phi);
		}

		//have we already stopped because of maxRad?
		if (Math.sqrt(xo*xo + yo*yo + zo*zo) > maxRad) {
			return null;
		}


		// normally we swim from small z to a larger z cutoff.
		// but we can handle either
		final boolean normalDirection = (fixedZ > zo);
		IStopper stopper = new DefaultZStopper(0, maxRad, sMax, fixedZ, accuracy, normalDirection);

		SwimTrajectory traj = null;
		// First try
		// SECTOR SWIM B
		try {
			traj = sectorSwimC(sector, charge, xo, yo, zo, momentum, theta, phi, stopper, 0, sMax, stepSize,
					relTolerance, hdata);

		} catch (Exception e) {
			e.printStackTrace();
		}
		// if we stopped because of max radius, we are done (never reached
		// target z)
		double finalPathLength = stopper.getFinalT();

		if (finalPathLength > sMax) {
			return traj;
		}

		// are we there yet?
		double lastY[] = traj.lastElement();
		double zlast = lastY[2];
		double del = Math.abs(zlast - fixedZ);
		int maxtry = 10;
		int count = 0;

		// set the step size to half the accuracy
		stepSize = accuracy / 2;


		// have to deal with the fact that the hdata array will reset so save
		// current values
		double oldHdata[] = new double[3];
		oldHdata[0] = hdata[0];
		oldHdata[1] = hdata[1] * traj.size(); // back to sum, not avg
		oldHdata[2] = hdata[2];

		while ((count < maxtry) && (del > accuracy)) {
			// last element had z beyond cutoff
			int lastIndex = traj.size() - 1;
			traj.remove(lastIndex);
			lastY = traj.lastElement();
			xo = lastY[0];
			yo = lastY[1];
			zo = lastY[2];
			// stepSize = Math.max(accuracy, Math.abs((fixedZ-zo)/10));
			double px = lastY[3];
			double py = lastY[4];
			double pz = lastY[5];

			stopper = new DefaultZStopper(finalPathLength, maxRad, sMax, fixedZ, accuracy, normalDirection);

			// momentum = traj.getFinalMomentum();
			theta = FastMath.acos2Deg(pz);
			phi = FastMath.atan2Deg(py, px);

			SwimTrajectory addTraj = sectorSwimC(sector, charge, xo, yo, zo, momentum, theta, phi, stopper, finalPathLength, sMax,
					stepSize, relTolerance, hdata);

			finalPathLength = stopper.getFinalT();

			hdata[0] = Math.min(oldHdata[0], hdata[0]);
			hdata[1] = hdata[1] * addTraj.size();
			hdata[1] = oldHdata[1] + hdata[1];
			hdata[2] = Math.max(oldHdata[2], hdata[2]);
			oldHdata[0] = hdata[0];
			oldHdata[1] = hdata[1];
			oldHdata[2] = hdata[2];

			// merge the trajectories
			traj.addAll(addTraj);
			lastY = traj.lastElement();
			zlast = lastY[2];
			del = Math.abs(zlast - fixedZ);
			count++;
			stepSize /= 2;
		} // while

		// now can get overall avg stepsize
		hdata[1] = hdata[1] / traj.size();
		return traj;
	}


	/**
	 * Swims a charged particle in a sector coordinate system. This swims to a
	 * fixed z value. This is for the trajectory mode, where you want to cache
	 * steps along the path. Uses an adaptive stepsize algorithm. THIS IS ONLY
	 * VALID IF THE FIELD IS A RotatedComnpositeField or RotatedCompositeProbe
	 *
	 * @param sector
	 *            the sector [1..6]
	 * @param charge
	 *            the charge: -1 for electron, 1 for proton, etc
	 * @param xo
	 *            the x vertex position in meters
	 * @param yo
	 *            the y vertex position in meters
	 * @param zo
	 *            the z vertex position in meters
	 * @param momentum
	 *            initial momentum in GeV/c
	 * @param theta
	 *            initial polar angle in degrees
	 * @param phi
	 *            initial azimuthal angle in degrees
	 * @param stopper
	 *            an optional object that can terminate the swimming based on
	 *            some condition
	 * @param s0
	 *            Starting path length in meters
	 * @param sMax
	 *            Max path length in meters. This determines the max number of
	 *            steps based on the step size. If a stopper is used, the
	 *            integration might terminate before all the steps are taken. A
	 *            reasonable value for CLAS is 8. meters
	 * @param stepSize
	 *            the initial step size in meters.
	 * @param relTolerance
	 *            the error tolerance as fractional diffs. Note it is a vector,
	 *            the same dimension of the problem, e.g., 6 for
	 *                     [x,y,z,vx,vy,vz]. It might be something like {1.0e-10,
	 *                     1.0e-10, 1.0e-10, 1.0e-8, 1.0e-8, 1.0e-8}
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] is the
	 *            min stepsize used (m), hdata[1] is the average stepsize used
	 *            (m), and hdata[2] is the max stepsize (m) used
	 * @return the trajectory of the particle
	 * @throws RungeKuttaException
	 */
	private SwimTrajectory sectorSwimC(int sector, int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
			IStopper stopper, double s0, double sMax, double stepSize, double relTolerance[], double hdata[])
			throws RungeKuttaException {


		//can only work for rotated composite fields or probes
		if (!(_probe instanceof RotatedCompositeProbe)) {
			System.err.println("Can only call sectorSwim with a RotatedComposite Probe");
			System.exit(1);
			return null;
		}

		// create the lists to hold the trajectory
		ArrayList<Double> s = new ArrayList<>(100);
		ArrayList<double[]> u = new ArrayList<>(100);

		// the the initial six vector
		double uo[] = initialState(xo, yo, zo, theta, phi);

		// create the trajectory container
		SwimTrajectory trajectory = new SwimTrajectory(charge, xo, yo, zo, momentum, theta, phi, 100);

		// the derivative
		SectorDerivative deriv = new SectorDerivative(sector, charge, momentum, (RotatedCompositeProbe)_probe);

		// integrate
		try {
			(new RungeKutta()).adaptiveStep(uo, s0, sMax, stepSize, s, u, deriv, stopper, _defaultTableau, relTolerance,
					hdata);
		} catch (RungeKuttaException e) {
			e.printStackTrace();
			trajectory = null;
			throw(e);
		}

		// now cycle through and get the save points
		for (double[] element : u) {
			trajectory.add(element);
		}

		return trajectory;
	}

	/**
	 * Swims a charged particle. This is for the trajectory mode, where you want
	 * to cache steps along the path. Uses an adaptive stepsize algorithm.
	 *
	 * @param charge
	 *            the charge: -1 for electron, 1 for proton, etc
	 * @param xo
	 *            the x vertex position in meters
	 * @param yo
	 *            the y vertex position in meters
	 * @param zo
	 *            the z vertex position in meters
	 * @param momentum
	 *            initial momentum in GeV/c
	 * @param theta
	 *            initial polar angle in degrees
	 * @param phi
	 *            initial azimuthal angle in degrees
	 * @param stopper
	 *            an optional object that can terminate the swimming based on
	 *            some condition
	 * @param sMax
	 *            Max path length in meters. This determines the max number of
	 *            steps based on the step size. If a stopper is used, the
	 *            integration might terminate before all the steps are taken. A
	 *            reasonable value for CLAS is 8. meters
	 * @param stepSize
	 *            the initial step size in meters.
	 * @param relTolerance
	 *            the error tolerance as fractional diffs. Note it is a vector,
	 *            the same dimension of the problem, e.g., 6 for
	 *                     [x,y,z,vx,vy,vz]. It might be something like {1.0e-10,
	 *                     1.0e-10, 1.0e-10, 1.0e-8, 1.0e-8, 1.0e-8}
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] is the
	 *            min stepsize used (m), hdata[1] is the average stepsize used
	 *            (m), and hdata[2] is the max stepsize (m) used
	 * @return the trajectory of the particle
	 * @throws RungeKuttaException
	 */

	public SwimTrajectory swim(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
			IStopper stopper, double sMax, double stepSize, double relTolerance[], double hdata[])
			throws RungeKuttaException {
		return swim(charge, xo, yo, zo, momentum, theta, phi, stopper, 0.0, sMax, stepSize, relTolerance, hdata);
	}

	/**
	 * Swims a charged particle. This is for the trajectory mode, where you want
	 * to cache steps along the path. Uses an adaptive stepsize algorithm.
	 *
	 * @param charge
	 *            the charge: -1 for electron, 1 for proton, etc
	 * @param xo
	 *            the x vertex position in meters
	 * @param yo
	 *            the y vertex position in meters
	 * @param zo
	 *            the z vertex position in meters
	 * @param momentum
	 *            initial momentum in GeV/c
	 * @param theta
	 *            initial polar angle in degrees
	 * @param phi
	 *            initial azimuthal angle in degrees
	 * @param stopper
	 *            an optional object that can terminate the swimming based on
	 *            some condition
	 * @param s0
	 *            Starting path length in meters
	 * @param sMax
	 *            Max path length in meters. This determines the max number of
	 *            steps based on the step size. If a stopper is used, the
	 *            integration might terminate before all the steps are taken. A
	 *            reasonable value for CLAS is 8. meters
	 * @param stepSize
	 *            the initial step size in meters.
	 * @param relTolerance
	 *            the error tolerance as fractional diffs. Note it is a vector,
	 *            the same dimension of the problem, e.g., 6 for
	 *                     [x,y,z,vx,vy,vz]. It might be something like {1.0e-10,
	 *                     1.0e-10, 1.0e-10, 1.0e-8, 1.0e-8, 1.0e-8}
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] is the
	 *            min stepsize used (m), hdata[1] is the average stepsize used
	 *            (m), and hdata[2] is the max stepsize (m) used
	 * @return the trajectory of the particle
	 * @throws RungeKuttaException
	 */
	public SwimTrajectory swim(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
			IStopper stopper, double s0, double sMax, double stepSize, double relTolerance[], double hdata[])
			throws RungeKuttaException {

		// create the lists to hold the trajectory
		ArrayList<Double> s = new ArrayList<>(100);
		ArrayList<double[]> u = new ArrayList<>(100);

		// the the initial six vector
		double uo[] = initialState(xo, yo, zo, theta, phi);

		// create the trajectory container
		SwimTrajectory trajectory = new SwimTrajectory(charge, xo, yo, zo, momentum, theta, phi, 100);

		// the derivative
		DefaultDerivative deriv = new DefaultDerivative(charge, momentum, _probe);

		// integrate
		(new RungeKutta()).adaptiveStep(uo, s0, sMax, stepSize, s, u, deriv, stopper, _defaultTableau, relTolerance,
				hdata);
		// now cycle through and get the save points
		for (double[] element : u) {
			trajectory.add(element);
		}

		return trajectory;
	}

	/**
	 * Swims a charged particle for the listener mode, where a callback is
	 * called for each advance of the integration Uses an adaptive stepsize
	 * algorithm.
	 *
	 * @param charge
	 *            the charge: -1 for electron, 1 for proton, etc
	 * @param xo
	 *            the x vertex position in meters
	 * @param yo
	 *            the y vertex position in meters
	 * @param zo
	 *            the z vertex position in meters
	 * @param momentum
	 *            initial momentum in GeV/c
	 * @param theta
	 *            initial polar angle in degrees
	 * @param phi
	 *            initial azimuthal angle in degrees
	 * @param stopper
	 *            an optional object that can terminate the swimming based on
	 *            some condition
	 * @param listener
	 *            a callback object that is called on every step
	 * @param maxPathLength
	 *            in meters. This determines the max number of steps based on
	 *            the step size. If a stopper is used, the integration might
	 *            terminate before all the steps are taken. A reasonable value
	 *            for CLAS is 8. meters
	 * @param stepSize
	 *            the initial step size in meters.
	 * @param relTolerance
	 *            the error tolerance as fractional diffs. Note it is a vector,
	 *            the same dimension of the problem, e.g., 6 for
	 *                      [x,y,z,vx,vy,vz]. It might be something like {1.0e-10,
	 *                      1.0e-10, 1.0e-10, 1.0e-8, 1.0e-8, 1.0e-8}
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] (m) is
	 *            the min stepsize used, hdata[1] (m) is the average stepsize
	 *            used, and hdata[2] (m) is the max stepsize used
	 * @return the total number of steps taken
	 * @throws RungeKuttaException
	 */
	public int swim(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
			IStopper stopper, IRkListener listener, double maxPathLength, double stepSize, double relTolerance[],
			double hdata[]) throws RungeKuttaException {

		if (momentum < MINMOMENTUM) {
			return 0;
		}

		// the the initial six vector
		double uo[] = initialState(xo, yo, zo, theta, phi);

		// Integrate
		DefaultDerivative deriv = new DefaultDerivative(charge, momentum, _probe);

		int nstep = (new RungeKutta()).adaptiveStep(uo, 0, maxPathLength, stepSize, deriv, stopper, listener,
				_defaultTableau, relTolerance, hdata);

		return nstep;
	}


	// create a straight line trajectory with just two points
	private SwimTrajectory straightLineTrajectory(GeneratedParticleRecord genPartRec, double pathLen) {

		double theta = genPartRec.getTheta();
		double phi = genPartRec.getPhi();
		double xo = genPartRec.getVertexX();
		double yo = genPartRec.getVertexY();
		double zo = genPartRec.getVertexZ();

		// the the initial six vector
		double uo[] = initialState(xo, yo, zo, theta, phi);

		double costheta = Math.cos(Math.toRadians(theta));
		double sintheta = Math.sin(Math.toRadians(theta));
		double cosphi = Math.cos(Math.toRadians(phi));
		double sinphi = Math.sin(Math.toRadians(phi));

		// all in meters
		double delz = pathLen * costheta;
		double delr = pathLen * sintheta;
		double delx = delr * cosphi;
		double dely = delr * sinphi;

		double uf[] = makeVector(xo + delx, yo + dely, zo + delz, uo[3], uo[4], uo[5]);

		// create the trajectory and add the two points
		SwimTrajectory traj = new SwimTrajectory(genPartRec, 2);
		traj.add(uo);
		traj.add(uf);

		return traj;
	}

	// create a straight line trajectory with just two points
	// stopping at a fixed z
	private SwimTrajectory straightLineTrajectoryFixedZ(GeneratedParticleRecord genPartRec, double zf) {

		double theta = genPartRec.getTheta();
		double phi = genPartRec.getPhi();
		double xo = genPartRec.getVertexX();
		double yo = genPartRec.getVertexY();
		double zo = genPartRec.getVertexZ();

		// the the initial six vector
		double uo[] = initialState(xo, yo, zo, theta, phi);

		double costheta = Math.cos(Math.toRadians(theta));
		double sintheta = Math.sin(Math.toRadians(theta));
		double cosphi = Math.cos(Math.toRadians(phi));
		double sinphi = Math.sin(Math.toRadians(phi));

		// all in meters
		double delz = costheta;
		double delr = sintheta;
		double delx = delr * cosphi;
		double dely = delr * sinphi;

		double uf[] = null;
		// use parameterization, get t from z
		if (Math.abs(delz) < 1.0e-20) {
			uf = makeVector(xo + delx, yo + dely, zo, uo[3], uo[4], uo[5]);
		} else {
			double tt = (zf - zo) / delz;
			double xf = xo + tt * delx;
			double yf = yo + tt * dely;
			uf = makeVector(xf, yf, zf, uo[3], uo[4], uo[5]);
		}

		// create the trajectory and add the two points
		SwimTrajectory traj = new SwimTrajectory(genPartRec, 2);
		traj.add(uo);
		traj.add(uf);

		return traj;
	}



	/**
	 * Get the state vector from the speed and angles
	 *
	 * @param xo
	 *            x coordinate in meters
	 * @param yo
	 *            y coordinate in meters
	 * @param zo
	 *            z coordinate in meters
	 * @param momentum
	 *            initial momentum in GeV/c
	 * @param theta
	 *            polar angle in degrees
	 * @param phi
	 *            azimuthal angle in degrees
	 * @return the corresponding state vector
	 */
	private static double[] initialState(double xo, double yo, double zo, double theta, double phi) {
		// initial values
		double costheta = Math.cos(Math.toRadians(theta));
		double sintheta = Math.sin(Math.toRadians(theta));
		double cosphi = Math.cos(Math.toRadians(phi));
		double sinphi = Math.sin(Math.toRadians(phi));

		// the the initial six vector
		double Q[] = new double[6];
		Q[0] = xo; // xo in meters
		Q[1] = yo; // yo in meters
		Q[2] = zo; // zo in meters
		Q[3] = sintheta * cosphi; // px/p
		Q[4] = sintheta * sinphi; // py/p
		Q[5] = costheta; // pz/p

		return Q;
	}

	// convenience method to make a vector
	private double[] makeVector(double x, double y, double z, double vx, double vy, double vz) {
		double v[] = new double[6];
		v[0] = x;
		v[1] = y;
		v[2] = z;
		v[3] = vx;
		v[4] = vy;
		v[5] = vz;
		return v;
	}
}
