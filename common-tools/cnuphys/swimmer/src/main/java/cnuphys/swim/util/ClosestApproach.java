package cnuphys.swim.util;

import java.util.Arrays;
import java.util.Random;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariatePointValuePair;

import cnuphys.adaptiveSwim.AdaptiveSwimException;
import cnuphys.adaptiveSwim.AdaptiveSwimResult;
import cnuphys.adaptiveSwim.AdaptiveSwimmer;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimmer;
import cnuphys.swimtest.RandomData;

public class ClosestApproach {
	
	//default values
	private static double defRelTol = 1.4e-4;
	private static double defAbsTol = 1.4e-6;
	private static int defMaxIter = 1000;
	private static int defMaxEval = 2000;


	// basic euclidian distance
	private static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
		return Math.sqrt(distanceSq(x1, y1, z1, x2, y2, z2));
	}
	
	// basic euclidian distance squared
	private static double distanceSq(double x1, double y1, double z1, double x2, double y2, double z2) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		double dz = z2 - z1;

		return dx * dx + dy * dy + dz * dz;
	}


	// generate a random LundId from a small set. Used by testing
	// to create a random Lund particle.
	private static LundId randomLundId(Random rand, boolean negative) {
		int neg[] = { 11, 13, -211, -213 }; // negative particles
		int pos[] = { 2212, 211, 213, 321 }; // positive particles

		if (negative) {
			return LundSupport.getInstance().get(neg[rand.nextInt(neg.length)]);
		} else {
			return LundSupport.getInstance().get(pos[rand.nextInt(pos.length)]);
		}
	}

	/**
	 * Get the relativistic beta (v/c)
	 * 
	 * @param momentum the momentum
	 * @param mass     the mass in the same units as the momentum
	 * @return beta the velocity v/c
	 */
	private static double beta(double momentum, double mass) {
		double energy = Math.sqrt(momentum * momentum + mass * mass);

		double gamma = energy / mass;
		double beta = Math.sqrt(1.0 - 1.0 / (gamma * gamma));
		return beta;
	}
		
	//interp, after already performed binary search to get index
	private static double interp(double a[], double t[], double tval, int index) {
		// unlikely, but maybe we are exactly on a value
		if (index >= 0) {
			return a[index];
		}
		if (index == t.length) {
			return a[index-1];
		}
		
		index = -(index + 1); // now the insertion point.

		if (index < 0) {
			System.err.println("Probem in linearInterp (A) in ClosestApproach index = " + index);
			return Double.NaN;
		}

		if (index == t.length) {
			return a[index-1];
		}

		if (index > t.length) {
			System.err.println("Probem in linearInterp (B) in ClosestApproach index = " + index);
			return Double.NaN;
		}

		double t0 = t[index - 1];
		double t1 = t[index];
		double fract = (tval - t0) / (t1 - t0);

		if ((fract < -0.00001) || (fract > 1.00001)) {
			System.err.println("Probem in linearInterp (C) in ClosestApproach t = " + t);
			return Double.NaN;
		}

		int indexm1 = index - 1;
		return a[indexm1] + fract * (a[index] - a[indexm1]);
	}


	// does a linear interpolation at time t returning the interpolates xyz val in
	// result[3]
	private static void linearInterp(double x[], double y[], double z[], double t[], double tval, double result[]) {

		int index = Arrays.binarySearch(t, tval);

		result[0] = interp(x, t, tval, index);
		result[1] = interp(y, t, tval, index);
		result[2] = interp(z, t, tval, index);

	}
	
	// does a linear interpolation at time t returning the interpolates xyz val in
	// result[6]
	private static void linearInterp(double x[], double y[], double z[], double tx[], double ty[], double tz[],
			double t[], double tval, double result[]) {

		int index = Arrays.binarySearch(t, tval);

		result[0] = interp(x, t, tval, index);
		result[1] = interp(y, t, tval, index);
		result[2] = interp(z, t, tval, index);
		result[3] = interp(tx, t, tval, index);
		result[4] = interp(ty, t, tval, index);
		result[5] = interp(tz, t, tval, index);
	}


	/**
	 * Compute the distance of closest approach in time. (not the over-all geometric
	 * closest approach of the trajectories, but the closest they get at any time.)
	 * 
	 * @param traj1   one trajectory
	 * @param traj2   other trajectory
	 * @param p1      momentum of traj 1 in GeV/c
	 * @param p2      momentum of traj 2 in GeV/c
	 * @param lundId2 Lund (PDG) id for traj1. If not known, set to <= 0. This will
	 *                be used to compute the speed from the momentum. If not known,
	 *                speed of light assumed.
	 * @param lundId2 Lund (PDG) id for traj2, as for traj1
	 * @param deltaT  time in ns between start of traj1 and start of traj2. Can be
	 *                negative, in which case traj2 starts first.
	 * 
	 * @return the object holding the results
	 */

	public static ClosestApproachResult closestApproach(SwimTrajectory traj1, SwimTrajectory traj2, double p1, double p2, int lundId1,
			int lundId2, double deltaT) {

		return closestApproach(traj1, traj2, p1, p2, lundId1, lundId2, deltaT, defRelTol, defAbsTol, defMaxIter, defMaxEval);
	}
	
	/**
	 * 
	 * Compute the distance of closest approach in time. (not the over-all geometric
	 * closest approach of the trajectories, but the closest they get at any time.)
	 * 
	 * @param traj1   one trajectory
	 * @param traj2   other trajectory
	 * @param p1      momentum of traj 1 in GeV/c
	 * @param p2      momentum of traj 2 in GeV/c
	 * @param lundId2 Lund (PDG) id for traj1. If not known, set to <= 0. This will
	 *                be used to compute the speed from the momentum. If not known,
	 *                speed of light assumed.
	 * @param lundId2 Lund (PDG) id for traj2, as for traj1
	 * @param deltaT  time in ns between start of traj1 and start of traj2. Can be
	 *                negative, in which case traj2 starts first.
	 * @param relTol  relative tolerance
	 * @param absTol  absoluteToleration
	 * @param maxIter max number of iterations
	 * @param maxEval max number of evaluations
	 * @return the object holding the results
	 */
	public static ClosestApproachResult closestApproach(SwimTrajectory traj1, SwimTrajectory traj2, double p1, double p2, int lundId1,
			int lundId2, double deltaT, double relTol, double absTol, int maxIter, int maxEval) {

		LundId lid1 = LundSupport.getInstance().get(lundId1);
		LundId lid2 = LundSupport.getInstance().get(lundId2);

		// a zero mass will result in beta = 1
		double mass1 = (lid1 != null) ? lid1.getMass() : 0;
		double mass2 = (lid2 != null) ? lid2.getMass() : 0;
		return closestApproach(traj1, traj2, p1, p2, mass1, mass2, deltaT, relTol, absTol, maxIter, maxEval);
	}


	/**
	 * Compute the distance of closest approach in time. (not the over-all geometric
	 * closest approach of the trajectories, but the closest they get at any time.)
	 * 
	 * @param traj1   one trajectory
	 * @param traj2   other trajectory
	 * @param p1      momentum of traj 1 in GeV/c
	 * @param p2      momentum of traj 2 in GeV/c
	 * @param m1      mass of particle 1 in GeV/c^2
	 * @param m1      mass of particle 1 in GeV/c^2
	 * @param deltaT  time in ns between start of traj1 and start of traj2. Can be
	 *                negative, in which case traj2 starts first.
	 * 
	 * @return the object holding the results
	 */

	public static ClosestApproachResult closestApproach(SwimTrajectory traj1, SwimTrajectory traj2, double p1, double p2, double m1,
			double m2, double deltaT) {

		return closestApproach(traj1, traj2, p1, p2, m1, m2, deltaT, defRelTol, defAbsTol, defMaxIter, defMaxEval);
	}
	
	/**
	 * Compute the distance of closest approach in time. (not the over-all geometric
	 * closest approach of the trajectories, but the closest they get at any time.)
	 * 
	 * @param traj1   one trajectory
	 * @param traj2   other trajectory
	 * @param p1      momentum of traj 1 in GeV/c
	 * @param p2      momentum of traj 2 in GeV/c
	 * @param m1      mass of particle 1 in GeV/c^2
	 * @param m1      mass of particle 1 in GeV/c^2
	 * @param deltaT  time in ns between start of traj1 and start of traj2. Can be
	 *                negative, in which case traj2 starts first.
	 * @param relTol  relative tolerance
	 * @param absTol  absoluteToleration
	 * @param maxIter max number of iterations
	 * @param maxEval max number of evaluations
	 * @return the object holding the results
	 */
	public static ClosestApproachResult closestApproach(SwimTrajectory traj1, SwimTrajectory traj2, double p1, double p2, double m1,
			double m2, double deltaT, double relTol, double absTol, int maxIter, int maxEval) {

		double beta1 = 1;
		double beta2 = 1;

		if (m1 > 1.0e-9) {
			beta1 = beta(p1, m1);
		}
		if (m2 > 1.0e-9) {
			beta2 = beta(p2, m2);
		}

		return closestApproach(traj1, traj2, beta1, beta2, deltaT, relTol, absTol, maxIter, maxEval);
	}

	

	/**
	 * Compute the distance of closest approach in time. (not the over-all geometric
	 * closest approach of the trajectories, but the closest they get at any time.)
	 * 
	 * @param traj1   one trajectory
	 * @param traj2   other trajectory
	 * @param beta1   the v/c for trajectory 1
	 * @param beta2   the v/c for trajectory 2
	 * @param deltaT  time in ns between start of traj1 and start of traj2. Can be
	 *                negative, in which case traj2 starts first.
	 * 
	 * @return the object holding the results
	 */
	public static ClosestApproachResult closestApproach(SwimTrajectory traj1, SwimTrajectory traj2, double beta1, double beta2,
			double deltaT) {

		return closestApproach(traj1, traj2, beta1, beta2, deltaT, defRelTol, defAbsTol, defMaxIter, defMaxEval);
	}

	
	/**
	 * Compute the distance of closest approach in time. (not the over-all geometric
	 * closest approach of the trajectories, but the closest they get at any time.)
	 * 
	 * @param traj1   one trajectory
	 * @param traj2   other trajectory
	 * @param beta1   the v/c for trajectory 1
	 * @param beta2   the v/c for trajectory 2
	 * @param deltaT  time in ns between start of traj1 and start of traj2. Can be
	 *                negative, in which case traj2 starts first.
	 * @param relTol  relative tolerance
	 * @param absTol  absoluteToleration
	 * @param maxIterations max number of iterations
	 * @param maxEvaluations max number of evaluations
	 * @return the object holding the results
	 */
	public static ClosestApproachResult closestApproach(SwimTrajectory traj1, SwimTrajectory traj2, double beta1,
			double beta2, double deltaT, double relTol, double absTol, int maxIterations, int maxEvaluations) {

		if ((traj1 == null) || (traj2 == null)) {
			return null;
		}
		
		ClosestApproachResult caResult = new ClosestApproachResult(traj1, traj2);

		int len1 = traj1.size(); // num points in traj1
		int len2 = traj2.size(); // num points in traj1

		double x1[] = traj1.getArray(SwimTrajectory.X_IDX);
		double y1[] = traj1.getArray(SwimTrajectory.Y_IDX);
		double z1[] = traj1.getArray(SwimTrajectory.Z_IDX);
		double tx1[] = traj1.getArray(SwimTrajectory.DIRCOSX_IDX);
		double ty1[] = traj1.getArray(SwimTrajectory.DIRCOSY_IDX);
		double tz1[] = traj1.getArray(SwimTrajectory.DIRCOSZ_IDX);


		double s1[] = new double[len1];
		double t1[] = new double[len1];

		double x2[] = traj2.getArray(SwimTrajectory.X_IDX);
		double y2[] = traj2.getArray(SwimTrajectory.Y_IDX);
		double z2[] = traj2.getArray(SwimTrajectory.Z_IDX);
		double tx2[] = traj2.getArray(SwimTrajectory.DIRCOSX_IDX);
		double ty2[] = traj2.getArray(SwimTrajectory.DIRCOSY_IDX);
		double tz2[] = traj2.getArray(SwimTrajectory.DIRCOSZ_IDX);

		double s2[] = new double[len2];
		double t2[] = new double[len2];

		double v1 = beta1 * Swimmer.C; // m/s
		double v2 = beta2 * Swimmer.C; // m/s

		s1[0] = 0;
		t1[0] = 0;

		for (int i = 1; i < len1; i++) {
			int im1 = i - 1;
			s1[i] = s1[im1] + distance(x1[im1], y1[im1], z1[im1], x1[i], y1[i], z1[i]);
			t1[i] = 1.0e9 * s1[i] / v1; // time in nS
		}

		s2[0] = 0;
		t2[0] = deltaT;

		for (int i = 1; i < len2; i++) {
			int im1 = i - 1;
			s2[i] = s2[im1] + distance(x2[im1], y2[im1], z2[im1], x2[i], y2[i], z2[i]);
			t2[i] = deltaT + 1.0e9 * s2[i] / v2; // time in nS
		}

		// get the minimum and max times

		double tMin = Math.max(t1[0], t2[0]);
		double tMax = Math.min(t1[len1 - 1], t2[len2 - 1]);

		double result1[] = new double[3];
		double result2[] = new double[3];

		// use brent algorithm from apache common maths to get minimum
		UnivariateFunction function = t -> trajDistSq(t, x1, y1, z1, t1, result1, x2, y2, z2, t2, result2);
		UnivariateObjectiveFunction func = new UnivariateObjectiveFunction(function);


		BrentOptimizer opt = new BrentOptimizer(relTol, absTol);

		MaxIter maxIter = new MaxIter(maxIterations);
		MaxEval maxEval = new MaxEval(maxEvaluations);
		SearchInterval interval = new SearchInterval(tMin, tMax);
		UnivariatePointValuePair min = opt.optimize(maxIter, maxEval, func, GoalType.MINIMIZE, interval);

		caResult.t = min.getPoint();
		
		//minimized distSq but want min dist
		caResult.doca = Math.sqrt(min.getValue());
		
		
		//get state vectors at intersection
		double stateVector1[] = new double[6];
		double stateVector2[] = new double[6];
		
		//get the state vectors at the doca
		linearInterp(x1, y1, z1, tx1, ty1, tz1, t1, caResult.t, stateVector1);
		linearInterp(x2, y2, z2, tx2, ty2, tz2, t2, caResult.t, stateVector2);
		
		caResult.stateVector1 = stateVector1;
		caResult.stateVector2 = stateVector2;
		
		return caResult;
	}

	//the distance squared between two linear interpolations of two trajectories
	private static double trajDistSq(double t, double x1[], double y1[], double z1[], double t1[], double result1[],
			double x2[], double y2[], double z2[], double t2[], double result2[]) {

		linearInterp(x1, y1, z1, t1, t, result1);
		linearInterp(x2, y2, z2, t2, t, result2);

		return distanceSq(result1[0], result1[1], result1[2], result2[0], result2[1], result2[2]);

	}

	// for testing closest approach alg
	public static void main(String arg[]) {
		
		long seed1 = 62384289L;
		long seed2 = 74239584L;
		
		Random lundRand = (seed1 < 0) ? new Random() : new Random(seed1);
		
		
		System.out.println("testing closest approach algorithm for VZ");

		// create some random initial data
		int n = 100;
		RandomData data = new RandomData(n, seed2, 0, 0.2, 0, 0.2, 0, 0.2, 1, 3, 25, 10, 170, 0);

		MagneticFields.getInstance().initializeMagneticFields();
		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);

		AdaptiveSwimmer adaptiveSwimmer = new AdaptiveSwimmer(); // new

		double stepsizeAdaptive = 0.01; // starting

		double eps = 1.0e-6;
		double sMax = 8;

		double deltaT = .5; // ns

		AdaptiveSwimResult result = new AdaptiveSwimResult(true);

		for (int i = 0; i < n; i += 2) {

			int j = i + 1;

			int charge1 = data.charge[i];
			double x1 = data.xo[i];
			double y1 = data.yo[i];
			double z1 = data.zo[i];
			double p1 = data.p[i];
			double theta1 = data.theta[i];
			double phi1 = data.phi[i];

			LundId lid1 = randomLundId(lundRand, charge1 < 0);

			int charge2 = data.charge[j];
			double x2 = data.xo[j];
			double y2 = data.yo[j];
			double z2 = data.zo[j];
			double p2 = data.p[j];
			double theta2 = data.theta[j];
			double phi2 = data.phi[j];

			LundId lid2 = randomLundId(lundRand, charge2 < 0);

			SwimTrajectory traj1 = null;
			SwimTrajectory traj2 = null;

			System.out.println("particle 1: " + lid1.smallString() + "   particle 12: " + lid2.smallString());

//			public void swim(int charge, double xo, double yo, double zo, double momentum,
//					double theta, double phi, double sMax, double h, double eps, AdaptiveSwimResult result)

			try {
				result.reset();

				adaptiveSwimmer.swim(charge1, x1, y1, z1, p1, theta1, phi1, sMax, stepsizeAdaptive, eps, result);

				traj1 = result.getTrajectory();

				traj1.computeBDL(adaptiveSwimmer.getProbe());

			} catch (AdaptiveSwimException e) {
				e.printStackTrace();
			}

			try {
				result.reset();

				adaptiveSwimmer.swim(charge2, x2, y2, z2, p2, theta2, phi2, sMax, stepsizeAdaptive, eps, result);

				traj2 = result.getTrajectory();

			} catch (AdaptiveSwimException e) {
				e.printStackTrace();
			}

				ClosestApproachResult caResult = closestApproach(traj1, traj2, p1, p2, lid1.getId(), lid2.getId(), deltaT);
			System.out.println(String.format("min time: %8.4f   min value: %8.4f\n", caResult.t, caResult.doca));

		}

		System.out.println("Done");
	}

}
