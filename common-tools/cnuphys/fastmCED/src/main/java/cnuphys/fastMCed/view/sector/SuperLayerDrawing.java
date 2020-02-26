package cnuphys.fastMCed.view.sector;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.List;

import org.jlab.geom.DetectorId;
import org.jlab.geom.prim.Point3D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.util.MathUtilities;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.fastMCed.eventio.PhysicsEventManager;
import cnuphys.fastMCed.fastmc.AugmentedDetectorHit;
import cnuphys.fastMCed.fastmc.ParticleHits;
import cnuphys.fastMCed.geometry.DCGeometry;
import cnuphys.fastMCed.geometry.GeometryManager;
import cnuphys.fastMCed.snr.SNRManager;
import cnuphys.fastMCed.view.AView;
import cnuphys.lund.LundId;
import cnuphys.snr.NoiseReductionParameters;

public class SuperLayerDrawing {

	// convenient access to the event manager
	private PhysicsEventManager _eventManager = PhysicsEventManager.getInstance();
	
	private static final int NUM_LAYER = 6;
	private static final int NUM_WIRE = 112;


	// pixel density thresholds. As we zoom in. the pixels/cm increases. At
	// certain thresholds, other drawing kicks in
	public static final double wireThreshold[] = { Double.NaN, 2.0, 2.0, 1.7, 1.7, 1.6, 1.6 };
	public static final double closeupThreshold[] = { Double.NaN, 16.0, 16.0, 12.0, 12.0, 7.0, 7.0 };
	
	// for hits cells
	public static final Color defaultHitCellFill = Color.red;
	public static final Color defaultHitCellLine = X11Colors.getX11Color("Dark Red");

	//hexagon color
	public static final Color hexColor = new Color(223, 239, 239);

	// pale color used to fill in DC layers to guide the eye
	public static final Color layerFillColors[] = { X11Colors.getX11Color("cornsilk"),
			X11Colors.getX11Color("azure") };

	// color for wires
	public static final Color senseWireColor = X11Colors.getX11Color("Dodger Blue");


	// cache the layer polygons. They must be recomputed if the item is dirty.
	private Polygon _layerPolygons[] = new Polygon[6];

	// owner view
	private AView _view;

	// superlayer interface for geometric objects
	private ISuperLayer _iSupl;


	/**
	 * Constructor
	 * 
	 * @param view
	 *            the owner view
	 * @param isupl
	 *            the superlayer geometry interface
	 */
	public SuperLayerDrawing(AView view, ISuperLayer isupl) {
		_view = view;
		_iSupl = isupl;

	}

	public void drawItem(Graphics g, IContainer container, Polygon lastDrawnPolygon) {

		Graphics2D g2 = (Graphics2D) g;

		// are we really zoomed in?
		boolean reallyClose = (WorldGraphicsUtilities
				.getMeanPixelDensity(_view.getContainer()) > SuperLayerDrawing.closeupThreshold[_iSupl.superlayer()]);

		// draw layer outlines to guide the eye

		Shape clip = g2.getClip();
		// Stroke oldStroke = g2.getStroke();

		g2.setClip(lastDrawnPolygon);
		for (int layer = 1; layer <= 6; layer++) {
			Polygon poly = getLayerPolygon(container, layer);

			if ((layer % 2) == 1) {
				g.setColor(layerFillColors[1]);
				g.fillPolygon(poly);
				g.drawPolygon(poly);
			}
		}

		// draw wires?
		if (reallyClose || (WorldGraphicsUtilities
				.getMeanPixelDensity(_view.getContainer()) > SuperLayerDrawing.wireThreshold[_iSupl.superlayer()])) {
			drawWires(g, container, reallyClose);
		}
		
		//draw SNR masks?
		if (_view.showMasks()) {
			drawSNRMasks(g, container);
		}


		// draw the hits
		drawHits(g, container, reallyClose);

		// draw outer boundary again.
		g.setColor(_iSupl.item().getStyle().getLineColor());
		g.drawPolygon(lastDrawnPolygon);

		g2.setClip(clip);
	}
	
	//draw the SNR mask data where SNR thinks segments mught start
	private void drawSNRMasks(Graphics g, IContainer container) {
		//need zero based sector and super layer
		NoiseReductionParameters parameters = SNRManager.getInstance().getParameters(
				_iSupl.sector() - 1, _iSupl.superlayer() - 1);

		for (int wire = 0; wire < parameters.getNumWire(); wire++) {
			boolean leftSeg = parameters.getLeftSegments().checkBit(wire);
			boolean rightSeg = parameters.getRightSegments().checkBit(wire);
			
			
			if (leftSeg) {
				drawMask(g, container, wire, parameters.getLeftLayerShifts(), 1);
			}
			if (rightSeg) {

				drawMask(g, container, wire, parameters.getRightLayerShifts(), -1);
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
	private void drawMask(Graphics g, IContainer container, int wire, int shifts[], int sign) {

		wire++; // convert to 1-based

		if (sign == 1) {
			g.setColor(SNRManager.maskFillLeft);
		} else {
			g.setColor(SNRManager.maskFillRight);
		}

		for (int layer = 1; layer <= NUM_LAYER; layer++) {

			Polygon hexagon = getHexagon(container, layer, wire);

			if (hexagon != null) {
				g.fillPolygon(hexagon);
				g.drawPolygon(hexagon);
			}

			// ugh -- shifts are 0-based
			for (int shift = 1; shift <= shifts[layer - 1]; shift++) {
				int tempWire = wire + sign * shift;
				if ((tempWire > 0) && (tempWire <= NUM_WIRE)) {
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
		for (int layer = 1; layer <= 6; layer++) {
			for (int wire = 1; wire <= 112; wire++) {
				drawOneWire(g, container, layer, wire, reallyClose, pp);
			} // wire loop
		}
	}

	// draw a single wire
	private void drawOneWire(Graphics g, IContainer container, int layer, int wire, boolean reallyClose, Point pp) {
		g.setColor(senseWireColor);
		Point2D.Double wp = wire(_iSupl.superlayer(), layer, wire, _iSupl.isLowerSector());

		if (wp != null) {
			container.worldToLocal(pp, wp);
			if (reallyClose) {
				g.fillRect(pp.x - 1, pp.y - 1, 2, 2);
				Polygon hexagon = getHexagon(container, layer, wire);
				if (hexagon == null) {
					return;
				} else {
					g.setColor(hexColor);
					g.drawPolygon(hexagon);
				}
			} else {
				g.fillRect(pp.x, pp.y, 1, 1);
			}
		}
	}



	/**
	 * Draw hits and related data
	 * 
	 * @param g
	 *            The graphics object
	 * @param container
	 *            the drawing container
	 */
	private void drawHits(Graphics g, IContainer container, boolean reallyClose) {
		drawSingleModeHits(g, container, reallyClose);
	}


	/**
	 * Draw hits (and other data) when we are in single hit mode
	 * 
	 * @param g
	 *            The graphics object
	 * @param container
	 *            the drawing container
	 */
	private void drawSingleModeHits(Graphics g, IContainer container, boolean reallyClose) {
		
		List<ParticleHits> hits = _eventManager.getParticleHits();

		if (hits != null) {
			for (ParticleHits particleHits : hits) {
				LundId lid = particleHits.getLundId();

				List<AugmentedDetectorHit> augHits = particleHits.getHits(DetectorId.DC, _iSupl.sector()-1, _iSupl.superlayer()-1);

				if (augHits != null) {
					for (AugmentedDetectorHit hit : augHits) {
						int layer = hit.getLayerId() + 1;
						int wire = hit.getComponentId() + 1;

						drawDCHit(g, container, layer, wire, hit.isNoise(), lid);

					}
				}
			}
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
	 *            1-based layer 1..6
	 * @param wire
	 *            1-based wire 1..112
	 * @param noise
	 *            is noise hit
	 * @param pid
	 *            Lund id
	 */
	private void drawDCHit(Graphics g, IContainer container, int layer, int wire, boolean noise, LundId pid) {

		//might not even draw it if it is noise
		if (noise && _view.hideNoise()) {
			return;
		}


		// get the hexagon
		Polygon hexagon = getHexagon(container, layer, wire);
		if (hexagon == null) {
			return;
		}
		
		Color fc = pid.getStyle().getFillColor();
		
		if (noise && _view.showNoiseAnalysis()) {
			fc = Color.black;
		}
		
		Color lc = pid.getStyle().getLineColor();
		
		g.setColor(fc);
		g.fillPolygon(hexagon);
		g.setColor(lc);
		g.drawPolygon(hexagon);


	}


	/**
	 * Draw a single dc hit
	 * 
	 * @param g
	 *            the graphics context
	 * @param container
	 *            the rendering container
	 * @param layer
	 *            1-based layer 1..6
	 * @param wire
	 *            1-based wire 1..112
	 * @param noise
	 *            is noise hit
	 * @param pid
	 *            gemc particle id
	 * @param doca
	 *            the distance of closest approach array in mm
	 * @param sdoca
	 *            the smeared distance of closest approach array in mm
	 */
	private void drawBasicDCHit(Graphics g, IContainer container, int layer, int wire, boolean noise, int pid,
			float doca, float sdoca) {



	}



	/**
	 * Obtain a crude outline of a sense wire layer
	 * 
	 * @param container
	 *            the container being rendered
	 * @param layer
	 *            the layer in question--a 1-based sensewire layer [1..6]
	 * @return a layer outline
	 */
	public Polygon getLayerPolygon(IContainer container, int layer) {

		if (_iSupl.item().isDirty()) {
			Point2D.Double verticies[] = GeometryManager.allocate(14);

			// all indices in DCGeometry calls are 1-based
			DCGeometry.getLayerPolygon(_iSupl.superlayer(), layer, _iSupl.projectionPlane(), verticies);

			if (_iSupl.isLowerSector()) {
				SuperLayerDrawing.flipPolyToLowerSector(verticies);
			}

			Polygon poly = new Polygon();
			Point pp = new Point();

			for (Point2D.Double wp : verticies) {
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
	 * @param pp
	 *            the screen point in question.
	 * @return the layer containing the given world point. It returns [1..6] or
	 *         -1 on failure
	 */
	public int getLayer(IContainer container, Point pp) {

		// first see if we are in the super layer
		if (_iSupl.item().contains(container, pp)) {

			// now check the layers
			for (int layer = 1; layer <= 6; layer++) {
				Polygon poly = getLayerPolygon(layer);
				if ((poly != null) && poly.contains(pp)) {
					return layer;
				}
			}
		} else {
			System.err.println("not in superlayer!");
		}
		return -1;
	}

	/**
	 * Get the layer and the wire we are in
	 * 
	 * @param container
	 *            holds 1-based layer and wire
	 * @param pp
	 *            the mouse point
	 * @param data
	 *            holds results [later, wire]
	 */
	public void getLayerAndWire(IContainer container, Point pp, int[] data) {
		data[0] = -1;
		data[1] = -1;
		data[0] = getLayer(container, pp);

		// could be off by one
		if (data[0] > 0) {

			// get wire guess
			Polygon poly = getLayerPolygon(data[0]);
			int ymin = poly.ypoints[4];
			int ymax = poly.ypoints[13];
			double fract = (pp.y - ymin) / (ymax - ymin + 1.);
			int wguess = 1 + (int) ((1. - fract) * 112);
			// System.err.println("WIRE GUESS: " + wguess);

			int minLay = Math.max(data[0] - 1, 1);
			int maxLay = Math.min(data[0] + 1, 6);
			for (int lay = minLay; lay <= maxLay; lay++) {

				int minWire = Math.max(1, wguess - 6);
				int maxWire = Math.min(112, wguess + 6);

				for (int wire = minWire; wire <= maxWire; wire++) {
					Polygon hexagon = getHexagon(container, lay, wire);
					if ((hexagon != null) && hexagon.contains(pp)) {
						data[0] = lay;
						data[1] = wire;
						return;
					}
				}
			}
		}
	}

	/**
	 * Get the layer polygon
	 * 
	 * @param layer
	 *            the 1-based layer [1..6]
	 * @return the layer polygon
	 */
	public Polygon getLayerPolygon(int layer) {
		return _layerPolygons[layer - 1];
	}

	/**
	 * Gets the cell hexagon as a screen polygon.
	 * 
	 * @param container
	 *            the container being rendered.
	 * @param layer
	 *            the 1-based layer 1..6
	 * @param wire
	 *            the one based wire 1..112
	 * @return the cell hexagon
	 */
	public Polygon getHexagon(IContainer container, int layer, int wire) {

		Point2D.Double wpoly[] = GeometryManager.allocate(6);
		// note all indices in calls to DCGeometry are 1-based
		if (!DCGeometry.getHexagon(_iSupl.superlayer(), layer, wire, _iSupl.projectionPlane(), wpoly, null)) {
			return null;
		}
		;

		if (_iSupl.isLowerSector()) {
			flipPolyToLowerSector(wpoly);
		}

		Point pp = new Point();
		Polygon poly = new Polygon();

		for (int i = 0; i < wpoly.length; i++) {
			Point2D.Double wp = wpoly[i];
			container.worldToLocal(pp, wp);
			poly.addPoint(pp.x, pp.y);
		}

		return poly;
	}

	/**
	 * flip a poly created for the upper sector to the lower sector
	 * 
	 * @param wpoly
	 *            the polygon to flip
	 */
	public static void flipPolyToLowerSector(Point2D.Double wpoly[]) {
		for (Point2D.Double wp : wpoly) {
			wp.y = -wp.y;
		}
	}

	/**
	 * Get the projected wire location
	 * 
	 * @param superlayer
	 *            the 1-based superlayer 1..6
	 * @param layer
	 *            the 1-based layer 1..6
	 * @param wire
	 *            the 1-based wire 1..112
	 * @param isLower
	 *            if <code>true</code> flip to lower sector (SectorView only)
	 * @return the point, which might have NaNs
	 */
	public Point2D.Double wire(int superlayer, int layer, int wire, boolean isLower) {
		// the indices to all DCGeometry calls are 1-based

		Point2D.Double wp = null;
		try {
			wp = DCGeometry.getCenter(superlayer, layer, wire, _iSupl.projectionPlane());

			if ((wp != null) && isLower) {
				wp.y = -wp.y;
			}
		} catch (Exception e) {
			String s = "Problem  in wire() [SectorSuperLayer] layer = " + layer + "  wire = " + wire;
			System.err.println(s);
			e.printStackTrace(); // System.exit(1);
		}
		return wp;
	}



	/**
	 * projected space point. Projected by finding the closest point on the
	 * plane.
	 * 
	 * @param x
	 *            the x coordinate
	 * @param y
	 *            the y coordinate
	 * @param z
	 *            the z coordinate
	 * @return the projected space point
	 */
	public Point3D projectedPoint(double x, double y, double z, Point2D.Double wp) {
		return _view.projectedPoint(x, y, z, _iSupl.projectionPlane(), wp);
	}



	/**
	 * Gets the wire from the world point. This only gives sensible results if
	 * the world point has already passed the "inside" test and we used getLayer
	 * on the same point to get the layer.
	 * 
	 * @param layer
	 *            the one based layer [1..6]
	 * @param wp
	 *            the world point in question.
	 * @return the closest wire index on the given layer in range [1..112].
	 */
	public int getWire(int layer, Point2D.Double wp) {
		Point2D.Double ends[] = GeometryManager.allocate(2);
		DCGeometry.getLayerExtendedPoints(_iSupl.superlayer(), layer, _iSupl.projectionPlane(), ends);

		if (_iSupl.isLowerSector()) {
			SuperLayerDrawing.flipPolyToLowerSector(ends);
		}

		double fract = MathUtilities.perpendicularIntersection(ends[0], ends[1], wp);
		int wire = (int) Math.round(fract * (NUM_WIRE + 1));
		return Math.max(1, Math.min(112, wire));
	}

	/**
	 * Add any appropriate feedback strings
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
	public void getFeedbackStrings(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings) {

		if (_iSupl.item().contains(container, screenPoint)) {



			// getLayer returns a 1 based index (-1 on failure)
			// int layer = getLayer(container, screenPoint);

			int data[] = new int[2];
			getLayerAndWire(container, screenPoint, data);
			int layer = data[0];
			int wire = data[1];  
	
			feedbackStrings.add("Superlayer " + _iSupl.superlayer() + "  Layer "
					+ layer + "  Wire " + wire);

//			System.err.println("Approx theta = " + SNRManager.getInstance().approximateTheta(_iSupl.superlayer()-1, wire-1));
			
			int wire0 = wire-1;  //convert to 0 based
			List<ParticleHits> hits = _eventManager.getParticleHits();

			if (hits != null) {
				for (ParticleHits particleHits : hits) {
					LundId lid = particleHits.getLundId();

					List<AugmentedDetectorHit> augHits = particleHits.getHits(DetectorId.DC, _iSupl.sector()-1, _iSupl.superlayer()-1);

					if (augHits != null) {
						for (AugmentedDetectorHit hit : augHits) {
							if (hit.getComponentId() == wire0) {
								
								//might not even care if it is noise
								if (hit.isNoise() && _view.hideNoise()) {
									break;
								}
								
								ParticleHits.addHitFeedback(hit, lid, feedbackStrings);
								break;
							}
						}
					}
				}
			}		

			SNRManager.getInstance().addParametersToFeedback(_iSupl.sector(), _iSupl.superlayer(), feedbackStrings);

		} //contains
	}


}
