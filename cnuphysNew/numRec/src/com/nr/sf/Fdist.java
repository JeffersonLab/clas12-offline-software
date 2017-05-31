package com.nr.sf;

import static java.lang.Math.*;
import static com.nr.sf.Gamma.*;

public class Fdist extends Beta {
    double nu1, nu2;
    double fac;

    public Fdist(double nnu1, double nnu2) {
	nu1 = nnu1;
	nu2 = nnu2;
	if (nu1 <= 0. || nu2 <= 0.)
	    throw new IllegalArgumentException("bad nu1,nu2 in Fdist");
	fac = 0.5 * (nu1 * log(nu1) + nu2 * log(nu2))
		+ gammln(0.5 * (nu1 + nu2)) - gammln(0.5 * nu1)
		- gammln(0.5 * nu2);
    }

    public double p(double f) {
	if (f <= 0.)
	    throw new IllegalArgumentException("bad f in Fdist");
	return exp((0.5 * nu1 - 1.) * log(f) - 0.5 * (nu1 + nu2)
		* log(nu2 + nu1 * f) + fac);
    }

    public double cdf(double f) {
	if (f < 0.)
	    throw new IllegalArgumentException("bad f in Fdist");
	return betai(0.5 * nu1, 0.5 * nu2, nu1 * f / (nu2 + nu1 * f));
    }

    public double invcdf(double p) {
	if (p <= 0. || p >= 1.)
	    throw new IllegalArgumentException("bad p in Fdist");
	double x = invbetai(p, 0.5 * nu1, 0.5 * nu2);
	return nu2 * x / (nu1 * (1. - x));
    }
}
