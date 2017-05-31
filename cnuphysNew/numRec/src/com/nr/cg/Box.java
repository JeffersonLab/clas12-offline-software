package com.nr.cg;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;

public class Box {
    public Point lo, hi;

    public Box(int dim) {
	lo = new Point(dim);
	hi = new Point(dim);
    }

    public Box(final Point mylo, final Point myhi) {
	if (mylo.dim() != myhi.dim())
	    throw new IllegalArgumentException("need same dim!");
	lo = new Point(mylo);
	hi = new Point(myhi);
    }

    public Box(final Box p) {
	lo = new Point(p.lo);
	hi = new Point(p.hi);
    }

    public Box copyAssign(final Box p) {
	lo = lo.copyAssign(p.lo);
	hi = hi.copyAssign(p.hi);
	return this;
    }

    @Override
    public Box clone() {
	return new Box(lo.clone(), hi.clone());
    }

    public static double dist(final Box b, final Point p) {
	if (b.lo.dim() != p.dim())
	    throw new IllegalArgumentException("need same dim!");

	int DIM = p.dim();

	double dd = 0;
	for (int i = 0; i < DIM; i++) {
	    if (p.x[i] < b.lo.x[i])
		dd += SQR(p.x[i] - b.lo.x[i]);
	    if (p.x[i] > b.hi.x[i])
		dd += SQR(p.x[i] - b.hi.x[i]);
	}
	return sqrt(dd);
    }

    public int dim() {
	return lo.dim();
    }
}
