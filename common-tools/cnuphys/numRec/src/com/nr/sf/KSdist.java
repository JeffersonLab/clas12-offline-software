package com.nr.sf;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;

/**
 * Kolmogorov-Smirnov cumulative distribution functions and their inverses.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class KSdist {

    public double pks(final double z) {
	if (z < 0.)
	    throw new IllegalArgumentException("bad z in KSdist");
	if (z < 0.042)
	    return 0.;
	if (z < 1.18) {
	    double y = exp(-1.23370055013616983 / SQR(z));
	    return 2.25675833419102515 * sqrt(-log(y))
		    * (y + pow(y, 9) + pow(y, 25) + pow(y, 49));
	} else {
	    double x = exp(-2. * SQR(z));
	    return 1. - 2. * (x - pow(x, 4) + pow(x, 9));
	}
    }

    public double qks(final double z) {
	if (z < 0.)
	    throw new IllegalArgumentException("bad z in KSdist");
	if (z == 0.)
	    return 1.;
	if (z < 1.18)
	    return 1. - pks(z);
	double x = exp(-2. * SQR(z));
	return 2. * (x - pow(x, 4) + pow(x, 9));
    }

    public double invqks(final double q) {
	double y, logy, x, xp, f, ff, u, t; // yp
	if (q <= 0. || q > 1.)
	    throw new IllegalArgumentException("bad q in KSdist");
	if (q == 1.)
	    return 0.;
	if (q > 0.3) {
	    f = -0.392699081698724155 * SQR(1. - q);
	    y = Integrals.invxlogx(f);
	    do {
		// yp = y; // yp not used
		logy = log(y);
		ff = f / SQR(1. + pow(y, 4) + pow(y, 12));
		u = (y * logy - ff) / (1. + logy);
		y = y - (t = u / max(0.5, 1. - 0.5 * u / (y * (1. + logy))));
	    } while (abs(t / y) > 1.e-15);
	    return 1.57079632679489662 / sqrt(-log(y));
	} else {
	    x = 0.03;
	    do {
		xp = x;
		x = 0.5 * q + pow(x, 4) - pow(x, 9);
		if (x > 0.06)
		    x += pow(x, 16) - pow(x, 25);
	    } while (abs((xp - x) / x) > 1.e-15);
	    return sqrt(-0.5 * log(x));
	}
    }

    public double invpks(final double p) {
	return invqks(1. - p);
    }
}
