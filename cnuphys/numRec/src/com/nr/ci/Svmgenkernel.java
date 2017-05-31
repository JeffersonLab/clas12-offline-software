package com.nr.ci;

public abstract class Svmgenkernel {
    int m, kcalls;

    double[][] ker;

    double[] y;

    double[][] data;

    public Svmgenkernel(final double[] yy, final double[][] ddata) {
	m = yy.length;
	kcalls = 0;
	ker = new double[m][m];
	y = yy;
	data = ddata;
    }

    public abstract double kernel(final double[] xi, final double[] xj);

    public double kernel(final int i, final double[] xj) {
	return kernel(data[i], xj);
    }

    public void fill() {
	int i, j;
	for (i = 0; i < m; i++)
	    for (j = 0; j <= i; j++) {
		ker[i][j] = ker[j][i] = kernel(data[i], data[j]);
	    }
    }
}
