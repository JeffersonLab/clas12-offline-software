package com.nr.pde;

import static java.lang.Math.*;

/**
 * Differentiation matrix for spectral methods
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Weights {
    private Weights() {
    }

    public static void weights(final double z, final double[] x,
	    final double[][] c) {
	int n = c.length - 1;
	int m = c[0].length - 1;
	double c1 = 1.0;
	double c4 = x[0] - z;
	for (int k = 0; k <= m; k++)
	    for (int j = 0; j <= n; j++)
		c[j][k] = 0.0;
	c[0][0] = 1.0;
	for (int i = 1; i <= n; i++) {
	    int mn = min(i, m);
	    double c2 = 1.0;
	    double c5 = c4;
	    c4 = x[i] - z;
	    for (int j = 0; j < i; j++) {
		double c3 = x[i] - x[j];
		c2 = c2 * c3;
		if (j == i - 1) {
		    for (int k = mn; k > 0; k--)
			c[i][k] = c1 * (k * c[i - 1][k - 1] - c5 * c[i - 1][k])
				/ c2;
		    c[i][0] = -c1 * c5 * c[i - 1][0] / c2;
		}
		for (int k = mn; k > 0; k--)
		    c[j][k] = (c4 * c[j][k] - k * c[j][k - 1]) / c3;
		c[j][0] = c4 * c[j][0] / c3;
	    }
	    c1 = c2;
	}
    }
}
