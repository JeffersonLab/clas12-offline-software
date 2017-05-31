package com.nr.min;

import static java.lang.Math.*;
import com.nr.ran.Ranq1;

/**
 * simulated annealing Copyright (C) Numerical Recipes Software 1986-2007 Java
 * translation Copyright (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Anneal {
    Ranq1 myran = new Ranq1(1234);

    public Anneal() {
    }

    public double order(final double[] x, final double[] y, final int[] iorder) {
	final double TFACTR = 0.9;
	boolean ans;
	int i, i1, i2, nn;
	int[] n = new int[6];
	double de, path = 0.0, t = 0.5;
	int ncity = x.length;
	int nover = 100 * ncity;
	int nlimit = 10 * ncity;
	for (i = 0; i < ncity - 1; i++) {
	    i1 = iorder[i];
	    i2 = iorder[i + 1];
	    path += alen(x[i1], x[i2], y[i1], y[i2]);
	}
	i1 = iorder[ncity - 1];
	i2 = iorder[0];
	path += alen(x[i1], x[i2], y[i1], y[i2]);
	// cout << fixed << setprecision(6);
	for (int j = 0; j < 100; j++) {
	    int nsucc = 0;
	    for (int k = 0; k < nover; k++) {
		do {
		    n[0] = (int) (ncity * myran.doub());
		    n[1] = (int) ((ncity - 1) * myran.doub());
		    if (n[1] >= n[0])
			++n[1];
		    nn = (n[0] - n[1] + ncity - 1) % ncity;
		} while (nn < 2);
		if (myran.doub() < 0.5) {
		    n[2] = n[1] + (int) (abs(nn - 1) * myran.doub()) + 1;
		    n[2] %= ncity;
		    de = trncst(x, y, iorder, n);
		    ans = metrop(de, t);
		    if (ans) {
			++nsucc;
			path += de;
			trnspt(iorder, n);
		    }
		} else {
		    de = revcst(x, y, iorder, n);
		    ans = metrop(de, t);
		    if (ans) {
			++nsucc;
			path += de;
			reverse(iorder, n);
		    }
		}
		if (nsucc >= nlimit)
		    break;
	    }
	    // cout << endl << "T = " << setw(12) << t;
	    // cout << "  Path Length = " << setw(12) << path << endl;
	    // cout << "Successful Moves: " << nsucc << endl;
	    t *= TFACTR;
	    if (nsucc == 0)
		return path;
	}

	return path;
    }

    double revcst(final double[] x, final double[] y, final int[] iorder,
	    final int[] n) {
	double[] xx = new double[4];
	double[] yy = new double[4];
	int ncity = x.length;
	n[2] = (n[0] + ncity - 1) % ncity;
	n[3] = (n[1] + 1) % ncity;
	for (int j = 0; j < 4; j++) {
	    int ii = iorder[n[j]];
	    xx[j] = x[ii];
	    yy[j] = y[ii];
	}
	double de = -alen(xx[0], xx[2], yy[0], yy[2]);
	de -= alen(xx[1], xx[3], yy[1], yy[3]);
	de += alen(xx[0], xx[3], yy[0], yy[3]);
	de += alen(xx[1], xx[2], yy[1], yy[2]);
	return de;
    }

    void reverse(final int[] iorder, final int[] n) {
	int ncity = iorder.length;
	int nn = (1 + ((n[1] - n[0] + ncity) % ncity)) / 2;
	for (int j = 0; j < nn; j++) {
	    int k = (n[0] + j) % ncity;
	    int l = (n[1] - j + ncity) % ncity;
	    int itmp = iorder[k];
	    iorder[k] = iorder[l];
	    iorder[l] = itmp;
	}
    }

    double trncst(final double[] x, final double[] y, final int[] iorder,
	    final int[] n) {
	double[] xx = new double[6];
	double[] yy = new double[6];
	int ncity = x.length;
	n[3] = (n[2] + 1) % ncity;
	n[4] = (n[0] + ncity - 1) % ncity;
	n[5] = (n[1] + 1) % ncity;
	for (int j = 0; j < 6; j++) {
	    int ii = iorder[n[j]];
	    xx[j] = x[ii];
	    yy[j] = y[ii];
	}
	double de = -alen(xx[1], xx[5], yy[1], yy[5]);
	de -= alen(xx[0], xx[4], yy[0], yy[4]);
	de -= alen(xx[2], xx[3], yy[2], yy[3]);
	de += alen(xx[0], xx[2], yy[0], yy[2]);
	de += alen(xx[1], xx[3], yy[1], yy[3]);
	de += alen(xx[4], xx[5], yy[4], yy[5]);
	return de;
    }

    public void trnspt(final int[] iorder, final int[] n) {
	int ncity = iorder.length;
	int[] jorder = new int[ncity];
	int m1 = (n[1] - n[0] + ncity) % ncity;
	int m2 = (n[4] - n[3] + ncity) % ncity;
	int m3 = (n[2] - n[5] + ncity) % ncity;
	int nn = 0;
	for (int j = 0; j <= m1; j++) {
	    int jj = (j + n[0]) % ncity;
	    jorder[nn++] = iorder[jj];
	}
	for (int j = 0; j <= m2; j++) {
	    int jj = (j + n[3]) % ncity;
	    jorder[nn++] = iorder[jj];
	}
	for (int j = 0; j <= m3; j++) {
	    int jj = (j + n[5]) % ncity;
	    jorder[nn++] = iorder[jj];
	}
	for (int j = 0; j < ncity; j++)
	    iorder[j] = jorder[j];
    }

    public boolean metrop(final double de, final double t) {
	return de < 0.0 || myran.doub() < exp(-de / t);
    }

    public double alen(final double a, final double b, final double c,
	    final double d) {
	return sqrt((b - a) * (b - a) + (d - c) * (d - c));
    }
}
