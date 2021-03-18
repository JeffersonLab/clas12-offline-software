package cnuphys.splot.plot;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

public class Gradient extends DraggableRectangle {

	// height of gradient
	private static int GRAD_HEIGHT = 250;

	// width of gradient not including labels
	private static int GRAD_WIDTH = 20;

	// the underlying color color scale model
	private ColorScaleModel _colorModel;

	// the owner plot panel
	private PlotCanvas _canvas;

	// the plot parameters
	private PlotParameters _params;

	// is it a relative 0 to 1 scale?
	private boolean _zeroMaxScale;

	// gap before text
	private final int HGAP = 2;

	/**
	 * Create a gradient rectangle rectangle using the standard blue to red gradient
	 * with 0 to 1 value range relative range
	 * 
	 * @param canvas the parent plot canvas
	 */
	public Gradient(PlotCanvas canvas) {
		_canvas = canvas;
		_params = canvas.getParameters();
		_colorModel = ColorScaleModel.blueToRed();
		_zeroMaxScale = true;
	}

	/**
	 * Draw the gradient
	 * 
	 * @param g the Graphics context
	 */
	public void draw(Graphics g) {

		width = getGradientWidth();
		height = getGradientHeight();

		if ((width < 2) || (height < 2)) {
			return;
		}

		// if never dragged, keep at lower right default location
		if (!_beenMoved) {
			Rectangle actRect = _canvas.getActiveBounds();
			int right = actRect.x + actRect.width;
			int bottom = actRect.y + actRect.height;
			x = right - width - 10;
			y = bottom - height - 10;
		}

		int bottom = y + height;

//		g.setColor(_params.getTextBackground());
		g.setFont(_params.getTextFont());
//		g.fillRect(x, y, width, height);

		g.setColor(_params.getTextForeground());
		FontMetrics fm = _canvas.getFontMetrics(_params.getTextFont());
		g.drawString("0", x + GRAD_WIDTH + HGAP, bottom);
		g.drawString("max", x + GRAD_WIDTH + HGAP, y + fm.getAscent());

		for (int i = 0; i < height; i += 2) {
			double val = ((double) i) / height;
			Color c = getColor(val);
			g.setColor(c);
			g.fillRect(x, bottom - i, GRAD_WIDTH, 2);
		}

		g.setColor(Color.black);
		g.drawRect(x, y + 1, GRAD_WIDTH, GRAD_HEIGHT + 1);
	}

	/**
	 * Get the height of the gradient
	 * 
	 * @return the height of the gradient
	 */
	protected int getGradientHeight() {
		return GRAD_HEIGHT;
	}

	protected int getGradientWidth() {
		int w = GRAD_WIDTH;

		// now account for text
		FontMetrics fm = _canvas.getFontMetrics(_params.getTextFont());

		if (_zeroMaxScale) {
			w = w + HGAP + fm.stringWidth("max");
		}
		else {
			// TODO implement
		}

		return w;
	}

	/**
	 * Get the color for the give value. Interpolation is used.
	 * 
	 * @param value the vale
	 * @return the color
	 */
	public Color getColor(double value) {
		// use interpolation
		return _colorModel.getColor(value);
	}

}
