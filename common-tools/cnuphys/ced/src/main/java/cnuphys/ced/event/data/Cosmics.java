package cnuphys.ced.event.data;

import org.jlab.io.base.DataEvent;

public class Cosmics  extends DetectorData {

	protected String _bankName;
	
	protected CosmicList _cosmics;
	
	private static Cosmics _instance;
	
	private Cosmics() {
		_bankName = "CVTRec::Cosmics";
		_cosmics = new CosmicList(_bankName);
	}
	
	
	public static Cosmics getInstance() {
		if (_instance == null) {
			_instance = new Cosmics();
		}
		return _instance;
	}
	
	@Override
	public void newClasIoEvent(DataEvent event) {
		_cosmics =  new CosmicList(_bankName);
//		System.err.println("NUM COSMICS: "+ _cosmics.size());
	}
	

	/**
	 * Get the cross list
	 * @return the cross list
	 */
	public CosmicList getCosmics() {
		return _cosmics;
	}

}