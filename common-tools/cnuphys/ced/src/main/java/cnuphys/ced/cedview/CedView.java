package cnuphys.ced.cedview;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.Timer;

import cnuphys.bCNU.component.InfoWindow;
import cnuphys.bCNU.component.TranslucentWindow;
import cnuphys.bCNU.feedback.IFeedbackProvider;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.toolbar.BaseToolBar;
import cnuphys.bCNU.graphics.toolbar.ToolBarToggleButton;
import cnuphys.bCNU.graphics.toolbar.UserToolBarComponent;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.bCNU.view.BaseView;
import cnuphys.bCNU.view.ViewManager;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.clasio.ClasIoEventManager.EventSourceType;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.MagFieldDisplayArray;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.IAccumulationListener;
import cnuphys.ced.geometry.ECGeometry;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.lund.SwimTrajectoryListener;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.MagneticFieldChangeListener;
import cnuphys.magfield.MagneticFields;
import cnuphys.swim.Swimming;

import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataEvent;

@SuppressWarnings("serial")
public abstract class CedView extends BaseView implements IFeedbackProvider, SwimTrajectoryListener,
		MagneticFieldChangeListener, IAccumulationListener, IClasIoEventListener {
	
	//for accumulation drawing
	private double _medianRelSetting = 0.25;

	// are we showing single events or are we showing accumulated data
	public enum Mode {
		SINGLE_EVENT, ACCUMULATED
	};
	
	static protected FieldProbe _activeProbe;
		
	//to add separator for first clone
	private static boolean _firstClone = true;

	// used for computing world circles
	private static final int NUMCIRCPNTS = 40;

	// next event button
	protected JButton nextEvent;

	// this is the projection plane. For SectorView it will be a constant
	// phi plane. For other views, something else, or null;
	protected Plane3D projectionPlane;

	// our mode
	protected Mode _mode = Mode.SINGLE_EVENT;

	// basic toolbar bits
	protected static final int TOOLBARBITS = BaseToolBar.NODRAWING & ~BaseToolBar.TEXTFIELD
			& ~BaseToolBar.CONTROLPANELBUTTON & ~BaseToolBar.RECTGRIDBUTTON & ~BaseToolBar.TEXTBUTTON
			& ~BaseToolBar.DELETEBUTTON;

	protected static final int NORANGETOOLBARBITS = TOOLBARBITS & ~BaseToolBar.RANGEBUTTON;

	/**
	 * A string that has r-theta-phi using unicode greek characters
	 */
	public static final String rThetaPhi = "r" + UnicodeSupport.THINSPACE +
			UnicodeSupport.SMALL_THETA + UnicodeSupport.THINSPACE + UnicodeSupport.SMALL_PHI;

	/**
	 * A string that has rho-theta-phi using unicode greek characters
	 */
	public static final String rhoZPhi = UnicodeSupport.SMALL_RHO + UnicodeSupport.THINSPACE + "z" +  UnicodeSupport.THINSPACE + UnicodeSupport.SMALL_PHI;

	/**
	 * A string that has rho-phi using unicode greek characters for hex views
	 */
	public static final String rhoPhi = UnicodeSupport.SMALL_RHO + UnicodeSupport.THINSPACE + UnicodeSupport.SMALL_PHI;

	private static final String evnumAppend = "  (Event# ";

	/**
	 * Name for the detector layer.
	 */
	public static final String _detectorLayerName = "Detectors";

	/**
	 * Name for the magnetic field layer.
	 */
	public static final String _magneticFieldLayerName = "Magnetic Field";

	// the control panel on the right
	protected ControlPanel _controlPanel;

	// the user component drawer for the toolbar
	protected UserComponentLundDrawer _userComponentDrawer;

	// checks for hovering;
	private long minHoverTrigger = 1000; // ms
	private long hoverStartCheck = -1;
	private MouseEvent hoverMouseEvent;
	
	// last trajectory hovering response
	protected String _lastTrajStr;

	// for Veronique's tilted sector coordinate system
	protected static final double COS_TILT = Math.cos(Math.toRadians(25.));
	protected static final double SIN_TILT = Math.sin(Math.toRadians(25.));

	// the clasIO event manager
	protected ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	/**
	 * Constructor
	 * 
	 * @param keyVals
	 *            variable length argument list
	 */
	public CedView(Object... keyVals) {
		super(keyVals);

		_eventManager.addClasIoEventListener(this, 2);

		IContainer container = getContainer();

		if (container != null) {
			container.getFeedbackControl().addFeedbackProvider(this);

			// add the magnetic field layer--mostly unused.
			container.addLogicalLayer(_magneticFieldLayerName);

			// add the detector drawing layer
			container.addLogicalLayer(_detectorLayerName);
		}

		// listen for trajectory changes
		Swimming.addSwimTrajectoryListener(this);

		MagneticFields.getInstance().addMagneticFieldChangeListener(this);

		_userComponentDrawer = new UserComponentLundDrawer(this);

		if (getUserComponent() != null) {
			getUserComponent().setUserDraw(_userComponentDrawer);
		}

		// create a heartbeat
		createHeartbeat();
		// prepare to check for hovering
		prepareForHovering();

		AccumulationManager.getInstance().addAccumulationListener(this);

		// add the next event button
		
		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (ClasIoEventManager.getInstance().isNextOK()) {
					ClasIoEventManager.getInstance().getNextEvent();
				} else {
					Toolkit.getDefaultToolkit().beep();
				}
			}

		};
		nextEvent = new JButton("Next");
		nextEvent.setToolTipText("Next Event");
		nextEvent.addActionListener(al);
		GraphicsUtilities.setSizeMini(nextEvent);
		getToolBar().add(Box.createHorizontalStrut(5), 0);
		getToolBar().add(nextEvent, 0);
		
		if (_activeProbe == null) {
			_activeProbe = FieldProbe.factory();
		}
	}

	// called when heartbeat goes off.
	protected void ping() {
		// check for over
		if (hoverStartCheck > 0) {
			if ((System.currentTimeMillis() - hoverStartCheck) > minHoverTrigger) {
				hovering(hoverMouseEvent);
				hoverStartCheck = -1;
			}
		}
	}

	// create the heartbeat. Default implementation is 1 second
	// Can overide to do nothing (so no heartbeat)
	protected void createHeartbeat() {
		int delay = 1000;
		ActionListener taskPerformer = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ping();
			}
		};
		new Timer(delay, taskPerformer).start();
	}

	// prepare hovering checker
	private void prepareForHovering() {

		IContainer container = getContainer();
		if (container == null) {
			return;
		}

		MouseMotionListener mml = new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent me) {
				resetHovering();
			}

			@Override
			public void mouseMoved(MouseEvent me) {
				closeHoverWindow();
				hoverStartCheck = System.currentTimeMillis();
				hoverMouseEvent = me;
			}
		};

		MouseListener ml = new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				resetHovering();
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				resetHovering();
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				resetHovering();
			}

		};

		container.getComponent().addMouseMotionListener(mml);
		container.getComponent().addMouseListener(ml);
	}

	private void closeHoverWindow() {
		TranslucentWindow.closeInfoWindow();
		InfoWindow.closeInfoWindow();
	}

	private void resetHovering() {
		hoverStartCheck = -1;
		closeHoverWindow();
	}

	/**
	 * Show or hide the annotation layer.
	 * 
	 * @param show
	 *            the value of the display flag.
	 */
	public void showAnnotations(boolean show) {
		if (getContainer().getAnnotationLayer() != null) {
			getContainer().getAnnotationLayer().setVisible(show);
		}
	}

	/**
	 * Sets whether or not we display the magnetic field layer.
	 * 
	 * @param show
	 *            the value of the display flag.
	 */
	public void showMagneticField(boolean show) {
		if (getMagneticFieldLayer() != null) {
			getMagneticFieldLayer().setVisible(show);
		}
	}

	/**
	 * Convenience method to see it we show results of the noise analysis
	 * 
	 * @return <code>true</code> if we are to show results of the noise analysis
	 */
	public boolean showNoiseAnalysis() {
		if (_controlPanel == null) {
			return false;
		}
		return _controlPanel.showNoiseAnalysis();
	}

	/**
	 * Convenience method to see it we show the segment masks
	 * 
	 * @return <code>true</code> if we are to show the masks.
	 */
	public boolean showMasks() {
		if (_controlPanel == null) {
			return false;
		}
		return _controlPanel.showMasks();
	}

	/**
	 * Convenience method to see it we show the scale
	 * 
	 * @return <code>true</code> if we are to show the scale.
	 */
	public boolean showScale() {
		return _controlPanel.showScale();
	}

	/**
	 * Convenience method to see it we hide the noise hits
	 * 
	 * @return <code>true</code> if we are to hide the noise hits
	 */
	public boolean hideNoise() {
		if (_controlPanel == null) {
			return false;
		}
		return _controlPanel.hideNoise();
	}

	/**
	 * Convenience method to see it we highlight the noise hits
	 * 
	 * @return <code>true</code> if we are to highlight the noise hits
	 */
	public boolean highlightNoise() {
		if (_controlPanel == null) {
			return false;
		}
		return _controlPanel.showNoiseAnalysis() && !hideNoise();
	}

	/**
	 * Convenience method to see it we show the bst reconstructed crosses.
	 * 
	 * @return <code>true</code> if we are to show the bst reconstructed
	 *         crosses.
	 */
	public boolean showCrosses() {
		if ((_controlPanel == null) || (_controlPanel.getDisplayArray() == null)) {
			return false;
		}
		return _controlPanel.getDisplayArray().showCrosses();
	}

	/**
	 * Convenience method to see it we show the dc hit-based reconstructed
	 * crosses.
	 * 
	 * @return <code>true</code> if we are to show the dc hit-based
	 *         reconstructed crosses.
	 */
	public boolean showDCHBCrosses() {
		if ((_controlPanel == null) || (_controlPanel.getDisplayArray() == null)) {
			return false;
		}
		return _controlPanel.getDisplayArray().showDCHBCrosses();
	}
	
	
	/**
	 * Convenience method global hit based display
	 * 
	 * @return <code>true</code> if we are to show hb globally
	 */
	public boolean showHB() {
		if ((_controlPanel == null) || (_controlPanel.getDisplayArray() == null)) {
			return false;
		}
		return _controlPanel.getDisplayArray().showHB();
	}

	/**
	 * Convenience method global time based display
	 * 
	 * @return <code>true</code> if we are to show tb globally
	 */
	public boolean showTB() {
		if ((_controlPanel == null) || (_controlPanel.getDisplayArray() == null)) {
			return false;
		}
		return _controlPanel.getDisplayArray().showTB();
	}

	/**
	 * Convenience method to see it we show the reconstructed clusters.
	 * 
	 * @return <code>true</code> if we are to show the the reconstructed
	 *         clusters.
	 */
	public boolean showClusters() {
		if ((_controlPanel == null) || (_controlPanel.getDisplayArray() == null)) {
			return false;
		}
		return _controlPanel.getDisplayArray().showClusters();
	}
	
	/**
	 * Convenience method to see it we show the reconstructed FMT Crosses.
	 * 
	 * @return <code>true</code> if we are to show the the reconstructed
	 *         FMT Crosses.
	 */
	public boolean showFMTCrosses() {
		if ((_controlPanel == null) || (_controlPanel.getDisplayArray() == null)) {
			return false;
		}
		return _controlPanel.getDisplayArray().showFMTCrosses();
	}


	
	/**
	 * Convenience method to see it we show the the reconstructed hits.
	 * 
	 * @return <code>true</code> if we are to show the the reconstructed
	 *         hits.
	 */
	public boolean showReconHits() {
		if ((_controlPanel == null) || (_controlPanel.getDisplayArray() == null)) {
			return false;
		}
		return _controlPanel.getDisplayArray().showReconHits();
	}
	
	/**
	 * Convenience method to see it we show the the adc hits.
	 * 
	 * @return <code>true</code> if we are to show the the adc
	 *         hits.
	 */
	public boolean showADCHits() {
		if ((_controlPanel == null) || (_controlPanel.getDisplayArray() == null)) {
			return false;
		}
		return _controlPanel.getDisplayArray().showADCHits();
	}

	/**
	 * Convenience method to see if we show CVT reconstructed tracks.
	 * These are ADC hits except 
	 * @return <code>true</code> if we are to show ADC hits.
	 */
	public boolean showCVTTracks() {
		if ((_controlPanel == null) || (_controlPanel.getDisplayArray() == null)) {
			return false;
		}
		return _controlPanel.getDisplayArray().showCVTTracks();
	}

	/**
	 * Convenience method to see it we show the dc time-based reconstructed
	 * crosses.
	 * 
	 * @return <code>true</code> if we are to show the dc time-based
	 *         reconstructed crosses.
	 */
	public boolean showDCTBCrosses() {
		if ((_controlPanel == null) || (_controlPanel.getDisplayArray() == null)) {
			return false;
		}
		return _controlPanel.getDisplayArray().showDCTBCrosses();
	}

	/**
	 * Convenience method to see it we show the dc hit based reconstructed
	 * hits.
	 * 
	 * @return <code>true</code> if we are to show the dc hit-based
	 *         reconstructed hits.
	 */
	public boolean showDCHBHits() {
		if ((_controlPanel == null) || (_controlPanel.getDisplayArray() == null)) {
			return false;
		}
		return _controlPanel.getDisplayArray().showDCHBHits();
	}
	
	/**
	 * Convenience method to see it we show the dc time based reconstructed
	 * hits.
	 * 
	 * @return <code>true</code> if we are to show the dc time-based
	 *         reconstructed hits.
	 */
	public boolean showDCTBHits() {
		if ((_controlPanel == null) || (_controlPanel.getDisplayArray() == null)) {
			return false;
		}
		return _controlPanel.getDisplayArray().showDCTBHits();
	}


	/**
	 * Should we draw hit based segments
	 * @return whether we should draw hits based segments
	 */
	public boolean showDCHBSegments() {
		if ((_controlPanel == null) || (_controlPanel.getDisplayArray() == null)) {
			return false;
		}
		return _controlPanel.getDisplayArray().showDCHBSegments();
	}

	/**
	 * Should we draw time based segments
	 * @return whether we should draw time based segments
	 */
	public boolean showDCTBSegments() {
		if ((_controlPanel == null) || (_controlPanel.getDisplayArray() == null)) {
			return false;
		}
		return _controlPanel.getDisplayArray().showDCTBSegments();
	}

	/**
	 * Convenience method to get the magnetic field display option.
	 * 
	 * @return the magnetic field display option.
	 */
	public int getMagFieldDisplayOption() {
		if ((_controlPanel == null) || (_controlPanel.getMagFieldDisplayArray() == null)) {
			return MagFieldDisplayArray.NOMAGDISPLAY;
		}
		return _controlPanel.getMagFieldDisplayArray().getMagFieldDisplayOption();
	}

	/**
	 * Convenience method to see it we show the montecarlo truth.
	 * 
	 * @return <code>true</code> if we are to show the montecarlo truth, if it
	 *         is available.
	 */
	public boolean showMcTruth() {
		if ((_controlPanel == null) || (_controlPanel.getDisplayArray() == null)) {
			return false;
		}
		return _controlPanel.getDisplayArray().showMcTruth();
	}

	/**
	 * Convenience method to see it we show the cosmic tracks.
	 * 
	 * @return <code>true</code> if we are to show the cosmic tracks, if it is
	 *         available.
	 */
	public boolean showCosmics() {
		if ((_controlPanel == null) || (_controlPanel.getDisplayArray() == null)) {
			return false;
		}
		return _controlPanel.getDisplayArray().showCosmics();
	}

	/**
	 * Convenience method to see if u strips displayed
	 * 
	 * @return <code>true</code> if we are to display u strips
	 */
	public boolean showUStrips() {
		return _controlPanel.getDisplayArray().showUStrips();
	}

	/**
	 * Convenience method to see if v strips displayed
	 * 
	 * @return <code>true</code> if we are to display v strips
	 */
	public boolean showVStrips() {
		return _controlPanel.getDisplayArray().showVStrips();
	}

	/**
	 * Convenience method to see if w strips displayed
	 * 
	 * @return <code>true</code> if we are to display w strips
	 */
	public boolean showWStrips() {
		return _controlPanel.getDisplayArray().showWStrips();
	}

	/**
	 * Should we show the strips
	 * 
	 * @param stripType
	 *            U, V or W
	 * @return <code>true</code> if thebstrips of that type should be shown
	 */
	public boolean showStrips(int stripType) {
		if (stripType == ECGeometry.EC_U) {
			return showUStrips();
		} else if (stripType == ECGeometry.EC_V) {
			return showVStrips();
		} else if (stripType == ECGeometry.EC_W) {
			return showWStrips();
		}
		return false;
	}

	/**
	 * Every view should be able to say what sector the current point location
	 * represents.
	 * 
	 * @param container
	 *            the base container for the view.
	 * @param screenPoint
	 *            the pixel point
	 * @param worldPoint
	 *            the corresponding world location.
	 * @return the sector [1..6] or -1 for none.
	 */
	public abstract int getSector(IContainer container, Point screenPoint, Point2D.Double worldPoint);

	/**
	 * The magnetic field has changed
	 */
	@Override
	public void magneticFieldChanged() {
		_activeProbe = FieldProbe.factory();
		getContainer().refresh();
	}

	// we are hovering
	protected void hovering(MouseEvent me) {

		// avoid cursor
		Point p = me.getLocationOnScreen();
		p.x += 5;
		p.y += 4;

		if (_lastTrajStr != null) {
			if (TranslucentWindow.isTranslucencySupported()) {
				TranslucentWindow.info(_lastTrajStr, 0.6f, p);
			} else {
				InfoWindow.info(_lastTrajStr, p);
			}
		}
	}

	/**
	 * Convert sector coordinates to tilted sector coordinates. The two vectors
	 * can be the same in which case it is overwritten.
	 * 
	 * @param tiltedXYZ
	 *            will hold the tilted coordinates
	 * @param sectorXYZ
	 *            the sector coordinates
	 */
	public void sectorToTilted(double[] tiltedXYZ, double[] sectorXYZ) {
		double sectx = sectorXYZ[0];
		double secty = sectorXYZ[1];
		double sectz = sectorXYZ[2];

		double tiltx = sectx * COS_TILT - sectz * SIN_TILT;
		double tilty = secty;
		double tiltz = sectx * SIN_TILT + sectz * COS_TILT;

		tiltedXYZ[0] = tiltx;
		tiltedXYZ[1] = tilty;
		tiltedXYZ[2] = tiltz;
	}

	/**
	 * Convert tilted sector coordinates to sector coordinates. The two vectors
	 * can be the same in which case it is overwritten.
	 * 
	 * @param tiltedXYZ
	 *            will hold the tilted coordinates
	 * @param sectorXYZ
	 *            the sector coordinates
	 */
	public void tiltedToSector(double[] tiltedXYZ, double[] sectorXYZ) {
		double tiltx = tiltedXYZ[0];
		double tilty = tiltedXYZ[1];
		double tiltz = tiltedXYZ[2];

		double sectx = tiltx * COS_TILT + tiltz * SIN_TILT;
		double secty = tilty;
		double sectz = -tiltx * SIN_TILT + tiltz * COS_TILT;

		sectorXYZ[0] = sectx;
		sectorXYZ[1] = secty;
		sectorXYZ[2] = sectz;
	}
	
	/**
	 * Check whether the pointer bar is active on the tool bar
	 * 
	 * @return <code>true</code> if the Pointer button is active.
	 */
	protected boolean isPointerButtonActive() {
		if (getContainer() == null) {
			return false;
		}
		ToolBarToggleButton mtb = getContainer().getActiveButton();
		return (mtb == getContainer().getToolBar().getPointerButton());
	}

	/**
	 * Some common feedback. Subclasses should override and then call
	 * super.getFeedbackStrings
	 * 
	 * @param container
	 *            the base container for the view.
	 * @param pp
	 *            the pixel point
	 * @param wp
	 *            the corresponding world location.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point pp, Point2D.Double wp, List<String> feedbackStrings) {

		boolean haveEvent = false;

		EventSourceType estype = ClasIoEventManager.getInstance().getEventSourceType();
		switch (estype) {
		case HIPOFILE:
//		case HIPORING:
		case ET:
		case EVIOFILE:
			haveEvent = (_eventManager.getCurrentEvent() != null);
			break;
		}

		// add some information about the current event
		if (!haveEvent) {
			feedbackStrings.add("$orange red$No event");
		} else {
			feedbackStrings.add("$orange red$" + "event " + _eventManager.getEventNumber());
			feedbackStrings.add("$orange red$" + _eventManager.getCurrentSourceDescription());
		}

		// get the sector
		int sector = getSector(container, pp, wp);
		if (sector > 0) {
			feedbackStrings.add("Sector " + sector);
		}

//		String pixStr = "$Sky Blue$ScreenXY: [" + pp.x + ", " + pp.y + "]";
//		feedbackStrings.add(pixStr);
	}

	/**
	 * Convenience method to get the detector layer from the container.
	 * 
	 * @return the detector layer.
	 */
	public LogicalLayer getDetectorLayer() {
		return getContainer().getLogicalLayer(_detectorLayerName);
	}

	/**
	 * Convenience method to get the magnetic field layer from the container.
	 * 
	 * @return the magnetic field layer.
	 */
	public LogicalLayer getMagneticFieldLayer() {
		return getContainer().getLogicalLayer(_magneticFieldLayerName);
	}

	/**
	 * Get the mode for the display.
	 * 
	 * @return the mode. This is either Mode.SINGLE_EVENT or Mode.ACCUMULATED.
	 */
	public Mode getMode() {
		return _mode;
	}

	/**
	 * See if we are in single event mode (vice accumulated mode)
	 * 
	 * @return <code>true</code> if we are in single event mode
	 */
	public boolean isSingleEventMode() {
		return (_mode == Mode.SINGLE_EVENT);
	}

	/**
	 * See if we are in accumulation event mode (vice single event mode)
	 * 
	 * @return <code>true</code> if we are in accumulation mode
	 */
	public boolean isAccumulatedMode() {
		return (_mode == Mode.ACCUMULATED);
	}


	/**
	 * Set the mode for this view.
	 * 
	 * @param mode
	 *            the mode to set. This is either Mode.SINGLE_EVENT or
	 *            Mode.ACCUMULATED.
	 */
	public void setMode(Mode mode) {
		_mode = mode;
		
		JSlider slider = _controlPanel.getAccumulationSlider();
		if (slider != null) {
			slider.setEnabled(_mode == Mode.ACCUMULATED);
		}
	}

	/**
	 * Convenience method for getting the view's toolbar, if there is one.
	 * 
	 * @return the view's toolbar, or <code>null</code>.
	 */
	@Override
	public BaseToolBar getToolBar() {
		if (getContainer() != null) {
			return getContainer().getToolBar();
		}
		return null;
	}

	/**
	 * Convenience method for getting the user component on the view's toolbar,
	 * if there is one.
	 * 
	 * @return the the user component on the view's toolbar, or
	 *         <code>null</code>.
	 */
	@Override
	public UserToolBarComponent getUserComponent() {
		if (getToolBar() != null) {
			return getToolBar().getUserComponent();
		}
		return null;
	}

	/**
	 * The swum trajectories have changed
	 */
	@Override
	public void trajectoriesChanged() {
		if (!_eventManager.isAccumulating()) {
			getContainer().refresh();
		}
	}

	/**
	 * A new event has arrived.
	 * 
	 * @param event
	 *            the new event.
	 */
	@Override
	public void newClasIoEvent(final DataEvent event) {
		if (!_eventManager.isAccumulating()) {
			getUserComponent().repaint();
			fixTitle(event);
			getContainer().redoFeedback();
		}
	}

	/**
	 * Fix the title of the view after an event arrives. The default is to
	 * append the event number.
	 * 
	 * @param event
	 *            the new event
	 */
	protected void fixTitle(DataEvent event) {
		String title = getTitle();
		int index = title.indexOf(evnumAppend);
		if (index > 0) {
			title = title.substring(0, index);
		}

		int num = _eventManager.getEventNumber();
		if (num > 0) {
			setTitle(title + evnumAppend + num + ")");
		}
	}
	
	/**
	 * Override getName to return the title of the view.
	 * 
	 * @return the title of the view as the name of the view.
	 */
	@Override
	public String getName() {
		String title = getTitle();
		int index = title.indexOf(evnumAppend);
		if (index > 0) {
			title = title.substring(0, index);
		}
		return title;
	}


	/**
	 * Opened a new event file
	 * 
	 * @param path
	 *            the path to the new file
	 */
	@Override
	public void openedNewEventFile(final String path) {
	}

	/**
	 * Change the event source type
	 * 
	 * @param source
	 *            the new source: File, ET
	 */
	@Override
	public void changedEventSource(ClasIoEventManager.EventSourceType source) {
	}

	public Shape clipView(Graphics g) {
		Shape oldClip = g.getClip();

		Rectangle b = getContainer().getInsetRectangle();
		g.clipRect(b.x, b.y, b.width, b.height);

		return oldClip;
	}

	@Override
	public void accumulationEvent(int reason) {
		switch (reason) {
		case AccumulationManager.ACCUMULATION_STARTED:
			break;

		case AccumulationManager.ACCUMULATION_CANCELLED:
			fixTitle(_eventManager.getCurrentEvent());
			break;

		case AccumulationManager.ACCUMULATION_FINISHED:
			fixTitle(_eventManager.getCurrentEvent());
			break;
		}
	}

	/**
	 * Compute a world polygon for (roughly) a circle centered about a point.
	 * 
	 * @param center
	 *            the center point
	 * @param radius
	 *            the radius in cm
	 */
	public Point2D.Double[] getCenteredWorldCircle(Point2D.Double center, double radius) {

		if (center == null) {
			return null;
		}

		Point2D.Double circle[] = new Point2D.Double[NUMCIRCPNTS];
		double deltheta = 2.0 * Math.PI / (NUMCIRCPNTS - 1);

		for (int i = 0; i < NUMCIRCPNTS; i++) {
			double theta = i * deltheta;
			double zz = center.x + radius * Math.cos(theta);
			double xx = center.y + radius * Math.sin(theta);
			circle[i] = new Point2D.Double(zz, xx);
		}

		return circle;

	}

	/**
	 * Get the geometry package transformation to constant phi plane
	 * 
	 * @return the current geometry package transformation for this view
	 */
	public Plane3D getProjectionPlane() {
		return projectionPlane;
	}

	/**
	 * Convert sector to world (not global, but graphical world)
	 * 
	 * @param projPlane
	 *            the projection plane
	 * @param wp
	 *            the world point
	 * @param sectorXYZ
	 *            the sector coordinates (cm)
	 * @param projectionPlane
	 *            the projection plane
	 * @param the
	 *            sector 1..6
	 */
	public void sectorToWorld(Plane3D projPlane, Point2D.Double wp, double[] sectorXYZ, int sector) {
		double sectx = sectorXYZ[0];
		double secty = sectorXYZ[1];
		double sectz = sectorXYZ[2];

		projectedPoint(sectx, secty, sectz, projPlane, wp);
		if (sector > 3) {
			wp.y = -wp.y;
		}
	}

	/**
	 * From detector xyz get the projected world point.
	 * 
	 * @param x
	 *            the detector x coordinate
	 * @param y
	 *            the detector y coordinate
	 * @param z
	 *            the detector z coordinate
	 * @param projectionPlane
	 *            the projection plane
	 * @param wp
	 *            the projected 2D world point.
	 */
	public void projectClasToWorld(double x, double y, double z, Plane3D projectionPlane, Point2D.Double wp) {

		Point3D sectorP = new Point3D();
		GeometryManager.clasToSector(x, y, z, sectorP);
		projectedPoint(sectorP.x(), sectorP.y(), sectorP.z(), projectionPlane, wp);
	}

	/**
	 * From detector xyz get the projected world point.
	 * 
	 * @param clasPoint
	 *            the point in lab (clas) coordinates
	 * @param projectionPlane
	 *            the projection plane
	 * @param wp
	 *            the projected 2D world point.
	 */
	public void projectClasToWorld(Point3D clasPoint, Plane3D projectionPlane, Point2D.Double wp) {
		projectClasToWorld(clasPoint.x(), clasPoint.y(), clasPoint.z(), projectionPlane, wp);
	}

	/**
	 * Project a space point. Projected by finding the closest point on the
	 * plane.
	 * 
	 * @param x
	 *            the x coordinate
	 * @param y
	 *            the y coordinate
	 * @param z
	 *            the z coordinate
	 * @param projectionPlane
	 *            the projection plane
	 * @param wp
	 *            will hold the projected 2D world point
	 * @return the projected 3D space point
	 */
	public Point3D projectedPoint(double x, double y, double z, Plane3D projectionPlane, Point2D.Double wp) {
		Point3D p1 = new Point3D(x, y, z);
		Vector3D normal = projectionPlane.normal();
		Point3D p2 = new Point3D(p1.x() + normal.x(), p1.y() + normal.y(), p1.z() + normal.z());
		Line3D perp = new Line3D(p1, p2);
		Point3D pisect = new Point3D();
		projectionPlane.intersection(perp, pisect);

		wp.x = pisect.z();
		wp.y = Math.hypot(pisect.x(), pisect.y());
		return pisect;
	}
	
	/**
	 * Tests whether this listener is interested in events while accumulating
	 * @return <code>true</code> if this listener is NOT interested in  events while accumulating
	 */
	@Override
	public boolean ignoreIfAccumulating() {
		return true;
	}

	/**
	 * Get the median setting used in accumulation drawing
	 * @return the median setting used in accumulation drawing
	 */
	public double getMedianSetting() {
		return _medianRelSetting;
	}
	
	/**
	 * Set the median setting used in accumulation drawing
	 * @param medianSetting the median setting used in accumulation drawing
	 */
	public void setMedianSetting(double medianSetting) {
		_medianRelSetting = Math.max(0, Math.min(1, medianSetting));
	}
	

	
	/**
	 * Clone the view. 
	 * @return the cloned view
	 */
	@Override
	public BaseView cloneView() {
		if (_firstClone) {
			ViewManager.getInstance().getViewMenu().addSeparator();
			_firstClone = false;
		}
		return null;
	}

}
