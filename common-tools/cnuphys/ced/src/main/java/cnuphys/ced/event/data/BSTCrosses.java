package cnuphys.ced.event.data;

public class BSTCrosses extends Crosses2 {
	
	private static BSTCrosses _instance;
	
	private BSTCrosses() {
		super("BSTRec::Crosses");
	}
	
	/**
	 * Public access to the singleton
	 * @return the singleton
	 */
	public static BSTCrosses getInstance() {
		if (_instance == null) {
			_instance = new BSTCrosses();
		}
		return _instance;
	}

}
