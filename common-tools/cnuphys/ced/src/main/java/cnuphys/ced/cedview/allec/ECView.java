package cnuphys.ced.cedview.allec;

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
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayArray;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.geometry.ECGeometry;
import cnuphys.ced.item.ECHexSectorItem;

public class ECView extends HexView {

	// sector items
	private ECHexSectorItem _hexItems[];

	// for naming clones
	private static int CLONE_COUNT = 0;

	// base title
	private static final String _baseTitle = "ECAL";

	// for drawing MC hits
	private McHitDrawer _mcHitDrawer;
	
	// for drawing REC::Calorimeter data
    private RecDrawer _recDrawer;

//	private static final double _xsize = 420.0;
	private static final double _xsize = 430.0;
	private static final double _ysize = _xsize * 1.154734;

	protected static Rectangle2D.Double _defaultWorld = new Rectangle2D.Double(_xsize, -_ysize, -2 * _xsize,
			2 * _ysize);

	/**
	 * Create an allDCView
	 * 
	 * @param keyVals variable set of arguments.
	 */
	private ECView(String title) {
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
				DisplayBits.ACCUMULATION + DisplayBits.MCTRUTH+ DisplayBits.RECCAL + DisplayBits.INNEROUTER + DisplayBits.UVWSTRIPS, 3, 5);

		add(_controlPanel, BorderLayout.EAST);
		pack();
	}

	/**
	 * Used to create the EC view
	 * 
	 * @return the view
	 */
	public static ECView createECView() {
		ECView view = new ECView(_baseTitle + ((CLONE_COUNT == 0) ? "" : ("_(" + CLONE_COUNT + ")")));

		return view;
	}

	// add items to the view
	@Override
	protected void addItems() {
		LogicalLayer detectorLayer = getContainer().getLogicalLayer(_detectorLayerName);

		_hexItems = new ECHexSectorItem[6];

		for (int sector = 0; sector < 6; sector++) {
			_hexItems[sector] = new ECHexSectorItem(detectorLayer, this, sector + 1);
			_hexItems[sector].getStyle().setFillColor(X11Colors.getX11Color("steel blue"));
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
		IDrawable afterDraw = new DrawableAdapter() {

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

		getContainer().setAfterDraw(afterDraw);
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

		if (displayInner()) {
			feedbackStrings.add("$white$INNER plane");
		} else {
			feedbackStrings.add("$white$OUTER plane");
		}

		super.getFeedbackStrings(container, pp, wp, feedbackStrings);

		if (showMcTruth()) {
			_mcHitDrawer.feedback(container, pp, wp, feedbackStrings);
		}
		
		_recDrawer.feedback(container, pp, wp, feedbackStrings);

	}

	// /**
	// * Convert the sector xy coordinates to IJK
	// * @param sectorXY sector xy coordinates
	// * @param pijk ijk coordinates
	// */
	// public void sectorXYToIJK(Point2D.Double sectorXY, PointIJK pijk) {
	// boolean inner = displayInner();
	//
	// double r0[] = inner ? ECGeometry.getR0(0) : ECGeometry.getR0(1);
	//
	// pijk.k = inner ? 0 : ECGeometry.getDeltaK();
	// pijk.j = sectorXY.y;
	// pijk.i = sectorXY.x - r0[0];
	//
	// }

	/**
	 * Convert ijk coordinates to sector xyz
	 * 
	 * @param pijk      the ijk coordinates
	 * @param sectorXYZ the sector xyz coordinates
	 */
	public void ijkToSectorXYZ(Point3D pijk, double[] sectorXYZ) {
		boolean inner = displayInner();

		int plane = inner ? ECGeometry.EC_INNER : ECGeometry.EC_OUTER;
		ECGeometry.ijkToSectorXYZ(plane, pijk, sectorXYZ);
	}

	/**
	 * Convert sector xyz to ijk coordinates
	 * 
	 * @param pijk      the ijk coordinates
	 * @param sectorXYZ the sector xyz coordinates
	 */
	public void sectorXYZToIJK(Point3D pijk, double[] sectorXYZ) {
		boolean inner = displayInner();

		Point3D r0 = inner ? ECGeometry.getR0(ECGeometry.EC_INNER) : ECGeometry.getR0(ECGeometry.EC_OUTER);

		double delx = sectorXYZ[0] - r0.x();
		double dely = sectorXYZ[1] - r0.y();
		double delz = sectorXYZ[2] - r0.z();

		pijk.setY(dely);

		pijk.setX(delx * ECGeometry.COSTHETA - delz * ECGeometry.SINTHETA);
		pijk.setZ(delz * ECGeometry.COSTHETA + delx * ECGeometry.SINTHETA);
	}

	/**
	 * Lab (CLAS) 3D Cartesian coordinates to world graphical coordinates.
	 * 
	 * @param labXYZ the lab 3D coordinates
	 * @param wp     will hold the graphical world coordinates
	 */
	public void labXYZToWorld(double labXYZ[], Point2D.Double wp) {
		wp.setLocation(labXYZ[0], labXYZ[1]);
	}

	/**
	 * Get the hex item for the given 1-based sector
	 * 
	 * @param sector the 1-based sector
	 * @return the corresponding item
	 */
	public ECHexSectorItem getHexSectorItem(int sector) {
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

		ECView view = createECView();
		view.setBounds(vr);
		return view;

	}

}
