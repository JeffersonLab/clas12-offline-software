package com.nr.cg;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;

public class Point extends Object {

    public double[] x;

    public int dim() {
	return x.length;
    }

    public Point(final int dim) {
	this(dim, new double[dim]);
    }

    public Point(final double x, final double y, final double z) {
	this(3, new double[] { x, y, z });
    }

    public Point(final double x, final double y) {
	this(2, new double[] { x, y });
    }

    public Point(final double x) {
	this(1, new double[] { x });
    }

    public Point(final int dim, final double[] xx) {
	x = new double[dim];
	x[0] = xx[0];
	if (dim > 1)
	    x[1] = xx[1];
	if (dim > 2)
	    x[2] = xx[2];
	if (dim > 3)
	    throw new IllegalArgumentException(
		    "Point not implemented for DIM > 3");
    }

    public Point(final Point p) {
	x = new double[p.dim()];
	for (int i = 0; i < x.length; i++)
	    x[i] = p.x[i];
    }

    public Point copyAssign(final Point p) {
	x = new double[p.dim()];
	for (int i = 0; i < x.length; i++)
	    x[i] = p.x[i];
	return this;
    }

    @Override
    public boolean equals(final Object obj) {
	if (obj instanceof Point) {
	    Point po = (Point) obj;
	    if (po.dim() != x.length)
		return false;
	    for (int i = 0; i < x.length; i++)
		if (x[i] != po.x[i])
		    return false;
	    return true;
	} else
	    return false;
    }

    @Override
    public Point clone() {
	int dim = x.length;
	double[] x0 = new double[x.length];
	System.arraycopy(x, 0, x0, 0, x0.length);
	return new Point(dim, x0);
    }

    public static double dist(final Point p, final Point q) {
	if (p.dim() != q.dim())
	    throw new IllegalArgumentException("need same dim!");

	int DIM = p.dim();
	double dd = 0.0;
	for (int j = 0; j < DIM; j++)
	    dd += SQR(q.x[j] - p.x[j]);
	return sqrt(dd);
    }

}
