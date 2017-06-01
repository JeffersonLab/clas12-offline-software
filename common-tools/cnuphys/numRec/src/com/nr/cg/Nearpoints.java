package com.nr.cg;

/**
 * 
 * Object for constructing a QO tree containing a set of points, and for
 * repeatedly querying which stored points are within a specified radius of a
 * specified new point.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Nearpoints {
    final int DIM;
    int npts;
    Qotree thetree;
    Sphcirc[] sphlist;

    public Nearpoints(final int dim, final Point[] pvec) {
	DIM = dim;
	for (Point p : pvec)
	    if (p.dim() != DIM)
		throw new IllegalArgumentException("Need same dim!");
	npts = pvec.length;
	thetree = new Qotree(dim, npts, npts, 32 / DIM); // Set the tree's outer
							 // box and store all
							 // the points.
	int j, k;
	sphlist = new Sphcirc[npts];
	Point lo = pvec[0], hi = pvec[0]; // Find bounding box for the points
	for (j = 1; j < npts; j++)
	    for (k = 0; k < DIM; k++) {
		if (pvec[j].x[k] < lo.x[k])
		    lo.x[k] = pvec[j].x[k];
		if (pvec[j].x[k] > hi.x[k])
		    hi.x[k] = pvec[j].x[k];
	    }
	// Expand it by 10% so that all points are well interior.
	for (k = 0; k < DIM; k++) {
	    lo.x[k] -= 0.1 * (hi.x[k] - lo.x[k]);
	    hi.x[k] += 0.1 * (hi.x[k] - lo.x[k]);
	}
	thetree.setouterbox(lo, hi);
	for (j = 0; j < npts; j++)
	    thetree.qostore(new Sphcirc(pvec[j], 0.0));
    }

    public int dim() {
	return DIM;
    }

    /**
     * 
     * Once the tree is constructed, this function can be called repeatedly with
     * varying points pt and radii r. It returns n, the number of stored points
     * within radius r of pt (but no larger than nmax), and copies those points
     * into list[0..n-1].
     * 
     * @param pt
     * @param r
     * @param list
     * @param nmax
     * @return
     */
    public int locatenear(final Point pt, final double r, final Point[] list,
	    final int nmax) {
	int j, n;
	n = thetree.qocollides(new Sphcirc(pt, r), sphlist, nmax);
	for (j = 0; j < n; j++)
	    list[j] = sphlist[j].center;
	return n;
    }
}
