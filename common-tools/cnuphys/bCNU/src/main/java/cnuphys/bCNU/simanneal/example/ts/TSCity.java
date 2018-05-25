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

}
