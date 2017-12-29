package cnuphys.bCNU.simanneal;

public abstract class Solution implements IAnneal {

	
	
	public abstract Solution getNeighbor();
	
	public abstract Solution copy();
}
