package cnuphys.bCNU.graphics.toolbar;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.Collections;
import java.util.Vector;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.magneticfield.swim.ASwimTrajectoryDrawer;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.view.BaseView;
import cnuphys.lund.LundId;
import cnuphys.lund.LundStyle;
import cnuphys.splot.plot.GraphicsUtilities;

public abstract class AUserComponentDrawer extends DrawableAdapter {

	// STring used when no PIDs found (e.g., a raw event)
//	private static final String NO_PIDS = "No Monte Carlo or reconstructed particles";
	private static final String NO_PIDS = "";

	// the component on the toolbar being drawn upon
	private UserToolBarComponent _component;

	// used to get the text in the right place
	private static int _ytext = -1;

	// used to get the text in the right place
	private static int _fh = -1;

	// used to get the line in the right place
	private static int _yeven = -1;

	// used to get the line in the right place
	private static int _yodd = -1;

	private static final Font labelFont = new Font("SansSerif", Font.PLAIN, 11);

	// the owner view
	protected BaseView _view;

	/**
	 * Create a User Component (on the toolbar) drawer for a given view.
	 * 
	 * @param view the view with a toolbar that has a user component.
	 */
	public AUserComponentDrawer(BaseView view) {
		_view = view;
		_component = view.getUserComponent();
	}

	/**
	 * Draw on the component.
	 * 
	 * @param g         the graphics context.
	 * @param container the container on the view.
	 */
	@Override
	public void draw(Graphics g, IContainer container) {

		Rectangle b = _component.getBounds();

		// fill the background
		g.setColor(_component.getBackground());
		g.fillRect(0, 0, b.width - 1, b.height - 1);

		// get the unique lundids found in this event
		Vector<LundId> lids = getUniqueLundIds();

		int numMC = (lids == null) ? 0 : lids.size();

		if (_fh < 0) {
			FontMetrics fm = _component.getFontMetrics(Fonts.smallFont);
			_fh = fm.getAscent();
			// _ytext = (b.height + fm.getHeight())/2 - 2;
			_ytext = (b.height + _fh) / 2;
			int yc = b.height / 2;

			_yeven = yc - _fh / 2;
			_yodd = 2 + yc + _fh / 2;
		}

		if (numMC == 0) {
			g.setFont(Fonts.smallFont);
			g.setColor(Color.red);

			if (_ytext < 0) {
				FontMetrics fm = _component.getFontMetrics(Fonts.smallFont);
				_ytext = (b.height + fm.getHeight()) / 2 - 2;
			}

			g.drawString(NO_PIDS, 2, _ytext);
		} else {
			// now draw all of them. Sort so order stays the same
			Collections.sort(lids);
			int x = 4;
			int xoff = -15;
			int index = 0;
			for (LundId lid : lids) {
				if ((index % 2) == 0) {
					x = x + drawLineForLegend(g, x, _yeven, lid) + xoff;
				} else {
					x = x + drawLineForLegend(g, x, _yodd, lid) + xoff;
				}
				index++;
			}

		}
	}

	/**
	 * Draw a line for use on a toolbar user component, most likely
	 * 
	 * @param g  the graphics context
	 * @param x  the horizontal staring point
	 * @param yc the central vertical position
	 * @return the offset
	 */
	public int drawLineForLegend(Graphics g, int x, int yc, LundId lid) {

		Graphics2D g2 = (Graphics2D) g;
		Stroke oldStroke = g2.getStroke();
		LundStyle style = LundStyle.getStyle(lid);
		g2.setStroke(style.getStroke());

		int linelen = 30;

//		if ((lid != null)) {
//			GraphicsUtilities.drawHighlightedLine(g2, x, yc, x + linelen, yc, style.getLineColor(),
//					ASwimTrajectoryDrawer.getHighlightColor(lid));
//		} else {
//			g.setColor(style.getLineColor());
//			g2.drawLine(x, yc, x + linelen, yc);
//		}
		
		g.setColor(style.getLineColor());
		g2.setStroke(style.getStroke());

		g2.drawLine(x, yc, x + linelen, yc);


		x += linelen + 3;

		g2.setStroke(oldStroke);

		// now the name
		g.setFont(labelFont);
		FontMetrics fm = g.getFontMetrics(labelFont);
		g.setColor(Color.black);

		int esw = 0;
		if (lid != null) {
			g.drawString(lid.getName(), x, yc + fm.getAscent() / 2 - 3);
			esw = fm.stringWidth(lid.getName()) + 9;
		}

		return linelen + esw;
	}

	/**
	 * This method must be filled in to return all the unique LundIds associated
	 * with this event.
	 * 
	 * @return all the unique LundIds associated with this event.
	 */
	protected abstract Vector<LundId> getUniqueLundIds();

}
