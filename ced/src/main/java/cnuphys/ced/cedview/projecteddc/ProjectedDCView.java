package cnuphys.ced.cedview.projecteddc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jlab.geom.prim.Plane3D;
import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.common.CrossDrawer;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.swim.SwimTrajectory2D;

public class ProjectedDCView extends CedView implements ChangeListener {
	
	//what sector 1..6
	private int _sector = 1;
	
	// small number test
	private static final double TINY = 1.0e-10;

	// offset left and top
	private static int LEFT = 40;
	private static int TOP = 300;
	
	//detector logical layer
	private LogicalLayer _detectorLayer;
	
	// determines if the wire intersections must be recalculated. This is caused
	// by a change in phi using the phi slider.
	private Boolean _wiresDirty = true;

	//1sr index is sector, second index is superlayer
	private ProjectedSuperLayer _superLayers[] = new ProjectedSuperLayer[6];
	
	// reconstructed cross drawer (and feedback handler)
	private CrossDrawer _crossDrawer;

	// used to draw swum trajectories (if any) in the after drawer
	private SwimTrajectoryDrawer _swimTrajectoryDrawer;

	// for drawing MC hits
	private McHitDrawer _mcHitDrawer;

	public ProjectedDCView(Object... keyVals) {
		super(keyVals);
		addItems();
		setBeforeDraw();
		setAfterDraw();
		
		//for objects not on a superlayer, project onto mid plane
		projectionPlane = GeometryManager.constantPhiPlane(0);
		
		// draws any swum trajectories (in the after draw)
		_swimTrajectoryDrawer = new SwimTrajectoryDrawer(this);

		// cross drawer
		_crossDrawer = new CrossDrawer(this);
		
		// MC hit drawer
		_mcHitDrawer = new McHitDrawer(this);
	}
	
	/**
	 * Convenience method for creating a ProjectedDCView View.
	 * 
	 * @return a new ProjectedDCView.
	 */
	public static ProjectedDCView createProjectedDCView() {
		ProjectedDCView view = null;

		double xo = 20; // cm. 
		double zo = 177; // cm.
		double wheight = 349;
		double wwidth = 370;

		Dimension d = GraphicsUtilities.screenFraction(0.60);

		// give container same aspect ratio
		int height = d.height;
		int width = (int) ((wwidth * height) / wheight);

		// give the view a title based on what sectors are displayed
		String title = "Projected Drift Chambers";

		// create the view
		view = new ProjectedDCView(PropertySupport.WORLDSYSTEM,
				new Rectangle2D.Double(zo, xo, wwidth, wheight),

				PropertySupport.LEFT, LEFT, PropertySupport.TOP, TOP,
				PropertySupport.WIDTH, width, PropertySupport.HEIGHT, height,
				PropertySupport.TOOLBAR, true, PropertySupport.TOOLBARBITS,
				CedView.NORANGETOOLBARBITS, PropertySupport.VISIBLE, true,
				PropertySupport.HEADSUP, false,

				PropertySupport.BACKGROUND,
				X11Colors.getX11Color("gray"),
				PropertySupport.TITLE, title,
				PropertySupport.STANDARDVIEWDECORATIONS, true);

		view._controlPanel = new ControlPanel(view, ControlPanel.NOISECONTROL
				+ ControlPanel.DISPLAYARRAY 
				+ ControlPanel.DRAWLEGEND + ControlPanel.FEEDBACK
				+ ControlPanel.ACCUMULATIONLEGEND, DisplayBits.DC_HB_RECONS_CROSSES
				+ DisplayBits.DC_TB_RECONS_CROSSES
				+ DisplayBits.DC_TB_RECONS_DOCA + DisplayBits.DC_HB_RECONS_SEGMENTS 
				+ DisplayBits.DC_TB_RECONS_SEGMENTS
				+ DisplayBits.GLOBAL_HB + DisplayBits.GLOBAL_TB
				+ DisplayBits.ACCUMULATION
				+ DisplayBits.MCTRUTH, 3, 5);

		view.add(view._controlPanel, BorderLayout.EAST);
		
		//select which sector
		SectorSelectorPanel ssp = new SectorSelectorPanel(view);
		view._controlPanel.addNorth(ssp);

		view.pack();

		return view;
	}

	/**
	 * Add all the items on this view
	 */
	private void addItems() {

		_detectorLayer = getContainer().getLogicalLayer(_detectorLayerName);

		for (int supl = 0; supl < 6; supl++) {
			_superLayers[supl] = new ProjectedSuperLayer(_detectorLayer, this, supl + 1);
		}
	}

	/**
	 * Set the views before draw
	 */
	private void setBeforeDraw() {
		IDrawable beforeDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {

				if (_wiresDirty) {

					for (int supl = 0; supl < 6; supl++) {
						_superLayers[supl].dirtyWires();
					}
					_wiresDirty = false;

				}
			}

		};
		getContainer().setBeforeDraw(beforeDraw);
	}

	/**
	 * Set the views before draw
	 */
	private void setAfterDraw() {
		final ProjectedDCView pview = this;
		IDrawable afterDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {
				
				// draw trajectories, once for each superlayer group
				//they have different projections!
				
				Shape oldclip = g.getClip();
				Plane3D oldplane = pview.projectionPlane;
				
				for (int supl = 1; supl <= 6; supl++) {
					Shape clip = _superLayers[supl - 1].getLastDrawnPolygon();
					if ((clip != null) && clip.intersects(container.getComponent().getBounds())) {
						g.setClip(clip);
						pview.projectionPlane = _superLayers[supl - 1].projectionPlane();
						_swimTrajectoryDrawer.draw(g, container);
						
						// draw MC Hits
						_mcHitDrawer.draw(g, container);

					}
				}
				
				
				g.setClip(oldclip);
				pview.projectionPlane = oldplane;
				

				// draw reconstructed dc crosses
				if (showDChbCrosses()) {
					_crossDrawer.setMode(CrossDrawer.HB);
					_crossDrawer.draw(g, container);
				}
				if (showDCtbCrosses()) {
					_crossDrawer.setMode(CrossDrawer.TB);
					_crossDrawer.draw(g, container);
				}
			}

		};
		getContainer().setAfterDraw(afterDraw);
	}

	/**
	 * Set the sector
	 * @param sector the new sector
	 */
	public void setSector(int sector) {
		_sector = sector;
		_wiresDirty = true;
		getContainer().setDirty(true);
		getContainer().refresh();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
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
	 * @return the sector [1..6].
	 */
	@Override
	public int getSector(IContainer container, Point screenPoint, Double worldPoint) {
		return _sector;
	}
	
	/**
	 * Get the sector 1..6
	 * @return the sector
	 */
	public int getSector() {
		return _sector;
	}
	
	/**
	 * Convert world (not global, but graphical world) to clas global (bab)
	 * 
	 * @param wp
	 *            the world point
	 * @param labXYZ
	 *            the clas global coordinates
	 */
	public void worldToLabXYZ(Point2D.Double wp, double[] labXYZ) {
		double perp = wp.y;
		double z = wp.x;

		// we are essentially display a plan yp=0, with xp vertical and zp
		// horizontal. We need to rotate around z by "phiRotate" to get the x
		// and y coordinates. Note
		// it is a simple rotation since yp is zero
		double phiRotate = Math.toRadians((_sector-1)*60.0);
		labXYZ[0] = perp * Math.cos(phiRotate);
		labXYZ[1] = perp * Math.sin(phiRotate);
		labXYZ[2] = z;
	}

	
	/**
	 * Some view specific feedback. Should always call super.getFeedbackStrings
	 * first.
	 * 
	 * @param container
	 *            the base container for the view.
	 * @param pp
	 *            the pixel point
	 * @param wp
	 *            the corresponding world location.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point pp,
			Point2D.Double wp, List<String> feedbackStrings) {

		// get the common information
		super.getFeedbackStrings(container, pp, wp, feedbackStrings);

		double result[] = new double[3];
		worldToLabXYZ(wp, result);
		double x = result[0];
		double y = result[1];
		double z = result[2];

		// System.err.println("SECTOR: " + GeometryManager.getSector(x, y));

		String xyz = "xyz " + vecStr(result) + " cm";

		feedbackStrings.add(xyz);

		double rho = x * x + y * y;
		double r = Math.sqrt(rho + z * z);
		rho = Math.sqrt(rho);
		double theta = Math.toDegrees(Math.atan2(rho, z));

		// get absolute phi
		double absphi = (_sector-1)*60.;

		String rtp = CedView.rThetaPhi + " (" + valStr(r, 2) + "cm, "
				+ valStr(theta, 2) + UnicodeSupport.DEGREE + ", "
				+ valStr(absphi, 2) + UnicodeSupport.DEGREE + ")";
		feedbackStrings.add(rtp);

		// cylindrical coordinates which are just the world coordinates!
		String rzp = CedView.rhoZPhi + " (" + valStr(rho, 2) + "cm, "
				+ valStr(z, 2) + "cm , " + valStr(absphi, 2)
				+ UnicodeSupport.DEGREE + ")";
		feedbackStrings.add(rzp);

//		// sector coordinates
//		worldToSector(wp, result);
//		String sectxyz = "$yellow$Sector xyz " + vecStr(result) + " cm";
//		feedbackStrings.add(sectxyz);

		// tilted sector
		sectorToTilted(result, result);
		String tiltsectxyz = "$yellow$Tilted sect xyz " + vecStr(result)
				+ " cm";
		feedbackStrings.add(tiltsectxyz);

//		IField activeField = MagneticFields.getActiveField();
//		if (activeField != null) {
//			float field[] = new float[3];
//			activeField.fieldCylindrical(absphi, rho, z, field);
//			// convert to Tesla from kG
//			field[0] /= 10.0;
//			field[1] /= 10.0;
//			field[2] /= 10.0;
//
//			double bmag = VectorSupport.length(field);
//			feedbackStrings.add("$Lawn Green$"
//					+ MagneticFields.getActiveFieldDescription());
//			feedbackStrings.add("$Lawn Green$Field " + valStr(bmag, 4) + " T "
//					+ vecStr(field) + " T");
//		}
//		// near a swum trajectory?
		double mindist = _swimTrajectoryDrawer.closestApproach(wp);

		double pixlen = WorldGraphicsUtilities.getMeanPixelDensity(container)
				* mindist;

		_lastTrajStr = null;
		if (pixlen < 25.0) {
			SwimTrajectory2D traj2D = _swimTrajectoryDrawer
					.getClosestTrajectory();
			if (traj2D != null) {
				traj2D.addToFeedback(feedbackStrings);
				_lastTrajStr = traj2D.summaryString();
			}
		}

		// reconstructed feedback?
		if (showDChbCrosses()) {
			_crossDrawer.setMode(CrossDrawer.HB);
			_crossDrawer.vdrawFeedback(container, pp, wp, feedbackStrings, 0);
		}
		if (showDCtbCrosses()) {
			_crossDrawer.setMode(CrossDrawer.TB);
			_crossDrawer.vdrawFeedback(container, pp, wp, feedbackStrings, 0);
		}

		if (showMcTruth()) {
			_mcHitDrawer.vdrawFeedback(container, pp, wp, feedbackStrings, 0);
		}

	}

	// convenience call for double formatter
	private String valStr(double value, int numdec) {
		return DoubleFormat.doubleFormat(value, numdec);
	}

	/**
	 * Returns a string representation of the form: "(x,y,z)".
	 * 
	 * @param numDec
	 *            the number of decimal places for each coordinate.
	 * @return a String representation of the vector
	 */
	private String vecStr(double v[]) {
		return "(" + DoubleFormat.doubleFormat(v[0], 2) + ", "
				+ DoubleFormat.doubleFormat(v[1], 2) + ", "
				+ DoubleFormat.doubleFormat(v[2], 2) + ")";
	}

	/**
	 * Returns a string representation of the form: "(x,y,z)".
	 * 
	 * @param numDec
	 *            the number of decimal places for each coordinate.
	 * @return a String representation of the vector
	 */
	private String vecStr(float v[]) {
		return "(" + DoubleFormat.doubleFormat(v[0], 3) + ", "
				+ DoubleFormat.doubleFormat(v[1], 3) + ", "
				+ DoubleFormat.doubleFormat(v[2], 3) + ")";
	}

	/**
	 * Are the given global x and y in this view? That is, does the sector they
	 * correspond to in this view based on the calculated phi?
	 * 
	 * @param x
	 *            the global x
	 * @param y
	 *            the global y in the same units as x
	 * @return <code>true</code> if the point is in
	 */
	public boolean inThisView(double x, double y) {
		if ((Math.abs(x) < TINY) && (Math.abs(y) < TINY)) {
			return true;
		}
		double tphi = Math.toDegrees(Math.atan2(y, x)) + 30;
		if (tphi < 0) {
			tphi = tphi + 360.0;
		}
		return inThisView(tphi);
	}
	
	/**
	 * Check whether a give phi is included in this view's range.
	 * 
	 * @param phi
	 *            the value of phi on decimal degrees.
	 * @return <code>true</code> if it is included.
	 */
	public boolean inThisView(double phi) {
		while (phi < -30) {
			phi += 360.0;
		}
		
		//now phis should be from -30 to 330
		
		int sector =  1 + ((int) (phi+30.)) / 60;

		return (sector == _sector);
	}
	
}
