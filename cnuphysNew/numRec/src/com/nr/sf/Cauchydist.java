package com.nr.sf;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;

/**
 * Cauchy distribution.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Cauchydist {
    double mu, sig;

    public Cauchydist() {
	this(0., 1.);
    }

    public Cauchydist(final double mmu, final double ssig) {
	mu = mmu;
	sig = ssig;
	if (sig <= 0.)
	    throw new IllegalArgumentException("bad sig in Cauchydist");
    }

    public double p(final double x) {
	return 0.318309886183790671 / (sig * (1. + SQR((x - mu) / sig)));
    }

    public double cdf(final double x) {
	return 0.5 + 0.318309886183790671 * atan2(x - mu, sig);
    }

    public double invcdf(final double p) {
	if (p <= 0. || p >= 1.)
	    throw new IllegalArgumentException("bad p in Cauchydist");
	return mu + sig * tan(3.14159265358979324 * (p - 0.5));
    }
}
