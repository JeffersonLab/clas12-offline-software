package com.nr.sf;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;

public class Lognormaldist extends Erf {
    double mu, sig;

    public Lognormaldist() {
	this(0., 1.);
    }

    public Lognormaldist(final double mmu, final double ssig) {
	mu = mmu;
	sig = ssig;
	if (sig <= 0.)
	    throw new IllegalArgumentException("bad sig in Lognormaldist");
    }

    public double p(final double x) {
	if (x < 0.)
	    throw new IllegalArgumentException("bad x in Lognormaldist");
	if (x == 0.)
	    return 0.;
	return (0.398942280401432678 / (sig * x))
		* exp(-0.5 * SQR((log(x) - mu) / sig));
    }

    public double cdf(final double x) {
	if (x < 0.)
	    throw new IllegalArgumentException("bad x in Lognormaldist");
	if (x == 0.)
	    return 0.;
	return 0.5 * erfc(-0.707106781186547524 * (log(x) - mu) / sig);
    }

    public double invcdf(final double p) {
	if (p <= 0. || p >= 1.)
	    throw new IllegalArgumentException("bad p in Lognormaldist");
	return exp(-1.41421356237309505 * sig * inverfc(2. * p) + mu);
    }
}
