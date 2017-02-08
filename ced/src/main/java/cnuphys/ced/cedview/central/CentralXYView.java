package cnuphys.ced.cedview.central;

import java.awt.BorderLayout;

/**
 * Note this view started out as just the XY view for the BST (SVT). But it has evolved into the xy view for 
 * all central detectors. 
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Vector;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.BaseContainer;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.CedXYView;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.event.data.AdcHit;
import cnuphys.ced.event.data.AdcHitList;
import cnuphys.ced.event.data.BST;
import cnuphys.ced.event.data.CTOF;
import cnuphys.ced.event.data.Cosmic;
import cnuphys.ced.event.data.CosmicList;
import cnuphys.ced.event.data.Cosmics;
import cnuphys.ced.event.data.SVT;
import cnuphys.ced.event.data.TdcAdcHit;
import cnuphys.ced.event.data.TdcAdcHitList;
import cnuphys.ced.geometry.SVTGeometry;
import cnuphys.ced.geometry.SVTxyPanel;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.micromegas.MicroMegasSector;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.lund.X11Colors;
import cnuphys.swim.SwimTrajectory2D;

@SuppressWarnings("serial")
public class CentralXYView extends CedXYView {

	private SVTxyPanel _closestPanel;

	private static Color _panelColors[] = { X11Colors.getX11Color("sky blue"),
			X11Colors.getX11Color("light blue") };
	

	// the CND xy polygons
	CNDXYPolygon cndPoly[][] = new CNDXYPolygon[3][48];
	
	//the CTOF polygons
	CTOFXYPolygon ctofPoly[] = new CTOFXYPolygon[48];
	
	//Micro megas [sector][layer]
	private MicroMegasSector microMegasSector[][];

	// units are mm
	// private static Rectangle2D.Double _defaultWorldRectangle = new
	// Rectangle2D.Double(
	// 200., -200., -400., 400.);

	private static Rectangle2D.Double _defaultWorldRectangle = new Rectangle2D.Double(
			400, -400, -800, 800);

	// used to draw swum trajectories (if any) in the after drawer
	private SwimTrajectoryDrawer _swimTrajectoryDrawer;

	// draws reconstructed crosses
	private CrossDrawerXY _crossDrawer;
	
	//draws hits
	private CentralXYHitDrawer _hitDrawer;

	/**
	 * Create a Central detector XY View
	 * 
	 * @param keyVals
	 */
	private CentralXYView(Object... keyVals) {
		super(keyVals);

		_crossDrawer = new CrossDrawerXY(this);
		_hitDrawer = new CentralXYHitDrawer(this);
		
		// draws any swum trajectories (in the after draw)
		_swimTrajectoryDrawer = new SwimTrajectoryDrawer(this);

		// add the CND polys
		for (int layer = 1; layer <= 3; layer++) {
			for (int paddleId = 1; paddleId <= 48; paddleId++) {
				cndPoly[layer - 1][paddleId - 1] = new CNDXYPolygon(layer,
						paddleId);
			}
		}
		
		//ad the ctof polygons
		for (int paddleId = 1; paddleId <= 48; paddleId++) {
			ctofPoly[paddleId - 1] = new CTOFXYPolygon(paddleId);
		}

	}
	
	/**
	 * Get the Micromegas sector item
	 * @param sector [1..3]
	 * @param layer [1..6]
	 * @return the Micromegas sector item
	 */
	public MicroMegasSector getMicroMegasSector(int sector, int layer) {
		return microMegasSector[sector-1][layer-1];
	}

	/**
	 * Create a Central detector XY view
	 * 
	 * @return a Central detector XY View
	 */
	public static CentralXYView createCentralXYView() {

		// set to a fraction of screen
		Dimension d = GraphicsUtilities.screenFraction(0.35);

		// make it square
		int width = d.width;
		int height = width;

		// create the view
		final CentralXYView view = new CentralXYView(
				PropertySupport.WORLDSYSTEM, _defaultWorldRectangle, 
				PropertySupport.WIDTH, width,
				PropertySupport.HEIGHT, height, 
				PropertySupport.LEFTMARGIN, LMARGIN, 
				PropertySupport.TOPMARGIN, TMARGIN,
				PropertySupport.RIGHTMARGIN, RMARGIN,
				PropertySupport.BOTTOMMARGIN, BMARGIN, 
				PropertySupport.TOOLBAR, true, 
				PropertySupport.TOOLBARBITS, CedView.TOOLBARBITS,
				PropertySupport.VISIBLE, true, PropertySupport.HEADSUP, false,
				PropertySupport.TITLE, "Central XY",
				PropertySupport.STANDARDVIEWDECORATIONS, true);

		view._controlPanel = new ControlPanel(view,
				ControlPanel.DISPLAYARRAY + ControlPanel.FEEDBACK
						+ ControlPanel.ACCUMULATIONLEGEND
						+ ControlPanel.DRAWLEGEND,
				DisplayBits.ACCUMULATION + DisplayBits.SVTRECONS_CROSSES
						+ DisplayBits.SVTHITS + DisplayBits.MCTRUTH
						+ DisplayBits.COSMICS,
				3, 5);

		view.add(view._controlPanel, BorderLayout.EAST);
		view.pack();

		// add a specialized magview drawer
		((BaseContainer) (view.getContainer()))
				.setMagnificationDraw(new CentralXYMagDrawer(view));

		// add quick zooms
		view.addQuickZoom("SVT & BMT", -190, -190, 190, 190);
		return view;
	}

	/**
	 * Create the view's before drawer.
	 */
	@Override
	protected void setBeforeDraw() {
		// use a before-drawer to sector dividers and labels
		IDrawable beforeDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {
				Component component = container.getComponent();
				Rectangle b = component.getBounds();

				// ignore b.x and b.y as usual

				b.x = 0;
				b.y = 0;

				Rectangle screenRect = container.getInsetRectangle();
				g.setColor(Color.white);
				g.fillRect(screenRect.x, screenRect.y, screenRect.width,
						screenRect.height);

				drawPanels(g, container);
			}

		};

		getContainer().setBeforeDraw(beforeDraw);
	}

	/**
	 * Set the view's after draw
	 */
	@Override
	protected void setAfterDraw() {
		
		IDrawable afterDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {

				if (!_eventManager.isAccumulating()) {
					_swimTrajectoryDrawer.draw(g, container);
					if (showCosmics()) {
						drawCosmicTracks(g, container);
					}
					
					_hitDrawer.draw(g, container);

					if (showReconsCrosses()) {
						_crossDrawer.draw(g, container);
					}


					Rectangle screenRect = getActiveScreenRectangle(container);
					drawAxes(g, container, screenRect, true);

				}

			}

		};
		getContainer().setAfterDraw(afterDraw);
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
			double y1 = 1000;
			double y2 = -1000;
			double x1 = cosmic.trkline_yx_slope * y1 + cosmic.trkline_yx_interc;
			double x2 = cosmic.trkline_yx_slope * y2 + cosmic.trkline_yx_interc;
			container.worldToLocal(p1, x1, y1);
			container.worldToLocal(p2, x2, y2);
			
			g.setColor(Color.red);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);

		}

		g.setClip(oldClip);
	}

	/**
	 * Get the panel based on the layer and sector
	 * @param layer 1..8
	 * @param sector 1..24
	 * @return the panel
	 */
	public static SVTxyPanel getPanel(int layer, int sector) {
		List<SVTxyPanel> panels = GeometryManager.getSVTxyPanels();
		if (panels == null) {
			return null;
		}

		synchronized (panels) {
			for (SVTxyPanel panel : panels) {
				if ((panel.getLayer() == layer) && (panel.getSector() == sector)) {
					return panel;
				}
			}
		}

		return null;
	}

	// draw the panels
	private void drawPanels(Graphics g, IContainer container) {

		Shape oldClip = g.getClip();

		List<SVTxyPanel> panels = GeometryManager.getSVTxyPanels();
		if (panels == null) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;

		Rectangle sr = container.getInsetRectangle();
		g2.clipRect(sr.x, sr.y, sr.width, sr.height);

		// SVT panels
		for (SVTxyPanel panel : panels) {
			drawSVTPanel(g2, container, panel,
					_panelColors[(panel.getSector()) % 2]);
		}

		// CND Polys
		for (int layer = 1; layer <= 3; layer++) {
			for (int paddleId = 1; paddleId <= 48; paddleId++) {
				if (cndPoly[layer - 1][paddleId - 1] != null) {
					cndPoly[layer - 1][paddleId - 1].draw(g2, container);
				}
			}

		}
		
		// CTOF Polys
		for (int paddleId = 1; paddleId <= 48; paddleId++) {
			if (ctofPoly[paddleId - 1] != null) {
				ctofPoly[paddleId - 1].draw(g2, container);
			}
		}
		

		g.setClip(oldClip);
	}

	// draw one SVT panel
	public void drawSVTPanel(Graphics2D g2, IContainer container,
			SVTxyPanel panel, Color color) {
		
		Stroke oldStroke = g2.getStroke();
		g2.setColor(color);
		Point p1 = new Point();
		Point p2 = new Point();
		Point2D.Double wp1 = new Point2D.Double();
		Point2D.Double wp2 = new Point2D.Double();
		g2.setStroke((panel == _closestPanel) ? stroke2 : stroke);
		// Just draw a line from (x1,y1) to (x2,y2)

		wp1.setLocation(panel.getX1(), panel.getY1());
		wp2.setLocation(panel.getX2(), panel.getY2());
		container.worldToLocal(p1, wp1);
		container.worldToLocal(p2, wp2);
		g2.drawLine(p1.x, p1.y, p2.x, p2.y);
		g2.setStroke(oldStroke);

		// draw sector number

		Point porig = new Point();
		Point pmid = new Point();
		Point2D.Double wporig = new Point2D.Double();
		container.worldToLocal(porig, wporig);
		g2.setFont(Fonts.tinyFont);
		FontMetrics fm = getFontMetrics(g2.getFont());

		if ((panel.getLayer() % 2) == 0) {
			g2.setColor(TEXT);
			pmid.x = (p1.x + p2.x) / 2;
			pmid.y = (p1.y + p2.y) / 2;
//			String s = "" + panel.getSector();
			//TODO fix HACK
//			String s = "" + svtSectorHack(panel.getLayer(), panel.getSector());
			String s = "" +  panel.getSector();
			extendLine(porig, pmid, 4 + panel.getLayer() / 2, fm.stringWidth(s),
					fm.getHeight());
			g2.drawString(s, pmid.x, pmid.y);
		}
	}
	
//	/**
//	 * Rotates the sector number by 180 degrees
//	 * @param layer the layer 1..8
//	 * @param sector the sector 1..N
//	 */
	public int svtSectorHack(int layer, int sector) {
		int superlayer = (layer-1) / 2; //zero based
		int numSect = SVTGeometry.sectorsPerSuperlayer[superlayer];
		int n2 = numSect/2;
		int hackSect = (sector + n2) % numSect;
		if (hackSect == 0) {
			hackSect = numSect;
		}
	//	System.err.println("LAY: " + layer + "  SUPL: " + superlayer + " SECT " + sector + "  NUMSECT: " + 
	//	numSect + "  hackSect: " + hackSect);
		return hackSect;
	}

	private static void extendLine(Point p0, Point p1, int del, int sw,
			int fh) {
		double dx = p1.x - p0.x;
		double dy = p1.y - p0.y;
		double theta = Math.atan2(dy, dx);

		int quad = ((int) ((180 + Math.toDegrees(theta)) / 90)) % 4;

		int delx = (int) (del * Math.cos(theta));
		int dely = (int) (del * Math.sin(theta));

		if (quad == 0) {
			delx = delx - sw;
			// dely = dely + fh/2;
		}
		else if (quad == 2) {
			delx = delx + 2;
			dely = dely + fh / 2;
		}
		else if (quad == 3) {
			delx = delx - sw;
			dely = dely + fh / 2;
		}

		p1.x += delx;
		p1.y += dely;
	}



	/**
	 * This adds the detector items. The AllDC view is not faithful to geometry.
	 * All we really uses in the number of superlayers, number of layers, and
	 * number of wires.
	 */
	@Override
	protected void addItems() {
		//micromegas sectors for now only layers 5 & 6		
		LogicalLayer detectorLayer = getContainer().getLogicalLayer(
				_detectorLayerName);
		
		microMegasSector = new MicroMegasSector[3][6];
	    for (int sect = 1; sect <= 3; sect++) {
	    	for (int lay = 5; lay <= 6; lay++) {
	    		microMegasSector[sect-1][lay-1] = new MicroMegasSector(detectorLayer, sect, lay);
	    	}
	    }
	}

	/**
	 * Get the panel closest to the mouse
	 * 
	 * @return the panel closest to the mouse
	 */
	protected SVTxyPanel closestPanel() {
		return _closestPanel;
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

		basicFeedback(container, screenPoint, worldPoint, "mm",
				feedbackStrings);

		if (!Environment.getInstance().isDragging()) {
			SVTxyPanel newClosest = getClosest(worldPoint);
			if (newClosest != _closestPanel) {
				_closestPanel = newClosest;
				container.refresh();
			}
		}

		if (_closestPanel != null) {
			
			int region = (_closestPanel.getLayer() + 1) / 2;
			fbString("red", "svt layer " + _closestPanel.getLayer(),
					feedbackStrings);
			fbString("red", "svt region " + region, feedbackStrings);
			fbString("red", "svt sector " + _closestPanel.getSector(),
					feedbackStrings);
		}
		else {
			double rad = Math.hypot(worldPoint.x, worldPoint.y);
			boolean found = false;

			// cnd ?
			if ((rad > 288) && (rad < 382)) {

				for (int layer = 1; layer <= 3; layer++) {
					for (int paddleId = 1; paddleId <= 48; paddleId++) {

						found = cndPoly[layer - 1][paddleId - 1]
								.getFeedbackStrings(container, screenPoint,
										worldPoint, feedbackStrings);

						if (found) {
							break;
						}
					}

					if (found) {
						break;
					}

				}
			}
			
			//ctof
			else if ((rad > 250) && (rad < 260)) {
				
	
				for (int index = 0; index < 48; index++) {
					if (ctofPoly[index].contains(screenPoint)) {
						int paddle = index+1;
						TdcAdcHit hit = null;
			  		    TdcAdcHitList hits = CTOF.getInstance().getHits();
						if ((hits != null) && !hits.isEmpty()) {
							hit = hits.get(0, 0, paddle);
						}
						
						if (hit == null) {
							feedbackStrings.add("$dodger blue$" + "CTOF paddle " + paddle);
						}
						else {
							hit.tdcAdcFeedback("CTOF paddle", feedbackStrings);
						}

						break;
					}
					
					
				}
			}

		}

		// hits data

		if (_closestPanel != null) {
			AdcHitList hits = SVT.getInstance().getHits();
			if ((hits != null) && !hits.isEmpty()) {
				Vector<int[]> stripADCData = SVT.getInstance().allStripsForSectorAndLayer(_closestPanel.getSector(),
						_closestPanel.getLayer());
				for (int sdtdat[] : stripADCData) {
					fbString("orange", "strip  " + sdtdat[0] + " adc: " + +sdtdat[1], feedbackStrings);
				}
			}
		}

		

//		int hitCount = BST.hitCount();
//		if ((_closestPanel != null) && (hitCount > 0)) {
//			Vector<int[]> stripADCData = BST.allStripsForSectorAndLayer(
//					_closestPanel.getSector(), _closestPanel.getLayer());
//			if (!stripADCData.isEmpty()) {
//				for (int sdtdat[] : stripADCData) {
//					fbString("orange",
//							"strip:  " + sdtdat[0] + " adc: " + +sdtdat[1],
//							feedbackStrings);
//				}
//			}
//		}

		// near a swum trajectory?
		double mindist = _swimTrajectoryDrawer.closestApproach(worldPoint);
		double pixlen = WorldGraphicsUtilities.getMeanPixelDensity(container)
				* mindist;

		_lastTrajStr = null; // for hovering response
		if (pixlen < 25.0) {
			SwimTrajectory2D traj2D = _swimTrajectoryDrawer
					.getClosestTrajectory();
			if (traj2D != null) {
				traj2D.addToFeedback(feedbackStrings);
				_lastTrajStr = traj2D.summaryString();
			}
		}

		// see if any feedback from simulated data
		getGemcFeedback(container, screenPoint, worldPoint, feedbackStrings);

		// reconstructed feedback?
		_crossDrawer.feedback(container, screenPoint, worldPoint,
				feedbackStrings);
		
		//hit feedback
		_hitDrawer.feedback(container, screenPoint, worldPoint, feedbackStrings);

	}

	// get the panel closest to a given point
	private SVTxyPanel getClosest(Point2D.Double wp) {
		List<SVTxyPanel> panels = GeometryManager.getSVTxyPanels();
		if (panels == null) {
			return null;
		}

		SVTxyPanel closest = null;
		double minDistance = Double.MAX_VALUE;

		for (SVTxyPanel panel : panels) {
			double dist = panel.pointToLineDistance(wp);
			if (dist < minDistance) {
				closest = panel;
				minDistance = dist;
			}
		}

		if (minDistance > 6.) {
			closest = null;
		}

		return closest;
	}

	// feedback from simulated data
	private void getGemcFeedback(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, List<String> feedbackStrings) {

//		String cstr = "$orange$";
//
//		double x[] = BST.avgX();
//
//		int len = (x == null) ? 0 : x.length;
//		if (len == 0) {
//			feedbackStrings.add(cstr + "No GEMC hits");
//			return;
//		}
//
//		double y[] = BST.avgY();
//		int pid[] = BST.pid();
//		Point p1 = new Point();
//		Point2D.Double wp1 = new Point2D.Double();
//		Rectangle rr = new Rectangle();
//		for (int index = len - 1; index >= 0; index--) {
//
//			wp1.setLocation(x[index], y[index]);
//			container.worldToLocal(p1, wp1);
//			rr.setFrame(p1.x - 3, p1.y - 3, 6, 6);
//
//			if (rr.contains(screenPoint)) {
//				if (pid != null) {
//					LundId lid = LundSupport.getInstance()
//							.get(pid[index]);
//					feedbackStrings.add(cstr + "GEMC pid: " + lid.getName());
//
//					String hitXYstr = cstr + String.format(
//							"GEMC [x,y]: (%-6.2f, %-6.2f)", x[index], y[index]);
//					feedbackStrings.add(hitXYstr);
//				}
//				break;
//			}
//
//		} // end for loop

	}
	
	/**
	 * Get a CTOF scintillator polygon
	 * @param index1 the 1=based index [1..48]
	 * @return the most recently drawn polygon
	 */
	public CTOFXYPolygon getCTOFPolygon(int index1) {
		int index0 = index1-1;
		if ((index0 < 0) || (index0 > 47)) {
			return null;
		}
		return ctofPoly[index0];
	}

	/**
	 * Get world point from lab coordinates
	 * 
	 * @param x lab x in mm
	 * @param y lab y in mm
	 * @param z lab z in mm
	 * @param wp the world point
	 */
	public void getWorldFromLabXYZ(double x, double y, double z,
			Point2D.Double wp) {
		wp.x = x;
		wp.y = y;
	}
}
