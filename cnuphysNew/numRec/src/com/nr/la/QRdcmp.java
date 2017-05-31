package com.nr.la;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;

/**
 * QR Decomposition - PTC
 * 
 * There is another matrix factorization that is sometimes very useful, the
 * so-called QR decomposition, A=Q*R Here R is upper triangular, while Q is
 * orthogonal, that is, QT*Q = 1
 * 
 * QR decomposition can be used to solve systems of linear equations. To solve
 * 
 * A*x=b
 * 
 * first form QT*b and then solve R*x=QT*b by backsubstitution.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class QRdcmp {
    public int n;

    /**
     * Stored QT and R
     */
    public double[][] qt, r;

    /**
     * Indicates whether A is singular
     */
    public boolean sing;

    /**
     * Construct the QR decomposition of a[0..n-1][0..n-1]. The upper triangular
     * matrix R and the transpose of the orthogonal matrix Q are stored. sing is
     * set to true if a singularity is encountered during the decomposition, but
     * the decomposition is still completed in this case; otherwise it is set to
     * false.
     * 
     * @param a
     */
    public QRdcmp(final double[][] a) {
	n = a.length;
	qt = new double[n][n];
	r = buildMatrix(a);
	sing = false;

	int i, j, k;
	double[] c = new double[n];
	double[] d = new double[n];

	double scale, sigma, sum, tau;
	for (k = 0; k < n - 1; k++) {
	    scale = 0.0;
	    for (i = k; i < n; i++)
		scale = max(scale, abs(r[i][k]));
	    if (scale == 0.0) {
		sing = true;
		c[k] = d[k] = 0.0;
	    } else {
		for (i = k; i < n; i++)
		    r[i][k] /= scale;
		for (sum = 0.0, i = k; i < n; i++)
		    sum += SQR(r[i][k]);
		sigma = SIGN(sqrt(sum), r[k][k]);
		r[k][k] += sigma;
		c[k] = sigma * r[k][k];
		d[k] = -scale * sigma;
		for (j = k + 1; j < n; j++) {
		    for (sum = 0.0, i = k; i < n; i++)
			sum += r[i][k] * r[i][j];
		    tau = sum / c[k];
		    for (i = k; i < n; i++)
			r[i][j] -= tau * r[i][k];
		}
	    }
	}
	d[n - 1] = r[n - 1][n - 1];
	if (d[n - 1] == 0.0)
	    sing = true;
	for (i = 0; i < n; i++) {
	    for (j = 0; j < n; j++)
		qt[i][j] = 0.0;
	    qt[i][i] = 1.0;
	}
	for (k = 0; k < n - 1; k++) {
	    if (c[k] != 0.0) {
		for (j = 0; j < n; j++) {
		    sum = 0.0;
		    for (i = k; i < n; i++)
			sum += r[i][k] * qt[i][j];
		    sum /= c[k];
		    for (i = k; i < n; i++)
			qt[i][j] -= sum * r[i][k];
		}
	    }
	}
	for (i = 0; i < n; i++) {
	    r[i][i] = d[i];
	    for (j = 0; j < i; j++)
		r[i][j] = 0.0;
	}
    }

    /**
     * Solve the set of n linear equations A*x = b. b[0..n-1] is input as the
     * right-hand side vector, and x[0..n-1] is returned as the solution vector
     * 
     * @param b
     * @param x
     */
    public void solve(final double[] b, final double[] x) {
	qtmult(b, x);
	rsolve(x, x);
    }

    /**
     * Multiply QT*b and put the result in x. Since Q is orthogonal, this is
     * equivalent to solving Q*x = b for x.
     * 
     * @param b
     * @param x
     */
    public void qtmult(final double[] b, final double[] x) {
	int i, j;
	double sum;
	for (i = 0; i < n; i++) {
	    sum = 0.;
	    for (j = 0; j < n; j++)
		sum += qt[i][j] * b[j];
	    x[i] = sum;
	}
    }

    /**
     * Solve the triangular set of n linear equations R*x = b. b[0..n-1] is
     * input as the right-hand side vector, and x[0..n-1] is returned as the
     * solution vector
     * 
     * @param b
     * @param x
     */
    public void rsolve(final double[] b, final double[] x) {
	int i, j;
	double sum;
	if (sing)
	    throw new IllegalArgumentException(
		    "attempting solve in a singular QR");
	for (i = n - 1; i >= 0; i--) {
	    sum = b[i];
	    for (j = i + 1; j < n; j++)
		sum -= r[i][j] * x[j];
	    x[i] = sum / r[i][i];
	}
    }

    /**
     * Starting from the stored QR decomposition A = Q*R, update it to be the QR
     * decomposition of the matrix Q*(R + u(+)v). Input quantities are
     * u[0..n-1], and v[0..n-1].
     * 
     * @param u
     * @param v
     */
    public void update(final double[] u, final double[] v) {
	int i, k;
	double[] w = buildVector(u);
	for (k = n - 1; k >= 0; k--)
	    if (w[k] != 0.0)
		break;
	if (k < 0)
	    k = 0;
	for (i = k - 1; i >= 0; i--) {
	    rotate(i, w[i], -w[i + 1]);
	    if (w[i] == 0.0)
		w[i] = abs(w[i + 1]);
	    else if (abs(w[i]) > abs(w[i + 1]))
		w[i] = abs(w[i]) * sqrt(1.0 + SQR(w[i + 1] / w[i]));
	    else
		w[i] = abs(w[i + 1]) * sqrt(1.0 + SQR(w[i] / w[i + 1]));
	}
	for (i = 0; i < n; i++)
	    r[0][i] += w[0] * v[i];
	for (i = 0; i < k; i++)
	    rotate(i, r[i][i], -r[i + 1][i]);
	for (i = 0; i < n; i++)
	    if (r[i][i] == 0.0)
		sing = true;
    }

    /**
     * Utility used by update. Given matrices r[0..n-1][0..n-1] and
     * qt[0..n-1][0..n-1], carry out a Jacobi rotation on rows i and i+1 of each
     * matrix. a and b are the parameters of the rotation:
     * cos(Â)=a/sqrt(a*a+b*b), sin(Â)=b/sqrt(a*a+b*b).
     * 
     * @param i
     * @param a
     * @param b
     */
    public void rotate(final int i, final double a, final double b) {
	int j;
	double c, fact, s, w, y;
	if (a == 0.0) {
	    c = 0.0;
	    s = (b >= 0.0 ? 1.0 : -1.0);
	} else if (abs(a) > abs(b)) {
	    fact = b / a;
	    c = SIGN(1.0 / sqrt(1.0 + (fact * fact)), a);
	    s = fact * c;
	} else {
	    fact = a / b;
	    s = SIGN(1.0 / sqrt(1.0 + (fact * fact)), b);
	    c = fact * s;
	}
	for (j = i; j < n; j++) {
	    y = r[i][j];
	    w = r[i + 1][j];
	    r[i][j] = c * y - s * w;
	    r[i + 1][j] = s * y + c * w;
	}
	for (j = 0; j < n; j++) {
	    y = qt[i][j];
	    w = qt[i + 1][j];
	    qt[i][j] = c * y - s * w;
	    qt[i + 1][j] = s * y + c * w;
	}
    }

}
