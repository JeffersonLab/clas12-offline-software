package cnuphys.ced.event.data;


public class AIHBSegments extends Segments {

	private static AIHBSegments _instance;

	private AIHBSegments() {
		super("HitBasedTrkg::AISegments");
	}

	/**
	 * Public access to the singleton
	 * 
	 * @return the singleton
	 */
	public static AIHBSegments getInstance() {
		if (_instance == null) {
			_instance = new AIHBSegments();
		}
		return _instance;
	}

}
