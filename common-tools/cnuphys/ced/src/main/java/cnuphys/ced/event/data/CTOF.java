package cnuphys.ced.event.data;

import org.jlab.io.base.DataEvent;

public class CTOF extends DetectorData {

	
	TdcAdcHitList _tdcAdcHits = new TdcAdcHitList("CTOF::tdc", "CTOF::adc");

	private static CTOF _instance;

	/**
	 * Public access to the singleton
	 * @return the CTOF singleton
	 */
	public static CTOF getInstance() {
		if (_instance == null) {
			_instance = new CTOF();
		}
		return _instance;
	}
	
	@Override
	public void newClasIoEvent(DataEvent event) {
		_tdcAdcHits =  new TdcAdcHitList("CTOF::tdc", "CTOF::adc");
	}
	
	/**
	 * Update the list. This is probably needed only during accumulation
	 * @return the updated list
	 */
	public TdcAdcHitList updateTdcAdcList() {
		_tdcAdcHits =  new TdcAdcHitList("CTOF::tdc", "CTOF::adc");
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