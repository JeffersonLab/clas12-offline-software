package cnuphys.ced.cedview.bst;

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
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Vector;

import cnuphys.bCNU.attributes.AttributeType;
import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.toolbar.BaseToolBar;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.Histo2DData;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.CedXYView;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayArray;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.data.BSTDataContainer;
import cnuphys.ced.geometry.BSTGeometry;
import cnuphys.ced.geometry.BSTxyPanel;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.swim.SwimTrajectory2D;

@SuppressWarnings("serial")
public class BSTxyView extends CedXYView {

    // Comments on the Geometry of the BST from Veronique
    // ------------------------------------
    // The BST geometry consists of 3 (or 4) superlayers of modules.
    // Each superlayer contains two layers of modules, labeled A and B.
    // Layer B corresponds to the top layer as seen from the outside of the
    // detector,
    // and Layer A to the layer underneath Layer B looking from the outside.
    // Each module contains 3 sensors (hybrid, intermediate, far).
    // The hybrid, intermediate and far sensors are aligned in the direction of
    // the beam
    // corresponding to the positive z-axis in the laboratory frame.
    // The coordinate system in the lab frame (center of the target) is a right
    // handed system, with
    // the z unit vector in the direction of the beam, and the y unit vector
    // pointing up;
    // the x unit vector points therefore to the left when looking in the
    // direction of the beam.
    // The numbering convention for the sectors is as follows:
    // sector 1 modules oriented at 90 deg (80 deg) with respect to the y-axis
    // for superlayers 1,2,4 (3);
    // sector numbers increase in the clockwise direction (viewed in the
    // direction of the beam).
    // The strips in the hybrid sensor of Layer B are connected to the pitch
    // adapter and
    // and implanted with 156 micron pitch. There are 256 strips oriented at
    // graded angle
    // from 0 to +3 deg with respect to the bottom edge of layer B which
    // corresponds to the z-direction.
    // Strip number 1 in Layer B is parallel to the bottom of the sensor.


    private BSTxyPanel _closestPanel;

    private static Color _panelColors[] = { Color.black, Color.darkGray };
    
    //the CND xy polygons
    CNDXYPolygon cndPoly[][] = new CNDXYPolygon[3][48];

    // units are mm
//    private static Rectangle2D.Double _defaultWorldRectangle = new Rectangle2D.Double(
//	    200., -200., -400., 400.);

    private static Rectangle2D.Double _defaultWorldRectangle = new Rectangle2D.Double(
	    400., -400., -800., 800.);
    
    // used to draw swum trajectories (if any) in the after drawer
    private SwimTrajectoryDrawer _swimTrajectoryDrawer;

    // draws reconstructed crosses
    private CrossDrawerXY _crossDrawer;

    /**
     * Create a BST View
     * 
     * @param keyVals
     */
    public BSTxyView(Object... keyVals) {
	super(keyVals);

	_crossDrawer = new CrossDrawerXY(this);

	// draws any swum trajectories (in the after draw)
	_swimTrajectoryDrawer = new SwimTrajectoryDrawer(this);

	// default properties
	setBooleanProperty(DisplayArray.HITCROSS_PROPERTY, true);

	//add the CND polys
	for (int layer = 1; layer <= 3; layer++) {
	    for (int paddleId = 1; paddleId <= 48; paddleId++) {
		cndPoly[layer-1][paddleId-1] = new CNDXYPolygon(layer, paddleId);
	    }
	}

    }

    /**
     * See if we are to display the midpoints or the crosses from the actual
     * dgtz hits
     * 
     * @return <code>true</code> if we are to display the crosses
     */
    public boolean displayDgtzCrosses() {
	return checkBooleanProperty(DisplayArray.HITCROSS_PROPERTY);
    }

    /**
     * Create a BstXY view
     * 
     * @return a BSTXy View
     */
    public static BSTxyView createBSTxyView() {
	BSTxyView view = null;

	// set to a fraction of screen
	Dimension d = GraphicsUtilities.screenFraction(0.35);

	// make it square
	int width = d.width;
	int height = width;

	// create the view
	view = new BSTxyView(AttributeType.WORLDSYSTEM, _defaultWorldRectangle,
		AttributeType.WIDTH, width, AttributeType.HEIGHT, height,
		AttributeType.LEFTMARGIN, LMARGIN, AttributeType.TOPMARGIN,
		TMARGIN, AttributeType.RIGHTMARGIN, RMARGIN,
		AttributeType.BOTTOMMARGIN, BMARGIN, AttributeType.TOOLBAR,
		true, AttributeType.TOOLBARBITS, BaseToolBar.NODRAWING
			& ~BaseToolBar.RANGEBUTTON & ~BaseToolBar.TEXTFIELD
			& ~BaseToolBar.CONTROLPANELBUTTON
			& ~BaseToolBar.TEXTBUTTON & ~BaseToolBar.DELETEBUTTON,
		AttributeType.VISIBLE, true, AttributeType.HEADSUP, false,
		AttributeType.TITLE, "Central XY",
		AttributeType.STANDARDVIEWDECORATIONS, true);

	view._controlPanel = new ControlPanel(view, ControlPanel.DISPLAYARRAY
		+ ControlPanel.FEEDBACK + ControlPanel.ACCUMULATIONLEGEND
		+ ControlPanel.RECONSARRAY, DisplayBits.ACCUMULATION
		+ DisplayBits.BSTRECONS_CROSSES + DisplayBits.BSTHITS
		+ DisplayBits.MCTRUTH + DisplayBits.COSMICS, 2, 6);

	view.add(view._controlPanel, BorderLayout.EAST);
	view.pack();
	
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
		    drawBSTHits(g, container);

		    if (showReconsBSTCrosses()) {
			_crossDrawer.draw(g, container);
		    }

		    if (showCosmics()) {
			drawCosmicTracks(g, container);
		    }

		    _swimTrajectoryDrawer.draw(g, container);
		    Rectangle screenRect = getActiveScreenRectangle(container);
		    drawAxes(g, container, screenRect, true);
		}

	    }

	};
	getContainer().setAfterDraw(afterDraw);
    }

    //draw cosmic ray tracks
    private void drawCosmicTracks(Graphics g, IContainer container) {

	BSTDataContainer bstData = _eventManager.getBSTData();
	if (bstData == null) {
	    return;
	}

	Shape oldClip = clipView(g);

	int ids[] = bstData.bstrec_cosmics_ID;
	if (ids != null) {
	    double yx_interc[] = bstData.bstrec_cosmics_trkline_yx_interc;
	    double yx_slope[] = bstData.bstrec_cosmics_trkline_yx_slope;

	    g.setColor(Color.red);
	    Point p1 = new Point();
	    Point p2 = new Point();

	    for (int i = 0; i < ids.length; i++) {
		double y1 = 1000;
		double y2 = -1000;
		double x1 = yx_slope[i] * y1 + yx_interc[i];
		double x2 = yx_slope[i] * y2 + yx_interc[i];
		container.worldToLocal(p1, x1, y1);
		container.worldToLocal(p2, x2, y2);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
	    }
	}

	g.setClip(oldClip);
    }

    // find the panel given the 1-based layer and sector
    private BSTxyPanel getPanel(int layer, int sector) {
	List<BSTxyPanel> panels = GeometryManager.getBSTxyPanels();
	if (panels == null) {
	    return null;
	}
	for (BSTxyPanel panel : panels) {
	    if ((panel.getLayer() == layer) && (panel.getSector() == sector)) {
		return panel;
	    }
	}

	return null;
    }

    // draw the panels
    private void drawPanels(Graphics g, IContainer container) {

	Shape oldClip = g.getClip();

	List<BSTxyPanel> panels = GeometryManager.getBSTxyPanels();
	if (panels == null) {
	    return;
	}

	Graphics2D g2 = (Graphics2D) g;
	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);

	Rectangle sr = container.getInsetRectangle();
	g2.clipRect(sr.x, sr.y, sr.width, sr.height);

	//SVT panels
	for (BSTxyPanel panel : panels) {
	    drawSVTPanel(g2, container, panel,
		    _panelColors[(panel.getSector()) % 2]);
	}
	
	//CND Polys
	for (int layer = 1; layer <= 3; layer++) {
	    for (int paddleId = 1; paddleId <= 48; paddleId++) {
		cndPoly[layer-1][paddleId-1].draw(g2, container);
	    }
	    
	}

	g.setClip(oldClip);
    }

    // draw one SVT panel
    private void drawSVTPanel(Graphics2D g2, IContainer container,
	    BSTxyPanel panel, Color color) {
	Stroke oldStroke = g2.getStroke();
	g2.setColor(color);
	Point p1 = new Point();
	Point p2 = new Point();
	Point2D.Double wp1 = new Point2D.Double();
	Point2D.Double wp2 = new Point2D.Double();
	g2.setStroke((panel == _closestPanel) ? stroke2 : stroke);
	// Just draw a line from (x1,y1) to (x2,y2)

	// TODO resolve -x hack with gagik

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
	    String s = "" + panel.getSector();
	    extendLine(porig, pmid, 4 + panel.getLayer()/2, fm.stringWidth(s), fm.getHeight());
	    g2.drawString(s, pmid.x, pmid.y);
	}

    }

    private static void extendLine(Point p0, Point p1, int del, int sw, int fh) {
	double dx = p1.x - p0.x;
	double dy = p1.y - p0.y;
	double theta = Math.atan2(dy, dx);

	int quad = ((int) ((180 + Math.toDegrees(theta)) / 90)) % 4;

	int delx = (int) (del * Math.cos(theta));
	int dely = (int) (del * Math.sin(theta));

	if (quad == 0) {
	    delx = delx - sw;
	    // dely = dely + fh/2;
	} else if (quad == 2) {
	    delx = delx + 2;
	    dely = dely + fh / 2;
	} else if (quad == 3) {
	    delx = delx - sw;
	    dely = dely + fh / 2;
	}

	p1.x += delx;
	p1.y += dely;
    }

    // draw gemc simulated hits
    private void drawBSTHits(Graphics g, IContainer container) {
	if (getMode() == CedView.Mode.SINGLE_EVENT) {
	    drawBSTHitsSingleMode(g, container);
	} else {
	    drawGEMCHitsAccumulatedMode(g, container);
	}
    }

    // draw gemc simulated hits accumulated mode
    private void drawGEMCHitsAccumulatedMode(Graphics g, IContainer container) {
	Histo2DData bstXYData = AccumulationManager.getInstance()
		.getBSTXYGemcAccumulatedData();
	if (bstXYData != null) {
	    // System.err.println("Good count: " + bstXYData.getGoodCount());
	    // System.err.println("Bad count: " +
	    // bstXYData.getOutOfRangeCount());
	    // System.err.println("Max count: " + bstXYData.getMaxZ());

	    long counts[][] = bstXYData.getCounts();
	    if (counts != null) {
		Rectangle2D.Double wr = new Rectangle2D.Double();
		Rectangle r = new Rectangle();

		double maxBinCount = bstXYData.getMaxZ();

		for (int i = 0; i < bstXYData.getNumberBinsX(); i++) {
		    double x1 = bstXYData.getBinMinX(i);
		    double x2 = bstXYData.getBinMaxX(i);

		    for (int j = 0; j < bstXYData.getNumberBinsY(); j++) {
			if (counts[i][j] > 0) {
			    double y1 = bstXYData.getBinMinY(j);
			    double y2 = bstXYData.getBinMaxY(j);

			    wr.setFrame(x1, y1, x2 - x1, y2 - y1);

			    container.worldToLocal(r, wr);

			    double fract = ((counts[i][j])) / maxBinCount;
			    Color color = AccumulationManager
				    .getColorScaleModel().getColor(fract);

			    g.setColor(color);
			    g.fillRect(r.x, r.y, r.width, r.height);
			}

		    }
		}
	    } // counts != null

	}
    }

    // draw gemc simulated hits single event mode
    private void drawBSTHitsSingleMode(Graphics g, IContainer container) {

	BSTDataContainer bstData = _eventManager.getBSTData();

	int hitCount = bstData.getHitCount(0);
	if (hitCount > 0) {

	    Shape oldClip = g.getClip();
	    Graphics2D g2 = (Graphics2D) g;
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		    RenderingHints.VALUE_ANTIALIAS_ON);

	    // panels
	    for (int i = 0; i < bstData.getHitCount(0); i++) {
		BSTxyPanel panel = getPanel(bstData.bst_dgtz_layer[i],
			bstData.bst_dgtz_sector[i]);
		if (panel != null) {
		    drawSVTPanel(g2, container, panel, Color.red);
		}
	    } // for on hits

	    // draw the actual hits

	    if (displayDgtzCrosses()) {

		// System.err.println("DISPLAY CROSSES");

		for (int superlayer = 0; superlayer < 4; superlayer++) {
		    for (int sector = 0; sector < BSTGeometry.sectorsPerSuperlayer[superlayer]; sector++) {
			for (int layer = 0; layer < 2; layer++) {
			    // get all the strips for this triplet
			    Vector<Integer> vstrips = strips(sector,
				    superlayer, layer);
			    if (vstrips != null) {

			    }
			}
		    }
		}
	    } else {
		// System.err.println("DISPLAY MIDPOINTS");
		Point pp = new Point();
		for (int hitIndex = 0; hitIndex < hitCount; hitIndex++) {
		    // covert all to zero based indices
		    int sector = bstData.bst_dgtz_sector[hitIndex] - 1;
		    int complayer = bstData.bst_dgtz_layer[hitIndex];
		    int superlayer = (complayer - 1) / 2;
		    int layer = (complayer - 1) % 2;
		    int strip = bstData.bst_dgtz_strip[hitIndex] - 1;

		    if ((strip > 255) || (strip < 0)) {
			Log.getInstance().warning(
				"In BST dgtz data, bad strip Id:"
					+ bstData.bst_dgtz_strip[hitIndex]);
		    } else {
			// System.err.println("Drawing strip midpoint ");
			Point2D.Double wp = BSTGeometry.getStripMidpoint(
				sector, superlayer, layer, strip);

			container.worldToLocal(pp, 10 * wp.x, 10 * wp.y);

			drawCross(g, pp.x, pp.y + 1,
				X11Colors.getX11Color("Aquamarine"));
			drawCross(g, pp.x, pp.y,
				X11Colors.getX11Color("Dark Green"));
		    }

		    // System.out.println("sect " + sector + " supl " +
		    // superlayer + " lay " + layer + " strip " + strip);
		}
	    }

	    // draw GEMC nearest x and y

	    if ((bstData.bst_true_avgX != null) && showMcTruth()) {

		Rectangle sr = container.getInsetRectangle();
		g2.clipRect(sr.x, sr.y, sr.width, sr.height);

		Point p1 = new Point();
		Point2D.Double wp1 = new Point2D.Double();
		Color default_fc = Color.red;

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(stroke);

		for (int i = 0; i < bstData.bst_true_avgX.length; i++) {
		    Color fc = default_fc;
		    if (bstData.bst_true_pid != null) {
			LundId lid = LundSupport.getInstance().get(
				bstData.bst_true_pid[i]);
			if (lid != null) {
			    fc = lid.getStyle().getFillColor();
			}
		    }
		    g2.setColor(fc);

		    wp1.setLocation(bstData.bst_true_avgX[i],
			    bstData.bst_true_avgY[i]);
		    container.worldToLocal(p1, wp1);

		    // draw an x
		    g2.drawLine(p1.x - 3, p1.y - 3, p1.x + 3, p1.y + 3);
		    g2.drawLine(p1.x + 3, p1.y - 3, p1.x - 3, p1.y + 3);
		}
		g2.setStroke(oldStroke);
	    }

	    g.setClip(oldClip);
	} // hotcount > 0

    }

    // get all the strips that match the input triplet
    private Vector<Integer> strips(int sector, int superlayer, int layer) {

	BSTDataContainer bstData = _eventManager.getBSTData();
	int hitCount = bstData.getHitCount(0);
	if (hitCount < 1) {
	    return null;
	}

	Vector<Integer> v = null;
	int sect = sector + 1; // 1-based
	int compositeLayer = 2 * superlayer + layer + 1; // composite layer

	for (int hitIndex = 0; hitIndex < hitCount; hitIndex++) {
	    if (bstData.bst_dgtz_sector[hitIndex] == sect) {
		if (bstData.bst_dgtz_layer[hitIndex] == compositeLayer) {
		    if (v == null) {
			v = new Vector<Integer>();
		    }
		    v.add(bstData.bst_dgtz_strip[hitIndex]);
		}
	    }
	}

	return v;
    }

    // draw a cross
    private void drawCross(Graphics g, int x, int y, Color color) {
	int len = 5;

	g.setColor(TRANS2);
	g.fillOval(x - len, y - len, 2 * len, 2 * len);
	g.setColor(color);
	g.drawLine(x - len, y, x + len, y);
	g.drawLine(x, y - len, x, y + len);
    }
    
    /**
     * This adds the detector items. The AllDC view is not faithful to geometry.
     * All we really uses in the number of superlayers, number of layers, and
     * number of wires.
     */
    @Override
    protected void addItems() {
    }

    /**
     * Some view specific feedback. Should always call super.getFeedbackStrings
     * first.
     * 
     * @param container
     *            the base container for the view.
     * @param screenPoint
     *            the pixel point
     * @param worldPoint
     *            the corresponding world location.
     */
    @Override
    public void getFeedbackStrings(IContainer container, Point screenPoint,
	    Point2D.Double worldPoint, List<String> feedbackStrings) {

	basicFeedback(container, screenPoint, worldPoint,  "mm", feedbackStrings);
	
	if (!Environment.getInstance().isDragging()) {
	    BSTxyPanel newClosest = getClosest(worldPoint);
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
	   
	}

	// hits data
	BSTDataContainer bstData = _eventManager.getBSTData();

	int hitCount = bstData.getHitCount(0);
	if ((_closestPanel != null) && (hitCount > 0)) {
	    Vector<int[]> stripADCData = bstData.allStripsForSectorAndLayer(
		    _closestPanel.getSector(), _closestPanel.getLayer());
	    if (!stripADCData.isEmpty()) {
		for (int sdtdat[] : stripADCData) {
		    fbString("orange", "strip:  " + sdtdat[0] + " adc: "
			    + +sdtdat[1], feedbackStrings);
		}
	    }
	}

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

    }

    // get the panel closest to a given point
    private BSTxyPanel getClosest(Point2D.Double wp) {
	List<BSTxyPanel> panels = GeometryManager.getBSTxyPanels();
	if (panels == null) {
	    return null;
	}

	BSTxyPanel closest = null;
	double minDistance = Double.MAX_VALUE;

	for (BSTxyPanel panel : panels) {
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

	BSTDataContainer bstData = _eventManager.getBSTData();
	String cstr = "$orange$";

	double x[] = bstData.bst_true_avgX;

	int len = (x == null) ? 0 : x.length;
	if (len == 0) {
	    feedbackStrings.add(cstr + "No GEMC hits");
	    return;
	} else {
	    feedbackStrings.add(cstr + "GEMC hit count: " + len);
	}

	double y[] = bstData.bst_true_avgY;
	Point p1 = new Point();
	Point2D.Double wp1 = new Point2D.Double();
	Rectangle rr = new Rectangle();
	for (int index = len - 1; index >= 0; index--) {

	    wp1.setLocation(x[index], y[index]);
	    container.worldToLocal(p1, wp1);
	    rr.setFrame(p1.x - 3, p1.y - 3, 6, 6);

	    if (rr.contains(screenPoint)) {
		if (bstData.bst_true_pid != null) {
		    LundId lid = LundSupport.getInstance().get(
			    bstData.bst_true_pid[index]);
		    feedbackStrings.add(cstr + "GEMC pid: " + lid.getName());

		    String hitXYstr = cstr
			    + String.format("GEMC [x,y]: (%-6.2f, %-6.2f)",
				    x[index], y[index]);
		    feedbackStrings.add(hitXYstr);
		}
		break;
	    }

	} // end for loop

    }

    /**
     * Get world point from lab coordinates
     * 
     * @param x
     *            lab x in mm
     * @param y
     *            lab y in mm
     * @param z
     *            lab z in mm
     * @param wp
     *            the world point
     */
    public void getWorldFromLabXYZ(double x, double y, double z,
	    Point2D.Double wp) {
	wp.x = x;
	wp.y = y;
    }
}
