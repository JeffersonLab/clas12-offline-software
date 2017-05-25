package cnuphys.swim;

import java.util.ArrayList;

import cnuphys.lund.GeneratedParticleRecord;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.IField;
import cnuphys.magfield.MagneticField;
import cnuphys.rk4.ButcherTableau;
import cnuphys.rk4.IRkListener;
import cnuphys.rk4.IStopper;
import cnuphys.rk4.RungeKutta;
import cnuphys.rk4.RungeKuttaException;

/**
 * Handles the swimming of a particle through a magnetic field.
 * 
 * @author heddle
 *
 */
public final class Swimmer {

	// Speed of light in m/s
	public static final double C = 299792458.0; // m/s

	// Min momentum to swim in GeV/c
	public static final double MINMOMENTUM = 5e-05;

	// We have different tableaus we can use for RK integration
	public static final ButcherTableau _defaultTableau = ButcherTableau.DORMAND_PRINCE;

	/**
	 * In swimming routines that require a tolerance vector, this is a
	 * reasonable one to use for CLAS.
	 */
	private static double _eps = 1.0e-5;
//	private static double _eps = 1.0e-4;
	public static double CLAS_Tolerance[];

	static {
		setCLASTolerance(_eps);
	}
	

	// Field getter.
	// NOTE: the method of interest in IField takes a position in cm
	// and returns a field in kG.This swim package works in SI (meters and
	// Tesla)
	// so care has to be taken when using the field object
	private IField _field;
	
	/**
	 * Swimmer constructor. Here we create a Swimmer that will use the given
	 * magnetic field.
	 * 
	 * @param field
	 *            interface into a magnetic field
	 */
	public Swimmer(IField field) {
		FieldProbe probe = FieldProbe.factory(field);
		_field = (probe != null) ? probe : field;
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

		// if no magnetic field or no charge, then simple straight line tracks.
		// the path will consist of just two points
		if ((_field == null) || (charge == 0)) {
			GeneratedParticleRecord genPartRec = new GeneratedParticleRecord(charge, xo, yo, zo, momentum, theta, phi);
			return straightLineTrajectory(genPartRec, maxPathLength);
		}

		if (momentum < MINMOMENTUM) {
			System.err.println("Skipping low momentum swim (A)");
			return new SwimTrajectory(charge, xo, yo, zo, momentum, theta, phi);
		}

		// cycle is the number of advances per save
		int cycle = (int) (distanceBetweenSaves / stepSize);
		cycle = Math.max(2, cycle);

		// max number of possible steps--may not use all of them
		int ntotal = (int) (maxPathLength / stepSize); // number steps
		int nsave = ntotal / cycle; // aprox number saves

		// the the initial six vector
		double uo[] = intitialState(xo, yo, zo, theta, phi);

		// storage for time and state
		double s[] = new double[ntotal];
		double u[][] = new double[6][ntotal];

		// create the trajectory container
		SwimTrajectory trajectory = new SwimTrajectory(charge, xo, yo, zo, momentum, theta, phi, nsave);

		// Integrate
		DefaultDerivative deriv = new DefaultDerivative(charge, momentum, _field);
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
	 * Swims a Lund particle with a built in stopper for the maximum value of
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
	 * @param rmax
	 *            the max radial coordinate in meters.
	 * @param maxPathLength
	 *            in meters. This is used to compute the maximum number of
	 *            integration steps.
	 * @param stepSize
	 *            either the initial stepsize or the constant step size based on
	 *            the integration method. Units are meters.
	 * @param distanceBetweenSaves
	 *            this should be bigger than stepSize. It is approximately the
	 *            distance between "saves" where the point is saved in a
	 *            trajectory for later drawing.
	 * @return the trajectory of the particle
	 */
	public SwimTrajectory swim(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
			final double rmax, double maxPathLength, double stepSize, double distanceBetweenSaves) {

		IStopper stopper = new DefaultSwimStopper(rmax);

		return swim(charge, xo, yo, zo, momentum, theta, phi, stopper, maxPathLength, stepSize, distanceBetweenSaves);
	}

	/**
	 * Swims a charged particle. This is for the listener mode, where a callback
	 * is called for each advance of the integration Uses a fixed stepsize
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
	 *            the uniform step size in meters.
	 * @return the total number of steps taken
	 */
	public int swim(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
			IStopper stopper, IRkListener listener, double maxPathLength, double stepSize) {

		if (momentum < MINMOMENTUM) {
			System.err.println("Skipping low momentum swim (B)");
			return 0;
		}

		// the the initial six vector
		double uo[] = intitialState(xo, yo, zo, theta, phi);

		// Integrate
		DefaultDerivative deriv = new DefaultDerivative(charge, momentum, _field);
		return (new RungeKutta()).uniformStep(uo, 0, maxPathLength, stepSize, deriv, stopper, listener);
	}

	/**
	 * Swims a particle with a built it stopper for the maximum value of the
	 * radial coordinate. This is for the trajectory mode, where you want to
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
	 * @param fixedZ
	 *            the fixed z value (meters) that terminates (or maxPathLength
	 *            if reached first)
	 * @param accuracy
	 *            the accuracy of the fixed z termination, in meters
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
			final double fixedZ, final double accuracy, double maxR, double maxPathLength, double stepSize,
			double distanceBetweenSaves) {

		// normally we swim from small z to a larger z cutoff.
		// but we can handle either
		final boolean normalDirection = (fixedZ > zo);
		DefaultSwimStopper stopper = zStopper(maxR, fixedZ, normalDirection);

		// if no magnetic field or no charge, then simple straight line tracks.
		// the path will consist of just two points
		if ((_field == null) || (charge == 0)) {
			System.out.println("Original Swimmer, straight line field is null: " + (_field == null) + "  charge: " + charge);
			GeneratedParticleRecord genPartRec = new GeneratedParticleRecord(charge, xo, yo, zo, momentum, theta, phi);
			return straightLineTrajectoryFixedZ(genPartRec, fixedZ);

			// fix for fixed z

		}

		if (momentum < MINMOMENTUM) {
			System.err.println("Skipping low momentum swim (C)");
			return new SwimTrajectory(charge, xo, yo, zo, momentum, theta, phi);

		}

		// our first attempt
		SwimTrajectory trajectory = swim(charge, xo, yo, zo, momentum, theta, phi, stopper, maxPathLength, stepSize,
				distanceBetweenSaves);

		// if we stopped because of max radius, we are done (never reached
		// target z)
		if (trajectory.getFinalR() > maxR) {
			return trajectory;
		}

		// are we there yet?
		double lastY[] = trajectory.lastElement();
		double zlast = lastY[2];
		double del = Math.abs(zlast - fixedZ);
		int maxtry = 10;
		int count = 0;

		// set the step size to half the accuracy
		stepSize = stepSize / 10;

		while ((count < maxtry) && (del > accuracy)) {
			// last element had z beyond cutoff
			int lastIndex = trajectory.size() - 1;
			
			trajectory.remove(lastIndex);
			lastY = trajectory.lastElement();
			xo = lastY[0];
			yo = lastY[1];
			zo = lastY[2];
			double px = lastY[3];
			double py = lastY[4];
			double pz = lastY[5];

			stopper = zStopper(maxR, fixedZ, normalDirection);

			theta = MagneticField.acos2Deg(pz);
			phi = MagneticField.atan2Deg(py, px);

			SwimTrajectory addTraj = swim(charge, xo, yo, zo, momentum, theta, phi, stopper, maxPathLength, stepSize,
					distanceBetweenSaves);

			// merge the trajectories
			trajectory.addAll(addTraj);
			lastY = trajectory.lastElement();
			zlast = lastY[2];
			del = Math.abs(zlast - fixedZ);
			count++;
			stepSize = stepSize / 10;
		} // while

		return trajectory;
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
	 * @param maxR
	 *            the max radial coordinate
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
	 *            [x,y,z,vx,vy,vz]. It might be something like {1.0e-10,
	 *            1.0e-10, 1.0e-10, 1.0e-8, 1.0e-8, 1.0e-8}
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] is the
	 *            min stepsize used (m), hdata[1] is the average stepsize used
	 *            (m), and hdata[2] is the max stepsize (m) used
	 * @return the trajectory of the particle
	 * @throws RungeKuttaException
	 */
	public SwimTrajectory swim(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
			final double fixedZ, double accuracy, double maxR, double maxPathLength, double stepSize,
			double relTolerance[], double hdata[]) throws RungeKuttaException {

		if (momentum < MINMOMENTUM) {
			System.err.println("Skipping low momentum swim (D)");
			return new SwimTrajectory(charge, xo, yo, zo, momentum, theta, phi);
		}

		// normally we swim from small z to a larger z cutoff.
		// but we can handle either
		final boolean normalDirection = (fixedZ > zo);
		DefaultSwimStopper stopper = zStopper(maxR, fixedZ, normalDirection);

		SwimTrajectory traj = null;
		// First try

		traj = swim(charge, xo, yo, zo, momentum, theta, phi, stopper, maxPathLength, stepSize, relTolerance, hdata);

		// if we stopped because of max radius, we are done (never reached
		// target z)
		if (traj.getFinalR() > maxR) {
			return traj;
		}

		// if we stopped because of max path, we are done (never reached target
		// z)
		if (stopper.getFinalT() > maxPathLength) {
			//System.err.println("Reached max path length");
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
			double px = lastY[3];
			double py = lastY[4];
			double pz = lastY[5];

			stopper = zStopper(maxR, fixedZ, normalDirection);

			// momentum = traj.getFinalMomentum();
			theta = MagneticField.acos2Deg(pz);
			phi = MagneticField.atan2Deg(py, px);


			SwimTrajectory addTraj = swim(charge, xo, yo, zo, momentum, theta, phi, stopper, maxPathLength, stepSize,
					relTolerance, hdata);

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

	// convenience for creating a z stopper
	private DefaultSwimStopper zStopper(double maxR, final double fixedZ, final boolean normalDirection) {
		DefaultSwimStopper stopper = new DefaultSwimStopper(maxR) {
			@Override
			public boolean stopIntegration(double s, double[] y) {
				double zz = y[2];
				boolean done;
				if (normalDirection) {
					done = (zz > fixedZ);
				} else {
					done = (zz < fixedZ);
				}
				return done || super.stopIntegration(s, y);
			}

		};
		return stopper;
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
	 *            [x,y,z,vx,vy,vz]. It might be something like {1.0e-10,
	 *            1.0e-10, 1.0e-10, 1.0e-8, 1.0e-8, 1.0e-8}
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] is the
	 *            min stepsize used (m), hdata[1] is the average stepsize used
	 *            (m), and hdata[2] is the max stepsize (m) used
	 * @return the trajectory of the particle
	 * @throws RungeKuttaException
	 */
	public SwimTrajectory swim(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
			IStopper stopper, double maxPathLength, double stepSize, double relTolerance[], double hdata[])
					throws RungeKuttaException {

		// create the lists to hold the trajectory
		ArrayList<Double> s = new ArrayList<Double>(100);
		ArrayList<double[]> u = new ArrayList<double[]>(100);

		// the the initial six vector
		double uo[] = intitialState(xo, yo, zo, theta, phi);

		// create the trajectory container
		SwimTrajectory trajectory = new SwimTrajectory(charge, xo, yo, zo, momentum, theta, phi, 100);

		// the derivative
		DefaultDerivative deriv = new DefaultDerivative(charge, momentum, _field);

		// integrate
		(new RungeKutta()).adaptiveStep(uo, 0, maxPathLength, stepSize, s, u, deriv, stopper, _defaultTableau,
				relTolerance, hdata);
		// now cycle through and get the save points
		for (int i = 0; i < u.size(); i++) {
			trajectory.add(u.get(i));
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
	 *            [x,y,z,vx,vy,vz]. It might be something like {1.0e-10,
	 *            1.0e-10, 1.0e-10, 1.0e-8, 1.0e-8, 1.0e-8}
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
			System.err.println("Skipping low momentum swim (E)");
			return 0;
		}

		// the the initial six vector
		double uo[] = intitialState(xo, yo, zo, theta, phi);

		// Integrate
		DefaultDerivative deriv = new DefaultDerivative(charge, momentum, _field);

		int nstep = (new RungeKutta()).adaptiveStep(uo, 0, maxPathLength, stepSize, deriv, stopper, listener,
				_defaultTableau, relTolerance, hdata);

		return nstep;
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
	 * @param maxPathLength
	 *            in meters. This determines the max number of steps based on
	 *            the step size. If a stopper is used, the integration might
	 *            terminate before all the steps are taken. A reasonable value
	 *            for CLAS is 8. meters
	 * @param stepSize
	 *            the initial step size in meters.
	 * @param tolerance
	 *            the required accuracy, something like 1.0e07
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] (m) is
	 *            the min stepsize used, hdata[1] (m) is the average stepsize
	 *            used, and hdata[2] (m) is the max stepsize used Swims a Lund
	 *            particle. This is for the trajectory mode, where you want to
	 *            cache steps along the path. Uses an adaptive stepsize
	 *            algorithm.
	 * @throws RungeKuttaException
	 */
	public SwimTrajectory swim(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
			IStopper stopper, double maxPathLength, double stepSize, double tolerance, double hdata[])
					throws RungeKuttaException {

		// construct an appropriate yscale array for CLAS12
		double yscale[] = { 1., 1., 1., 1., 1., 1., };

		// create the lists to hold the trajectory
		ArrayList<Double> t = new ArrayList<Double>(100);
		ArrayList<double[]> y = new ArrayList<double[]>(100);

		// the the initial six vector
		double uo[] = intitialState(xo, yo, zo, theta, phi);

		// create the trajectory container
		SwimTrajectory trajectory = new SwimTrajectory(charge, xo, yo, zo, momentum, theta, phi, 100);

		// Integrate
		DefaultDerivative deriv = new DefaultDerivative(charge, momentum, _field);

		// integrate
		(new RungeKutta()).adaptiveStep(uo, 0, maxPathLength, stepSize, t, y, deriv, stopper, _defaultTableau,
				tolerance, yscale, hdata);
		// now cycle through and get the save points

		for (int i = 0; i < y.size(); i++) {
			trajectory.add(y.get(i));
		}

		return trajectory;
	}

	/**
	 * Swims a Lund particle for the listener mode, where a callback is called
	 * for each advance of the integration Uses an adaptive stepsize algorithm.
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
	 * @param tolerance
	 *            the required accuracy, something like 1.0e07
	 * @param hdata
	 *            if not null, should be double[3]. Upon return, hdata[0] (m) is
	 *            the min stepsize used, hdata[1] (m) is the average stepsize
	 *            used, and hdata[2] (m) is the max stepsize used
	 * @return the total number of steps taken
	 * @throws RungeKuttaException
	 */
	public int swim(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
			IStopper stopper, IRkListener listener, double maxPathLength, double stepSize, double tolerance,
			double hdata[]) throws RungeKuttaException {

		if (momentum < MINMOMENTUM) {
			System.err.println("Skipping low momentum swim (F)");
			return 0;
		}

		// construct an appropriate yscale array for CLAS12
		double yscale[] = { 1., 1., 1., 3.e8, 3.e8, 3.e8 };

		// int ntotal = (int) (maxPathLength / stepSize); // number steps

		// the the initial six vector
		double uo[] = intitialState(xo, yo, zo, theta, phi);

		// Integrate
		DefaultDerivative deriv = new DefaultDerivative(charge, momentum, _field);

		int nstep = (new RungeKutta()).adaptiveStep(uo, 0, maxPathLength, stepSize, deriv, stopper, listener,
				_defaultTableau, tolerance, yscale, hdata);

		return nstep;
	}

	/**
	 * Swims a Lund particle with a built it stopper for the maximum value of
	 * the radial coordinate radial coordinate. This is for the listener method,
	 * where a callback is called for each advance of the integration Uses a
	 * fixed stepsize algorithm.
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
	 * @param listener
	 *            a callback object that is called on every step
	 * @param rmax
	 *            the max radial coordinate in meters.
	 * @param maxPathLength
	 *            in meters. This determines the max number of steps based on
	 *            the step size. all the steps are taken. A reasonable value for
	 *            CLAS is 8. meters
	 * @param stepSize
	 *            the uniform step size in meters.
	 * @return the total number of steps taken
	 */
	public int swim(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
			IRkListener listener, double rmax, double maxPathLength, double stepSize) {

		IStopper stopper = new DefaultSwimStopper(rmax);

		return swim(charge, xo, yo, zo, momentum, theta, phi, stopper, listener, maxPathLength, stepSize);
	}

	// create a straight line trajectory with just two points
	private SwimTrajectory straightLineTrajectory(GeneratedParticleRecord genPartRec, double pathLen) {

		double theta = genPartRec.getTheta();
		double phi = genPartRec.getPhi();
		double xo = genPartRec.getVertexX();
		double yo = genPartRec.getVertexY();
		double zo = genPartRec.getVertexZ();

		// the the initial six vector
		double uo[] = intitialState(xo, yo, zo, theta, phi);

		double costheta = MagneticField.cos(Math.toRadians(theta));
		double sintheta = MagneticField.sin(Math.toRadians(theta));
		double cosphi = MagneticField.cos(Math.toRadians(phi));
		double sinphi = MagneticField.sin(Math.toRadians(phi));

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
		double uo[] = intitialState(xo, yo, zo, theta, phi);

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
	 * @param q
	 *            the charge: -1 for electron, 1 for proton, etc
	 * @param xo
	 *            the x start position in meters
	 * @param yo
	 *            the y start position in meters
	 * @param zo
	 *            the z start position in meters
	 * @param px
	 *            the x start momentum in GeV/c
	 * @param py
	 *            the y start momentum in GeV/c
	 * @param pz
	 *            the z start momentum in GeV/c
	 */
	public static SwimTrajectory swimBackwardsToVertex(IField field, int q, double xo, double yo, double zo, double px, double py,
			double pz) {
		// reverse the direction
		px = -px;
		py = -py;
		pz = -pz;
		q = -q;

		// get the angles
		double pt = MagneticField.hypot(px, py);
		double p = MagneticField.hypot(pt, pz);
		double theta = MagneticField.acos2Deg(pz);
		double phi = MagneticField.atan2Deg(py, px);

		// accuracy to z = 0 (m)
		double ztarget = 0;
		double accuracy = 1.0e-5;
		double stepSize = 5e-4; // m

		Swimmer swimmer = new Swimmer(field);
		SwimTrajectory traj = null;

		double hdata[] = new double[3];

		try {
			traj = swimmer.swim(q, xo, yo, zo, p, theta, phi, ztarget, accuracy, 10, 10, stepSize,
					Swimmer.CLAS_Tolerance, hdata);
		} catch (RungeKuttaException e) {
			e.printStackTrace();
		}
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
	private static double[] intitialState(double xo, double yo, double zo, double theta, double phi) {
		// initial values
		double costheta = MagneticField.cos(Math.toRadians(theta));
		double sintheta = MagneticField.sin(Math.toRadians(theta));
		double cosphi = MagneticField.cos(Math.toRadians(phi));
		double sinphi = MagneticField.sin(Math.toRadians(phi));

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
