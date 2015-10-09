package cnuphys.ced.event;

import java.awt.Color;

import javax.swing.event.EventListenerList;

import cnuphys.bCNU.event.IAccumulator;
import cnuphys.bCNU.graphics.colorscale.ColorScaleModel;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Histo2DData;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.geometry.GeoConstants;
import cnuphys.ced.event.data.BSTDataContainer;
import cnuphys.ced.event.data.DCDataContainer;
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

	/** Indicates hat accumulation has finished */
	public static final int ACCUMULATION_CLEAR = 2;

	// common colorscale
	public static ColorScaleModel colorScaleModel = new ColorScaleModel(
			getAccumulationValues(), getAccumulationColors());

	// the singleton
	private static AccumulationManager instance;

	// dc accumulated data indices are sector, superlayer, layer, wire
	private int _dcGemcAccumulatedData[][][][];
	private int _maxGemcDcCount;

	// dc XY accumulated data stored in a 2D histogram
	private Histo2DData _dcXYGemcAccumulatedData;

	// bst hit xy accumulated data
	private Histo2DData _bstXYAccumulatedData;

	// time based momentum resolution
	private GrowableArray _tbPResolutionHistoData = new GrowableArray(500, 100);

	// time based theta resolution
	private GrowableArray _tbThetaResolutionHistoData = new GrowableArray(500,
			100);

	// time based phi resolution
	private GrowableArray _tbPhiResolutionHistoData = new GrowableArray(500,
			100);

	// hit based momentum resolution
	private GrowableArray _hbPResolutionHistoData = new GrowableArray(500, 100);

	// hit based theta resolution
	private GrowableArray _hbThetaResolutionHistoData = new GrowableArray(500,
			100);

	// hit based phi resolution
	private GrowableArray _hbPhiResolutionHistoData = new GrowableArray(500,
			100);

	private ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// list of accumulation listeners
	private EventListenerList _listeners;

	/**
	 * private constructor for singleton.
	 */
	private AccumulationManager() {
		_eventManager.addPhysicsListener(this, 1);
		_dcGemcAccumulatedData = new int[GeoConstants.NUM_SECTOR][GeoConstants.NUM_SUPERLAYER][GeoConstants.NUM_LAYER][GeoConstants.NUM_WIRE];

		// dc XY accumulated data stored in a 2D histogram
		// use 100 bins in each direction
		_dcXYGemcAccumulatedData = new Histo2DData("DC XY Data", -390, 390,
				100, -450, 450, 100);

		// BST XY accumulated data also stored in histo
		_bstXYAccumulatedData = new Histo2DData("BST XY Data", -170, 170, 100,
				-170, 170, 100);

		// testing a splot creation

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
						_dcGemcAccumulatedData[sector][superLayer][layer][wire] = 0;
					}
				}
			}
		}
		_maxGemcDcCount = 0;

		// clear other stuff
		_dcXYGemcAccumulatedData.clear();
		_bstXYAccumulatedData.clear();

		// growable arrays used for canned histograms
		_tbPResolutionHistoData.clear();
		_tbThetaResolutionHistoData.clear();
		_tbPhiResolutionHistoData.clear();

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
	 * Get the data used for the testtime based momentum resolution histo
	 * 
	 * @return the data used for the test momentum resolution histo
	 */
	public GrowableArray getTBMomentumResolutionData() {
		return _tbPResolutionHistoData;
	}

	/**
	 * Get the data used for the test time based theta resolution histo
	 * 
	 * @return the data used for the test theta resolution histo
	 */
	public GrowableArray getTBThetaResolutionData() {
		return _tbThetaResolutionHistoData;
	}

	/**
	 * Get the data used for the test time based phi resolution histo
	 * 
	 * @return the data used for the test phi resolution histo
	 */
	public GrowableArray getTBPhiResolutionData() {
		return _tbPhiResolutionHistoData;
	}

	/**
	 * Get the data used for the test hit based momentum resolution histo
	 * 
	 * @return the data used for the test momentum resolution histo
	 */
	public GrowableArray getHBMomentumResolutionData() {
		return _hbPResolutionHistoData;
	}

	/**
	 * Get the data used for the test hit based theta resolution histo
	 * 
	 * @return the data used for the test theta resolution histo
	 */
	public GrowableArray getHBThetaResolutionData() {
		return _hbThetaResolutionHistoData;
	}

	/**
	 * Get the data used for the test hit based phi resolution histo
	 * 
	 * @return the data used for the test hit based phi resolution histo
	 */
	public GrowableArray getHBPhiResolutionData() {
		return _hbPhiResolutionHistoData;
	}

	/**
	 * Get the accumulated Gemc DC data
	 * 
	 * @return the accumulated dc data
	 */
	public int[][][][] getAccumulatedGemcDcData() {
		return _dcGemcAccumulatedData;
	}

	/**
	 * @return the max counts for any DC wire.
	 */
	public int getMaxGemcDcCount() {
		return _maxGemcDcCount;
	}

	/**
	 * @return the colorScaleModel
	 */
	public static ColorScaleModel getColorScaleModel() {
		return colorScaleModel;
	}

	/**
	 * Get the Gemc DC xy accumulated data
	 * 
	 * @return the Gemc DC xy accumulated data
	 */
	public Histo2DData getDcXYGemcAccumulatedData() {
		return _dcXYGemcAccumulatedData;
	}

	/**
	 * Get the BST XY ccumulated data
	 * 
	 * @return the BST XY accumulated data
	 */
	public Histo2DData getBSTXYGemcAccumulatedData() {
		return _bstXYAccumulatedData;
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
				_dcGemcAccumulatedData[sect0][supl0][lay0][wire0] += 1;
				_maxGemcDcCount = Math.max(
						_dcGemcAccumulatedData[sect0][supl0][lay0][wire0],
						_maxGemcDcCount);

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

		if ((dcData.getHitCount(0) > 0) && (dcData.dc_true_avgX != null)) {
			for (int i = 0; i < dcData.dc_true_avgX.length; i++) {
				double valueX = dcData.dc_true_avgX[i] / 10; // convert to cm
				double valueY = dcData.dc_true_avgY[i] / 10;
				_dcXYGemcAccumulatedData.add(valueX, valueY);
			}
		}

		// BST XY accumulation
		BSTDataContainer bstData = _eventManager.getBSTData();
		if (bstData != null) {
			if (bstData.bst_true_avgX != null) {
				int len = bstData.bst_true_avgX.length;
				for (int i = 0; i < len; i++) {
					_bstXYAccumulatedData.add(bstData.bst_true_avgX[i],
							bstData.bst_true_avgY[i]);
				}
			}

		} // bstData != null

		// splot histo test

		GenPartDataContainer genPart = _eventManager.getGenPartData();
		if (genPart.genpart_true_px != null) {
			double px = genPart.genpart_true_px[0] / 1000;
			double py = genPart.genpart_true_py[0] / 1000;
			double pz = genPart.genpart_true_pz[0] / 1000;
			double trueP = Math.sqrt(px * px + py * py + pz * pz);

			double trueTheta = Math.toDegrees(Math.acos(pz / trueP));
			double truePhi = Math.toDegrees(Math.atan2(py, px));

			// hit based
			if (dcData.getHitBasedTrackCount() > 0) {
				double reconsP = dcData.hitbasedtrkg_hbtracks_p[0];
				if (trueP > 0.001) {
					double frac = (trueP - reconsP) / trueP;
					_hbPResolutionHistoData.add(frac);
				}

				// have to swim traj backwards!
				// swimBackwardsToVertex(int q, double xo, double yo, double zo,
				// double px, double py, double pz) {

				int q = dcData.hitbasedtrkg_hbtracks_q[0];
				double xo = dcData.hitbasedtrkg_hbtracks_c3_x[0] / 100;
				double yo = dcData.hitbasedtrkg_hbtracks_c3_y[0] / 100;
				double zo = dcData.hitbasedtrkg_hbtracks_c3_z[0] / 100;

				double ux = dcData.hitbasedtrkg_hbtracks_c3_ux[0];
				double uy = dcData.hitbasedtrkg_hbtracks_c3_uy[0];
				double uz = dcData.hitbasedtrkg_hbtracks_c3_uz[0];

				double pxo = reconsP * ux;
				double pyo = reconsP * uy;
				double pzo = reconsP * uz;

				SwimTrajectory traj = Swimmer.swimBackwardsToVertex(q, xo, yo,
						zo, pxo, pyo, pzo);
				Swimming.addReconTrajectory(traj);

				// Q = [x, y, z, px/p, py/p, pz/p]
				double lastQ[] = traj.lastElement();

				xo = lastQ[0];
				yo = lastQ[1];
				zo = lastQ[2];
				pxo = -lastQ[3];
				pyo = -lastQ[4];
				pzo = -lastQ[5];

				// pt = Math.hypot(pxo, pyo);
				double reconTheta = Math.toDegrees(Math.acos(pzo));
				double reconPhi = Math.toDegrees(Math.atan2(pyo, pxo));

				_hbThetaResolutionHistoData.add(reconTheta - trueTheta);
				_hbPhiResolutionHistoData.add(reconPhi - truePhi);

			} // hb track count

			// time based
			if (dcData.getTimeBasedTrackCount() > 0) {
				double reconsP = dcData.timebasedtrkg_tbtracks_p[0];
				if (trueP > 0.001) {
					double frac = (trueP - reconsP) / trueP;
					_tbPResolutionHistoData.add(frac);
				}

				// have to swim traj backwards!
				// swimBackwardsToVertex(int q, double xo, double yo, double zo,
				// double px, double py, double pz) {

				int q = dcData.timebasedtrkg_tbtracks_q[0];
				double xo = dcData.timebasedtrkg_tbtracks_c3_x[0] / 100;
				double yo = dcData.timebasedtrkg_tbtracks_c3_y[0] / 100;
				double zo = dcData.timebasedtrkg_tbtracks_c3_z[0] / 100;

				double ux = dcData.timebasedtrkg_tbtracks_c3_ux[0];
				double uy = dcData.timebasedtrkg_tbtracks_c3_uy[0];
				double uz = dcData.timebasedtrkg_tbtracks_c3_uz[0];

				double pxo = reconsP * ux;
				double pyo = reconsP * uy;
				double pzo = reconsP * uz;

				SwimTrajectory traj = Swimmer.swimBackwardsToVertex(q, xo, yo,
						zo, pxo, pyo, pzo);
				Swimming.addReconTrajectory(traj);

				// Q = [x, y, z, px/p, py/p, pz/p]
				double lastQ[] = traj.lastElement();

				xo = lastQ[0];
				yo = lastQ[1];
				zo = lastQ[2];
				pxo = -lastQ[3];
				pyo = -lastQ[4];
				pzo = -lastQ[5];

				// pt = Math.hypot(pxo, pyo);
				double reconTheta = Math.toDegrees(Math.acos(pzo));
				double reconPhi = Math.toDegrees(Math.atan2(pyo, pxo));

				_tbThetaResolutionHistoData.add(reconTheta - trueTheta);
				_tbPhiResolutionHistoData.add(reconPhi - truePhi);

			} // time based track count
		}

	}

	@Override
	public void openedNewEventFile(String path) {
	}

	/**
	 * Get the values array for the plot.
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

		int r[] = { 255, 176, 255, 255, 255, 255, 255, 200, 150 };
		int g[] = { 255, 224, 255, 255, 165, 100, 0, 0, 0 };
		int b[] = { 255, 230, 128, 0, 0, 0, 0, 0, 0 };

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
