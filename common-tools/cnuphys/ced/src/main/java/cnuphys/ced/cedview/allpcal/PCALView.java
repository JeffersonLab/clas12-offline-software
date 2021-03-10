package cnuphys.ced.cedview.allpcal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Properties;

import org.jlab.geom.prim.Point3D;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.bCNU.view.BaseView;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.HexView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayArray;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.geometry.PCALGeometry;
import cnuphys.ced.item.PCALHexSectorItem;

public class PCALView extends HexView {

	// for naming clones
	private static int CLONE_COUNT = 0;

	// base title
	private static final String _baseTitle = "PCAL";

	// sector items
	private PCALHexSectorItem _hexItems[];

	// for drawing MC hits
	private McHitDrawer _mcHitDrawer;
	
	// for drawing REC::Calorimeter data
    private RecDrawer _recDrawer;

	private static final double _xsize = 410.0;
	private static final double _ysize = _xsize * 1.154734;
	// private static final double _ysize = 1.0;
	protected static Rectangle2D.Double _defaultWorld = new Rectangle2D.Double(_xsize, -_ysize, -2 * _xsize,
			2 * _ysize);

	// public static final double _XMAX = 400; //cm
	// public static final double _YMAX = _XMAX;

	/**
	 * Create an allDCView
	 * 
	 * @param keyVals variable set of arguments.
	 */
	private PCALView(String title) {
		super(getAttributes(title));

		// MC hit drawer
		_mcHitDrawer = new McHitDrawer(this);
		
		// REC::Calorimeter drawer
		_recDrawer = new RecDrawer(this);

		setBeforeDraw();
		setAfterDraw();
		getContainer().getComponent().setBackground(Color.gray);

		// default properties
		setBooleanProperty(DisplayArray.SHOWINNER_PROPERTY, true);
	}

	/**
	 * See if we are to display the inner plane.
	 * 
	 * @return <code>true</code> if we are to display the inner plane, otherwise
	 *         display the outer.
	 */
	public boolean displayInner() {
		return checkBooleanProperty(DisplayArray.SHOWINNER_PROPERTY);
	}

	// add the control panel
	@Override
	protected void addControls() {

		_controlPanel = new ControlPanel(this,
				ControlPanel.DISPLAYARRAY + ControlPanel.FEEDBACK + ControlPanel.ACCUMULATIONLEGEND,
				DisplayBits.ACCUMULATION + DisplayBits.MCTRUTH+ DisplayBits.RECCAL + DisplayBits.UVWSTRIPS, 3, 5);

		add(_controlPanel, BorderLayout.EAST);
		pack();
	}

	/**
	 * Used to create the EC view
	 * 
	 * @return the view
	 */
	public static PCALView createPCALView() {
		PCALView view = new PCALView(_baseTitle + ((CLONE_COUNT == 0) ? "" : ("_(" + CLONE_COUNT + ")")));

		return view;
	}

	// add items to the view
	@Override
	protected void addItems() {
		LogicalLayer detectorLayer = getContainer().getLogicalLayer(_detectorLayerName);

		_hexItems = new PCALHexSectorItem[6];

		for (int sector = 0; sector < 6; sector++) {
			_hexItems[sector] = new PCALHexSectorItem(detectorLayer, this, sector + 1);
			_hexItems[sector].getStyle().setFillColor(X11Colors.getX11Color("dark cyan"));
		}
	}

	/**
	 * Create the view's before drawer.
	 */
	private void setBeforeDraw() {
		// use a before-drawer to sector dividers and labels
		IDrawable beforeDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {

			}

		};

		getContainer().setBeforeDraw(beforeDraw);
	}

	private void setAfterDraw() {
		// use a before-drawer to sector dividers and labels
		IDrawable beforeDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {
				if (!_eventManager.isAccumulating()) {

					// draw MC Hits
					_mcHitDrawer.draw(g, container);
					
					//draw REC::Calorimeter data
					_recDrawer.draw(g, container);
					
				} // not acumulating
			}

		};

		getContainer().setAfterDraw(beforeDraw);
	}

	// get the attributes to pass to the super constructor
	private static Object[] getAttributes(String title) {

		Properties props = new Properties();
		props.put(PropertySupport.TITLE, title);

		// set to a fraction of screen
		Dimension d = GraphicsUtilities.screenFraction(0.7);

		props.put(PropertySupport.WORLDSYSTEM, _defaultWorld);
		props.put(PropertySupport.WIDTH, (int) (0.866 * d.height));
		props.put(PropertySupport.HEIGHT, d.height);

		props.put(PropertySupport.TOOLBAR, true);
		props.put(PropertySupport.TOOLBARBITS, CedView.TOOLBARBITS);
		props.put(PropertySupport.VISIBLE, true);
		props.put(PropertySupport.BACKGROUND, X11Colors.getX11Color("Alice Blue"));
		props.put(PropertySupport.STANDARDVIEWDECORATIONS, true);

		return PropertySupport.toObjectArray(props);
	}

	@Override
	public void getFeedbackStrings(IContainer container, Point pp, Point2D.Double wp, List<String> feedbackStrings) {

		super.getFeedbackStrings(container, pp, wp, feedbackStrings);

		if (showMcTruth()) {
			_mcHitDrawer.feedback(container, pp, wp, feedbackStrings);
		}

		_recDrawer.feedback(container, pp, wp, feedbackStrings);

	}

	/**
	 * Convert ijk coordinates to sector xyz
	 * 
	 * @param pijk      the ijk coordinates
	 * @param sectorXYZ the sector xyz coordinates
	 */
	public void ijkToSectorXYZ(Point3D pijk, double[] sectorXYZ) {
		PCALGeometry.ijkToSectorXYZ(pijk, sectorXYZ);
	}

	/**
	 * Convert sector xyz to ijk coordinates
	 * 
	 * @param pijk      the ijk coordinates
	 * @param sectorXYZ the sector xyz coordinates
	 */
	public void sectorXYZToIJK(Point3D pijk, double[] sectorXYZ) {
		Point3D sectorP = new Point3D(sectorXYZ[0], sectorXYZ[1], sectorXYZ[2]);
		PCALGeometry.getTransformations().sectorToLocal(pijk, sectorP);
	}

	/**
	 * Get the hex item for the given 1-based sector
	 * 
	 * @param sector the 1-based sector
	 * @return the corresponding item
	 */
	public PCALHexSectorItem getHexSectorItem(int sector) {
		return _hexItems[sector - 1];
	}

	/**
	 * Clone the view.
	 * 
	 * @return the cloned view
	 */
	@Override
	public BaseView cloneView() {
		super.cloneView();
		CLONE_COUNT++;

		// limit
		if (CLONE_COUNT > 2) {
			return null;
		}

		Rectangle vr = getBounds();
		vr.x += 40;
		vr.y += 40;

		PCALView view = createPCALView();
		view.setBounds(vr);
		return view;

	}
}
