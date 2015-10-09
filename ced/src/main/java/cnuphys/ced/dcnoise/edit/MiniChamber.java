package cnuphys.ced.dcnoise.edit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.geometry.GeoConstants;
import cnuphys.snr.NoiseReductionParameters;

/**
 * Displays a little chamber so that the parameters can be represented
 * graphically. This will sit on the dialog so that when changes are made they
 * are reflected.
 * 
 * @author DHeddle
 * 
 */
@SuppressWarnings("serial")
public class MiniChamber extends JComponent {

	// number of wires to display. Roughy 2xMaxLayerShift
	private static final int _NUMWIRES = 48;

	// cell size (cell==wire) in pixels
	private static final int _CELLSIZE = 6;

	// color of "inside the template" wires
	private static final Color _FILL = X11Colors.getX11Color("teal");

	// color of cell boundaries (layers)
	private static final Color _LINE = X11Colors.getX11Color("dark blue");

	// the parameters being displayed
	private NoiseReductionParameters _parameters;

	// pixel margin around minichamber
	private static final int _MARGIN = 4;

	/**
	 * Create a little display to show the parameters
	 * 
	 * @param parameters
	 *            the initial values
	 */
	public MiniChamber() {
	}

	/**
	 * Paint the MiniChamber.
	 * 
	 * @param g
	 *            the Graphics context.
	 * @return
	 */
	@Override
	public void paintComponent(Graphics g) {
		Rectangle bounds = getBounds();

		if (_parameters != null) {
			g.setColor(_FILL);
			int wire = _NUMWIRES / 2; // central wire
			for (int layer = 0; layer < GeoConstants.NUM_LAYER; layer++) {
				Rectangle r = getCell(layer, wire);
				g.fillRect(r.x, r.y, r.width, r.height);
				int leftShift = _parameters.getLeftLayerShifts()[layer];
				for (int shift = 1; shift <= leftShift; shift++) {
					r = getCell(layer, wire + shift);
					g.fillRect(r.x, r.y, r.width, r.height);
				}
				int rightShift = _parameters.getRightLayerShifts()[layer];
				for (int shift = 1; shift <= rightShift; shift++) {
					r = getCell(layer, wire - shift);
					g.fillRect(r.x, r.y, r.width, r.height);
				}

			}
		}

		// draw the mesh;

		g.setColor(_LINE);
		for (int layer = 0; layer < GeoConstants.NUM_LAYER; layer++) {
			Rectangle rr = getCell(layer, 0);
			Rectangle lr = getCell(layer, _NUMWIRES - 1);
			g.drawLine(lr.x, lr.y, rr.x + rr.width, rr.y);
			if (layer == 0) {
				g.drawLine(lr.x, lr.y + lr.height, rr.x + rr.width, rr.y
						+ rr.height);
			}
		}

		for (int wire = 0; wire < _NUMWIRES; wire++) {
			Rectangle br = getCell(0, wire);
			Rectangle tr = getCell(GeoConstants.NUM_LAYER - 1, wire);
			g.drawLine(br.x, br.y + br.height, tr.x, tr.y);
			if (wire == 0) {
				g.drawLine(br.x + br.width, br.y + br.height, tr.x + tr.width,
						tr.y);
			}
		}

		GraphicsUtilities.drawSimple3DRect(g, 0, 0, bounds.width - 1,
				bounds.height - 1, true);

	}

	/**
	 * Get the cell boundary for a given layer and wire. Mimic the traditional
	 * layout--wires increase from right to left and layers from bottom to top.
	 * 
	 * @param layer
	 *            the layer [0..(GeoConstants.NUM_LAYER-1)]
	 * @param wire
	 *            the wire [0..(_NUMWIRES-1)]
	 * @return the rectangular boundary.
	 */
	private Rectangle getCell(int layer, int wire) {
		Rectangle bounds = getBounds();
		int w = (bounds.width - 2 * _MARGIN) / _NUMWIRES;
		int h = (bounds.height - 2 * _MARGIN) / GeoConstants.NUM_LAYER;

		int hmargin = (bounds.width - _NUMWIRES * w) / 2;
		int vmargin = (bounds.height - GeoConstants.NUM_LAYER * h) / 2;

		int x = bounds.width - hmargin - (wire + 1) * w;
		int y = bounds.height - vmargin - (layer + 1) * h;
		return new Rectangle(x, y, w, h);
	}

	/**
	 * Get the preferred size.
	 * 
	 * @return the preferred size.
	 */
	@Override
	public Dimension getPreferredSize() {
		int w = _NUMWIRES * _CELLSIZE + 2 * _MARGIN;
		int h = GeoConstants.NUM_LAYER * _CELLSIZE + 2 * _MARGIN;
		return new Dimension(w, h);
	}

	/**
	 * Set new display parameters.
	 * 
	 * @param parameters
	 *            the parameters to set
	 */
	public void setParameters(NoiseReductionParameters parameters) {
		_parameters = parameters;
		repaint();
	}
}
