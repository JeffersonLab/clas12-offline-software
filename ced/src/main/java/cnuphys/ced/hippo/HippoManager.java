package cnuphys.ced.hippo;

import org.jlab.io.hipo.HipoDataDictionary;

import cnuphys.ced.alldata.DataManager;

public class HippoManager extends DataManager {
	
	//singleton
	private static  HippoManager _instance;
	
	
	private HippoManager() {
		super(new HipoDataDictionary());
	}	
	
	/**
	 * public access to the singleton
	 * @return the singleton
	 */
	public static HippoManager getInstance() {
		if (_instance == null)  {
			_instance = new HippoManager();
		}
		return _instance;
	}

}
