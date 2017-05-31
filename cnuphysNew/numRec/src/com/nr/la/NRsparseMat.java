package com.nr.la;

/**
 * Sparse matrix data structure for compressed column storage.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class NRsparseMat {
    /**
     * Number of rows.
     */
    public int nrows = 0;

    /**
     * Number of columns.
     */
    public int ncols = 0;

    /**
     * Maximum number of nonzeros.
     */
    public int nvals = 0;

    /**
     * Pointers to start of columns. Length is ncols+1.
     */
    public int[] col_ptr = null;

    /**
     * Row indices of nonzeros
     */
    public int[] row_ind = null;

    /**
     * Array of nonzero values.
     */
    public double[] val = null;

    public NRsparseMat() {
    }

    public NRsparseMat(final int m, final int n, final int nnvals) {
	nrows = m;
	ncols = n;
	nvals = nnvals;
	col_ptr = new int[n + 1];
	row_ind = new int[nnvals];
	val = new double[nnvals];
    }

    public double[] ax(final double[] x) {
	double[] y = new double[nrows];
	for (int j = 0; j < ncols; j++) {
	    for (int i = col_ptr[j]; i < col_ptr[j + 1]; i++)
		y[row_ind[i]] += val[i] * x[j];
	}
	return y;
    }

    public double[] atx(final double[] x) {
	double[] y = new double[ncols];
	for (int i = 0; i < ncols; i++) {
	    y[i] = 0.0;
	    for (int j = col_ptr[i]; j < col_ptr[i + 1]; j++)
		y[i] += val[j] * x[row_ind[j]];
	}
	return y;
    }

    public NRsparseMat transpose() {
	int i, j, k, index, m = nrows, n = ncols;
	NRsparseMat at = new NRsparseMat(n, m, nvals);
	int[] count = new int[m];
	for (i = 0; i < n; i++)
	    for (j = col_ptr[i]; j < col_ptr[i + 1]; j++) {
		k = row_ind[j];
		count[k]++;
	    }
	for (j = 0; j < m; j++)
	    at.col_ptr[j + 1] = at.col_ptr[j] + count[j];
	for (j = 0; j < m; j++)
	    count[j] = 0;
	for (i = 0; i < n; i++)
	    for (j = col_ptr[i]; j < col_ptr[i + 1]; j++) {
		k = row_ind[j];
		index = at.col_ptr[k] + count[k];
		at.row_ind[index] = i;
		at.val[index] = val[j];
		count[k]++;
	    }
	return at;
    }
}
