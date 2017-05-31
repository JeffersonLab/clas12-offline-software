package com.nr.la;

/**
 * bi-conjugate gradient sparse linear solver (example)
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class NRsparseLinbcg extends Linbcg {
    NRsparseMat mat;
    int n;

    public NRsparseLinbcg(final NRsparseMat matrix) {
	mat = matrix;
	n = mat.nrows;
    }

    @Override
    public void atimes(final double[] x, final double[] r, final int itrnsp) {
	double[] rr;
	if (itrnsp > 0)
	    rr = mat.atx(x);
	else
	    rr = mat.ax(x);
	System.arraycopy(rr, 0, r, 0, rr.length);
    }

    @Override
    public void asolve(final double[] b, final double[] x, final int itrnsp) {
	int i, j;
	double diag;
	for (i = 0; i < n; i++) {
	    diag = 0.0;
	    for (j = mat.col_ptr[i]; j < mat.col_ptr[i + 1]; j++)
		if (mat.row_ind[j] == i) {
		    diag = mat.val[j];
		    break;
		}
	    x[i] = (diag != 0.0 ? b[i] / diag : b[i]);
	}
    }

}
