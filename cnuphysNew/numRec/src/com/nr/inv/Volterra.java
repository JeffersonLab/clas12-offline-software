package com.nr.inv;

import com.nr.la.LUdcmp;

/**
 * Solves a set of m linear Volterra equations of the second kind using the
 * extended trapezoidal rule. On input, t0 is the starting point of the
 * integration and h is the step size. g(k,t) is a user-supplied function or
 * functor that returns gk(t), while ak(k,l,t,s) is another user- supplied
 * function or functor that returns the (k,l) element of the matrix K(t,s). The
 * solution is returned in f[0..m-1][0..n-1], with the corresponding abscissas
 * in t[0..n-1], where n-1 is the number of steps to be taken. The value of m is
 * determined from the row-dimension of the solution matrix f.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public abstract class Volterra {

    public abstract double g(int k, double t);

    public abstract double ak(int k, int l, double t1, double t2);

    public void voltra(final double t0, final double h, final double[] t,
	    final double[][] f) {
	int m = f.length;
	int n = f[0].length;
	double[] b = new double[m];
	double[][] a = new double[m][m];
	t[0] = t0;
	for (int k = 0; k < m; k++)
	    f[k][0] = g(k, t[0]);
	for (int i = 1; i < n; i++) {
	    t[i] = t[i - 1] + h;
	    for (int k = 0; k < m; k++) {
		double sum = g(k, t[i]);
		for (int l = 0; l < m; l++) {
		    sum += 0.5 * h * ak(k, l, t[i], t[0]) * f[l][0];
		    for (int j = 1; j < i; j++)
			sum += h * ak(k, l, t[i], t[j]) * f[l][j];
		    if (k == l)
			a[k][l] = 1.0 - 0.5 * h * ak(k, l, t[i], t[i]);
		    else
			a[k][l] = -0.5 * h * ak(k, l, t[i], t[i]);
		}
		b[k] = sum;
	    }
	    LUdcmp alu = new LUdcmp(a);
	    alu.solve(b, b);
	    for (int k = 0; k < m; k++)
		f[k][i] = b[k];
	}
    }
}
