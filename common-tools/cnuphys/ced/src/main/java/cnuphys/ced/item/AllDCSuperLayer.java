package cnuphys.ced.item;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import org.jlab.geom.prim.Point3D;

import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.alldc.AllDCView;
import cnuphys.ced.clasio.ClasIoEventManager;
//import cnuphys.ced.dcnoise.NoiseEventListener;
//import cnuphys.ced.dcnoise.NoiseReductionParameters;
//import cnuphys.ced.dcnoise.test.TestParameters;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.data.AIDC;
import cnuphys.ced.event.data.DC;
import cnuphys.ced.event.data.DCReconHit;
import cnuphys.ced.event.data.DCReconHitList;
import cnuphys.ced.event.data.DCTdcHit;
import cnuphys.ced.event.data.DCTdcHitList;
import cnuphys.ced.event.data.DataSupport;
import cnuphys.ced.frame.CedColors;
import cnuphys.ced.geometry.DCGeometry;
import cnuphys.ced.geometry.GeoConstants;
import cnuphys.ced.noise.NoiseManager;
import cnuphys.lund.LundId;
import cnuphys.lund.LundStyle;
import cnuphys.lund.LundSupport;
import cnuphys.snr.NoiseReductionParameters;
import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.item.RectangleItem;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.UnicodeSupport;

public class AllDCSuperLayer extends RectangleItem {

	// convenient access to the event manager
	ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// convenient access to the noise manager
	NoiseManager _noiseManager = NoiseManager.getInstance();

	// font for label text
	private static final Font labelFont = Fonts.commonFont(Font.PLAIN, 11);
	
	private Color _lastColor;

	// the sector [1..6]
	private int _sector;

	// the super layer [1..6]
	private int _superLayer;

	// cell overlay transparent color
	private static final Color cellOverlayColor = new Color(180, 180, 180, 32);

	// the number of wires per layer
	private int _numWires;

	// the parent view
	private AllDCView _view;

	// for hits cells
	private static final Color _defaultHitCellFill = Color.red;
//	private static final Color _defaultHitCellLine = X11Colors.getX11Color("Dark Red");
	private static final Color _defaultHitCellLine = Color.gray;

	// this is the world rectangle that defines the super layer
	private Rectangle2D.Double _worldRectangle;

	// cache the layer world rectangles
	private Rectangle2D.Double _layerWorldRects[] = new Rectangle2D.Double[GeoConstants.NUM_LAYER];

	// cache the "position" rects which span the superlayer. That is, a wire
	// cell
	// is the intersection of the layer rect and the position rect
	private Rectangle2D.Double _positionWorldRects[];

	/**
	 * Constructor for a geometrically unfaithful "all dc" superlayer.
	 * 
	 * @param layer          the Layer this item is on.
	 * @param alldc          the AllDCView parent
	 * @param worldRectangle the boundaries which are not the real boundaries.
	 * @param sector         the sector [0..5]
	 * @param superLayer     the superLayer [0..5]
	 * @param numWires       the number of wires per layer
	 */
	public AllDCSuperLayer(LogicalLayer layer, CedView view, Rectangle2D.Double worldRectangle, int sector,
			int superLayer, int numWires) {
		super(layer, worldRectangle);
		_worldRectangle = worldRectangle;
		_view = (AllDCView)view;
		_numWires = numWires;

		_style.setFillColor(Color.white);
		_style.setLineColor(Color.black);
		_sector = sector + 1; // convert to 1-based
		setLayerRects();
		setPositionRects();

		_superLayer = superLayer + 1; // convert to 1-based
		_name = "Sector: " + _sector + " SuperLayer: " + _superLayer;

	}

	// cache the layer outline rectangles
	private void setLayerRects() {
		double dy = _worldRectangle.height / GeoConstants.NUM_LAYER;
		double x = _worldRectangle.x;
		double y = _worldRectangle.y;
		double w = _worldRectangle.width;

		for (int i = 0; i < GeoConstants.NUM_SUPERLAYER; i++) {
			// trick to invert layers in lower sector
			int recIndex = (_sector < 4) ? i : (GeoConstants.NUM_SUPERLAYER - i - 1);
			_layerWorldRects[recIndex] = new Rectangle2D.Double(x, y, w, dy);
			y += dy;
		}
	}

	// cache the position rectangles
	private void setPositionRects() {
		_positionWorldRects = new Rectangle2D.Double[_numWires];

		double dx = _worldRectangle.width / _numWires;
		double x = _worldRectangle.x;
		double y = _worldRectangle.y;
		double h = _worldRectangle.height;

		// note counting right to left
		for (int i = 0; i < _numWires; i++) {
			_positionWorldRects[_numWires - i - 1] = new Rectangle2D.Double(x, y, dx, h);
			x += dx;
		}
	}

	/**
	 * Custom drawer for the item.
	 * 
	 * @param g         the graphics context.
	 * @param container the graphical container being rendered.
	 */
	@Override
	public void drawItem(Graphics g, IContainer container) {

		if (_eventManager.isAccumulating()) {
			return;
		}

		// System.err.println("All DC SuperLayer DIRTY: " + isDirty());
		super.drawItem(g, container); // draws rectangular shell

		double left = _worldRectangle.x;
		double top = _worldRectangle.y;
		g.setFont(labelFont);
		g.setColor(Color.cyan);
		WorldGraphicsUtilities.drawWorldText(g, container, left, top, "" + _superLayer, -9, -5);

		// now the data
		if (_view.isSingleEventMode()) {
			// shade the layers
			for (int i = 0; i < GeoConstants.NUM_LAYER; i += 2) {
				WorldGraphicsUtilities.drawWorldRectangle(g, container, _layerWorldRects[i], cellOverlayColor, null);

			}

			// causes cell shading
//			for (int i = 0; i < _numWires; i += 2) {
//				WorldGraphicsUtilities.drawWorldRectangle(g, container, _positionWorldRects[i], cellOverlayColor, null);
//
//			}
			
			singleEventDrawItem(g, container);

		} else {
			accumulatedDrawItem(g, container);
		}

		// just to make clean
		g.setColor(_style.getLineColor());
		g.drawPolygon(_lastDrawnPolygon);
	}

	/**
	 * Draw in single event mode
	 * 
	 * @param g         the graphics context
	 * @param container the rendering container
	 */
	private void singleEventDrawItem(Graphics g, IContainer container) {

		Rectangle2D.Double wr = new Rectangle2D.Double(); // used over and over
		Rectangle pr = new Rectangle(); // used over and over

		// draw results of noise reduction? If so will need the parameters
		// (which have the results)
		NoiseReductionParameters parameters = _noiseManager.getParameters(_sector - 1, _superLayer - 1);

		// show the noise segment masks?
		if (_view.showMasks()) {
			drawMasks(g, container, parameters);
		}
					
		// draw raw hits
		if (_view.showRawHits()) {
			DCTdcHitList hits = DC.getInstance().getTDCHits();
			if ((hits != null) && !hits.isEmpty()) {

				for (DCTdcHit hit : hits) {
					if ((hit.sector == _sector) && (hit.superlayer == _superLayer)) {
						drawDCRawHit(g, container, hit.layer6, hit.wire, hit.noise, -1, wr);
					}
				}
			}
		}

		// draw regular HB Hits
		if (_view.showHBHits()) {
			DCReconHitList hits = DC.getInstance().getHBHits();
			if ((hits != null) && !hits.isEmpty()) {
				for (DCReconHit hit : hits) {
					if ((hit.sector == _sector) && (hit.superlayer == _superLayer)) {
						drawDCHit(g, container, hit.layer, hit.wire, pr, wr, CedColors.HB_COLOR, 0);
					}
				}
			}
		}

		// draw regular TB Hits
		if (_view.showTBHits()) {
			DCReconHitList hits = DC.getInstance().getTBHits();
			if ((hits != null) && !hits.isEmpty()) {
				for (DCReconHit hit : hits) {
					if ((hit.sector == _sector) && (hit.superlayer == _superLayer)) {
						drawDCHit(g, container, hit.layer, hit.wire, pr, wr, CedColors.TB_COLOR, 0);
					}
				}
			}
		}
		
		// draw AI HB Hits
		if (_view.showAIHBHits()) {
			DCReconHitList hits = AIDC.getInstance().getAIHBHits();
			if ((hits != null) && !hits.isEmpty()) {
				for (DCReconHit hit : hits) {
					if ((hit.sector == _sector) && (hit.superlayer == _superLayer)) {
						drawDCHit(g, container, hit.layer, hit.wire, pr, wr, CedColors.AIHB_COLOR, 1);
					}
				}
			}
		}

		// draw AI TB Hits
		if (_view.showAITBHits()) {
			DCReconHitList hits = AIDC.getInstance().getAITBHits();
			if ((hits != null) && !hits.isEmpty()) {
				for (DCReconHit hit : hits) {
					if ((hit.sector == _sector) && (hit.superlayer == _superLayer)) {
						drawDCHit(g, container, hit.layer, hit.wire, pr, wr, CedColors.AITB_COLOR, 2);
					}
				}
			}
		}

		// draw nn hits
		if (_view.showNNHits()) {
			DCTdcHitList hits = DC.getInstance().getTDCHits();
			if ((hits != null) && !hits.isEmpty()) {

				for (DCTdcHit hit : hits) {
					if ((hit.sector == _sector) && (hit.superlayer == _superLayer) && hit.nnHit) {
						drawDCNNHit(g, container, hit.layer6, hit.wire, wr);
					}
				}
			}
		}


//		int hitCount = DC.hitCount();
//		
//		if (hitCount > 0)  {
//			byte sector[] = DC.sector();
//			byte superlayer[] = DC.superlayer();
//			byte layer[] = DC.layer();
//			short wire[] = DC.wire();
//			int pid[] = DC.pid();
//			
//			for (int i = 0; i < hitCount; i++) {
//				int sect1 = sector[i]; // 1 based
//				int supl1 = superlayer[i]; // 1 based
//
//				if ((sect1 == _sector) && (supl1 == _superLayer)) {
//					int lay1 = layer[i]; // 1 based
//					int wire1 = wire[i]; // 1 based
//					
//					boolean noise = false;
//					if (_noiseManager.getNoise() != null) {
//						noise = _noiseManager.getNoise()[i];
//					}
//
//					int pdgid = (pid == null) ? -1
//							: pid[i];
//					drawDCHit(g, container, lay1, wire1, noise, pdgid, wr);
//				}
//			} // for
//
//		}

	}

	/**
	 * Highlight a noise hit
	 * 
	 * @param g         the graphics context
	 * @param container the rendering container
	 * @param dchit     the wire hit object
	 * @param simple    if <code>true</code> use simple highlighting
	 */
	private void highlightNoiseHit(Graphics g, IContainer container, boolean simple, Rectangle2D.Double cell) {
		if (simple) {
			WorldGraphicsUtilities.drawWorldRectangle(g, container, cell, Color.black, Color.black);
		} else {
			WorldGraphicsUtilities.drawWorldRectangle(g, container, cell, Color.black, Color.black);

			double x0 = cell.x;
			double x1 = cell.getMaxX();
			double y0 = cell.y;
			double y1 = cell.getMaxY();

			WorldGraphicsUtilities.drawWorldLine(g, container, x0, y0, x1, y1, Color.gray);
			WorldGraphicsUtilities.drawWorldLine(g, container, x0, y1, x1, y0, Color.gray);

			WorldGraphicsUtilities.drawWorldRectangle(g, container, cell, null, Color.black);
		}
	}

	/**
	 * Draw a single dc hit
	 * 
	 * @param g         the graphics context
	 * @param container the rendering container
	 * @param layer     a 1-based layer
	 * @param wire      the 1-based wire
	 * @param noise     is this marked as a noise hit
	 * @param pid       the gemc pid
	 * @param wr        workspace
	 */
	private void drawDCRawHit(Graphics g, IContainer container, int layer, int wire, boolean noise, int pid,
			Rectangle2D.Double wr) {

		if (wire > GeoConstants.NUM_WIRE) {
			String msg = "Bad wire number in drawDCHit " + wire + " seq event number " + _eventManager.getSequentialEventNumber();
			Log.getInstance().warning(msg);
			System.err.println(msg);
			return;
		}

		// abort if hiding noise and this is noise
		if (_view.hideNoise() && noise) {
			return;
		}

		getCell(layer, wire, wr);

		// are we to show mc (MonteCarlo simulation) truth?
		boolean showTruth = _view.showMcTruth();

		Color hitFill = _defaultHitCellFill;
		Color hitLine = _defaultHitCellLine;

		// do we have simulated "truth" data?
		if (showTruth && (pid >= 0)) {
			LundId lid = LundSupport.getInstance().get(pid);
			if (lid != null) {
				LundStyle style = lid.getStyle();
				if (style != null) {
					hitFill = lid.getStyle().getFillColor();
					hitLine = hitFill.darker();
				}
			}
		} // end gemcData != null

		if ((_view.showNoiseAnalysis()) && noise) {
			highlightNoiseHit(g, container, !showTruth, wr);
		} else {
			WorldGraphicsUtilities.drawWorldRectangle(g, container, wr, hitFill, hitLine);
		}
		
		_lastColor = hitFill;
		
	}
	

	/**
	 * Draw a single dc hit
	 * 
	 * @param g         the graphics context
	 * @param container the rendering container
	 * @param layer     a 1-based layer
	 * @param wire      the 1-based wire
	 * @param noise     is this marked as a noise hit
	 * @param pid       the gemc pid
	 * @param pr        workspace
	 * @param wr        workspace
	 * @param color     color
	 * @param option    0 for rect, 1 for X, 2 for +
	 */
	private void drawDCHit(Graphics g, IContainer container, int layer, int wire, 
			Rectangle pr, Rectangle2D.Double wr, Color color, int option) {

		if (wire > GeoConstants.NUM_WIRE) {
			String msg = "Bad wire number in drawDCHit " + wire + " seq event number " + _eventManager.getSequentialEventNumber();
			Log.getInstance().warning(msg);
			System.err.println(msg);
			return;
		}


		getCell(layer, wire, wr);
		if (option == 1) { //draw X
			container.worldToLocal(pr, wr);
			g.setColor(color);
			
			int l = pr.x;
			int t = pr.y;
			int r = pr.x + pr.width;
			int b = pr.y + pr.height;
			
			g.drawLine(l, t, r, b);
			g.drawLine(r, t, l, b);	
			g.setColor(_defaultHitCellLine);
			g.drawRect(l, t, pr.width, pr.height);
		}
		else if (option == 2) { //draw cross
			container.worldToLocal(pr, wr);
			g.setColor(color);
			
			int l = pr.x;
			int t = pr.y;
			int r = pr.x + pr.width;
			int b = pr.y + pr.height;
			int xc = (l + r)/2;
			int yc = (t + b)/2;
			g.drawLine(l, yc, r, yc);
			g.drawLine(xc, t, xc, b);
			g.setColor(_defaultHitCellLine);
			g.drawRect(l, t, pr.width, pr.height);
						
		} else {
			WorldGraphicsUtilities.drawWorldRectangle(g, container, wr, color, _defaultHitCellLine);
		}
		_lastColor  = color;
	}
	
	/**
	 * Draw a single nn marked hit
	 * 
	 * @param g         the graphics context
	 * @param container the rendering container
	 * @param dcHit     a dc hit object
	 * @param wire      the 1-based wire
	 * @param noise     is this marked as a noise hit
	 * @param pid       the gemc pid
	 * @param nn        is this a hit also marked by nnet?
	 * @param wr        workspace
	 */
	private void drawDCNNHit(Graphics g, IContainer container, int layer, int wire, Rectangle2D.Double wr) {

		if (wire > GeoConstants.NUM_WIRE) {
			String msg = "Bad wire number in drawDCNNHit " + wire + " event number " + _eventManager.getSequentialEventNumber();
			Log.getInstance().warning(msg);
			System.err.println(msg);
			return;
		}

		getCell(layer, wire, wr);


		WorldGraphicsUtilities.drawWorldOval(g, container, wr, CedColors.NN_COLOR, _lastColor);
	}


	/**
	 * Draw the masks showing the effect of the noise finding algorithm
	 * 
	 * @param g          the graphics context
	 * @param container  the rendering container
	 * @param parameters the noise algorithm parameters
	 */
	private void drawMasks(Graphics g, IContainer container, NoiseReductionParameters parameters) {

		Rectangle2D.Double wr = new Rectangle2D.Double();

		for (int wire = 0; wire < parameters.getNumWire(); wire++) {
			boolean leftSeg = parameters.getLeftSegments().checkBit(wire);
			boolean rightSeg = parameters.getRightSegments().checkBit(wire);
			if (leftSeg || rightSeg) {
				if (leftSeg) {
					drawMask(g, container, wire, parameters.getLeftLayerShifts(), 1, wr);
				}
				if (rightSeg) {
					drawMask(g, container, wire, parameters.getRightLayerShifts(), -1, wr);
				}
			}
		}
	}

	/**
	 * Draws the masking that shows where the noise algorithm thinks there are
	 * segments. Anything not masked is noise.
	 * 
	 * @param g         the graphics context.
	 * @param container the rendering container
	 * @param wire      the ZERO BASED wire 0..
	 * @param shifts    the parameter shifts for this direction
	 * @param sign      the direction 1 for left -1 for right
	 * @param wr        essentially workspace
	 */
	private void drawMask(Graphics g, IContainer container, int wire, int shifts[], int sign, Rectangle2D.Double wr) {

		wire++; // convert to 1-based

		Color fill;
		if (sign == 1) {
			fill = NoiseManager.maskFillLeft;
		} else {
			fill = NoiseManager.maskFillRight;
		}

		for (int layer = 1; layer <= GeoConstants.NUM_LAYER; layer++) {
			getCell(layer, wire, wr);
			WorldGraphicsUtilities.drawWorldRectangle(g, container, wr, fill, null);

			// ugh -- shifts are 0-based
			for (int shift = 1; shift <= shifts[layer - 1]; shift++) {
				int tempWire = wire + sign * shift;
				if ((tempWire > 0) && (tempWire <= GeoConstants.NUM_WIRE)) {
					getCell(layer, tempWire, wr);
					WorldGraphicsUtilities.drawWorldRectangle(g, container, wr, fill, null);
				}
			}
		}

	}

	/**
	 * Draw hits in accumulated mode
	 * 
	 * @param g         the graphics context
	 * @param container the rendering container
	 */
	private void accumulatedDrawItem(Graphics g, IContainer container) {

		Rectangle2D.Double wr = new Rectangle2D.Double(); // used over and over
		int dcAccumulatedData[][][][] = AccumulationManager.getInstance().getAccumulatedDCData();

		int medianHit = AccumulationManager.getInstance().getMedianDCCount(_superLayer - 1);

		for (int layer = 0; layer < GeoConstants.NUM_LAYER; layer++) {
			for (int wire = 0; wire < GeoConstants.NUM_WIRE; wire++) {
				int hitCount = dcAccumulatedData[_sector - 1][_superLayer - 1][layer][wire];
				getCell(layer + 1, wire + 1, wr);

				double fract = _view.getMedianSetting() * (((double) hitCount) / (1 + medianHit));

				AccumulationManager.getInstance();
				Color color = AccumulationManager.getInstance().getColor(_view.getColorScaleModel(), fract);

				if (color.getRed() > color.getBlue()) {
					System.out.println("Dude!");
				}

				WorldGraphicsUtilities.drawWorldRectangle(g, container, wr, color, color, 1, LineStyle.SOLID);

			}
		}
	}

	/**
	 * Add any appropriate feedback strings panel.
	 * 
	 * @param container       the Base container.
	 * @param screenPoint     the mouse location.
	 * @param worldPoint      the corresponding world point.
	 * @param feedbackStrings the List of feedback strings to add to.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings) {
		if (_worldRectangle.contains(worldPoint)) {

			int layer = getLayer(worldPoint); // 1-based

			int wire = getWire(worldPoint); // 1-based

			// report approximate position
			// for now nearest wire--could interpolate
			if ((wire > 0) && (wire <= 112)) {

				Point3D midPoint = DCGeometry.getMidPoint(_superLayer, layer, wire);

				double x = midPoint.x();
				double y = midPoint.y();
				double z = midPoint.z();

				double rho = x * x + y * y;
				double r = Math.sqrt(rho + z * z);
				rho = Math.sqrt(rho);
				double theta = Math.toDegrees(Math.atan2(rho, z));

				// get absolute phi
				double phi = (_sector - 1) * 60.0;

				String rtp = "approx " + CedView.rThetaPhi + " " + DoubleFormat.doubleFormat(r, 1) + "cm, "
						+ DoubleFormat.doubleFormat(theta, 1) + UnicodeSupport.DEGREE + ", "
						+ DoubleFormat.doubleFormat(phi, 1) + UnicodeSupport.DEGREE;
				feedbackStrings.add(rtp);

			}

			if (_view.isSingleEventMode()) {
				singleEventFeedbackStrings(wire, layer, feedbackStrings);
			} else {
				accumulatedFeedbackStrings(wire, layer, feedbackStrings);
			}

		} // end contains
	}

	/**
	 * Get the feedback strings for single event mode
	 * 
	 * @param wire            [1..6]
	 * @param layer           [1..6]
	 * @param feedbackStrings
	 */
	private void singleEventFeedbackStrings(int wire, int layer, List<String> feedbackStrings) {

		// some occupancy numbers
		NoiseReductionParameters parameters = _noiseManager.getParameters(_sector - 1, _superLayer - 1);

		feedbackStrings.add(DataSupport.prelimColor + "Raw Superlayer Occ "
				+ DoubleFormat.doubleFormat(100.0 * parameters.getRawOccupancy(), 2) + "%");
		feedbackStrings.add(DataSupport.prelimColor + "Reduced Superlayer Occ "
				+ DoubleFormat.doubleFormat(100.0 * parameters.getNoiseReducedOccupancy(), 2) + "%");

		DCTdcHitList hits = DC.getInstance().getTDCHits();

		DCTdcHit hit = null;
		if ((hits != null) && !hits.isEmpty()) {
			hit = hits.getHit(_sector, _superLayer, layer, wire);
		}

		if (hit == null) {
			feedbackStrings.add("superlayer " + _superLayer + "  layer " + layer + "  wire " + wire);
		} else {
			hit.tdcAdcFeedback(_view.showNoiseAnalysis(), _view.showMcTruth(), feedbackStrings);
		}
	}

	/**
	 * Get the feedback strings for single event mode
	 * 
	 * @param wire            [1..6]
	 * @param layer           [1..6]
	 * @param feedbackStrings
	 */
	private void accumulatedFeedbackStrings(int wire, int layer, List<String> feedbackStrings) {

		int dcAccumulatedData[][][][] = AccumulationManager.getInstance().getAccumulatedDCData();

		double wireRate = AccumulationManager.getInstance().getAccumulatedWireHitPercentage(_sector - 1,
				_superLayer - 1, layer - 1, wire - 1);
		double avgOccupancy = AccumulationManager.getInstance().getAverageDCOccupancy(_sector - 1, _superLayer - 1);

		int hitCount = dcAccumulatedData[_sector - 1][_superLayer - 1][layer - 1][wire - 1];

		feedbackStrings.add(AccumulationManager.accumulationFBColor + "accumulated event count: "
				+ AccumulationManager.getInstance().getAccumulationEventCount());
		feedbackStrings.add(AccumulationManager.accumulationFBColor + "avg occupancy superlayer: " + _superLayer
				+ " is " + DoubleFormat.doubleFormat(100 * avgOccupancy, 3) + "%");
		feedbackStrings.add(AccumulationManager.accumulationFBColor + "hit rate layer: " + layer + ", wire: " + wire
				+ " is " + DoubleFormat.doubleFormat(wireRate, 3) + "%");
		feedbackStrings.add(AccumulationManager.accumulationFBColor + "hit count layer: " + layer + ", wire: " + wire
				+ " is " + hitCount);

	}

	/**
	 * For the given world point return the 1-based layer.
	 * 
	 * @param worldPoint the point in question
	 * @return the layer [1..6]
	 */
	private int getLayer(Point2D.Double worldPoint) {
		if (_worldRectangle.contains(worldPoint)) {
			for (int i = 0; i < GeoConstants.NUM_LAYER; i++) {
				if (_layerWorldRects[i].contains(worldPoint)) {
					return i + 1; // convert to 1-based
				}
			}
		}

		return -1;
	}

	/**
	 * For the given world point return the 1-based wire.
	 * 
	 * @param worldPoint the point in question
	 * @return the wire [1..]
	 */
	private int getWire(Point2D.Double worldPoint) {
		if (_worldRectangle.contains(worldPoint)) {
			for (int i = 0; i < _numWires; i++) {
				if (_positionWorldRects[i].contains(worldPoint)) {
					return i + 1; // convert to 1-based
				}
			}
		}

		return -1;
	}

	/**
	 * Get the world rectangle for a given cell (the wire is in the center)
	 * 
	 * @param layer the 1-based layer [1..6]
	 * @param wire  the 1-based wire [1..] return the world rectangle cell for this
	 *              layer, wire
	 */
	public void getCell(int layer, int wire, Rectangle2D.Double wr) {

		int lm1 = layer - 1;
		int wm1 = wire - 1;

		Rectangle2D.Double layerRect = _layerWorldRects[lm1];
		Rectangle2D.Double positionRect = _positionWorldRects[wm1];
		wr.setFrame(positionRect.x, layerRect.y, positionRect.width, layerRect.height);

	}

}
