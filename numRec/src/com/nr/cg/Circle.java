package com.nr.cg;

import static java.lang.Math.*;

/**
 * circumcircle of three points
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Circle {
    public final static int DIM = 2;
    public Point center;
    public double radius;

    public Circle(final Point cen, final double rad) {
	if (cen.dim() != DIM)
	    new IllegalArgumentException("Need same dim!");
	center = new Point(cen);
	radius = rad;
    }

    @Override
    public Circle clone() {
	return new Circle(center.clone(), radius);
    }

    public int dim() {
	return DIM;
    }

    public static Circle circumcircle(final Point a, final Point b,
	    final Point c) {
	if (a.dim() != DIM || b.dim() != DIM || c.dim() != DIM)
	    throw new IllegalArgumentException("Need same dim!");
	double a0, a1, c0, c1, det, asq, csq, ctr0, ctr1, rad2;
	a0 = a.x[0] - b.x[0];
	a1 = a.x[1] - b.x[1];
	c0 = c.x[0] - b.x[0];
	c1 = c.x[1] - b.x[1];
	det = a0 * c1 - c0 * a1;
	if (det == 0.0)
	    throw new IllegalArgumentException("no circle thru colinear points");
	det = 0.5 / det;
	asq = a0 * a0 + a1 * a1;
	csq = c0 * c0 + c1 * c1;
	ctr0 = det * (asq * c1 - csq * a1);
	ctr1 = det * (csq * a0 - asq * c0);
	rad2 = ctr0 * ctr0 + ctr1 * ctr1;
	return new Circle(new Point(DIM, new double[] { ctr0 + b.x[0],
		ctr1 + b.x[1] }), sqrt(rad2));
    }
}
