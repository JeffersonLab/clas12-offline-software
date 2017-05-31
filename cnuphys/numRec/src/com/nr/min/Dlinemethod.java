package com.nr.min;

import com.nr.RealValueFunWithDiff;

/**
 * Base class for line-minimization algorithms using derivative information.
 * Provides the line-minimization routine linmin.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Dlinemethod {
    public double[] p;
    public double[] xi;
    RealValueFunWithDiff func;
    int n;

    public Dlinemethod(final RealValueFunWithDiff funcc) {
	func = funcc;
    }

    /**
     * @return
     */
    public double linmin() {
	double ax, xx, xmin;
	n = p.length;
	Df1dim df1dim = new Df1dim(p, xi, func);
	ax = 0.0;
	xx = 1.0;
	Dbrent dbrent = new Dbrent();
	dbrent.bracket(ax, xx, df1dim);
	xmin = dbrent.minimize(df1dim);
	for (int j = 0; j < n; j++) {
	    xi[j] *= xmin;
	    p[j] += xi[j];
	}
	return dbrent.fmin;
    }
}
