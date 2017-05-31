package cnuphys.bCNU.feedback;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.item.YouAreHereItem;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.X11Colors;

public class HeadsUpDisplay {

	// Font used to display HUD text;
	private static Font _hudFont = Fonts.commonFont(Font.BOLD, 9);

	// color behind text
	private Color _backgroundColor = new Color(0, 0, 64, 160);

	// the parent container
	private IContainer _container;

	// hack for flash
	private static int _udcount = 0;

	// which corner
	private static final int UPPERLEFT = 0;
	private static final int UPPERRIGHT = 1;
	private static final int LOWERRIGHT = 2;
	private static final int LOWERLEFT = 3;
	private int _corner = UPPERLEFT;

	private Rectangle _cornerRects[] = new Rectangle[4];

	/**
	 * Create a HeadsUp display.
	 * 
	 * @param container
	 *            the parent container
	 */
	public HeadsUpDisplay(IContainer container) {
		_container = container;

		for (int i = 0; i < 4; i++) {
			_cornerRects[i] = new Rectangle();
		}
	}

	/**
	 * This updates the heads up display.
	 * 
	 * @param feedbackStrings
	 *            the new feedback strings.
	 */
	public void updateHeadsUp(List<String> feedbackStrings,
			MouseEvent mouseEvent) {

		if (mouseEvent == null) {
			return;
		}

		Rectangle bounds = _container.getComponent().getBounds();
		if ((bounds == null) || (bounds.height < 10)) {
			return;
		}

		boolean drawStrings = true;
		Dimension fbSize = getFeedbackSize(bounds, feedbackStrings);

		int fbw = 0;
		int fbh = 0;
		if (fbSize == null) {
			drawStrings = false;
		} else {
			fbw = fbSize.width;
			fbh = fbSize.height;
		}

		// get the container's image
		BufferedImage offscreenBuffer = _container.getImage();
		if (offscreenBuffer == null) {
			return;
		}

		// make a new buffer the same size
		BufferedImage newBuffer = new BufferedImage(offscreenBuffer.getWidth(),
				offscreenBuffer.getHeight(), BufferedImage.TYPE_INT_RGB);

		// draw the background image onto the new buffer
		Graphics newg = newBuffer.getGraphics();
		newg.drawImage(offscreenBuffer, 0, 0, _container.getComponent());

		// set both upper left and upper right rects, later we'll decide which
		// to use

		int left = 4;
		int top = 0;
		int right = bounds.width - fbw - 4;
		int bottom = bounds.height - fbh - 4;

		_cornerRects[UPPERLEFT].setBounds(left, top, fbw, fbh + 4);
		_cornerRects[UPPERRIGHT].setBounds(right, top, fbw, fbh + 4);
		_cornerRects[LOWERRIGHT].setBounds(right, bottom, fbw, fbh + 4);
		_cornerRects[LOWERLEFT].setBounds(left, bottom, fbw, fbh + 4);

		// find a corner the mouse is not in
		int count = 0;
		while ((count < 4)
				&& _cornerRects[_corner].contains(mouseEvent.getPoint())) {
			_corner = (_corner + 1) % 4;
			count++;
		}

		// now the strings
		if (drawStrings) {

			Rectangle rect = _cornerRects[_corner];

			newg.setColor(_backgroundColor);
			newg.fillRect(rect.x, rect.y, rect.width, rect.height);
			drawStrings(newg, rect.x + 2, rect.y, feedbackStrings);

		}

		newg.dispose();

		Graphics g = _container.getComponent().getGraphics();
		// this causes that flash the first time i dinna ken why
		// hack: skip the first few
		if (_udcount < 3) {
			_udcount++;
		} else {
			g.drawImage(newBuffer, 0, 0, _container.getComponent());

			// anchor (urhere) feedback?
			YouAreHereItem item = _container.getYouAreHereItem();

			if (item != null) {
				// draw line if shift down
				if (mouseEvent.isShiftDown()) {
					Point anchor = item.getFocusPoint(_container);
					g.setColor(Color.black);
					g.drawLine(mouseEvent.getX(), mouseEvent.getY(), anchor.x,
							anchor.y);
				}
			}
		}

		g.dispose();
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
		FontMetrics fm = _container.getComponent().getFontMetrics(_hudFont);
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

		FontMetrics fm = _container.getComponent().getFontMetrics(_hudFont);

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

}
