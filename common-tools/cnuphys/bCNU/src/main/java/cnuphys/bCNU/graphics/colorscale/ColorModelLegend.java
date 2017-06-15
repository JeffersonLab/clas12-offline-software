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

	private int _gap = 2;
	private int left;
	private int right;
	private int top;
	private int bottom;

	/**
	 * Create a color legend with the given color model.
	 * 
	 * @param model
	 *            the model to display as a legend.
	 * @param desiredWidth the width of the component (not including gap)
	 * @param name the border text
	 * @param gap left and right gap
	 */
	public ColorModelLegend(ColorScaleModel model, int desiredWidth, String name, int gap) {

		_gap = gap;
		
		if (desiredWidth > 0) {
			size = new Dimension(desiredWidth + 2 * gap, 50);
		}
		setLayout(new BorderLayout(2, 4));
		_model = model;
		setBorder(new CommonBorder(name));
		setOpaque(true);
	}
	
	/**
	 * Create a color legend with the given color model.
	 * 
	 * @param model
	 *            the model to display as a legend.
	 * @param desiredWidth the width of the component (not including gap)
	 * @param name the border text
	 */
	public ColorModelLegend(ColorScaleModel model, int desiredWidth, String name) {
		this(model, desiredWidth, name, 2);
	}

	@Override
	public void paintComponent(Graphics g) {
		Color colors[] = _model.getColors();
		double values[] = _model.getValues();
		Rectangle bounds = getBounds();

		double useableW = bounds.width - 2 * _gap;
		double dx = useableW / colors.length;

//		// make sure it fits
//		if ((colors.length * dx) > (useableW - 8)) {
//			dx -= 1;
//		}
		
		double left = _gap + (useableW - (colors.length * dx)) / 2;
		double right = left + dx;
		int height = 12;
		int ytext = bounds.height - 6;
		top = bounds.height - 30;
		bottom = top + height;

		for (int i = 0; i < colors.length; i++) {
			g.setColor(colors[i]);
			g.setFont(Fonts.smallFont);
			int xt = (int)left;
			g.fillRect(xt, top, (int)(right-left), height);
			g.drawRect(xt, top, (int)(right-left), height);
			
			if (i == 0) {
				double val = _model.getMinValue();

				g.setColor(Color.black);
				if (Math.abs(val) < 1.0e-5) {
					g.drawString("0", xt - 1, ytext);
				}
				else {
					g.drawString(DoubleFormat.doubleFormat(val, _model.getPrecision()), xt - 3, ytext);
				}
			}

			else if (i == (colors.length / 2)) {
				double val = values[values.length / 2];
				g.setColor(Color.black);
				g.drawString(DoubleFormat.doubleFormat(val, _model.getPrecision()), xt - 4, ytext);
			}

			else if (i == (colors.length / 4)) {
				double val = values[values.length / 4];
				g.setColor(Color.black);
				g.drawString(DoubleFormat.doubleFormat(val, _model.getPrecision()), xt - 4, ytext);
			}

			else if (i == (3 * colors.length / 4)) {
				double val = values[3 * values.length / 4];
				g.setColor(Color.black);
				g.drawString(DoubleFormat.doubleFormat(val, _model.getPrecision()), xt - 4, ytext);
			}

			else if (i == (colors.length - 1)) {
				double val = _model.getMaxValue();
				g.setColor(Color.black);
				if (Math.abs(val - 1) < 1.0e-5) {
					g.drawString("1", xt - 3, ytext);
				}
				else {
					g.drawString(DoubleFormat.doubleFormat(val, _model.getPrecision()), xt - 5, ytext);
				}
			}			
			
			left = right;
			right += dx;
		}		

		right = left;
		left = _gap + (useableW - (colors.length * dx)) / 2;
		GraphicsUtilities.drawSimple3DRect(g, (int)left, top-1, (int)(right - left), bottom - top + 1, false);

	}

	@Override
	public Dimension getPreferredSize() {
		if (size == null) {
			return super.getPreferredSize();
		}
		return size;
	}

}
