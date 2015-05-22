package com.nr.sf;

import static java.lang.Math.*;

public class Poissondist extends Gamma {
    double lam;

    public Poissondist(double llam) {
	lam = llam;
	if (lam <= 0.)
	    throw new IllegalArgumentException("bad lam in Poissondist");
    }

    public double p(int n) {
	if (n < 0)
	    throw new IllegalArgumentException("bad n in Poissondist");
	return exp(-lam + n * log(lam) - gammln(n + 1.));
    }

    public double cdf(int n) {
	if (n < 0)
	    throw new IllegalArgumentException("bad n in Poissondist");
	if (n == 0)
	    return 0.;
	return gammq(n, lam);
    }

    public int invcdf(double p) {
	int n, nl, nu, inc = 1;
	if (p <= 0. || p >= 1.)
	    throw new IllegalArgumentException("bad p in Poissondist");
	if (p < exp(-lam))
	    return 0;
	n = (int) max(sqrt(lam), 5.);
	if (p < cdf(n)) {
	    do {
		n = max(n - inc, 0);
		inc *= 2;
	    } while (p < cdf(n));
	    nl = n;
	    nu = n + inc / 2;
	} else {
	    do {
		n += inc;
		inc *= 2;
	    } while (p > cdf(n));
	    nu = n;
	    nl = n - inc / 2;
	}
	while (nu - nl > 1) {
	    n = (nl + nu) / 2;
	    if (p < cdf(n))
		nu = n;
	    else
		nl = n;
	}
	return nl;
    }
}
