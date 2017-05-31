package cnuphys.ced.event.data;

public class HBSegments extends Segments {
	
	private static HBSegments _instance;
	
	private HBSegments() {
		super("HitBasedTrkg::HBSegments");
	}
	
	/**
	 * Public access to the singleton
	 * @return the singleton
	 */
	public static HBSegments getInstance() {
		if (_instance == null) {
			_instance = new HBSegments();
		}
		return _instance;
	}
	

}
