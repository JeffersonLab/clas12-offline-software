package com.nr.interp;

import static java.lang.Math.*;

/**
 * polynomial coefficients from polynomial values Copyright (C) Numerical
 * Recipes Software 1986-2007 Java translation Copyright (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class PolCoef {

    private PolCoef() {
    }

    /**
     * Given arrays x[0..n-1] and y[0..n-1] containing a tabulated function yi D
     * f .xi /, this routine returns an array of coefficients cof[0..n-1], such
     * that yi =sum(cofj * xij)(j=0...n-1).
     * 
     * @param x
     * @param y
     * @param cof
     */
    public static void polcoe(final double[] x, final double[] y,
	    final double[] cof) {
	int k, j, i, n = x.length;
	double phi, ff, b;
	double[] s = new double[n];
	for (i = 0; i < n; i++)
	    s[i] = cof[i] = 0.0;
	s[n - 1] = -x[0];
	for (i = 1; i < n; i++) {
	    for (j = n - 1 - i; j < n - 1; j++)
		s[j] -= x[i] * s[j + 1];
	    s[n - 1] -= x[i];
	}
	for (j = 0; j < n; j++) {
	    phi = n;
	    for (k = n - 1; k > 0; k--)
		phi = k * s[k] + x[j] * phi;
	    ff = y[j] / phi;
	    b = 1.0;
	    for (k = n - 1; k >= 0; k--) {
		cof[k] += b * ff;
		b = s[k] + x[j] * b;
	    }
	}
    }

    /**
     * Given arrays xa[0..n-1] and ya[0..n-1] containing a tabulated function
     * yai D f .xai /, this routine returns an array of coefficients
     * cof[0..n-1], such that yai = sum(cofj*xaij)(j=0...n-1) .
     * 
     * @param xa
     * @param ya
     * @param cof
     */
    public static void polcof(final double[] xa, final double[] ya,
	    final double[] cof) {
	int k, j, i, n = xa.length;
	double xmin;
	double[] x = new double[n];
	double[] y = new double[n];
	for (j = 0; j < n; j++) {
	    x[j] = xa[j];
	    y[j] = ya[j];
	}
	for (j = 0; j < n; j++) {
	    double[] x_t = new double[n - j];
	    double[] y_t = new double[n - j];
	    for (k = 0; k < n - j; k++) {
		x_t[k] = x[k];
		y_t[k] = y[k];
	    }
	    Poly_interp interp = new Poly_interp(x, y, n - j);
	    cof[j] = interp.rawinterp(0, 0.);
	    xmin = 1.0e99;
	    k = -1;
	    for (i = 0; i < n - j; i++) {
		if (abs(x[i]) < xmin) {
		    xmin = abs(x[i]);
		    k = i;
		}
		if (x[i] != 0.0)
		    y[i] = (y[i] - cof[j]) / x[i];
	    }
	    for (i = k + 1; i < n - j; i++) {
		y[i - 1] = y[i];
		x[i - 1] = x[i];
	    }
	}
    }

}
