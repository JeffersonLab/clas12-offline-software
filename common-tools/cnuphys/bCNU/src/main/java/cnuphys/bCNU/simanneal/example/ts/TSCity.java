package cnuphys.bCNU.simanneal.example.ts;

public class TSCity {
	
	public final double x;
	public final double y;
	
	public TSCity() {
		x = Math.random();
		y = Math.random();
	}
	
	public double distance(TSCity c) {
		return Math.hypot(c.x-x, c.y-y);
	}
	
	//used for the energy pe
	public int leftOrRight() {
		return (x < 0.5) ? -1 : 1;
	}
	
	/**
	 * Is the given city across the river?
	 * @param c the given city
	 * @return <code>true</code> if it is across the river
	 */
	public boolean acrossRiver(TSCity c) {
		return leftOrRight() != c.leftOrRight();
	}

}
