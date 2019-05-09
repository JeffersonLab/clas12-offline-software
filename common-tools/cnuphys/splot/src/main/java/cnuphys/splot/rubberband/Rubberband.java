package cnuphys.splot.rubberband;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

import cnuphys.splot.plot.GraphicsUtilities;
import cnuphys.splot.plot.PlotCanvas;

public final class Rubberband {

	// Needed by a hack that prevents a 1st time flash
	private static boolean veryFirst = true;

	/**
	 * Enum of possible rubber banding polices.
	 */
	public static enum Policy {
		RECTANGLE, RECTANGLE_PRESERVE_ASPECT, POLYGON, POLYLINE, OVAL, LINE, RADARC, XONLY, YONLY
	}

	// The default fill color. Should be fairly transparent.
	private static final Color _defaultFillColor = new Color(255, 128, 128, 96);

	// One default color for highlighted drawing of boundary.
	public Color _highlightColor1 = Color.red;

	// Second default color for highlighted drawing of boundary.
	public Color _highlightColor2 = Color.yellow;

	// The default shape policy for rubberbanding
	private Policy _policy = Policy.RECTANGLE;

	// The anchor or starting screen point.
	private Point _startPt = new Point();

	// The current point during rubber banding
	private Point _currentPt = new Point();

	// Component being rubber banded.
	private Component _component;

	// A transparent fill color.
	private Color _fillColor = _defaultFillColor;

	// For collecting polygon points.
	private Polygon _poly;

	// also used for collecting points
	private Polygon _tempPoly;

	// Make sure we only start once
	private boolean _started = false;

	// used for component image
	private BufferedImage _backgroundImage;
	private BufferedImage _image;

	// temporary mouse listeners
	private MouseMotionAdapter _mouseMotionAdapter;
	private MouseAdapter _mouseAdapter;

	// Flag for whether rubberband is active
	private boolean _active = false;

	// Listener to notify when we are done.
	private IRubberbanded _rubberbanded;

	private PlotCanvas _canvas;

	/**
	 * Create a Rubberband
	 * 
	 * @param container    the parent component being rubberbanded
	 * @param rubberbanded who gets notified when we are done.
	 * @param policy       the stretching shape policy.
	 */
	public Rubberband(PlotCanvas canvas, IRubberbanded rubberbanded, Policy policy) {
		_canvas = canvas;
		setComponent(canvas);
		_rubberbanded = rubberbanded;
		_policy = policy;
	}

	/**
	 * This activates or deactivates the rubber banding.
	 * 
	 * @param b the value of the active flag.
	 */
	public void setActive(boolean b) {
		_active = b;
	}

	/**
	 * Sets the component being controlled by this rubber band
	 * 
	 * @param component the component being rubberbanded.
	 */
	private void setComponent(Component component) {
		_component = component;

		// Make the component a listener for mouse events.
		_mouseAdapter = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent event) {
				if (isActive()) {
					if (polyMode()) {
						if (_tempPoly == null) {
							startRubberbanding(event.getPoint());
						}
						else {
							if (event.getClickCount() == 2) {
								endRubberbanding(event.getPoint());
							}
							else {
								addPoint(_tempPoly, event.getX(), event.getY());
							}
						}
					}
					else if (radArcMode()) {
						if (_tempPoly == null) {
							startRubberbanding(event.getPoint());
						}
						else {
							addPoint(_tempPoly, event.getX(), event.getY());
							if (_tempPoly.npoints == 3) {
								endRubberbanding(event.getPoint());
							}
						}
					}
					else if (lineMode()) {
						if (_tempPoly == null) {
							startRubberbanding(event.getPoint());
						}
						else {
							addPoint(_tempPoly, event.getX(), event.getY());
							if (_tempPoly.npoints == 2) {
								endRubberbanding(event.getPoint());
							}
						}
					}
					else { // rects and ovals by dragging
						startRubberbanding(event.getPoint());
					}
				}
			}

			@Override
			public void mouseClicked(MouseEvent event) {
				if (isActive()) {
					if (polyMode()) {
						// double click to end
						if (event.getClickCount() > 1) {
							endRubberbanding(event.getPoint());
						}
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent event) {
				if (isActive()) {
					if (normalMode()) {
						endRubberbanding(event.getPoint());
					}
				}
			}
		};

		// Make the rubber band a listener for mouse motion events.
		_mouseMotionAdapter = new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent event) {
				if (isActive()) {
					if (normalMode()) { // rects and ovals
						Point cp = event.getPoint();
						modifyCurrentPoint(cp);
						setCurrent(cp);
					}
				}
			}

			@Override
			public void mouseMoved(MouseEvent event) {
				if (isActive()) {
					if ((polyMode() || radArcMode() || lineMode()) && (_tempPoly != null)) {
						setCurrent(event.getPoint());
					}
				}
			}
		};

		component.addMouseListener(_mouseAdapter);
		component.addMouseMotionListener(_mouseMotionAdapter);
	}

	/**
	 * A hack that prevents the same point from being added consecutively.
	 * 
	 * @param p the polygon in question.
	 * @param x the x coordinate.
	 * @param y the y coordinate.
	 */
	private void addPoint(Polygon p, int x, int y) {
		int n = p.npoints;

		if (n > 0) {
			int xp[] = p.xpoints;
			int yp[] = p.ypoints;
			if ((x == xp[n - 1]) && (y == yp[n - 1])) {
				return;
			}
		}
		p.addPoint(x, y);
	}

	/**
	 * Convenience method to see we are in a poly mode.
	 * 
	 * @return <code>true</code> if we are in a poly mode.
	 */
	private boolean polyMode() {
		return (_policy == Policy.POLYGON) || (_policy == Policy.POLYLINE);
	}

	/**
	 * Convenience method to see we are in radarc mode.
	 * 
	 * @return <code>true</code> if we are in radarc mode.
	 */
	private boolean radArcMode() {
		return (_policy == Policy.RADARC);
	}

	/**
	 * Convenience method to see we are in line mode.
	 * 
	 * @return <code>true</code> if we are in line mode.
	 */
	private boolean lineMode() {
		return (_policy == Policy.LINE);
	}

	/**
	 * Convenience method to see we are in rectangle/ellipse "normal" mode.
	 * 
	 * @return <code>true</code> if we are in rectangle/ellipse "normal" mode.
	 */
	private boolean normalMode() {
		return (!polyMode() && !radArcMode() && !lineMode());
	}

	/**
	 * Draw the rubber band outline. Erasing is not necessary because we are saving
	 * a background image.
	 * 
	 * @param g the graphics context.
	 */
	private void draw(Graphics2D g) {

		Rectangle rect = getRubberbandBounds();

		Polygon tpoly = null;

		switch (_policy) {
		case RECTANGLE:
		case XONLY:
		case YONLY:
		case RECTANGLE_PRESERVE_ASPECT:
			g.fillRect(rect.x, rect.y, rect.width, rect.height);
			GraphicsUtilities.drawHighlightedRectangle(g, rect, _highlightColor1, _highlightColor2);
			break;

		case LINE:
			GraphicsUtilities.drawHighlightedLine(g, _startPt.x, _startPt.y, _currentPt.x, _currentPt.y,
					_highlightColor1, _highlightColor2);
			break;

		case OVAL:
			g.fillOval(rect.x, rect.y, rect.width, rect.height);
			GraphicsUtilities.drawHighlightedOval(g, rect, _highlightColor1, _highlightColor2);
			break;

		case RADARC:

			if (_tempPoly.npoints == 1) {
				GraphicsUtilities.drawHighlightedLine(g, _tempPoly.xpoints[0], _tempPoly.ypoints[0], _currentPt.x,
						_currentPt.y, _highlightColor1, _highlightColor2);
			}
			else if (_tempPoly.npoints == 2) {
				double xc = _tempPoly.xpoints[0];
				double yc = _tempPoly.ypoints[0];
				double x1 = _tempPoly.xpoints[1];
				double y1 = _tempPoly.ypoints[1];
				double x2 = _currentPt.x;
				double y2 = _currentPt.y;
				double dx1 = x1 - xc;
				double dy1 = y1 - yc;
				double dx2 = x2 - xc;
				double dy2 = y2 - yc;
				double r1 = Math.sqrt(dx1 * dx1 + dy1 * dy1);
				double r2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);

				if (r1 < 0.99) {
					return;
				}
				if (r2 < 0.99) {
					return;
				}

				double sAngle = Math.atan2(-dy1, dx1);

				double aAngle = Math.acos((dx1 * dx2 + dy1 * dy2) / (r1 * r2));
				if ((dx1 * dy2 - dx2 * dy1) > 0.0) {
					aAngle = -aAngle;
				}

				// scale to have save radius
				double scale = r1 / r2;
				dx2 *= scale;
				dy2 *= scale;
				x2 = xc + dx2;
				y2 = yc + dy2;

				int startAngle = (int) Math.toDegrees(sAngle);
				int arcAngle = (int) Math.toDegrees(aAngle);

				int pixrad = (int) r1;
				int size = (int) (2 * r1);

				g.fillArc(_tempPoly.xpoints[0] - pixrad, _tempPoly.ypoints[0] - pixrad, size, size, startAngle,
						arcAngle);
				GraphicsUtilities.drawHighlightedArc(g, _tempPoly.xpoints[0] - pixrad, _tempPoly.ypoints[0] - pixrad,
						size, size, startAngle, arcAngle, _highlightColor1, _highlightColor2);

				GraphicsUtilities.drawHighlightedLine(g, _tempPoly.xpoints[0], _tempPoly.ypoints[0],
						_tempPoly.xpoints[1], _tempPoly.ypoints[1], _highlightColor1, _highlightColor2);
				GraphicsUtilities.drawHighlightedLine(g, _tempPoly.xpoints[0], _tempPoly.ypoints[0], (int) x2, (int) y2,
						_highlightColor1, _highlightColor2);
			}

			break;

		case POLYLINE:
			tpoly = new Polygon(_tempPoly.xpoints, _tempPoly.ypoints, _tempPoly.npoints);
			addPoint(tpoly, _currentPt.x, _currentPt.y);
			GraphicsUtilities.drawHighlightedPolyline(g, tpoly.xpoints, tpoly.ypoints, tpoly.npoints, _highlightColor1,
					_highlightColor2);
			break;

		case POLYGON:
			tpoly = new Polygon(_tempPoly.xpoints, _tempPoly.ypoints, _tempPoly.npoints);
			addPoint(tpoly, _currentPt.x, _currentPt.y);
			g.fillPolygon(tpoly);
			GraphicsUtilities.drawHighlightedShape(g, tpoly, _highlightColor1, _highlightColor2);
			break;
		}
	}

	/**
	 * Here the current or stretched point can be modified to affect some
	 * policy--such as preserve the aspect ratio. The default is to do nothing.
	 * 
	 * @param cp the new current point.
	 */
	private void modifyCurrentPoint(Point cp) {
		Rectangle b = _canvas.getActiveBounds();
		if (_policy == Policy.RECTANGLE_PRESERVE_ASPECT) {
			GraphicsUtilities.rectangleARFixedAdjust(b, getStart(), cp);
		}
		else if ((_policy == Policy.XONLY) && (cp != _startPt)) {
			cp.y = b.y + b.height;
		}
		else if ((_policy == Policy.YONLY) && (cp != _startPt)) {
			cp.x = b.x + b.width;
		}

		cp.x = Math.max(1, Math.min(b.x + b.width - 1, cp.x));
		cp.y = Math.max(1, Math.min(b.y + b.height - 1, cp.y));

	}

	/**
	 * Check whether this rubberband is active.
	 * 
	 * @return <code>true</code> if active
	 */
	public boolean isActive() {
		return _active;
	}

	/**
	 * Get the starting screen point.
	 * 
	 * @return the anchor (starting) screen point.
	 */
	public Point getStart() {
		return _startPt;
	}

	/**
	 * Get the current screen point.
	 * 
	 * @return The current screen point.
	 */
	public Point getCurrent() {
		return _currentPt;
	}

	/**
	 * Set the starting point to the given point. Essentially reset the
	 * rubberbanding.
	 * 
	 * @param anchorPt the new starting screen point.
	 */

	public void startRubberbanding(Point anchorPt) {
		if (_started) {
			return;
		}
		_started = true;

		// first image is simply big enough for component

		_image = GraphicsUtilities.getComponentImageBuffer(_component);

		// this image holds background
		_backgroundImage = GraphicsUtilities.getComponentImage(_component);

		// XONLY? (like for a time chart)
		if (_policy == Policy.XONLY) {
			Rectangle b = _canvas.getActiveBounds();
			anchorPt.y = b.y;
		}
		if (_policy == Policy.YONLY) {
			Rectangle b = _canvas.getActiveBounds();
			anchorPt.x = b.x;
		}

		_startPt.setLocation(anchorPt);
		_currentPt.setLocation(anchorPt);

		if (polyMode() || radArcMode() || lineMode()) {
			_tempPoly = new Polygon();
			addPoint(_tempPoly, anchorPt.x, anchorPt.y);
		}
	}

	/**
	 * Set the current point to the given point.
	 * 
	 * @param newCurrentPoint the new current point.
	 */
	private void setCurrent(Point newCurrentPoint) {

		_currentPt.setLocation(newCurrentPoint);
		if (_image == null) {
			return;
		}

		// we are drawing on the bare image

		Graphics2D g2 = _image.createGraphics();
		g2.setColor(_fillColor);

		// copy the background image to the image
		g2.drawImage(_backgroundImage, 0, 0, _component);
		draw(g2);
		g2.dispose();

		Graphics g = _component.getGraphics();
		// this causes that flash the first time i dinna ken why
		// hack with veryFirst prevents flash
		if (veryFirst) {
			veryFirst = false;
		}
		else {
			g.drawImage(_image, 0, 0, _component);
		}
		g.dispose();
	}

	/**
	 * Set the end point.
	 * 
	 * @param p the end point.
	 */
	public void endRubberbanding(Point p) {

		modifyCurrentPoint(p);
		_currentPt.setLocation(p);
		_image = null;
		_backgroundImage = null;
		_poly = _tempPoly;
		_tempPoly = null;
		_active = false;
		_started = false;
		_component.removeMouseListener(_mouseAdapter);
		_component.removeMouseMotionListener(_mouseMotionAdapter);

		// let someone know we are done.
		if (_rubberbanded != null) {
			_rubberbanded.doneRubberbanding();
		}
	}

	/**
	 * Get the component being rubber banded.
	 * 
	 * @return the component being rubber banded.
	 */
	public Component getComponent() {
		return _component;
	}

	/**
	 * This gets the rubber banded polygon, which will be <code>null</code> for
	 * rectangle or oval policies.
	 * 
	 * @return the rubber banded polygon.
	 */
	Polygon getRubberbandPolygon() {
		return _poly;
	}

	/**
	 * Get the vertices of the rubber band. For an oval, it the corners of the
	 * enclosing rectangle.
	 * 
	 * @return the rubber band vertices in a Point array.
	 */
	public Point[] getRubberbandVertices() {
		switch (_policy) {
		case POLYGON:
		case POLYLINE:
		case RADARC:
			if ((_poly == null) || (_poly.npoints < 1)) {
				return null;
			}
			else {
				Point p[] = new Point[_poly.npoints];
				int x[] = _poly.xpoints;
				int y[] = _poly.ypoints;
				for (int i = 0; i < _poly.npoints; i++) {
					p[i] = new Point(x[i], y[i]);
				}
				return p;
			}

		default:
			Rectangle r = getRubberbandBounds();
			Point p[] = new Point[4];
			int left = r.x;
			int top = r.y;
			int right = left + r.width;
			int bottom = top + r.height;
			p[0] = new Point(left, bottom);
			p[1] = new Point(left, top);
			p[2] = new Point(right, top);
			p[3] = new Point(right, bottom);
			return p;
		}
	}

	/**
	 * Return a rectangle that gives the final bounds of the rubber band.
	 * 
	 * @return a bounding rectangle.
	 */
	public Rectangle getRubberbandBounds() {

		if (polyMode() || radArcMode()) {
			if (_poly == null) {
				return null;
			}
			else {
				return _poly.getBounds();
			}
		}
		else { // rect or oval
			return new Rectangle((_currentPt.x < _startPt.x) ? _currentPt.x : _startPt.x,
					(_currentPt.y < _startPt.y) ? _currentPt.y : _startPt.y, Math.abs(_currentPt.x - _startPt.x),
					Math.abs(_currentPt.y - _startPt.y));
		}
	}

	/**
	 * Set the fill color for this rubber band.
	 * 
	 * @param color the new fill color.
	 */
	public void setFillColor(Color color) {
		_fillColor = color;
	}

	/**
	 * Set one of the highlight colors used to stipple draw the boundary.
	 * 
	 * @param highlightColor1 the value for the highlight color.
	 */
	public void setHighlightColor1(Color highlightColor1) {
		_highlightColor1 = highlightColor1;
	}

	/**
	 * Set the other highlight color used to stipple draw the boundary.
	 * 
	 * @param highlightColor2 the value for the other highlight color.
	 */
	public void setHighlightColor2(Color highlightColor2) {
		_highlightColor2 = highlightColor2;
	}

	/**
	 * @return the _startPt
	 */
	public Point getStartPt() {
		return _startPt;
	}

	/**
	 * @return the _currentPt
	 */
	public Point getCurrentPt() {
		return _currentPt;
	}
}