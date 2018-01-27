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
	
	//tdc adc data
	private TdcAdcHitList _tdcAdcHits = new TdcAdcHitList("ECAL::tdc", "ECAL::adc");
	
	//clusters
	private ClusterList _clusters = new ClusterList("ECAL::clusters");
	
	private int _maxPCALAdc;
	private int _maxECALAdc;
	
	//singleton
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
	
	/**
	 * Get the layer name 
	 * @param layer the 1-based layer 1..9
	 * @return the brief layer name
	 */
	public String getLayerName(byte layer) {
		if ((layer < 1) || (layer > 9)) {
			return "" + layer;
		}
		else {
			return layerNames[layer];
		}
	}

	
	@Override
	public void newClasIoEvent(DataEvent event) {
		_clusters = new ClusterList("ECAL::clusters"); 
		_tdcAdcHits =  new TdcAdcHitList("ECAL::tdc", "ECAL::adc");
		computeADCMax();
	}
	
	//compte the max for PCAL and ECAL separately. Fugly.
	private void computeADCMax() {
		_maxPCALAdc = 0;
		_maxECALAdc = 0;
		
		if ((_tdcAdcHits != null) && !_tdcAdcHits.isEmpty()) {
			for (TdcAdcHit hit : _tdcAdcHits) {
				if (hit != null) {
					if (hit.layer < 4) {
						_maxPCALAdc = Math.max(_maxPCALAdc, hit.averageADC());
					}
					else {
						_maxECALAdc = Math.max(_maxECALAdc, hit.averageADC());
					}
				}
			}
		}
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
	 * Get the max adc for just the PCAL
	 * @return the max adc for just the PCAL
	 */
	public int getMaxPCALAdc() {
		return _maxPCALAdc;
	}
	
	/**
	 * Get the max adc for just the ECAL
	 * @return the max adc for just the ECAL
	 */
	public int getMaxECALAdc() {
		return _maxECALAdc;
	}



	/**
	 * Get the tdc and adc hit list
	 * @return the tdc adc hit list
	 */
	public TdcAdcHitList getHits() {
		return _tdcAdcHits;
	}
	
	
	/**
	 * Get the reconstructed cluster list
	 * @return reconstructed list
	 */
	public ClusterList getClusters() {
		return _clusters;
	}


}
