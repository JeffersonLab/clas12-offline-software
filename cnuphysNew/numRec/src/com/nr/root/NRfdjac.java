package com.nr.root;

import static java.lang.Math.*;
import com.nr.RealMultiValueFun;
import static com.nr.NRUtil.*;

/**
 * Computes forward-difference approximation to Jacobian.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class NRfdjac {
    final double EPS;
    final RealMultiValueFun func;

    public NRfdjac(final RealMultiValueFun funcc) {
	func = funcc;
	EPS = 1.0e-8;
    }

    public double[][] get(final double[] x, final double[] fvec) {
	int n = x.length;
	double[][] df = new double[n][n];
	double[] xh = buildVector(x);
	for (int j = 0; j < n; j++) {
	    double temp = xh[j];
	    double h = EPS * abs(temp);
	    if (h == 0.0)
		h = EPS;
	    xh[j] = temp + h;
	    h = xh[j] - temp;
	    double[] f = func.funk(xh);
	    xh[j] = temp;
	    for (int i = 0; i < n; i++)
		df[i][j] = (f[i] - fvec[i]) / h;
	}
	return df;
    }
}
