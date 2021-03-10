package cnuphys.ced.event.data;

import org.jlab.io.base.DataEvent;

public class RTPC extends DetectorData {
	
	public static final int NUMLAYER = 96;
	public static final int NUMCOMPONENT =  180;

	
	RTPCHitList _rtpcHits = new RTPCHitList("RTPC::adc");
	
	private static RTPC _instance;
	
	/**
	 * Public access to the singleton
	 * 
	 * @return the RTPC singleton
	 */
	public static RTPC getInstance() {
		if (_instance == null) {
			_instance = new RTPC();
		}
		return _instance;
	}

	@Override
	public void newClasIoEvent(DataEvent event) {
		_rtpcHits = new RTPCHitList("RTPC::adc");
	}

	/**
	 * Update the list. This is probably needed only during accumulation
	 * 
	 * @return the updated list
	 */
	public RTPCHitList updateAdcList() {
		_rtpcHits = new RTPCHitList("RTPC::adc");
		return _rtpcHits;
	}

	/**
	 * Get the adc hit list
	 * 
	 * @return the adc hit list
	 */
	public RTPCHitList getHits() {
		return _rtpcHits;
	}

}
