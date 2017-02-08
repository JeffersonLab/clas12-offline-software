package cnuphys.ced.event.data;

public class SVTCrosses extends Crosses2 {
	
	private static SVTCrosses _instance;
	
	private SVTCrosses() {
		super("BSTRec::Crosses");
	}
	
	/**
	 * Public access to the singleton
	 * @return the singleton
	 */
	public static SVTCrosses getInstance() {
		if (_instance == null) {
			_instance = new SVTCrosses();
		}
		return _instance;
	}

}
