package cnuphys.ced.common;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;

import org.jlab.geom.DetectorHit;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.MathUtilities;
import cnuphys.bCNU.util.VectorSupport;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.projecteddc.ISuperLayer;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.data.DC;
import cnuphys.ced.event.data.DataSupport;
import cnuphys.ced.fastmc.FastMCManager;
import cnuphys.ced.fastmc.ParticleHits;
import cnuphys.ced.frame.CedColors;
import cnuphys.ced.geometry.DCGeometry;
import cnuphys.ced.geometry.GeoConstants;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.noise.NoiseManager;
import cnuphys.lund.LundId;
import cnuphys.lund.LundStyle;
import cnuphys.lund.LundSupport;
import cnuphys.snr.NoiseReductionParameters;

public class SuperLayerDrawing {

	// pixel density thresholds. As we zoom in. the pixels/cm increases. At
	// certain thresholds, other drawing kicks in
	public static final double wireThreshold[] = { Double.NaN, 2.0, 2.0, 1.7, 1.7, 1.6, 1.6 };
	public static final double closeupThreshold[] = { Double.NaN, 16.0, 16.0, 12.0, 12.0, 7.0, 7.0 };

	// cache the layer polygons. They must be recomputed if the item is dirty.
	private Polygon _layerPolygons[] = new Polygon[6];

	// owner view
	private CedView _view;
	
	//superlayer interface for geometric objects
	private ISuperLayer _iSupl;
		
	// convenient access to the noise manager
	private NoiseManager _noiseManager = NoiseManager.getInstance();

	// the characteristic direction of this superlayer. It is simply the
	// dirction,
	// as a unit vector, of any of the wires
	private double[] _direction;
	
	/**
	 * Constructor
	 * 
	 * @param view
	 *            the owner view
	 * @param isupl
	 *            the superlayer geometry interface
	 */
	public SuperLayerDrawing(CedView view, ISuperLayer isupl) {
		_view = view;
		_iSupl = isupl;
		
		//set the wire direction
		//use any wire to set the wire direction
		Line3D l3d = DCGeometry.getWire(_iSupl.sector(), _iSupl.superlayer(), 3, 56);	
		_direction = new double[3];
		_direction[0] = l3d.end().x() - l3d.origin().x();
		_direction[1] = l3d.end().y() - l3d.origin().y();
		_direction[2] = l3d.end().z() - l3d.origin().z();
		_direction = VectorSupport.unitVector(_direction);
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
				g.setColor(CedColors.layerFillColors[1]);
				g.fillPolygon(poly);
				g.drawPolygon(poly);
			}
		}

		// draw results of noise reduction? If so will need the parameters
		// (which also have the results)
		NoiseReductionParameters parameters = _noiseManager.getParameters(_iSupl.sector() - 1, _iSupl.superlayer() - 1);

		// show the noise segment masks?
		if (_view.showMasks()) {
			drawMasks(g, container, parameters);
		}


		// draw wires?
		if (reallyClose || (WorldGraphicsUtilities
				.getMeanPixelDensity(_view.getContainer()) > SuperLayerDrawing.wireThreshold[_iSupl.superlayer()])) {
			drawWires(g, container, reallyClose);
		}

		// draw the hits
		drawHits(g, container, reallyClose);

		// draw outer boundary again.
		g.setColor(_iSupl.item().getStyle().getLineColor());
		g.drawPolygon(lastDrawnPolygon);

		g2.setClip(clip);
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
			}  //wire loop
		}
	}
	
	private void drawOneWire(Graphics g, IContainer container, int layer, int wire, boolean reallyClose, Point pp) {
		g.setColor(CedColors.senseWireColor);
		Point2D.Double wp = wire(_iSupl.superlayer(), layer, wire, _iSupl.isLowerSector());

		if (wp != null) {
			container.worldToLocal(pp, wp);
			if (reallyClose) {
				g.fillRect(pp.x - 1, pp.y - 1, 2, 2);
				Polygon hexagon = getHexagon(container, layer, wire);
				if (hexagon == null) {
					return;
				} else {
					g.setColor(CedColors.hexColor);
					g.drawPolygon(hexagon);
				}
			} else {
				g.fillRect(pp.x, pp.y, 1, 1);
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
	private void drawMasks(Graphics g, IContainer container, NoiseReductionParameters parameters) {
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
			g.setColor(NoiseManager.maskFillLeft);
		} else {
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
	 * Draw hits and related data
	 * 
	 * @param g
	 *            The graphics object
	 * @param container
	 *            the drawing container
	 */
	private void drawHits(Graphics g, IContainer container, boolean reallyClose) {

		if (_view.isSingleEventMode()) {
			drawSingleModeHits(g, container, reallyClose);
		} else {
			drawAccumulatedHits(g, container, reallyClose);
		}
	}
	
	//draw hits in accumulated mode
	private void drawAccumulatedHits(Graphics g, IContainer container, boolean reallyClose) {

		int dcAccumulatedData[][][][] = AccumulationManager.getInstance().getAccumulatedDgtzDcData();
		int maxHit = AccumulationManager.getInstance().getMaxDgtzDcCount();
		if (maxHit < 1) {
			return;
		}

		int sect0 = _iSupl.sector() - 1;
		int supl0 = _iSupl.superlayer() - 1;

		for (int lay0 = 0; lay0 < 6; lay0++) {
			for (int wire0 = 0; wire0 < 112; wire0++) {

				int hit = dcAccumulatedData[sect0][supl0][lay0][wire0];
				double fract;
				if (_view.isSimpleAccumulatedMode()) {
					fract = ((double) hit) / maxHit;
				} else {
					fract = Math.log(hit + 1.) / Math.log(maxHit + 1.);
				}

				Color color = AccumulationManager.getInstance().getColor(fract);

				g.setColor(color);
				Polygon hexagon = getHexagon(container, lay0 + 1, wire0 + 1);
				if (hexagon != null) {
					g.fillPolygon(hexagon);
					g.drawPolygon(hexagon);

				}
			} //wire loop
		} //layer loop

	}
	
	/**
	 * Draw hits (and other data) when we are in single hit mode
	 * 
	 * @param g
	 *            The graphics object
	 * @param container
	 *            the drawing container
	 */
	private void drawFastMCSingleModeHits(Graphics g, IContainer container, boolean reallyClose) {
		if (FastMCManager.getInstance().isStreaming()) {
			return;
		}

		Vector<ParticleHits> phits = FastMCManager.getInstance().getFastMCHits();
		if ((phits == null) || phits.isEmpty()) {
			return;
		}
		
		for (ParticleHits hits : phits) {
			List<DetectorHit> dchits = hits.getDCHits();
			if (dchits != null) {
				for (DetectorHit hit : dchits) {
					int sect1 = hit.getSectorId() + 1;
					int supl1 = hit.getSuperlayerId() + 1;
					if ((sect1 == _iSupl.sector()) && (supl1 == _iSupl.superlayer())) {
						int lay1 = hit.getLayerId() + 1;
						int wire1 = hit.getComponentId() + 1;
						drawDCHit(g, container, lay1, wire1, false, hits.getLundId().getId(), -1, null, null);
					}
				}
			}
		}
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

		if (ClasIoEventManager.getInstance().isSourceFastMC()) {
			drawFastMCSingleModeHits(g, container, reallyClose);
			return;
		}
		
		
		int hitCount = DC.hitCount();
		
	//	System.err.println("SINGLEMODEHITS HC: " + hitCount);

		if (hitCount > 0) {
			Point pp = new Point();
			byte sector[] = DC.sector();
			byte superlayer[] = DC.superlayer();
			byte layer[] = DC.layer();
			short wire[] = DC.wire();
			int pid[] = DC.pid();
			float doca[] = DC.doca();
			float sdoca[] = DC.sdoca();

			for (int i = 0; i < hitCount; i++) {
				try {
					int sect1 = sector[i]; // 1 based
					int supl1 = superlayer[i]; // 1 based

					if ((sect1 == _iSupl.sector()) && (supl1 == _iSupl.superlayer())) {
						int lay1 = layer[i]; // 1 based
						int wire1 = wire[i]; // 1 based

						boolean noise = false;
						if (_noiseManager.getNoise() != null) {
							if (i < _noiseManager.getNoise().length) {
								noise = _noiseManager.getNoise()[i];
							} else {
								String ws = " hit index = " + i + " noise length = " + _noiseManager.getNoise().length;
								Log.getInstance().warning(ws);
							}
						}

						int pdgid = (pid == null) ? -1 : pid[i];

						drawDCHit(g, container, lay1, wire1, noise, pdgid, i, doca, sdoca);
						
						//just draw the wire again
						drawOneWire(g, container, lay1, wire1, reallyClose, pp);
					}
				} catch (NullPointerException e) {
					System.err.println("null pointer in SectorSuperLayer hit drawing");
					e.printStackTrace();
				}
			} // for loop on hit count

		} // hitcount > 0

		// draw track based hits (docas) and segments
		drawTimeBasedHits(g, container);
		drawTimeBasedSegments(g, container);
	}
	
	
	//draw time based reconstruction hits
	private void drawTimeBasedHits(Graphics g, IContainer container) {
		int hitCount = DC.timeBasedTrkgHitCount();

		if (hitCount > 0) {
			int sector[] = DC.timeBasedTrkgSector();
			int superlayer[] = DC.timeBasedTrkgSuperlayer();
			int layer[] = DC.timeBasedTrkgLayer();
			int wire[] = DC.timeBasedTrkgWire();
			double doca[] = DC.timeBasedTrkgDoca();
			
			for (int i = 0; i < hitCount; i++) {
				try {
					int sect1 = sector[i]; // 1 based
					int supl1 = superlayer[i]; // 1 based

					if ((sect1 == _iSupl.sector()) && (supl1 == _iSupl.superlayer())) {
						int lay1 = layer[i]; // 1 based
						int wire1 = wire[i]; // 1 based

						// drawDOCA(Graphics g, IContainer container, int layer,
						// int wire, double doca2d, Color fillColor, Color
						// lineColor) {

						// note conversion to mm
						if (_view.showDCtbDoca()) {
							drawDOCA(g, container, lay1, wire1, 
									10 * doca[i], CedColors.tbDocaFill, CedColors.tbDocaLine);
						}
					}
				} catch (NullPointerException e) {
					System.err.println("null pointer in SectorSuperLayer time based hit drawing");
					e.printStackTrace();
				}
			} // for loop
		} //hit count > 0
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
	private void drawDCHit(Graphics g, IContainer container, int layer, int wire, boolean noise, int pid, int index,
			float doca[], float sdoca[]) {

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

		Color hitFill = CedColors.defaultHitCellFill;
		Color hitLine = CedColors.defaultHitCellLine;

		// do we have simulated "truth" data?
		if (showTruth && (pid >= 0)) {
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

		// draw gems docas?
		if (showTruth && (WorldGraphicsUtilities
				.getMeanPixelDensity(_view.getContainer()) > SuperLayerDrawing.wireThreshold[_iSupl.superlayer()])) {
			if ((doca != null) && (doca.length > 0) && (doca[index] > 1.0e-6)) {
				drawDOCA(g, container, layer, wire, doca[index], CedColors.docaFill,
						CedColors.docaLine);
			}
			if ((sdoca != null) && (sdoca[index] > 1.0e-6)) {
				// drawDOCA(g, container, layer, wire, doca[index],
				// CedColors.sdocaFill, CedColors.sdocaLine);
			}
		}

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
			DCGeometry.getLayerPolygon(_iSupl.superlayer(), layer, 
					_iSupl.projectionPlane(), verticies);

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
	 * @param container
	 * @param pp
	 * @param data
	 */
	public void getLayerAndWire(IContainer container, Point pp, int[] data) {
		data[0] = -1;
		data[1] = -1;
		data[0] = getLayer(container, pp);
		
		//could be off by one
		if (data[0] > 0) {
			
			//get wire guess
			Polygon poly = getLayerPolygon(data[0]);
			int ymin = poly.ypoints[4];
			int ymax = poly.ypoints[13];
			double fract = (pp.y - ymin)/(ymax-ymin+1.);
			int wguess = 1 + (int)((1.-fract)*112);
			//System.err.println("WIRE GUESS: " + wguess);
			
			int minLay = Math.max(data[0] - 1, 1);
			int maxLay = Math.min(data[0] + 1, 6);
			for (int lay = minLay; lay <= maxLay; lay++) {
				
				int minWire = Math.max(1, wguess-6);
				int maxWire = Math.min(112, wguess+6);
				
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
	 * Highlight a noise hit on a drift chamber
	 * 
	 * @param g
	 *            the graphics context
	 * @param container
	 *            the drawing container
	 * @param simple
	 *            if <code>true</code>, use simpler drawing
	 * @param hexagon
	 *            the cell hexagon
	 */
	public void highlightNoiseHit(Graphics g, IContainer container, boolean simple, Polygon hexagon) {

		if (hexagon == null) {
			return;
		}

		if (simple) {
			g.setColor(Color.black);
			g.fillPolygon(hexagon);
			g.setColor(Color.black);
			g.drawPolygon(hexagon);
		} else {
			g.setColor(X11Colors.getX11Color("lavender blush"));
			g.fillPolygon(hexagon);
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
	public Polygon getHexagon(IContainer container, int layer,
			int wire) {

		Point2D.Double wpoly[] = GeometryManager.allocate(6);
		// note all indices in calls to DCGeometry are 1-based
		if (!DCGeometry.getHexagon(_iSupl.superlayer(), layer, wire, 
				_iSupl.projectionPlane(), wpoly, null)) {
			return null;
		};

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

//		if ((layer == 1) && (wire == 1)) {
//			System.err.println("\n");
//		}
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
	 * Draw a distance of closest approach circle
	 * 
	 * @param g
	 *            the graphics context
	 * @param container
	 *            the rendering container
	 * @param layer
	 *            the 1-based layer 1..6
	 * @param wire
	 *            the 1-based wire 1..112
	 * @param doca2d
	 *            the doca in mm
	 */
	public void drawDOCA(Graphics g, IContainer container, int layer, int wire,
			double doca2d, Color fillColor, Color lineColor) {

		if (Double.isNaN(doca2d) || (doca2d < 1.0e-6)) {
			return;
		}

		// draw gemc doca
		// convert micron to cm
		double radius = doca2d / 10.0; // converted mm to cm

		if (radius > 5) {
			String wmsg = "Very large doca radius: " + radius + " cm. Sect: " + _iSupl.sector() + " supl: " + _iSupl.superlayer()
					+ "lay: " + layer + " wire: " + wire;

			Log.getInstance().warning(wmsg);
			System.err.println(wmsg);
			return;
		}

		// center is the given wire projected locations

		Point2D.Double center = wire(_iSupl.superlayer(), layer, wire, _iSupl.isLowerSector());
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
		g.setColor(fillColor);
		g.fillPolygon(docaPoly);
		g.setColor(lineColor);
		g.drawPolygon(docaPoly);
	}

	/**
	 * projected space point. Projected by finding the closest point on the plane.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @return the projected space point
	 */
	public Point3D projectedPoint(double x, double y, double z, Point2D.Double wp) {
		return _view.projectedPoint(x, y, z, _iSupl.projectionPlane(), wp);
	}

	/**
	 * 
	 * @param g
	 *            the graphics context
	 * @param container
	 *            the drawing container
	 */
	public void drawTimeBasedSegments(Graphics g, IContainer container) {

		if (!_view.showDCtbSegments()) {
			return;
		}

		int segCount = DC.timeBasedSegmentCount();

		if (segCount > 0) {
			byte sector[] = DC.timeBasedSegmentSector();
			byte superlayer[] = DC.timeBasedSegmentSuperlayer();

			float x1[] = DC.timeBasedSegment1X();
			float z1[] = DC.timeBasedSegment1Z();
			float x2[] = DC.timeBasedSegment2X();
			float z2[] = DC.timeBasedSegment2Z();

			if ((x1 == null) || (z1 == null) || (x2 == null) || (z2 == null)) {
				return;
			}

			// g.setColor(tbSegmentLine);
			Point2D.Double wp1 = new Point2D.Double();
			Point2D.Double wp2 = new Point2D.Double();

			for (int i = 0; i < segCount; i++) {
				int sect1 = sector[i]; // 1 based
				int supl1 = superlayer[i]; // 1 based

				if ((sect1 == _iSupl.sector()) && (supl1 == _iSupl.superlayer())) {
					
					Point3D p3d1 = projectedPoint(x1[i], 0, z1[i], wp1);
					Point3D p3d2 = projectedPoint(x2[i], 0, z2[i], wp2);
					
					drawSegment(g, container, _view, wp1, wp2, CedColors.tbSegmentLine);

				}
			}
		}
	} // drawTimeBasedSegments
	

	private void drawSegment(Graphics g, IContainer container, CedView view, Point2D.Double sectPnt1,
			Point2D.Double sectPnt2, Color color) {

		Graphics2D g2 = (Graphics2D) g;
		Stroke oldStroke = g2.getStroke();

		Point p1 = new Point();
		Point p2 = new Point();

		container.worldToLocal(p1, sectPnt1);
		container.worldToLocal(p2, sectPnt2);

		g.setColor(CedColors.docaFill);
		g2.setStroke(GraphicsUtilities.getStroke(6f, LineStyle.SOLID));
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
		g.setColor(color);
		g2.setStroke(GraphicsUtilities.getStroke(1.5f, LineStyle.SOLID));
		g.drawLine(p1.x, p1.y, p2.x, p2.y);

		g2.setStroke(oldStroke);
	}

	private void rotatePoint(Point2D.Double wp, double phi) {
		double cphi = Math.cos(phi);
		// double sphi = Math.sin(phi);

		// double x = wp.x*cphi + wp.y*sphi;
		// double y = wp.y*cphi - wp.x*sphi;
		wp.y = wp.y * cphi;
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
		int wire = (int) Math.round(fract * (GeoConstants.NUM_WIRE + 1));
		return Math.max(1, Math.min(112, wire));
	}

	
	/**
	 * Get the direction in the lab system of a wire in this superlayer
	 * @return a unit vector in the wire direction
	 */
	public double [] getWireDirection() {
		return _direction;
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
	public void getFeedbackStrings(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings) {
		
		
		
		
		if (_iSupl.item().contains(container, screenPoint)) {

			NoiseReductionParameters parameters = _noiseManager.getParameters(_iSupl.sector() - 1, _iSupl.superlayer() - 1);
			
			//wire direction
			String wds = "wire dir: " + VectorSupport.toString(getWireDirection(), 3);
			feedbackStrings.add(wds);

			feedbackStrings.add(DataSupport.prelimColor + "Raw Occupancy "
					+ DoubleFormat.doubleFormat(100.0 * parameters.getRawOccupancy(), 2) + "%");
			feedbackStrings.add(DataSupport.prelimColor + "Reduced Occupancy "
					+ DoubleFormat.doubleFormat(100.0 * parameters.getNoiseReducedOccupancy(), 2) + "%");

			// getLayer returns a 1 based index (-1 on failure)
			// int layer = getLayer(container, screenPoint);

			int data[] = new int[2];
			getLayerAndWire(container, screenPoint, data);
			int layer = data[0];
			int wire = data[1];

			if ((layer > 0) && (wire > 0)) {
				// int wire = getWire(layer, worldPoint);
				// if ((wire > 0) && (wire <= GeoConstants.NUM_WIRE)) {

				int hitIndex = DC.hitIndex(_iSupl.sector(), _iSupl.superlayer(), layer, wire);
				if (hitIndex < 0) {
					feedbackStrings.add("superlayer " + _iSupl.superlayer() + "  layer " + layer + "  wire " + wire);
				} else {
					DC.noiseFeedback(hitIndex, feedbackStrings);
					DC.trueFeedback(hitIndex, feedbackStrings);
					DataSupport.truePidFeedback(DC.pid(), hitIndex, feedbackStrings);
					DC.dcBanksFeedback(hitIndex, feedbackStrings);
				}

				// } // good wire
			} // good layer

			addReconstructedFeedback(feedbackStrings);
		}
	}

	// add time based recons fb
	private void addReconstructedFeedback(List<String> feedbackStrings) {

		double p[] = DC.timeBasedTrackP();
		if (p != null) {
			int reconTrackCount = p.length;
			if (reconTrackCount > 0) {
				feedbackStrings.add(DataSupport.reconColor + "TB #reconstructed tracks " + reconTrackCount);
				for (int i = 0; i < reconTrackCount; i++) {

					feedbackStrings.add(DataSupport.reconColor + "TB trk# " + (i + 1) + " recon p "
							+ DoubleFormat.doubleFormat(p[i], 5) + " Gev/c");
				}
			}
		} // p != null
	}

}
