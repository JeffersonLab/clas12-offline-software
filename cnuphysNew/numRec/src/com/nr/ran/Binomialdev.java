package com.nr.ran;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;
import static com.nr.sf.Gamma.*;

/**
 * Binomial Deviates
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Binomialdev extends Ran {
    double pp, p, pb, expnp, np, glnp, plog, pclog, sq;
    int n, swch;
    long uz, uo, unfin, diff, rltp;
    int[] pbits = new int[5];
    double[] cdf = new double[64];
    double[] logfact = new double[1024];

    public Binomialdev(final int nn, final double ppp, final long i) {
	super(i);
	pp = ppp;
	n = nn;

	int j;
	pb = p = (pp <= 0.5 ? pp : 1.0 - pp);
	if (n <= 64) {
	    uz = 0;
	    uo = 0xffffffffffffffffL;
	    rltp = 0;
	    for (j = 0; j < 5; j++)
		pbits[j] = 1 & ((int) (pb *= 2.));
	    pb -= floor(pb);
	    swch = 0;
	} else if (n * p < 30.) {
	    cdf[0] = exp(n * log(1 - p));
	    for (j = 1; j < 64; j++)
		cdf[j] = cdf[j - 1]
			+ exp(gammln(n + 1.) - gammln(j + 1.)
				- gammln(n - j + 1.) + j * log(p) + (n - j)
				* log(1. - p));
	    swch = 1;
	} else {
	    np = n * p;
	    glnp = gammln(n + 1.);
	    plog = log(p);
	    pclog = log(1. - p);
	    sq = sqrt(np * (1. - p));
	    if (n < 1024)
		for (j = 0; j <= n; j++)
		    logfact[j] = gammln(j + 1.);
	    swch = 2;
	}
    }

    public int dev() {
	int j, k, kl, km;
	double y, u, v, u2, v2, b;
	if (swch == 0) {
	    unfin = uo;
	    for (j = 0; j < 5; j++) {
		diff = unfin & (int64() ^ (pbits[j] != 0 ? uo : uz));
		if (pbits[j] != 0)
		    rltp |= diff;
		else
		    rltp = rltp & ~diff;
		unfin = unfin & ~diff;
	    }
	    k = 0;
	    for (j = 0; j < n; j++) {
		if ((unfin & 1) != 0) {
		    if (doub() < pb)
			++k;
		} else {
		    if ((rltp & 1) != 0)
			++k;
		}
		unfin >>>= 1;
		rltp >>>= 1;
	    }
	} else if (swch == 1) {
	    y = doub();
	    kl = -1;
	    k = 64;
	    while (k - kl > 1) {
		km = (kl + k) / 2;
		if (y < cdf[km])
		    k = km;
		else
		    kl = km;
	    }
	} else {
	    for (;;) {
		u = 0.645 * doub();
		v = -0.63 + 1.25 * doub();
		v2 = SQR(v);
		if (v >= 0.) {
		    if (v2 > 6.5 * u * (0.645 - u) * (u + 0.2))
			continue;
		} else {
		    if (v2 > 8.4 * u * (0.645 - u) * (u + 0.1))
			continue;
		}
		k = (int) (floor(sq * (v / u) + np + 0.5));
		if (k < 0 || k > n)
		    continue;
		u2 = SQR(u);
		if (v >= 0.) {
		    if (v2 < 12.25 * u2 * (0.615 - u) * (0.92 - u))
			break;
		} else {
		    if (v2 < 7.84 * u2 * (0.615 - u) * (1.2 - u))
			break;
		}
		b = sq
			* exp(glnp
				+ k
				* plog
				+ (n - k)
				* pclog
				- (n < 1024 ? logfact[k] + logfact[n - k]
					: gammln(k + 1.) + gammln(n - k + 1.)));
		if (u2 < b)
		    break;
	    }
	}
	if (p != pp)
	    k = n - k;
	return k;
    }
}
