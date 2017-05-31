package com.nr.eig;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;

/**
 * Computes all eigenvalues and eigenvectors of a real symmetric matrix by
 * Jacobi's method.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class Jacobi {
    public final int n;
    public double[][] a, v;
    public double[] d;
    public int nrot;
    final double EPS;

    /**
     * Computes all eigenvalues and eigenvectors of a real symmetric matrix
     * a[0..n-1][0..n-1]. On output, d[0..n-1] contains the eigenvalues of a
     * sorted into descending order, while v[0..n-1][0..n-1] is a matrix whose
     * columns contain the corresponding normalized eigenvectors. nrot contains
     * the number of Jacobi rotations that were required. Only the upper
     * triangle of a is accessed.
     * 
     * @param aa
     */
    public Jacobi(final double[][] aa) {
	n = aa.length;
	a = buildMatrix(aa);
	v = new double[n][n];
	d = new double[n];
	nrot = 0;
	EPS = DBL_EPSILON;

	int i, j, ip, iq;
	double tresh, theta, tau, t, sm, s, h, g, c;
	double[] b = new double[n];
	double[] z = new double[n];

	for (ip = 0; ip < n; ip++) {
	    for (iq = 0; iq < n; iq++)
		v[ip][iq] = 0.0;
	    v[ip][ip] = 1.0;
	}
	for (ip = 0; ip < n; ip++) {
	    b[ip] = d[ip] = a[ip][ip];
	    z[ip] = 0.0;
	}
	for (i = 1; i <= 50; i++) {
	    sm = 0.0;
	    for (ip = 0; ip < n - 1; ip++) {
		for (iq = ip + 1; iq < n; iq++)
		    sm += abs(a[ip][iq]);
	    }
	    if (sm == 0.0) {
		eigsrt(d, v);
		return;
	    }
	    if (i < 4)
		tresh = 0.2 * sm / (n * n);
	    else
		tresh = 0.0;
	    for (ip = 0; ip < n - 1; ip++) {
		for (iq = ip + 1; iq < n; iq++) {
		    g = 100.0 * abs(a[ip][iq]);
		    if (i > 4 && g <= EPS * abs(d[ip]) && g <= EPS * abs(d[iq]))
			a[ip][iq] = 0.0;
		    else if (abs(a[ip][iq]) > tresh) {
			h = d[iq] - d[ip];
			if (g <= EPS * abs(h))
			    t = (a[ip][iq]) / h;
			else {
			    theta = 0.5 * h / (a[ip][iq]);
			    t = 1.0 / (abs(theta) + sqrt(1.0 + theta * theta));
			    if (theta < 0.0)
				t = -t;
			}
			c = 1.0 / sqrt(1 + t * t);
			s = t * c;
			tau = s / (1.0 + c);
			h = t * a[ip][iq];
			z[ip] -= h;
			z[iq] += h;
			d[ip] -= h;
			d[iq] += h;
			a[ip][iq] = 0.0;
			for (j = 0; j < ip; j++)
			    rot(a, s, tau, j, ip, j, iq);
			for (j = ip + 1; j < iq; j++)
			    rot(a, s, tau, ip, j, j, iq);
			for (j = iq + 1; j < n; j++)
			    rot(a, s, tau, ip, j, iq, j);
			for (j = 0; j < n; j++)
			    rot(v, s, tau, j, ip, j, iq);
			++nrot;
		    }
		}
	    }
	    for (ip = 0; ip < n; ip++) {
		b[ip] += z[ip];
		d[ip] = b[ip];
		z[ip] = 0.0;
	    }
	}
	throw new IllegalArgumentException(
		"Too many iterations in routine jacobi");
    }

    public static void rot(final double[][] a, final double s,
	    final double tau, final int i, final int j, final int k, final int l) {
	double g = a[i][j];
	double h = a[k][l];
	a[i][j] = g - s * (h + g * tau);
	a[k][l] = h + s * (g - h * tau);
    }

    /**
     * Given the eigenvalues d[0..n-1] and (optionally) the eigenvectors
     * v[0..n-1][0..n-1] as determined by Jacobi or tqli, this routine sorts the
     * eigenvalues into descending order and rearranges the columns of v
     * correspondingly. The method is straight insertion.
     * 
     * @param d
     * @param v
     */
    public static void eigsrt(final double[] d, final double[][] v) {
	int k;
	int n = d.length;
	for (int i = 0; i < n - 1; i++) {
	    double p = d[k = i];
	    for (int j = i; j < n; j++)
		if (d[j] >= p)
		    p = d[k = j];
	    if (k != i) {
		d[k] = d[i];
		d[i] = p;
		if (v != null)
		    for (int j = 0; j < n; j++) {
			p = v[j][i];
			v[j][i] = v[j][k];
			v[j][k] = p;
		    }
	    }
	}
    }

    public static void eigsrt(final double[] d) {
	eigsrt(d, null);
    }

}
