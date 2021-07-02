package cnuphys.splot.plot;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * A legend like rectangle for extra text
 * 
 * @author heddle
 *
 */
public class ExtraText extends DraggableRectangle {

	// the owner plot panel
	private PlotCanvas _canvas;

	// the plot parameters
	private PlotParameters _params;

	// gap before text
	private final int HGAP = 8;

	// extra v gap
	private final int VGAP = 2;

	/**
	 * Create a Extra Text rectangle
	 * 
	 * @param canvas the parent plot canvas
	 */
	public ExtraText(PlotCanvas canvas) {
		_canvas = canvas;
		_params = canvas.getParameters();
	}

	/**
	 * Draw the extra text
	 * 
	 * @param g the graphics context
	 */
	public void draw(Graphics g) {

		String[] extraStrings = _params.getExtraStrings();
		if ((extraStrings == null) || (extraStrings.length < 1)) {
			return;
		}

		width = getExtraWidth();
		height = getExtraHeight();

		if ((width < 2) || (height < 2)) {
			return;
		}

		// if never dragged, keep at upper right default location
		if (!_beenMoved) {
			Rectangle actRect = _canvas.getActiveBounds();
			int right = actRect.x + actRect.width;
			x = right - width - 10;
		}

		g.setColor(_params.getExtraBackground());
		g.setFont(_params.getExtraFont());
		g.fillRect(x, y, width, height);

		g.setColor(_params.getExtraForeground());
		FontMetrics fm = _canvas.getFontMetrics(_params.getExtraFont());

		int xs = x + HGAP;
		int ys = y + VGAP + fm.getHeight();
		for (String s : extraStrings) {
			g.drawString(s, xs, ys);
			ys += fm.getHeight();
		}

		if (_params.isExtraBorder()) {
			g.setColor(_params.getExtraBorderColor());
			g.drawRect(x, y, width, height);
		}

	}

	// get required width of the extra box
	private int getExtraWidth() {
		FontMetrics fm = _canvas.getFontMetrics(_params.getExtraFont());
		String[] extraStrings = _params.getExtraStrings();
		if ((extraStrings == null) || (extraStrings.length < 1)) {
			return 0;
		}

		int maxStringWidth = 0;
		for (String s : extraStrings) {
			maxStringWidth = Math.max(maxStringWidth, fm.stringWidth(s));
		}

		return maxStringWidth + 2 * HGAP;
	}

	// get required height of the extra text rectangle
	private int getExtraHeight() {
		FontMetrics fm = _canvas.getFontMetrics(_params.getExtraFont());
		String[] extraStrings = _params.getExtraStrings();
		if ((extraStrings == null) || (extraStrings.length < 1)) {
			return 0;
		}

		return VGAP + (extraStrings.length) * (VGAP + fm.getHeight());
	}

}
