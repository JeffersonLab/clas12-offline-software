package cnuphys.bCNU.simanneal;

public abstract class Solution implements IAnneal {

	/**
	 * Get a neighbor (candidate solution)
	 * @return a neighbor
	 */
	public abstract Solution getNeighbor();
	
	/**
	 * Copy the solution
	 * @return a copy of the colution
	 */
	public abstract Solution copy();
}
