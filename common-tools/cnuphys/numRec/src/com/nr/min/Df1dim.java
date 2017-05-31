package com.nr.min;

import com.nr.RealValueFunWithDiff;
import com.nr.UniValRealValueFunWithDiff;

public class Df1dim implements UniValRealValueFunWithDiff {
    final double[] p;
    final double[] xi;
    int n;
    RealValueFunWithDiff funcd;
    double[] xt;
    double[] dft;

    public Df1dim(final double[] pp, final double[] xii,
	    final RealValueFunWithDiff funcdd) {
	p = pp;
	xi = xii;
	n = pp.length;
	funcd = funcdd;
	xt = new double[n];
	dft = new double[n];
    }

    @Override
    public double funk(final double x) {
	return get(x);
    }

    public double get(final double x) {
	for (int j = 0; j < n; j++)
	    xt[j] = p[j] + x * xi[j];
	return funcd.funk(xt);
    }

    @Override
    public double df(final double x) {
	double df1 = 0.0;
	funcd.df(xt, dft);
	for (int j = 0; j < n; j++)
	    df1 += dft[j] * xi[j];
	return df1;
    }
}
