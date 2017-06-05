package cnuphys.ced.event.data;

public class BMTCrosses extends Crosses2 {
	
	private static BMTCrosses _instance;
	
	private BMTCrosses() {
		super("BMTRec::Crosses");
	}
	
	/**
	 * Public access to the singleton
	 * @return the singleton
	 */
	public static BMTCrosses getInstance() {
		if (_instance == null) {
			_instance = new BMTCrosses();
		}
		return _instance;
	}

}
