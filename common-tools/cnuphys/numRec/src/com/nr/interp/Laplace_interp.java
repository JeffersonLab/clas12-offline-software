package com.nr.interp;

import static java.lang.Math.*;

import org.netlib.util.*;

import com.nr.la.Linbcg;

/**
 * Object for interpolating missing data in a matrix by solving Laplace's
 * equation. Call constructor once, then solve one or more times
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class Laplace_interp extends Linbcg {
    double[][] mat;
    int ii, jj;
    int nn, iter;
    double[] b, y, mask;

    /**
     * Values greater than 1.e99 in the input matrix mat are deemed to be
     * missing data. The matrix is not altered until solve is called.
     * 
     * @param matrix
     */
    public Laplace_interp(final double[][] matrix) {
	mat = matrix;
	ii = mat.length;
	jj = mat[0].length;
	nn = ii * jj;
	iter = 0;
	b = new double[nn];
	y = new double[nn];
	mask = new double[nn];
	int i, j, k;
	double vl = 0.;
	for (k = 0; k < nn; k++) {
	    i = k / jj;
	    j = k - i * jj;
	    if (mat[i][j] < 1.e99) {
		b[k] = y[k] = vl = mat[i][j];
		mask[k] = 1;
	    } else {
		b[k] = 0.;
		y[k] = vl;
		mask[k] = 0;
	    }
	}
    }

    public double solve() {
	return solve(1.e-6, -1);
    }

    /**
     * Invoke Linbcg::solve with appropriate arguments. The default argument
     * values will usually work, in which case this routine need be called only
     * once. The original matrix mat is refilled with the interpolated solution.
     * 
     * @param tol
     * @param itmax
     * @return
     */
    public double solve(final double tol, final int itmax) {
	int i, j, k;
	int itmax0 = itmax;
	doubleW err = new doubleW(0);
	if (itmax0 <= 0)
	    itmax0 = 2 * max(ii, jj);
	intW iter0 = new intW(0);
	iter0.val = iter;
	super.solve(b, y, 1, tol, itmax0, iter0, err);
	iter = iter0.val;
	for (k = 0, i = 0; i < ii; i++)
	    for (j = 0; j < jj; j++)
		mat[i][j] = y[k++];
	return err.val;
    }

    /**
     * Diagonal preconditioner. (Diagonal elements all unity.)
     * 
     */
    @Override
    public void asolve(final double[] b, final double[] x, final int itrnsp) {
	int i, n = b.length;
	for (i = 0; i < n; i++)
	    x[i] = b[i];
    }

    /**
     * Sparse matrix, and matrix transpose, multiply.
     */
    @Override
    public void atimes(final double[] x, final double[] r, final int itrnsp) {
	int i, j, k, n = r.length, jjt, it;
	double del;
	for (k = 0; k < n; k++)
	    r[k] = 0.;
	for (k = 0; k < n; k++) {
	    i = k / jj;
	    j = k - i * jj;
	    if (mask[k] != 0) {
		r[k] += x[k];
	    } else if (i > 0 && i < ii - 1 && j > 0 && j < jj - 1) {
		if (itrnsp != 0) {
		    r[k] += x[k];
		    del = -0.25 * x[k];
		    r[k - 1] += del;
		    r[k + 1] += del;
		    r[k - jj] += del;
		    r[k + jj] += del;
		} else {
		    r[k] = x[k] - 0.25
			    * (x[k - 1] + x[k + 1] + x[k + jj] + x[k - jj]);
		}
	    } else if (i > 0 && i < ii - 1) {
		if (itrnsp != 0) {
		    r[k] += x[k];
		    del = -0.5 * x[k];
		    r[k - jj] += del;
		    r[k + jj] += del;
		} else {
		    r[k] = x[k] - 0.5 * (x[k + jj] + x[k - jj]);
		}
	    } else if (j > 0 && j < jj - 1) {
		if (itrnsp != 0) {
		    r[k] += x[k];
		    del = -0.5 * x[k];
		    r[k - 1] += del;
		    r[k + 1] += del;
		} else {
		    r[k] = x[k] - 0.5 * (x[k + 1] + x[k - 1]);
		}
	    } else {
		jjt = i == 0 ? jj : -jj;
		it = j == 0 ? 1 : -1;
		if (itrnsp != 0) {
		    r[k] += x[k];
		    del = -0.5 * x[k];
		    r[k + jjt] += del;
		    r[k + it] += del;
		} else {
		    r[k] = x[k] - 0.5 * (x[k + jjt] + x[k + it]);
		}
	    }
	}
    }
}