package cnuphys.bCNU.feedback;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.List;

import cnuphys.bCNU.application.BaseMDIApplication;
import cnuphys.bCNU.graphics.component.GlassPane;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.X11Colors;

public class HeadsUpDisplay extends GlassPane {

	// Font used to display HUD text;
	private static Font _hudFont = Fonts.commonFont(Font.BOLD, 12);

	// color behind text
	private Color _backgroundColor = new Color(48, 48, 48, 196);

	// the parent application
	private BaseMDIApplication _application;

	// which corner to display
	private static final int UPPERLEFT = 0;
	private static final int UPPERRIGHT = 1;
	private static final int LOWERRIGHT = 2;
	private static final int LOWERLEFT = 3;
	
	//corner choice
	private int _desiredCorner = LOWERRIGHT;
	
	//the current strings
	private List<String> _currentStrings;
	
	private Rectangle _cornerRect = new Rectangle();
	

	/**
	 * Create a HeadsUp display.
	 * 
	 * @param container
	 *            the parent container
	 */
	public HeadsUpDisplay(BaseMDIApplication application) {
		_application = application;
	}
	
	@Override
	public void paintComponent(Graphics g) {
				
		if (_currentStrings != null) {
			updateStrings(g, _currentStrings);
		}
	}
	
	public void update(List<String> feedbackStrings) {
		_currentStrings = feedbackStrings;
		
		Graphics g = this.getGraphics();
		repaint();
	}

	
	/**
	 * Set the desired corner
	 * @param corner the desired corner
	 */
	public void setDesiredCorner(int corner) {
		if ((corner >= 0) && (corner < 4)) {
			_desiredCorner = corner;
		}
	}

	/**
	 * This updates the heads up display.
	 * 
	 * @param feedbackStrings
	 *            the new feedback strings.
	 */
	private void updateStrings(Graphics g, List<String> feedbackStrings) {

		Rectangle bounds = _application.getBounds();
		if ((bounds == null) || (bounds.height < 10)) {
			return;
		}

		Dimension fbSize = getFeedbackSize(bounds, feedbackStrings);

		int fbw = 0;
		int fbh = 0;
		if (fbSize == null) {
			return;
		} else {
			fbw = fbSize.width;
			fbh = fbSize.height;
		}


		// set all four corner rects, later we'll decide which
		// to use (that the mouse is not in)

		int left = 10;
		int top = 40;
		int right = bounds.width - fbw - 10;
		int bottom = bounds.height - fbh - 40;
		
		switch (_desiredCorner) {
		case UPPERLEFT:
			_cornerRect.setBounds(left, top, fbw, fbh + 4);
			break;
		case UPPERRIGHT:
			_cornerRect.setBounds(right, top, fbw, fbh + 4);
			break;
		case LOWERRIGHT:
			_cornerRect.setBounds(right, bottom, fbw, fbh + 4);
			break;
		case LOWERLEFT:
			_cornerRect.setBounds(left, bottom, fbw, fbh + 4);
			break;
		}

		g.setColor(_backgroundColor);
		g.fillRect(_cornerRect.x, _cornerRect.y, _cornerRect.width, _cornerRect.height);

		
		drawStrings(g, _cornerRect.x + 2, _cornerRect.y, feedbackStrings);
	}

	/**
	 * Draw all the feedback strings
	 * 
	 * @param g
	 *            the graphics context
	 * @param x
	 *            the left
	 * @param feedbackStrings
	 *            the strings to be drawn
	 */
	private void drawStrings(Graphics g, int x, int yo,
			List<String> feedbackStrings) {
		FontMetrics fm = _application.getFontMetrics(_hudFont);
		int y = yo + fm.getHeight();
		g.setFont(_hudFont);

		for (String str : feedbackStrings) {
			if (str != null) {
				g.setColor(Color.cyan);

				if (str.startsWith("$")) {
					int nextIndex = str.indexOf("$", 1);
					if ((nextIndex > 3) && (nextIndex < 30)) {
						String x11color = str.substring(1, nextIndex)
								.toLowerCase();

						str = str.substring(nextIndex + 1);
						Color color = X11Colors.getX11Color(x11color);
						if (color != null) {
							g.setColor(color);
						}
					}
				}

				g.drawString(str, x, y);
				y += fm.getHeight();
			}
		}
	}

	/**
	 * Get the size needed to draw all the feedback. This also computes all the
	 * feedback strings on the fly.
	 * 
	 * @param bounds
	 *            the container bounds.
	 * @param screenPoint
	 *            the current mouse location.
	 * @param worldPoint
	 *            the corresponding world point.
	 * @return the size needed to draw all the feedback
	 */
	private Dimension getFeedbackSize(Rectangle bounds,
			List<String> feedbackStrings) {
		int w = 0;
		int h = 0;
		int slop = 4;

		FontMetrics fm = _application.getFontMetrics(_hudFont);

		for (String s : feedbackStrings) {
			if (s != null) {
				String snocolor = colorRemoved(s);
				h += fm.getHeight();
				w = Math.max(w, fm.stringWidth(snocolor) + slop);

				// too big?
				if (h > bounds.height) {
					return new Dimension(w, bounds.height);
				}
			}
		}

		if ((h < 3) || (w < 3)) {
			return null;
		}

		return new Dimension(w, h);
	}

	// removes the color $[color]$ prefix so string is size properly
	private String colorRemoved(String str) {
		if (str.startsWith("$")) {
			int nextIndex = str.indexOf("$", 1);
			if ((nextIndex > 3) && (nextIndex < 30)) {
				return str.substring(nextIndex + 1);
			}
		}
		return str;
	}

	/**
	 * Clear the strings
	 */
	public void clear() {
		if (_currentStrings != null) {
			_currentStrings = null;
			repaint();
		}
	}

}
