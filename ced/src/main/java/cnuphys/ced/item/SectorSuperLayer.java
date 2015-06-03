package cnuphys.ced.item;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.ced.cedview.sectorview.SectorView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.data.ADataContainer;
import cnuphys.ced.event.data.DCDataContainer;
import cnuphys.ced.geometry.GeoConstants;
import cnuphys.ced.noise.NoiseManager;
import cnuphys.lund.LundId;
import cnuphys.lund.LundStyle;
import cnuphys.lund.LundSupport;
import cnuphys.snr.NoiseReductionParameters;
import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.item.PolygonItem;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.MathUtilities;
import cnuphys.bCNU.util.X11Colors;

/**
 * Used in SectorView views.
 * 
 * @author heddle
 *
 */
public class SectorSuperLayer extends PolygonItem {

    // pixel density thresholds. As we zoom in. the pixels/cm increases. At
    // certain thresholds, other drawing kicks in
    private static final double wireThreshold[] = { Double.NaN, 2.0, 2.0, 1.7,
	    1.7, 1.6, 1.6 };
    private static final double closeupThreshold[] = { Double.NaN, 16.0, 16.0,
	    12.0, 12.0, 7.0, 7.0 };

    // for gemc doca's
    private static final Color docaLine = Color.lightGray;
    private static final Color docaFill = new Color(255, 255, 255, 60);

    // for hits cells
    private static final Color defaultHitCellFill = Color.red;
    private static final Color defaultHitCellLine = X11Colors
	    .getX11Color("Dark Red");

    // used for making hexagons
    // private static final double COS30 = Math.sqrt(3.0)/2.0;
    // private static final double SIN30 = 0.5;
    private static final double TAN30 = 1.0 / Math.sqrt(3.0);

    // pale color used to fill in layers
    private static final Color _layerFillColors[] = {
	    X11Colors.getX11Color("cornsilk"), X11Colors.getX11Color("azure") };

    private static final Color senseWireColor = Color.cyan;
    private static final Color guardWireColor = Color.yellow;

    // convenient access to the event manager
    ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

    // convenient access to the noise manager
    NoiseManager _noiseManager = NoiseManager.getInstance();
    // sector 1-based 1..6
    private int _sector;

    // superlayer 1-based 1..6
    private int _superLayer;

    // cache the outline
    private Point2D.Double[] _cachedWorldPolygon;

    // cache the layer polygons. They must be recomputed if the item is dirty.
    private Polygon layerPolygons[] = new Polygon[GeoConstants.NUM_LAYER];

    // a set of wire positions with a border of guard wires. The first index is
    // the layer, it runs 0..7 with 0 layers 0 and 7 being guard wires, and 1-6
    // being the sense wires. The second index is the wire index. It runs 0-113
    // with wire 0 and 113 being guard wires and 1-112 being sense wires.
    private Point2D.Double _wires[][];

    // the view this item lives on.
    private SectorView _view;

    /**
     * Create a super layer item for the sector view. Note, no points are added
     * in the constructor. The points will always be supplied by the setPoints
     * method, which will send projected wire positions (with a border of guard
     * wires)
     * 
     * @param layer
     *            the Layer this item is on.
     * @param view
     *            the view this item lives on.
     * @param sector
     *            the 1-based sector [1..6]
     * @param superLayer
     *            the 1-based superlayer [1..6]
     */
    public SectorSuperLayer(LogicalLayer layer, SectorView view, int sector,
	    int superLayer) {
	super(layer);
	_view = view;
	_sector = sector;
	_superLayer = superLayer;
    }

    /**
     * Custom drawer for the item.
     * 
     * @param g
     *            the graphics context.
     * @param container
     *            the graphical container being rendered.
     */
    @Override
    public void drawItem(Graphics g, IContainer container) {

	if (_eventManager.isAccumulating()) {
	    return;
	}

	Graphics2D g2 = (Graphics2D) g;

	super.drawItem(g, container); // draws shell

	// are we really zoomed in?
	boolean reallyClose = (WorldGraphicsUtilities.getMeanPixelDensity(_view
		.getContainer()) > closeupThreshold[_superLayer]);

	// draw layer outlines to guide the eye
	for (int layer = 1; layer <= 6; layer++) {
	    Polygon poly = getLayerPolygon(container, layer);
	    g.setColor(_layerFillColors[layer % 2]);
	    g.fillPolygon(poly);
	}

	// draw results of noise reduction? If so will need the parameters
	// (which also have the results)
	NoiseReductionParameters parameters = _noiseManager.getParameters(
		_sector - 1, _superLayer - 1);

	// show the noise segment masks?
	if (_view.showMasks()) {
	    drawMasks(g, container, parameters);
	}

	// if really zoomed in, draw cell outlines thicker
	Stroke oldStroke = g2.getStroke();
	if (reallyClose) {
	    g2.setStroke(GraphicsUtilities.getStroke(1.5f, LineStyle.SOLID));
	}

	// draw the hits

	DCDataContainer dcData = _eventManager.getDCData();

	for (int i = 0; i < dcData.getHitCount(0); i++) {
	    try {
		int sect1 = dcData.dc_dgtz_sector[i]; // 1 based
		int supl1 = dcData.dc_dgtz_superlayer[i]; // 1 based

		if ((sect1 == _sector) && (supl1 == _superLayer)) {
		    int lay1 = dcData.dc_dgtz_layer[i]; // 1 based
		    int wire1 = dcData.dc_dgtz_wire[i]; // 1 based

		    boolean noise = false;
		    if (_noiseManager.getNoise() != null) {
			if (i < _noiseManager.getNoise().length) {
			    noise = _noiseManager.getNoise()[i];
			} else {
			    String ws = " hit index = " + i
				    + " noise length = "
				    + _noiseManager.getNoise().length;
			    Log.getInstance().warning(ws);
			}
		    }

		    int pid = (dcData.dc_true_pid == null) ? -1
			    : dcData.dc_true_pid[i];

		    double doca = dcData.get(dcData.dc_dgtz_doca, i);
		    drawGemcDCHit(g2, container, lay1, wire1, noise, pid, doca);
		}
	    } catch (NullPointerException e) {
		System.err
			.println("null pointer in SectorSuperLayer hit drawing");
		e.printStackTrace();
	    }
	} // for loop

	g2.setStroke(oldStroke);

	// draw outer boundary again.
	g.setColor(_style.getLineColor());
	g.drawPolygon(_lastDrawnPolygon);

	// draw wires?
	if (reallyClose
		|| (WorldGraphicsUtilities.getMeanPixelDensity(_view
			.getContainer()) > wireThreshold[_superLayer])) {
	    drawWires(g, container, reallyClose);
	}
    }

    /**
     * Draw a single dc hit
     * 
     * @param g
     *            the graphics context
     * @param container
     *            the rendering container
     * @param layer
     *            1-based layer
     * @param wire
     *            1-based wire
     * @param noise
     *            is noise hit
     * @param pid
     *            gemc id
     * @param doca
     *            the distance of closest approach in microns
     */
    private void drawGemcDCHit(Graphics g, IContainer container, int layer,
	    int wire, boolean noise, int pid, double doca) {

	// abort if hiding noise and this is noise
	if (_view.hideNoise() && noise) {
	    return;
	}

	// get the hexagon
	Polygon hexagon = getHexagon(container, layer, wire);

	// are we to show mc (MonteCarlo simulation) truth?
	boolean showTruth = _view.showMcTruth();

	Color hitFill = defaultHitCellFill;
	Color hitLine = defaultHitCellLine;

	// do we have simulated "truth" data?
	if (showTruth) {
	    LundId lid = LundSupport.getInstance().get(pid);
	    if (lid != null) {
		LundStyle style = lid.getStyle();
		if (style != null) {
		    hitFill = style.getFillColor();
		    hitLine = hitFill.darker();
		}
	    }
	} // end gemcData != null

	if ((_view.showNoiseAnalysis()) && noise) {
	    highlightNoiseHit(g, container, !showTruth, hexagon);
	} else {
	    g.setColor(hitFill);
	    g.fillPolygon(hexagon);
	    g.setColor(hitLine);
	    g.drawPolygon(hexagon);
	}

	// draw doca?
	if (showTruth
		&& (WorldGraphicsUtilities.getMeanPixelDensity(_view
			.getContainer()) > wireThreshold[_superLayer])) {
	    drawDOCA(g, container, layer, wire, doca);
	}

    }

    /**
     * Draw a distance of closest approach circle
     * 
     * @param g
     *            the graphics context
     * @param container
     *            the rendering container
     * @param layer
     *            the 1-based layer
     * @param wire
     *            the 1-based wire
     * @param doca2d
     *            the doca in mm
     */
    private void drawDOCA(Graphics g, IContainer container, int layer,
	    int wire, double doca2d) {

	if (Double.isNaN(doca2d) || (doca2d < 1.0e-6)) {
	    return;
	}

	// draw gemc doca
	// convert micron to cm
	double radius = doca2d / 10.0; // converted mm to cm

	if (radius > 5) {
	    String wmsg = "Very large doca radius: " + radius + " cm. Sect: "
		    + _sector + " supl: " + _superLayer + "lay: " + layer
		    + " wire: " + wire;

	    Log.getInstance().warning(wmsg);
	    System.err.println(wmsg);
	    return;
	}
	
	
	//center is the given wire projected locations
	
	Point2D.Double center = _wires[layer][wire];
	Point2D.Double doca[] = _view.getCenteredWorldCircle(center, radius);
	
	
	
//	Point2D.Double doca[] = _view.getCenteredWorldCircle(_sector - 1,
//		_superLayer - 1, layer, wire, radius);
	Polygon docaPoly = new Polygon();
	Point dp = new Point();
	for (int i = 0; i < doca.length; i++) {
	    container.worldToLocal(dp, doca[i]);
	    docaPoly.addPoint(dp.x, dp.y);
	}
	g.setColor(docaFill);
	g.fillPolygon(docaPoly);
	g.setColor(docaLine);
	g.drawPolygon(docaPoly);
    }

    /**
     * Highlight a noise hit
     * 
     * @param g
     *            the graphics context
     * @param container
     *            the rendering container
     * @param dchit
     *            the wire hit object
     * @param simple
     *            if <code>true</code> use simple highlighting
     */
    private void highlightNoiseHit(Graphics g, IContainer container,
	    boolean simple, Polygon hexagon) {

	if (hexagon == null) {
	    return;
	}

	if (simple) {
	    g.setColor(Color.black);
	    g.fillPolygon(hexagon);
	    g.setColor(Color.black);
	    g.drawPolygon(hexagon);
	} else {
	    g.setColor(Color.gray);
	    {
		int x[] = hexagon.xpoints;
		int y[] = hexagon.ypoints;
		g.drawLine(x[0], y[0], x[3], y[3]);
		g.drawLine(x[1], y[1], x[4], y[4]);
		g.drawLine(x[2], y[2], x[5], y[5]);
	    }

	    g.setColor(Color.black);
	    g.drawPolygon(hexagon);
	}
    }

    /**
     * Draw the wires.
     * 
     * @param g
     *            the graphics context
     * @param container
     *            the rendering container
     * @param reallyClose
     *            if <code>true</code> we are really close
     */
    private void drawWires(Graphics g, IContainer container, boolean reallyClose) {
	Point pp = new Point(); // workspace
	for (int layer = 0; layer <= 7; layer++) {
	    for (int wire = 0; wire < GeoConstants.NUM_WIRE + 2; wire++) {
		// note: no conversion to 0-based because the wire array has
		// a "border" of guard wires!

		if ((layer == 0) || (layer == 7) || (wire == 0)
			|| (wire > GeoConstants.NUM_WIRE)) {
		    g.setColor(guardWireColor);
		} else {
		    g.setColor(senseWireColor);
		}
		Point2D.Double wp = _wires[layer][wire];
		container.worldToLocal(pp, wp);
		if (reallyClose) {
		    g.fillRect(pp.x - 1, pp.y - 1, 2, 2);
		} else {
		    g.fillRect(pp.x, pp.y, 1, 1);
		}
	    }
	}
    }

    /**
     * Draw the masks showing the effect of the noise finding algorithm
     * 
     * @param g
     *            the graphics context
     * @param container
     *            the rendering container
     * @param parameters
     *            the noise algorithm parameters
     */
    private void drawMasks(Graphics g, IContainer container,
	    NoiseReductionParameters parameters) {
	for (int wire = 0; wire < parameters.getNumWire(); wire++) {
	    boolean leftSeg = parameters.getLeftSegments().checkBit(wire);
	    boolean rightSeg = parameters.getRightSegments().checkBit(wire);
	    if (leftSeg || rightSeg) {
		if (leftSeg) {
		    drawMask(g, container, wire,
			    parameters.getLeftLayerShifts(), 1);
		}
		if (rightSeg) {
		    drawMask(g, container, wire,
			    parameters.getRightLayerShifts(), -1);
		}

	    }
	}
    }

    /**
     * Draws the masking that shows where the noise algorithm thinks there are
     * segments. Anything not masked is noise.
     * 
     * @param g
     *            the graphics context.
     * @param container
     *            the rendering container
     * @param wire
     *            the ZERO BASED wire 0..
     * @param shifts
     *            the parameter shifts for this direction
     * @param sign
     *            the direction 1 for left -1 for right * @param wr essentially
     *            workspace
     */
    private void drawMask(Graphics g, IContainer container, int wire,
	    int shifts[], int sign) {

	wire++; // convert to 1-based

	if (sign == 1) {
	    g.setColor(NoiseManager.maskFillLeft);
	} else {
	    g.setColor(NoiseManager.maskFillRight);
	}

	for (int layer = 1; layer <= GeoConstants.NUM_LAYER; layer++) {

	    Polygon hexagon = getHexagon(container, layer, wire);
	    g.fillPolygon(hexagon);
	    g.drawPolygon(hexagon);

	    // ugh -- shifts are 0-based
	    for (int shift = 1; shift <= shifts[layer - 1]; shift++) {
		int tempWire = wire + sign * shift;
		if ((tempWire > 0) && (tempWire <= GeoConstants.NUM_WIRE)) {
		    hexagon = getHexagon(container, layer, tempWire);
		    g.fillPolygon(hexagon);
		    g.drawPolygon(hexagon);
		}
	    }
	}

    }

    /**
     * Obtain a crude outline of a sense wire layer
     * 
     * @param container
     *            the container being rendered
     * @param layer
     *            the layer in question--a sensewire layer [1..6]
     * @return a layer outline
     */
    private Polygon getLayerPolygon(IContainer container, int layer) {
	if ((layer < 1) || (layer > 6)) {
	    return null;
	}

	// if we are not dirty use the cached polygons
	if (!isDirty()) {
	    return layerPolygons[layer - 1];
	}

	// max index (last wire == guard wire)
	int n = GeoConstants.NUM_WIRE + 1;

	// the first and last wires (i.e., the guard wires) on the layer
	Point2D.Double wp0 = _wires[layer][0];
	Point2D.Double wp1 = _wires[layer][n];

	Point2D.Double wp[] = new Point2D.Double[7];

	wp[0] = new Point2D.Double();
	averagePoint(wp0, _wires[layer + 1][0], wp[0]);

	wp[1] = wp0;

	wp[2] = new Point2D.Double();
	averagePoint(wp0, _wires[layer - 1][0], wp[2]);

	wp[3] = new Point2D.Double();
	averagePoint(wp1, _wires[layer - 1][n], wp[3]);

	wp[4] = wp1;

	wp[5] = new Point2D.Double();
	averagePoint(wp1, _wires[layer + 1][n], wp[5]);

	wp[6] = wp[0];

	Polygon poly = new Polygon();
	Point pp = new Point();

	for (int i = 0; i < 7; i++) {
	    container.worldToLocal(pp, wp[i]);
	    poly.addPoint(pp.x, pp.y);
	}

	layerPolygons[layer - 1] = poly;
	return poly;
    }

    // simple average of toe world points
    private void averagePoint(Point2D.Double wp0, Point2D.Double wp1,
	    Point2D.Double wpavg) {
	wpavg.x = 0.5 * (wp0.x + wp1.x);
	wpavg.y = 0.5 * (wp0.y + wp1.y);
    }

    /**
     * Gets the layer from the world point. This only gives sensible results if
     * the world point has already passed the "inside" test.
     * 
     * @param wp
     *            the world point in question.
     * @return the layer containing the given world point. It returns [0..7]
     *         with 0 and 7 meaning the guard "layers"
     */
    private int getLayer(Point2D.Double wp) {
	Point2D.Double pintersect = new Point2D.Double();
	MathUtilities.perpendicularIntersection(_wires[0][0],
		_wires[0][GeoConstants.NUM_WIRE + 1], wp, pintersect);

	double d1 = wp.distance(pintersect);
	MathUtilities.perpendicularIntersection(_wires[7][0],
		_wires[7][GeoConstants.NUM_WIRE + 1], wp, pintersect);

	double d2 = wp.distance(pintersect);
	double fraction = d1 / (d1 + d2);
	int layer = (1 + (int) (14 * fraction)) / 2;
	return layer;
    }

    /**
     * Gets the wire from the world point. This only gives sensible results if
     * the world point has already passed the "inside" test and we used getLayer
     * on the same point to get the layer.
     * 
     * @param wp
     *            the world point in question.
     * @return the closest wire index on the given layer. It should return
     *         [0..GeoConstants.NUM_WIRE+1] (e.g., 0..113) with 0 and
     *         GeoConstants.NUM_WIRE+1 meaning a guard wire.
     */
    private int getWire(int layer, Point2D.Double wp) {
	if ((layer < 0) || (layer > 7)) {
	    return -1;
	}

	double fract = MathUtilities.perpendicularIntersection(
		_wires[layer][0], _wires[layer][GeoConstants.NUM_WIRE + 1], wp);
	int wire = (int) Math.round(fract * (GeoConstants.NUM_WIRE + 1));
	return wire;
    }

    /**
     * Add any appropriate feedback strings for the headsup display or feedback
     * panel.
     * 
     * @param container
     *            the Base container.
     * @param screenPoint
     *            the mouse location.
     * @param worldPoint
     *            the corresponding world point.
     * @param feedbackStrings
     *            the List of feedback strings to add to.
     */
    @Override
    public void getFeedbackStrings(IContainer container, Point screenPoint,
	    Point2D.Double worldPoint, List<String> feedbackStrings) {
	if (contains(container, screenPoint)) {

	    NoiseReductionParameters parameters = _noiseManager.getParameters(
		    _sector - 1, _superLayer - 1);

	    feedbackStrings.add(ADataContainer.prelimColor
		    + "Raw Occupancy "
		    + DoubleFormat.doubleFormat(
			    100.0 * parameters.getRawOccupancy(), 2) + "%");
	    feedbackStrings.add(ADataContainer.prelimColor
		    + "Reduced Occupancy "
		    + DoubleFormat.doubleFormat(
			    100.0 * parameters.getNoiseReducedOccupancy(), 2)
		    + "%");

	    int layer = getLayer(worldPoint);

	    DCDataContainer dcData = _eventManager.getDCData();

	    if ((layer > 0) && (layer < 7)) {
		int wire = getWire(layer, worldPoint);
		if ((wire > 0) && (wire <= GeoConstants.NUM_WIRE)) {

		    int hitIndex = dcData.getHitIndex(_sector, _superLayer,
			    layer, wire);
		    if (hitIndex < 0) {
			feedbackStrings.add("superlayer " + _superLayer
				+ "  layer " + layer + "  wire " + wire);
		    } else {
			dcData.onHitFeedbackStrings(hitIndex, 0,
				dcData.dc_true_pid, dcData.dc_true_mpid,
				dcData.dc_true_tid, dcData.dc_true_mtid,
				dcData.dc_true_otid, feedbackStrings);
		    }

		} // good wire
	    } // good layer

	    dcData.generalFeedbackStrings(0, feedbackStrings);
	}
    }

    /**
     * Set the wires to a new set of points
     * 
     * @param wires
     *            the new wire projections.
     */
    public void setPoints(Point2D.Double wires[][]) {
	_wires = wires;
	setDirty(true);
	setPath(getWorldPolygon());
    }

    // get the world polygon corresponding to the boundary of the superlayer
    private Point2D.Double[] getWorldPolygon() {
	if (_wires == null) {
	    _cachedWorldPolygon = null;
	    return null;
	}

	if (_dirty) {
	    // compute the total number of points

	    int n0 = GeoConstants.NUM_WIRE + 2;
	    int n1 = 6;
	    int n = 2 * (n0 + n1);
	    _cachedWorldPolygon = new Point2D.Double[n];

	    int index = 0;

	    // start with layer 0
	    int layer = 0;
	    for (int wire = 0; wire < n0; wire++) {
		_cachedWorldPolygon[index++] = _wires[layer][wire];
	    }

	    // last wire from each layer
	    for (layer = 1; layer <= 6; layer++) {
		_cachedWorldPolygon[index++] = _wires[layer][GeoConstants.NUM_WIRE + 1];
	    }

	    // layer 7 another guard layer
	    layer = 7;
	    for (int wire = (n0 - 1); wire >= 0; wire--) {
		_cachedWorldPolygon[index++] = _wires[layer][wire];
	    }

	    // first wire from each layer
	    for (layer = 6; layer >= 1; layer--) {
		_cachedWorldPolygon[index++] = _wires[layer][0];
	    }

	} // end dirty
	return _cachedWorldPolygon;
    }

    /**
     * Gets the cell hexagon as a screen polygon.
     * 
     * @param container
     *            the container being rendered.
     * @param layer
     *            the 1-based layer 1--6
     * @param wire
     *            the one based wire 1..GeoConstants.NUM_WIRE
     * @return the cell hexagon
     */
    public Polygon getHexagon(IContainer container, int layer, int wire) {

	if ((layer < 1) || (layer > 6)) {
	    return null;
	}

	if ((wire < 1) || (wire > GeoConstants.NUM_WIRE)) {
	    return null;
	}

	// wire in question
	Point2D.Double wirePoint = _wires[layer][wire];

	Point2D.Double neighbors[] = new Point2D.Double[7];

	// staggering makes finding neighbors layer dependent
	if ((layer % 2) == 0) { // layers 2,4, 6
	    neighbors[0] = _wires[layer + 1][wire];
	    neighbors[1] = _wires[layer + 1][wire + 1];
	    neighbors[2] = _wires[layer][wire + 1];
	    neighbors[3] = _wires[layer - 1][wire + 1];
	    neighbors[4] = _wires[layer - 1][wire];
	    neighbors[5] = _wires[layer][wire - 1];
	} else { // layers 1, 3, 5
	    neighbors[0] = _wires[layer + 1][wire - 1];
	    neighbors[1] = _wires[layer + 1][wire];
	    neighbors[2] = _wires[layer][wire + 1];
	    neighbors[3] = _wires[layer - 1][wire];
	    neighbors[4] = _wires[layer - 1][wire - 1];
	    neighbors[5] = _wires[layer][wire - 1];
	}
	neighbors[6] = neighbors[0]; // close it tight

	Polygon poly = new Polygon();
	Point2D.Double tp = new Point2D.Double();
	Point pp = new Point();
	for (int i = 0; i < 7; i++) {
	    averagePoint(wirePoint, neighbors[i], tp);
	    fastRotate30(tp, wirePoint);
	    container.worldToLocal(pp, tp);
	    poly.addPoint(pp.x, pp.y);
	}

	return poly;
    }

    /**
     * A fast rotation by 30 degrees for use by the hexagon finder
     * 
     * @param wp
     *            the point being rotated
     * @param anchor
     *            the point being rotated about
     */
    private void fastRotate30(Point2D.Double wp, Point2D.Double anchor) {
	// scaling is necessary to enlarge hex from one created using half-way
	// points
	// double x = (wp.x - anchor.x)/COS30;
	// double y = (wp.y - anchor.y)/COS30;
	// wp.x = anchor.x + (x*COS30 - y*SIN30);
	// wp.y = anchor.y + (x*SIN30 + y*COS30);

	// use of the tangent combines rotation and the required scaling
	double x = wp.x - anchor.x;
	double y = wp.y - anchor.y;
	wp.x = anchor.x + (x - y * TAN30);
	wp.y = anchor.y + (x * TAN30 + y);

    }
}
