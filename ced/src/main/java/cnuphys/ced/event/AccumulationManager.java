package cnuphys.ced.event;

import java.awt.Color;
import javax.swing.event.EventListenerList;

import cnuphys.bCNU.graphics.colorscale.ColorScaleModel;
import cnuphys.bCNU.log.Log;
import cnuphys.ced.cedview.central.CentralXYView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IAccumulator;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.geometry.BSTGeometry;
import cnuphys.ced.geometry.SVTxyPanel;
import cnuphys.ced.geometry.FTOFGeometry;
import cnuphys.ced.geometry.GeoConstants;
import cnuphys.ced.geometry.PCALGeometry;
import cnuphys.ced.event.data.AdcHit;
import cnuphys.ced.event.data.AdcHitList;
import cnuphys.ced.event.data.AllEC;
import cnuphys.ced.event.data.BST;
import cnuphys.ced.event.data.CTOF;
import cnuphys.ced.event.data.DC;
import cnuphys.ced.event.data.DCTdcHit;
import cnuphys.ced.event.data.DCTdcHitList;
import cnuphys.ced.event.data.FTCAL;
import cnuphys.ced.event.data.FTOF;
import cnuphys.ced.event.data.HTCC2;
import cnuphys.ced.event.data.LTCC;
import cnuphys.ced.event.data.SVT;
import cnuphys.ced.event.data.TdcAdcHit;
import cnuphys.ced.event.data.TdcAdcHitList;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.io.base.DataEvent;

/**
 * Manages the accumulation of data
 * 
 * @author heddle
 * 
 */
public class AccumulationManager
		implements IAccumulator, IClasIoEventListener, IAccumulationListener {

	/** Indicates hat accumulation has started */
	public static final int ACCUMULATION_STARTED = 0;

	/** Indicates hat accumulation has been cancelled */
	public static final int ACCUMULATION_CANCELLED = -1;

	/** Indicates hat accumulation has finished */
	public static final int ACCUMULATION_FINISHED = 1;

	/** Indicates hat accumulation has received clear */
	public static final int ACCUMULATION_CLEAR = 2;

	// common colorscale
	public static ColorScaleModel colorScaleModel = new ColorScaleModel(
			getAccumulationValues(), getAccumulationColors());

	// the singleton
	private static AccumulationManager instance;

	private static final Color NULLCOLOR = new Color(128, 128, 128);

	// HTCC accumulated accumulated data indices are sector, ring, half
	private int _HTCCAccumulatedData[][][];
	private int _maxHTCCCount;
	
	// LTCC accumulated accumulated data indices are sector, half, ring
	//NOTICE THE DIFFERENT ORDER FROM HTCC
	private int _LTCCAccumulatedData[][][];
	private int _maxLTCCCount;


	//ftcc accumulated data
	private int _FTCALAccumulatedData[];
	private int _maxFTCALCount;
	
	// dc accumulated data indices are sector, superlayer, layer, wire
	private int _DCAccumulatedData[][][][];
	private int _maxDCCount;

	// SVT accumulated data (layer[0..7], sector[0..23])
	private int _SVTAccumulatedData[][];
	private int _maxSVTCount;

	// SVT accumulated data (layer[0..7], sector[0..23], strip [0..254])
	private int _SVTFullAccumulatedData[][][];
	private int _maxSVTFullCount;
	
	//CTOF accumulated data
	private int _CTOFAccumulatedData[];
	private int _maxCTOFCount;

	// FTOF accumulated Data
	private int _FTOF1AAccumulatedData[][];
	private int _FTOF1BAccumulatedData[][];
	private int _FTOF2AccumulatedData[][];
	private int _maxFTOFCount;

	// EC [sector, stack (inner, outer), view (uvw), strip]
	private int _ECALAccumulatedData[][][][];
	private int _maxECALCount;

	// PCAL [sector, view (uvw), strip]
	private int _PCALAccumulatedData[][][];
	private int _maxPCALCount;

	// overall event count
	private long _eventCount;

	/** Colors used for accumulated related feedback */
	public static final String accumulationFBColor = "$Pale Green$";

	// occupancy data by sector, superlayer
	public static double avgDcOccupancy[][] = new double[6][6];


	//event manager
	private ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// list of accumulation listeners
	private EventListenerList _listeners;

	/**
	 * private constructor for singleton.
	 */
	private AccumulationManager() {
		addAccumulationListener(this);
		_eventManager.addClasIoEventListener(this, 1);
		
		//FTCAL data
		_FTCALAccumulatedData = new int[476];
		
		//htcc data
		_HTCCAccumulatedData = new int[GeoConstants.NUM_SECTOR][4][2];
		
		//ltcc data NOTICE THE DIFFERENT ORDER FROM HTCC
		_LTCCAccumulatedData = new int[GeoConstants.NUM_SECTOR][2][18];

		//dc data
		_DCAccumulatedData = new int[GeoConstants.NUM_SECTOR][GeoConstants.NUM_SUPERLAYER][GeoConstants.NUM_LAYER][GeoConstants.NUM_WIRE];

		// down to layer
		_SVTAccumulatedData = new int[8][];
		for (int lay0 = 0; lay0 < 8; lay0++) {
			int supl0 = lay0 / 2;
			_SVTAccumulatedData[lay0] = new int[BSTGeometry.sectorsPerSuperlayer[supl0]];
		}

		// _bstDgtzAccumulatedData = new int[8][24];

		// down to strip
		_SVTFullAccumulatedData = new int[8][][];
		for (int lay0 = 0; lay0 < 8; lay0++) {
			int supl0 = lay0 / 2;
			_SVTFullAccumulatedData[lay0] = new int[BSTGeometry.sectorsPerSuperlayer[supl0]][256];
		}
		
		//ctof storage
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
		_maxFTCALCount = 0;

		//clear accumulated HTCC
		for (int sector = 0; sector < GeoConstants.NUM_SECTOR; sector++) {
			for (int ring = 0; ring < 4; ring++) {
				for (int half = 0; half < 2; half++) {
					_HTCCAccumulatedData[sector][ring][half] = 0;
				}
			}
		}
		_maxHTCCCount = 0;

		// clear accumulated LTCC
		// NOTICE THE DIFFERENT ORDER FROM HTCC
		for (int sector = 0; sector < GeoConstants.NUM_SECTOR; sector++) {
			for (int half = 0; half < 2; half++) {
				for (int ring = 0; ring < 18; ring++) {
					_LTCCAccumulatedData[sector][half][ring] = 0;
				}
			}
		}
		_maxLTCCCount = 0;
		
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
		_maxDCCount = 0;

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
		_maxECALCount = 0;

		// clear pcal data
		for (int sector = 0; sector < 6; sector++) {
			for (int view = 0; view < 3; view++) {
				for (int strip = 0; strip < PCALGeometry.PCAL_NUMSTRIP[view]; strip++) {
					_PCALAccumulatedData[sector][view][strip] = 0;
				}
			}
		}
		_maxPCALCount = 0;

		// clear bst panel accumulation
		for (int layer = 0; layer < 8; layer++) {
			int supl0 = layer / 2;
			for (int sector = 0; sector < BSTGeometry.sectorsPerSuperlayer[supl0]; sector++) {
				_SVTAccumulatedData[layer][sector] = 0;
				for (int strip = 0; strip < 256; strip++) {
					_SVTFullAccumulatedData[layer][sector][strip] = 0;
				}
			}
		}
		_maxSVTCount = 0;
		_maxSVTFullCount = 0;
		
		//clear CTOF data
		for (int i = 1; i < 48; i++) {
			_CTOFAccumulatedData[i] = 0;
		}
		_maxCTOFCount = 0;

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
		_maxFTOFCount = 0;

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

	/**
	 * Get the max counts on any wire
	 * 
	 * @return the max counts for any DC wire.
	 */
	public int getMaxDCCount() {
		return _maxDCCount;
	}

	/**
	 * Get the accumulated Bst panel data
	 * 
	 * @return the accumulated bst panel data
	 */
	public int[][] getAccumulatedSVTData() {
		return _SVTAccumulatedData;
	}

	/**
	 * Get the accumulated full Bst strip data
	 * 
	 * @return the accumulated bst strip data
	 */
	public int[][][] getAccumulatedSVTFullData() {
		return _SVTFullAccumulatedData;
	}
	
	public int getMaxFTCALCount() {
		return _maxFTCALCount;
	}

	/**
	 * Get the max counts for ec strips
	 * 
	 * @return the max counts for ec strips.
	 */
	public int getMaxECALCount() {
		return _maxECALCount;
	}

	/**
	 * Get the max counts for HTCC
	 * 
	 * @return the max counts for HTCC
	 */
	public int getMaxHTCCCount() {
		return _maxHTCCCount;
	}
	
	/**
	 * Get the max counts for LTCC
	 * 
	 * @return the max counts for LTCC
	 */
	public int getMaxLTCCCount() {
		return _maxLTCCCount;
	}


	/**
	 * Get the max counts for pcal strips
	 * 
	 * @return the max counts for pcal strips.
	 */
	public int getMaxPCALCount() {
		return _maxPCALCount;
	}

	/**
	 * Get the max counts on any bst panel
	 * 
	 * @return the max counts for any bst panel.
	 */
	public int getMaxSVTCount() {
		return _maxSVTCount;
	}

	/**
	 * Get the max counts on any bst strip
	 * 
	 * @return the max counts for any bst strip.
	 */
	public int getMaxFullBSTCount() {
		return _maxSVTFullCount;
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
	 * Get the max counts for CTOFf panel
	 * 
	 * @return the max counts for any CTOF paddle.
	 */
	public int getMaxCTOFCount() {
		return _maxCTOFCount;
	}

	/**
	 * Get the max counts on any ftof panel
	 * 
	 * @return the max counts for any ftof panel.
	 */
	public int getMaxFTOFCount() {
		return _maxFTOFCount;
	}

	//
	// /**
	// * @return the colorScaleModel
	// */
	// public static ColorScaleModel getColorScaleModel() {
	// return colorScaleModel;
	// }

	/**
	 * Get the color to use
	 * 
	 * @param fract the fraction (compared to max hits)
	 * @return the color to use
	 */
	public Color getColor(double fract) {
		if (fract < 1.0e-6) {
			return NULLCOLOR;
		}
		return colorScaleModel.getColor(fract);
	}

	/**
	 * Get a color via getColor but add an alpha value
	 * 
	 * @param value the value
	 * @param alpha the alpha value [0..255]
	 * @return the color corresponding to the value.
	 */
	public Color getAlphaColor(double value, int alpha) {
		Color c = getColor(value);
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
	 * New fast mc event
	 * @param event the generated physics event
	 */
	@Override
	public void newFastMCGenEvent(PhysicsEvent event) {
		
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
		
		//FTCal Data
		AdcHitList ftcalList = FTCAL.getInstance().updateAdcList();
		accumFTCAL(ftcalList);

		//htcc data
		AdcHitList htccList = HTCC2.getInstance().updateAdcList();
		accumHTCC(htccList);
		
		//ltcc data
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


		//SVT
		AdcHitList svtList = SVT.getInstance().updateAdcList();
		accumSVT(svtList);

	}
	
	//accumulate svt
	private void accumSVT(AdcHitList list) {
		if ((list == null) || list.isEmpty()) {
			return;
		}

		for (AdcHit hit : list) {
			SVTxyPanel panel = CentralXYView.getPanel(hit.layer,
					hit.sector);
			if (panel != null) {
				int lay0 = hit.layer - 1;
				int sect0 = hit.sector - 1;
				int strip0 = hit.component - 1;
				try {
					_SVTAccumulatedData[lay0][sect0] += 1;
					_maxSVTCount = Math.max(
							_SVTAccumulatedData[lay0][sect0],
							_maxSVTCount);

					if (strip0 >= 0) {
						_SVTFullAccumulatedData[lay0][sect0][strip0] += 1;
						_maxSVTFullCount = Math.max(
								_SVTFullAccumulatedData[lay0][sect0][strip0],
								_maxSVTFullCount);
					}

				} catch (ArrayIndexOutOfBoundsException e) {
					String msg = String.format(
							"Index out of bounds (BST). Event# %d lay %d sect %d  strip %d",
							_eventManager.getEventNumber(),
							hit.layer,
							hit.sector,
							hit.component);
					Log.getInstance().warning(msg);
				}

			}
		}
	}

	//accumulate ftcal
	private void accumFTCAL(AdcHitList list) {
		if ((list == null) || list.isEmpty()) {
			return;
		}

		for (AdcHit hit : list) {
			_FTCALAccumulatedData[hit.component] += 1;
			_maxFTCALCount = Math.max(_maxFTCALCount, _FTCALAccumulatedData[hit.component]);
		}
	}
	
	//accumulate htcc
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
						
						_maxHTCCCount = Math.max(_HTCCAccumulatedData[sect0][ring0][half0],
								_maxHTCCCount);

					} catch (ArrayIndexOutOfBoundsException e) {
						String msg = String.format("HTCC index out of bounds. Event# %d sect %d ring %d half %d",
								_eventManager.getEventNumber(), hit.sector, hit.layer, hit.component);
						Log.getInstance().warning(msg);
						System.err.println(msg);
					}
				}
			}
		}
	}
	
	//accumulate ltcc
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
						
						_maxLTCCCount = Math.max(_LTCCAccumulatedData[sect0][half0][ring0],
								_maxLTCCCount);

					} catch (ArrayIndexOutOfBoundsException e) {
						String msg = String.format("LTCC index out of bounds. Event# %d sect %d ring %d half %d",
								_eventManager.getEventNumber(), hit.sector, hit.layer, hit.component);
						Log.getInstance().warning(msg);
						System.err.println(msg);
					}
				}
			}
		}
	}

	//accumulate all ec
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

						_maxPCALCount = Math.max(_PCALAccumulatedData[sect0][view0][strip0], _maxPCALCount);
					} else { // ec
						layer -= 4; // convert to 0..5
						int stack0 = layer / 3; // 000,111
						int view0 = layer % 3; // 012012

						_ECALAccumulatedData[sect0][stack0][view0][strip0] += 1;

						_maxECALCount = Math.max(_ECALAccumulatedData[sect0][stack0][view0][strip0],
								_maxECALCount);

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

				_maxDCCount = Math.max(
						_DCAccumulatedData[hit.sector - 1][hit.superlayer - 1][hit.layer6 - 1][hit.wire - 1],
						_maxDCCount);
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
					_CTOFAccumulatedData[hit.component-1] += 1; 
					_maxCTOFCount = Math.max(_CTOFAccumulatedData[hit.component-1], _maxCTOFCount);
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
						_maxFTOFCount = Math.max(_FTOF1AAccumulatedData[sect0][paddle0], _maxFTOFCount);
					} else if (hit.layer == 2) {
						_FTOF1BAccumulatedData[sect0][paddle0] += 1;
						_maxFTOFCount = Math.max(_FTOF1BAccumulatedData[sect0][paddle0], _maxFTOFCount);
					}
					if (hit.layer == 3) {
						_FTOF2AccumulatedData[sect0][paddle0] += 1;
						_maxFTOFCount = Math.max(_FTOF2AccumulatedData[sect0][paddle0], _maxFTOFCount);
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
	 * @param source the new source: File, ET, FastMC
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

		int len = getAccumulationColors().length + 1;

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
	 * Get the color array for the plot.
	 * 
	 * @return the color array for the plot.
	 */
	private static Color getAccumulationColors()[] {

		// int r[] = { 255, 176, 255, 255, 255, 255, 255, 200, 150 };
		// int g[] = { 255, 224, 255, 255, 165, 100, 0, 0, 0 };
		// int b[] = { 255, 230, 128, 0, 0, 0, 0, 0, 0 };

		// int r[] = { 176, 124, 0, 173, 255, 255, 255, 255, 139 };
		// int g[] = { 224, 252, 255, 255, 255, 165, 69, 0, 0 };
		// int b[] = { 230, 0, 0, 47, 0, 0, 0, 0, 0 };

//		int r[] = { 240, 176, 124, 173, 255, 255, 255, 255, 139 };
		int r[] = { 0, 173, 255, 255, 255, 255, 139 };
//		int g[] = { 248, 224, 255, 255, 255, 165, 69, 0, 0 };
		int g[] = { 127, 255, 255, 165, 69, 0, 0 };
	//	int b[] = { 255, 230, 0, 47, 0, 0, 0, 0, 0 };
		int b[] = { 0, 47, 0, 0, 0, 0, 0 };

		int n = 8;

		double f = 1.0 / n;

		int len = r.length;
		int colorlen = (len - 1) * n + 1;
		Color colors[] = new Color[colorlen];

		int k = 0;
		for (int i = 0; i < (len - 1); i++) {
			for (int j = 0; j < n; j++) {
				int rr = r[i] + (int) (j * f * (r[i + 1] - r[i]));
				int gg = g[i] + (int) (j * f * (g[i + 1] - g[i]));
				int bb = b[i] + (int) (j * f * (b[i + 1] - b[i]));
				colors[k] = new Color(rr, gg, bb);
				k++;
			}
		}

		colors[(len - 1) * n] = new Color(r[len - 1], g[len - 1], b[len - 1]);
		return colors;
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
					((IAccumulationListener) listeners[i + 1])
							.accumulationEvent(reason);
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

						double avgHits = avgDcOccupancy[sect0][supl0] = ((double) count)
								/ _eventCount;
						// divide by num wires in superlayer
						avgDcOccupancy[sect0][supl0] = avgHits / (6 * 112);
					} // supl0
				} // sect0
			} // _eventCount != 0

			break;
		}
	}

}
