package cnuphys.bCNU.graphics.world;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

public class WorldPolygon {
	
	/**
	 * The total number of points. The value of <code>npoints</code> represents
	 * the number of valid points in this <code>Polygon</code> and might be less
	 * than the number of elements in {@link #xpoints xpoints} or
	 * {@link #ypoints ypoints}. This value can be NULL.
	 */

	public int npoints;
	
	/**
	 * The array of X coordinates. The number of elements in this array might be
	 * more than the number of X coordinates in this <code>Polygon</code>. The
	 * extra elements allow new points to be added to this <code>Polygon</code>
	 * without re-creating this array. The value of {@link #npoints npoints} is
	 * equal to the number of valid points in this <code>Polygon</code>.
	 */
	public double xpoints[];

	/**
	 * The array of Y coordinates. The number of elements in this array might be
	 * more than the number of Y coordinates in this <code>Polygon</code>. The
	 * extra elements allow new points to be added to this <code>Polygon</code>
	 * without re-creating this array. The value of <code>npoints</code> is
	 * equal to the number of valid points in this <code>Polygon</code>.
	 * 
	 * @serial
	 * @since 1.0
	 */
	public double ypoints[];

	/**
	 * The bounds of this {@code Polygon}. This value can be null.
	 */
	protected Rectangle2D.Double bounds;
	
	/*
	 * Default length for xpoints and ypoints.
	 */
	private static final int MIN_LENGTH = 4;

	/**
	 * Creates an empty polygon.
	 * 
	 * @since 1.0
	 */
	public WorldPolygon() {
		xpoints = new double[MIN_LENGTH];
		ypoints = new double[MIN_LENGTH];
	}
	
	/**
	 * Constructs and initializes a <code>Polygon</code> from the specified
	 * parameters.
	 * 
	 * @param xpoints
	 *            an array of X coordinates
	 * @param ypoints
	 *            an array of Y coordinates
	 * @param npoints
	 *            the total number of points in the <code>Polygon</code>
	 * @exception NegativeArraySizeException
	 *                if the value of <code>npoints</code> is negative.
	 * @exception IndexOutOfBoundsException
	 *                if <code>npoints</code> is greater than the length of
	 *                <code>xpoints</code> or the length of <code>ypoints</code>
	 *                .
	 * @exception NullPointerException
	 *                if <code>xpoints</code> or <code>ypoints</code> is
	 *                <code>null</code>.
	 * @since 1.0
	 */
	public WorldPolygon(double xpoints[], double ypoints[], int npoints) {
		// Fix 4489009: should throw IndexOutofBoundsException instead
		// of OutofMemoryException if npoints is huge and > {x,y}points.length
		if (npoints > xpoints.length || npoints > ypoints.length) {
			throw new IndexOutOfBoundsException("npoints > xpoints.length || "
					+ "npoints > ypoints.length");
		}
		// Fix 6191114: should throw NegativeArraySizeException with
		// negative npoints
		if (npoints < 0) {
			throw new NegativeArraySizeException("npoints < 0");
		}
		// Fix 6343431: Applet compatibility problems if arrays are not
		// exactly npoints in length
		this.npoints = npoints;
		this.xpoints = Arrays.copyOf(xpoints, npoints);
		this.ypoints = Arrays.copyOf(ypoints, npoints);
	}

	public WorldPolygon(Point2D.Double w1, Point2D.Double w2,
			Point2D.Double w3, Point2D.Double w4) {
		xpoints = new double[4];
		ypoints = new double[4];
		xpoints[0] = w1.x;
		ypoints[0] = w1.y;
		xpoints[1] = w2.x;
		ypoints[1] = w2.y;
		xpoints[2] = w3.x;
		ypoints[2] = w3.y;
		xpoints[3] = w4.x;
		ypoints[3] = w4.y;
		npoints = 4;

	}

	/**
	 * Resets this <code>Polygon</code> object to an empty polygon. The
	 * coordinate arrays and the data in them are left untouched but the number
	 * of points is reset to zero to mark the old vertex data as invalid and to
	 * start accumulating new vertex data at the beginning. All
	 * internally-cached data relating to the old vertices are discarded. Note
	 * that since the coordinate arrays from before the reset are reused,
	 * creating a new empty <code>Polygon</code> might be more memory efficient
	 * than resetting the current one if the number of vertices in the new
	 * polygon data is significantly smaller than the number of vertices in the
	 * data from before the reset.
	 * 
	 * @see java.awt.Polygon#invalidate
	 * @since 1.4
	 */
	public void reset() {
		npoints = 0;
		bounds = null;
	}

	/**
	 * Invalidates or flushes any internally-cached data that depends on the
	 * vertex coordinates of this <code>Polygon</code>. This method should be
	 * called after any direct manipulation of the coordinates in the
	 * <code>xpoints</code> or <code>ypoints</code> arrays to avoid inconsistent
	 * results from methods such as <code>getBounds</code> or
	 * <code>contains</code> that might cache data from earlier computations
	 * relating to the vertex coordinates.
	 * 
	 * @see java.awt.Polygon#getBounds
	 * @since 1.4
	 */
	public void invalidate() {
		bounds = null;
	}

	/*
	 * Calculates the bounding box of the points passed to the constructor. Sets
	 * <code>bounds</code> to the result.
	 * 
	 * @param xpoints[] array of <i>x</i> coordinates
	 * 
	 * @param ypoints[] array of <i>y</i> coordinates
	 * 
	 * @param npoints the total number of points
	 */
	void calculateBounds(double xpoints[], double ypoints[], int npoints) {
		double boundsMinX = Double.MAX_VALUE;
		double boundsMinY = Double.MAX_VALUE;
		double boundsMaxX = Double.MIN_VALUE;
		double boundsMaxY = Double.MIN_VALUE;

		for (int i = 0; i < npoints; i++) {
			double x = xpoints[i];
			boundsMinX = Math.min(boundsMinX, x);
			boundsMaxX = Math.max(boundsMaxX, x);
			double y = ypoints[i];
			boundsMinY = Math.min(boundsMinY, y);
			boundsMaxY = Math.max(boundsMaxY, y);
		}
		bounds = new Rectangle2D.Double(boundsMinX, boundsMinY, boundsMaxX
				- boundsMinX, boundsMaxY - boundsMinY);
	}

	/*
	 * Resizes the bounding box to accomodate the specified coordinates.
	 * 
	 * @param x,&nbsp;y the specified coordinates
	 */
	void updateBounds(double x, double y) {
		if (x < bounds.x) {
			bounds.width = bounds.width + (bounds.x - x);
			bounds.x = x;
		} else {
			bounds.width = Math.max(bounds.width, x - bounds.x);
			// bounds.x = bounds.x;
		}

		if (y < bounds.y) {
			bounds.height = bounds.height + (bounds.y - y);
			bounds.y = y;
		} else {
			bounds.height = Math.max(bounds.height, y - bounds.y);
			// bounds.y = bounds.y;
		}
	}

	/**
	 * Appends the specified coordinates to this <code>Polygon</code>.
	 * <p>
	 * If an operation that calculates the bounding box of this
	 * <code>Polygon</code> has already been performed, such as
	 * <code>getBounds</code> or <code>contains</code>, then this method updates
	 * the bounding box.
	 * 
	 * @param x
	 *            the specified X coordinate
	 * @param y
	 *            the specified Y coordinate
	 * @see java.awt.Polygon#getBounds
	 * @see java.awt.Polygon#contains
	 * @since 1.0
	 */
	public void addPoint(double x, double y) {
		if (npoints >= xpoints.length || npoints >= ypoints.length) {
			int newLength = npoints * 2;
			// Make sure that newLength will be greater than MIN_LENGTH and
			// aligned to the power of 2
			if (newLength < MIN_LENGTH) {
				newLength = MIN_LENGTH;
			} else if ((newLength & (newLength - 1)) != 0) {
				newLength = Integer.highestOneBit(newLength);
			}

			xpoints = Arrays.copyOf(xpoints, newLength);
			ypoints = Arrays.copyOf(ypoints, newLength);
		}
		xpoints[npoints] = x;
		ypoints[npoints] = y;
		npoints++;
		if (bounds != null) {
			updateBounds(x, y);
		}
	}

	/**
	 * Gets the bounding box of this <code>Polygon</code>. The bounding box is
	 * the smallest {@link Rectangle} whose sides are parallel to the x and y
	 * axes of the coordinate space, and can completely contain the
	 * <code>Polygon</code>.
	 * 
	 * @return a <code>Rectangle</code> that defines the bounds of this
	 *         <code>Polygon</code>.
	 * @since 1.1
	 */
	public Rectangle getBounds() {
		return getBoundingBox();
	}

	/**
	 * Determines whether the specified coordinates are inside this
	 * <code>Polygon</code>.
	 * <p>
	 * 
	 * @param x
	 *            the specified X coordinate to be tested
	 * @param y
	 *            the specified Y coordinate to be tested
	 * @return {@code true} if this {@code Polygon} contains the specified
	 *         coordinates {@code (x,y)}; {@code false} otherwise.
	 * @see #contains(double, double)
	 * @since 1.1
	 */
	public boolean contains(int x, int y) {
		return contains((double) x, (double) y);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @since 1.2
	 */
	public boolean contains(double x, double y) {
		if (npoints <= 2 || !getBoundingBox().contains(x, y)) {
			return false;
		}
		int hits = 0;

		double lastx = xpoints[npoints - 1];
		double lasty = ypoints[npoints - 1];
		double curx, cury;

		// Walk the edges of the polygon
		for (int i = 0; i < npoints; lastx = curx, lasty = cury, i++) {
			curx = xpoints[i];
			cury = ypoints[i];

			if (cury == lasty) {
				continue;
			}

			double leftx;
			if (curx < lastx) {
				if (x >= lastx) {
					continue;
				}
				leftx = curx;
			} else {
				if (x >= curx) {
					continue;
				}
				leftx = lastx;
			}

			double test1, test2;
			if (cury < lasty) {
				if (y < cury || y >= lasty) {
					continue;
				}
				if (x < leftx) {
					hits++;
					continue;
				}
				test1 = x - curx;
				test2 = y - cury;
			} else {
				if (y < lasty || y >= cury) {
					continue;
				}
				if (x < leftx) {
					hits++;
					continue;
				}
				test1 = x - lastx;
				test2 = y - lasty;
			}

			if (test1 < (test2 / (lasty - cury) * (lastx - curx))) {
				hits++;
			}
		}

		return ((hits & 1) != 0);
	}

	
	/**
	 * Returns the bounds of this <code>Polygon</code>.
	 * 
	 * @return the bounds of this <code>Polygon</code>.
	 * @since 1.0
	 */
	public Rectangle getBoundingBox() {
		if (npoints == 0) {
			return new Rectangle();
		}
		if (bounds == null) {
			calculateBounds(xpoints, ypoints, npoints);
		}
		return bounds.getBounds();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 */
	public boolean contains(Point2D p) {
		return contains(p.getX(), p.getY());
	}
	


	public PathIterator getPathIterator(AffineTransform arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public PathIterator getPathIterator(AffineTransform arg0, double arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
