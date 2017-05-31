package com.nr.la;

import static com.nr.NRUtil.*;

/**
 * Sparse vector data structure.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class NRsparseCol {
    /**
     * Number of rows.
     * 
     */
    public int nrows;

    /**
     * Maximum number of nonzeros.
     * 
     */
    public int nvals;

    /**
     * Row indices of nonzeros.
     * 
     */
    public int[] row_ind;

    /**
     * Array of nonzero values.
     * 
     */
    public double[] val;

    public NRsparseCol(final int m, final int nnvals) {
	nrows = m;
	nvals = nnvals;
	row_ind = new int[nnvals];
	val = buildVector(nnvals, 0.0);
    }

    public NRsparseCol() {
	nrows = 0;
	nvals = 0;
	row_ind = null;
	val = null;
    }

    public void resize(final int m, final int nnvals) {
	nrows = m;
	nvals = nnvals;
	row_ind = new int[nnvals];
	val = new double[nvals];
    }
}
