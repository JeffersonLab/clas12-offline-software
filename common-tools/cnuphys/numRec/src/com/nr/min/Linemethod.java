package com.nr.min;

import com.nr.RealValueFun;

public class Linemethod {
    public double[] p;
    public double[] xi;
    RealValueFun func;
    int n;

    public Linemethod(final RealValueFun funcc) {
	func = funcc;
    }

    /**
     * @return
     */
    public double linmin() {
	double ax, xx, xmin;
	n = p.length;
	F1dim f1dim = new F1dim(p, xi, func);
	ax = 0.0;
	xx = 1.0;
	Brent brent = new Brent();
	brent.bracket(ax, xx, f1dim);
	xmin = brent.minimize(f1dim);
	for (int j = 0; j < n; j++) {
	    xi[j] *= xmin;
	    p[j] += xi[j];
	}
	return brent.fmin;
    }
}
