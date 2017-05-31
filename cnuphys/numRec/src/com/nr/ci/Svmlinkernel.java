package com.nr.ci;

public class Svmlinkernel extends Svmgenkernel {
    int n;

    double[] mu;

    public Svmlinkernel(final double[][] ddata, final double[] yy) {
	super(yy, ddata);
	n = data[0].length;
	mu = new double[n];
	int i, j;
	for (j = 0; j < n; j++)
	    mu[j] = 0.;
	for (i = 0; i < m; i++)
	    for (j = 0; j < n; j++)
		mu[j] += data[i][j];
	for (j = 0; j < n; j++)
	    mu[j] /= m;
	fill();
    }

    @Override
    public double kernel(final double[] xi, final double[] xj) {
	double dott = 0.;
	for (int k = 0; k < n; k++)
	    dott += (xi[k] - mu[k]) * (xj[k] - mu[k]);
	return dott;
    }
}
