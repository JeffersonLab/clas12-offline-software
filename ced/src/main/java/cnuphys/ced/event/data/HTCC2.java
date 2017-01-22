package cnuphys.ced.event.data;

import org.jlab.io.base.DataEvent;

public class HTCC2 extends DetectorData {

	AdcHitList _adcHits = new AdcHitList("HTCC::adc");

	private static HTCC2 _instance;

	/**
	 * Public access to the singleton
	 * @return the FTOF singleton
	 */
	public static HTCC2 getInstance() {
		if (_instance == null) {
			_instance = new HTCC2();
		}
		return _instance;
	}
	
	@Override
	public void newClasIoEvent(DataEvent event) {
		_adcHits = new AdcHitList("HTCC::adc");
	}
	
	/**
	 * Update the list. This is probably needed only during accumulation
	 * @return the updated list
	 */
	public AdcHitList updateAdcList() {
		_adcHits = new AdcHitList("HTCC::adc");
		return _adcHits;
	}

	/**
	 * Get the adc hit list
	 * @return the adc hit list
	 */
	public AdcHitList getHits() {
		return _adcHits;
	}
}
