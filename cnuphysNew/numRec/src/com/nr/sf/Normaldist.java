package com.nr.sf;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;

public class Normaldist extends Erf {
    double mu, sig;

    public Normaldist() {
	this(0., 1.);
    }

    public Normaldist(final double mmu, final double ssig) {
	mu = mmu;
	sig = ssig;
	if (sig <= 0.)
	    throw new IllegalArgumentException("bad sig in Normaldist");
    }

    public double p(final double x) {
	return (0.398942280401432678 / sig) * exp(-0.5 * SQR((x - mu) / sig));
    }

    public double cdf(final double x) {
	return 0.5 * erfc(-0.707106781186547524 * (x - mu) / sig);
    }

    public double invcdf(final double p) {
	if (p <= 0. || p >= 1.)
	    throw new IllegalArgumentException("bad p in Normaldist");
	return -1.41421356237309505 * sig * inverfc(2. * p) + mu;
    }
}
