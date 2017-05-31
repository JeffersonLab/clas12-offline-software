package cnuphys.ced.event.data;

public class TBSegments extends Segments {
	
	private static TBSegments _instance;
	
	private TBSegments() {
		super("TimeBasedTrkg::TBSegments");
	}
	
	/**
	 * Public access to the singleton
	 * @return the singleton
	 */
	public static TBSegments getInstance() {
		if (_instance == null) {
			_instance = new TBSegments();
		}
		return _instance;
	}
	
}
