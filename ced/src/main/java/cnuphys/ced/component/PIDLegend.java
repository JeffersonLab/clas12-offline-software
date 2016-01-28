package cnuphys.ced.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JComponent;

import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.Fonts;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.lund.LundId;
import cnuphys.lund.LundStyle;

public class PIDLegend extends JComponent {

	// convenience reference to event manager
	private static ClasIoEventManager _eventManager = ClasIoEventManager
			.getInstance();

	// STring used when no PIDs found (e.g., a raw event)
	private static final String NO_PIDS = "No Monte Carlo or reconstructed particles";

	// used to get the text in the right place
	private static int _ytext = -1;

	// used to get the text in the right place
	private static int _fh = -1;

	// used to get the line in the right place
	private static int _yeven = -1;

	// used to get the line in the right place
	private static int _yodd = -1;

	private static final Font labelFont = new Font("SansSerif", Font.PLAIN, 11);

	// the owner
	protected JComponent _component;

	private final int prefHeight;

	/**
	 * Create a User Component (on the toolbar) drawer for a given view.
	 * 
	 * @param view
	 *            the view with a toolbar that has a user component.
	 */
	public PIDLegend(JComponent parent) {
		_component = parent;

		prefHeight = _component.getFontMetrics(labelFont).getHeight() * 2;

		setBorder(new CommonBorder());
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		d.height = prefHeight;
		return d;
	}

	/**
	 * Draw on the component.
	 * 
	 * @param g
	 *            the graphics context.
	 * @param container
	 *            the container on the view.
	 */
	@Override
	public void paintComponent(Graphics g) {

		Rectangle b = getBounds();

		// fill the background
		g.setColor(getBackground());
		g.fillRect(0, 0, b.width - 1, b.height - 1);

		if (_eventManager.isAccumulating()) {
			return;
		}

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
			_yodd = yc + _fh / 2;
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
	 * @param g
	 *            the graphics context
	 * @param x
	 *            the horizontal staring point
	 * @param yc
	 *            the central vertical position
	 * @return the offset
	 */
	public int drawLineForLegend(Graphics g, int x, int yc, LundId lid) {

		Graphics2D g2 = (Graphics2D) g;
		Stroke oldStroke = g2.getStroke();
		LundStyle style = LundStyle.getStyle(lid);

		int linelen = 30;

		Color color1 = Color.lightGray;

		if (style != null) {
			g2.setStroke(style.getStroke());
			color1 = style.getFillColor();
		}
		g.setColor(color1);

		g2.drawLine(x, yc, x + linelen, yc);

		x += linelen + 3;

		g2.setStroke(oldStroke);

		// now the name
		g.setFont(labelFont);
		FontMetrics fm = g.getFontMetrics(labelFont);
		g.setColor(Color.black);
		g.drawString(lid.getName(), x, yc + fm.getAscent() / 2 - 3);

		return linelen + fm.stringWidth(lid.getName()) + 9;
	}

	/**
	 * This method must be filled in to return all the unique LundIds associated
	 * with this event.
	 * 
	 * @return all the unique LundIds associated with this event.
	 */
	protected Vector<LundId> getUniqueLundIds() {
		return _eventManager.uniqueLundIds();
	}

}
