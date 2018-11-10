package cnuphys.swim;

import java.util.ArrayDeque;
import java.util.ArrayList;

import org.jlab.clas.clas.math.FastMath;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.IMagField;
import cnuphys.magfield.MagneticField;
import cnuphys.rk4.ButcherTableau;
import cnuphys.rk4.IStopper;
import cnuphys.rk4.RungeKutta;
import cnuphys.rk4.RungeKuttaException;

public class Swimmer2 {
	
	private static final RungeKutta _rungeKutta = new RungeKutta();
	
	// We have different tableaus we can use for RK integration
	public static final ButcherTableau _defaultTableau = ButcherTableau.DORMAND_PRINCE;
	
	
	//object cache
	private ArrayDeque<double[]> _hdataCache = new ArrayDeque<>();
	
	//object cache
	private ArrayDeque<DefaultZStopper> _zstopperCache = new ArrayDeque<>();
	
	//object cache
	private ArrayDeque<DefaultDerivative> _derivCache = new ArrayDeque<>();

	// Field probe.
	// NOTE: methods of interest in FieldProbe takes a position in cm and
	// return a field in kG.This swim package works in SI (meters and Tesla)
	// so care has to be taken when using the field object
	private FieldProbe _probe;
	
	/**
	 * Swimmer2 constructor. Here we create a Swimmer that will use the given
	 * magnetic field.
	 * 
	 * @param field
	 *            interface into a magnetic field
	 */
	public Swimmer2() {
		_probe = FieldProbe.factory();
	}
	
	/**
	 * Create a swimmer specific to a magnetic field
	 * @param magneticField the magnetic field
	 */
	public Swimmer2(MagneticField magneticField) {
		_probe = FieldProbe.factory(magneticField);
	}
	
	/**
	 * Create a swimmer specific to a magnetic field
	 * @param magneticField the magnetic field
	 */
	public Swimmer2(IMagField magneticField) {
		_probe = FieldProbe.factory(magneticField);
	}
	
	/**
	 * Get the underlying field probe
	 * @return the probe
	 */
	public FieldProbe getProbe() {
		return _probe;
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
	 *            Max path length in meters.  A reasonable value for CLAS is 8. meters
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
			final double fixedZ, double accuracy, double sMax, double stepSize,
			double relTolerance[], double hdata[]) throws RungeKuttaException {

		if (momentum < Swimmer.MINMOMENTUM) {
			System.err.println("Skipping low momentum swim (D)");
			return new SwimTrajectory(charge, xo, yo, zo, momentum, theta, phi);
		}

		// normally we swim from small z to a larger z cutoff.
		// but we can handle either
		final boolean normalDirection = (fixedZ > zo);
		IStopper stopper = new DefaultZStopper(0, sMax, fixedZ, accuracy, normalDirection);

		SwimTrajectory traj = null;
		// First try

		traj = swim(charge, xo, yo, zo, momentum, theta, phi, stopper, 0, sMax, stepSize, relTolerance, hdata);

		// if we stopped because of max pathlength, we are done (never reached
		// target z)
		double finalPathLength = stopper.getFinalT();
		
	//	System.err.println("** STOP PLEN (A) = " + finalPathLength);
		if (finalPathLength > sMax) {
			return traj;
		}

		//got here, then went beyond target z
		int maxtry = 10;
		int count = 0;

		// set the step size to half the accuracy
		//stepSize = accuracy / 2;
		
		//set the reverse stepsize to about 1/10 of distance to cover
		int size = traj.size();
		double zn = traj.get(size-2)[2];
		double znp1 = traj.get(size-1)[2];
		stepSize = Math.max(accuracy, Math.abs((znp1-zn)/10));

		// have to deal with the fact that the hdata array will reset so save
		// current values
		double oldHdata[] = new double[3];
		oldHdata[0] = hdata[0];
		oldHdata[1] = hdata[1] * traj.size(); // back to sum, not avg
		oldHdata[2] = hdata[2];

		while ((count < maxtry) && (!stopper.terminateIntegration(finalPathLength, traj.lastElement()))) {
			// last element had z beyond cutoff
			int lastIndex = traj.size() - 1;
			traj.remove(lastIndex);
			double uf[] = traj.lastElement();
			
	//		System.err.println("New start state = " + String.format("(%9.6f, %9.6f, %9.6f) (%9.6f, %9.6f, %9.6f)", xo, yo, zo, px, py, pz));

			// momentum = traj.getFinalMomentum();
			theta = FastMath.acos2Deg(uf[5]);
			phi = FastMath.atan2Deg(uf[4], uf[3]);


			SwimTrajectory addTraj = swim(charge, uf[0], uf[1], uf[2], momentum, theta, phi, stopper, finalPathLength,
					sMax, stepSize, relTolerance, hdata);

			finalPathLength = stopper.getFinalT();
	//	    System.err.println("** STOP PLEN (B) = " + finalPathLength);

			hdata[0] = Math.min(oldHdata[0], hdata[0]);
			hdata[1] = hdata[1] * addTraj.size();
			hdata[1] = oldHdata[1] + hdata[1];
			hdata[2] = Math.max(oldHdata[2], hdata[2]);
			oldHdata[0] = hdata[0];
			oldHdata[1] = hdata[1];
			oldHdata[2] = hdata[2];

			// merge the trajectories
			traj.addAll(addTraj);
			count++;
			stepSize /= 2;
		} // while

		// now can get overall avg stepsize
		hdata[1] = hdata[1] / traj.size();
		return traj;
	}
	

	/**
	 * Swims a charged particle. This swims to a fixed z value. This is for the
	 * non-trajectory mode, where you only care about the final state. Uses an
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
	 * @param zTarget
	 *            the fixed z value (meters) that terminates (or maxPathLength
	 *            if reached first)
	 * @param accuracy
	 *            the accuracy of the fixed z termination, in meters
	 * @param sMax
	 *            Max path length in meters. The integration might
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
	 * @param uf the final state
	 * @return the trajectory of the particle
	 * @throws RungeKuttaException
	 */

	public int swim(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
			final double zTarget, double accuracy, double sMax, double stepSize, double maxStepSize,
			double relTolerance[], double hdata[], double uf[]) throws RungeKuttaException {
		
		int nStep = 0;
		
		if (momentum < Swimmer.MINMOMENTUM) {
			System.err.println("Skipping low momentum swim (D)");
			return 0;
		}

		DefaultDerivative deriv;
		if (_derivCache.isEmpty()) {
			deriv = new DefaultDerivative(charge, momentum, _probe);
		}
		else {
			deriv = _derivCache.pop();
			deriv.set(charge, momentum, _probe);
		}

		// normally we swim from small z to a larger z cutoff.
		// but we can handle either
		final boolean normalDirection = (zTarget > zo);
		
		DefaultZStopper stopper;
		if (_zstopperCache.isEmpty()) {
			stopper = new DefaultZStopper(0, sMax, zTarget, accuracy, normalDirection);
		}
		else {
			stopper = _zstopperCache.pop();
			stopper.setS0(0);
			stopper.setSMax(sMax);
			stopper.setTargetZ(zTarget);
			stopper.setAccuracy(accuracy);
			stopper.setNormalDirection(normalDirection);
		}


		nStep += swim(charge, xo, yo, zo, momentum, theta, phi, stopper, 0, sMax, stepSize, maxStepSize, relTolerance, hdata, uf);

		// if we stopped because of max path length, we are done (never reached target z)
		double finalPathLength = stopper.getFinalT();
		
	//	System.err.println("** STOP PLEN (A) = " + finalPathLength);
		if (finalPathLength > sMax) {
			return nStep;
		}

		int maxtry = 10;
		int count = 0;

		// set the step size to half the accuracy
		//stepSize = accuracy / 2;
		
		//set the reverse stepsize to about 1/10 of distance to cover
		double zn = uf[2];
		stepSize = Math.max(accuracy, Math.abs((zn - zTarget)/10));

		// have to deal with the fact that the hdata array will reset so save
		// current values
		
		double oldHdata[];
		if (_hdataCache.isEmpty()) {
			oldHdata = new double[3];
		}
		else {
			oldHdata = _hdataCache.pop();
		}

		oldHdata[0] = hdata[0];
		oldHdata[1] = hdata[1] * nStep; // back to sum, not average
		oldHdata[2] = hdata[2];

		while ((count < maxtry) && !stopper.terminateIntegration(finalPathLength, uf)) {
			
			maxStepSize = Math.max(1.0e-4, (Math.abs(uf[2] - zTarget)/2));

			theta = FastMath.acos2Deg(uf[5]);  //pz
			phi = FastMath.atan2Deg(uf[4], uf[3]); //(py, px)

			int newNStep = swim(charge, uf[0], uf[1], uf[2], 
					momentum, theta, phi, stopper, finalPathLength, sMax, 
					stepSize, maxStepSize, relTolerance, hdata, uf);
            nStep += newNStep;
			
			finalPathLength = stopper.getFinalT();
	//	    System.err.println("** STOP PLEN (B) = " + finalPathLength);

			hdata[0] = Math.min(oldHdata[0], hdata[0]);
			hdata[1] = hdata[1] * nStep;
			hdata[1] = oldHdata[1] + hdata[1];
			hdata[2] = Math.max(oldHdata[2], hdata[2]);
			oldHdata[0] = hdata[0];
			oldHdata[1] = hdata[1];
			oldHdata[2] = hdata[2];

			count++;
			stepSize /= 2;
		} // while
		
		
		_hdataCache.push(oldHdata);
		_zstopperCache.push(stopper);
		_derivCache.push(deriv);
		
		// now can get overall avg stepsize
		hdata[1] = hdata[1] / nStep;
		return nStep;
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
	 *            Max path length in meters. This determines the max number of steps based on
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
			IStopper stopper, double s0, double sMax, double stepSize, double relTolerance[], double hdata[])
					throws RungeKuttaException {

		// create the lists to hold the trajectory
		ArrayList<Double> s = new ArrayList<Double>(100);
		ArrayList<double[]> u = new ArrayList<double[]>(100);

		// the the initial six vector
		double uo[] = initialState(xo, yo, zo, theta, phi);

		// create the trajectory container
		SwimTrajectory trajectory = new SwimTrajectory(charge, xo, yo, zo, momentum, theta, phi, 100);

		// the derivative
		DefaultDerivative deriv = new DefaultDerivative(charge, momentum, _probe);

		// integrate
		_rungeKutta.adaptiveStep(uo, s0, sMax, stepSize, s, u, deriv, stopper, _defaultTableau,
				relTolerance, hdata);
		// now cycle through and get the save points
		for (int i = 0; i < u.size(); i++) {
			trajectory.add(u.get(i));
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
	 * @param s0
	 *            Starting path length in meters
	 * @param sMax
	 *            Max path length in meters. This determines the max number of steps based on
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
	 * @param uf space to hold the final state vector
	 * @return the number of steps taken
	 * @throws RungeKuttaException
	 */
	public int swim(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
			IStopper stopper, double s0, double sMax, double stepSize, double maxStepSize, double relTolerance[], double hdata[], double uf[])
					throws RungeKuttaException {

		// the the initial six vector
		double uo[] = initialState(xo, yo, zo, theta, phi);

		// the derivative
		
		DefaultDerivative deriv;
		if (_derivCache.isEmpty()) {
			deriv = new DefaultDerivative(charge, momentum, _probe);
		}
		else {
			deriv = _derivCache.pop();
			deriv.set(charge, momentum, _probe);
		}

		// integrate
		int nStep = _rungeKutta.adaptiveStep(uo, uf, s0, sMax, stepSize, maxStepSize, deriv, stopper, _defaultTableau,
				relTolerance, hdata);
		
		_derivCache.push(deriv);

		return nStep;
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

}
