package cnuphys.bCNU.simanneal;

public abstract class Solution implements IAnneal {

	/**
	 * Get a rearrangement (candidate solution)
	 * @return a rearrangement
	 */
	public abstract Solution getRearrangement();
	
	/**
	 * Copy the solution (often in preparation for a rearrangement) for 
	 * @return a copy of the solution
	 */
	public abstract Solution copy();
	
	/**
	 * Get the y value for the plot. E.g., energy.
	 * @return the y value for the plot
	 */
	public abstract double getPlotY();
}
