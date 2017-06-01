package com.nr.la;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;

/**
 * Band-Diagonal Systems - PTC
 * 
 * Object for solving linear equations A*x = b for a band-diagonal matrix A,
 * using LU decomposition
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Bandec {
    int n, m1, m2;
    /**
     * Upper and lower triangular matrices, stored compactly.
     */
    double[][] au, al;
    int[] indx;
    double d;

    public static void banmul(final double[][] a, final int m1, final int m2,
	    final double[] x, final double[] b) {
	int i, j, k, tmploop, n = a.length;
	for (i = 0; i < n; i++) {
	    k = i - m1;
	    tmploop = min(m1 + m2 + 1, (n - k));
	    b[i] = 0.0;
	    for (j = max(0, -k); j < tmploop; j++)
		b[i] += a[i][j] * x[j + k];
	}
    }

    public Bandec(final double[][] a, final int mm1, final int mm2) {
	n = a.length;
	au = buildMatrix(a);
	m1 = mm1;
	m2 = mm2;
	al = buildMatrix(n, m1, 0.0);
	indx = new int[n];

	final double TINY = 1.0e-40;
	int i, j, k, l, mm;
	double dum;
	mm = m1 + m2 + 1;
	l = m1;
	for (i = 0; i < m1; i++) {
	    for (j = m1 - i; j < mm; j++)
		au[i][j - l] = au[i][j];
	    l--;
	    for (j = mm - l - 1; j < mm; j++)
		au[i][j] = 0.0;
	}
	d = 1.0;
	l = m1;
	for (k = 0; k < n; k++) {
	    dum = au[k][0];
	    i = k;
	    if (l < n)
		l++;
	    for (j = k + 1; j < l; j++) {
		if (abs(au[j][0]) > abs(dum)) {
		    dum = au[j][0];
		    i = j;
		}
	    }
	    indx[k] = i + 1;
	    if (dum == 0.0)
		au[k][0] = TINY;
	    if (i != k) {
		d = -d;
		for (j = 0; j < mm; j++) {
		    // SWAP(au[k][j],au[i][j]);
		    double swap = au[k][j];
		    au[k][j] = au[i][j];
		    au[i][j] = swap;
		}
	    }
	    for (i = k + 1; i < l; i++) {
		dum = au[i][0] / au[k][0];
		al[k][i - k - 1] = dum;
		for (j = 1; j < mm; j++)
		    au[i][j - 1] = au[i][j] - dum * au[k][j];
		au[i][mm - 1] = 0.0;
	    }
	}
    }

    /**
     * Given a right-hand side vector b[0..n-1], solves the band-diagonal linear
     * equations A*x = b. The solution vector x is returned as x[0..n-1].
     * 
     * @param b
     * @param x
     */
    public void solve(final double[] b, final double[] x) {
	int i, j, k, l, mm;
	double dum;
	mm = m1 + m2 + 1;
	l = m1;
	for (k = 0; k < n; k++)
	    x[k] = b[k];
	for (k = 0; k < n; k++) {
	    j = indx[k] - 1;
	    if (j != k) {
		swap(x, k, j);
	    }
	    if (l < n)
		l++;
	    for (j = k + 1; j < l; j++)
		x[j] -= al[k][j - k - 1] * x[k];
	}
	l = 1;
	for (i = n - 1; i >= 0; i--) {
	    dum = x[i];
	    for (k = 1; k < l; k++)
		dum -= au[i][k] * x[k + i];
	    x[i] = dum / au[i][0];
	    if (l < mm)
		l++;
	}
    }

    /**
     * Using the stored decomposition, return the determinant of the matrix A.
     * 
     * @return
     */
    public double det() {
	double dd = d;
	for (int i = 0; i < n; i++)
	    dd *= au[i][0];
	return dd;
    }

}
