package cnuphys.bCNU.graphics.colorscale;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;
import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.Fonts;

/**
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class ColorModelLegend extends JComponent {

	private ColorScaleModel _model;

	private Dimension size;

	/**
	 * Create a color legend with the given color model.
	 * 
	 * @param model
	 *            the model to display as a legend.
	 */
	public ColorModelLegend(ColorScaleModel model, int desiredWidth, String name) {
		size = new Dimension(desiredWidth, 50);
		setLayout(new BorderLayout(2, 4));
		_model = model;
		setBorder(new CommonBorder(name));
	}

	@Override
	public void paintComponent(Graphics g) {
		Color colors[] = _model.getColors();
		double values[] = _model.getValues();
		Rectangle bounds = getBounds();
		int dx = bounds.width / colors.length;

		// make sure it fits
		if ((colors.length * dx) > (bounds.width - 8)) {
			dx -= 1;
		}

		int left = (bounds.width - (colors.length * dx)) / 2;
		int top = bounds.height - 30;
		int height = 12;
		int ytext = bounds.height - 6;
		int bottom = top + height;
		int right = left + colors.length * dx;

		for (int i = 0; i < colors.length; i++) {
			int xt = left + i * dx;
			g.setColor(colors[i]);
			g.setFont(Fonts.smallFont);
			g.fillRect(xt, top, dx, height);
			if (i == 0) {
				double val = _model.getMinValue();
				g.setColor(Color.black);
				g.drawString(
						DoubleFormat.doubleFormat(val, _model.getPrecision()),
						xt - 3, ytext);
			}

			else if (i == (colors.length / 2)) {
				double val = values[values.length / 2];
				g.setColor(Color.black);
				g.drawString(
						DoubleFormat.doubleFormat(val, _model.getPrecision()),
						xt - 4, ytext);
			}

			else if (i == (colors.length / 4)) {
				double val = values[values.length / 4];
				g.setColor(Color.black);
				g.drawString(
						DoubleFormat.doubleFormat(val, _model.getPrecision()),
						xt - 4, ytext);
			}

			else if (i == (3 * colors.length / 4)) {
				double val = values[3 * values.length / 4];
				g.setColor(Color.black);
				g.drawString(
						DoubleFormat.doubleFormat(val, _model.getPrecision()),
						xt - 4, ytext);
			}

			else if (i == (colors.length - 1)) {
				double val = _model.getMaxValue();
				g.setColor(Color.black);
				g.drawString(
						DoubleFormat.doubleFormat(val, _model.getPrecision()),
						xt - 5, ytext);
			}
		}

		GraphicsUtilities.drawSimple3DRect(g, left, top, right - left, bottom
				- top, true);
	}

	@Override
	public Dimension getPreferredSize() {
		return size;
	}

}
