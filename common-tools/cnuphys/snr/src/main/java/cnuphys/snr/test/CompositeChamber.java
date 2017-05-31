package cnuphys.snr.test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import cnuphys.snr.ExtendedWord;
import cnuphys.snr.NoiseReductionParameters;

public class CompositeChamber extends ChamberTest {

    private Color fillColor = new Color(240, 248, 255);
    private Color lineColor = new Color(0, 0, 139);

    /**
     * Create a test chamber--rectangular with uniform rectangular cells.
     * 
     * @param name
     *            the name of the chamber.
     * @param boundary
     *            the rectangular boundary in world coordinates.
     */
    public CompositeChamber(DetectorTest detectorTest, String name,
	    NoiseReductionParameters parameters, Rectangle2D.Double boundary) {
	super(detectorTest, -1, parameters, boundary);
	_name = name;
    }

    /**
     * Draw the composite chamber.
     * 
     * @param g
     *            the Graphics context.
     * @param world
     *            the world system
     * @param local
     *            the local system
     */
    @Override
    public void draw(Graphics g, Rectangle2D.Double world, Rectangle local) {

	Rectangle cell = new Rectangle();
	drawCellOutlines(g, world, local, fillColor);

	drawString(g, world, local, _name, _boundary.x, _boundary.y
		+ _boundary.height);

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

	for (int layer = 0; layer < _parameters.getNumLayer(); layer++) {
	    for (int wire = 0; wire < _parameters.getNumWire(); wire++) {
		if (_parameters.getPackedData(layer).checkBit(wire)) {
		    cellBounds(world, local, layer, wire, cell);
		    g.setColor(Color.red);
		    g.fillRect(cell.x, cell.y, cell.width, cell.height);
		} else if (_parameters.getRawData(layer).checkBit(wire)) {
		    cellBounds(world, local, layer, wire, cell);
		    g.setColor(Color.gray);
		    g.fillRect(cell.x, cell.y, cell.width, cell.height);
		}
	    }
	}

	drawCellOutlines(g, world, local, null);

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
    @Override
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
     * Set the data for the composite chamber from the left and right segments
     * of the coresponding real chamber
     * 
     * @param layer
     *            the composite 0-based layer. E.g., if this is 3, then it will
     *            hold the results fro chamber 3.
     * @param segments
     * @param rightSegments
     */
    public void setCompositeLayerPackedData(int layer, ExtendedWord segments) {
	ExtendedWord layerData = data[layer];
	ExtendedWord.copy(segments, layerData);
    }

    /**
     * Apply the noise reduction algorithm to the composite chamber.
     */
    @Override
    public void removeNoise() {
	_parameters.createWorkSpace();
	_parameters.setPackedData(data);
	_parameters.removeNoise();

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
    @Override
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

	// allowed missing layers
	sb.append("  Missing Layers Allowed "
		+ _parameters.getAllowedMissingLayers());

	// layers shifts
	sb.append(shiftString("  Left Shifts", _parameters.getLeftLayerShifts()));
	sb.append(shiftString("  Right Shifts",
		_parameters.getRightLayerShifts()));

	return sb.toString();
    }

}
