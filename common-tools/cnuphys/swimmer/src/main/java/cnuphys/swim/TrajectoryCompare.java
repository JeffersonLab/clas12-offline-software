package cnuphys.swim;

/**
 * static methods useful for comparing trajectories in testing
 * 
 * @author heddle
 *
 */
public class TrajectoryCompare {

	/**
	 * Compare the distance between final positions
	 * 
	 * @param traj1 one trajectory
	 * @param traj2 another trajectory
	 * @return the distance between the final positions
	 */
	public static double finalPositionDifference(SwimTrajectory traj1, SwimTrajectory traj2) {
		double finalPos1[] = traj1.getFinalPosition();
		double finalPos2[] = traj2.getFinalPosition();
		double delX = finalPos2[0] - finalPos1[0];
		double delY = finalPos2[1] - finalPos1[1];
		double delZ = finalPos2[2] - finalPos1[2];
		return Math.sqrt(delX * delX + delY * delY + delZ * delZ);
	}
}
