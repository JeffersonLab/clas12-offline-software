package cnuphys.ced.event.data;

import org.jlab.io.base.DataEvent;

public class SVT extends DetectorData {

	AdcHitList _adcHits = new AdcHitList("SVT::adc");

	private static SVT _instance;

	/**
	 * Public access to the singleton
	 * @return the FTOF singleton
	 */
	public static SVT getInstance() {
		if (_instance == null) {
			_instance = new SVT();
		}
		return _instance;
	}
	
	@Override
	public void newClasIoEvent(DataEvent event) {
		_adcHits = new AdcHitList("SVT::adc");
	}
	
	/**
	 * Update the list. This is probably needed only during accumulation
	 * @return the updated list
	 */
	public AdcHitList updateAdcList() {
		_adcHits = new AdcHitList("SVT::adc");
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