package com.nr.sf;

import static java.lang.Math.*;

public class Chisqdist extends Gamma {
    double nu, fac;

    public Chisqdist(double nnu) {
	nu = nnu;
	if (nu <= 0.)
	    throw new IllegalArgumentException("bad nu in Chisqdist");
	fac = 0.693147180559945309 * (0.5 * nu) + gammln(0.5 * nu);
    }

    public double p(double x2) {
	if (x2 <= 0.)
	    throw new IllegalArgumentException("bad x2 in Chisqdist");
	return exp(-0.5 * (x2 - (nu - 2.) * log(x2)) - fac);
    }

    public double cdf(double x2) {
	if (x2 < 0.)
	    throw new IllegalArgumentException("bad x2 in Chisqdist");
	return gammp(0.5 * nu, 0.5 * x2);
    }

    public double invcdf(double p) {
	if (p < 0. || p >= 1.)
	    throw new IllegalArgumentException("bad p in Chisqdist");
	return 2. * invgammp(p, 0.5 * nu);
    }
}
