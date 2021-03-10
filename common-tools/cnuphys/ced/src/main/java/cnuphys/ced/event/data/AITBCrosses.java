package cnuphys.ced.event.data;

public class AITBCrosses extends Crosses {

	private static AITBCrosses _instance;

	private AITBCrosses() {
		super("TimeBasedTrkg::AICrosses");
	}

	/**
	 * Public access to the singleton
	 * 
	 * @return the singleton
	 */
	public static AITBCrosses getInstance() {
		if (_instance == null) {
			_instance = new AITBCrosses();
		}
		return _instance;
	}

}
