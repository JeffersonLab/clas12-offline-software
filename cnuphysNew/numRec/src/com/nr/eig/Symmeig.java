package com.nr.eig;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;

/**
 * Computes all eigenvalues and eigenvectors of a real symmetric matrix by
 * reduction to tridiagonal form followed by QL iteration.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class Symmeig {
    public int n;
    public double[][] z;
    public double[] d, e;
    public boolean yesvecs;

    public Symmeig(final double[][] a) {
	this(a, true);
    }

    /**
     * Computes all eigenvalues and eigenvectors of a real symmetric matrix
     * a[0..n-1][0..n-1] by reduction to tridiagonal form followed by QL
     * iteration. On output, d[0..n-1] contains the eigenvalues of a sorted into
     * descending order, while z[0..n-1][0..n-1] is a matrix whose columns
     * contain the corresponding normalized eigenvectors. If yesvecs is input as
     * true (the default), then the eigenvectors are computed. If yesvecs is
     * input as false, only the eigenvalues are computed.
     * 
     * @param a
     * @param yesvec
     */
    public Symmeig(final double[][] a, final boolean yesvec) {
	n = a.length;
	z = buildMatrix(a);
	d = new double[n];
	e = new double[n];
	yesvecs = yesvec;

	tred2();
	tqli();
	sort();
    }

    /**
     * Computes all eigenvalues and (optionally) eigenvectors of a real,
     * symmetric, tridiagonal matrix by QL iteration. On input, dd[0..n-1]
     * contains the diagonal elements of the tridi- agonal matrix. The vector
     * ee[0..n-1] inputs the subdiagonal elements of the tridiagonal matrix,
     * with ee[0] arbitrary. Output is the same as the constructor above.
     * 
     * @param dd
     * @param ee
     */
    public Symmeig(final double[] dd, final double[] ee) {
	this(dd, ee, true);
    }

    public Symmeig(final double[] dd, final double[] ee, final boolean yesvec) {
	n = dd.length;
	d = buildVector(dd);
	e = buildVector(ee);
	z = new double[n][n];
	yesvecs = yesvec;

	for (int i = 0; i < n; i++)
	    z[i][i] = 1.0;
	tqli();
	sort();
    }

    private void sort() {
	if (yesvecs)
	    Jacobi.eigsrt(d, z);
	else
	    Jacobi.eigsrt(d);
    }

    /**
     * Householder reduction of a real symmetric matrix z[0..n-1][0..n-1]. (The
     * input matrix A to Symmeig is stored in z.) On output, z is replaced by
     * the orthogonal matrix Q effecting the transformation. d[0..n-1] contains
     * the diagonal elements of the tridiagonal matrix and e[0..n-1] the
     * off-diagonal elements, with e[0]=0. If yesvecs is false, so that only
     * eigenvalues will subsequently be determined, several statements are
     * omitted, in which case z contains no useful information on output.
     */
    private void tred2() {
	int l, k, j, i;
	double scale, hh, h, g, f;
	for (i = n - 1; i > 0; i--) {
	    l = i - 1;
	    h = scale = 0.0;
	    if (l > 0) {
		for (k = 0; k < i; k++)
		    scale += abs(z[i][k]);
		if (scale == 0.0)
		    e[i] = z[i][l];
		else {
		    for (k = 0; k < i; k++) {
			z[i][k] /= scale;
			h += z[i][k] * z[i][k];
		    }
		    f = z[i][l];
		    g = (f >= 0.0 ? -sqrt(h) : sqrt(h));
		    e[i] = scale * g;
		    h -= f * g;
		    z[i][l] = f - g;
		    f = 0.0;
		    for (j = 0; j < i; j++) {
			if (yesvecs)
			    z[j][i] = z[i][j] / h;
			g = 0.0;
			for (k = 0; k < j + 1; k++)
			    g += z[j][k] * z[i][k];
			for (k = j + 1; k < i; k++)
			    g += z[k][j] * z[i][k];
			e[j] = g / h;
			f += e[j] * z[i][j];
		    }
		    hh = f / (h + h);
		    for (j = 0; j < i; j++) {
			f = z[i][j];
			e[j] = g = e[j] - hh * f;
			for (k = 0; k < j + 1; k++)
			    z[j][k] -= (f * e[k] + g * z[i][k]);
		    }
		}
	    } else
		e[i] = z[i][l];
	    d[i] = h;
	}
	if (yesvecs)
	    d[0] = 0.0;
	e[0] = 0.0;
	for (i = 0; i < n; i++) {
	    if (yesvecs) {
		if (d[i] != 0.0) {
		    for (j = 0; j < i; j++) {
			g = 0.0;
			for (k = 0; k < i; k++)
			    g += z[i][k] * z[k][j];
			for (k = 0; k < i; k++)
			    z[k][j] -= g * z[k][i];
		    }
		}
		d[i] = z[i][i];
		z[i][i] = 1.0;
		for (j = 0; j < i; j++)
		    z[j][i] = z[i][j] = 0.0;
	    } else {
		d[i] = z[i][i];
	    }
	}
    }

    /**
     * QL algorithm with implicit shifts to determine the eigenvalues and
     * (optionally) the eigenvectors of a real, symmetric, tridiagonal matrix,
     * or of a real symmetric matrix previously reduced by tred2. On input,
     * d[0..n-1] contains the diagonal elements of the tridiagonal matrix. On
     * output, it returns the eigenvalues. The vector e[0..n-1] inputs the
     * subdiagonal elements of the tridiagonal matrix, with e[0] arbitrary. On
     * output e is destroyed. If the eigenvectors of a tridiagonal matrix are
     * desired, the matrix z[0..n-1][0..n-1] is input as the identity matrix. If
     * the eigenvectors of a matrix that has been reduced by tred2 are required,
     * then z is input as the matrix output by tred2. In either case, column k
     * of z returns the normalized eigenvector corresponding to d[k]
     */
    private void tqli() {
	int m, l, iter, i, k;
	double s, r, p, g, f, dd, c, b;
	final double EPS = DBL_EPSILON;
	for (i = 1; i < n; i++)
	    e[i - 1] = e[i];
	e[n - 1] = 0.0;
	for (l = 0; l < n; l++) {
	    iter = 0;
	    do {
		for (m = l; m < n - 1; m++) {
		    dd = abs(d[m]) + abs(d[m + 1]);
		    if (abs(e[m]) <= EPS * dd)
			break;
		}
		if (m != l) {
		    if (iter++ == 30)
			throw new IllegalArgumentException(
				"Too many iterations in tqli");
		    g = (d[l + 1] - d[l]) / (2.0 * e[l]);
		    r = pythag(g, 1.0);
		    g = d[m] - d[l] + e[l] / (g + SIGN(r, g));
		    s = c = 1.0;
		    p = 0.0;
		    for (i = m - 1; i >= l; i--) {
			f = s * e[i];
			b = c * e[i];
			e[i + 1] = (r = pythag(f, g));
			if (r == 0.0) {
			    d[i + 1] -= p;
			    e[m] = 0.0;
			    break;
			}
			s = f / r;
			c = g / r;
			g = d[i + 1] - p;
			r = (d[i] - g) * s + 2.0 * c * b;
			d[i + 1] = g + (p = s * r);
			g = c * r - b;
			if (yesvecs) {
			    for (k = 0; k < n; k++) {
				f = z[k][i + 1];
				z[k][i + 1] = s * z[k][i] + c * f;
				z[k][i] = c * z[k][i] - s * f;
			    }
			}
		    }
		    if (r == 0.0 && i >= l)
			continue;
		    d[l] -= p;
		    e[l] = g;
		    e[m] = 0.0;
		}
	    } while (m != l);
	}
    }

    private double pythag(final double a, final double b) {
	double absa = abs(a), absb = abs(b);
	return (absa > absb ? absa * sqrt(1.0 + SQR(absb / absa))
		: (absb == 0.0 ? 0.0 : absb * sqrt(1.0 + SQR(absa / absb))));
    }
}
