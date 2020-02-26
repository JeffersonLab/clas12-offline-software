package cnuphys.fastMCed.view;


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
import java.io.File;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
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
import cnuphys.fastMCed.eventgen.AEventGenerator;
import cnuphys.fastMCed.eventio.IPhysicsEventListener;
import cnuphys.fastMCed.eventio.PhysicsEventManager;
import cnuphys.fastMCed.fastmc.ParticleHits;
import cnuphys.fastMCed.geometry.GeometryManager;
import cnuphys.lund.SwimTrajectoryListener;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.MagneticFieldChangeListener;
import cnuphys.magfield.MagneticFields;
import cnuphys.swim.Swimming;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

@SuppressWarnings("serial")
public abstract class AView extends BaseView implements IFeedbackProvider, SwimTrajectoryListener,
		MagneticFieldChangeListener, IPhysicsEventListener {
		
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

	// the physics event manager
	protected PhysicsEventManager _eventManager = PhysicsEventManager.getInstance();

	/**
	 * Constructor
	 * 
	 * @param keyVals
	 *            variable length argument list
	 */
	public AView(Object... keyVals) {
		super(keyVals);

		_eventManager.addPhysicsListener(this, 2);

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

		// add the next event button
		
		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (PhysicsEventManager.getInstance().isNextOK()) {
					PhysicsEventManager.getInstance().nextEvent();
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
	 * Convenience method to see it we show the scale
	 * 
	 * @return <code>true</code> if we are to show the scale.
	 */
	public boolean showScale() {
		return true;
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

		PhysicsEvent event = PhysicsEventManager.getInstance().getCurrentEvent();


		// add some information about the current event
		if (event == null) {
			feedbackStrings.add("$orange red$No event");
		} else {
			int evNum = PhysicsEventManager.getInstance().eventNumber();
			int evCount = PhysicsEventManager.getInstance().getEventCount();
			
			String evStr = "event #" + evNum + " of " + evCount;

			feedbackStrings.add("$orange red$" +evStr);
			
			feedbackStrings.add("$orange red$" + _eventManager.getGeneratorDescription());
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
		getContainer().refresh();
	}

	/**
	 * A new event has arrived.
	 * 
	 * @param event
	 *            the new event.
	 */
	@Override
	public void newPhysicsEvent(final PhysicsEvent event, List<ParticleHits> particleHits) {
		getUserComponent().repaint();
		fixTitle(event);
		getContainer().redoFeedback();
	}

	/**
	 * Fix the title of the view after an event arrives. The default is to
	 * append the event number.
	 * 
	 * @param event
	 *            the new event
	 */
	protected void fixTitle(PhysicsEvent event) {
		String title = getTitle();
		int index = title.indexOf(evnumAppend);
		if (index > 0) {
			title = title.substring(0, index);
		}

		int num = _eventManager.eventNumber();
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
	 * A new event generator is active
	 * @param generator the now active generator
	 */
	@Override
	public void newEventGenerator(final AEventGenerator generator) {
	}


	public Shape clipView(Graphics g) {
		Shape oldClip = g.getClip();

		Rectangle b = getContainer().getInsetRectangle();
		g.clipRect(b.x, b.y, b.width, b.height);

		return oldClip;
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
	 * Convenience method to see it we show results of the SNR analysis
	 * 
	 * @return <code>true</code> if we are to show results of the SNR analysis
	 */
	public boolean showNoiseAnalysis() {
		if (_controlPanel == null) {
			return false;
		}
		return _controlPanel.showSNRAnalysis();
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