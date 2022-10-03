package cnuphys.adaptiveSwim.swimZ;

import java.util.ArrayList;

public class SwimZTrajectory  extends ArrayList<double[]> {
	
	
	// the integer charge
	private final int _charge;
	
	//the momentum (constant) in GeV/c
	private final double _p;

	/**
	 * Create a swimZ trajectory with no initial points
	 * @param charge the integer charge
	 * @param p the momentum in GeV/c
	 */
	public SwimZTrajectory(int charge, double p) {
		_charge = charge;
		_p = p;
	}
	
	/**
	 * Clear the trajectory
	 */
	@Override
	public void clear() {
		super.clear();
	}

	/**
	 * Get the integer charge
	 * @return the integer charge
	 */
	public int getCharge() {
		return _charge;
	}
	
	/**
	 * Get the momentum in GeV/c
	 * @return the momentum in GeV/c
	 */
	public double getMomentum() {
		return _p;
	}
	
	@Override
	public boolean add(double u[]) {
		if (u == null) {
			return false;
		}
		
		int dim = u.length;
		
		//adds a copy!!
		double ucopy[] = new double[dim];
		System.arraycopy(u, 0, ucopy, 0, dim);
		return super.add(ucopy);
	}
	
	/**
	 * Add to the trajectory
	 * @param u the new 4D vector
	 * @param z the coordinate, z is the independent variable
	 * @return true if add was successful
	 */
	public boolean add(double u[], double z) {
		if (u == null) {
			return false;
		}
		
		//extra space for z
		double ucopy[] = new double[5];
		System.arraycopy(u, 0, ucopy, 0, 4);
		ucopy[4] = z;
		return super.add(ucopy);
	}

}
