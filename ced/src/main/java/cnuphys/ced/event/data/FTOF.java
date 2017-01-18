package cnuphys.ced.event.data;

import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.detector.DetectorResponse;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.alldata.ColumnData;
import cnuphys.ced.alldata.DataManager;
import cnuphys.ced.clasio.ClasIoEventManager;

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
	
	private static FTOF _instance;
	
	//the sector and paddle arrays for each "layer"
	private int[][] _sector = new int[3][];
	private int[][] _paddle = new int[3][];

	
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
		extractSectorArrays();
		extractPaddleArrays();
	}
	
	//get the sector data from the event
	private void extractSectorArrays() {
		DataList lists[] = new DataList[3];
		for (int i = 0; i < 3; i++) {
			lists[i] = new DataList();
		}
		
		//get all the sector data from tdc
		byte[] tdcsect = ColumnData.getByteArray("FTOF::tdc.sector");
		byte[] adcsect = ColumnData.getByteArray("FTOF::adc.sector");
		byte[] tdclay = ColumnData.getByteArray("FTOF::tdc.layer");
		byte[] adclay = ColumnData.getByteArray("FTOF::adc.layer");
		
		if (tdcsect != null) {
			for (int i = 0; i < tdcsect.length; i++) {
				int panel = tdclay[i] - 1;
				lists[panel].add((int)tdcsect[i]);
			}
		}
		if (adcsect != null) {
			for (int i = 0; i < adcsect.length; i++) {
				int panel = adclay[i] - 1;
				lists[panel].add((int)adcsect[i]);
			}
		}

		for (int i = 0; i < 3; i++) {
			_sector[i] = lists[i] == null ? null : lists[i].toIntArray();
			System.err.println(" PANEL: " + i + "   SECT LEN: " + (_sector[i] == null ? 0 : _sector[i].length));
		}
	}
	
	//get the paddle data from the event
	private void extractPaddleArrays() {
		
		DataList lists[] = new DataList[3];
		for (int i = 0; i < 3; i++) {
			lists[i] = new DataList();
		}
		
		//get all the sector data from tdc
		short[] tdcpaddle = ColumnData.getShortArray("FTOF::tdc.component");
		short[] adcpaddle = ColumnData.getShortArray("FTOF::adc.component");
		byte[] tdclay = ColumnData.getByteArray("FTOF::tdc.layer");
		byte[] adclay = ColumnData.getByteArray("FTOF::adc.layer");
		
		if (tdcpaddle != null) {
			for (int i = 0; i < tdcpaddle.length; i++) {
				int panel = tdclay[i] - 1;
				lists[panel].add((int)tdcpaddle[i]);
			}
		}
		if (adcpaddle != null) {
			for (int i = 0; i < adcpaddle.length; i++) {
				int panel = adclay[i] - 1;
				lists[panel].add((int)adcpaddle[i]);
			}
		}

		for (int i = 0; i < 3; i++) {
			_paddle[i] = lists[i] == null ? null : lists[i].toIntArray();
			System.err.println(" PANEL: " + i + "   PADD LEN: " + (_paddle[i] == null ? 0 : _paddle[i].length));
		}
	}


	/**
	 * Get the pid array from the true data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the pid array
	 */
	public int[] pid(int panelType) {

		int pid[] = null;
		switch (panelType) {
		case PANEL_1A:
			pid = ColumnData.getIntArray("FTOF1A::true.pid");
			break;

		case PANEL_1B:
			pid = ColumnData.getIntArray("FTOF1B::true.pid");
			break;

		case PANEL_2:
			pid = ColumnData.getIntArray("FTOF2B::true.pid");
			break;
		}
		return pid;
	}

	/**
	 * Get the sector array from the dgtz data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the sector array
	 */
	public int[] sector(int panelType) {
		return _sector[panelType];
	}
	

	/**
	 * Get the paddle array from the dgtz data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the paddle array
	 */
	public int[] paddle(int panelType) {
		return _paddle[panelType];
    }

	/**
	 * Get the ADCL array from the dgtz data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the ADCL array
	 */
	public int[] ADCL(int panelType) {

		int ADCL[] = null;
		switch (panelType) {
		case PANEL_1A:
			ADCL = ColumnData.getIntArray("FTOF1A::dgtz.ADCL");
			break;

		case PANEL_1B:
			ADCL = ColumnData.getIntArray("FTOF1B::dgtz.ADCL");
			break;

		case PANEL_2:
			ADCL = ColumnData.getIntArray("FTOF2B::dgtz.ADCL");
			break;
		}
		return ADCL;
	}

	/**
	 * Get the ADCR array from the dgtz data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the ADCR array
	 */
	public int[] ADCR(int panelType) {

		int ADCR[] = null;
		switch (panelType) {
		case PANEL_1A:
			ADCR = ColumnData.getIntArray("FTOF1A::dgtz.ADCR");
			break;

		case PANEL_1B:
			ADCR = ColumnData.getIntArray("FTOF1B::dgtz.ADCR");
			break;

		case PANEL_2:
			ADCR = ColumnData.getIntArray("FTOF2B::dgtz.ADCR");
			break;
		}
		return ADCR;
	}

	/**
	 * Get the TDCL array from the dgtz data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the TDCL array
	 */
	public int[] TDCL(int panelType) {

		int TDCL[] = null;
		switch (panelType) {
		case PANEL_1A:
			TDCL = ColumnData.getIntArray("FTOF1A::dgtz.TDCL");
			break;

		case PANEL_1B:
			TDCL = ColumnData.getIntArray("FTOF1B::dgtz.TDCL");
			break;

		case PANEL_2:
			TDCL = ColumnData.getIntArray("FTOF2B::dgtz.TDCL");
			break;
		}
		return TDCL;
	}

	/**
	 * Get the TDCR array from the dgtz data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the TDCR array
	 */
	public int[] TDCR(int panelType) {

		int TDCR[] = null;
		switch (panelType) {
		case PANEL_1A:
			TDCR = ColumnData.getIntArray("FTOF1A::dgtz.TDCR");
			break;

		case PANEL_1B:
			TDCR = ColumnData.getIntArray("FTOF1B::dgtz.TDCR");
			break;

		case PANEL_2:
			TDCR = ColumnData.getIntArray("FTOF2B::dgtz.TDCR");
			break;
		}
		return TDCR;
	}
	
	/**
	 * Get the avgX array from the true data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the avgX array
	 */
	public double[] avgX(int panelType) {
		double avgX[] = null;
		switch (panelType) {

		case PANEL_1A:
			avgX = ColumnData.getDoubleArray("FTOF1A::true.avgX");
			break;

		case PANEL_1B:
			avgX = ColumnData.getDoubleArray("FTOF1B::true.avgX");
			break;

		case PANEL_2:
			avgX = ColumnData.getDoubleArray("FTOF2B::true.avgX");
			break;
		}
		return avgX;
	}
	
	
	/**
	 * Get the avgY array from the true data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the avgY array
	 */
	public double[] avgY(int panelType) {
		double avgY[] = null;
		switch (panelType) {

		case PANEL_1A:
			avgY = ColumnData.getDoubleArray("FTOF1A::true.avgY");
			break;

		case PANEL_1B:
			avgY = ColumnData.getDoubleArray("FTOF1B::true.avgY");
			break;

		case PANEL_2:
			avgY = ColumnData.getDoubleArray("FTOF2B::true.avgY");
			break;
		}
		return avgY;
	}
	
	
	/**
	 * Get the avgZ array from the true data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the avgZ array
	 */
	public double[] avgZ(int panelType) {
		double avgZ[] = null;
		switch (panelType) {

		case PANEL_1A:
			avgZ = ColumnData.getDoubleArray("FTOF1A::true.avgZ");
			break;

		case PANEL_1B:
			avgZ = ColumnData.getDoubleArray("FTOF1B::true.avgZ");
			break;

		case PANEL_2:
			avgZ = ColumnData.getDoubleArray("FTOF2B::true.avgZ");
			break;
		}
		return avgZ;
	}
	
	/**
	 * Get the reconstructed sector array from the reconstructed data
	 * 
	 * @return the reconstructed sector array
	 */
	public int[] reconSector() {
		return ColumnData.getIntArray("FTOFRec::ftofhits.sector");
	}

	/**
	 * Get the reconstructed panel array from the reconstructed data
	 * 
	 * @return the reconstructed panel array
	 */
	public int[] reconPanel() {
		return ColumnData.getIntArray("FTOFRec::ftofhits.panel_id");
	}

	/**
	 * Get the reconstructed paddle array from the reconstructed data
	 * 
	 * @return the reconstructed sector array
	 */
	public int[] reconPaddle() {
		return ColumnData.getIntArray("FTOFRec::ftofhits.paddle_id");
	}

	/**
	 * Get the reconstructed x array from the reconstructed data
	 * 
	 * @return the reconstructed x array
	 */
	public float[] reconX() {
		return ColumnData.getFloatArray("FTOFRec::ftofhits.x");
	}

	/**
	 * Get the reconstructed y array from the reconstructed data
	 * 
	 * @return the reconstructed y array
	 */
	public float[] reconY() {
		return ColumnData.getFloatArray("FTOFRec::ftofhits.y");
	}

	/**
	 * Get the reconstructed z array from the reconstructed data
	 * 
	 * @return the reconstructed z array
	 */
	public float[] reconZ() {
		return ColumnData.getFloatArray("FTOFRec::ftofhits.z");
	}
	
	/**
	 * Add some dgtz hit feedback for ftof
	 * 
	 * @param hitIndex
	 *            the hit index
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @param feedbackStrings
	 *            the collection of feedback strings
	 */
	public void dgtzFeedback(int hitIndex, int panelType,
			List<String> feedbackStrings) {

		if (hitIndex < 0) {
			return;
		}

		int sector[] = sector(panelType);
		int paddle[] = paddle(panelType);
		int ADCL[] = ADCL(panelType);
		int ADCR[] = ADCR(panelType);
		int TDCL[] = TDCL(panelType);
		int TDCR[] = TDCR(panelType);
		if ((sector == null) || (paddle == null) || (ADCL == null)
				|| (ADCR == null) || (TDCL == null) || (TDCR == null)) {
			return;
		}

		String pname = name(panelType);
		feedbackStrings.add(DataSupport.dgtzColor + pname + " sector "
				+ sector[hitIndex] + "  paddle " + paddle[hitIndex]);

		feedbackStrings.add(DataSupport.dgtzColor + "adc_left "
				+ ADCL[hitIndex] + "  adc_right " + ADCR[hitIndex]);
		feedbackStrings.add(DataSupport.dgtzColor + "tdc_left "
				+ TDCL[hitIndex] + "  tdc_right " + TDCR[hitIndex]);

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
	 * Get the index of the ftof hit
	 * 
	 * @param sect
	 *            the 1-based sector
	 * @param paddle
	 *            the 1-based paddle
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the index of a hit in a panel with these parameters, or -1 if not
	 *         found
	 */
	public int hitIndex(int sect, int paddle, int panelType) {

		int sector[] = sector(panelType);
		int paddles[] = paddle(panelType);

		if (sector == null) {
			return -1;
		}

		if (paddles == null) {
			Log.getInstance().warning(
					"null paddles array in hitIndex for panelType: "
							+ panelType + " sector: " + sect + " paddle: "
							+ paddle);
			return -1;
		}

		for (int i = 0; i < hitCount(panelType); i++) {
			if ((sect == sector[i]) && (paddle == paddles[i])) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get the hit count for panel 1A panel
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the hit count for panel 1A
	 */
	public int hitCount(int panelType) {
		int sector[] = sector(panelType);
		return (sector == null) ? 0 : sector.length;
	}


	class DataList extends ArrayList<Integer> {
		public int[] toIntArray() {
			if (isEmpty()) {
				return null;
			}
			
			int array[] = new int[size()];
			for (int i = 0; i < size(); i++) {
				array[i] = get(i);
			}
			return array;
		}
	}
}
