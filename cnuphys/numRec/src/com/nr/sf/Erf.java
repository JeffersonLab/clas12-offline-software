package com.nr.sf;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;

/**
 * Error function and related distributions Copyright (C) Numerical Recipes
 * Software 1986-2007 Java translation Copyright (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Erf {
    private static final int ncof = 28;
    private static final double[] cof = { -1.3026537197817094,
	    6.4196979235649026e-1, 1.9476473204185836e-2,
	    -9.561514786808631e-3, -9.46595344482036e-4, 3.66839497852761e-4,
	    4.2523324806907e-5, -2.0278578112534e-5, -1.624290004647e-6,
	    1.303655835580e-6, 1.5626441722e-8, -8.5238095915e-8,
	    6.529054439e-9, 5.059343495e-9, -9.91364156e-10, -2.27365122e-10,
	    9.6467911e-11, 2.394038e-12, -6.886027e-12, 8.94487e-13,
	    3.13092e-13, -1.12708e-13, 3.81e-16, 7.106e-15, -1.523e-15,
	    -9.4e-17, 1.21e-16, -2.8e-17 };

    public Erf() {
    }

    public double erf(final double x) {
	if (x >= 0.)
	    return 1.0 - erfccheb(x);
	else
	    return erfccheb(-x) - 1.0;
    }

    public double erfc(final double x) {
	if (x >= 0.)
	    return erfccheb(x);
	else
	    return 2.0 - erfccheb(-x);
    }

    public double erfccheb(final double z) {
	int j;
	double t, ty, tmp, d = 0., dd = 0.;
	if (z < 0.)
	    throw new IllegalArgumentException(
		    "erfccheb requires nonnegative argument");
	t = 2. / (2. + z);
	ty = 4. * t - 2.;
	for (j = ncof - 1; j > 0; j--) {
	    tmp = d;
	    d = ty * d - dd + cof[j];
	    dd = tmp;
	}
	return t * exp(-z * z + 0.5 * (cof[0] + ty * d) - dd);
    }

    public double inverfc(final double p) {
	double x, err, t, pp;
	if (p >= 2.0)
	    return -100.;
	if (p <= 0.0)
	    return 100.;
	pp = (p < 1.0) ? p : 2. - p;
	t = sqrt(-2. * log(pp / 2.));
	x = -0.70711
		* ((2.30753 + t * 0.27061) / (1. + t * (0.99229 + t * 0.04481)) - t);
	for (int j = 0; j < 2; j++) {
	    err = erfc(x) - pp;
	    x += err / (1.12837916709551257 * exp(-SQR(x)) - x * err);
	}
	return (p < 1.0 ? x : -x);
    }

    public double inverf(final double p) {
	return inverfc(1. - p);
    }

    public static double erfcc(final double x) {
	double t, z = abs(x), ans;
	t = 2. / (2. + z);
	ans = t
		* exp(-z
			* z
			- 1.26551223
			+ t
			* (1.00002368 + t
				* (0.37409196 + t
					* (0.09678418 + t
						* (-0.18628806 + t
							* (0.27886807 + t
								* (-1.13520398 + t
									* (1.48851587 + t
										* (-0.82215223 + t * 0.17087277)))))))));
	return (x >= 0.0 ? ans : 2.0 - ans);
    }

}
