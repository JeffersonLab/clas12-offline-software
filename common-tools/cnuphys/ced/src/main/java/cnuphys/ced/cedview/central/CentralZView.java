package cnuphys.ced.cedview.central;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.graphics.world.WorldPolygon;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.bCNU.util.VectorSupport;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.bCNU.view.BaseView;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.event.data.Cosmic;
import cnuphys.ced.event.data.CosmicList;
import cnuphys.ced.event.data.Cosmics;
import cnuphys.ced.geometry.BSTGeometry;
import cnuphys.ced.geometry.BSTxyPanel;
import cnuphys.ced.geometry.bmt.BMTSectorItem;
import cnuphys.ced.geometry.bmt.Constants;
import cnuphys.ced.geometry.BMTGeometry;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.item.BeamLineItem;
import cnuphys.ced.item.MagFieldItem;
import cnuphys.magfield.MagneticFields;
import cnuphys.swim.SwimTrajectory2D;

@SuppressWarnings("serial")
public class CentralZView extends CedView implements ChangeListener {

	
	//for naming clones
	private static int CLONE_COUNT = 0;
	
	//base title
	private static final String _baseTitle = "Central Z";

	private double _targetZ = 0;
	private double _phi = 0; // the cross-sectional phi value
	private double _sinphi = 0;
	private double _cosphi = 1;
	// font for label text
	private static final Font labelFont = Fonts.commonFont(Font.PLAIN, 11);

	// margins around active area
	private static int LMARGIN = 50;
	private static int TMARGIN = 20;
	private static int RMARGIN = 20;
	private static int BMARGIN = 50;

	// fill color
	private static final Color HITFILL = new Color(255, 128, 0, 64);
	private static final Color TRANS = new Color(192, 192, 192, 128);

	// line stroke
	private static Stroke stroke = GraphicsUtilities.getStroke(1.5f,
			LineStyle.SOLID);


	// units are mm
	private static Rectangle2D.Double _defaultWorldRectangle = new Rectangle2D.Double(
			-240., -230., 520., 460.);

	// used to draw swum trajectories (if any) in the after drawer
	private SwimTrajectoryDrawerZ _swimTrajectoryDrawer;

	// draws reconstructed crosses
	private CrossDrawerZ _crossDrawer;

	// draws hits
	private CentralZHitDrawer _hitDrawer;

	public CentralZView(Object... keyVals) {
		super(keyVals);
		_crossDrawer = new CrossDrawerZ(this);
		_hitDrawer = new CentralZHitDrawer(this);

		setBeforeDraw();
		setAfterDraw();
		addItems();

		// draws any swum trajectories (in the after draw)
		_swimTrajectoryDrawer = new SwimTrajectoryDrawerZ(this);
	}

	/**
	 * Create a CentralZView object
	 * 
	 * @return the new view
	 */
	public static CentralZView createCentralZView() {
		CentralZView view = null;

		// set to a fraction of screen
		Dimension d = GraphicsUtilities.screenFraction(0.35);

		// make it square
		int width = d.width;
		int height = width;
		
		String title = _baseTitle + ((CLONE_COUNT == 0) ? "" : ("_(" + CLONE_COUNT + ")"));


		// create the view
		view = new CentralZView(PropertySupport.WORLDSYSTEM, _defaultWorldRectangle,
				PropertySupport.WIDTH, width, // container width, not total view
											  // width
				PropertySupport.HEIGHT, height, // container height, not total
												// view width
				PropertySupport.LEFTMARGIN, LMARGIN, PropertySupport.TOPMARGIN,
				TMARGIN, PropertySupport.RIGHTMARGIN, RMARGIN,
				PropertySupport.BOTTOMMARGIN, BMARGIN, PropertySupport.TOOLBAR,
				true, PropertySupport.TOOLBARBITS, CedView.TOOLBARBITS,
				PropertySupport.VISIBLE, true,
				PropertySupport.TITLE, title,
				PropertySupport.STANDARDVIEWDECORATIONS, true);

		view._controlPanel = new ControlPanel(view,
				ControlPanel.DISPLAYARRAY + ControlPanel.FEEDBACK
						+ ControlPanel.ACCUMULATIONLEGEND
						+ ControlPanel.PHISLIDER + ControlPanel.TARGETSLIDER
						+ ControlPanel.PHI_SLIDER_BIG + ControlPanel.FIELDLEGEND
						+ ControlPanel.DRAWLEGEND,
				DisplayBits.MAGFIELD | DisplayBits.ACCUMULATION
						| DisplayBits.CROSSES | DisplayBits.MCTRUTH
						| DisplayBits.COSMICS | DisplayBits.CVTTRACKS
						| DisplayBits.GLOBAL_HB |  DisplayBits.GLOBAL_TB,
				3, 5);

		view.add(view._controlPanel, BorderLayout.EAST);
		view.pack();
		
		view.phiFromSlider();
		return view;
	}

	/**
	 * Create the view's before drawer.
	 */
	private void setBeforeDraw() {
		// use a before-drawer to sector dividers and labels
		IDrawable beforeDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {
				Rectangle screenRect = container.getInsetRectangle();
				g.setColor(Color.white);
				g.fillRect(screenRect.x, screenRect.y, screenRect.width,
						screenRect.height);

			}

		};

		getContainer().setBeforeDraw(beforeDraw);
	}

	/**
	 * Set the view's before draw
	 */
	private void setAfterDraw() {
		IDrawable afterDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {
				if (showCosmics()) {
					drawCosmicTracks(g, container);
				}

				drawGEMCHits(g, container);
				drawBSTPanels(g, container);

				// not very sophisticated
				denoteBMT(g, container);

				_hitDrawer.draw(g, container);

				_swimTrajectoryDrawer.draw(g, container);

				if (showCrosses()) {
					_crossDrawer.draw(g, container);
				}

				Rectangle screenRect = getActiveScreenRectangle(container);
				drawAxes(g, container, screenRect);

				drawCoordinateSystem(g, container);
			}

		};
		getContainer().setAfterDraw(afterDraw);
	}

	//not real items
	private void denoteBMT(Graphics g, IContainer container) {
		
		Graphics2D g2 = (Graphics2D) g;
		Shape oldClip = g2.getClip();
		
		// clip the active area
		Rectangle sr = container.getInsetRectangle();
		g2.clipRect(sr.x, sr.y, sr.width, sr.height);


		for (int layer = 1; layer <= 6; layer++) {
			double r = BMTSectorItem.innerRadius[layer-1];
			int region = (layer+1)/2 - 1;
			double zmin;
			double zmax;
			Color color;
			
			if (BMTGeometry.getGeometry().isZLayer(layer)) {
				zmin = Constants.getCRZZMIN()[region];
				zmax = Constants.getCRZZMAX()[region];
				color = BMTSectorItem.zColor;
			}
			else {
				zmin = Constants.getCRCZMIN()[region];
				zmax = Constants.getCRCZMAX()[region];
				color = BMTSectorItem.cColor;
		}
			
			double w = zmax - zmin;
			double h = BMTSectorItem.FAKEWIDTH;
			
			Rectangle2D.Double wr = new Rectangle2D.Double(zmin, r, w, h);
			WorldGraphicsUtilities.drawWorldRectangle(g, container, wr, color,
					Color.gray, 1, LineStyle.SOLID);

			wr.setFrame(zmin, -r - h, w, h);
			WorldGraphicsUtilities.drawWorldRectangle(g, container, wr, color,
					Color.gray, 1, LineStyle.SOLID);


		}
		

		// hits?
		if (ClasIoEventManager.getInstance().isAccumulating()) {

		}
		else {
			if (isSingleEventMode()) {
//
//				int hitCount = BMT.hitCount();
//				if (hitCount > 0) {
//					int sect[] = BMT.sector();
//					int layer[] = BMT.layer();
//					int strip[] = BMT.strip();
//
//					Point2D.Double wp = new Point2D.Double();
//					Point pp = new Point();
//					for (int hit = 0; hit < hitCount; hit++) {
//
//						if ((layer[hit] == 5) || (layer[hit] == 6)) {
//							if (strip[hit] > 0) {
//								double z = geo.CRC_GetZStrip(sect[hit],
//										layer[hit], strip[hit]);
//								wp.x = z;
//
//								if (layer[hit] == 6) {
//									wp.y = r6 + 2;
//									container.worldToLocal(pp, wp);
//									g.setColor(X11Colors
//											.getX11Color("lawn green"));
//									g.fillOval(pp.x - 3, pp.y - 3, 6, 6);
//									g.setColor(Color.black);
//									g.drawOval(pp.x - 3, pp.y - 3, 6, 6);
//
//									wp.y = -r6 - 2;
//									container.worldToLocal(pp, wp);
//									g.setColor(X11Colors
//											.getX11Color("lawn green"));
//									g.fillOval(pp.x - 3, pp.y - 3, 6, 6);
//									g.setColor(Color.black);
//									g.drawOval(pp.x - 3, pp.y - 3, 6, 6);
//
//								}
//
//							}
//						} // layer == 5 or 6
//					} // for
//
//					if (showMcTruth()) {
//
//					}
//
//				} // hit count > 0
			} // single event mode
		}

		g2.setClip(oldClip);
	}
	
	// draw cosmic ray tracks
	private void drawCosmicTracks(Graphics g, IContainer container) {

		CosmicList cosmics;
		cosmics = Cosmics.getInstance().getCosmics();
		
		if ((cosmics == null) || cosmics.isEmpty()) {
			return;
		}

		Shape oldClip = clipView(g);
		
		Point p1 = new Point();
		Point p2 = new Point();
		for (Cosmic cosmic : cosmics) {
			double y1 = 100;
			double y2 = -100;
			double x1 = cosmic.trkline_yx_slope * y1 + cosmic.trkline_yx_interc;
			double x2 = cosmic.trkline_yx_slope * y2 + cosmic.trkline_yx_interc;
			double z1 = cosmic.trkline_yz_slope * y1 + cosmic.trkline_yz_interc;
			double z2 = cosmic.trkline_yz_slope * y2 + cosmic.trkline_yz_interc;
			
			//convert to mm
			x1 *= 10;
			x2 *= 10;
			y1 *= 10;
			y2 *= 10;
			z1 *= 10;
			z2 *= 10;
			labToLocal(x1, y1, z1, p1);
			labToLocal(x2, y2, z2, p2);
			
			g.setColor(Color.red);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);

		}

		g.setClip(oldClip);
	}



	// draw the panels
	private void drawBSTPanels(Graphics g, IContainer container) {
		List<BSTxyPanel> panels = GeometryManager.getBSTxyPanels();
		if (panels == null) {
			return;
		}

		// set the perp distance
		for (BSTxyPanel panel : panels) {
			Point2D.Double avgXY = panel.getXyAverage();
			double perp = avgXY.y * _cosphi - avgXY.x * _sinphi;
			panel.setPerp(perp);
		}

		Collections.sort(panels);
		Graphics2D g2 = (Graphics2D) g;
		Shape oldClip = g2.getClip();
		// clip the active area
		Rectangle sr = container.getInsetRectangle();
		g2.clipRect(sr.x, sr.y, sr.width, sr.height);

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(stroke);
		g2.setColor(Color.black);

		// there are 132 panels
		// mark the hits if there is gemc data
		CentralSupport.markPanelHits(this, panels);

		int index = 0;
		for (BSTxyPanel panel : panels) {

			int alpha = 10 + index / 3;
			Color col = new Color(128, 128, 128, alpha);
			Color col2 = new Color(128, 128, 128, alpha + 40);
			WorldPolygon poly[] = getFromPanel(panel);

			for (int j = 0; j < 3; j++) {
				boolean hit = panel.hit[j];

				WorldGraphicsUtilities.drawWorldPolygon(g2, container, poly[j],
						hit ? HITFILL : col, col2, 0, LineStyle.SOLID);
			}
		}

		// restore
		g2.setStroke(oldStroke);
		g2.setClip(oldClip);
	}

	private WorldPolygon[] getFromPanel(BSTxyPanel panel) {

		WorldPolygon polys[] = new WorldPolygon[3];

		double x1 = panel.getX1();
		double x2 = panel.getX2();

		double y1 = panel.getY1();
		double y2 = panel.getY2();

		double z0 = panel.getZ0();
		double z1 = panel.getZ1();
		double z2 = panel.getZ2();
		double z3 = panel.getZ3();
		double z4 = panel.getZ4();
		double z5 = panel.getZ5();

		double x[] = new double[5];
		double y[] = new double[5];

		Point2D.Double wp = new Point2D.Double();

		labToWorld(x1, y1, z0, wp);
		x[0] = wp.x;
		y[0] = wp.y;

		labToWorld(x2, y2, z0, wp);
		x[1] = wp.x;
		y[1] = wp.y;

		labToWorld(x2, y2, z1, wp);
		x[2] = wp.x;
		y[2] = wp.y;

		labToWorld(x1, y1, z1, wp);
		x[3] = wp.x;
		y[3] = wp.y;

		x[4] = x[0];
		y[4] = y[0];

		polys[0] = new WorldPolygon(x, y, 5);

		labToWorld(x1, y1, z2, wp);
		x[0] = wp.x;
		y[0] = wp.y;

		labToWorld(x2, y2, z2, wp);
		x[1] = wp.x;
		y[1] = wp.y;

		labToWorld(x2, y2, z3, wp);
		x[2] = wp.x;
		y[2] = wp.y;

		labToWorld(x1, y1, z3, wp);
		x[3] = wp.x;
		y[3] = wp.y;

		x[4] = x[0];
		y[4] = y[0];

		polys[1] = new WorldPolygon(x, y, 5);

		labToWorld(x1, y1, z4, wp);
		x[0] = wp.x;
		y[0] = wp.y;

		labToWorld(x2, y2, z4, wp);
		x[1] = wp.x;
		y[1] = wp.y;

		labToWorld(x2, y2, z5, wp);
		x[2] = wp.x;
		y[2] = wp.y;

		labToWorld(x1, y1, z5, wp);
		x[3] = wp.x;
		y[3] = wp.y;

		x[4] = x[0];
		y[4] = y[0];

		polys[2] = new WorldPolygon(x, y, 5);

		return polys;
	}

	@Override
	public Shape getSpecialClip() {
		Rectangle sr = getContainer().getInsetRectangle();
		return sr;
	}

	private void drawGEMCHits(Graphics g, IContainer container) {

	}

	private void drawCoordinateSystem(Graphics g, IContainer container) {
		Rectangle sr = getActiveScreenRectangle(container);

		FontMetrics fm = container.getComponent().getFontMetrics(labelFont);

		int size2 = 40;
		int size = 2 * size2;

		int left = sr.x + 15;
		int right = left + size;
		int bottom = sr.y + sr.height - 15;
		int top = bottom - size;
		int xc = left + size2;
		int yc = top + size2;

		// g.setFont(labelFont);
		// fm = getFontMetrics(labelFont);

		g.setColor(TRANS);
		g.fillRect(left - 4, top - 4, size + 8, size + 8);

		g.setColor(X11Colors.getX11Color("dark red"));
		g.drawLine(xc, yc, right - fm.stringWidth("z") - 4, yc);
		g.drawString("z", right - fm.stringWidth("z") - 2,
				yc + fm.getAscent() / 2);

		int xscale = (int) Math.abs(size2 * _cosphi);
		int xx = (int) (_sinphi * xscale);
		int xy = (int) (_cosphi * xscale);
		g.drawLine(xc, yc, xc + xx, yc - xy);
		if ((Math.abs(xx) > 5) || (Math.abs(xy) > 5)) {
			g.drawString("x", xc + xx, yc - xy);
		}
		int yscale = (int) Math.abs(size2 * _sinphi);
		int yx = (int) (-_cosphi * yscale);
		int yy = (int) (_sinphi * yscale);
		g.drawLine(xc, yc, xc + yx, yc - yy);

		if ((Math.abs(yx) > 5) || (Math.abs(yy) > 5)) {
			g.drawString("y", xc + yx, yc - yy);
		}

		// g.drawLine(right, bottom, right, top);
		//
		// g.drawString("y", right+3, top + fm.getHeight()/2-1);
		// g.drawString("x", left - fm.stringWidth("x") - 2, bottom +
		// fm.getHeight()/2);

	}

	// draw the coordinate axes
	private void drawAxes(Graphics g, IContainer container, Rectangle bounds) {
		Rectangle sr = getActiveScreenRectangle(container);

		FontMetrics fm = container.getComponent().getFontMetrics(labelFont);
		int fh = fm.getAscent();

		Rectangle2D.Double wr = new Rectangle2D.Double();
		container.localToWorld(sr, wr);
		Point2D.Double wp = new Point2D.Double();
		Point pp = new Point();

		g.setColor(Color.black);
		g.setFont(labelFont);
		g.drawRect(sr.x, sr.y, sr.width, sr.height);

		double del = wr.width / 50.;
		wp.y = wr.y;
		int bottom = sr.y + sr.height;
		for (int i = 0; i <= 50; i++) {
			wp.x = wr.x + del * i;
			container.worldToLocal(pp, wp);
			if ((i % 5) == 0) {
				g.drawLine(pp.x, bottom, pp.x, bottom - 12);

				String vs = valueString(wp.x);
				int xs = pp.x - fm.stringWidth(vs) / 2;

				g.drawString(vs, xs, bottom + fh + 1);

			}
			else {
				g.drawLine(pp.x, bottom, pp.x, bottom - 5);
			}
		}

		del = wr.height / 40.;
		wp.x = wr.x;
		for (int i = 0; i <= 40; i++) {
			wp.y = wr.y + del * i;
			container.worldToLocal(pp, wp);
			if ((i % 5) == 0) {
				g.drawLine(sr.x, pp.y, sr.x + 12, pp.y);
				String vs = valueString(wp.y);
				int xs = sr.x - fm.stringWidth(vs) - 1;

				g.drawString(vs, xs, pp.y + fh / 2);
			}
			else {
				g.drawLine(sr.x, pp.y, sr.x + 5, pp.y);
			}
		}

	}

	private String valueString(double val) {
		if (Math.abs(val) < 1.0e-3) {
			return "0";
		}
		if (Math.abs(val) < 1.0) {
			return DoubleFormat.doubleFormat(val, 1);
		}
		else {
			return "" + (int) Math.round(val);
		}
	}

	private Rectangle getActiveScreenRectangle(IContainer container) {
		return container.getInsetRectangle();
	}

	/**
	 * This adds the detector items. The AllDC view is not faithful to geometry.
	 * All we really uses in the number of superlayers, number of layers, and
	 * number of wires.
	 */
	private void addItems() {

		// add a field object, which won't do anything unless we can read in the
		// field.
		LogicalLayer magneticFieldLayer = getContainer()
				.getLogicalLayer(_magneticFieldLayerName);
		new MagFieldItem(magneticFieldLayer, this);
		magneticFieldLayer.setVisible(false);

		LogicalLayer detectorLayer = getContainer()
				.getLogicalLayer(_detectorLayerName);
		new BeamLineItem(detectorLayer);
	}

	@Override
	public int getSector(IContainer container, Point screenPoint,
			Point2D.Double worldPoint) {
		return 0;
	}

	/**
	 * Converts the local screen coordinate obtained by a previous localToWorld
	 * call to full 3D CLAS coordinates
	 * 
	 * @param screenPoint the pixel point
	 * @param worldPoint the corresponding world location.
	 * @param result holds the result. It has five elements. Cartesian x, y, and
	 *            z are in 0, 1, and 2. Cylindrical rho and phi are in 3 and 4.
	 *            (And of course cylindrical z is the same as Cartesian z.)
	 */
	public void getCLASCordinates(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, double result[]) {

		double labRho = Math.abs(worldPoint.y);
		double labZ = worldPoint.x;
		double labX = labRho * _cosphi;
		double labY = labRho * _sinphi;

		result[0] = labX;
		result[1] = labY;
		result[2] = labZ;
		result[3] = labRho;
		result[4] = _phi;
	}

	/**
	 * Some view specific feedback. Should always call super.getFeedbackStrings
	 * first.
	 * 
	 * @param container the base container for the view.
	 * @param screenPoint the pixel point
	 * @param worldPoint the corresponding world location.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, List<String> feedbackStrings) {

		// get the common information
		super.getFeedbackStrings(container, screenPoint, worldPoint,
				feedbackStrings);

		Rectangle sr = getActiveScreenRectangle(container);
		if (!sr.contains(screenPoint)) {
			return;
		}

		// the world coordinates
		double labRho = Math.abs(worldPoint.y);
		float labZ = (float)worldPoint.x;
		float labX = (float)(labRho * _cosphi);
		float labY = (float)(labRho * _sinphi);
		double r = Math.sqrt(labX * labX + labY * labY + labZ * labZ);
		double theta = Math.toDegrees(Math.atan2(labRho, labZ));

		String xyz = "xyz " + vecStr(labX, labY, labZ) + " mm";

		String rtp = CedView.rThetaPhi + " (" + valStr(r, 2) + " mm, "
				+ valStr(theta, 2) + UnicodeSupport.DEGREE + ", "
				+ valStr(_phi, 2) + UnicodeSupport.DEGREE + ")";

		String rzp = CedView.rhoZPhi + " (" + valStr(labRho, 2) + " mm, "
				+ valStr(labZ, 2) + " mm , " + valStr(_phi, 2)
				+ UnicodeSupport.DEGREE + ")";

		feedbackStrings.add(xyz);
		feedbackStrings.add(rtp);
		feedbackStrings.add(rzp);

		if (_activeProbe != null) {
			float field[] = new float[3];
			_activeProbe.field(labX/10, labY/10, labZ/10, field);
			// convert to Tesla from kG
			field[0] /= 10.0;
			field[1] /= 10.0;
			field[2] /= 10.0;

			double bmag = VectorSupport.length(field);
			feedbackStrings.add("$Lawn Green$"
					+ MagneticFields.getInstance().getActiveFieldDescription());
			feedbackStrings.add("$Lawn Green$Field " + valStr(bmag, 4) + " T "
					+ vecStr(field) + " T");
		}

		// near a swum trajectory?
		double mindist = _swimTrajectoryDrawer.closestApproach(worldPoint);
		double pixlen = WorldGraphicsUtilities.getMeanPixelDensity(container)
				* mindist;
		if (pixlen < 25.0) {
			SwimTrajectory2D traj2D = _swimTrajectoryDrawer
					.getClosestTrajectory();
			if (traj2D != null) {
				traj2D.addToFeedback(feedbackStrings);
			}
			else {
				System.err.println("null traj");
			}
		}

		// reconstructed feedback?
		_crossDrawer.feedback(container, screenPoint, worldPoint,
				feedbackStrings);

	}

	/**
	 * Returns a string representation of the form: "(x,y,z)".
	 * 
	 * @param numDec the number of decimal places for each coordinate.
	 * @return a String representation of the vector
	 */
	private String vecStr(float v[]) {
		return "(" + DoubleFormat.doubleFormat(v[0], 3) + ", "
				+ DoubleFormat.doubleFormat(v[1], 3) + ", "
				+ DoubleFormat.doubleFormat(v[2], 3) + ")";
	}

	// convenience call for double formatter
	private String valStr(double value, int numdec) {
		return DoubleFormat.doubleFormat(value, numdec);
	}

	/**
	 * Returns a string representation of the form: "(x,y,z)".
	 * 
	 * @param numDec the number of decimal places for each coordinate.
	 * @return a String representation of the vector
	 */
	private String vecStr(double vx, double vy, double vz) {
		return "(" + DoubleFormat.doubleFormat(vx, 2) + ", "
				+ DoubleFormat.doubleFormat(vy, 2) + ", "
				+ DoubleFormat.doubleFormat(vz, 2) + ")";
	}

	/**
	 * Get the world graphic coordinates from lab XYZ
	 * 
	 * @param x the lab x in cm
	 * @param y the lab y in cm
	 * @param z the lab z in cm
	 * @param wp the world point
	 */
	public void labToWorld(double x, double y, double z, Point2D.Double wp) {
		wp.x = z;
		wp.y = x * _cosphi + y * _sinphi;

	}

	public void labToLocal(double x, double y, double z, Point pp) {
		Point2D.Double wp = new Point2D.Double();
		labToWorld(x, y, z, wp);
		getContainer().worldToLocal(pp, wp);
	}

	/**
	 * Draw an svt strip
	 * 
	 * @param g2 graphics context
	 * @param container the container
	 * @param color the color
	 * @param sector 1-based sector 1..
	 * @param layer 1-based layer 1..8
	 * @param strip 1-based strip 1..255
	 */
	public void drawBSTStrip(Graphics2D g2, IContainer container, Color color,
			int sector, int layer, int strip) {

		float coords[] = new float[6];

		BSTGeometry.getStrip(sector, layer, strip, coords);

		Stroke oldStroke = g2.getStroke();
		g2.setColor(color);
		g2.setStroke(stroke);
		Point p1 = new Point();
		Point p2 = new Point();
		// Just draw a line from (x1,y1,z1) to (x2,y2,z2)

		// cm to mm
		labToLocal(10 * coords[0], 10 * coords[1], 10 * coords[2], p1);
		labToLocal(10 * coords[3], 10 * coords[4], 10 * coords[5], p2);

		g2.drawLine(p1.x, p1.y, p2.x, p2.y);
		g2.setStroke(oldStroke);

	}

	/**
	 * The z location of the target
	 * 
	 * @return z location of the target in cm
	 */
	public double getTargetZ() {
		return _targetZ;
	}

	/**
	 * This is used to listen for changes on components like sliders.
	 * 
	 * @param e the causal event.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();

		// change target z?
		if (source == _controlPanel.getTargetSlider()) {
			_targetZ = (_controlPanel.getTargetSlider().getValue());
			getContainer().refresh();
		}
		else if (source == _controlPanel.getPhiSlider()) {
			phiFromSlider();
			getContainer().setDirty(true);
			getContainer().refresh();
		}
	}
	
	private void phiFromSlider() {
		_phi = _controlPanel.getPhiSlider().getValue();
		_sinphi = Math.sin(Math.toRadians(_phi));
		_cosphi = Math.cos(Math.toRadians(_phi));
	}
	
	
	/**
	 * Clone the view. 
	 * @return the cloned view
	 */
	@Override
	public BaseView cloneView() {
		super.cloneView();
		CLONE_COUNT++;
		
		//limit
		if (CLONE_COUNT > 2) {
			return null;
		}
		
		Rectangle vr = getBounds();
		vr.x += 40;
		vr.y += 40;
		
		CentralZView view = createCentralZView();
		view.setBounds(vr);
		return view;

	}
}
