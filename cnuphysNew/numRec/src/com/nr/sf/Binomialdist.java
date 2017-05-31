package com.nr.sf;

import static java.lang.Math.*;
import static com.nr.sf.Gamma.*;

public class Binomialdist extends Beta {
    int n;
    double pe, fac;

    public Binomialdist(int nn, double ppe) {
	n = nn;
	pe = ppe;
	if (n <= 0 || pe <= 0. || pe >= 1.)
	    throw new IllegalArgumentException("bad args in Binomialdist");
	fac = gammln(n + 1.);
    }

    public double p(int k) {
	if (k < 0)
	    throw new IllegalArgumentException("bad k in Binomialdist");
	if (k > n)
	    return 0.;
	return exp(k * log(pe) + (n - k) * log(1. - pe) + fac - gammln(k + 1.)
		- gammln(n - k + 1.));
    }

    public double cdf(int k) {
	if (k < 0)
	    throw new IllegalArgumentException("bad k in Binomialdist");
	if (k == 0)
	    return 0.;
	if (k > n)
	    return 1.;
	return 1. - betai(k, n - k + 1., pe);
    }

    public int invcdf(double p) {
	int k, kl, ku, inc = 1;
	if (p <= 0. || p >= 1.)
	    throw new IllegalArgumentException("bad p in Binomialdist");
	k = max(0, min(n, (int) (n * pe)));
	if (p < cdf(k)) {
	    do {
		k = max(k - inc, 0);
		inc *= 2;
	    } while (p < cdf(k));
	    kl = k;
	    ku = k + inc / 2;
	} else {
	    do {
		k = min(k + inc, n + 1);
		inc *= 2;
	    } while (p > cdf(k));
	    ku = k;
	    kl = k - inc / 2;
	}
	while (ku - kl > 1) {
	    k = (kl + ku) / 2;
	    if (p < cdf(k))
		ku = k;
	    else
		kl = k;
	}
	return kl;
    }
}
