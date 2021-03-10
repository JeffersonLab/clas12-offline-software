package cnuphys.ced.event;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.event.EventListenerList;

import cnuphys.bCNU.graphics.colorscale.ColorScaleModel;
import cnuphys.bCNU.log.Log;
import cnuphys.ced.cedview.central.CentralXYView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IAccumulator;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.geometry.BSTGeometry;
import cnuphys.ced.geometry.BSTxyPanel;
import cnuphys.ced.geometry.FTOFGeometry;
import cnuphys.ced.geometry.GeoConstants;
import cnuphys.ced.geometry.PCALGeometry;
import cnuphys.ced.event.data.AdcHit;
import cnuphys.ced.event.data.AdcHitList;
import cnuphys.ced.event.data.AllEC;
import cnuphys.ced.event.data.CTOF;
import cnuphys.ced.event.data.DC;
import cnuphys.ced.event.data.DCTdcHit;
import cnuphys.ced.event.data.DCTdcHitList;
import cnuphys.ced.event.data.FTCAL;
import cnuphys.ced.event.data.FTOF;
import cnuphys.ced.event.data.HTCC2;
import cnuphys.ced.event.data.LTCC;
import cnuphys.ced.event.data.RTPC;
import cnuphys.ced.event.data.RTPCHit;
import cnuphys.ced.event.data.RTPCHitList;
import cnuphys.ced.event.data.BST;
import cnuphys.ced.event.data.CND;
import cnuphys.ced.event.data.TdcAdcHit;
import cnuphys.ced.event.data.TdcAdcHitList;

import org.jlab.io.base.DataEvent;

/**
 * Manages the accumulation of data
 * 
 * @author heddle
 * 
 */
public class AccumulationManager implements IAccumulator, IClasIoEventListener, IAccumulationListener {

	/** Indicates hat accumulation has started */
	public static final int ACCUMULATION_STARTED = 0;

	/** Indicates hat accumulation has been cancelled */
	public static final int ACCUMULATION_CANCELLED = -1;

	/** Indicates hat accumulation has finished */
	public static final int ACCUMULATION_FINISHED = 1;

	/** Indicates hat accumulation has received clear */
	public static final int ACCUMULATION_CLEAR = 2;

	// common colorscale
	public static ColorScaleModel colorScaleModel = new ColorScaleModel(getAccumulationValues(),
			ColorScaleModel.getWeatherMapColors(8));

	// the singleton
	private static AccumulationManager instance;

	// private static final Color NULLCOLOR = new Color(128, 128, 128);
	// private static final Color NULLCOLOR = Color.gray;

//	private static final Color HOTCOLOR = X11Colors.getX11Color("red");

	// CND accumulated accumulated data indices are sector, layer, order (0 or 1,
	// adc only)
	private int _CNDAccumulatedData[][][];

	// HTCC accumulated accumulated data indices are sector, ring, half
	private int _HTCCAccumulatedData[][][];

	// LTCC accumulated accumulated data indices are sector, half, ring
	// NOTICE THE DIFFERENT ORDER FROM HTCC
	private int _LTCCAccumulatedData[][][];

	// ftcc accumulated data
	private int _FTCALAccumulatedData[];
	
	//rtpc accumulated data
	private int _RTPCAccumulatedData[][];

	// dc accumulated data indices are sector, superlayer, layer, wire
	private int _DCAccumulatedData[][][][];

	// BST accumulated data (layer[0..7], sector[0..23])
	private int _BSTAccumulatedData[][];

	// BST accumulated data (layer[0..7], sector[0..23], strip [0..254])
	private int _BSTFullAccumulatedData[][][];

	// CTOF accumulated data
	private int _CTOFAccumulatedData[];

	// FTOF accumulated Data
	private int _FTOF1AAccumulatedData[][];
	private int _FTOF1BAccumulatedData[][];
	private int _FTOF2AccumulatedData[][];

	// EC [sector, stack (inner, outer), view (uvw), strip]
	private int _ECALAccumulatedData[][][][];

	// PCAL [sector, view (uvw), strip]
	private int _PCALAccumulatedData[][][];

	// overall event count
	private long _eventCount;

	/** Colors used for accumulated related feedback */
	public static final String accumulationFBColor = "$Pale Green$";

	// occupancy data by sector, superlayer
	public static double avgDcOccupancy[][] = new double[6][6];

	// event manager
	private ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// list of accumulation listeners
	private EventListenerList _listeners;

	/**
	 * private constructor for singleton.
	 */
	private AccumulationManager() {
		addAccumulationListener(this);
		_eventManager.addClasIoEventListener(this, 1);

		// FTCAL data
		_FTCALAccumulatedData = new int[476];
		
		// RTPC Data
		_RTPCAccumulatedData = new int[RTPC.NUMCOMPONENT][RTPC.NUMLAYER];

		// cnd data (24 sectors, 3 layers, left and right)
		_CNDAccumulatedData = new int[24][3][2];

		// htcc data
		_HTCCAccumulatedData = new int[GeoConstants.NUM_SECTOR][4][2];

		// ltcc data NOTICE THE DIFFERENT ORDER FROM HTCC
		_LTCCAccumulatedData = new int[GeoConstants.NUM_SECTOR][2][18];

		// dc data
		_DCAccumulatedData = new int[GeoConstants.NUM_SECTOR][GeoConstants.NUM_SUPERLAYER][GeoConstants.NUM_LAYER][GeoConstants.NUM_WIRE];

		// down to layer
		_BSTAccumulatedData = new int[8][];
		for (int lay0 = 0; lay0 < 8; lay0++) {
			int supl0 = lay0 / 2;
			_BSTAccumulatedData[lay0] = new int[BSTGeometry.sectorsPerSuperlayer[supl0]];
		}

		// _bstDgtzAccumulatedData = new int[8][24];

		// down to strip
		_BSTFullAccumulatedData = new int[8][][];
		for (int lay0 = 0; lay0 < 8; lay0++) {
			int supl0 = lay0 / 2;
			_BSTFullAccumulatedData[lay0] = new int[BSTGeometry.sectorsPerSuperlayer[supl0]][256];
		}

		// ctof storage
		_CTOFAccumulatedData = new int[48];

		// ftof storage
		_FTOF1AAccumulatedData = new int[6][FTOFGeometry.numPaddles[0]];
		_FTOF1BAccumulatedData = new int[6][FTOFGeometry.numPaddles[1]];
		_FTOF2AccumulatedData = new int[6][FTOFGeometry.numPaddles[2]];

		// ec and pcal storage
		_ECALAccumulatedData = new int[6][2][3][36];

		_PCALAccumulatedData = new int[6][3][];
		for (int sect0 = 0; sect0 < 6; sect0++) {
			for (int view0 = 0; view0 < 3; view0++) {
				_PCALAccumulatedData[sect0][view0] = new int[PCALGeometry.PCAL_NUMSTRIP[view0]];
			}
		}

		clear();
	}

	/**
	 * Clears all accumulated data.
	 */
	@Override
	public void clear() {
		_eventCount = 0;

		// clear ftcal
		for (int i = 0; i < _FTCALAccumulatedData.length; i++) {
			_FTCALAccumulatedData[i] = 0;
		}
		
		//clear RTPC
		for (int i = 0; i < RTPC.NUMCOMPONENT; i++) {
			for (int j = 0; j < RTPC.NUMLAYER; j++) {
				_RTPCAccumulatedData[i][j] = 0;
			}
		}

		// clear accumulated CND
		for (int sector = 0; sector < 24; sector++) {
			for (int layer = 0; layer < 3; layer++) {
				for (int leftright = 0; leftright < 2; leftright++) {
					_CNDAccumulatedData[sector][layer][leftright] = 0;
				}
			}
		}

		// clear accumulated HTCC
		for (int sector = 0; sector < GeoConstants.NUM_SECTOR; sector++) {
			for (int ring = 0; ring < 4; ring++) {
				for (int half = 0; half < 2; half++) {
					_HTCCAccumulatedData[sector][ring][half] = 0;
				}
			}
		}

		// clear accumulated LTCC
		// NOTICE THE DIFFERENT ORDER FROM HTCC
		for (int sector = 0; sector < GeoConstants.NUM_SECTOR; sector++) {
			for (int half = 0; half < 2; half++) {
				for (int ring = 0; ring < 18; ring++) {
					_LTCCAccumulatedData[sector][half][ring] = 0;
				}
			}
		}

		// clear accumulated dc data
		for (int sector = 0; sector < GeoConstants.NUM_SECTOR; sector++) {
			for (int superLayer = 0; superLayer < GeoConstants.NUM_SUPERLAYER; superLayer++) {
				avgDcOccupancy[sector][superLayer] = 0;
				for (int layer = 0; layer < GeoConstants.NUM_LAYER; layer++) {
					for (int wire = 0; wire < GeoConstants.NUM_WIRE; wire++) {
						_DCAccumulatedData[sector][superLayer][layer][wire] = 0;
					}
				}
			}
		}

		// clear ecal data
		for (int sector = 0; sector < 6; sector++) {
			for (int stack = 0; stack < 2; stack++) {
				for (int view = 0; view < 3; view++) {
					for (int strip = 0; strip < 36; strip++) {
						_ECALAccumulatedData[sector][stack][view][strip] = 0;
					}
				}
			}
		}

		// clear pcal data
		for (int sector = 0; sector < 6; sector++) {
			for (int view = 0; view < 3; view++) {
				for (int strip = 0; strip < PCALGeometry.PCAL_NUMSTRIP[view]; strip++) {
					_PCALAccumulatedData[sector][view][strip] = 0;
				}
			}
		}

		// clear bst panel accumulation
		for (int layer = 0; layer < 8; layer++) {
			int supl0 = layer / 2;
			for (int sector = 0; sector < BSTGeometry.sectorsPerSuperlayer[supl0]; sector++) {
				_BSTAccumulatedData[layer][sector] = 0;
				for (int strip = 0; strip < 256; strip++) {
					_BSTFullAccumulatedData[layer][sector][strip] = 0;
				}
			}
		}

		// clear CTOF data
		for (int i = 1; i < 48; i++) {
			_CTOFAccumulatedData[i] = 0;
		}

		// clear ftof data
		for (int sector = 0; sector < 6; sector++) {
			for (int paddle = 0; paddle < _FTOF1AAccumulatedData[0].length; paddle++) {
				_FTOF1AAccumulatedData[sector][paddle] = 0;
			}
			for (int paddle = 0; paddle < _FTOF1BAccumulatedData[0].length; paddle++) {
				_FTOF1BAccumulatedData[sector][paddle] = 0;
			}
			for (int paddle = 0; paddle < _FTOF2AccumulatedData[0].length; paddle++) {
				_FTOF2AccumulatedData[sector][paddle] = 0;
			}
		}

		notifyListeners(ACCUMULATION_CLEAR);
	}

	/**
	 * Public access to the singleton.
	 * 
	 * @return the singleton AccumulationManager
	 */
	public static AccumulationManager getInstance() {
		if (instance == null) {
			instance = new AccumulationManager();
		}
		return instance;
	}

	/**
	 * Get the accumulated CTOF data
	 * 
	 * @return the accumulated FTCAL data
	 */

	public int[] getAccumulatedCTOFData() {
		return _CTOFAccumulatedData;
	}

	/**
	 * Get the accumulated FTCAL data
	 * 
	 * @return the accumulated FTCAL data
	 */

	public int[] getAccumulatedFTCALData() {
		return _FTCALAccumulatedData;
	}
	
	/**
	 * Get the accumulated RTPC data
	 * 
	 * @return the accumulated RTPC data
	 */

	public int[][] getAccumulatedRTPCData() {
		return _RTPCAccumulatedData;
	}

	/**
	 * Get the accumulated CND data
	 * 
	 * @return the accumulated CND data
	 */
	public int[][][] getAccumulatedCNDData() {
		return _CNDAccumulatedData;
	}

	/**
	 * Get the accumulated HTCC data
	 * 
	 * @return the accumulated HTCC data
	 */
	public int[][][] getAccumulatedHTCCData() {
		return _HTCCAccumulatedData;
	}

	/**
	 * Get the accumulated LTCC data
	 * 
	 * @return the accumulated LTCC data
	 */
	public int[][][] getAccumulatedLTCCData() {
		return _LTCCAccumulatedData;
	}

	/**
	 * Get the accumulated EC data
	 * 
	 * @return the accumulated ec data
	 */
	public int[][][][] getAccumulatedECALData() {
		return _ECALAccumulatedData;
	}

	/**
	 * Get the accumulated PCAL data
	 * 
	 * @return the accumulated PCAL data
	 */
	public int[][][] getAccumulatedPCALData() {
		return _PCALAccumulatedData;
	}

	/**
	 * Get the accumulated DC data
	 * 
	 * @return the accumulated dc data
	 */
	public int[][][][] getAccumulatedDCData() {
		return _DCAccumulatedData;
	}

	// // BST accumulated data (layer[0..7], sector[0..23])
	// private int _BSTAccumulatedData[][];
	//
	// // BST accumulated data (layer[0..7], sector[0..23], strip [0..254])
	// private int _BSTFullAccumulatedData[][][];

	/**
	 * Get the median counts on any bst panel
	 * 
	 * @return the median counts for any bst panel.
	 */
	public int getMedianBSTCount() {
		return getMedian(_BSTAccumulatedData);
	}

	public int getMedianCTOFCount() {
		return getMedian(_CTOFAccumulatedData);
	}

	/**
	 * Get the median counts on any BST strip
	 * 
	 * @return the median counts for any BST strip.
	 */
	public int getMedianFullBSTCount() {
		return getMedian(_BSTFullAccumulatedData);
	}

	/**
	 * Get the median count of accumulated hits
	 * 
	 * @return the median count of accumulated hits
	 */
	public int getMedianPCALCount() {
		return getMedian(_PCALAccumulatedData);
	}

	// _ECALAccumulatedData = new int[6][2][3][36];

	public int getMedianECALCount(int plane) {
		return getMedian(_ECALAccumulatedData);
	}

	/**
	 * Get the median count for a given superlayer across all sectors
	 * 
	 * @param suplay the superlayer 0..5
	 * @return the median count for a given superlayer across all sectors
	 */
	public int getMedianDCCount(int suplay) {
		ArrayList<Integer> v = new ArrayList<>(24192);

		// specialized because of the suplay fixed
		for (int sect = 0; sect < 6; sect++) {
			for (int lay = 0; lay < 6; lay++) {
				for (int wire = 0; wire < 12; wire++) {
					int count = _DCAccumulatedData[sect][suplay][lay][wire];
					v.add(count);
				}
			}
		}

		int size = v.size();
		if (size == 0) {
			return 0;
		}

		Collections.sort(v);
		return v.get(size / 2);
	}

	/**
	 * Get the accumulated Bst panel data
	 * 
	 * @return the accumulated bst panel data
	 */
	public int[][] getAccumulatedBSTData() {
		return _BSTAccumulatedData;
	}

	/**
	 * Get the accumulated full Bst strip data
	 * 
	 * @return the accumulated bst strip data
	 */
	public int[][][] getAccumulatedBSTFullData() {
		return _BSTFullAccumulatedData;
	}

	/**
	 * Get the median counts for FTCAL
	 * 
	 * @return the median counts for FTCAL
	 */
	public int getMedianFTCALCount() {
		return getMedian(_FTCALAccumulatedData);
	}
	
	/**
	 * Get the median counts for RTPC
	 * 
	 * @return the median counts for RTPC
	 */
	public int getMedianRTPCCount() {
		return getMedian(_RTPCAccumulatedData);
	}

	/**
	 * Get the median counts for CND
	 * 
	 * @return the median counts for CND
	 */
	public int getMedianCNDCount() {
		return getMedian(_CNDAccumulatedData);
	}

	/**
	 * Get the median counts for HTCC
	 * 
	 * @return the median counts for HTCC
	 */
	public int getMedianHTCCCount() {
		return getMedian(_HTCCAccumulatedData);
	}

	/**
	 * Get the median counts for LTCC
	 * 
	 * @return the median counts for LTCC
	 */
	public int getMedianLTCCCount() {
		return getMedian(_LTCCAccumulatedData);
	}

	/**
	 * Get the accumulated ftof panel 1a
	 * 
	 * @return the accumulated ftof panel 1a
	 */
	public int[][] getAccumulatedFTOF1AData() {
		return _FTOF1AAccumulatedData;
	}

	/**
	 * Get the accumulated ftof panel 1b
	 * 
	 * @return the accumulated ftof panel 1b
	 */
	public int[][] getAccumulatedFTOF1BData() {
		return _FTOF1BAccumulatedData;
	}

	/**
	 * Get the accumulated ftof panel 2
	 * 
	 * @return the accumulated ftof panel 2
	 */
	public int[][] getAccumulatedFTOF2Data() {
		return _FTOF2AccumulatedData;
	}

	/**
	 * Get the median counts on any ftof1a panel
	 * 
	 * @return the median counts for any ftof1a panel.
	 */
	public int getMedianFTOF1ACount() {
		return getMedian(_FTOF1AAccumulatedData);
	}

	/**
	 * Get the median counts on any ftof1b panel
	 * 
	 * @return the median counts for any ftof1b panel.
	 */
	public int getMedianFTOF1BCount() {
		return getMedian(_FTOF1BAccumulatedData);
	}

	/**
	 * Get the median counts on any ftof2 panel
	 * 
	 * @return the median counts for any ftof2 panel.
	 */
	public int getMedianFTOF2Count() {
		return getMedian(_FTOF2AccumulatedData);
	}

	/**
	 * Get the color to use
	 * 
	 * @param fract the fraction (compared to max hits)
	 * @return the color to use
	 */
	public Color getColor(ColorScaleModel model, double fract) {
		fract = Math.max(0.0001f, Math.min(fract, 0.9999f));
//		if (fract < 1.0e-6) {
//			return NULLCOLOR;
//		} else if (fract > 1.0) {
//			return model.getHotColor();
//		}
		return model.getColor(fract);
	}

	/**
	 * Get a color via getColor but add an alpha value
	 * 
	 * @param value the value
	 * @param alpha the alpha value [0..255]
	 * @return the color corresponding to the value.
	 */
	public Color getAlphaColor(ColorScaleModel model, double value, int alpha) {
		Color c = getColor(model, value);
		Color color = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
		return color;
	}

	/**
	 * Get the number of events in the current accumulation
	 * 
	 * @return the number of events in the current accumulation
	 */
	public long getAccumulationEventCount() {
		return _eventCount;
	}

	/**
	 * Get the average occupancy for a given sector and superlayer
	 * 
	 * @param sect0 0 based sector 0..5
	 * @param supl0 0 based superlayer 0..5
	 * @return the occupancy
	 */
	public double getAverageDCOccupancy(int sect0, int supl0) {
		return avgDcOccupancy[sect0][supl0];
	}

	/**
	 * Here is an event, so increment the correct accumulation arrays
	 */
	@Override
	public void newClasIoEvent(DataEvent event) {

		// only care if I am accumulating
		if (!_eventManager.isAccumulating() || (event == null)) {
			return;
		}

		_eventCount++;

		// FTCal Data
		AdcHitList ftcalList = FTCAL.getInstance().updateAdcList();
		accumFTCAL(ftcalList);
		
		//RTPCData
		RTPCHitList rtpcList = RTPC.getInstance().updateAdcList();
		accumRTPC(rtpcList);
		
		// CND a special case
		CND.getInstance().updateData();
		accumCND();

		// htcc data
		AdcHitList htccList = HTCC2.getInstance().updateAdcList();
		accumHTCC(htccList);

		// ltcc data
		AdcHitList ltccList = LTCC.getInstance().updateAdcList();
		accumLTCC(ltccList);

		// dc data
		DCTdcHitList dclist = DC.getInstance().updateTdcAdcList();
		accumDC(dclist);

		// ctof data
		TdcAdcHitList ctoflist = CTOF.getInstance().updateTdcAdcList();
		accumCTOF(ctoflist);

		// ftof data
		TdcAdcHitList ftoflist = FTOF.getInstance().updateTdcAdcList();
		accumFTOF(ftoflist);

		// all ec
		TdcAdcHitList allEClist = AllEC.getInstance().updateTdcAdcList();
		accumAllEC(allEClist);

		// BST
		AdcHitList bstList = BST.getInstance().updateAdcList();
		accumBST(bstList);

	}

	// accumulate bst
	private void accumBST(AdcHitList list) {
		if ((list == null) || list.isEmpty()) {
			return;
		}

		for (AdcHit hit : list) {
			BSTxyPanel panel = CentralXYView.getPanel(hit.layer, hit.sector);
			if (panel != null) {
				int lay0 = hit.layer - 1;
				int sect0 = hit.sector - 1;
				int strip0 = hit.component - 1;
				try {
					_BSTAccumulatedData[lay0][sect0] += 1;

					if (strip0 >= 0) {
						_BSTFullAccumulatedData[lay0][sect0][strip0] += 1;
					}

				} catch (ArrayIndexOutOfBoundsException e) {
					String msg = String.format("Index out of bounds (BST). Event# %d lay %d sect %d  strip %d",
							_eventManager.getSequentialEventNumber(), hit.layer, hit.sector, hit.component);
					Log.getInstance().warning(msg);
				}

			}
		}
	}

	// accumulate ftcal
	private void accumFTCAL(AdcHitList list) {
		if ((list == null) || list.isEmpty()) {
			return;
		}

		for (AdcHit hit : list) {
			_FTCALAccumulatedData[hit.component] += 1;
		}
	}
	
	// accumulate rtpc
		private void accumRTPC(RTPCHitList list) {
			if ((list == null) || list.isEmpty()) {
				return;
			}

			for (RTPCHit hit : list) {
				int cm1 = hit.component-1;
				int lm1 = hit.layer-1;
				_RTPCAccumulatedData[cm1][lm1] += 1;
			}
		}

	// accumulate CND which is a special case
	private void accumCND() {
		CND cnd = CND.getInstance();
		int adcCount = cnd.getCountAdc();
		if (adcCount > 0) {
			byte[] sect = cnd.adc_sect;
			byte[] layer = cnd.adc_layer;
			byte[] order = cnd.adc_order;

			for (int i = 0; i < adcCount; i++) {

				int sect0 = sect[i] - 1;
				int lay0 = layer[i] - 1;
				// note order is already a zero based quantity
				int ord0 = order[i];

				_CNDAccumulatedData[sect0][lay0][ord0] += 1;
			}
		}
	}

	// accumulate htcc
	private void accumHTCC(AdcHitList list) {
		if ((list == null) || list.isEmpty()) {
			return;
		}

		for (AdcHit hit : list) {
			if (hit != null) {
				int sect0 = hit.sector - 1; // make 0 based
				int ring0 = hit.component - 1; // make 0 based
				int half0 = hit.layer - 1; // make 0 based

				if (sect0 >= 0) {
					try {
						_HTCCAccumulatedData[sect0][ring0][half0] += 1;
					} catch (ArrayIndexOutOfBoundsException e) {
						String msg = String.format("HTCC index out of bounds. Event# %d sect %d ring %d half %d",
								_eventManager.getSequentialEventNumber(), hit.sector, hit.layer, hit.component);
						Log.getInstance().warning(msg);
						System.err.println(msg);
					}
				}
			}
		}
	}

	// accumulate ltcc
	private void accumLTCC(AdcHitList list) {
		if ((list == null) || list.isEmpty()) {
			return;
		}

		for (AdcHit hit : list) {
			if (hit != null) {
				int sect0 = hit.sector - 1; // make 0 based

				int half0 = hit.layer - 1; // make 0 based
				int ring0 = hit.component - 1; // make 0 based

				if (sect0 >= 0) {
					try {
						_LTCCAccumulatedData[sect0][half0][ring0] += 1;
					} catch (ArrayIndexOutOfBoundsException e) {
						String msg = String.format("LTCC index out of bounds. Event# %d sect %d ring %d half %d",
								_eventManager.getSequentialEventNumber(), hit.sector, hit.layer, hit.component);
						Log.getInstance().warning(msg);
						System.err.println(msg);
					}
				}
			}
		}
	}

	// accumulate all ec
	private void accumAllEC(TdcAdcHitList list) {
		if ((list == null) || list.isEmpty()) {
			return;
		}

		for (TdcAdcHit hit : list) {
			if (hit != null) {
				try {
					int sect0 = hit.sector - 1;
					int layer = hit.layer;
					int strip0 = hit.component - 1;

					if (layer < 4) { // pcal
						int view0 = layer - 1;
						_PCALAccumulatedData[sect0][view0][strip0] += 1;
					} else { // ec
						layer -= 4; // convert to 0..5
						int stack0 = layer / 3; // 000,111
						int view0 = layer % 3; // 012012

						_ECALAccumulatedData[sect0][stack0][view0][strip0] += 1;

					}

				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
					Log.getInstance().warning("In accumulation, ECAL hit has bad indices: " + hit);
				}
			}

		}

	}

	// accumulate dc data
	private void accumDC(DCTdcHitList list) {
		if ((list == null) || list.isEmpty()) {
			return;
		}

		for (DCTdcHit hit : list) {
			if (hit.inRange()) {
				_DCAccumulatedData[hit.sector - 1][hit.superlayer - 1][hit.layer6 - 1][hit.wire - 1] += 1;

				// _maxDCCount = Math.max(
				// _DCAccumulatedData[hit.sector - 1][hit.superlayer - 1][hit.layer6 -
				// 1][hit.wire - 1],
				// _maxDCCount);
			} else {
				Log.getInstance().warning("In accumulation, DC hit has bad indices: " + hit);
			}

		}
	}

	// for ctof accumulating
	private void accumCTOF(TdcAdcHitList list) {

		if ((list == null) || list.isEmpty()) {
			return;
		}

		for (TdcAdcHit hit : list) {
			if (hit != null) {
				try {
					_CTOFAccumulatedData[hit.component - 1] += 1;
				} catch (ArrayIndexOutOfBoundsException e) {
					Log.getInstance().warning("In accumulation, CTOF hit has bad indices: " + hit);
				}
			}

		}

	}

	// for ftof accumulating
	private void accumFTOF(TdcAdcHitList list) {

		if ((list == null) || list.isEmpty()) {
			return;
		}

		for (TdcAdcHit hit : list) {
			if (hit != null) {
				try {
					int sect0 = hit.sector - 1;
					int paddle0 = hit.component - 1;

					if (hit.layer == 1) {
						_FTOF1AAccumulatedData[sect0][paddle0] += 1;
					} else if (hit.layer == 2) {
						_FTOF1BAccumulatedData[sect0][paddle0] += 1;
					}
					if (hit.layer == 3) {
						_FTOF2AccumulatedData[sect0][paddle0] += 1;
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					Log.getInstance().warning("In accumulation, FTOF hit has bad indices: " + hit);
				}
			}

		}

	}

	@Override
	public void openedNewEventFile(String path) {
	}

	/**
	 * Change the event source type
	 * 
	 * @param source the new source: File, ET
	 */
	@Override
	public void changedEventSource(ClasIoEventManager.EventSourceType source) {
	}

	/**
	 * Get the values array for the color scale. Note the range is 0..1 so use
	 * fraction of max value to get color
	 * 
	 * @return the values array.
	 */
	private static double getAccumulationValues()[] {

		int len = ColorScaleModel.getWeatherMapColors(8).length + 1;

		double values[] = new double[len];

		double min = 0.0;
		double max = 1.0;
		double del = (max - min) / (values.length - 1);
		for (int i = 0; i < values.length; i++) {
			values[i] = i * del;
		}
		return values;
	}

	/**
	 * Notify listeners we of an accumulation event
	 * 
	 * @param reason should be one of the ACCUMULATION_X constants
	 * 
	 */
	public void notifyListeners(int reason) {

		if (_listeners != null) {

			// Guaranteed to return a non-null array
			Object[] listeners = _listeners.getListenerList();

			// This weird loop is the bullet proof way of notifying all
			// listeners.
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] == IAccumulationListener.class) {
					((IAccumulationListener) listeners[i + 1]).accumulationEvent(reason);
				}
			}
		}
	}

	/**
	 * Remove an Accumulation listener.
	 * 
	 * @param listener the Accumulation listener to remove.
	 */
	public void removeAccumulationListener(IAccumulationListener listener) {

		if (listener == null) {
			return;
		}

		if (_listeners != null) {
			_listeners.remove(IAccumulationListener.class, listener);
		}
	}

	/**
	 * Add an Accumulation listener.
	 * 
	 * @param listener the Accumulation listener to add.
	 */
	public void addAccumulationListener(IAccumulationListener listener) {

		if (listener == null) {
			return;
		}

		if (_listeners == null) {
			_listeners = new EventListenerList();
		}

		_listeners.add(IAccumulationListener.class, listener);
	}

	@Override
	public void accumulationEvent(int reason) {
		switch (reason) {
		case AccumulationManager.ACCUMULATION_STARTED:
			break;

		case AccumulationManager.ACCUMULATION_CANCELLED:
		case AccumulationManager.ACCUMULATION_FINISHED:

			if (_eventCount != 0) {

				for (int sect0 = 0; sect0 < 6; sect0++) {
					for (int supl0 = 0; supl0 < 6; supl0++) {

						long count = 0;

						for (int lay0 = 0; lay0 < 6; lay0++) {
							for (int wire0 = 0; wire0 < 112; wire0++) {
								count += _DCAccumulatedData[sect0][supl0][lay0][wire0];
							}
						} // lay0

						double avgHits = avgDcOccupancy[sect0][supl0] = ((double) count) / _eventCount;
						// divide by num wires in superlayer
						avgDcOccupancy[sect0][supl0] = avgHits / (6 * 112);
					} // supl0
				} // sect0
			} // _eventCount != 0

			break;
		}
	}

	/**
	 * Get the prectange hit rate in the accumulated data for a given wire
	 * 
	 * @param sect0 0 based sector 0..5
	 * @param supl0 0 based superlayer 0..5
	 * @param lay0  0 based layer 0..5
	 * @param wire0 0 based wire 0..111
	 * @return the occupancy
	 */
	public double getAccumulatedWireHitPercentage(int sect0, int supl0, int lay0, int wire0) {

		if (_eventCount < 1) {
			return 0;
		}
		return 100.0 * (_DCAccumulatedData[sect0][supl0][lay0][wire0] / (double) _eventCount);
	}

	/**
	 * Tests whether this listener is interested in events while accumulating
	 * 
	 * @return <code>true</code> if this listener is NOT interested in events while
	 *         accumulating
	 */
	@Override
	public boolean ignoreIfAccumulating() {
		return false;
	}

	// get the median of a 1D array of ints
	private int getMedian(int[] data) {

		if ((data == null) || (data.length < 1)) {
			return 0;
		}

		ArrayList<Integer> v = new ArrayList<>(data.length);

		for (int val : data) {
			if (val != 0) {
				v.add(val);
			}
		}

		int size = v.size();
		if (size == 0) {
			return 0;
		}

		Collections.sort(v);
		return v.get(size / 2);
	}

	// get the median of a 2D array of ints
	private int getMedian(int[][] data) {

		if (data == null) {
			return 0;
		}

		ArrayList<Integer> v = new ArrayList<>(1000);

		for (int iarry[] : data) {
			for (int val : iarry) {
				if (val != 0) {
					v.add(val);
				}

			}
		}

		int size = v.size();
		if (size == 0) {
			return 0;
		}

		Collections.sort(v);
		return v.get(size / 2);
	}

	// get the median of a 3D array of ints
	private int getMedian(int[][][] data) {

		if (data == null) {
			return 0;
		}

		ArrayList<Integer> v = new ArrayList<>(1000);

		for (int iarry1[][] : data) {
			for (int iarray2[] : iarry1) {
				for (int val : iarray2) {
					if (val != 0) {
						v.add(val);
					}
				}
			}
		}

		int size = v.size();
		if (size == 0) {
			return 0;
		}
		Collections.sort(v);
		return v.get(size / 2);
	}

	// get the median of a 4D array of ints
	private int getMedian(int[][][][] data) {

		if (data == null) {
			return 0;
		}

		ArrayList<Integer> v = new ArrayList<>(1000);

		for (int iarry1[][][] : data) {
			for (int iarray2[][] : iarry1) {
				for (int iarray3[] : iarray2) {
					for (int val : iarray3) {
						v.add(val);
					}
				}
			}
		}

		int size = v.size();
		if (size == 0) {
			return 0;
		}
		Collections.sort(v);
		return v.get(size / 2);
	}

}
