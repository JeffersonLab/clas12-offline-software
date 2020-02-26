package cnuphys.fastMCed.item;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.jlab.geom.DetectorId;
import org.jlab.geom.prim.Point3D;

//import cnuphys.ced.dcnoise.NoiseEventListener;
//import cnuphys.ced.dcnoise.NoiseReductionParameters;
//import cnuphys.ced.dcnoise.test.TestParameters;
import cnuphys.lund.LundId;
import cnuphys.lund.LundStyle;
import cnuphys.snr.NoiseReductionParameters;
import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.item.RectangleItem;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.fastMCed.eventio.PhysicsEventManager;
import cnuphys.fastMCed.fastmc.AugmentedDetectorHit;
import cnuphys.fastMCed.fastmc.ParticleHits;
import cnuphys.fastMCed.geometry.DCGeometry;
import cnuphys.fastMCed.snr.SNRManager;
import cnuphys.fastMCed.streaming.StreamManager;
import cnuphys.fastMCed.view.AView;
import cnuphys.fastMCed.view.alldc.AllDCView;

public class AllDCSuperLayer extends RectangleItem {
	
	// convenient access to the event manager
	private PhysicsEventManager _eventManager = PhysicsEventManager.getInstance();


	// font for label text
	private static final Font labelFont = Fonts.commonFont(Font.PLAIN, 11);

	// the sector [1..6]
	private int _sector;

	// the super layer [1..6]
	private int _superLayer;

	// cell overlay transparent color
	private static final Color cellOverlayColor = new Color(180, 180, 180, 64);

	// the number of wires per layer
	private int _numWires;

	// the AllDC view this item lives on
	private AllDCView _view;

	private static final int NUM_LAYER = 6;
	private static final int NUM_SUPERLAYER = 6;
	private static final int NUM_WIRE = 112;
	
	// for hits cells
	private static final Color _defaultHitCellFill = Color.red;
	private static final Color _defaultHitCellLine = X11Colors
			.getX11Color("Dark Red");

	// this is the world rectangle that defines the super layer
	private Rectangle2D.Double _worldRectangle;

	// cache the layer world rectangles
	private Rectangle2D.Double _layerWorldRects[] = new Rectangle2D.Double[NUM_LAYER];

	// cache the "position" rects which span the superlayer. That is, a wire
	// cell
	// is the intersection of the layer rect and the position rect
	private Rectangle2D.Double _positionWorldRects[];

	/**
	 * Constructor for a geometrically unfaithful "all dc" superlayer.
	 * 
	 * @param layer
	 *            the Layer this item is on.
	 * @param view
	 *            the AllDCView parent
	 * @param worldRectangle
	 *            the boundaries which are not the real boundaries.
	 * @param sector
	 *            the sector [0..5]
	 * @param superLayer
	 *            the superLayer [0..5]
	 * @param numWires
	 *            the number of wires per layer
	 */
	public AllDCSuperLayer(LogicalLayer layer, AllDCView view,
			Rectangle2D.Double worldRectangle, int sector, int superLayer,
			int numWires) {
		super(layer, worldRectangle);
		_worldRectangle = worldRectangle;
		_view = view;
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
		double dy = _worldRectangle.height / NUM_LAYER;
		double x = _worldRectangle.x;
		double y = _worldRectangle.y;
		double w = _worldRectangle.width;

		for (int i = 0; i < NUM_SUPERLAYER; i++) {
			// trick to invert layers in lower sector
			int recIndex = (_sector < 4) ? i
					: (NUM_SUPERLAYER - i - 1);
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
			_positionWorldRects[_numWires - i - 1] = new Rectangle2D.Double(x,
					y, dx, h);
			x += dx;
		}
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

		if (StreamManager.getInstance().isStarted()) {
			return;
		}

		// System.err.println("All DC SuperLayer DIRTY: " + isDirty());
		super.drawItem(g, container); // draws rectangular shell
	

		double left = _worldRectangle.x;
		double top = _worldRectangle.y;
		g.setFont(labelFont);
		g.setColor(Color.cyan);
		WorldGraphicsUtilities.drawWorldText(g, container, left, top, ""
				+ _superLayer, -9, -5);

		//draw SNR masks?
		if (_view.showMasks()) {
			drawSNRMasks(g, container);
		}
		
		// now the data
		drawHitData(g, container);
		// shade the layers
		for (int i = 0; i < NUM_LAYER; i += 2) {
			WorldGraphicsUtilities.drawWorldRectangle(g, container,
					_layerWorldRects[i], cellOverlayColor, null);

		}

		// causes cell shading
		for (int i = 0; i < _numWires; i += 2) {
			WorldGraphicsUtilities.drawWorldRectangle(g, container,
					_positionWorldRects[i], cellOverlayColor, null);

		}

		// just to make clean
		g.setColor(_style.getLineColor());
		g.drawPolygon(_lastDrawnPolygon);
	}
	
	//draw the SNR mask data where SNR thinks segments mught start
	private void drawSNRMasks(Graphics g, IContainer container) {
		//need zero based sector and super layer
		NoiseReductionParameters parameters = SNRManager.getInstance().getParameters(
				_sector - 1, _superLayer - 1);

		Rectangle2D.Double wr = new Rectangle2D.Double();

		for (int wire = 0; wire < parameters.getNumWire(); wire++) {
			//where it thinks segments start
			boolean leftSeg = parameters.getLeftSegments().checkBit(wire);
			boolean rightSeg = parameters.getRightSegments().checkBit(wire);
			if (leftSeg || rightSeg) {
				if (leftSeg) {
					drawMask(g, container, wire,
							parameters.getLeftLayerShifts(), 1, wr);
				}
				if (rightSeg) {
					drawMask(g, container, wire,
							parameters.getRightLayerShifts(), -1, wr);
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
	 *            the direction 1 for left -1 for right
	 * @param wr
	 *            essentially workspace
	 */
	private void drawMask(Graphics g, IContainer container, int wire,
			int shifts[], int sign, Rectangle2D.Double wr) {

		wire++; // convert to 1-based

		Color fill;
		if (sign == 1) {
			fill = SNRManager.maskFillLeft;
		} else {
			fill = SNRManager.maskFillRight;
		}

		for (int layer = 1; layer <= NUM_LAYER; layer++) {
			getCell(layer, wire, wr);
			WorldGraphicsUtilities.drawWorldRectangle(g, container, wr, fill,
					null);

			// ugh -- shifts are 0-based
			for (int shift = 1; shift <= shifts[layer - 1]; shift++) {
				int tempWire = wire + sign * shift;
				if ((tempWire > 0) && (tempWire <= NUM_WIRE)) {
					getCell(layer, tempWire, wr);
					WorldGraphicsUtilities.drawWorldRectangle(g, container, wr,
							fill, null);
				}
			}
		}

	}

	/**
	 * Draw in single event mode
	 * 
	 * @param g
	 *            the graphics context
	 * @param container
	 *            the rendering container
	 */
	private void drawHitData(Graphics g, IContainer container) {

		Rectangle2D.Double wr = new Rectangle2D.Double(); // used over and over

		List<ParticleHits> hits = _eventManager.getParticleHits();

		if (hits != null) {
			for (ParticleHits particleHits : hits) { //essentially a loop over tracks
				LundId lid = particleHits.getLundId();

				List<AugmentedDetectorHit> augHits = particleHits.getHits(DetectorId.DC, _sector-1, _superLayer-1);

				if (augHits != null) {
					for (AugmentedDetectorHit hit : augHits) {

						// NUMBERS COMING OUT OF DETECTOR HIT ARE 0-BASED

						// get 1-based
						int layer = hit.getLayerId() + 1;
						int wire = hit.getComponentId() + 1;

						drawDCHit(g, container, layer, wire, hit.isNoise(), lid, wr);
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
	 * @param dcHit
	 *            a dc hit object
	 * @param wire
	 *            the 1-based wire
	 * @param noise
	 *            is this marked as a noise hit
	 * @param pid
	 *            the gemc pid
	 * @param wr
	 *            workspace
	 */
	private void drawDCHit(Graphics g, IContainer container, int layer,
			int wire, boolean noise, LundId lid, Rectangle2D.Double wr) {
		
		//might not even draw it if it is noise
		if (noise && _view.hideNoise()) {
			return;
		}

		if (wire > NUM_WIRE) {
			String msg = "Bad wire number in drawGemcDCHit " + wire
					+ " event number " + _eventManager.eventNumber();
			Log.getInstance().warning(msg);
			System.err.println(msg);
			return;
		}

		getCell(layer, wire, wr);


		Color hitFill = _defaultHitCellFill;
		Color hitLine = _defaultHitCellLine;

		// do we have simulated "truth" data?
		if (lid != null) {
			LundStyle style = lid.getStyle();
			if (style != null) {
				hitFill = lid.getStyle().getFillColor();
				hitLine = hitFill.darker();
			}
		}
		
		if (noise && _view.showNoiseAnalysis()) {
			hitFill = Color.black;
		}


		WorldGraphicsUtilities.drawWorldRectangle(g, container, wr,
				hitFill, hitLine);
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
	@Override
	public void getFeedbackStrings(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, List<String> feedbackStrings) {
		
		if (StreamManager.getInstance().isStarted()) {
			return;
		}

		if (_worldRectangle.contains(worldPoint)) {

			int layer = getLayer(worldPoint); // 1-based

			int wire = getWire(worldPoint); // 1-based
			
			feedbackStrings.add("Superlayer " + _superLayer + "  Layer "
					+ layer + "  Wire " + wire);


			// report approximate position
			// for now nearest wire--could interpolate
			if ((wire > 0) && (wire <= 112)) {
				
				Point3D midPoint = DCGeometry.getMidPoint(_superLayer, layer,
						wire);

				double x = midPoint.x();
				double y = midPoint.y();
				double z = midPoint.z();

				double rho = x * x + y * y;
				double r = Math.sqrt(rho + z * z);
				rho = Math.sqrt(rho);
				double theta = Math.toDegrees(Math.atan2(rho, z));

				// get absolute phi
				double phi = (_sector - 1) * 60.0;

				String rtp = "approx " + AView.rThetaPhi + " "
						+ DoubleFormat.doubleFormat(r, 1) + "cm, "
						+ DoubleFormat.doubleFormat(theta, 1)
						+ UnicodeSupport.DEGREE + ", "
						+ DoubleFormat.doubleFormat(phi, 1)
						+ UnicodeSupport.DEGREE;
				feedbackStrings.add(rtp);

			}


			singleEventFeedbackStrings(layer, wire, feedbackStrings);

		} // end contains
	}

	/**
	 * Get the feedback strings for single event mode
	 * 
	 * @param layer
	 *            [1..6]
	 * @param wire
	 *            [1..112]
	 * @param feedbackStrings
	 */
	private void singleEventFeedbackStrings(int layer, int wire,
			List<String> feedbackStrings) {
		
		
		List<ParticleHits> hits = _eventManager.getParticleHits();
		int wire0 = wire - 1;

		if (hits != null) {
			for (ParticleHits particleHits : hits) {
				LundId lid = particleHits.getLundId();

				List<AugmentedDetectorHit> augHits = particleHits.getHits(DetectorId.DC, _sector - 1, _superLayer - 1);

				if (augHits != null) {
					for (AugmentedDetectorHit hit : augHits) {
						if (hit.getComponentId() == wire0) {

							// might not even care if it is noise
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
		
		SNRManager.getInstance().addParametersToFeedback(_sector, _superLayer, feedbackStrings);
		
//		feedbackStrings.add(DataSupport.prelimColor
//				+ "Raw Superlayer Occ "
//				+ DoubleFormat.doubleFormat(
//						100.0 * parameters.getRawOccupancy(), 2) + "%");
//		feedbackStrings
//				.add(DataSupport.prelimColor
//						+ "Reduced Superlayer Occ "
//						+ DoubleFormat.doubleFormat(
//								100.0 * parameters.getNoiseReducedOccupancy(),
//								2) + "%");
//		
//		DCTdcHitList hits = DC.getInstance().getTDCHits();
//
//		DCTdcHit hit = null;
//		if ((hits != null) && !hits.isEmpty()) {
//			hit = hits.getHit(_sector, _superLayer, layer, wire);
//		}
//		
//		if (hit == null) {
//			
//		}
//		else {
//			hit.tdcAdcFeedback(_view.showNoiseAnalysis(), _view.showMcTruth(), feedbackStrings);
//		}
	}
		

	/**
	 * For the given world point return the 1-based layer.
	 * 
	 * @param worldPoint
	 *            the point in question
	 * @return the layer [1..6]
	 */
	private int getLayer(Point2D.Double worldPoint) {
		if (_worldRectangle.contains(worldPoint)) {
			for (int i = 0; i < NUM_LAYER; i++) {
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
	 * @param worldPoint
	 *            the point in question
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
	 * @param layer
	 *            the 1-based layer [1..6]
	 * @param wire
	 *            the 1-based wire [1..] return the world rectangle cell for
	 *            this layer, wire
	 */
	public void getCell(int layer, int wire, Rectangle2D.Double wr) {

		int lm1 = layer - 1;
		int wm1 = wire - 1;

		Rectangle2D.Double layerRect = _layerWorldRects[lm1];
		Rectangle2D.Double positionRect = _positionWorldRects[wm1];
		wr.setFrame(positionRect.x, layerRect.y, positionRect.width,
				layerRect.height);

	}

}