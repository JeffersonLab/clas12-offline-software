package cnuphys.ced.event.data;

import org.jlab.io.base.DataEvent;

public class AllEC extends DetectorData {

	// EC "layer" constants
	public static final int PCAL_U = 1;
	public static final int PCAL_V = 2;
	public static final int PCAL_W = 3;
	public static final int ECAL_IN_U = 4;
	public static final int ECAL_IN_V = 5;
	public static final int ECAL_IN_W = 6;
	public static final int ECAL_OUT_U = 7;
	public static final int ECAL_OUT_V = 8;
	public static final int ECAL_OUT_W = 9;

	public static final String layerNames[] = { "???",
			"PCAL_U", "PCAL_V", "PCAL_W",
			"ECAL_IN_U", "ECAL_IN_V", "ECAL_IN_W",
			"ECAL_OUT_U", "ECAL_OUT_V", "ECAL_OUTW" };

//	bank name: [ECAL::adc] column name: [ADC] full name: [ECAL::adc.ADC] data type: int
//	bank name: [ECAL::adc] column name: [component] full name: [ECAL::adc.component] data type: short
//	bank name: [ECAL::adc] column name: [layer] full name: [ECAL::adc.layer] data type: byte
//	bank name: [ECAL::adc] column name: [order] full name: [ECAL::adc.order] data type: byte
//	bank name: [ECAL::adc] column name: [ped] full name: [ECAL::adc.ped] data type: short
//	bank name: [ECAL::adc] column name: [sector] full name: [ECAL::adc.sector] data type: byte
//	bank name: [ECAL::adc] column name: [time] full name: [ECAL::adc.time] data type: float
//	bank name: [ECAL::tdc] column name: [TDC] full name: [ECAL::tdc.TDC] data type: int
//	bank name: [ECAL::tdc] column name: [component] full name: [ECAL::tdc.component] data type: short
//	bank name: [ECAL::tdc] column name: [layer] full name: [ECAL::tdc.layer] data type: byte
//	bank name: [ECAL::tdc] column name: [order] full name: [ECAL::tdc.order] data type: byte
//	bank name: [ECAL::tdc] column name: [sector] full name: [ECAL::tdc.sector] data type: byte
	
	TdcAdcHitList _tdcAdcHits = new TdcAdcHitList("ECAL::tdc", "ECAL::adc");

	private static AllEC _instance;

	/**
	 * Public access to the singleton
	 * @return the FTOF singleton
	 */
	public static AllEC getInstance() {
		if (_instance == null) {
			_instance = new AllEC();
		}
		return _instance;
	}
	
	@Override
	public void newClasIoEvent(DataEvent event) {
		_tdcAdcHits =  new TdcAdcHitList("ECAL::tdc", "ECAL::adc");
	}
	
	/**
	 * Update the list. This is probably needed only during accumulation
	 * @return the updated list
	 */
	public TdcAdcHitList updateTdcAdcList() {
		_tdcAdcHits =  new TdcAdcHitList("ECAL::tdc", "ECAL::adc");
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
