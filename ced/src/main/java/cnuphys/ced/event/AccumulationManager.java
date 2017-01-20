package cnuphys.ced.event;

import java.awt.Color;
import javax.swing.event.EventListenerList;

import cnuphys.bCNU.graphics.colorscale.ColorScaleModel;
import cnuphys.bCNU.log.Log;
import cnuphys.ced.alldata.ColumnData;
import cnuphys.ced.cedview.bst.BSTxyView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IAccumulator;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.geometry.BSTGeometry;
import cnuphys.ced.geometry.BSTxyPanel;
import cnuphys.ced.geometry.FTOFGeometry;
import cnuphys.ced.geometry.GeoConstants;
import cnuphys.ced.geometry.PCALGeometry;
import cnuphys.ced.event.data.BST;
import cnuphys.ced.event.data.DC;
import cnuphys.ced.event.data.EC;
import cnuphys.ced.event.data.FTOF;
import cnuphys.ced.event.data.HTCC;
import cnuphys.ced.event.data.PCAL;
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
	private int _htccDgtzAccumulatedData[][][];
	private int _maxDgtzHTCCCount;
	
	
	// dc accumulated data indices are sector, superlayer, layer, wire
	private int _dcDgtzAccumulatedData[][][][];
	private int _maxDgtzDcCount;

	// BST accumulated data (layer[0..7], sector[0..23])
	private int _bstDgtzAccumulatedData[][];
	private int _maxDgtzBstCount;

	// BST accumulated data (layer[0..7], sector[0..23], strip [0..254])
	private int _bstDgtzFullAccumulatedData[][][];
	private int _maxDgtzFullBstCount;

	// FTOF accumulated Data
	private int _ftof1aDgtzAccumulatedData[][];
	private int _ftof1bDgtzAccumulatedData[][];
	private int _ftof2DgtzAccumulatedData[][];
	private int _maxDgtzFtofCount;

	// EC [sector, stack (inner, outer), view (uvw), strip]
	private int _ecDgtzAccumulatedData[][][][];
	private int _maxDgtzEcCount;

	// PCAL [sector, view (uvw), strip]
	private int _pcalDgtzAccumulatedData[][][];
	private int _maxDgtzPcalCount;

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
		
		//htcc data
		_htccDgtzAccumulatedData = new int[GeoConstants.NUM_SECTOR][4][2];
		
		//dc data
		_dcDgtzAccumulatedData = new int[GeoConstants.NUM_SECTOR][GeoConstants.NUM_SUPERLAYER][GeoConstants.NUM_LAYER][GeoConstants.NUM_WIRE];

		// down to layer
		_bstDgtzAccumulatedData = new int[8][];
		for (int lay0 = 0; lay0 < 8; lay0++) {
			int supl0 = lay0 / 2;
			_bstDgtzAccumulatedData[lay0] = new int[BSTGeometry.sectorsPerSuperlayer[supl0]];
		}

		// _bstDgtzAccumulatedData = new int[8][24];

		// down to strip
		_bstDgtzFullAccumulatedData = new int[8][][];
		for (int lay0 = 0; lay0 < 8; lay0++) {
			int supl0 = lay0 / 2;
			_bstDgtzFullAccumulatedData[lay0] = new int[BSTGeometry.sectorsPerSuperlayer[supl0]][256];
		}

		// ftop storage
		_ftof1aDgtzAccumulatedData = new int[6][FTOFGeometry.numPaddles[0]];
		_ftof1bDgtzAccumulatedData = new int[6][FTOFGeometry.numPaddles[1]];
		_ftof2DgtzAccumulatedData = new int[6][FTOFGeometry.numPaddles[2]];

		// ec and pcal storage
		_ecDgtzAccumulatedData = new int[6][2][3][36];

		_pcalDgtzAccumulatedData = new int[6][3][];
		for (int sect0 = 0; sect0 < 6; sect0++) {
			for (int view0 = 0; view0 < 3; view0++) {
				_pcalDgtzAccumulatedData[sect0][view0] = new int[PCALGeometry.PCAL_NUMSTRIP[view0]];
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

		//clear accumulated HTCC
		for (int sector = 0; sector < GeoConstants.NUM_SECTOR; sector++) {
			for (int ring = 0; ring < 4; ring++) {
				for (int half = 0; half < 2; half++) {
						_htccDgtzAccumulatedData[sector][ring][half] = 0;
				}
			}
		}
		_maxDgtzHTCCCount = 0;
		
		// clear accumulated gemc dc data
		for (int sector = 0; sector < GeoConstants.NUM_SECTOR; sector++) {
			for (int superLayer = 0; superLayer < GeoConstants.NUM_SUPERLAYER; superLayer++) {
				avgDcOccupancy[sector][superLayer] = 0;
				for (int layer = 0; layer < GeoConstants.NUM_LAYER; layer++) {
					for (int wire = 0; wire < GeoConstants.NUM_WIRE; wire++) {
						_dcDgtzAccumulatedData[sector][superLayer][layer][wire] = 0;
					}
				}
			}
		}
		_maxDgtzDcCount = 0;

		// clear ec data
		for (int sector = 0; sector < 6; sector++) {
			for (int stack = 0; stack < 2; stack++) {
				for (int view = 0; view < 3; view++) {
					for (int strip = 0; strip < 36; strip++) {
						_ecDgtzAccumulatedData[sector][stack][view][strip] = 0;
					}
				}
			}
		}
		_maxDgtzEcCount = 0;

		// clear pcal data
		for (int sector = 0; sector < 6; sector++) {
			for (int view = 0; view < 3; view++) {
				for (int strip = 0; strip < PCALGeometry.PCAL_NUMSTRIP[view]; strip++) {
					_pcalDgtzAccumulatedData[sector][view][strip] = 0;
				}
			}
		}
		_maxDgtzPcalCount = 0;

		// clear bst panel accumulation
		for (int layer = 0; layer < 8; layer++) {
			int supl0 = layer / 2;
			for (int sector = 0; sector < BSTGeometry.sectorsPerSuperlayer[supl0]; sector++) {
				_bstDgtzAccumulatedData[layer][sector] = 0;
				for (int strip = 0; strip < 256; strip++) {
					_bstDgtzFullAccumulatedData[layer][sector][strip] = 0;
				}
			}
		}
		_maxDgtzBstCount = 0;
		_maxDgtzFullBstCount = 0;

		// clear ftof data
		for (int sector = 0; sector < 6; sector++) {
			for (int paddle = 0; paddle < _ftof1aDgtzAccumulatedData[0].length; paddle++) {
				_ftof1aDgtzAccumulatedData[sector][paddle] = 0;
			}
			for (int paddle = 0; paddle < _ftof1bDgtzAccumulatedData[0].length; paddle++) {
				_ftof1bDgtzAccumulatedData[sector][paddle] = 0;
			}
			for (int paddle = 0; paddle < _ftof2DgtzAccumulatedData[0].length; paddle++) {
				_ftof2DgtzAccumulatedData[sector][paddle] = 0;
			}
		}
		_maxDgtzFtofCount = 0;

		// growable arrays used for canned histograms
		// _tbPResolutionHistoData.clear();
		// _tbThetaResolutionHistoData.clear();
		// _tbPhiResolutionHistoData.clear();

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
	 * Get the accumulated dgtz HTCC data
	 * 
	 * @return the accumulated HTCC data
	 */
	public int[][][] getAccumulatedDgtzHTCCData() {
		return _htccDgtzAccumulatedData;
	}


	/**
	 * Get the accumulated dgtz EC data
	 * 
	 * @return the accumulated ec data
	 */
	public int[][][][] getAccumulatedDgtzEcData() {
		return _ecDgtzAccumulatedData;
	}

	/**
	 * Get the accumulated dgtz PCAL data
	 * 
	 * @return the accumulated PCAL data
	 */
	public int[][][] getAccumulatedDgtzPcalData() {
		return _pcalDgtzAccumulatedData;
	}

	/**
	 * Get the accumulated dgtz DC data
	 * 
	 * @return the accumulated dc data
	 */
	public int[][][][] getAccumulatedDgtzDcData() {
		return _dcDgtzAccumulatedData;
	}

	/**
	 * Get the max counts on any wire
	 * 
	 * @return the max counts for any DC wire.
	 */
	public int getMaxDgtzDcCount() {
		return _maxDgtzDcCount;
	}

	/**
	 * Get the accumulated dgtz Bst panel data
	 * 
	 * @return the accumulated bst panel data
	 */
	public int[][] getAccumulatedDgtzBstData() {
		return _bstDgtzAccumulatedData;
	}

	/**
	 * Get the accumulated dgtz full Bst strip data
	 * 
	 * @return the accumulated bst strip data
	 */
	public int[][][] getAccumulatedDgtzFullBstData() {
		return _bstDgtzFullAccumulatedData;
	}

	/**
	 * Get the max counts for ec strips
	 * 
	 * @return the max counts for ec strips.
	 */
	public int getMaxDgtzEcCount() {
		return _maxDgtzEcCount;
	}

	/**
	 * Get the max counts for ec strips
	 * 
	 * @return the max counts for ec strips.
	 */
	public int getMaxDgtzHTCCCount() {
		return _maxDgtzHTCCCount;
	}

	/**
	 * Get the max counts for pcal strips
	 * 
	 * @return the max counts for pcal strips.
	 */
	public int getMaxDgtzPcalCount() {
		return _maxDgtzPcalCount;
	}

	/**
	 * Get the max counts on any bst panel
	 * 
	 * @return the max counts for any bst panel.
	 */
	public int getMaxDgtzBstCount() {
		return _maxDgtzBstCount;
	}

	/**
	 * Get the max counts on any bst strip
	 * 
	 * @return the max counts for any bst strip.
	 */
	public int getMaxDgtzFullBstCount() {
		return _maxDgtzFullBstCount;
	}

	/**
	 * Get the accumulated dgtz ftof panel 1a
	 * 
	 * @return the accumulated ftof panel 1a
	 */
	public int[][] getAccumulatedDgtzFtof1aData() {
		return _ftof1aDgtzAccumulatedData;
	}

	/**
	 * Get the accumulated dgtz ftof panel 1b
	 * 
	 * @return the accumulated ftof panel 1b
	 */
	public int[][] getAccumulatedDgtzFtof1bData() {
		return _ftof1bDgtzAccumulatedData;
	}

	/**
	 * Get the accumulated dgtz ftof panel 2
	 * 
	 * @return the accumulated ftof panel 2
	 */
	public int[][] getAccumulatedDgtzFtof2Data() {
		return _ftof2DgtzAccumulatedData;
	}

	/**
	 * Get the max counts on any ftof panel
	 * 
	 * @return the max counts for any ftof panel.
	 */
	public int getMaxDgtzFtofCount() {
		return _maxDgtzFtofCount;
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
		
		//htcc data
		int htccHitCount = HTCC.hitCount();
		if (htccHitCount > 0) {
			int sector[] = HTCC.sector();
			int ring[] = HTCC.ring();
			int half[] = HTCC.half();
			
			for (int i = 0; i < htccHitCount; i++) {
				int sect0 = sector[i] - 1; // make 0 based
				int ring0 = ring[i] - 1; // make 0 based
				int half0 = half[i] - 1; // make 0 based

				if (sect0 >= 0) {
					try {
						_htccDgtzAccumulatedData[sect0][ring0][half0] += 1;
						
						_maxDgtzHTCCCount = Math.max(_htccDgtzAccumulatedData[sect0][ring0][half0],
								_maxDgtzHTCCCount);

					} catch (ArrayIndexOutOfBoundsException e) {
						String msg = String.format("HTCC index out of bounds. Event# %d sect %d ring %d half %d",
								_eventManager.getEventNumber(), sector[i], ring[i], half[i]);
						Log.getInstance().warning(msg);
						System.err.println(msg);
					}
				}

			} // end loop hits

		} // htcc hit count > 0

		// dc data
		int dcHitCount = DC.hitCount();

		if (dcHitCount > 0) {
			byte sector[] = DC.sector();
			byte superlayer[] = DC.superlayer();
			byte layer[] = DC.layer();
			short wire[] = DC.wire();

			for (int i = 0; i < dcHitCount; i++) {
				int sect0 = sector[i] - 1; // make 0 based
				int supl0 = superlayer[i] - 1; // make 0 based
				int lay0 = layer[i] - 1; // make 0 based
				int wire0 = wire[i] - 1; // make 0 based
				try {
					_dcDgtzAccumulatedData[sect0][supl0][lay0][wire0] += 1;

					_maxDgtzDcCount = Math.max(
							_dcDgtzAccumulatedData[sect0][supl0][lay0][wire0],
							_maxDgtzDcCount);

				} catch (ArrayIndexOutOfBoundsException e) {
					String msg = String.format(
							"DC index out of bounds. Event# %d sect %d supl %d lay %d wire %d",
							_eventManager.getEventNumber(), sector[i],
							superlayer[i], layer[i], wire[i]);
					Log.getInstance().warning(msg);
					System.err.println(msg);
				}

			} // end loop hits

		} // dcHitCount > 0


		// ec data
		int ecHitCount = EC.hitCount();
		if (ecHitCount > 0) {
			int sect[] = EC.sector();
			int stack[] = EC.stack();
			int view[] = EC.view();
			int strip[] = EC.strip();
			
			for (int i = 0; i < ecHitCount; i++) {
				int sect0 = sect[i] - 1; // make 0 based
				int stack0 = stack[i] - 1; // make 0 based
				int view0 = view[i] - 1; // make 0 based
				int strip0 = strip[i] - 1; // make 0 based
				_ecDgtzAccumulatedData[sect0][stack0][view0][strip0] += 1;

				_maxDgtzEcCount = Math.max(
						_ecDgtzAccumulatedData[sect0][stack0][view0][strip0],
						_maxDgtzEcCount);
			}
		}
		
		int pcalHitCount = PCAL.hitCount();
		if (pcalHitCount > 0) {
			int sect[] = PCAL.sector();
			int view[] = PCAL.view();
			int strip[] = PCAL.strip();
			for (int i = 0; i < pcalHitCount; i++) {
				int sect0 = sect[i] - 1; // make 0 based
				int view0 = view[i] - 1; // make 0 based
				int strip0 = strip[i] - 1; // make 0 based
				_pcalDgtzAccumulatedData[sect0][view0][strip0] += 1;

				_maxDgtzPcalCount = Math.max(
						_pcalDgtzAccumulatedData[sect0][view0][strip0],
						_maxDgtzPcalCount);
			}
		}


		//bst
		int hitCount = BST.hitCount();
		if (hitCount > 0) {
			int bstsector[] = BST.sector();
			int bstlayer[] = BST.layer();
			int bststrip[] = BST.strip();
			
			for (int i = 0; i < hitCount; i++) {
				BSTxyPanel panel = BSTxyView.getPanel(bstlayer[i],
						bstsector[i]);
				if (panel != null) {
					int lay0 = bstlayer[i] - 1;
					int sect0 = bstsector[i] - 1;
					int strip0 = bststrip[i] - 1;
					try {
						_bstDgtzAccumulatedData[lay0][sect0] += 1;
						_maxDgtzBstCount = Math.max(
								_bstDgtzAccumulatedData[lay0][sect0],
								_maxDgtzBstCount);

						if (strip0 >= 0) {
							_bstDgtzFullAccumulatedData[lay0][sect0][strip0] += 1;
							_maxDgtzFullBstCount = Math.max(
									_bstDgtzFullAccumulatedData[lay0][sect0][strip0],
									_maxDgtzFullBstCount);
						}

					} catch (ArrayIndexOutOfBoundsException e) {
						String msg = String.format(
								"Index out of bounds (BST). Event# %d lay %d sect %d  strip %d",
								_eventManager.getEventNumber(),
								bstlayer[i],
								bstsector[i],
								bststrip[i]);
						Log.getInstance().warning(msg);
						System.err.println(msg);
					}

				}
			} // for on hits
		} //hitcount > 0
		

		// ftof data
		TdcAdcHitList list = FTOF.getInstance().updateTdcAdcList();

		accumFtof(list);


	}

	// for ftot accumulating
	private void accumFtof(TdcAdcHitList list) {

		if ((list == null) || list.isEmpty()) {
			return;
		}
		
		for (TdcAdcHit hit : list) {
			if (hit != null) {
				int sect0 = hit.sector-1;
				int paddle0 = hit.component-1;
				
				if (hit.layer == 1) {
					_ftof1aDgtzAccumulatedData[sect0][paddle0] += 1;
					_maxDgtzFtofCount = Math.max(_ftof1aDgtzAccumulatedData[sect0][paddle0],
							_maxDgtzFtofCount);
				}
				else if (hit.layer == 2) {
					_ftof1bDgtzAccumulatedData[sect0][paddle0] += 1;
					_maxDgtzFtofCount = Math.max(_ftof1bDgtzAccumulatedData[sect0][paddle0],
							_maxDgtzFtofCount);
				}
				if (hit.layer == 3) {
					_ftof2DgtzAccumulatedData[sect0][paddle0] += 1;
					_maxDgtzFtofCount = Math.max(_ftof2DgtzAccumulatedData[sect0][paddle0],
							_maxDgtzFtofCount);
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

		int r[] = { 240, 176, 124, 173, 255, 255, 255, 255, 139 };
		int g[] = { 248, 224, 255, 255, 255, 165, 69, 0, 0 };
		int b[] = { 255, 230, 0, 47, 0, 0, 0, 0, 0 };

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
								count += _dcDgtzAccumulatedData[sect0][supl0][lay0][wire0];
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
