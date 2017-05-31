package com.nr.ran;

import static java.lang.Math.*;

public class Normaldev_BM extends Ran {
    double mu, sig;
    double storedval;

    public Normaldev_BM(final double mmu, final double ssig, final long i) {
	super(i);
	mu = mmu;
	sig = ssig;
	storedval = 0.;
    }

    public double dev() {
	double v1, v2, rsq, fac;
	if (storedval == 0.) {
	    do {
		v1 = 2.0 * doub() - 1.0;
		v2 = 2.0 * doub() - 1.0;
		rsq = v1 * v1 + v2 * v2;
	    } while (rsq >= 1.0 || rsq == 0.0);
	    fac = sqrt(-2.0 * log(rsq) / rsq);
	    storedval = v1 * fac;
	    return mu + sig * v2 * fac;
	} else {
	    fac = storedval;
	    storedval = 0.;
	    return mu + sig * fac;
	}
    }
}
