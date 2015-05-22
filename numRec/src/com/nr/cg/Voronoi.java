package com.nr.cg;

import static com.nr.cg.Circle.*;

/**
 * Constructor for Voronoi diagram of a vector of sites pvec. Bit "1" sent to
 * the Delaunay constructor tells it not to delete linehash.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Voronoi extends Delaunay {
    public int nseg;
    public int[] trindx;
    public Voredge[] segs;

    public static class Voredge {
	public Point[] p = new Point[2];
	public int nearpt;

	public Voredge(final Point pa, final Point pb, final int np) {
	    if (pa.dim() != DIM || pb.dim() != DIM)
		throw new IllegalArgumentException("Need same dim!");
	    nearpt = np;
	    p[0] = pa;
	    p[1] = pb;
	}
    }

    public Voronoi(final Point[] pvec) {
	super(pvec, 1);
	nseg = 0;
	trindx = new int[npts];
	segs = new Voredge[6 * npts + 12];
	int i, j, k, p, jfirst;
	long key;
	Triel tt;
	Point cc, ccp;
	for (j = 0; j < ntree; j++) {
	    if (thelist[j].stat <= 0)
		continue;
	    tt = thelist[j];
	    for (k = 0; k < 3; k++)
		trindx[tt.p[k]] = j;
	}
	for (p = 0; p < npts; p++) {
	    tt = thelist[trindx[p]];
	    if (tt.p[0] == p) {
		i = tt.p[1];
		j = tt.p[2];
	    } else if (tt.p[1] == p) {
		i = tt.p[2];
		j = tt.p[0];
	    } else if (tt.p[2] == p) {
		i = tt.p[0];
		j = tt.p[1];
	    } else
		throw new IllegalArgumentException("triangle should contain p");
	    jfirst = j;
	    ccp = circumcircle(pts[p], pts[i], pts[j]).center;
	    while (true) {
		key = hashfn.int64p(i) - hashfn.int64p(p);
		Integer[] k_w = new Integer[1];
		if (linehash.get(key, k_w, 0) == 0)
		    throw new IllegalArgumentException("Delaunay is incomplete");
		k = k_w[0];
		cc = circumcircle(pts[p], pts[k], pts[i]).center;
		segs[nseg++] = new Voredge(ccp, cc, p);
		if (k == jfirst)
		    break;
		ccp = cc;
		j = i;
		i = k;
	    }
	}
    }
}
