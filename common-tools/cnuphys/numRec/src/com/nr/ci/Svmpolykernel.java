package com.nr.ci;

import static java.lang.Math.*;

public class Svmpolykernel extends Svmgenkernel {
    int n;

    double a, b, d;

    public Svmpolykernel(final double[][] ddata, final double[] yy,
	    final double aa, final double bb, final double dd) {
	super(yy, ddata);
	n = data[0].length;
	a = aa;
	b = bb;
	d = dd;
	fill();
    }

    @Override
    public double kernel(final double[] xi, final double[] xj) {
	double dott = 0.;
	for (int k = 0; k < n; k++)
	    dott += xi[k] * xj[k];
	return pow(a * dott + b, d);
    }
}
