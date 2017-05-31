package com.nr.min;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;
import com.nr.RealValueFunWithDiff;

public class Frprmn extends Dlinemethod { // orig is Linemethod XXX may be
					  // Dlinemethod is better
    public int iter;
    public double fret;
    /*
     * using Linemethod<T>::func; using Linemethod<T>::linmin; using
     * Linemethod<T>::p; using Linemethod<T>::xi;
     */
    final double ftol;

    public Frprmn(final RealValueFunWithDiff funcd) {
	this(funcd, 3.0e-8);
    }

    public Frprmn(final RealValueFunWithDiff funcd, final double ftoll) {
	super(funcd);
	ftol = ftoll;
    }

    public double[] minimize(final double[] pp) {
	final int ITMAX = 200;
	final double EPS = 1.0e-18;
	final double GTOL = 1.0e-8;
	double gg, dgg;
	int n = pp.length;
	p = buildVector(pp);
	double[] g = new double[n], h = new double[n];
	xi = new double[n];
	double fp = func.funk(p);
	func.df(p, xi);
	for (int j = 0; j < n; j++) {
	    g[j] = -xi[j];
	    xi[j] = h[j] = g[j];
	}
	for (int its = 0; its < ITMAX; its++) {
	    iter = its;
	    fret = linmin();
	    if (2.0 * abs(fret - fp) <= ftol * (abs(fret) + abs(fp) + EPS))
		return p;
	    fp = fret;
	    func.df(p, xi);
	    double test = 0.0;
	    double den = max(abs(fp), 1.0);
	    for (int j = 0; j < n; j++) {
		double temp = abs(xi[j]) * max(abs(p[j]), 1.0) / den;
		if (temp > test)
		    test = temp;
	    }
	    if (test < GTOL)
		return p;
	    dgg = gg = 0.0;
	    for (int j = 0; j < n; j++) {
		gg += g[j] * g[j];
		// dgg += xi[j]*xi[j];
		dgg += (xi[j] + g[j]) * xi[j];
	    }
	    if (gg == 0.0)
		return p;
	    double gam = dgg / gg;
	    for (int j = 0; j < n; j++) {
		g[j] = -xi[j];
		xi[j] = h[j] = g[j] + gam * h[j];
	    }
	}
	throw new IllegalArgumentException("Too many iterations in frprmn");
    }
}
