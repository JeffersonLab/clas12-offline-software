package cnuphys.ced.event.data;

public class HBCrosses extends Crosses {
	
	private static HBCrosses _instance;
	
	private HBCrosses() {
		super("HitBasedTrkg::HBCrosses");
	}
	
	/**
	 * Public access to the singleton
	 * @return the singleton
	 */
	public static HBCrosses getInstance() {
		if (_instance == null) {
			_instance = new HBCrosses();
		}
		return _instance;
	}


}
