package cnuphys.ced.event.data;

import org.jlab.io.base.DataEvent;

public class DC2 extends DetectorData {

	
	//tdc adc hit list
	DCTdcHitList _tdcHits = new DCTdcHitList();
	
	private static DC2 _instance;
	
	
	/**
	 * Public access to the singleton
	 * @return the FTOF singleton
	 */
	public static DC2 getInstance() {
		if (_instance == null) {
			_instance = new DC2();
		}
		return _instance;
	}
	
	@Override
	public void newClasIoEvent(DataEvent event) {
		_tdcHits =  new DCTdcHitList();
	}
	
	public DCTdcHitList updateTdcAdcList() {
		_tdcHits =  new DCTdcHitList();
		return _tdcHits;
	}

}
