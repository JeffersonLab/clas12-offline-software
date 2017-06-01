package com.nr.la;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;

/**
 * Cholesky decomposition of a matrix A. - PTC
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Cholesky {
    int n;

    /**
     * Stores the decomposition
     * 
     */
    double[][] el;

    /**
     * Given a positive-definite symmetric matrix a[0..n-1][0..n-1], construct
     * and store its Cholesky decomposition, A = L* Lt .
     * 
     * @param a
     */
    public Cholesky(final double[][] a) {
	n = a.length;
	el = buildMatrix(a);

	int i, j, k;
	double sum;
	if (el[0].length != n)
	    throw new IllegalArgumentException("need square matrix");
	for (i = 0; i < n; i++) {
	    for (j = i; j < n; j++) {
		for (sum = el[i][j], k = i - 1; k >= 0; k--)
		    sum -= el[i][k] * el[j][k];
		if (i == j) {
		    if (sum <= 0.0)
			throw new IllegalArgumentException("Cholesky failed");
		    el[i][i] = sqrt(sum);
		} else
		    el[j][i] = sum / el[i][i];
	    }
	}
	for (i = 0; i < n; i++)
	    for (j = 0; j < i; j++)
		el[j][i] = 0.;
    }

    /**
     * Solve the set of n linear equations A*x = b, where a is a
     * positive-definite symmetric matrix whose Cholesky decomposition has been
     * stored. b[0..n-1] is input as the right-hand side vector. The solution
     * vector is returned in x[0..n-1].
     * 
     * @param b
     * @param x
     */
    public void solve(final double[] b, final double[] x) {
	int i, k;
	double sum;
	if (b.length != n || x.length != n)
	    throw new IllegalArgumentException("bad lengths in Cholesky");
	for (i = 0; i < n; i++) {
	    for (sum = b[i], k = i - 1; k >= 0; k--)
		sum -= el[i][k] * x[k];
	    x[i] = sum / el[i][i];
	}
	for (i = n - 1; i >= 0; i--) {
	    for (sum = x[i], k = i + 1; k < n; k++)
		sum -= el[k][i] * x[k];
	    x[i] = sum / el[i][i];
	}
    }

    /**
     * Multiply L y = b, where L is the lower triangular matrix in the stored
     * Cholesky decomposition. y[0..n-1] is input. The result is returned in
     * b[0..n-1].
     * 
     * @param y
     * @param b
     */
    public void elmult(final double[] y, final double[] b) {
	int i, j;
	if (b.length != n || y.length != n)
	    throw new IllegalArgumentException("bad lengths");
	for (i = 0; i < n; i++) {
	    b[i] = 0.;
	    for (j = 0; j <= i; j++)
		b[i] += el[i][j] * y[j];
	}
    }

    /**
     * Solve L*y = b, where L is the lower triangular matrix in the stored
     * Cholesky decomposition. b[0..n-1] is input as the right-hand side vector.
     * The solution vector is returned in y[0..n-1].
     * 
     * @param b
     * @param y
     */
    public void elsolve(final double[] b, final double[] y) {
	int i, j;
	double sum;
	if (b.length != n || y.length != n)
	    throw new IllegalArgumentException("bad lengths");
	for (i = 0; i < n; i++) {
	    for (sum = b[i], j = 0; j < i; j++)
		sum -= el[i][j] * y[j];
	    y[i] = sum / el[i][i];
	}
    }

    /**
     * Set ainv[0..n-1][0..n-1] to the matrix inverse of A, the matrix whose
     * Cholesky decomposition has been stored.
     * 
     * @param ainv
     */
    public void inverse(final double[][] ainv) {
	if (ainv == null || ainv[0] == null)
	    throw new IllegalArgumentException("Must be n x n matrix");
	if (ainv.length != n || ainv[0].length != n)
	    throw new IllegalArgumentException("Must be n x n matrix");

	int i, j, k;
	double sum;
	// ainv = resize(ainv, n,n);
	for (i = 0; i < n; i++)
	    for (j = 0; j <= i; j++) {
		sum = (i == j ? 1. : 0.);
		for (k = i - 1; k >= j; k--)
		    sum -= el[i][k] * ainv[j][k];
		ainv[j][i] = sum / el[i][i];
	    }
	for (i = n - 1; i >= 0; i--)
	    for (j = 0; j <= i; j++) {
		sum = (i < j ? 0. : ainv[j][i]);
		for (k = i + 1; k < n; k++)
		    sum -= el[k][i] * ainv[j][k];
		ainv[i][j] = ainv[j][i] = sum / el[i][i];
	    }
    }

    /**
     * Return the logarithm of the determinant of A, the matrix whose Cholesky
     * decomposition has been stored.
     * 
     * @return
     */
    public double logdet() {
	double sum = 0.;
	for (int i = 0; i < n; i++)
	    sum += log(el[i][i]);
	return 2. * sum;
    }
}
