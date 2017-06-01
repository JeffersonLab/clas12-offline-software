package com.nr.cg;

import static com.nr.cg.Triel.*;
import com.nr.ran.Hash;
import com.nr.ran.Ranhash;

import static com.nr.NRUtil.*;

/**
 * Delaunay triangulation Copyright (C) Numerical Recipes Software 1986-2007
 * Java translation Copyright (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Delaunay {
    static final int DIM = 2;
    private static final double fuzz = 1.0e-6, bigscale = 1000.0;

    /**
     * Random number counter
     */
    private static int jran = 14921620;
    public int npts, ntri, ntree, ntreemax, opt;
    public double delx, dely;
    public Point[] pts;
    public Triel[] thelist;

    /**
     * Create the hash memories with null hash function.
     */
    public Hash1 linehash;
    public Hash1 trihash;
    public int[] perm;

    /**
     * The raw hash function
     */
    public Ranhash hashfn = new Ranhash();

    public class Hash1 extends Hash<Long, Integer> {
	public Hash1(final int nh, final int nm) {
	    super(nh, nm);
	}

	@Override
	public long fn(final Long k) {
	    return k & 0x7FFFFFFFFFFFFFFFL;
	}
    }

    public int dim() {
	return DIM;
    }

    public Delaunay(final Point[] pvec) {
	this(pvec, 0);
    }

    /**
     * Construct Delaunay triangulation from a vector of points pvec. If bit 0
     * in options is nonzero, hash memories used in the construction are
     * deleted. (Some applications may want to use them and will set options to
     * 1.)
     * 
     * @param pvec
     * @param options
     */
    public Delaunay(final Point[] pvec, final int options) {
	for (int i = 0; i < pvec.length; i++)
	    if (pvec[i].dim() != DIM)
		throw new IllegalArgumentException("Need same dim!");

	npts = pvec.length;
	ntri = 0;
	ntree = 0;
	ntreemax = 10 * npts + 1000;
	opt = options;
	pts = new Point[npts + 3];
	thelist = new Triel[ntreemax];
	for (int i = 0; i < ntreemax; i++)
	    thelist[i] = new Triel();

	int j;
	double xl, xh, yl, yh;
	linehash = new Hash1(6 * npts + 12, 6 * npts + 12);
	trihash = new Hash1(2 * npts + 6, 2 * npts + 6);
	perm = new int[npts];
	xl = xh = pvec[0].x[0];
	yl = yh = pvec[0].x[1];
	for (j = 0; j < npts; j++) {
	    pts[j] = pvec[j];
	    perm[j] = j;
	    if (pvec[j].x[0] < xl)
		xl = pvec[j].x[0];
	    if (pvec[j].x[0] > xh)
		xh = pvec[j].x[0];
	    if (pvec[j].x[1] < yl)
		yl = pvec[j].x[1];
	    if (pvec[j].x[1] > yh)
		yh = pvec[j].x[1];
	}
	delx = xh - xl;
	dely = yh - yl;
	pts[npts] = new Point(0.5 * (xl + xh), yh + bigscale * dely);
	pts[npts + 1] = new Point(xl - 0.5 * bigscale * delx, yl - 0.5
		* bigscale * dely);
	pts[npts + 2] = new Point(xh + 0.5 * bigscale * delx, yl - 0.5
		* bigscale * dely);
	storetriangle(npts, npts + 1, npts + 2);
	for (j = npts; j > 0; j--) {
	    // SWAP(perm[j-1],perm[hashfn.int64p(jran++) % j]);
	    int nnn = (int) (hashfn.int64p(jran++) % j);
	    swap(perm, j - 1, nnn);
	}
	for (j = 0; j < npts; j++)
	    insertapoint(perm[j]);
	for (j = 0; j < ntree; j++) {
	    if (thelist[j].stat > 0) {
		if (thelist[j].p[0] >= npts || thelist[j].p[1] >= npts
			|| thelist[j].p[2] >= npts) {
		    thelist[j].stat = -1;
		    ntri--;
		}
	    }
	}
	/*
	 * if (!(opt & 1)) { delete [] perm; delete trihash; delete linehash; }
	 */
    }

    /**
     * Add the point with index r incrementally to the Delaunay triangulation
     * 
     * @param r
     */
    public void insertapoint(final int r) {
	int i, j, k, l, s, tno = 0, ntask, d0, d1, d2;
	long key;
	int[] tasks = new int[50], taski = new int[50], taskj = new int[50];
	for (j = 0; j < 3; j++) {
	    tno = whichcontainspt(pts[r], 1);
	    if (tno >= 0)
		break;
	    pts[r].x[0] += fuzz * delx * (hashfn.doub(jran++) - 0.5);
	    pts[r].x[1] += fuzz * dely * (hashfn.doub(jran++) - 0.5);
	}
	if (j == 3)
	    throw new IllegalArgumentException(
		    "points degenerate even after fuzzing");
	ntask = 0;
	i = thelist[tno].p[0];
	j = thelist[tno].p[1];
	k = thelist[tno].p[2];
	if ((opt & 2) != 0 && i < npts && j < npts && k < npts)
	    return;
	d0 = storetriangle(r, i, j);
	tasks[++ntask] = r;
	taski[ntask] = i;
	taskj[ntask] = j;
	d1 = storetriangle(r, j, k);
	tasks[++ntask] = r;
	taski[ntask] = j;
	taskj[ntask] = k;
	d2 = storetriangle(r, k, i);
	tasks[++ntask] = r;
	taski[ntask] = k;
	taskj[ntask] = i;
	erasetriangle(i, j, k, d0, d1, d2);
	while (ntask != 0) {
	    s = tasks[ntask];
	    i = taski[ntask];
	    j = taskj[ntask--];
	    key = hashfn.int64(j) - hashfn.int64(i);
	    Integer[] l_w = new Integer[1];
	    if (linehash.get(key, l_w, 0) == 0)
		continue;
	    l = l_w[0];
	    if (incircle(pts[l], pts[j], pts[s], pts[i]) > 0.0) {
		d0 = storetriangle(s, l, j);
		d1 = storetriangle(s, i, l);
		erasetriangle(s, i, j, d0, d1, -1);
		erasetriangle(l, j, i, d0, d1, -1);
		key = hashfn.int64(i) - hashfn.int64(j);
		linehash.erase(key);
		key = 0 - key;
		linehash.erase(key);
		tasks[++ntask] = s;
		taski[ntask] = l;
		taskj[ntask] = j;
		tasks[++ntask] = s;
		taski[ntask] = i;
		taskj[ntask] = l;
	    }
	}
    }

    public int whichcontainspt(final Point p) {
	return whichcontainspt(p, 0);
    }

    /**
     * Given point p, return index in thelist of the triangle in the
     * triangulation that contains it, or return 1 for failure. If strict is
     * nonzero, require strict containment, otherwise allow the point to lie on
     * an edge.
     * 
     * @param p
     * @param strict
     * @return
     */
    public int whichcontainspt(final Point p, final int strict) {
	int i, j = 0, k = 0;
	while (thelist[k].stat <= 0) {
	    for (i = 0; i < 3; i++) {
		if ((j = thelist[k].d[i]) < 0)
		    continue;
		if (strict != 0) {
		    if (thelist[j].contains(p) > 0)
			break;
		} else {
		    if (thelist[j].contains(p) >= 0)
			break;
		}
	    }
	    if (i == 3)
		return -1;
	    k = j;
	}
	return k;
    }

    /**
     * Erase triangle abc in trihash and inactivate it in thelist after setting
     * its daughters.
     * 
     * @param a
     * @param b
     * @param c
     * @param d0
     * @param d1
     * @param d2
     */
    public void erasetriangle(final int a, final int b, final int c,
	    final int d0, final int d1, final int d2) {
	long key;
	int j;
	key = hashfn.int64(a) ^ hashfn.int64(b) ^ hashfn.int64(c);
	Integer[] j_w = new Integer[1];
	if (trihash.get(key, j_w, 0) == 0)
	    throw new IllegalArgumentException("nonexistent triangle");
	j = j_w[0];
	trihash.erase(key);
	thelist[j].d[0] = d0;
	thelist[j].d[1] = d1;
	thelist[j].d[2] = d2;
	thelist[j].stat = 0;
	ntri--;
    }

    /**
     * Store a triangle with vertices a, b, c in trihash. Store its points in
     * linehash under keys to opposite sides. Add it to thelist, returning its
     * index there.
     * 
     * @param a
     * @param b
     * @param c
     * @return
     */
    public int storetriangle(final int a, final int b, final int c) {
	long key;
	thelist[ntree].setme(a, b, c, pts);
	key = hashfn.int64(a) ^ hashfn.int64(b) ^ hashfn.int64(c);
	trihash.set(key, ntree);
	key = hashfn.int64(b) - hashfn.int64(c);
	linehash.set(key, a);
	key = hashfn.int64(c) - hashfn.int64(a);
	linehash.set(key, b);
	key = hashfn.int64(a) - hashfn.int64(b);
	linehash.set(key, c);
	if (++ntree == ntreemax)
	    throw new IllegalArgumentException("thelist is sized too small");
	ntri++;
	return (ntree - 1);
    }

    public double interpolate(final Point p, final double[] fnvals,
	    final double defaultval) {
	int n, i, j, k;
	double[] wgts = new double[3];
	int[] ipts = new int[3];
	double sum, ans = 0.0;
	n = whichcontainspt(p);
	if (n < 0)
	    return defaultval;
	for (i = 0; i < 3; i++)
	    ipts[i] = thelist[n].p[i];
	for (i = 0, j = 1, k = 2; i < 3; i++, j++, k++) {
	    if (j == 3)
		j = 0;
	    if (k == 3)
		k = 0;
	    wgts[k] = (pts[ipts[j]].x[0] - pts[ipts[i]].x[0])
		    * (p.x[1] - pts[ipts[i]].x[1])
		    - (pts[ipts[j]].x[1] - pts[ipts[i]].x[1])
		    * (p.x[0] - pts[ipts[i]].x[0]);
	}
	sum = wgts[0] + wgts[1] + wgts[2];
	if (sum == 0)
	    throw new IllegalArgumentException("degenerate triangle");
	for (i = 0; i < 3; i++)
	    ans += wgts[i] * fnvals[ipts[i]] / sum;
	return ans;
    }
}
