package cnuphys.adaptiveSwim;

import cnuphys.adaptiveSwim.geometry.Cylinder;
import cnuphys.adaptiveSwim.geometry.Line;
import cnuphys.adaptiveSwim.geometry.Plane;
import cnuphys.adaptiveSwim.geometry.Sphere;
import cnuphys.lund.GeneratedParticleRecord;
import cnuphys.magfield.FastMath;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.IMagField;
import cnuphys.magfield.MagneticField;
import cnuphys.magfield.RotatedCompositeProbe;
import cnuphys.rk4.ButcherTableau;
import cnuphys.swim.DefaultDerivative;
import cnuphys.swim.SectorDerivative;
import cnuphys.swim.SwimTrajectory;

/**
 * A swimmer for adaptive stepsize integrators. These swimmers are not thread safe. Every thread that needs an
 * AdaptiveSwimmer should create its own.
 * 
 * @author heddle
 *
 */
public class AdaptiveSwimmer {
			
	//result status values
	/** The swim was a success */
	public static final int SWIM_SUCCESS = 0;
	
	/** A target, such as a target rho or z, was not reached
	 * before the swim was stopped for some other reason
	 */
	public static final int SWIM_TARGET_MISSED = -1;
	
	/**
	 * A swim was requested for a particle with extremely low
	 * momentum
	 */
	public static final int SWIM_BELOW_MIN_P = -2;
	
	// Min momentum to swim in GeV/c
	public static final double MINMOMENTUM = 5e-05;

	//max tries to reset if swam passed a target
	private static final int MAXTRIES = 11;


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
	 * @return the probe
	 */
	public FieldProbe getProbe() {
		return _probe;
	};

	//create a straight line for neutral particles
	private void straightLine(final double xo, final double yo, final double zo, final double momentum, double theta, double phi,
			final double sf, AdaptiveSwimResult result) {
		result.setFinalS(sf);
		result.setNStep(2);
		result.setStatus(SWIM_SUCCESS);

		double uo[] = new double[6];
		double uf[] = result.getUf();

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
	
	/**
	 * Swim using the current active field. This swimmer has no target. It will
	 * stop when the pathlength exceeds the maximum. For a more precise 
	 * pathlength swim, use swimS.
	 * @param charge in integer units of e
	 * @param xo the x vertex position in meters
	 * @param yo the y vertex position in meters
	 * @param zo the z vertex position in meters
	 * @param momentum the momentum in GeV/c
	 * @param theta the initial polar angle in degrees
	 * @param phi the initial azimuthal angle in degrees
	 * @param sf the final (max) value of the independent variable (pathlength) unless the integration is terminated by the stopper
	 * @param h0 the initial stepsize in meters
	 * @param eps the overall fractional tolerance (e.g., 1.0e-5)
	 * @param result the container for some of the swimming results
	 * @throws AdaptiveSwimException
	 */
	public void swim(final int charge, final double xo, final double yo, final double zo, final double momentum, double theta, double phi,
			final double sf, final double h0, final double eps, AdaptiveSwimResult result)
			throws AdaptiveSwimException {
		
		double uf[] = init(charge, xo, yo, zo, momentum, theta, phi, result);

		if (charge == 0) {
			straightLine(xo, yo, zo, momentum, theta, phi, sf, result);
			return;
		}

		double h = h0; //running stepsize
		
		if (momentum < MINMOMENTUM) {
			System.err.println("Skipping low momentum fixed rho swim");
			result.setNStep(0);
			result.setFinalS(0);
			
			//give this a result status of -2
			result.setStatus(SWIM_BELOW_MIN_P);
			return;
		}
		
		//create the derivative object
		DefaultDerivative deriv = new DefaultDerivative(charge, momentum, _probe);
		AdaptiveDefaultStopper stopper = new AdaptiveDefaultStopper(uf, sf, result.getTrajectory());
		ButcherAdvance advancer = new ButcherAdvance(6, ButcherTableau.CASH_KARP);

		int ns = AdaptiveSwimUtilities.driver(h, deriv, stopper, advancer, eps, uf);
		result.setFinalS(stopper.getS());
		stopper.copy(stopper.getU(), uf);
		result.setNStep(ns);
		result.setStatus(SWIM_SUCCESS);
	}
	
	
	/**
	 * Swim to a definite pathlength. If extreme accuracy is not needed, use swim instead.
	 * @param charge in integer units of e
	 * @param xo the x vertex position in meters
	 * @param yo the y vertex position in meters
	 * @param zo the z vertex position in meters
	 * @param momentum the momentum in GeV/c
	 * @param theta the initial polar angle in degrees
	 * @param phi the initial azimuthal angle in degrees
	 * @param accuracy the requested accuracy for the target pathlength in meters
	 * @param sf the final (max) value of the independent variable (pathlength) unless the integration is terminated by the stopper
	 * @param h0 the initial stepsize in meters
	 * @param eps the overall fractional tolerance (e.g., 1.0e-5)
	 * @param result the container for some of the swimming results
	 * @throws AdaptiveSwimException
	 */
	public void swimS(final int charge, final double xo, final double yo, final double zo, final double momentum, double theta, double phi,
			final double accuracy, final double sf, final double h0, final double eps, AdaptiveSwimResult result)
			throws AdaptiveSwimException {
		
		double uf[] = init(charge, xo, yo, zo, momentum, theta, phi, result);
		
		if (charge == 0) {
			straightLine(xo, yo, zo, momentum, theta, phi, sf, result);
			return;
		}
		
		double h = h0; //running stepsize
		
		//bail if below minimum momentum
		if (belowMinimumMomentum(momentum, result)) {
			return;
		}
				
		double del = Double.POSITIVE_INFINITY;
		int count = 0;
		int ns = 0;

		//create the derivative object
		DefaultDerivative deriv = new DefaultDerivative(charge, momentum, _probe);

		//the stopper will stop if we come within range of the target z or if the
		//pathlength reaches sf
		AdaptiveSStopper stopper = new AdaptiveSStopper(uf, sf, accuracy, result.getTrajectory());

		//use a half-step advancer
	//	RK4HalfStepAdvance advancer = new RK4HalfStepAdvance(6);

		ButcherAdvance advancer = new ButcherAdvance(6, ButcherTableau.CASH_KARP);
		while (count < MAXTRIES) {
			
			if ((stopper.getS() + h) > stopper.getSmax()) {
				h = stopper.getSmax()-stopper.getS();
				if (h < 0) {
					break;
				}
			}

			//this will try to swim us the rest of the way to sf, but hopefully
			//we'll get stopped earlier because we reach the target z
			
			try {
				ns += AdaptiveSwimUtilities.driver(h, deriv, stopper, advancer, eps, uf);
			} catch (AdaptiveSwimException e) {
				//put in a message that allows us to reproduce the track
			}
						
			if ((stopper.getS()) > stopper.getSmax()) {
				break;
			}
			
			del = Math.abs((stopper.getS()) - stopper.getSmax());
			
			//succeed?
			if (del < accuracy) {
				break;
			}
						
			count++;
			
			h /= 2;
		}
		
		result.setFinalS(stopper.getS());
		stopper.copy(stopper.getU(), uf);
		result.setNStep(ns);

		//if we are at the target s, the status = 0
		//if we have reached sf, the status = -1
		if (del < accuracy) {
			result.setStatus(SWIM_SUCCESS);
		} else {
			result.setStatus(SWIM_TARGET_MISSED);
		}
	}

	/**
	 * Swims a charged particle in a sector coordinate system. This swims to a fixed
	 * z value. THIS IS ONLY VALID IF THE
	 * FIELD IS A RotatedComnpositeField or RotatedCompositeProbe
	 * 
	 * @param sector       the sector [1..6]
	 * @param charge in integer units of e
	 * @param xo the x vertex position in meters
	 * @param yo the y vertex position in meters
	 * @param zo the z vertex position in meters
	 * @param momentum the momentum in GeV/c
	 * @param theta the initial polar angle in degrees
	 * @param phi the initial azimuthal angle in degrees
	 * @param targetZ the target z in meters
	 * @param accuracy the requested accuracy for the target z in meters
	 * @param sf the final (max) value of the independent variable (pathlength) unless the integration is terminated by the stopper
	 * @param h0 the initial stepsize in meters
	 * @param eps the overall fractional tolerance (e.g., 1.0e-5)
	 * @param result the container for some of the swimming results
	 * @throws AdaptiveSwimException
	 */
	public void sectorSwimZ(final int sector, final int charge, final double xo, final double yo, final double zo, final double momentum, double theta, double phi,
			final double targetZ, final double accuracy, final double sf, final double h0, final double eps, AdaptiveSwimResult result)
			throws AdaptiveSwimException {
		
		if (!(_probe instanceof RotatedCompositeProbe)) {
			System.err.println("Can only call sectorSwim with a RotatedComposite Probe");
			throw new AdaptiveSwimException("Bad Probe: Must call a sectorSwim method with a RotatedCompositeProbe");
		}
		
		double uf[] = init(charge, xo, yo, zo, momentum, theta, phi, result);
		double h = h0; //running stepsize
		
		//bail if below minimum momentum
		if (belowMinimumMomentum(momentum, result)) {
			return;
		}
				
		double del = Double.POSITIVE_INFINITY;
		int count = 0;
		int ns = 0;

		//create the derivative object
		SectorDerivative deriv = new SectorDerivative(sector, charge, momentum, (RotatedCompositeProbe) _probe);

		//the stopper will stop if we come within range of the target z or if the
		//pathlength reaches sf
		AdaptiveZStopper stopper = new AdaptiveZStopper(uf, sf, targetZ, accuracy, result.getTrajectory());

		//use a half-step advancer
	//	RK4HalfStepAdvance advancer = new RK4HalfStepAdvance(6);

		ButcherAdvance advancer = new ButcherAdvance(6, ButcherTableau.CASH_KARP);
		while (count < MAXTRIES) {
			
			if ((stopper.getS() + h) > stopper.getSmax()) {
				h = stopper.getSmax()-stopper.getS();
				if (h < 0) {
					break;
				}
			}

			//this will try to swim us the rest of the way to sf, but hopefully
			//we'll get stopped earlier because we reach the target z
			
			try {
				ns += AdaptiveSwimUtilities.driver(h, deriv, stopper, advancer, eps, uf);
			} catch (AdaptiveSwimException e) {
				//put in a message that allows us to reproduce the track
			}
						
			if ((stopper.getS()) > stopper.getSmax()) {
				break;
			}
			
			double zlast = stopper.getU()[2];
			del = Math.abs(zlast - targetZ);
			
			//succeed?
			if (del < accuracy) {
				break;
			}
						
			count++;
			
			h /= 2;
		}
		
		result.setFinalS(stopper.getS());
		stopper.copy(stopper.getU(), uf);
		result.setNStep(ns);

		//if we are at the target z, the status = 0
		//if we have reached sf, the status = -1
		if (del < accuracy) {
			result.setStatus(SWIM_SUCCESS);
		} else {
			result.setStatus(SWIM_TARGET_MISSED);
		}
		
	};
	
	/**
	 * Swim to a fixed z using the current active field
	 * @param charge in integer units of e
	 * @param xo the x vertex position in meters
	 * @param yo the y vertex position in meters
	 * @param zo the z vertex position in meters
	 * @param momentum the momentum in GeV/c
	 * @param theta the initial polar angle in degrees
	 * @param phi the initial azimuthal angle in degrees
	 * @param targetZ the target z in meters
	 * @param accuracy the requested accuracy for the target z in meters
	 * @param sf the final (max) value of the independent variable (pathlength) unless the integration is terminated by the stopper
	 * @param h0 the initial stepsize in meters
	 * @param eps the overall fractional tolerance (e.g., 1.0e-5)
	 * @param result the container for some of the swimming results
	 * @throws AdaptiveSwimException
	 */
	public void swimZ(final int charge, final double xo, final double yo, final double zo, final double momentum, double theta, double phi,
			final double targetZ, final double accuracy, final double sf, final double h0, final double eps, AdaptiveSwimResult result)
			throws AdaptiveSwimException {
		
		double uf[] = init(charge, xo, yo, zo, momentum, theta, phi, result);
		double h = h0; //running stepsize
				
		//bail if below minimum momentum
		if (belowMinimumMomentum(momentum, result)) {
			return;
		}
				
		double del = Double.POSITIVE_INFINITY;
		int count = 0;
		int ns = 0;

		//create the derivative object
		DefaultDerivative deriv = new DefaultDerivative(charge, momentum, _probe);

		//the stopper will stop if we come within range of the target z or if the
		//pathlength reaches sf
		AdaptiveZStopper stopper = new AdaptiveZStopper(uf, sf, targetZ, accuracy, result.getTrajectory());

		//use a half-step advancer
	//	RK4HalfStepAdvance advancer = new RK4HalfStepAdvance(6);

		ButcherAdvance advancer = new ButcherAdvance(6, ButcherTableau.CASH_KARP);
		while (count < MAXTRIES) {
			
			if ((stopper.getS() + h) > stopper.getSmax()) {
				h = stopper.getSmax()-stopper.getS();
				if (h < 0) {
					break;
				}
			}

			//this will try to swim us the rest of the way to sf, but hopefully
			//we'll get stopped earlier because we reach the target z
			
			try {
				ns += AdaptiveSwimUtilities.driver(h, deriv, stopper, advancer, eps, uf);
			} catch (AdaptiveSwimException e) {
				//put in a message that allows us to reproduce the track
			}
						
			if ((stopper.getS()) > stopper.getSmax()) {
				break;
			}
			
			double zlast = stopper.getU()[2];
			del = Math.abs(zlast - targetZ);
			
			//succeed?
			if (del < accuracy) {
				break;
			}
						
			count++;
			
			h /= 2;
		}
		
		result.setFinalS(stopper.getS());
		stopper.copy(stopper.getU(), uf);
		result.setNStep(ns);

		//if we are at the target z, the status = 0
		//if we have reached sf, the status = -1
		if (del < accuracy) {
			result.setStatus(SWIM_SUCCESS);
		} else {
			result.setStatus(SWIM_TARGET_MISSED);
		}
	}

	/**
	 * Swim to a fixed rho using the current active field
	 * @param charge in integer units of e
	 * @param xo the x vertex position in meters
	 * @param yo the y vertex position in meters
	 * @param zo the z vertex position in meters
	 * @param momentum the momentum in GeV/c
	 * @param theta the initial polar angle in degrees
	 * @param phi the initial azimuthal angle in degrees
	 * @param targetRho the target rho in meters
	 * @param accuracy the requested accuracy for the target rho in meters
	 * @param sf the final (max) value of the independent variable (pathlength) unless the integration is terminated by the stopper
	 * @param h0 the initial stepsize in meters
	 * @param eps the overall fractional tolerance (e.g., 1.0e-5)
	 * @param result the container for some of the swimming results
	 * @throws AdaptiveSwimException
	 */
	public void swimRho(final int charge, final double xo, final double yo, final double zo, final double momentum, double theta, double phi,
			final double targetRho, final double accuracy, final double sf, final double h0, final double eps, AdaptiveSwimResult result)
			throws AdaptiveSwimException {
		
		double uf[] = init(charge, xo, yo, zo, momentum, theta, phi, result);
		double h = h0; //running stepsize
				
		//bail if below minimum momentum
		if (belowMinimumMomentum(momentum, result)) {
			return;
		}
		
		double del = Double.POSITIVE_INFINITY;
		int count = 0;
		int ns = 0;

		//create the derivative object
		DefaultDerivative deriv = new DefaultDerivative(charge, momentum, _probe);

		//the stopper will stop if we come within range of the target rho or if the
		//pathlength reaches sf
		AdaptiveRhoStopper stopper = new AdaptiveRhoStopper(uf, sf, targetRho, accuracy, result.getTrajectory());

		//use a half-step advancer
	//	RK4HalfStepAdvance advancer = new RK4HalfStepAdvance(6);

		ButcherAdvance advancer = new ButcherAdvance(6, ButcherTableau.CASH_KARP);
		while (count < MAXTRIES) {
			
			if ((stopper.getS() + h) > stopper.getSmax()) {
				h = (stopper.getSmax()-stopper.getS())/4;
				if (h < 0) {
					break;
				}
			}

			//this will try to swim us the rest of the way to sf, but hopefully
			//we'll get stopped earlier because we reach the target rho
			
			try {
				ns += AdaptiveSwimUtilities.driver(h, deriv, stopper, advancer, eps, uf);
			} catch (AdaptiveSwimException e) {
				//put in a message that allows us to reproduce the track
			}
						
			if ((stopper.getS()) > stopper.getSmax()) {
				break;
			}
			
			double rholast = FastMath.hypot(stopper.getU()[0], stopper.getU()[1]);
			del = Math.abs(rholast - targetRho);
			
			//succeed?
			if (del < accuracy) {
				break;
			}
						
			count++;
			
			h /= 2;
		}
		
		result.setFinalS(stopper.getS());
		stopper.copy(stopper.getU(), uf);
		result.setNStep(ns);

		//if we are at the target rho, the status = 0
		//if we have reached sf, the status = -1
		if (del < accuracy) {
			result.setStatus(SWIM_SUCCESS);
		} else {
			result.setStatus(SWIM_TARGET_MISSED);
		}
	}


	/**
	 * Swim to a plane using the current active field
	 * @param charge in integer units of e
	 * @param xo the x vertex position in meters
	 * @param yo the y vertex position in meters
	 * @param zo the z vertex position in meters
	 * @param momentum the momentum in GeV/c
	 * @param theta the initial polar angle in degrees
	 * @param phi the initial azimuthal angle in degrees
	 * @param targetPlane the target plane
	 * @param accuracy the requested accuracy for the final distance to the plane in meters
	 * @param sf the final (max) value of the independent variable (pathlength) unless the integration is terminated by the stopper
	 * @param h0 the initial stepsize in meters
	 * @param eps the overall fractional tolerance (e.g., 1.0e-5)
	 * @param result the container for some of the swimming results
	 * @throws AdaptiveSwimException
	 */
	public void swimPlane(final int charge, final double xo, final double yo, final double zo, final double momentum, double theta, double phi,
			Plane targetPlane, final double accuracy, final double sf, final double h0, final double eps, AdaptiveSwimResult result)
			throws AdaptiveSwimException {
		
		double uf[] = init(charge, xo, yo, zo, momentum, theta, phi, result);
		double h = h0; //running stepsize
				
		//bail if below minimum momentum
		if (belowMinimumMomentum(momentum, result)) {
			return;
		}
				
		double del = Double.POSITIVE_INFINITY;
		int count = 0;
		int ns = 0;

		//create the derivative object
		DefaultDerivative deriv = new DefaultDerivative(charge, momentum, _probe);

		//the stopper will stop if we come within range of the target plane or the
		//pathlength reaches sf
		AdaptivePlaneStopper stopper = new AdaptivePlaneStopper(uf, sf, targetPlane, accuracy, result.getTrajectory());

		ButcherAdvance advancer = new ButcherAdvance(6, ButcherTableau.CASH_KARP);
		while (count < MAXTRIES) {
			
			if ((stopper.getS() + h) > stopper.getSmax()) {
				h = (stopper.getSmax()-stopper.getS())/4;
				if (h < 0) {
					break;
				}
			}

			//this will try to swim us the rest of the way to sf, but hopefully
			//we'll get stopped earlier because we reach the target plane
			
			try {
				ns += AdaptiveSwimUtilities.driver(h, deriv, stopper, advancer, eps, uf);
			} catch (AdaptiveSwimException e) {
				//put in a message that allows us to reproduce the track
			}
						
			if ((stopper.getS()) > stopper.getSmax()) {
				break;
			}
			
			del = Math.abs(targetPlane.distance(stopper.getU()[0], stopper.getU()[1], stopper.getU()[2]));
			
			//succeed?
			if (del < accuracy) {
				break;
			}
						
			count++;
			
			h /= 2;
		}
		
		result.setFinalS(stopper.getS());
		stopper.copy(stopper.getU(), uf);
		result.setNStep(ns);

		//if we are at the target rho, the status = 0
		//if we have reached sf, the status = -1
		if (del < accuracy) {
			result.setStatus(SWIM_SUCCESS);
		} else {
			result.setStatus(SWIM_TARGET_MISSED);
		}
	}
	
	/**
	 * Swim to an arbitrary sphere using the current active field
	 * @param charge in integer units of e
	 * @param xo the x vertex position in meters
	 * @param yo the y vertex position in meters
	 * @param zo the z vertex position in meters
	 * @param momentum the momentum in GeV/c
	 * @param theta the initial polar angle in degrees
	 * @param phi the initial azimuthal angle in degrees
	 * @param targetSphere the target sphere
	 * @param accuracy the requested accuracy for the target rho in meters
	 * @param sf the final (max) value of the independent variable (pathlength) unless the integration is terminated by the stopper
	 * @param h0 the initial stepsize in meters
	 * @param eps the overall fractional tolerance (e.g., 1.0e-5)
	 * @param result the container for some of the swimming results
	 * @throws AdaptiveSwimException
	 */
	public void swimSphere(final int charge, final double xo, final double yo, final double zo, final double momentum, double theta, double phi,
			Sphere targetSphere, final double accuracy, final double sf, final double h0, final double eps, AdaptiveSwimResult result)
			throws AdaptiveSwimException {
		
		double uf[] = init(charge, xo, yo, zo, momentum, theta, phi, result);
		double h = h0; //running stepsize
				
		//bail if below minimum momentum
		if (belowMinimumMomentum(momentum, result)) {
			return;
		}
				
		double del = Double.POSITIVE_INFINITY;
		int count = 0;
		int ns = 0;

		//create the derivative object
		DefaultDerivative deriv = new DefaultDerivative(charge, momentum, _probe);

		//the stopper will stop if we come within range of the target cylinder or the
		//pathlength reaches sf
		AdaptiveSphereStopper stopper = new AdaptiveSphereStopper(uf, sf, targetSphere, accuracy, result.getTrajectory());

		ButcherAdvance advancer = new ButcherAdvance(6, ButcherTableau.CASH_KARP);
		while (count < MAXTRIES) {
			
			if ((stopper.getS() + h) > stopper.getSmax()) {
				h = (stopper.getSmax()-stopper.getS())/4;
				if (h < 0) {
					break;
				}
			}

			//this will try to swim us the rest of the way to sf, but hopefully
			//we'll get stopped earlier because we reach the target plane
			
			try {
				ns += AdaptiveSwimUtilities.driver(h, deriv, stopper, advancer, eps, uf);
			} catch (AdaptiveSwimException e) {
				//put in a message that allows us to reproduce the track
			}
						
			if ((stopper.getS()) > stopper.getSmax()) {
				break;
			}
			
			del = Math.abs(targetSphere.distance(stopper.getU()[0], stopper.getU()[1], stopper.getU()[2]));
			
			//succeed?
			if (del < accuracy) {
				break;
			}
						
			count++;
			
			h /= 2;
		}
		
		result.setFinalS(stopper.getS());
		stopper.copy(stopper.getU(), uf);
		result.setNStep(ns);

		//if we are at the target rho, the status = 0
		//if we have reached sf, the status = -1
		if (del < accuracy) {
			result.setStatus(SWIM_SUCCESS);
		} else {
			result.setStatus(SWIM_TARGET_MISSED);
		}
	}
	
	

	/**
	 * Swim to an arbitrary infinitely long cylinder using the current active field
	 * @param charge in integer units of e
	 * @param xo the x vertex position in meters
	 * @param yo the y vertex position in meters
	 * @param zo the z vertex position in meters
	 * @param momentum the momentum in GeV/c
	 * @param theta the initial polar angle in degrees
	 * @param phi the initial azimuthal angle in degrees
	 * @param targetCylinder the target cylinder
	 * @param accuracy the requested accuracy for the target rho in meters
	 * @param sf the final (max) value of the independent variable (pathlength) unless the integration is terminated by the stopper
	 * @param h0 the initial stepsize in meters
	 * @param eps the overall fractional tolerance (e.g., 1.0e-5)
	 * @param result the container for some of the swimming results
	 * @throws AdaptiveSwimException
	 */
	public void swimCylinder(final int charge, final double xo, final double yo, final double zo, final double momentum, double theta, double phi,
			Cylinder targetCylinder, final double accuracy, final double sf, final double h0, final double eps, AdaptiveSwimResult result)
			throws AdaptiveSwimException {
		
		double uf[] = init(charge, xo, yo, zo, momentum, theta, phi, result);
		double h = h0; //running stepsize
				
		//bail if below minimum momentum
		if (belowMinimumMomentum(momentum, result)) {
			return;
		}
				
		double del = Double.POSITIVE_INFINITY;
		int count = 0;
		int ns = 0;

		//create the derivative object
		DefaultDerivative deriv = new DefaultDerivative(charge, momentum, _probe);

		//the stopper will stop if we come within range of the target cylinder or the
		//pathlength reaches sf
		AdaptiveCylinderStopper stopper = new AdaptiveCylinderStopper(uf, sf, targetCylinder, accuracy, result.getTrajectory());

		ButcherAdvance advancer = new ButcherAdvance(6, ButcherTableau.CASH_KARP);
		while (count < MAXTRIES) {
			
			if ((stopper.getS() + h) > stopper.getSmax()) {
				h = (stopper.getSmax()-stopper.getS())/4;
				if (h < 0) {
					break;
				}
			}

			//this will try to swim us the rest of the way to sf, but hopefully
			//we'll get stopped earlier because we reach the target plane
			
			try {
				ns += AdaptiveSwimUtilities.driver(h, deriv, stopper, advancer, eps, uf);
			} catch (AdaptiveSwimException e) {
				//put in a message that allows us to reproduce the track
			}
						
			if ((stopper.getS()) > stopper.getSmax()) {
				break;
			}
			
			del = Math.abs(targetCylinder.distance(stopper.getU()[0], stopper.getU()[1], stopper.getU()[2]));
			
			//succeed?
			if (del < accuracy) {
				break;
			}
						
			count++;
			
			h /= 2;
		}
		
		result.setFinalS(stopper.getS());
		stopper.copy(stopper.getU(), uf);
		result.setNStep(ns);

		//if we are at the target rho, the status = 0
		//if we have reached sf, the status = -1
		if (del < accuracy) {
			result.setStatus(SWIM_SUCCESS);
		} else {
			result.setStatus(SWIM_TARGET_MISSED);
		}
	}
	

	/**
	 * Swim to an arbitrary infinitely long line using the current active field
	 * @param charge in integer units of e
	 * @param xo the x vertex position in meters
	 * @param yo the y vertex position in meters
	 * @param zo the z vertex position in meters
	 * @param momentum the momentum in GeV/c
	 * @param theta the initial polar angle in degrees
	 * @param phi the initial azimuthal angle in degrees
	 * @param targetLine the target line
	 * @param accuracy the requested accuracy for the target rho in meters
	 * @param sf the final (max) value of the independent variable (pathlength) unless the integration is terminated by the stopper
	 * @param h0 the initial stepsize in meters
	 * @param eps the overall fractional tolerance (e.g., 1.0e-5)
	 * @param result the container for some of the swimming results
	 * @throws AdaptiveSwimException
	 */
	public void swimLine(final int charge, final double xo, final double yo, final double zo, final double momentum, double theta, double phi,
			Line targetLine, final double accuracy, final double sf, final double h0, final double eps, AdaptiveSwimResult result)
			throws AdaptiveSwimException {
		
		double uf[] = init(charge, xo, yo, zo, momentum, theta, phi, result);
		double h = h0; //running stepsize
				
		//bail if below minimum momentum
		if (belowMinimumMomentum(momentum, result)) {
			return;
		}
				
		double del = Double.POSITIVE_INFINITY;
		int count = 0;
		int ns = 0;

		//create the derivative object
		DefaultDerivative deriv = new DefaultDerivative(charge, momentum, _probe);

		//the stopper will stop if we come within range of the target cylinder or the
		//pathlength reaches sf
		AdaptiveLineStopper stopper = new AdaptiveLineStopper(uf, sf, targetLine, accuracy, result.getTrajectory());

		ButcherAdvance advancer = new ButcherAdvance(6, ButcherTableau.CASH_KARP);
		while (count < MAXTRIES) {
			
			if ((stopper.getS() + h) > stopper.getSmax()) {
				h = (stopper.getSmax()-stopper.getS())/4;
				if (h < 0) {
					break;
				}
			}

			//this will try to swim us the rest of the way to sf, but hopefully
			//we'll get stopped earlier because we reach the target plane
			
			try {
				ns += AdaptiveSwimUtilities.driver(h, deriv, stopper, advancer, eps, uf);
			} catch (AdaptiveSwimException e) {
				//put in a message that allows us to reproduce the track
			}
						
			if ((stopper.getS()) > stopper.getSmax()) {
				break;
			}
			
			del = targetLine.distance(stopper.getU()[0], stopper.getU()[1], stopper.getU()[2]);
			
			//succeed?
			if (del < accuracy) {
				break;
			}
						
			count++;
			
			h /= 2;
		}
		
		result.setFinalS(stopper.getS());
		stopper.copy(stopper.getU(), uf);
		result.setNStep(ns);

		//if we are at the target rho, the status = 0
		//if we have reached sf, the status = -1
		if (del < accuracy) {
			result.setStatus(SWIM_SUCCESS);
		} else {
			result.setStatus(SWIM_TARGET_MISSED);
		}
	}
	
	
	//init common to many swimmers
	private double[] init(final int charge, final double xo, final double yo, final double zo, final double momentum, double theta, double phi,
			AdaptiveSwimResult result) {
		
		//clear old trajectory
		if (result.getTrajectory() != null) {
			result.getTrajectory().clear();
			
			result.getTrajectory().setGeneratedParticleRecord(new GeneratedParticleRecord(charge, xo, yo, zo, momentum, theta, phi));
		}
		
		//store initial values
		result.setInitialValues(charge, xo, yo, zo, momentum, theta, phi);
		
		// set uf (in the result container) to the starting state vector
		double uf[] = result.getUf();
		stuffU(uf, xo, yo, zo, momentum, theta, phi);
		return uf;
	}
	
	private void stuffU(double[] u, final double xo, final double yo, final double zo, final double momentum, double theta, double phi) {
		double thetaRad = Math.toRadians(theta);
		double phiRad = Math.toRadians(phi);
		double sinTheta = Math.sin(thetaRad);

		double tx = sinTheta*Math.cos(phiRad); //px/p
		double ty = sinTheta*Math.sin(phiRad); //py/p
		double tz = Math.cos(thetaRad); //pz/p
		
		// set uf (in the result container) to the starting state vector
		u[0] = xo;
		u[1] = yo;
		u[2] = zo;
		u[3] = tx;
		u[4] = ty;
		u[5] = tz;
	}
	
	//allows us to bail if below min momentum
	private boolean belowMinimumMomentum(double p, AdaptiveSwimResult result) {
		if (p < MINMOMENTUM) {
			System.err.println("Skipping low momentum fixed rho swim");
			result.setNStep(0);
			result.setFinalS(0);
			
			//give this a result status of -2
			result.setStatus(SWIM_BELOW_MIN_P);
			return true;
		}
		else {
			return false;
		}

	}

}
