package cnuphys.ced.event.data;

import org.jlab.io.base.DataEvent;

/**
 * static methods to centralize getting data arrays related to BMT
 * @author heddle
 *
 */

public class BMT extends DetectorData {


	//list of adc hits
	AdcHitList _adcHits = new AdcHitList("BMT::adc");
	
	//list of BMT reconstructed hits
	BaseHit2List _bmtRecHits;

	private static BMT _instance;

	/**
	 * Public access to the singleton
	 * @return the BMT singleton
	 */
	public static BMT getInstance() {
		if (_instance == null) {
			_instance = new BMT();
		}
		return _instance;
	}
	
	@Override
	public void newClasIoEvent(DataEvent event) {
		_adcHits = new AdcHitList("BMT::adc");
		_bmtRecHits = new BaseHit2List("BMTRec::Hits", "strip");
	}
	
	/**
	 * Update the list. This is probably needed only during accumulation
	 * @return the updated list
	 */
	public AdcHitList updateAdcList() {
		_adcHits = new AdcHitList("BMT::adc");
		return _adcHits;
	}

	/**
	 * Get the adc hit list
	 * @return the adc hit list
	 */
	public AdcHitList getADCHits() {
		return _adcHits;
	}
	
	/**
	 * Get the BMT reconstructed hits
	 * @return the BMT reconstructed hits
	 */
	public BaseHit2List getRecHits() {
		return _bmtRecHits;
	}
	
}
