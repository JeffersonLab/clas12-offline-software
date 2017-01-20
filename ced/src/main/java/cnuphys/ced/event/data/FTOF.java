package cnuphys.ced.event.data;

import org.jlab.io.base.DataEvent;

/**
 * static methods to centralize getting data arrays
 * 
 * @author heddle
 *
 */
public class FTOF extends DetectorData {

	// ftof constants
	public static final int PANEL_1A = 0;
	public static final int PANEL_1B = 1;
	public static final int PANEL_2 = 2;
	public static final String panelNames[] = { "Panel 1A", "Panel 1B",
			"Panel 2" };

//	bank name: [FTOF::adc] column name: [sector] full name: [FTOF::adc.sector] data type: byte
//	bank name: [FTOF::adc] column name: [layer] full name: [FTOF::adc.layer] data type: byte
//	bank name: [FTOF::adc] column name: [component] full name: [FTOF::adc.component] data type: short
//	bank name: [FTOF::adc] column name: [order] full name: [FTOF::adc.order] data type: byte
//	bank name: [FTOF::adc] column name: [ADC] full name: [FTOF::adc.ADC] data type: int
//	bank name: [FTOF::adc] column name: [time] full name: [FTOF::adc.time] data type: float
//	bank name: [FTOF::adc] column name: [ped] full name: [FTOF::adc.ped] data type: short
//	bank name: [FTOF::clusters] column name: [id] full name: [FTOF::clusters.id] data type: short
//	bank name: [FTOF::clusters] column name: [status] full name: [FTOF::clusters.status] data type: short
//	bank name: [FTOF::clusters] column name: [trackid] full name: [FTOF::clusters.trackid] data type: short
//	bank name: [FTOF::clusters] column name: [sector] full name: [FTOF::clusters.sector] data type: byte
//	bank name: [FTOF::clusters] column name: [layer] full name: [FTOF::clusters.layer] data type: byte
//	bank name: [FTOF::clusters] column name: [component] full name: [FTOF::clusters.component] data type: short
//	bank name: [FTOF::clusters] column name: [energy] full name: [FTOF::clusters.energy] data type: float
//	bank name: [FTOF::clusters] column name: [time] full name: [FTOF::clusters.time] data type: float
//	bank name: [FTOF::clusters] column name: [energy_unc] full name: [FTOF::clusters.energy_unc] data type: float
//	bank name: [FTOF::clusters] column name: [time_unc] full name: [FTOF::clusters.time_unc] data type: float
//	bank name: [FTOF::clusters] column name: [x] full name: [FTOF::clusters.x] data type: float
//	bank name: [FTOF::clusters] column name: [y] full name: [FTOF::clusters.y] data type: float
//	bank name: [FTOF::clusters] column name: [z] full name: [FTOF::clusters.z] data type: float
//	bank name: [FTOF::clusters] column name: [x_unc] full name: [FTOF::clusters.x_unc] data type: float
//	bank name: [FTOF::clusters] column name: [y_unc] full name: [FTOF::clusters.y_unc] data type: float
//	bank name: [FTOF::clusters] column name: [z_unc] full name: [FTOF::clusters.z_unc] data type: float
//	bank name: [FTOF::hits] column name: [id] full name: [FTOF::hits.id] data type: short
//	bank name: [FTOF::hits] column name: [status] full name: [FTOF::hits.status] data type: short
//	bank name: [FTOF::hits] column name: [trackid] full name: [FTOF::hits.trackid] data type: short
//	bank name: [FTOF::hits] column name: [sector] full name: [FTOF::hits.sector] data type: byte
//	bank name: [FTOF::hits] column name: [layer] full name: [FTOF::hits.layer] data type: byte
//	bank name: [FTOF::hits] column name: [component] full name: [FTOF::hits.component] data type: short
//	bank name: [FTOF::hits] column name: [energy] full name: [FTOF::hits.energy] data type: float
//	bank name: [FTOF::hits] column name: [time] full name: [FTOF::hits.time] data type: float
//	bank name: [FTOF::hits] column name: [energy_unc] full name: [FTOF::hits.energy_unc] data type: float
//	bank name: [FTOF::hits] column name: [time_unc] full name: [FTOF::hits.time_unc] data type: float
//	bank name: [FTOF::hits] column name: [x] full name: [FTOF::hits.x] data type: float
//	bank name: [FTOF::hits] column name: [y] full name: [FTOF::hits.y] data type: float
//	bank name: [FTOF::hits] column name: [z] full name: [FTOF::hits.z] data type: float
//	bank name: [FTOF::hits] column name: [x_unc] full name: [FTOF::hits.x_unc] data type: float
//	bank name: [FTOF::hits] column name: [y_unc] full name: [FTOF::hits.y_unc] data type: float
//	bank name: [FTOF::hits] column name: [z_unc] full name: [FTOF::hits.z_unc] data type: float
//	bank name: [FTOF::hits] column name: [tx] full name: [FTOF::hits.tx] data type: float
//	bank name: [FTOF::hits] column name: [ty] full name: [FTOF::hits.ty] data type: float
//	bank name: [FTOF::hits] column name: [tz] full name: [FTOF::hits.tz] data type: float
//	bank name: [FTOF::matchedclusters] column name: [sector] full name: [FTOF::matchedclusters.sector] data type: byte
//	bank name: [FTOF::matchedclusters] column name: [paddle_id1A] full name: [FTOF::matchedclusters.paddle_id1A] data type: short
//	bank name: [FTOF::matchedclusters] column name: [paddle_id1B] full name: [FTOF::matchedclusters.paddle_id1B] data type: short
//	bank name: [FTOF::matchedclusters] column name: [clus_1Aid] full name: [FTOF::matchedclusters.clus_1Aid] data type: short
//	bank name: [FTOF::matchedclusters] column name: [clus_1Bid] full name: [FTOF::matchedclusters.clus_1Bid] data type: short
//	bank name: [FTOF::matchedclusters] column name: [clusSize_1A] full name: [FTOF::matchedclusters.clusSize_1A] data type: short
//	bank name: [FTOF::matchedclusters] column name: [clusSize_1B] full name: [FTOF::matchedclusters.clusSize_1B] data type: short
//	bank name: [FTOF::matchedclusters] column name: [tminAlgo_1B_tCorr] full name: [FTOF::matchedclusters.tminAlgo_1B_tCorr] data type: float
//	bank name: [FTOF::matchedclusters] column name: [midbarAlgo_1B_tCorr] full name: [FTOF::matchedclusters.midbarAlgo_1B_tCorr] data type: float
//	bank name: [FTOF::matchedclusters] column name: [EmaxAlgo_1B_tCorr] full name: [FTOF::matchedclusters.EmaxAlgo_1B_tCorr] data type: float
//	bank name: [FTOF::rawhits] column name: [id] full name: [FTOF::rawhits.id] data type: short
//	bank name: [FTOF::rawhits] column name: [status] full name: [FTOF::rawhits.status] data type: short
//	bank name: [FTOF::rawhits] column name: [sector] full name: [FTOF::rawhits.sector] data type: byte
//	bank name: [FTOF::rawhits] column name: [layer] full name: [FTOF::rawhits.layer] data type: byte
//	bank name: [FTOF::rawhits] column name: [component] full name: [FTOF::rawhits.component] data type: short
//	bank name: [FTOF::rawhits] column name: [energy_left] full name: [FTOF::rawhits.energy_left] data type: float
//	bank name: [FTOF::rawhits] column name: [energy_right] full name: [FTOF::rawhits.energy_right] data type: float
//	bank name: [FTOF::rawhits] column name: [time_left] full name: [FTOF::rawhits.time_left] data type: float
//	bank name: [FTOF::rawhits] column name: [time_right] full name: [FTOF::rawhits.time_right] data type: float
//	bank name: [FTOF::rawhits] column name: [energy_left_unc] full name: [FTOF::rawhits.energy_left_unc] data type: float
//	bank name: [FTOF::rawhits] column name: [energy_right_unc] full name: [FTOF::rawhits.energy_right_unc] data type: float
//	bank name: [FTOF::rawhits] column name: [time_left_unc] full name: [FTOF::rawhits.time_left_unc] data type: float
//	bank name: [FTOF::rawhits] column name: [time_right_unc] full name: [FTOF::rawhits.time_right_unc] data type: float
//	bank name: [FTOF::tdc] column name: [sector] full name: [FTOF::tdc.sector] data type: byte
//	bank name: [FTOF::tdc] column name: [layer] full name: [FTOF::tdc.layer] data type: byte
//	bank name: [FTOF::tdc] column name: [component] full name: [FTOF::tdc.component] data type: short
//	bank name: [FTOF::tdc] column name: [order] full name: [FTOF::tdc.order] data type: byte
//	bank name: [FTOF::tdc] column name: [TDC] full name: [FTOF::tdc.TDC] data type: int

//	public List<DetectorResponse> getDetectorResponse(DataEvent event, String bankName, DetectorType type) {

//	public static List<DetectorResponse> getFTOFResponseTDC() {
//		DataEvent event = ClasIoEventManager.getInstance().getCurrentEvent();
//		return DataManager.getInstance().getDetectorResponse(event, "FTOF::hits", DetectorType.FTOF);
//	}
	
	
	//tdc adc hit list
	TdcAdcHitList _tdcAdcHits = new TdcAdcHitList("FTOF::tdc", "FTOF::adc");
	
	private static FTOF _instance;
	
	
	/**
	 * Public access to the singleton
	 * @return the FTOF singleton
	 */
	public static FTOF getInstance() {
		if (_instance == null) {
			_instance = new FTOF();
		}
		return _instance;
	}
	

	
	@Override
	public void newClasIoEvent(DataEvent event) {
		_tdcAdcHits =  new TdcAdcHitList("FTOF::tdc", "FTOF::adc");
	}
	
	public TdcAdcHitList updateTdcAdcList() {
		_tdcAdcHits =  new TdcAdcHitList("FTOF::tdc", "FTOF::adc");
		return _tdcAdcHits;
	}


	/**
	 * Get the pid array from the true data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the pid array
	 */
	public int[] pid(int panelType) {
		return null;
	}


	
	/**
	 * Get the avgX array from the true data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the avgX array
	 */
	public double[] avgX(int panelType) {
		return null;
	}
	
	
	/**
	 * Get the avgY array from the true data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the avgY array
	 */
	public double[] avgY(int panelType) {
		return null;
	}
	
	
	/**
	 * Get the avgZ array from the true data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the avgZ array
	 */
	public double[] avgZ(int panelType) {
		return null;
	}
	
	/**
	 * Get the reconstructed sector array from the reconstructed data
	 * 
	 * @return the reconstructed sector array
	 */
	public int[] reconSector() {
		return null;
	}

	/**
	 * Get the reconstructed panel array from the reconstructed data
	 * 
	 * @return the reconstructed panel array
	 */
	public int[] reconPanel() {
		return null;
	}

	/**
	 * Get the reconstructed paddle array from the reconstructed data
	 * 
	 * @return the reconstructed sector array
	 */
	public int[] reconPaddle() {
		return null;
	}

	/**
	 * Get the reconstructed x array from the reconstructed data
	 * 
	 * @return the reconstructed x array
	 */
	public float[] reconX() {
		return null;
	}

	/**
	 * Get the reconstructed y array from the reconstructed data
	 * 
	 * @return the reconstructed y array
	 */
	public float[] reconY() {
		return null;
	}

	/**
	 * Get the reconstructed z array from the reconstructed data
	 * 
	 * @return the reconstructed z array
	 */
	public float[] reconZ() {
		return null;
	}
	

	/**
	 * Get the name from the panel type
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the name of the panel type
	 */
	public static String name(int panelType) {
		if ((panelType < 0) || (panelType > 2)) {
			return "???";
		} else {
			return panelNames[panelType];
		}
	}



	/**
	 * Get the tdc and adc hit list
	 * @return the tdc adc hit list
	 */
	public TdcAdcHitList getTdcAdcHits() {
		return _tdcAdcHits;
	}
}
