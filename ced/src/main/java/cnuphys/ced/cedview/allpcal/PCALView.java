package cnuphys.ced.cedview.allpcal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.jlab.geom.prim.Point3D;

import cnuphys.bCNU.attributes.AttributeType;
import cnuphys.bCNU.attributes.Attributes;
import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.toolbar.BaseToolBar;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.cedview.HexView;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayArray;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.geometry.PCALGeometry;
import cnuphys.ced.item.HexSectorItem;
import cnuphys.ced.item.PCALHexSectorItem;

public class PCALView extends HexView {

	// sector items
	private PCALHexSectorItem _hexItems[];

	// for drawing MC hits
	private McHitDrawer _mcHitDrawer;

	private static final double _xsize = 400.0;
	private static final double _ysize = _xsize * 1.154734;
	// private static final double _ysize = 1.0;
	protected static Rectangle2D.Double _defaultWorld = new Rectangle2D.Double(
			_xsize, -_ysize, -2 * _xsize, 2 * _ysize);

	// public static final double _XMAX = 400; //cm
	// public static final double _YMAX = _XMAX;

	/**
	 * Create an allDCView
	 * 
	 * @param keyVals
	 *            variable set of arguments.
	 */
	private PCALView(String title) {
		super(getAttributes(title));

		// MC hit drawer
		_mcHitDrawer = new McHitDrawer(this);

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

		_controlPanel = new ControlPanel(this, ControlPanel.DISPLAYARRAY
				+ ControlPanel.FEEDBACK + ControlPanel.ACCUMULATIONLEGEND
				+ ControlPanel.RECONSARRAY, DisplayBits.ACCUMULATION
				+ DisplayBits.MCTRUTH + DisplayBits.UVWSTRIPS, 3, 2);

		add(_controlPanel, BorderLayout.EAST);
		pack();
	}

	/**
	 * Used to create the EC view
	 * 
	 * @return the view
	 */
	public static PCALView createPCALView() {
		PCALView view = new PCALView("PCAL");

		return view;
	}

	// add items to the view
	@Override
	protected void addItems() {
		LogicalLayer detectorLayer = getContainer().getLogicalLayer(
				_detectorLayerName);

		_hexItems = new PCALHexSectorItem[6];

		for (int sector = 0; sector < 6; sector++) {
			_hexItems[sector] = new PCALHexSectorItem(detectorLayer, this,
					sector + 1);
			_hexItems[sector].getStyle().setFillColor(
					X11Colors.getX11Color("dark cyan"));
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

				} // not acumulating
			}

		};

		getContainer().setAfterDraw(beforeDraw);
	}

	// get the attributes to pass to the super constructor
	private static Object[] getAttributes(String title) {

		Attributes attributes = new Attributes();
		attributes.add(AttributeType.TITLE, title);

		// set to a fraction of screen
		Dimension d = GraphicsUtilities.screenFraction(0.7);

		attributes.add(AttributeType.WORLDSYSTEM, _defaultWorld);
		attributes.add(AttributeType.WIDTH, (int) (0.866 * d.height));
		attributes.add(AttributeType.HEIGHT, d.height);

		attributes.add(AttributeType.TOOLBAR, true);
		attributes.add(AttributeType.TOOLBARBITS, BaseToolBar.NODRAWING
				& ~BaseToolBar.RANGEBUTTON & ~BaseToolBar.TEXTFIELD
				& ~BaseToolBar.CONTROLPANELBUTTON & ~BaseToolBar.TEXTBUTTON
				& ~BaseToolBar.DELETEBUTTON);
		attributes.add(AttributeType.VISIBLE, true);
		attributes.add(AttributeType.HEADSUP, false);

		attributes.add(AttributeType.BACKGROUND,
				X11Colors.getX11Color("Alice Blue"));
		attributes.add(AttributeType.STANDARDVIEWDECORATIONS, true);

		return attributes.toObjectArray();
	}

	@Override
	public void getFeedbackStrings(IContainer container, Point pp,
			Point2D.Double wp, List<String> feedbackStrings) {

		super.getFeedbackStrings(container, pp, wp, feedbackStrings);

		if (showMcTruth()) {
			_mcHitDrawer.feedback(container, pp, wp, feedbackStrings);
		}

	}

	/**
	 * Convert ijk coordinates to sector xyz
	 * 
	 * @param pijk
	 *            the ijk coordinates
	 * @param sectorXYZ
	 *            the sector xyz coordinates
	 */
	public void ijkToSectorXYZ(Point3D pijk, double[] sectorXYZ) {
		PCALGeometry.ijkToSectorXYZ(pijk, sectorXYZ);
	}

	/**
	 * Convert sector xyz to ijk coordinates
	 * 
	 * @param pijk
	 *            the ijk coordinates
	 * @param sectorXYZ
	 *            the sector xyz coordinates
	 */
	public void sectorXYZToIJK(Point3D pijk, double[] sectorXYZ) {
		Point3D sectorP = new Point3D(sectorXYZ[0], sectorXYZ[1], sectorXYZ[2]);
		PCALGeometry.getTransformations().sectorToLocal(pijk, sectorP);
	}

	/**
	 * Lab (CLAS) 3D Cartesian coordinates to screen coordinates.
	 * 
	 * @param labXYZ
	 *            the lab 3D coordinates
	 * @param pp
	 *            will hold the screen coordinates
	 */
	public void labXYZToScreen(double labXYZ[], Point pp) {

		Point3D clasP = new Point3D(labXYZ[0], labXYZ[1], labXYZ[2]);
		int sector = GeometryManager.clasToSectorNumber(clasP);
		Point3D localP = new Point3D();
		PCALGeometry.getTransformations().clasToLocal(localP, clasP);
		ijkToScreen(sector, localP, pp);
	}

	/**
	 * Convert ijk coordinates to world graphics coordinates
	 * 
	 * @param sector
	 *            the 1-based sector [1..6]
	 * @param pijk
	 *            the ijk coordinates
	 * @param wp
	 *            the world graphics coordinates
	 */
	public void ijkToWorld(int sector, Point3D pijk, Point2D.Double wp) {
		double sectorXYZ[] = new double[3];
		double labXYZ[] = new double[3];
		ijkToSectorXYZ(pijk, sectorXYZ);
		GeometryManager.sectorXYZToLabXYZ(sector, labXYZ, sectorXYZ);
		GeometryManager.cal_labXYZToWorld(0, labXYZ, wp);
	}

	/**
	 * Convert ijk coordinates to world graphics coordinates
	 * 
	 * @param sector
	 *            the 1-based sector [1..6]
	 * @param pijk
	 *            the ijk coordinates
	 * @param pp
	 *            the screen coordinates
	 */
	public void ijkToScreen(int sector, Point3D pijk, Point pp) {
		Point2D.Double wp = new Point2D.Double();
		ijkToWorld(sector, pijk, wp);
		getContainer().worldToLocal(pp, wp);
	}

	/**
	 * Get the hex item for the given 1-based sector
	 * 
	 * @param sector
	 *            the 1-based sector
	 * @return the corresponding item
	 */
	public HexSectorItem getHexSectorItem(int sector) {
		return _hexItems[sector - 1];
	}

}
