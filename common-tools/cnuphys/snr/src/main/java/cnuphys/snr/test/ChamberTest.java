package cnuphys.snr.test;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import cnuphys.snr.ExtendedWord;
import cnuphys.snr.NoiseReductionParameters;

public class ChamberTest {

    public static final Color maskFillLeft = new Color(255, 128, 0, 48);
    public static final Color maskFillRight = new Color(0, 128, 255, 48);
    public static final Color almostTransparent = new Color(0, 0, 0, 16);

    protected Color fillColor = new Color(244, 244, 244);
    protected Color lineColor = Color.gray;

    private static final Font _smallFont = new Font("SanSerif", Font.BOLD, 9);
    /**
     * The parent detector that presumably contains multiple chambers.
     */
    protected DetectorTest _detectorTest;

    /**
     * A world coordinate rectangular boundary.
     */
    protected Rectangle2D.Double _boundary;

    /**
     * The name of the chamber.
     */
    protected String _name;

    /**
     * A collection of all the hits.
     */
    protected Vector<HitTest> hits = new Vector<HitTest>(100);

    /**
     * Parameters for testing noise reduction. chambers.
     */
    protected NoiseReductionParameters _parameters;

    /**
     * Data in extended words for noise reduction
     */
    protected ExtendedWord data[];

    public int index;

    /**
     * Create a test chamber--rectangular with uniform rectangular cells.
     * 
     * @param index
     *            the index in the DetectorTest collection
     * @param name
     *            the name of the chamber.
     * @param boundary
     *            the rectangular boundary in world coordinates.
     */
    public ChamberTest(DetectorTest detectorTest, int index,
	    NoiseReductionParameters parameters, Rectangle2D.Double boundary) {
	_detectorTest = detectorTest;
	_name = "Superlayer " + (index + 1);
	_boundary = boundary;
	_parameters = parameters;
	this.index = index;
	initializeSpace();
    }

    /**
     * Draw the chamber.
     * 
     * @param g
     *            the Graphics context.
     * @param world
     *            the world system
     * @param local
     *            the local system
     */
    public void draw(Graphics g, Rectangle2D.Double world, Rectangle local) {

	Rectangle cell = new Rectangle();
	drawCellOutlines(g, world, local, fillColor);

	drawString(g, world, local, _name, _boundary.x, _boundary.y
		+ _boundary.height);

	// mark segments

	if (_detectorTest.getDisplayOption() == DetectorTest.DisplayOption.ANALYZED) {

	    for (int wire = 0; wire < _parameters.getNumWire(); wire++) {
		boolean left = _parameters.getLeftSegments().checkBit(wire);
		boolean right = _parameters.getRightSegments().checkBit(wire);
		if (left || right) {
		    cellBounds(world, local, 0, wire, cell);

		    if (left) {
			drawMask(g, world, local, wire,
				_parameters.getLeftLayerShifts(), 1);
		    }
		    if (right) {
			drawMask(g, world, local, wire,
				_parameters.getRightLayerShifts(), -1);
		    }

		}
	    }
	} // end analyzed

	// draw hits
	for (HitTest th : hits) {
	    cellBounds(world, local, th.getLayer(), th.getWire(), cell);
	    g.setColor(getHitColor(th));
	    g.fillRect(cell.x, cell.y, cell.width, cell.height);
	    g.setColor(lineColor);
	    g.drawRect(cell.x, cell.y, cell.width, cell.height);

	    if (_detectorTest.getDisplayOption() == DetectorTest.DisplayOption.ANALYZED) {

		// is it a noise hit that is being preserved?
		if (th.getComputedHitType() == HitTest.HitType.TRACK
			&& th.getActualHitType() == HitTest.HitType.NOISE) {
		    g.setColor(Color.blue);
		    g.drawRect(cell.x + 1, cell.y + 1, cell.width - 2,
			    cell.height - 2);
		    g.setColor(Color.darkGray);
		    g.drawRect(cell.x + 2, cell.y + 2, cell.width - 4,
			    cell.height - 4);
		}
	    }
	}
    }

    protected void drawString(Graphics g, Rectangle2D.Double world,
	    Rectangle local, String s, double wx, double wy) {
	Point pp = new Point();
	TestSupport.toLocal(world, local, pp, wx, wy);
	g.setFont(_smallFont);
	g.setColor(Color.blue);
	g.drawString(s, pp.x, pp.y - 2);
    }

    // Draw the mask
    protected void drawMask(Graphics g, Rectangle2D.Double world,
	    Rectangle local, int wire, int shifts[], int sign) {
	if (sign == 1) {
	    g.setColor(maskFillLeft);
	} else {
	    g.setColor(maskFillRight);
	}

	Rectangle tr = new Rectangle();

	for (int layer = 0; layer < _parameters.getNumLayer(); layer++) {
	    cellBounds(world, local, layer, wire, tr);
	    g.fillRect(tr.x, tr.y, tr.width, tr.height);
	    for (int shift = 1; shift <= shifts[layer]; shift++) {
		int tempWire = wire + sign * shift;
		if (tempWire >= 0 && (tempWire < _parameters.getNumWire()))
		    cellBounds(world, local, layer, tempWire, tr);
		g.fillRect(tr.x, tr.y, tr.width, tr.height);
	    }
	}

    }

    /**
     * Draw all the cell outlines efficiently.
     * 
     * @param g
     *            the graphics context.
     * @param world
     *            the world system.
     * @param local
     *            the local system.
     * @param fc
     *            the fill color, can be <code>null</code>.
     */
    protected void drawCellOutlines(Graphics g, Rectangle2D.Double world,
	    Rectangle local, Color fc) {
	Rectangle cell = new Rectangle();

	cellBounds(world, local, 0, 0, cell); // bottom-right
	int bottom = cell.y + cell.height;
	int right = cell.x + cell.width;

	cellBounds(world, local, _parameters.getNumLayer() - 1,
		_parameters.getNumWire() - 1, cell); // top-left
	int top = cell.y;
	int left = cell.x;

	int width = right - left;
	int height = bottom - top;

	if (fc != null) {
	    g.setColor(fc);
	    g.fillRect(left, top, width, height);
	}

	g.setColor(lineColor);
	g.drawRect(left, top, width, height);

	for (int wire = 0; wire < (_parameters.getNumWire() - 1); wire++) {
	    cellBounds(world, local, 0, wire, cell);
	    g.drawLine(cell.x, bottom, cell.x, top);
	}

	for (int layer = 0; layer < (_parameters.getNumLayer() - 1); layer++) {
	    cellBounds(world, local, layer, 0, cell);
	    g.drawLine(left, cell.y, right, cell.y);
	}

    }

    /**
     * Get the color for a hit
     * 
     * @param ht
     *            the hit in question
     * @return the fill color to use.
     */
    protected Color getHitColor(HitTest ht) {
	boolean noNoise = TestParameters.noiseOff; // no noise == cleaned data

	if (_detectorTest.getDisplayOption() == DetectorTest.DisplayOption.REALITY) {
	    if (ht.getActualHitType() == HitTest.HitType.NOISE) {
		return noNoise ? almostTransparent : TestParameters
			.getRealityNoiseColor();
	    }
	    if (ht.getActualHitType() == HitTest.HitType.TRACK) {
		return TestParameters.getRealityTrackColor();
	    }
	} else if (_detectorTest.getDisplayOption() == DetectorTest.DisplayOption.DATA) {
	    return TestParameters.getGenericHitColor();
	} else if (_detectorTest.getDisplayOption() == DetectorTest.DisplayOption.ANALYZED) {
	    if (ht.getComputedHitType() == HitTest.HitType.TRACK
		    && ht.getActualHitType() == HitTest.HitType.NOISE) {
		return TestParameters.getSavedNoiseColor();
	    } else if (ht.getComputedHitType() == HitTest.HitType.NOISE) {
		return noNoise ? almostTransparent : TestParameters
			.getAnalyzedNoiseColor();
	    } else if (ht.getComputedHitType() == HitTest.HitType.TRACK) {
		return TestParameters.getAnalyzedTrackColor();
	    }
	}
	return Color.yellow;

    }

    /**
     * Fill in a pixel rectangle with the cell pixel.
     * 
     * @param world
     *            the world system.
     * @param local
     *            the local system.
     * @param layer
     *            the layer index [0..]
     * @param wire
     *            the wire index [0..]
     * @param cell
     *            the Rectangle that will be set to the cell boundary.
     */
    protected void cellBounds(Rectangle2D.Double world, Rectangle local,
	    int layer, int wire, Rectangle cell) {
	Rectangle2D.Double wr = new Rectangle2D.Double();
	cellWorldBounds(layer, wire, wr);
	TestSupport.toLocal(world, local, cell, wr);
    }

    /**
     * Get the world boundary of a cell.
     * 
     * @param layer
     *            the layer index [0..]
     * @param wire
     *            the wire index [0..]
     * @param wr
     *            will be set to the cell boundary.
     */
    protected void cellWorldBounds(int layer, int wire, Rectangle2D.Double wr) {
	double dy = _boundary.height / _parameters.getNumLayer();
	double dx = _boundary.width / _parameters.getNumWire();
	// note layers counted from bottom, wires counted from right
	double xmin = _boundary.x + _boundary.width - dx * (wire + 1);
	double ymin = _boundary.y + dy * layer;
	wr.setFrame(xmin, ymin, dx, dy);
    }

    /**
     * Get the world boundary of a range of cells.
     * 
     * @param layer
     *            the layer index [0..]
     * @param minwire
     *            the min wire index [0..]
     * @param maxwire
     *            the max wire index [minwire..]
     * @param wr
     *            will be set to the cell range boundary.
     */
    protected void cellRangeWorldBounds(int layer, int minwire, int maxwire,
	    Rectangle2D.Double wr) {
	double dy = _boundary.height / _parameters.getNumLayer();
	double dx = _boundary.width / _parameters.getNumWire();
	int nc = maxwire - minwire + 1;
	// note layers counted from bottom, wires counted from right
	double xmin = _boundary.x + _boundary.width - dx * (maxwire + 1);
	double ymin = _boundary.y + dy * layer;
	wr.setFrame(xmin, ymin, nc * dx, dy);
    }

    /**
     * Get the world boundary of a layer.
     * 
     * @param layer
     *            the layer index [0..]
     * @param wr
     *            will be set to the layer boundary.
     */
    protected void layerWorldBounds(int layer, Rectangle2D.Double wr) {
	double dy = _boundary.height / _parameters.getNumLayer();
	// note layers counted from bottom
	double ymin = _boundary.y + dy * layer;
	wr.setFrame(_boundary.x, ymin, _boundary.width, dy);
    }

    /**
     * Get a mouse over feedback string.
     * 
     * @param pp
     *            the mouse location.
     * @param world
     *            the world system.
     * @param local
     *            the local system.
     * @return the feedback string, or null if not over any cell.
     */
    public String feedback(Point pp, Rectangle2D.Double world, Rectangle local) {
	StringBuilder sb = new StringBuilder(500);

	sb.append("[" + _name + "] ");

	if (!contains(pp, world, local)) {
	    return null;
	}
	Rectangle bounds = getBounds(world, local);

	int layer = -1;
	int wire = -1;

	int bottom = bounds.y + bounds.height;
	int right = bounds.x + bounds.width;

	for (int tlayer = 0; tlayer < _parameters.getNumLayer(); tlayer++) {
	    int ymin = bottom - (tlayer + 1) * bounds.height
		    / _parameters.getNumLayer();
	    if (pp.y > ymin) {
		layer = tlayer;
		// use non C indices
		sb.append("Layer " + (layer + 1) + " ");
		break;
	    }
	}

	for (int twire = 0; twire < _parameters.getNumWire(); twire++) {
	    int xmin = right - (twire + 1) * bounds.width
		    / _parameters.getNumWire();
	    if (pp.x > xmin) {
		wire = twire;
		// use non C indices
		sb.append("Wire " + (wire + 1) + " ");
		break;
	    }
	}

	if ((layer >= 0) && (wire >= 0)) {
	    HitTest ht = findHit(layer, wire);
	    if (ht == null) {
		sb.append("[No Hit] ");
	    } else {
		sb.append("  Reality: " + ht.getActualHitType()
			+ "  Computed: " + ht.getComputedHitType());
	    }
	}

	// occupancy
	sb.append(String.format("  #hits %d occ %4.1f%%", getNumHits(),
		100.0 * getOccupancy()));

	// allowed missing layers
	sb.append("  Missing Layers Allowed "
		+ _parameters.getAllowedMissingLayers());

	// layers shifts
	sb.append(shiftString("  Left Shifts", _parameters.getLeftLayerShifts()));
	sb.append(shiftString("  Right Shifts",
		_parameters.getRightLayerShifts()));

	return sb.toString();
    }

    protected String shiftString(String prompt, int shifts[]) {
	StringBuilder sb = new StringBuilder(100);
	sb.append(" ");
	sb.append(prompt);
	sb.append(" [");
	for (int i = 0; i < shifts.length; i++) {
	    sb.append(shifts[i]);
	    if (i != (shifts.length - 1)) {
		sb.append(", ");
	    }
	}
	sb.append("]");
	return sb.toString();
    }

    /**
     * Obtains the bounding rectangle.
     * 
     * @param world
     *            the world system.
     * @param local
     *            the local system.
     * @return the bounding screen rectangle.
     */
    public Rectangle getBounds(Rectangle2D.Double world, Rectangle local) {
	Rectangle r = new Rectangle();
	TestSupport.toLocal(world, local, r, _boundary);
	return r;
    }

    /**
     * See if the pixel point is within the screen boundary of the chamber.
     * 
     * @param pp
     *            the pixel point.
     * @param world
     *            the world system.
     * @param local
     *            the local system.
     * @return <code>true</code> if the point is conatined within the cjamber
     *         boundary.
     */
    public boolean contains(Point pp, Rectangle2D.Double world, Rectangle local) {
	Point2D.Double wp = new Point2D.Double();
	TestSupport.toWorld(world, local, pp, wp);
	return _boundary.contains(wp);
    }

    /**
     * Get the rectangular boundary of the chamber in world coordinates.
     * 
     * @return the rectangular boundary of the chamber in world coordinates.
     */
    public Rectangle2D.Double getBoundary() {
	return _boundary;
    }

    /**
     * Get the number of name in this chamber.
     * 
     * @return the name of layers.
     */
    public String getName() {
	return _name;
    }

    public void forceHit(int layerOneBased, int wireOneBased) {
	HitTest ht = createHit(layerOneBased - 1, wireOneBased - 1,
		HitTest.HitType.TRACK);
	hits.add(ht);
    }

    /**
     * Generate random noise
     */
    public void generateNoise() {
	for (int layer = 0; layer < _parameters.getNumLayer(); layer++) {
	    for (int wire = 0; wire < _parameters.getNumWire(); wire++) {
		if (Math.random() < TestParameters.getNoiseRate()) {
		    HitTest ht = createHit(layer, wire, HitTest.HitType.NOISE);
		    hits.add(ht);
		}
	    }

	}

	// add a blob?
	if (Math.random() < TestParameters.getProbBlob()) {
	    int blobSize = TestParameters.getBlobSize();

	    // random central wire
	    int ranWire = (int) (_parameters.getNumWire() * Math.random());
	    for (int layer = 0; layer < _parameters.getNumLayer(); layer++) {
		int minWire = Math.max(0, ranWire - blobSize);
		int maxWire = Math.min(_parameters.getNumWire() - 1, ranWire
			+ blobSize);
		for (int w = minWire; w <= maxWire; w++) {
		    if (Math.random() > TestParameters.getProbBadWire()) {
			HitTest ht = createHit(layer, w, HitTest.HitType.NOISE);
			hits.add(ht);
		    }
		}
	    } // layer loop
	}
    }

    /**
     * Load all the hits into the bit data for noise reduction.
     */
    public void loadBitData() {
	for (int i = 0; i < data.length; i++) {
	    data[i].clear();
	}

	for (HitTest ht : hits) {
	    data[ht.getLayer()].setBit(ht.getWire());
	}
    }

    /**
     * Apply the noise reduction algorithm.
     */
    public void removeNoise() {
	_parameters.createWorkSpace();
	_parameters.setPackedData(data);
	_parameters.removeNoise();

	// System.err.println("\n LEFT: " + _parameters.getLeftSegments());
	// System.err.println("RIGHT: " + _parameters.getRightSegments());
	//
	// stuff the corresponding layer in the composite chambers
	_detectorTest.getCompositeChamber(NoiseReductionParameters.LEFT_LEAN)
		.setCompositeLayerPackedData(index,
			_parameters.getLeftSegments());
	_detectorTest.getCompositeChamber(NoiseReductionParameters.RIGHT_LEAN)
		.setCompositeLayerPackedData(index,
			_parameters.getRightSegments());

    }

    public void markHits() {
	// mark hits according to result
	for (HitTest ht : hits) {
	    int layer = ht.getLayer();
	    int wire = ht.getWire();
	    if (data[layer].checkBit(wire)) {
		ht.setComputedHitType(HitTest.HitType.TRACK);
	    } else {
		ht.setComputedHitType(HitTest.HitType.NOISE);
	    }
	}
    }

    /**
     * Create a hit
     * 
     * @param layer
     *            the layer in question.
     * @param wire
     *            the wire in question.
     * @param type
     *            the type of hit.
     * @return the created hit.
     */
    protected HitTest createHit(int layer, int wire, HitTest.HitType type) {
	HitTest ht = new HitTest(layer, wire, type);
	return ht;
    }

    /**
     * Initialize space
     */
    public void initializeSpace() {
	System.err.println("Initializing Space for num wires: "
		+ _parameters.getNumWire());

	// space for bitwise data
	data = new ExtendedWord[_parameters.getNumLayer()];
	for (int i = 0; i < _parameters.getNumLayer(); i++) {
	    data[i] = new ExtendedWord(_parameters.getNumWire());
	}

    }

    /**
     * See if there is a hit at that spot.
     * 
     * @param layer
     *            the layer to check.
     * @param wire
     *            the wire to check.
     * @return the hit at the given layer and wire, or <code>null</code> if
     *         there is none.
     */
    public HitTest findHit(int layer, int wire) {
	for (HitTest ht : hits) {
	    if ((ht.getLayer() == layer) && (ht.getWire() == wire)) {
		return ht;
	    }
	}
	return null;
    }

    /**
     * Generate hits from a straight line track
     * 
     * @param world
     *            the world system.
     * @param local
     *            the local system.
     * @param tt
     *            the track used to generate hits.
     */
    public void hitsFromTrack(TrackTest tt) {

	Point2D.Double wp0 = tt.getStartPoint();
	Point2D.Double wp1 = tt.getEndPoint();
	int bracket[] = new int[2];

	// intersects the overall boundary?

	if (_boundary.intersectsLine(wp0.x, wp0.y, wp1.x, wp1.y)) {

	    Rectangle.Double wcell = new Rectangle.Double();
	    for (int layer = 0; layer < _parameters.getNumLayer(); layer++) {
		// intersects layer?
		if (trackIntersectsLayer(layer, tt)) {

		    this.bracketTrack(tt, layer, bracket);
		    for (int wire = bracket[0]; wire <= bracket[1]; wire++) {
			cellWorldBounds(layer, wire, wcell);

			if (wcell.intersectsLine(wp0.x, wp0.y, wp1.x, wp1.y)) {
			    // remove noise hit
			    HitTest ht = findHit(layer, wire);
			    if (ht != null) {
				hits.remove(ht);
			    }

			    if (Math.random() > TestParameters.getProbBadWire()) {
				HitTest htnew = createHit(layer, wire,
					HitTest.HitType.TRACK);
				hits.add(htnew);
			    }
			}
		    }
		} // intersects layer
	    }
	} // intersects boundary
    }

    /**
     * Bracket the range of wires that might be intersected by the track. This
     * is to speed up the process.
     * 
     * @param tt
     *            the track in question.
     * @param layer
     *            the layer--already assumed that the layer test has been
     *            passed.
     * @param bracket
     *            int array with two elements. Upon return, search from
     *            bracket[0] to bracket[1].
     */
    protected void bracketTrack(TrackTest tt, int layer, int[] bracket) {
	int left = 0;
	int right = _parameters.getNumWire() - 1;
	bracket[0] = left;
	bracket[1] = right;

	while ((right - left) > 1) {
	    int nmid = (right + left) / 2;
	    boolean inLeft = trackIntersectsRange(layer, left, nmid - 1, tt);
	    boolean inRight = trackIntersectsRange(layer, nmid, right, tt);
	    if (inLeft && inRight) {
		bracket[0] = left;
		bracket[1] = right;
		return;
	    }
	    if (inLeft) {
		right = nmid - 1;
	    } else if (inRight) {
		left = nmid;
	    } else { // shouldn't happen of layer test passed
		System.out.println("That's rarely a good sign");
		bracket[0] = -1;
		bracket[1] = -2;
		return;
	    }
	}
	bracket[0] = left;
	bracket[1] = right;
    }

    /**
     * Check if a track intersects a layer.
     * 
     * @param layer
     *            the layer to check.
     * @param tt
     *            the track to test.
     * @return <code>true</code> if the track intersects the layer.
     */
    protected boolean trackIntersectsLayer(int layer, TrackTest tt) {
	Point2D.Double wp0 = tt.getStartPoint();
	Point2D.Double wp1 = tt.getEndPoint();

	Rectangle2D.Double wr = new Rectangle2D.Double();
	layerWorldBounds(layer, wr);

	return wr.intersectsLine(wp0.x, wp0.y, wp1.x, wp1.y);
    }

    /**
     * Check if a track intersects a range of wires.
     * 
     * @param layer
     *            the layer to check.
     * @param minwire
     *            the min wire of the range.
     * @param maxwire
     *            the max wire of the range.
     * @param tt
     *            the track to test.
     * @return <code>true</code> if the track intersects the layer.
     */
    protected boolean trackIntersectsRange(int layer, int minwire, int maxwire,
	    TrackTest tt) {
	Point2D.Double wp0 = tt.getStartPoint();
	Point2D.Double wp1 = tt.getEndPoint();

	Rectangle2D.Double wr = new Rectangle2D.Double();
	cellRangeWorldBounds(layer, minwire, maxwire, wr);

	return wr.intersectsLine(wp0.x, wp0.y, wp1.x, wp1.y);
    }

    /**
     * clear all the hits
     */
    public void clearHits() {
	hits.removeAllElements();
    }

    /**
     * Get the fractional occupancy of this chamber.
     * 
     * @return the fractional occupancy of this chamber.
     */
    public double getOccupancy() {
	return ((double) getNumHits() / getTotalNumWires());
    }

    /**
     * Get the total number of hits.
     * 
     * @return the number of hits;
     */
    public int getNumHits() {
	return hits.size();
    }

    // get number of noise hits
    public int getNumNoiseHits() {
	int num = 0;
	for (HitTest th : hits) {
	    if (th.getActualHitType() == HitTest.HitType.NOISE) {
		num++;
	    }
	}
	return num;
    }

    // get number of noise hits not removed
    public int getNumSavedNoiseHits() {
	int num = 0;
	for (HitTest th : hits) {
	    if (th.getComputedHitType() == HitTest.HitType.TRACK
		    && th.getActualHitType() == HitTest.HitType.NOISE) {
		num++;
	    }
	}
	return num;
    }

    public int getNumRemovedNoiseHits() {
	int num = 0;
	for (HitTest th : hits) {
	    if (th.getComputedHitType() != HitTest.HitType.TRACK
		    && th.getActualHitType() == HitTest.HitType.NOISE) {
		num++;
	    }
	}
	return num;
    }

    // get number of hits from segments
    public int getNumTrackHits() {
	int num = 0;
	for (HitTest th : hits) {

	    if (th.getComputedHitType() == HitTest.HitType.TRACK
		    && th.getActualHitType() == HitTest.HitType.TRACK) {
		num++;
	    }
	}
	return num;
    }

    /**
     * Get the total number of wires.
     * 
     * @return the total number of wires in all layers.
     */
    public int getTotalNumWires() {
	return _parameters.getNumLayer() * _parameters.getNumWire();
    }

}
