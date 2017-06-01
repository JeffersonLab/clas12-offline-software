package com.nr.ode;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;
import com.nr.la.NRsparseCol;
import com.nr.ran.Ran;

/**
 * stochastic simulation of ODEs Copyright (C) Numerical Recipes Software
 * 1986-2007 Java translation Copyright (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Stochsim {
    public double[] s;
    public double[] a;
    public double[][] instate, outstate;
    public NRsparseCol[] outchg, depend;
    public int[] pr;
    public double t, asum;
    private Ran ran;

    // begin user section
    static final int mm = 3;
    static final int nn = 4;

    public double k0, k1, k2;

    public Stochsim(final double[] sinit) {
	this(sinit, 1);
    }

    public Stochsim(final double[] sinit, final int seed) {
	s = sinit;
	a = new double[mm];
	outchg = new NRsparseCol[mm];
	depend = new NRsparseCol[mm];
	for (int i = 0; i < mm; i++) {
	    outchg[i] = new NRsparseCol();
	    depend[i] = new NRsparseCol();
	}
	pr = new int[mm];
	t = 0;
	asum = 0;
	ran = new Ran(seed);

	int i, j, k, d;
	describereactions();
	sparmatfill(outchg, outstate);
	double[][] dep = new double[mm][mm];
	for (i = 0; i < mm; i++)
	    for (j = 0; j < mm; j++) {
		d = 0;
		for (k = 0; k < nn; k++)
		    d = d | ((int) instate[k][i] & (int) outstate[k][j]);
		// d = d || ((int)instate[k][i] && (int)outstate[k][j]);
		dep[i][j] = d;
	    }
	sparmatfill(depend, dep);
	for (i = 0; i < mm; i++) {
	    pr[i] = i;
	    if (i == 0)
		a[i] = rate0();
	    else if (i == 1)
		a[i] = rate1();
	    else
		a[i] = rate2();
	    asum += a[i];
	}
    }

    private double rate0() {
	return k0 * s[0] * s[1];
    }

    private double rate1() {
	return k1 * s[1] * s[2];
    }

    private double rate2() {
	return k2 * s[2];
    }

    public void describereactions() {
	k0 = 0.01;
	k1 = .1;
	k2 = 1.;
	double indat[] = { 1., 0., 0., 1., 1., 0., 0., 1., 1., 0., 0., 0. };
	instate = buildMatrix(nn, mm, indat);
	double outdat[] = { -1., 0., 0., 1., -1., 0., 0., 1., -1., 0., 0., 1. };
	outstate = buildMatrix(nn, mm, outdat);
    }

    // end user section

    public double step() {
	int i, n, m, k = 0;
	double tau, atarg, sum, anew;
	if (asum == 0.) {
	    t *= 2.;
	    return t;
	}
	tau = -log(ran.doub()) / asum;
	atarg = ran.doub() * asum;
	sum = a[pr[0]];
	while (sum < atarg)
	    sum += a[pr[++k]];
	m = pr[k];
	if (k > 0) {
	    swap(pr, k, k - 1);
	}
	if (k == mm - 1)
	    asum = sum;
	n = outchg[m].nvals;
	for (i = 0; i < n; i++) {
	    k = outchg[m].row_ind[i];
	    s[k] += outchg[m].val[i];
	}
	n = depend[m].nvals;
	for (i = 0; i < n; i++) {
	    k = depend[m].row_ind[i];
	    if (k == 0)
		anew = rate0();
	    else if (k == 1)
		anew = rate1();
	    else
		anew = rate2();
	    asum += (anew - a[k]);
	    a[k] = anew;
	}
	if (t * asum < 0.1)
	    for (asum = 0., i = 0; i < mm; i++)
		asum += a[i];
	return (t += tau);
    }

    public static void sparmatfill(final NRsparseCol[] sparmat,
	    final double[][] fullmat) {
	int n, m, nz, nn = fullmat.length, mm = fullmat[0].length;
	if (sparmat.length != mm)
	    throw new IllegalArgumentException("bad sizes");
	for (m = 0; m < mm; m++) {
	    for (nz = n = 0; n < nn; n++)
		if (fullmat[n][m] != 0)
		    nz++;
	    sparmat[m].resize(nn, nz);
	    for (nz = n = 0; n < nn; n++)
		if (fullmat[n][m] != 0) {
		    sparmat[m].row_ind[nz] = n;
		    sparmat[m].val[nz++] = fullmat[n][m];
		}
	}
    }
}
