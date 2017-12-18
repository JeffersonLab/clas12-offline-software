package cnuphys.ced.event.data;

public class FMTCrosses extends Crosses2 {
	
	private static FMTCrosses _instance;
	
	private FMTCrosses() {
		super("FMTRec::Crosses");
	}
	
	/**
	 * Public access to the singleton
	 * @return the singleton
	 */
	public static FMTCrosses getInstance() {
		if (_instance == null) {
			_instance = new FMTCrosses();
		}
		return _instance;
	}

}
