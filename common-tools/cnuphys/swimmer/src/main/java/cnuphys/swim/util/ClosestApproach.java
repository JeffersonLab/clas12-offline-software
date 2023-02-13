package cnuphys.swim.util;

import java.util.Arrays;

import cnuphys.swim.SwimTrajectory;
import cnuphys.swimtest.RandomData;

public class ClosestApproach {
	
	
	
	/**
	 * Compute the distance of closest approach in time. (not the over-all geometric closest
	 * approach of the trajectories, but the closest they get at any time.)
	 * @param traj1  one trajectory
	 * @param traj2  other trajectory
	 * @param p1 momentum of traj 1 in GeV/c
	 * @param p2  momentum of traj 2 in GeV/c
	 * @param lundId2 Lund (PDG) id for traj1. If not known, set to <= 0. This will be used
	 * to compute the speed from the momentum. If not known, speed of light assumed.
	 * @param lundId2 Lund (PDG) id for traj2, as for traj1
	 * @param deltaT time in ns between start of traj1 and start of traj2. Can be negative, in which
	 * case traj2 starts first.
	 * 
	 * @param results upon return, results[0] is the closest approach in time,
	 * results[1] is the time in nS
	 */

	public static void closestApproach(SwimTrajectory traj1, SwimTrajectory traj2, 
			double p1, double p2,
			int lundId1, int lundId2, double deltaT, double results[]) {

		double beta1 = 1; // v/c
		double beta2 = 1; // v/c

		if (lundId1 > 0) {}
		
	}
	
	/**
	 * perform a linear interpolation
	 * @param val the value
	 * @param x the x data (sorted)
	 * @param f the function value
	 * @return the linear interpolated values
	 */
	private double linearInterp(double val, double x[], double f[]) {
		int len = (x == null) ? 0 : x.length;
		if (len == 0) {
			return Double.NaN;
		}
		
		if ((val < x[0]) || (val > x[len-1])) {
			return Double.NaN;
		}
		
		int index = Arrays.binarySearch(x, val);
		if (index >= 0) {
			return f[index];
		}
		else {
			index = -(index + 1); // now the insertion point.
			double x0 = x[index-1];
			double x1 = x[index];
			double dx = x1-x0;
			
			double f0 = f[index-1];
			double f1 = f[index];
			double df = f1-f0;
			return f0 + ((val-x0)/dx)*df;
		}
	}
	
	/**
	 * Get the relativistic beta (v/c)
	 * @param momentum the momentum
	 * @param mass the mass in the same units as the momentum
	 * @return beta
	 */
	private double beta(double momentum, double mass) {
		double energy = Math.sqrt(momentum * momentum + mass * mass);

		double gamma = energy / mass;
		double beta = Math.sqrt(1.0 - 1.0 / (gamma * gamma));
		return beta;
	}
	
	//for testing closest approach alg
	public static void main(String arg[]) {
		System.out.println("testing closest approach algorithm for VZ");
		
		//create some random initial data
		int n = 10;
		long seed = 45636479L;
		RandomData randomData = new RandomData(n, seed, 0, 0.1, 0, 0.1, 0, 0.1, 4, 9, 25, 25, 0, 360);
		
	}

}
