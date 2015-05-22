package com.nr.ci;

import org.netlib.util.doubleW;

public class Phylo_nj extends Phylagglom {
    double[] u;

    @Override
    public void premin(final double[][] d, final int[] nextp) {
	int i, j, ncurr = 0;
	double sum;
	for (i = 0; i >= 0; i = nextp[i])
	    ncurr++;
	for (i = 0; i >= 0; i = nextp[i]) {
	    sum = 0.;
	    for (j = 0; j >= 0; j = nextp[j])
		if (i != j)
		    sum += d[i][j];
	    u[i] = sum / (ncurr - 2);
	}
    }

    @Override
    public double dminfn(final double[][] d, final int i, final int j) {
	return d[i][j] - u[i] - u[j];
    }

    @Override
    public double dbranchfn(final double[][] d, final int i, final int j) {
	return 0.5 * (d[i][j] + u[i] - u[j]);
    }

    @Override
    public double dnewfn(final double[][] d, final int k, final int i,
	    final int j, final int ni, final int nj) {
	return 0.5 * (d[i][k] + d[j][k] - d[i][j]);
    }

    @Override
    public void drootbranchfn(final double[][] d, final int i, final int j,
	    final int ni, final int nj, final doubleW bi, final doubleW bj) {
	bi.val = d[i][j] * (nj - 1 + 1.e-15) / (ni + nj - 2 + 2.e-15);
	bj.val = d[i][j] * (ni - 1 + 1.e-15) / (ni + nj - 2 + 2.e-15);
    }

    public Phylo_nj(final double[][] dist) {
	this(dist, -1);
    }

    public Phylo_nj(final double[][] dist, final int fsr) {
	super(dist, fsr);
	u = new double[n];
	makethetree(dist);
    }
}
