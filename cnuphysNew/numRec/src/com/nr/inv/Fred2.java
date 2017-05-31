package com.nr.inv;

import static com.nr.fi.GaussianWeights.*;
import com.nr.la.LUdcmp;

public abstract class Fred2 {
    final double a, b;
    final int n;
    public double[] t, f, w;

    public abstract double g(double x);

    public abstract double ak(double x, double t);

    public Fred2(final double a, final double b, final int nn) {
	this.a = a;
	this.b = b;
	this.n = nn;
	t = new double[n];
	f = new double[n];
	w = new double[n];

	double[][] omk = new double[n][n];
	gauleg(a, b, t, w);
	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < n; j++) {
		omk[i][j] = ((i == j) ? 1 : 0) - ak(t[i], t[j]) * w[j];
	    }
	    f[i] = g(t[i]);
	}
	LUdcmp alu = new LUdcmp(omk);
	alu.solve(f, f);
    }

    public double fredin(final double x) {
	double sum = 0.0;
	for (int i = 0; i < n; i++)
	    sum += ak(x, t[i]) * w[i] * f[i];
	return g(x) + sum;
    }
}
