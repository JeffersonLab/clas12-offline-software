package cnuphys.ced.event.data;

public class AITBSegments extends Segments {

	private static AITBSegments _instance;

	private AITBSegments() {
		super("TimeBasedTrkg::AISegments");
	}

	/**
	 * Public access to the singleton
	 * 
	 * @return the singleton
	 */
	public static AITBSegments getInstance() {
		if (_instance == null) {
			_instance = new AITBSegments();
		}
		return _instance;
	}

}
