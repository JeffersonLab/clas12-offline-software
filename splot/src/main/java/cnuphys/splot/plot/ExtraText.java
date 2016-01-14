package cnuphys.splot.plot;

import java.awt.FontMetrics;
import java.awt.Graphics;

/**
 * A legend like rectangle for extra text
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
	private final int VGAP = 6;

	/**
	 * Create a Extra Text rectangle
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
		width = getExtraWidth();
		height = getExtraHeight();
		if ((width < 2) || (height < 2)) {
			return;
		}
		
		g.setColor(_params.getExtraBackground());
		g.fillRect(x, y, width, height);

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

		return maxStringWidth + 2*HGAP;
	}

	// get required height of the extra text rectangle
	private int getExtraHeight() {
		FontMetrics fm = _canvas.getFontMetrics(_params.getExtraFont());
		String[] extraStrings = _params.getExtraStrings();
		if ((extraStrings == null) || (extraStrings.length < 1)) {
			return 0;
		}

		return VGAP + (extraStrings.length)*(VGAP + fm.getHeight());
	}
	
}
