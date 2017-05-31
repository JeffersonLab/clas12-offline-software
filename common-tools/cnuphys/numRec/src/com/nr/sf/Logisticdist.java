package com.nr.sf;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;

public class Logisticdist {
    double mu, sig;

    public Logisticdist() {
	this(0., 1.);
    }

    public Logisticdist(final double mmu, final double ssig) {
	mu = mmu;
	sig = ssig;
	if (sig <= 0.)
	    throw new IllegalArgumentException("bad sig in Logisticdist");
    }

    public double p(final double x) {
	double e = exp(-abs(1.81379936423421785 * (x - mu) / sig));
	return 1.81379936423421785 * e / (sig * SQR(1. + e));
    }

    public double cdf(final double x) {
	double e = exp(-abs(1.81379936423421785 * (x - mu) / sig));
	if (x >= mu)
	    return 1. / (1. + e);
	else
	    return e / (1. + e);
    }

    public double invcdf(final double p) {
	if (p <= 0. || p >= 1.)
	    throw new IllegalArgumentException("bad p in Logisticdist");
	return mu + 0.551328895421792049 * sig * log(p / (1. - p));
    }
}
