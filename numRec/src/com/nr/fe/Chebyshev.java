package com.nr.fe;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;
import com.nr.UniVarRealValueFun;

/**
 * Chebyshev approximation
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Chebyshev {
    private int n, m;
    private double[] c;
    private double a, b;

    public Chebyshev(final double[] cc, final double aa, final double bb) {
	n = cc.length;
	m = n;
	c = buildVector(cc);
	a = aa;
	b = bb;
    }

    /**
     * Inverse of routine polycofs in Chebyshev: Given an array of polynomial
     * coefficients d[0..n-1], construct an equivalent Chebyshev object.
     * 
     * @param d
     */
    public Chebyshev(final double[] d) {
	n = d.length;
	m = n;
	c = new double[n];
	a = -1.;
	b = 1.;

	c[n - 1] = d[n - 1];
	c[n - 2] = 2.0 * d[n - 2];
	for (int j = n - 3; j >= 0; j--) {
	    c[j] = 2.0 * d[j] + c[j + 2];
	    for (int i = j + 1; i < n - 2; i++) {
		c[i] = (c[i] + c[i + 2]) / 2;
	    }
	    c[n - 2] /= 2;
	    c[n - 1] /= 2;
	}
    }

    public Chebyshev(final UniVarRealValueFun func, final double aa,
	    final double bb) {
	this(func, aa, bb, 50);
    }

    public Chebyshev(final UniVarRealValueFun func, final double aa,
	    final double bb, final int nn) {
	n = nn;
	m = nn;
	c = new double[n];
	a = aa;
	b = bb;

	int k, j;
	double fac, bpa, bma, y, sum;
	double[] f = new double[n];
	bma = 0.5 * (b - a);
	bpa = 0.5 * (b + a);
	for (k = 0; k < n; k++) {
	    y = cos(PI * (k + 0.5) / n);
	    f[k] = func.funk(y * bma + bpa);
	}
	fac = 2.0 / n;
	for (j = 0; j < n; j++) {
	    sum = 0.0;
	    for (k = 0; k < n; k++)
		sum += f[k] * cos(PI * j * (k + 0.5) / n);
	    c[j] = fac * sum;
	}
    }

    public double eval(final double x, final int m) {
	double d = 0.0, dd = 0.0, sv, y, y2;
	int j;
	if ((x - a) * (x - b) > 0.0)
	    throw new IllegalArgumentException("x not in range in eval");
	y2 = 2.0 * (y = (2.0 * x - a - b) / (b - a));
	for (j = m - 1; j > 0; j--) {
	    sv = d;
	    d = y2 * d - dd + c[j];
	    dd = sv;
	}
	return y * d - dd + 0.5 * c[0];
    }

    public double[] getc() {
	return c;
    }

    /**
     * Return a new Chebyshev object that approximates the derivative of the
     * existing function over the same range [a,b].
     * 
     * @return
     */
    public Chebyshev derivative() {
	int j;
	double con;
	double[] cder = new double[n];
	cder[n - 1] = 0.0;
	cder[n - 2] = 2 * (n - 1) * c[n - 1];
	for (j = n - 2; j > 0; j--)
	    cder[j - 1] = cder[j + 1] + 2 * j * c[j];
	con = 2.0 / (b - a);
	for (j = 0; j < n; j++)
	    cder[j] *= con;
	return new Chebyshev(cder, a, b);
    }

    /**
     * Return a new Chebyshev object that approximates the indefinite integral
     * of the existing function over the same range [a,b]. The constant of
     * integration is set so that the integral vanishes at a.
     * 
     * @return
     */
    public Chebyshev integral() {
	int j;
	double sum = 0.0, fac = 1.0, con;
	double[] cint = new double[n];
	con = 0.25 * (b - a);
	for (j = 1; j < n - 1; j++) {
	    cint[j] = con * (c[j - 1] - c[j + 1]) / j;
	    sum += fac * cint[j];
	    fac = -fac;
	}
	cint[n - 1] = con * c[n - 2] / (n - 1);
	sum += fac * cint[n - 1];
	cint[0] = 2.0 * sum;
	return new Chebyshev(cint, a, b);
    }

    /**
     * Polynomial coefficients from a Chebyshev fit. Given a coefficient array
     * c[0..n-1], this routine returns a coefficient array d[0..n-1] such that
     * sum(k=0,n-1)dky^k = sum(k=0,n-1)Tk(y)-c0/2. The method is Clenshaw's
     * recurrence (5.8.11), but now applied algebraically rather than
     * arithmetically.
     * 
     * 
     * @param m
     * @return
     */
    public double[] polycofs(final int m) {
	int k, j;
	double sv;
	double[] d = new double[m];
	double[] dd = new double[m];
	for (j = 0; j < m; j++)
	    d[j] = dd[j] = 0.0;
	d[0] = c[m - 1];
	for (j = m - 2; j > 0; j--) {
	    for (k = m - j; k > 0; k--) {
		sv = d[k];
		d[k] = 2.0 * d[k - 1] - dd[k];
		dd[k] = sv;
	    }
	    sv = d[0];
	    d[0] = -dd[0] + c[j];
	    dd[0] = sv;
	}
	for (j = m - 1; j > 0; j--)
	    d[j] = d[j - 1] - dd[j];
	d[0] = -dd[0] + 0.5 * c[0];
	return d;
    }

    public int setm(final double thresh) {
	while (m > 1 && abs(c[m - 1]) < thresh)
	    m--;
	return m;
    }

    // public double operator() (double x) {return eval(x,m);}

    public double get(final double x) {
	return eval(x, m);
    }

    public double[] polycofs() {
	return polycofs(m);
    }

    public static void pcshft(final double a, final double b, final double[] d) {
	int k, j, n = d.length;
	double cnst = 2.0 / (b - a), fac = cnst;
	for (j = 1; j < n; j++) {
	    d[j] *= fac;
	    fac *= cnst;
	}
	cnst = 0.5 * (a + b);
	for (j = 0; j <= n - 2; j++)
	    for (k = n - 2; k >= j; k--)
		d[k] -= cnst * d[k + 1];
    }

    public static void ipcshft(final double a, final double b, final double[] d) {
	pcshft(-(2. + b + a) / (b - a), (2. - b - a) / (b - a), d);
    }
}
