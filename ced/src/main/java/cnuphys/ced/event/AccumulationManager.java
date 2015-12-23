package cnuphys.ced.event;

import java.awt.Color;
import java.util.Vector;

import javax.swing.event.EventListenerList;

import cnuphys.bCNU.graphics.colorscale.ColorScaleModel;
import cnuphys.bCNU.log.Log;
import cnuphys.ced.cedview.bst.BSTxyView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IAccumulator;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.geometry.BSTxyPanel;
import cnuphys.ced.geometry.FTOFGeometry;
import cnuphys.ced.geometry.GeoConstants;
import cnuphys.ced.event.data.BSTDataContainer;
import cnuphys.ced.event.data.DCDataContainer;
import cnuphys.ced.event.data.ECDataContainer;
import cnuphys.ced.event.data.FTOFDataContainer;
import cnuphys.ced.event.data.GenPartDataContainer;
import cnuphys.splot.pdata.GrowableArray;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimmer;
import cnuphys.swim.Swimming;

import org.jlab.evio.clas12.EvioDataEvent;

/**
 * Manages the accumulation of data
 * 
 * @author heddle
 * 
 */
public class AccumulationManager implements IAccumulator, IClasIoEventListener {

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

	// dc accumulated data indices are sector, superlayer, layer, wire
	private int _dcDgtzAccumulatedData[][][][];
	private int _maxDgtzDcCount;
	
	//BST accumulated data (layer[0..7], sector[0..23])
	private int _bstDgtzAccumulatedData[][];
	private int _maxDgtzBstCount;
	
	//FTOF accumulated Data
	private int _ftof1aDgtzAccumulatedData[][];
	private int _ftof1bDgtzAccumulatedData[][];
	private int _ftof2DgtzAccumulatedData[][];
	private int _maxDgtzFtofCount;
	
	//EC  [sector, stack (inner, outer), view (uvw), strip]
	private int _ecDgtzAccumulatedData[][][][];
	private int _maxDgtzEcCount;
	
	//PCAL  [sector, view (uvw), strip]
	private int _pcalDgtzAccumulatedData[][][];
	private int _maxDgtzPcalCount;


//	// time based momentum resolution
//	private GrowableArray _tbPResolutionHistoData = new GrowableArray(500, 100);
//
//	// time based theta resolution
//	private GrowableArray _tbThetaResolutionHistoData = new GrowableArray(500,
//			100);
//
//	// time based phi resolution
//	private GrowableArray _tbPhiResolutionHistoData = new GrowableArray(500,
//			100);
//
//	// hit based momentum resolution
//	private GrowableArray _hbPResolutionHistoData = new GrowableArray(500, 100);
//
//	// hit based theta resolution
//	private GrowableArray _hbThetaResolutionHistoData = new GrowableArray(500,
//			100);
//
//	// hit based phi resolution
//	private GrowableArray _hbPhiResolutionHistoData = new GrowableArray(500,
//			100);

	private ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// list of accumulation listeners
	private EventListenerList _listeners;

	/**
	 * private constructor for singleton.
	 */
	private AccumulationManager() {
		_eventManager.addClasIoEventListener(this, 1);
		_dcDgtzAccumulatedData = new int[GeoConstants.NUM_SECTOR][GeoConstants.NUM_SUPERLAYER][GeoConstants.NUM_LAYER][GeoConstants.NUM_WIRE];
		_bstDgtzAccumulatedData = new int[8][24];
		
		//ftop storage
		_ftof1aDgtzAccumulatedData = new int[6][FTOFGeometry.numPaddles[0]];
		_ftof1bDgtzAccumulatedData = new int[6][FTOFGeometry.numPaddles[1]];
		_ftof2DgtzAccumulatedData = new int[6][FTOFGeometry.numPaddles[2]];
		
		//ec and pcal storage
		_ecDgtzAccumulatedData = new int[6][2][3][36];
		_pcalDgtzAccumulatedData = new int[6][3][68];
		
		clear();
	}

	/**
	 * Clears all accumulated data.
	 */
	@Override
	public void clear() {
		// System.err.println("AccumMgr clear");
		// clear accumulated gemc dc data
		for (int sector = 0; sector < GeoConstants.NUM_SECTOR; sector++) {
			for (int superLayer = 0; superLayer < GeoConstants.NUM_SUPERLAYER; superLayer++) {
				for (int layer = 0; layer < GeoConstants.NUM_LAYER; layer++) {
					for (int wire = 0; wire < GeoConstants.NUM_WIRE; wire++) {
						_dcDgtzAccumulatedData[sector][superLayer][layer][wire] = 0;
					}
				}
			}
		}
		_maxDgtzDcCount = 0;
		
		//clear ec data
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
		
		//clear pcal data
		for (int sector = 0; sector < 6; sector++) {
				for (int view = 0; view < 3; view++) {
					for (int strip = 0; strip < 68; strip++) {
						_pcalDgtzAccumulatedData[sector][view][strip] = 0;
					}
				}
		}
		_maxDgtzPcalCount = 0;


		// clear bst panel accumulation
		for (int layer = 0; layer < 8; layer++) {
			for (int sector = 0; sector < 24; sector++) {
				_bstDgtzAccumulatedData[layer][sector] = 0;
			}
		}
		_maxDgtzBstCount = 0;
		
		//clear ftof data
		for (int sector = 0; sector < 6; sector++) {
			for (int  paddle = 0; paddle < _ftof1aDgtzAccumulatedData[0].length; paddle++) {
				_ftof1aDgtzAccumulatedData[sector][paddle] = 0;
			}
			for (int  paddle = 0; paddle < _ftof1bDgtzAccumulatedData[0].length; paddle++) {
				_ftof1bDgtzAccumulatedData[sector][paddle] = 0;
			}
			for (int  paddle = 0; paddle < _ftof2DgtzAccumulatedData[0].length; paddle++) {
				_ftof2DgtzAccumulatedData[sector][paddle] = 0;
			}
		}
		_maxDgtzFtofCount = 0;
		
		// growable arrays used for canned histograms
//		_tbPResolutionHistoData.clear();
//		_tbThetaResolutionHistoData.clear();
//		_tbPhiResolutionHistoData.clear();

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

//	/**
//	 * Get the data used for the testtime based momentum resolution histo
//	 * 
//	 * @return the data used for the test momentum resolution histo
//	 */
//	public GrowableArray getTBMomentumResolutionData() {
//		return _tbPResolutionHistoData;
//	}
//
//	/**
//	 * Get the data used for the test time based theta resolution histo
//	 * 
//	 * @return the data used for the test theta resolution histo
//	 */
//	public GrowableArray getTBThetaResolutionData() {
//		return _tbThetaResolutionHistoData;
//	}
//
//	/**
//	 * Get the data used for the test time based phi resolution histo
//	 * 
//	 * @return the data used for the test phi resolution histo
//	 */
//	public GrowableArray getTBPhiResolutionData() {
//		return _tbPhiResolutionHistoData;
//	}
//
//	/**
//	 * Get the data used for the test hit based momentum resolution histo
//	 * 
//	 * @return the data used for the test momentum resolution histo
//	 */
//	public GrowableArray getHBMomentumResolutionData() {
//		return _hbPResolutionHistoData;
//	}
//
//	/**
//	 * Get the data used for the test hit based theta resolution histo
//	 * 
//	 * @return the data used for the test theta resolution histo
//	 */
//	public GrowableArray getHBThetaResolutionData() {
//		return _hbThetaResolutionHistoData;
//	}
//
//	/**
//	 * Get the data used for the test hit based phi resolution histo
//	 * 
//	 * @return the data used for the test hit based phi resolution histo
//	 */
//	public GrowableArray getHBPhiResolutionData() {
//		return _hbPhiResolutionHistoData;
//	}
	
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
	 * Get the max counts for ec strips
	 * @return the max counts for ec strips.
	 */
	public int getMaxDgtzEcCount() {
		return _maxDgtzEcCount;
	}

	/**
	 * Get the max counts for pcal strips
	 * @return the max counts for pcal strips.
	 */
	public int getMaxDgtzPcalCount() {
		return _maxDgtzPcalCount;
	}

	/**
	 * Get the max counts on any bst panel
	 * @return the max counts for any bst panel.
	 */
	public int getMaxDgtzBstCount() {
		return _maxDgtzBstCount;
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
	 * @return the max counts for any ftof panel.
	 */
	public int getMaxDgtzFtofCount() {
		return _maxDgtzFtofCount;
	}

//
//	/**
//	 * @return the colorScaleModel
//	 */
//	public static ColorScaleModel getColorScaleModel() {
//		return colorScaleModel;
//	}

	/**
	 * Get the color to use
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
	 * @param value the value 
	 * @param alpha the alpha value [0..255]
	 * @return the color corresponding to the value.
	 */
	public Color getAlphaColor(double value, int alpha) {
		Color c = getColor(value);
		Color color = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
		return color;
	}

	
	@Override
	public void newClasIoEvent(EvioDataEvent event) {

		// only care if I am accumulating
		if (!_eventManager.isAccumulating() || (event == null)) {
			return;
		}

		// dc data
		DCDataContainer dcData = _eventManager.getDCData();
		for (int i = 0; i < dcData.getHitCount(0); i++) {
			int sect0 = dcData.dc_dgtz_sector[i] - 1; // make 0 based
			int supl0 = dcData.dc_dgtz_superlayer[i] - 1; // make 0 based
			int lay0 = dcData.dc_dgtz_layer[i] - 1; // make 0 based
			int wire0 = dcData.dc_dgtz_wire[i] - 1; // make 0 based
			try {
				_dcDgtzAccumulatedData[sect0][supl0][lay0][wire0] += 1;
				
				_maxDgtzDcCount = Math.max(
						_dcDgtzAccumulatedData[sect0][supl0][lay0][wire0],
						_maxDgtzDcCount);
				
			} catch (ArrayIndexOutOfBoundsException e) {
				String msg = String
						.format("Index out of bounds. Event# %d sect %d supl %d lay %d wire %d",
								_eventManager.getEventNumber(),
								dcData.dc_dgtz_sector[i],
								dcData.dc_dgtz_superlayer[i],
								dcData.dc_dgtz_layer[i], dcData.dc_dgtz_wire[i]);
				Log.getInstance().warning(msg);
				System.err.println(msg);
			}

		} // end loop hits
		
		//ec and pcal
		ECDataContainer ecData = _eventManager.getECData();

		//ec data
		for (int i = 0; i < ecData.getHitCount(0); i++) {
			int sect0 = ecData.ec_dgtz_sector[i] - 1; // make 0 based
			int stack0 = ecData.ec_dgtz_stack[i] - 1; // make 0 based
			int view0 = ecData.ec_dgtz_view[i] - 1; // make 0 based
			int strip0 = ecData.ec_dgtz_strip[i] - 1; // make 0 based
			_ecDgtzAccumulatedData[sect0][stack0][view0][strip0] += 1;
			
			_maxDgtzEcCount = Math.max(
					_ecDgtzAccumulatedData[sect0][stack0][view0][strip0],
					_maxDgtzEcCount);
		}

		
		//pcal data
		for (int i = 0; i < ecData.getHitCount(1); i++) {
			int sect0 = ecData.pcal_dgtz_sector[i] - 1; // make 0 based
			int view0 = ecData.pcal_dgtz_view[i] - 1; // make 0 based
			int strip0 = ecData.pcal_dgtz_strip[i] - 1; // make 0 based
			_pcalDgtzAccumulatedData[sect0][view0][strip0] += 1;
			
			_maxDgtzPcalCount = Math.max(
					_pcalDgtzAccumulatedData[sect0][view0][strip0],
					_maxDgtzPcalCount);
		}
		
		//bst data
		BSTDataContainer bstData = _eventManager.getBSTData();

		for (int i = 0; i < bstData.getHitCount(0); i++) {
			BSTxyPanel panel = BSTxyView.getPanel(bstData.bst_dgtz_layer[i],
					bstData.bst_dgtz_sector[i]);
			if (panel != null) {
				int lay0 = bstData.bst_dgtz_layer[i] - 1;
				int sect0 = bstData.bst_dgtz_sector[i] - 1;
				try {
					_bstDgtzAccumulatedData[lay0][sect0] += 1;
					_maxDgtzBstCount = Math.max(
							_bstDgtzAccumulatedData[lay0][sect0],
							_maxDgtzBstCount);

				} catch (ArrayIndexOutOfBoundsException e) {
					String msg = String.format(
							"Index out of bounds (BST). Event# %d lay %d sect %d ",
							_eventManager.getEventNumber(),
							dcData.dc_dgtz_layer[i],
							dcData.dc_dgtz_sector[i]);
					Log.getInstance().warning(msg);
					System.err.println(msg);
				}

			}
		} // for on hits

		
		//ftof data
		// the overall container
		FTOFDataContainer ftofData = _eventManager.getFTOFData();
		if (ftofData != null) {
			accumFtof(ftofData.ftof1a_dgtz_sector, ftofData.ftof1a_dgtz_paddle, _ftof1aDgtzAccumulatedData);
			accumFtof(ftofData.ftof1b_dgtz_sector, ftofData.ftof1b_dgtz_paddle, _ftof1bDgtzAccumulatedData);
			accumFtof(ftofData.ftof2b_dgtz_sector, ftofData.ftof2b_dgtz_paddle, _ftof2DgtzAccumulatedData);
		}

		// splot histo test

//		GenPartDataContainer genPart = _eventManager.getGenPartData();
//		if (genPart.genpart_true_px != null) {
//			double px = genPart.genpart_true_px[0] / 1000;
//			double py = genPart.genpart_true_py[0] / 1000;
//			double pz = genPart.genpart_true_pz[0] / 1000;
//			double trueP = Math.sqrt(px * px + py * py + pz * pz);
//
//			double trueTheta = Math.toDegrees(Math.acos(pz / trueP));
//			double truePhi = Math.toDegrees(Math.atan2(py, px));
//
//			// hit based
//			if (dcData.getHitBasedTrackCount() > 0) {
//				double reconsP = dcData.hitbasedtrkg_hbtracks_p[0];
//				if (trueP > 0.001) {
//					double frac = (trueP - reconsP) / trueP;
//					_hbPResolutionHistoData.add(frac);
//				}
//
//				// have to swim traj backwards!
//				// swimBackwardsToVertex(int q, double xo, double yo, double zo,
//				// double px, double py, double pz) {
//
//				int q = dcData.hitbasedtrkg_hbtracks_q[0];
//				double xo = dcData.hitbasedtrkg_hbtracks_c3_x[0] / 100;
//				double yo = dcData.hitbasedtrkg_hbtracks_c3_y[0] / 100;
//				double zo = dcData.hitbasedtrkg_hbtracks_c3_z[0] / 100;
//
//				double ux = dcData.hitbasedtrkg_hbtracks_c3_ux[0];
//				double uy = dcData.hitbasedtrkg_hbtracks_c3_uy[0];
//				double uz = dcData.hitbasedtrkg_hbtracks_c3_uz[0];
//
//				double pxo = reconsP * ux;
//				double pyo = reconsP * uy;
//				double pzo = reconsP * uz;
//
//				SwimTrajectory traj = Swimmer.swimBackwardsToVertex(q, xo, yo,
//						zo, pxo, pyo, pzo);
//				Swimming.addReconTrajectory(traj);
//
//				// Q = [x, y, z, px/p, py/p, pz/p]
//				double lastQ[] = traj.lastElement();
//
//				xo = lastQ[0];
//				yo = lastQ[1];
//				zo = lastQ[2];
//				pxo = -lastQ[3];
//				pyo = -lastQ[4];
//				pzo = -lastQ[5];
//
//				// pt = Math.hypot(pxo, pyo);
//				double reconTheta = Math.toDegrees(Math.acos(pzo));
//				double reconPhi = Math.toDegrees(Math.atan2(pyo, pxo));
//
//				_hbThetaResolutionHistoData.add(reconTheta - trueTheta);
//				_hbPhiResolutionHistoData.add(reconPhi - truePhi);
//
//			} // hb track count
//
//			// time based
//			if (dcData.getTimeBasedTrackCount() > 0) {
//				double reconsP = dcData.timebasedtrkg_tbtracks_p[0];
//				if (trueP > 0.001) {
//					double frac = (trueP - reconsP) / trueP;
//					_tbPResolutionHistoData.add(frac);
//				}
//
//				// have to swim traj backwards!
//				// swimBackwardsToVertex(int q, double xo, double yo, double zo,
//				// double px, double py, double pz) {
//
//				int q = dcData.timebasedtrkg_tbtracks_q[0];
//				double xo = dcData.timebasedtrkg_tbtracks_c3_x[0] / 100;
//				double yo = dcData.timebasedtrkg_tbtracks_c3_y[0] / 100;
//				double zo = dcData.timebasedtrkg_tbtracks_c3_z[0] / 100;
//
//				double ux = dcData.timebasedtrkg_tbtracks_c3_ux[0];
//				double uy = dcData.timebasedtrkg_tbtracks_c3_uy[0];
//				double uz = dcData.timebasedtrkg_tbtracks_c3_uz[0];
//
//				double pxo = reconsP * ux;
//				double pyo = reconsP * uy;
//				double pzo = reconsP * uz;
//
//				SwimTrajectory traj = Swimmer.swimBackwardsToVertex(q, xo, yo,
//						zo, pxo, pyo, pzo);
//				Swimming.addReconTrajectory(traj);
//
//				// Q = [x, y, z, px/p, py/p, pz/p]
//				double lastQ[] = traj.lastElement();
//
//				xo = lastQ[0];
//				yo = lastQ[1];
//				zo = lastQ[2];
//				pxo = -lastQ[3];
//				pyo = -lastQ[4];
//				pzo = -lastQ[5];
//
//				// pt = Math.hypot(pxo, pyo);
//				double reconTheta = Math.toDegrees(Math.acos(pzo));
//				double reconPhi = Math.toDegrees(Math.atan2(pyo, pxo));
//
//				_tbThetaResolutionHistoData.add(reconTheta - trueTheta);
//				_tbPhiResolutionHistoData.add(reconPhi - truePhi);
//
//			} // time based track count
//		}

	}

	//for ftot accumulating
	private void accumFtof(int sector[], int paddle[], int[][] hitHolder) {
		
		if ((sector == null) || (paddle == null)) {
			return;
		}
		
		for (int hit = 0; hit < sector.length; hit++) {
			int sect0 = sector[hit] - 1;
			int paddle0 = paddle[hit] - 1;
			hitHolder[sect0][paddle0] += 1;
			_maxDgtzFtofCount = Math.max(
					hitHolder[sect0][paddle0],
					_maxDgtzFtofCount);
		}
		
	}
	
	@Override
	public void openedNewEventFile(String path) {
	}

	/**
	 * Get the values array for the color scale.
	 * Note the range is 0..1 so use fraction of max value to get color
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

//		int r[] = { 255, 176, 255, 255, 255, 255, 255, 200, 150 };
//		int g[] = { 255, 224, 255, 255, 165, 100, 0, 0, 0 };
//		int b[] = { 255, 230, 128, 0, 0, 0, 0, 0, 0 };

//		int r[] = { 176, 124, 0, 173, 255, 255, 255, 255, 139 };
//		int g[] = { 224, 252, 255, 255, 255, 165, 69, 0, 0 };
//		int b[] = { 230, 0, 0, 47, 0, 0, 0, 0, 0 };

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
	 * @param reason
	 *            should be one of the ACCUMULATION_X constants
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
	 * @param listener
	 *            the Accumulation listener to remove.
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
	 * @param listener
	 *            the Accumulation listener to add.
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
	
	

}
