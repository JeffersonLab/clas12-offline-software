package cnuphys.swim.util;

import cnuphys.swim.SwimTrajectory;

public class ClosestApproachResult {
	
	/** time of the doca (ns)*/
	public double t;
	
	/** distance of the doca (m)*/
	public double doca;
	
	/** the first swim trajectory */
	public SwimTrajectory traj1;
	
	/** the second swim trajectory */
	public  SwimTrajectory traj2;
	
	/** momentum of particle 1 GeV/c^2 */
	public double p1;
	
	/** momentum of particle 2 GeV/c^2 */
	public double p2;
	
	/** the state vector of particle 1 at doca */
	public double[] stateVector1 = new double[6];

	/** the state vector of particle 2 at doca */
	public double[] stateVector2 = new double[6];
	
	public ClosestApproachResult(SwimTrajectory traj1, SwimTrajectory traj2) {
		this.traj1 = traj1;
		this.traj2 = traj2;
		
		p1 = traj1.getGeneratedParticleRecord().getMomentum();
		p2 = traj2.getGeneratedParticleRecord().getMomentum();
	}
	

	@Override
	public String toString() {
		String s1 = String.format("-------\ndoca time: %8.4f ns    doca: %8.4f m\n", t, doca);
		String s2 = String.format("p1: %8.4f SV1: (%8.4f, %8.4f, %8.4f) (%8.4f, %8.4f, %8.4f) norm: %8.5f\n", p1, stateVector1[0], stateVector1[1],
				stateVector1[2], stateVector1[3], stateVector1[4], stateVector1[4], tNorm(stateVector1));
		String s3 = String.format("p2: %8.4f SV2: (%8.4f, %8.4f, %8.4f) (%8.4f, %8.4f, %8.4f) norm: %8.5f\n", p2, stateVector2[0], stateVector2[1],
				stateVector2[2], stateVector2[3], stateVector2[4], stateVector2[4], tNorm(stateVector2));
		
		double dx = stateVector2[0] - stateVector1[0];
		double dy = stateVector2[1] - stateVector1[1];
		double dz = stateVector2[2] - stateVector1[2];
		double distCheck = Math.sqrt(dx*dx + dy*dy + dz*dz);
		
		String s4 = String.format("Distance check: %8.4f", distCheck);
		
		return s1 + s2 + s3 + s4;
	}
	
	private double tNorm(double[] sv) {
		double tx2 = sv[3]*sv[3];
		double ty2 = sv[4]*sv[4];
		double tz2 = sv[5]*sv[5];
		return Math.sqrt(tx2 + ty2 + tz2);
	}


}
