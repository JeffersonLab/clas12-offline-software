package com.nr.cg;

/**
 * Structure for constructing the convex hull of a set of points in the plane.
 * After construction, nhull is the number of points in the hull, and
 * hullpoints[0..nhull-1] are integers pointing to points in the vector pvec
 * that are in the hull, in CCW order.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Convexhull extends Delaunay {
    public int nhull;
    public int[] hullpts;

    public Convexhull(final Point[] pvec) {
	super(pvec, 2);
	nhull = 0;
	int i, j, k, pstart = 0;
	int[] nextpt = new int[npts];
	for (j = 0; j < ntree; j++) {
	    if (thelist[j].stat != -1)
		continue;
	    for (i = 0, k = 1; i < 3; i++, k++) {
		if (k == 3)
		    k = 0;
		if (thelist[j].p[i] < npts && thelist[j].p[k] < npts)
		    break;
	    }
	    if (i == 3)
		continue;
	    ++nhull;
	    nextpt[(pstart = thelist[j].p[k])] = thelist[j].p[i];
	}
	if (nhull == 0)
	    throw new IllegalArgumentException("no hull segments found");
	hullpts = new int[nhull];
	j = 0;
	i = hullpts[j++] = pstart;
	while ((i = nextpt[i]) != pstart)
	    hullpts[j++] = i;
    }
}
