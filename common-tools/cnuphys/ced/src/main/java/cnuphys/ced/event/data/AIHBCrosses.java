package cnuphys.ced.event.data;

public class AIHBCrosses extends Crosses {

	private static AIHBCrosses _instance;

	private AIHBCrosses() {
		super("HitBasedTrkg::AICrosses");
	}

	/**
	 * Public access to the singleton
	 * 
	 * @return the singleton
	 */
	public static AIHBCrosses getInstance() {
		if (_instance == null) {
			_instance = new AIHBCrosses();
		}
		return _instance;
	}

}
