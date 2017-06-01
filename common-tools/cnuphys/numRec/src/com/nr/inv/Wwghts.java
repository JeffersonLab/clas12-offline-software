package com.nr.inv;

import com.nr.UniVarRealMultiValueFun;
import static com.nr.NRUtil.*;

/**
 * Constructs weights for the n-point equal-interval quadrature from O to (n-1)h
 * of a function f(x) times an arbitrary (possibly singular) weight function
 * w(x). The indefinite-integral moments Fn(y) of w(x) are provided by the
 * user-supplied function kermom in the quad object.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Wwghts {
    double h;
    int n;
    UniVarRealMultiValueFun quad; // kermom
    double[] wghts;

    public Wwghts(final double hh, final int nn, final UniVarRealMultiValueFun q) {
	h = hh;
	n = nn;
	quad = q;
	wghts = new double[n];
    }

    public double[] weights() {
	int k;
	double fac;
	double hi = 1.0 / h;
	for (int j = 0; j < n; j++)
	    wghts[j] = 0.0;
	if (n >= 4) {
	    double[] wold = new double[4], wnew = new double[4], w = new double[4];
	    wold = buildVector(quad.funk(0.0));
	    double b = 0.0;
	    for (int j = 0; j < n - 3; j++) {
		double c = j;
		double a = b;
		b = a + h;
		if (j == n - 4)
		    b = (n - 1) * h;
		wnew = buildVector(quad.funk(b));
		for (fac = 1.0, k = 0; k < 4; k++, fac *= hi)
		    w[k] = (wnew[k] - wold[k]) * fac;
		wghts[j] += (((c + 1.0) * (c + 2.0) * (c + 3.0) * w[0]
			- (11.0 + c * (12.0 + c * 3.0)) * w[1] + 3.0
			* (c + 2.0) * w[2] - w[3]) / 6.0);
		wghts[j + 1] += ((-c * (c + 2.0) * (c + 3.0) * w[0]
			+ (6.0 + c * (10.0 + c * 3.0)) * w[1] - (3.0 * c + 5.0)
			* w[2] + w[3]) * 0.5);
		wghts[j + 2] += ((c * (c + 1.0) * (c + 3.0) * w[0]
			- (3.0 + c * (8.0 + c * 3.0)) * w[1] + (3.0 * c + 4.0)
			* w[2] - w[3]) * 0.5);
		wghts[j + 3] += ((-c * (c + 1.0) * (c + 2.0) * w[0]
			+ (2.0 + c * (6.0 + c * 3.0)) * w[1] - 3.0 * (c + 1.0)
			* w[2] + w[3]) / 6.0);
		for (k = 0; k < 4; k++)
		    wold[k] = wnew[k];
	    }
	} else if (n == 3) {
	    double[] wold = new double[3], wnew = new double[3], w = new double[3];
	    wold = buildVector(quad.funk(0.0));
	    wnew = buildVector(quad.funk(h + h));
	    w[0] = wnew[0] - wold[0];
	    w[1] = hi * (wnew[1] - wold[1]);
	    w[2] = hi * hi * (wnew[2] - wold[2]);
	    wghts[0] = w[0] - 1.5 * w[1] + 0.5 * w[2];
	    wghts[1] = 2.0 * w[1] - w[2];
	    wghts[2] = 0.5 * (w[2] - w[1]);
	} else if (n == 2) {
	    double[] wold = new double[2], wnew = new double[2];// ,w=new
								// double[2];
	    wold = buildVector(quad.funk(0.0));
	    wnew = buildVector(quad.funk(h));
	    wghts[0] = wnew[0] - wold[0]
		    - (wghts[1] = hi * (wnew[1] - wold[1]));
	}
	return wghts;
    }
}
