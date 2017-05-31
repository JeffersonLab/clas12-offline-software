package com.nr.la;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;

/**
 * LU decomposition - PTC
 * 
 * Iterative Improvement of a Solution to Linear Equations
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class LUdcmp {
    private int n;
    /**
     * Stores the decomposition
     */
    private double[][] lu;

    /**
     * Stores the permutation.
     */
    private int[] indx;

    /**
     * Used by det.
     */
    private double d;

    private double[][] aref;

    /**
     * Given a matrix a[0..n-1][0..n-1], this routine replaces it by the LU
     * decomposition of a rowwise permutation of itself. a is input. On output,
     * it is arranged as in equation (2.3.14) above; indx[0..n-1] is an output
     * vector that records the row permutation effected by the partial pivoting;
     * d is output as +/-1 depending on whether the number of row interchanges
     * was even or odd, respectively. This routine is used in combination with
     * solve to solve linear equations or invert a matrix.
     * 
     * @param a
     */
    public LUdcmp(final double[][] a) {
	n = a.length;
	lu = buildMatrix(a);
	aref = a;
	indx = new int[n];

	final double TINY = 1.0e-40;
	int i, imax, j, k;
	double big, temp;
	double[] vv = new double[n];
	d = 1.0;
	for (i = 0; i < n; i++) {
	    big = 0.0;
	    for (j = 0; j < n; j++)
		if ((temp = abs(lu[i][j])) > big)
		    big = temp;
	    if (big == 0.0)
		throw new IllegalArgumentException("Singular matrix in LUdcmp");
	    vv[i] = 1.0 / big;
	}
	for (k = 0; k < n; k++) {
	    big = 0.0;
	    imax = k;
	    for (i = k; i < n; i++) {
		temp = vv[i] * abs(lu[i][k]);
		if (temp > big) {
		    big = temp;
		    imax = i;
		}
	    }
	    if (k != imax) {
		for (j = 0; j < n; j++) {
		    temp = lu[imax][j];
		    lu[imax][j] = lu[k][j];
		    lu[k][j] = temp;
		}
		d = -d;
		vv[imax] = vv[k];
	    }
	    indx[k] = imax;
	    if (lu[k][k] == 0.0)
		lu[k][k] = TINY;
	    for (i = k + 1; i < n; i++) {
		temp = lu[i][k] /= lu[k][k];
		for (j = k + 1; j < n; j++)
		    lu[i][j] -= temp * lu[k][j];
	    }
	}
    }

    /**
     * Solves the set of n linear equations A*x = b using the stored LU
     * decomposition of A. b[0..n-1] is input as the right-hand side vector b,
     * while x returns the solution vector x; b and x may reference the same
     * vector, in which case the solution overwrites the input. This routine
     * takes into account the possibility that b will begin with many zero
     * elements, so it is efficient for use in matrix inversion.
     * 
     * @param b
     * @param x
     */
    public void solve(final double[] b, final double[] x) {
	int i, ii = 0, ip, j;
	double sum;
	if (b.length != n || x.length != n)
	    throw new IllegalArgumentException("solve bad sizes");
	for (i = 0; i < n; i++)
	    x[i] = b[i];
	for (i = 0; i < n; i++) {
	    ip = indx[i];
	    sum = x[ip];
	    x[ip] = x[i];
	    if (ii != 0)
		for (j = ii - 1; j < i; j++)
		    sum -= lu[i][j] * x[j];
	    else if (sum != 0.0)
		ii = i + 1;
	    x[i] = sum;
	}
	for (i = n - 1; i >= 0; i--) {
	    sum = x[i];
	    for (j = i + 1; j < n; j++)
		sum -= lu[i][j] * x[j];
	    x[i] = sum / lu[i][i];
	}
    }

    /**
     * Solves m sets of n linear equations A*X = B using the stored LU
     * decomposition of A. The matrix b[0..n-1][0..m-1] inputs the right-hand
     * sides, while x[0..n-1][0..m-1] returns the solution A^-1* B. b and x may
     * reference the same matrix, in which case the solution overwrites the
     * input.
     * 
     * @param b
     * @param x
     */
    public void solve(final double[][] b, final double[][] x) {
	int i, j, m = b[0].length;
	if (b.length != n || x.length != n || b[0].length != x[0].length)
	    throw new IllegalArgumentException("solve bad sizes");
	double[] xx = new double[n];
	for (j = 0; j < m; j++) { // Copy and solve each column in turn.
	    for (i = 0; i < n; i++)
		xx[i] = b[i][j];
	    solve(xx, xx);
	    for (i = 0; i < n; i++)
		x[i][j] = xx[i];
	}
    }

    /**
     * Using the stored LU decomposition, return in ainv the matrix inverse
     * 
     * @param ainv
     */
    public double[][] inverse() {
	int i, j;
	final double[][] ainv = new double[n][n];
	for (i = 0; i < n; i++) {
	    for (j = 0; j < n; j++)
		ainv[i][j] = 0.;
	    ainv[i][i] = 1.;
	}
	solve(ainv, ainv);
	return ainv;
    }

    /**
     * Using the stored LU decomposition, return the determinant of the matrix
     * A.
     * 
     * @return
     */
    public double det() {
	double dd = d;
	for (int i = 0; i < n; i++)
	    dd *= lu[i][i];
	return dd;
    }

    /**
     * Improves a solution vector x[0..n-1] of the linear set of equations A*x=
     * b. The vectors b[0..n-1] and x[0..n-1] are input. On output, x[0..n-1] is
     * modified, to an improved set of values.
     * 
     * @param b
     * @param x
     */
    public void mprove(final double[] b, final double[] x) {
	int i, j;
	double[] r = new double[n];
	for (i = 0; i < n; i++) {
	    double sdp = -b[i];
	    for (j = 0; j < n; j++)
		sdp += aref[i][j] * x[j];
	    r[i] = sdp;
	}
	solve(r, r);
	for (i = 0; i < n; i++)
	    x[i] -= r[i];
    }

}
