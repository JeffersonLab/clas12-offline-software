package com.nr.fe;

import static com.nr.NRUtil.*;
import com.nr.Complex;

/***
 * operations on polynomials
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Poly {
    /**
     * polynomial c[0]+c[1]x+c[2]x^2+ ... + c[n-2]x^n-2 + c[n-1]x^n-1
     * 
     */
    double[] c;

    /**
     * Construct polynomial
     * 
     * @param cc
     */
    public Poly(final double[] cc) {
	if (cc.length < 2)
	    throw new IllegalArgumentException("Poly order must be at least 2.");
	c = cc;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder(32);
	int j = c.length - 1;
	sb.append(String.format("%fx^%d", c[j], j));
	j--;
	for (; j != 0; j--) {
	    sb.append(String.format("%+fx^%d", c[j], j));
	}
	sb.append(String.format("%+f ", c[0]));
	return sb.substring(0);
    }

    // double operator() (double x) {
    public double get(final double x) {
	int j = c.length - 1;
	double p = c[j];
	while (j > 0)
	    p = p * x + c[--j];
	return p;
    }

    /**
     * Build Polynomial from roots
     * 
     * @param z
     * @return
     */
    public static Poly buildFromRoots(final Complex[] z) {
	for (int i = 0; i < z.length; i++) {
	    boolean found = false;
	    for (int j = 0; j < z.length; j++) {
		if (z[i].re() == z[j].re() && z[i].im() == -z[j].im()) {
		    found = true;
		    break;
		}
	    }
	    if (!found)
		throw new IllegalArgumentException("Roots must be conjugate");
	}

	Complex[] c = new Complex[z.length + 1];
	c[0] = z[0].neg();
	c[1] = new Complex(1, 0);
	for (int i = 1; i < z.length; i++) {
	    Complex d = c[0];
	    c[0] = c[0].mul(z[i].neg());
	    for (int j = 1; j < i + 1; j++) {
		Complex dd = c[j];
		c[j] = d.sub(z[i].mul(c[j]));
		d = dd;
	    }
	    c[i + 1] = d;
	}
	double[] cc = new double[c.length];
	for (int i = 0; i < cc.length; i++)
	    cc[i] = c[i].re();
	return new Poly(cc);
    }

    /**
     * Build Polynomial from roots
     * 
     * @param z
     * @return
     */
    public static Poly buildFromRoots(final double[] z) {
	double[] c = new double[z.length + 1];
	c[0] = -z[0];
	c[1] = 1;
	for (int i = 1; i < z.length; i++) {
	    double d = c[0];
	    c[0] *= -z[i];
	    for (int j = 1; j < i + 1; j++) {
		double dd = c[j];
		c[j] = d - z[i] * c[j];
		d = dd;
	    }
	    c[i + 1] = d;
	}

	return new Poly(c);
    }

    /**
     * Given the coefficients of a polynomial of degree nc as an array c[0..nc]
     * of size nc+1 (with c[0] being the constant term), and given a value x,
     * this routine fills an output array pd of size nd+1 with the value of the
     * polynomial evaluated at x in pd[0], and the first nd derivatives at x in
     * pd[1..nd].
     * 
     * @param c
     * @param x
     * @param pd
     */
    public static void ddpoly(final double[] c, final double x,
	    final double[] pd) {
	int nnd, j, i, nc = c.length - 1, nd = pd.length - 1;
	double cnst = 1.0;
	pd[0] = c[nc];
	for (j = 1; j < nd + 1; j++)
	    pd[j] = 0.0;
	for (i = nc - 1; i >= 0; i--) {
	    nnd = (nd < (nc - i) ? nd : nc - i);
	    for (j = nnd; j > 0; j--)
		pd[j] = pd[j] * x + pd[j - 1];
	    pd[0] = pd[0] * x + c[i];
	}
	for (i = 2; i < nd + 1; i++) {
	    cnst *= i;
	    pd[i] *= cnst;
	}
    }

    /**
     * Given the coefficients of a polynomial of degree nc as an array c[0..nc]
     * of size nc+1 (with c[0] being the constant term), and given a value x,
     * this routine fills an output array pd of size nd+1 with the value of the
     * polynomial evaluated at x in pd[0], and the first nd derivatives at x in
     * pd[1..nd].
     * 
     * @param u
     * @param v
     * @param q
     * @param r
     */
    public static void poldiv(final double[] u, final double[] v,
	    final double[] q, final double[] r) {
	if (q.length < u.length)
	    throw new IllegalArgumentException("q.length < u.length.");
	if (r.length < u.length)
	    throw new IllegalArgumentException("r.length < u.length.");

	int k, j, n = u.length - 1, nv = v.length - 1;
	while (nv >= 0 && v[nv] == 0.)
	    nv--;
	if (nv < 0)
	    throw new IllegalArgumentException(
		    "poldiv divide by zero polynomial");
	copyAssign(r, u);
	// assign(q, u.length,0.);
	for (k = n - nv; k >= 0; k--) {
	    q[k] = r[nv + k] / v[nv];
	    for (j = nv + k - 1; j >= k; j--)
		r[j] -= q[k] * v[j - k];
	}
	for (j = nv; j <= n; j++)
	    r[j] = 0.0;
    }

    public static void main(final String[] args) {
	double[] c = { 6, -5, 1 };
	Poly poly = new Poly(c);
	System.out.println(poly);

	poly = buildFromRoots(new double[] { 2, 3, 1 });
	System.out.println(poly);
	Complex p0 = new Complex(-0.00196389, 0.00196448);
	Complex p1 = new Complex(-0.00196389, -0.00196448);
	Complex p2 = new Complex(-3.1175, 3.90912);
	Complex p3 = new Complex(-3.1175, -3.90912);
	poly = buildFromRoots(new Complex[] { p0, p1, p2, p3 });
	System.out.println(poly);
    }
}
