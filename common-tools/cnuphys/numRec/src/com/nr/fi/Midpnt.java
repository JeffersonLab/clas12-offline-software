package com.nr.fi;

import static java.lang.Math.*;
import com.nr.UniVarRealValueFun;
import com.nr.interp.Poly_interp;

/**
 * Routine implementing the extended midpoint rule.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Midpnt extends Quadrature {
    protected double a, b, s;
    protected UniVarRealValueFun funk;

    /**
     * The constructor takes as inputs func, the function or functor to be
     * integrated between limits a and b, also input.
     * 
     * @param funcc
     * @param aa
     * @param bb
     */
    public Midpnt(final UniVarRealValueFun funcc, final double aa,
	    final double bb) {
	funk = funcc;
	a = aa;
	b = bb;
	n = 0;
    }

    /**
     * Returns the nth stage of refinement of the extended midpoint rule. On the
     * first call (n=1), the routine returns the crudest estimate of
     * S(a,b)f(x)dx. Subsequent calls set n=2,3,... and improve the accuracy by
     * adding (2/3)x3^(n-1) additional interior points.
     */
    @Override
    public double next() {
	int it, j;
	double x, tnm, sum, del, ddel;
	n++;
	if (n == 1) {
	    return (s = (b - a) * func(0.5 * (a + b)));
	} else {
	    for (it = 1, j = 1; j < n - 1; j++)
		it *= 3;
	    tnm = it;
	    del = (b - a) / (3.0 * tnm);
	    ddel = del + del;
	    x = a + 0.5 * del;
	    sum = 0.0;
	    for (j = 0; j < it; j++) {
		sum += func(x);
		x += ddel;
		sum += func(x);
		x += del;
	    }
	    s = (s + (b - a) * sum / tnm) / 3.0;
	    return s;
	}
    }

    public double func(final double x) {
	return funk.funk(x);
    }

    public static double qromo(final Midpnt q) {
	return qromo(q, 3.0e-9);
    }

    /**
     * Romberg integration on an open interval. Returns the integral of a
     * function using any specified elementary quadrature algorithm q and
     * Romberg's method. Normally q will be an open formula, not evaluating the
     * function at the endpoints. It is assumed that q triples the number of
     * steps on each call, and that its error series contains only even powers
     * of the number of steps. The routines midpnt, midinf, midsql, midsqu,
     * midexp are possible choices for q. The constants below have the same
     * meanings as in qromb.
     * 
     * @param q
     * @param eps
     * @return
     */
    public static double qromo(final Midpnt q, final double eps) {
	final int JMAX = 14, JMAXP = JMAX + 1, K = 5;
	double[] h = new double[JMAXP], s = new double[JMAX];
	Poly_interp polint = new Poly_interp(h, s, K);
	h[0] = 1.0;
	for (int j = 1; j <= JMAX; j++) {
	    s[j - 1] = q.next();
	    if (j >= K) {
		double ss = polint.rawinterp(j - K, 0.0);
		if (abs(polint.dy) <= eps * abs(ss))
		    return ss;
	    }
	    h[j] = h[j - 1] / 9.0;
	}
	throw new IllegalArgumentException("Too many steps in routine qromo");
    }
}
