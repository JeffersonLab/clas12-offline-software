package com.nr.ci;

import org.netlib.util.doubleW;

public class Phylo_wpgma extends Phylagglom {
    @Override
    public void premin(final double[][] d, final int[] nextp) {
    }

    @Override
    public double dminfn(final double[][] d, final int i, final int j) {
	return d[i][j];
    }

    @Override
    public double dbranchfn(final double[][] d, final int i, final int j) {
	return 0.5 * d[i][j];
    }

    @Override
    public double dnewfn(final double[][] d, final int k, final int i,
	    final int j, final int ni, final int nj) {
	return 0.5 * (d[i][k] + d[j][k]);
    }

    @Override
    public void drootbranchfn(final double[][] d, final int i, final int j,
	    final int ni, final int nj, final doubleW bi, final doubleW bj) {
	bi.val = bj.val = 0.5 * d[i][j];
    }

    public Phylo_wpgma(final double[][] dist) {
	super(dist);
	makethetree(dist);
    }

}
