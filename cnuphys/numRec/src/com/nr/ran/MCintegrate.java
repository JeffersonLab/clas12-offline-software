package com.nr.ran;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;

/**
 * 
 * Monte Carlo integration
 * 
 * Object for Monte Carlo integration of one or more functions in an
 * ndim-dimensional region
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public abstract class MCintegrate {
    private int ndim, nfun, n;
    public double[] ff, fferr;
    private double[] xlo, xhi, x, xx, fn, sf, sferr;
    private double vol;
    private Ran ran;

    boolean xmappValid = false;

    public abstract double[] funcs(final double[] x);

    public abstract boolean inregion(final double[] x);

    public abstract double[] xmap(final double[] x);

    public MCintegrate(final double[] xlow, final double[] xhigh,
	    final int ranseed, boolean xmappValid) {
	this.xmappValid = xmappValid;
	ndim = xlow.length;
	n = 0;
	xlo = buildVector(xlow);
	xhi = buildVector(xhigh);
	x = new double[ndim];
	xx = new double[ndim];
	vol = 1.;
	ran = new Ran(ranseed);
	if (xmappValid)
	    nfun = funcs(xmap(xlo)).length;
	else
	    nfun = funcs(xlo).length;
	ff = new double[nfun];
	fferr = new double[nfun];
	fn = new double[nfun];
	sf = new double[nfun];
	sferr = new double[nfun];
	for (int j = 0; j < ndim; j++)
	    vol *= abs(xhi[j] - xlo[j]);
    }

    public void step(final int nstep) {
	int i, j;
	for (i = 0; i < nstep; i++) {
	    for (j = 0; j < ndim; j++)
		x[j] = xlo[j] + (xhi[j] - xlo[j]) * ran.doub();
	    if (xmappValid)
		xx = xmap(x);
	    else
		xx = x;
	    if (inregion(xx)) {
		fn = funcs(xx);
		for (j = 0; j < nfun; j++) {
		    sf[j] += fn[j];
		    sferr[j] += SQR(fn[j]);
		}
	    }
	}
	n += nstep;
    }

    public void calcanswers() {
	for (int j = 0; j < nfun; j++) {
	    ff[j] = vol * sf[j] / n;
	    fferr[j] = vol * sqrt((sferr[j] / n - SQR(sf[j] / n)) / n);
	}
    }

    public double[] torusfuncs(final double[] x) {
	double den = 1.;
	double[] f = new double[4];
	f[0] = den;
	for (int i = 1; i < 4; i++)
	    f[i] = x[i - 1] * den;
	return f;
    }

    public boolean torusregion(final double[] x) {
	return SQR(x[2]) + SQR(sqrt(SQR(x[0]) + SQR(x[1])) - 3.) <= 1.;
    }

    public double[] torusmap(final double[] s) {
	double[] xx = buildVector(s);
	xx[2] = 0.2 * log(5. * s[2]);
	return xx;
    }
}
