package com.nr.cg;

import static com.nr.NRUtil.*;

public class Triel {
    public final static int DIM = 2;

    /**
     * The array of the points.
     */
    public Point[] pts;
    /**
     * The triangle's three vertices, always in CCW order.
     */
    public int[] p = new int[3];

    /**
     * Pointers for up to three daughters.
     */
    public int[] d = new int[3];

    /**
     * Nonzero if this element is "live."
     */
    public int stat;

    public Triel() {

    }

    public int dim() {
	return DIM;
    }

    public void setme(final int a, final int b, final int c, final Point[] ptss) {
	for (int i = 0; i < ptss.length; i++)
	    if (ptss[i].dim() != DIM)
		throw new IllegalArgumentException("Need same dim!");
	pts = ptss;
	p[0] = a;
	p[1] = b;
	p[2] = c;
	d[0] = d[1] = d[2] = -1; // The values 1 mean no daughters.
	stat = 1; // Create as "live."
    }

    /**
     * Return 1 if point is in the triangle, 0 if on boundary, 1 if outside.
     * (CCW triangle is assumed.)
     * 
     * @param point
     * @return
     */
    public int contains(final Point point) {
	if (point.dim() != DIM)
	    throw new IllegalArgumentException("Need same dim!");
	double d;
	int i, j, ztest = 0;
	for (i = 0; i < 3; i++) {
	    j = (i + 1) % 3;
	    d = (pts[p[j]].x[0] - pts[p[i]].x[0])
		    * (point.x[1] - pts[p[i]].x[1])
		    - (pts[p[j]].x[1] - pts[p[i]].x[1])
		    * (point.x[0] - pts[p[i]].x[0]);
	    if (d < 0.0)
		return -1;
	    if (d == 0.0)
		ztest = 1;
	}
	return (ztest != 0 ? 0 : 1);
    }

    /**
     * Return positive, zero, or negative value if point d is respectively
     * inside, on, or outside the circle through points a, b, and c.
     * 
     * @param d
     * @param a
     * @param b
     * @param c
     * @return
     */
    public static double incircle(final Point d, final Point a, final Point b,
	    final Point c) {
	if (d.dim() != DIM || a.dim() != DIM || c.dim() != DIM)
	    throw new IllegalArgumentException("Need same dim!");
	Circle cc = Circle.circumcircle(a, b, c);
	double radd = SQR(d.x[0] - cc.center.x[0])
		+ SQR(d.x[1] - cc.center.x[1]);
	return (SQR(cc.radius) - radd);
    }
}
