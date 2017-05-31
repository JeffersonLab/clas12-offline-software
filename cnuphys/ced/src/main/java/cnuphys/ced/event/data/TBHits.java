package cnuphys.ced.event.data;

public class TBHits extends TbHbHits {
	
	private static TBHits _instance;
	
	private TBHits() {
		super("TimeBasedTrkg::TBHits");
	}
	
	/**
	 * Public access to the singleton
	 * @return the singleton
	 */
	public static TBHits getInstance() {
		if (_instance == null) {
			_instance = new TBHits();
		}
		return _instance;
	}

}
