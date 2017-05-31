package com.nr.cg;

import static com.nr.cg.Point.*;
import com.nr.sort.Indexx;

public class Minspantree extends Delaunay {
    public int nspan;
    public int[] minsega, minsegb;

    public Minspantree(final Point[] pvec) {
	super(pvec, 0);
	nspan = npts - 1;
	minsega = new int[nspan];
	minsegb = new int[nspan];

	int i, j, k, jj, kk, m, tmp, nline, n = 0;
	Triel tt;
	nline = ntri + npts - 1;
	int[] sega = new int[nline];
	int[] segb = new int[nline];
	double[] segd = new double[nline];
	int[] mo = new int[npts];
	for (j = 0; j < ntree; j++) {
	    if (thelist[j].stat == 0)
		continue;
	    tt = thelist[j];
	    for (i = 0, k = 1; i < 3; i++, k++) {
		if (k == 3)
		    k = 0;
		if (tt.p[i] > tt.p[k])
		    continue;
		if (tt.p[i] >= npts || tt.p[k] >= npts)
		    continue;
		sega[n] = tt.p[i];
		segb[n] = tt.p[k];
		segd[n] = dist(pts[sega[n]], pts[segb[n]]);
		n++;
	    }
	}
	Indexx idx = new Indexx(segd);
	for (j = 0; j < npts; j++)
	    mo[j] = j;
	n = -1;
	for (i = 0; i < nspan; i++) {
	    for (;;) {
		jj = j = idx.el(sega, ++n);
		kk = k = idx.el(segb, n);
		while (mo[jj] != jj)
		    jj = mo[jj];
		while (mo[kk] != kk)
		    kk = mo[kk];
		if (jj != kk) {
		    minsega[i] = j;
		    minsegb[i] = k;
		    m = mo[jj] = kk;
		    jj = j;
		    while (mo[jj] != m) {
			tmp = mo[jj];
			mo[jj] = m;
			jj = tmp;
		    }
		    kk = k;
		    while (mo[kk] != m) {
			tmp = mo[kk];
			mo[kk] = m;
			kk = tmp;
		    }
		    break;
		}
	    }
	}
    }
}
