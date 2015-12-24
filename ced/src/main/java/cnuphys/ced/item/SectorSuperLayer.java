package cnuphys.ced.item;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.List;

import org.jlab.geom.prim.Line3D;

import cnuphys.ced.cedview.sectorview.SectorView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.data.ADataContainer;
import cnuphys.ced.event.data.DCDataContainer;
import cnuphys.ced.geometry.DCGeometry;
import cnuphys.ced.geometry.GeoConstants;
import cnuphys.ced.geometry.GeometryManager;
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
	
	private static final Color hexColor = new Color(223, 239, 239);

	// pale color used to fill in layers
	private static final Color _layerFillColors[] = {
			X11Colors.getX11Color("cornsilk"), X11Colors.getX11Color("azure") };

	// color for wires
	private static final Color senseWireColor = X11Colors
			.getX11Color("Dodger Blue");

	// convenient access to the event manager
	ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// convenient access to the noise manager
	NoiseManager _noiseManager = NoiseManager.getInstance();
	// sector 1-based 1..6
	private int _sector;

	// superlayer 1-based 1..6
	private int _superLayer;

	// cache the outline
	private Point2D.Double[] _cachedWorldPolygon = GeometryManager.allocate(4);

	// cache the layer polygons. They must be recomputed if the item is dirty.
	private Polygon _layerPolygons[] = new Polygon[6];

	// the view this item lives on.
	private SectorView _view;

	/**
	 * Create a super layer item for the sector view. Note, no points are added
	 * in the constructor. The points will always be supplied by the setPoints
	 * method, which will send projected wire positions (with a border of guard
	 * wires)
	 * 
	 * @param layer the Layer this item is on.
	 * @param view the view this item lives on.
	 * @param sector the 1-based sector [1..6]
	 * @param superLayer the 1-based superlayer [1..6]
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
	 * @param g the graphics context.
	 * @param container the graphical container being rendered.
	 */
	@Override
	public void drawItem(Graphics g, IContainer container) {

		if (_eventManager.isAccumulating()) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;

		getStyle().setFillColor(Color.white);
		super.drawItem(g, container); // draws shell

		// are we really zoomed in?
		boolean reallyClose = (WorldGraphicsUtilities.getMeanPixelDensity(
				_view.getContainer()) > closeupThreshold[_superLayer]);

		// draw layer outlines to guide the eye

		Shape clip = g2.getClip();
		g2.setClip(_lastDrawnPolygon);
		for (int layer = 1; layer <= 6; layer++) {
			Polygon poly = getLayerPolygon(container, layer);
			g.setColor(_layerFillColors[layer % 2]);
			g.fillPolygon(poly);
			g.drawPolygon(poly);
		}

		// draw results of noise reduction? If so will need the parameters
		// (which also have the results)
		NoiseReductionParameters parameters = _noiseManager
				.getParameters(_sector - 1, _superLayer - 1);

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
		drawHits(g, container);

		g2.setStroke(oldStroke);

		// draw wires?
		if (reallyClose || (WorldGraphicsUtilities.getMeanPixelDensity(
				_view.getContainer()) > wireThreshold[_superLayer])) {
			drawWires(g, container, reallyClose);
		}

		// draw outer boundary again.
		g.setColor(_style.getLineColor());
		g.drawPolygon(_lastDrawnPolygon);

		g2.setClip(clip);

	}

	
	private void drawHits(Graphics g, IContainer container) {
		
		if (_view.isSingleEventMode()) {
			drawSingleModeHits(g, container);
		}
		else {
			drawAccumulatedHits(g, container);
		}
	}
	
	private void drawSingleModeHits(Graphics g, IContainer container) {

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
						}
						else {
							String ws = " hit index = " + i + " noise length = "
									+ _noiseManager.getNoise().length;
							Log.getInstance().warning(ws);
						}
					}

					int pid = (dcData.dc_true_pid == null) ? -1
							: dcData.dc_true_pid[i];

					double doca = dcData.get(dcData.dc_dgtz_doca, i);
					drawGemcDCHit((Graphics2D)g, container, lay1, wire1, noise, pid, doca);
				}
			} catch (NullPointerException e) {
				System.err.println(
						"null pointer in SectorSuperLayer hit drawing");
				e.printStackTrace();
			}
		} // for loop

	}
	
	private void drawAccumulatedHits(Graphics g, IContainer container) {
		
		int dcAccumulatedData[][][][] = AccumulationManager.getInstance()
				.getAccumulatedDgtzDcData();
		int maxHit = AccumulationManager.getInstance().getMaxDgtzDcCount();
		if (maxHit < 1) {
			return;
		}
		
		int sect0 = _sector-1;
		int supl0 = _superLayer-1;
		
		for (int lay0 = 0; lay0 < 6; lay0++) {
			for (int wire0 = 0; wire0 < 112; wire0++) {
				
				int hit = dcAccumulatedData[sect0][supl0][lay0][wire0];
				double fract;
				if (_view.isSimpleAccumulatedMode()) {
					fract = ((double) hit) / maxHit;
				}
				else {
					fract = Math.log((double)(hit+1.))/Math.log(maxHit+1.);
				}

				Color color = AccumulationManager.getInstance()
						.getColor(fract);
				
				g.setColor(color);
				Polygon hexagon = getHexagon(container, lay0+1, wire0+1);
				if (hexagon != null) {
					g.fillPolygon(hexagon);
					g.drawPolygon(hexagon);
				}

			}
		}

	}
	
	
	/**
	 * Draw a single dc hit
	 * 
	 * @param g the graphics context
	 * @param container the rendering container
	 * @param layer 1-based layer
	 * @param wire 1-based wire
	 * @param noise is noise hit
	 * @param pid gemc id
	 * @param doca the distance of closest approach in mm
	 */
	private void drawGemcDCHit(Graphics g, IContainer container, int layer,
			int wire, boolean noise, int pid, double doca) {

		// abort if hiding noise and this is noise
		if (_view.hideNoise() && noise) {
			return;
		}

		// get the hexagon
		Polygon hexagon = getHexagon(container, layer, wire);
		if (hexagon == null) {
			return;
		}

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
		}
		else {
			g.setColor(hitFill);
			g.fillPolygon(hexagon);
			g.setColor(hitLine);
			g.drawPolygon(hexagon);
		}

		// draw doca?
		if (showTruth && (WorldGraphicsUtilities.getMeanPixelDensity(
				_view.getContainer()) > wireThreshold[_superLayer])) {
			if (doca > 1.0e-6) {
				drawDOCA(g, container, layer, wire, doca);
			}
		}

	}

	/**
	 * Draw a distance of closest approach circle
	 * 
	 * @param g the graphics context
	 * @param container the rendering container
	 * @param layer the 1-based layer
	 * @param wire the 1-based wire
	 * @param doca2d the doca in mm
	 */
	private void drawDOCA(Graphics g, IContainer container, int layer, int wire,
			double doca2d) {

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

		// center is the given wire projected locations

		Point2D.Double center = wire(layer, wire);
		Point2D.Double doca[] = _view.getCenteredWorldCircle(center, radius);

		if (doca == null) {
			return;
		}

		// Point2D.Double doca[] = _view.getCenteredWorldCircle(_sector - 1,
		// _superLayer - 1, layer, wire, radius);
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
	 * Get the projected wire location
	 * 
	 * @param layer the 1-based layer 1..6
	 * @param wire the 1-based wire 1..112
	 * @return the point, which might have NaNs
	 */
	public Point2D.Double wire(int layer, int wire) {
		// the indices to all DCGeometry calls are 1-based

		Point2D.Double wp = null;
		try {
			wp = DCGeometry.getCenter(_superLayer, layer, wire,
					_view.getTransformation3D());

			if ((wp != null) && isLowerSector()) {
				wp.y = -wp.y;
			}
		} catch (Exception e) {
			String s = "Problem  in wire() [SectorSuperLayer] layer = " + layer
					+ "  wire = " + wire;
			System.err.println(s);
			e.printStackTrace();			// System.exit(1);
		}
		return wp;
	}

	/**
	 * Highlight a noise hit
	 * 
	 * @param g the graphics context
	 * @param container the rendering container
	 * @param dchit the wire hit object
	 * @param simple if <code>true</code> use simple highlighting
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
		}
		else {
			g.setColor(Color.gray);
			int x[] = hexagon.xpoints;
			int y[] = hexagon.ypoints;

			// from projection may not be 6 points!
			if (hexagon.npoints > 5) {
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
	 * @param g the graphics context
	 * @param container the rendering container
	 * @param reallyClose if <code>true</code> we are really close
	 */
	private void drawWires(Graphics g, IContainer container,
			boolean reallyClose) {
		Point pp = new Point(); // workspace
		for (int layer = 1; layer <= 6; layer++) {
			for (int wire = 1; wire <= 112; wire++) {
				// note: no conversion to 0-based because the wire array has
				// a "border" of guard wires!

				g.setColor(senseWireColor);
				Point2D.Double wp = wire(layer, wire);

				if (wp != null) {
					container.worldToLocal(pp, wp);
					if (reallyClose) {
						g.fillRect(pp.x - 1, pp.y - 1, 2, 2);
						Polygon hexagon = getHexagon(container, layer, wire);
						if (hexagon == null) {
							return;
						}
						else {
							g.setColor(hexColor);
							g.drawPolygon(hexagon);
						}
					}
					else {
						g.fillRect(pp.x, pp.y, 1, 1);
					}
				}
			}
		}
	}

	/**
	 * Draw the masks showing the effect of the noise finding algorithm
	 * 
	 * @param g the graphics context
	 * @param container the rendering container
	 * @param parameters the noise algorithm parameters
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
	 * @param g the graphics context.
	 * @param container the rendering container
	 * @param wire the ZERO BASED wire 0..
	 * @param shifts the parameter shifts for this direction
	 * @param sign the direction 1 for left -1 for right * @param wr essentially
	 *            workspace
	 */
	private void drawMask(Graphics g, IContainer container, int wire,
			int shifts[], int sign) {

		wire++; // convert to 1-based

		if (sign == 1) {
			g.setColor(NoiseManager.maskFillLeft);
		}
		else {
			g.setColor(NoiseManager.maskFillRight);
		}

		for (int layer = 1; layer <= GeoConstants.NUM_LAYER; layer++) {

			Polygon hexagon = getHexagon(container, layer, wire);

			if (hexagon != null) {
				g.fillPolygon(hexagon);
				g.drawPolygon(hexagon);
			}

			// ugh -- shifts are 0-based
			for (int shift = 1; shift <= shifts[layer - 1]; shift++) {
				int tempWire = wire + sign * shift;
				if ((tempWire > 0) && (tempWire <= GeoConstants.NUM_WIRE)) {
					hexagon = getHexagon(container, layer, tempWire);
					if (hexagon != null) {
						g.fillPolygon(hexagon);
						g.drawPolygon(hexagon);
					}
				}
			}
		}

	}

	/**
	 * Obtain a crude outline of a sense wire layer
	 * 
	 * @param container the container being rendered
	 * @param layer the layer in question--a 1-based sensewire layer [1..6]
	 * @return a layer outline
	 */
	private Polygon getLayerPolygon(IContainer container, int layer) {

		if (_dirty) {
			Point2D.Double corners[] = GeometryManager.allocate(5);

			// all indices in DCGeometry calls are 1-based
			DCGeometry.getLayerPolygon(_superLayer, layer,
					_view.getTransformation3D(), corners);

			if (isLowerSector()) {
				flipPolyToLowerSector(corners);
			}

			Polygon poly = new Polygon();
			Point pp = new Point();

			for (Point2D.Double wp : corners) {
				container.worldToLocal(pp, wp);
				poly.addPoint(pp.x, pp.y);
			}

			_layerPolygons[layer - 1] = poly;
		}

		return _layerPolygons[layer - 1];
	}

	/**
	 * Gets the layer from the screen point. This only gives sensible results if
	 * the world point has already passed the "inside" test.
	 * 
	 * @param pp the screen point in question.
	 * @return the layer containing the given world point. It returns [1..6] or
	 *         -1 on failure
	 */
	private int getLayer(IContainer container, Point pp) {

		// first see if we are in the super layer
		if (contains(container, pp)) {

			// now check the layers
			for (int layer = 1; layer <= 6; layer++) {
				Polygon poly = _layerPolygons[layer - 1];
				if ((poly != null) && poly.contains(pp)) {
					return layer;
				}
			}
		}
		else {
			System.err.println("not in superlayer!");
		}
		return -1;
	}

	/**
	 * Gets the layer from the screen point. This only gives sensible results if
	 * the world point has already passed the "inside" test.
	 * 
	 * @param wp the world point in question.
	 * @return the layer containing the given world point. It returns [1..6] or
	 *         -1 on failure
	 */
	// private int getLayer(Point pp) {
	// Point2D.Double corners[] = GeometryManager.allocate(5);
	//
	// // first see if we are in the super layer
	// corners = getWorldPolygon();
	// if (WorldGraphicsUtilities.contains(corners, wp)) {
	//
	// // now check the layers
	// for (int layer = 1; layer <= 6; layer++) {
	// // all indices in DCGeometry calls are 1-based
	// DCGeometry.getLayerPolygon(_superLayer, layer,
	// _view.getTransformation3D(), corners);
	//
	// if (isLowerSector()) {
	// flipPolyToLowerSector(corners);
	// }
	//
	// if (WorldGraphicsUtilities.contains(corners, wp)) {
	// return layer;
	// }
	// }
	// }
	// return -1;
	// }

	/**
	 * Gets the wire from the world point. This only gives sensible results if
	 * the world point has already passed the "inside" test and we used getLayer
	 * on the same point to get the layer.
	 * 
	 * @param layer the one based layer [1..6]
	 * @param wp the world point in question.
	 * @return the closest wire index on the given layer in range [1..112].
	 */
	private int getWire(int layer, Point2D.Double wp) {
		Point2D.Double ends[] = GeometryManager.allocate(2);
		DCGeometry.getLayerExtendedPoints(_superLayer, layer,
				_view.getTransformation3D(), ends);

		if (isLowerSector()) {
			flipPolyToLowerSector(ends);
		}

		double fract = MathUtilities.perpendicularIntersection(ends[0], ends[1],
				wp);
		int wire = (int) Math.round(fract * (GeoConstants.NUM_WIRE + 1));
		return Math.max(1, Math.min(112, wire));
	}

	/**
	 * Add any appropriate feedback strings for the headsup display or feedback
	 * panel.
	 * 
	 * @param container the Base container.
	 * @param screenPoint the mouse location.
	 * @param worldPoint the corresponding world point.
	 * @param feedbackStrings the List of feedback strings to add to.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, List<String> feedbackStrings) {
		if (contains(container, screenPoint)) {

			NoiseReductionParameters parameters = _noiseManager
					.getParameters(_sector - 1, _superLayer - 1);

			feedbackStrings
					.add(ADataContainer.prelimColor + "Raw Occupancy "
							+ DoubleFormat.doubleFormat(
									100.0 * parameters.getRawOccupancy(), 2)
					+ "%");
			feedbackStrings.add(ADataContainer.prelimColor
					+ "Reduced Occupancy "
					+ DoubleFormat.doubleFormat(
							100.0 * parameters.getNoiseReducedOccupancy(), 2)
					+ "%");

			// getLayer returns a 1 based index (-1 on failure)
			int layer = getLayer(container, screenPoint);

			DCDataContainer dcData = _eventManager.getDCData();

			if (layer > 0) {
				int wire = getWire(layer, worldPoint);
				if ((wire > 0) && (wire <= GeoConstants.NUM_WIRE)) {

					int hitIndex = dcData.getHitIndex(_sector, _superLayer,
							layer, wire);
					if (hitIndex < 0) {
						feedbackStrings.add("superlayer " + _superLayer
								+ "  layer " + layer + "  wire " + wire);
					}
					else {
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
	 * The wires are dirty, probably because of a phi rotation
	 * 
	 * @param wires the new wire projections.
	 */
	public void dirtyWires() {
		setDirty(true);
		setPath(getWorldPolygon());
	}

	// get the world polygon corresponding to the boundary of the superlayer
	private Point2D.Double[] getWorldPolygon() {

		if (_dirty) {
			DCGeometry.getSuperLayerPolygon(_superLayer,
					_view.getTransformation3D(), _cachedWorldPolygon);
			if (isLowerSector()) {
				flipPolyToLowerSector(_cachedWorldPolygon);
			}
		} // end dirty
		return _cachedWorldPolygon;
	}

	// flip a poly created for the upper sector to the lower sector
	private void flipPolyToLowerSector(Point2D.Double wpoly[]) {
		for (Point2D.Double wp : wpoly) {
			wp.y = -wp.y;
		}
	}

	/**
	 * Gets the cell hexagon as a screen polygon.
	 * 
	 * @param container the container being rendered.
	 * @param layer the 1-based layer 1--6
	 * @param wire the one based wire 1..GeoConstants.NUM_WIRE
	 * @return the cell hexagon
	 */
	public Polygon getHexagon(IContainer container, int layer, int wire) {

		Point2D.Double wpoly[] = GeometryManager.allocate(6);
		// note all indices in calls to DCGeometry are 1-based
		int size = DCGeometry.worldPolygon(_superLayer, layer, wire,
				_view.getTransformation3D(), wpoly, null);

		if (isLowerSector()) {
			flipPolyToLowerSector(wpoly);
		}

		if (size < 3) {
			return null;
		}

		Point pp = new Point();
		Polygon poly = new Polygon();

		for (int i = 0; i < size; i++) {
			Point2D.Double wp = wpoly[i];
			container.worldToLocal(pp, wp);
			poly.addPoint(pp.x, pp.y);
		}

		return poly;
	}

	/**
	 * Test whether this is a lower sector
	 * 
	 * @return <code>true</code> if this is a lower sector
	 */
	public boolean isLowerSector() {
		return (_sector > 3);
	}

}
