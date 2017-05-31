package com.nr.la;

/**
 * Solve tridiagonal linear systems - PTC Copyright (C) Numerical Recipes
 * Software 1986-2007 Java translation Copyright (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Tridag {
    private Tridag() {
    }

    /**
     * Solves for a vector u[0..n-1] the tridiagonal linear set. a[0..n-1],
     * b[0..n-1], c[0..n-1], and r[0..n-1] are input vectors and are not
     * modified.
     * 
     * @param a
     * @param b
     * @param c
     * @param r
     * @param u
     */
    public static void tridag(final double[] a, final double[] b,
	    final double[] c, final double[] r, final double[] u) {
	int j, n = a.length;
	double bet;
	double[] gam = new double[n];
	if (b[0] == 0.0)
	    throw new IllegalArgumentException("Error 1 in tridag");
	u[0] = r[0] / (bet = b[0]);
	for (j = 1; j < n; j++) {
	    gam[j] = c[j - 1] / bet;
	    bet = b[j] - a[j] * gam[j];
	    if (bet == 0.0)
		throw new IllegalArgumentException("Error 2 in tridag");
	    u[j] = (r[j] - a[j] * u[j - 1]) / bet;
	}
	for (j = (n - 2); j >= 0; j--)
	    u[j] -= gam[j + 1] * u[j + 1];
    }

    /**
     * Solves for a vector x[0..n-1] the "cyclic" set of linear equations. a, b,
     * c, and r are input vectors, all dimensioned as [0..n-1], while alpha and
     * beta are the corner entries in the matrix. The input is not modified.
     * 
     * @param a
     * @param b
     * @param c
     * @param alpha
     * @param beta
     * @param r
     * @param x
     */
    public static void cyclic(final double[] a, final double[] b,
	    final double[] c, final double alpha, final double beta,
	    final double[] r, final double[] x) {
	int i, n = a.length;
	double fact, gamma;
	if (n <= 2)
	    throw new IllegalArgumentException("n too small in cyclic");
	double[] bb = new double[n];
	double[] u = new double[n];
	double[] z = new double[n];
	gamma = -b[0];
	bb[0] = b[0] - gamma;
	bb[n - 1] = b[n - 1] - alpha * beta / gamma;
	for (i = 1; i < n - 1; i++)
	    bb[i] = b[i];
	tridag(a, bb, c, r, x);
	u[0] = gamma;
	u[n - 1] = alpha;
	for (i = 1; i < n - 1; i++)
	    u[i] = 0.0;
	tridag(a, bb, c, u, z);
	fact = (x[0] + beta * x[n - 1] / gamma)
		/ (1.0 + z[0] + beta * z[n - 1] / gamma);
	for (i = 0; i < n; i++)
	    x[i] -= fact * z[i];
    }
}
