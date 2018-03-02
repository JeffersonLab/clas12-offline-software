package cnuphys.ced.event.data;

import org.jlab.io.base.DataEvent;

public class CND extends DetectorData {

	
	TdcAdcHitList _tdcAdcHits = new TdcAdcHitList("CND::tdc", "CND::adc");

	private static CND _instance;


	/**
	 * Public access to the singleton
	 * @return the CND singleton
	 */
	public static CND getInstance() {
		if (_instance == null) {
			_instance = new CND();
		}
		return _instance;
	}
	
	@Override
	public void newClasIoEvent(DataEvent event) {
		_tdcAdcHits =  new TdcAdcHitList("CND::tdc", "CND::adc");
	}
	
	/**
	 * Update the list. This is probably needed only during accumulation
	 * @return the updated list
	 */
	public TdcAdcHitList updateTdcAdcList() {
		_tdcAdcHits =  new TdcAdcHitList("CND::tdc", "CND::adc");
		return _tdcAdcHits;
	}

	/**
	 * Get the tdc and adc hit list
	 * @return the tdc adc hit list
	 */
	public TdcAdcHitList getHits() {
		return _tdcAdcHits;
	}
}