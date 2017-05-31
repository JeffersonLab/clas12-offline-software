package com.nr.la;

import java.util.Arrays;

/**
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class ADAT {
    final NRsparseMat a, at;

    private NRsparseMat adat; // XXX pointer to ref

    public ADAT(final NRsparseMat A, final NRsparseMat AT) {
	a = A;
	at = AT;

	int h, i, j, k, l, nvals, m = AT.ncols;
	int[] done = new int[m];
	for (i = 0; i < m; i++)
	    done[i] = -1;
	nvals = 0;
	for (j = 0; j < m; j++) {
	    for (i = AT.col_ptr[j]; i < AT.col_ptr[j + 1]; i++) {
		k = AT.row_ind[i];
		for (l = A.col_ptr[k]; l < A.col_ptr[k + 1]; l++) {
		    h = A.row_ind[l];
		    if (done[h] != j) {
			done[h] = j;
			nvals++;
		    }
		}
	    }
	}
	adat = new NRsparseMat(m, m, nvals);
	for (i = 0; i < m; i++)
	    done[i] = -1;
	nvals = 0;
	for (j = 0; j < m; j++) {
	    adat.col_ptr[j] = nvals;
	    for (i = AT.col_ptr[j]; i < AT.col_ptr[j + 1]; i++) {
		k = AT.row_ind[i];
		for (l = A.col_ptr[k]; l < A.col_ptr[k + 1]; l++) {
		    h = A.row_ind[l];
		    if (done[h] != j) {
			done[h] = j;
			adat.row_ind[nvals] = h;
			nvals++;
		    }
		}
	    }
	}
	adat.col_ptr[m] = nvals;
	for (j = 0; j < m; j++) {
	    i = adat.col_ptr[j];
	    int size = adat.col_ptr[j + 1] - i;
	    if (size > 1) { // XXX pointer
		// int[] col(size,&adat->row_ind[i]);
		int[] col = new int[size];
		for (int kk = 0; kk < col.length; kk++) {
		    col[kk] = adat.row_ind[i + kk];
		}
		Arrays.sort(col); // XXX use java sort
		for (k = 0; k < size; k++)
		    adat.row_ind[i + k] = col[k];
	    }
	}
    }

    public void updateD(final double[] D) {
	int h, i, j, k, l, m = a.nrows, n = a.ncols;
	double[] temp = new double[n];
	double[] temp2 = new double[m];
	for (i = 0; i < m; i++) {
	    for (j = at.col_ptr[i]; j < at.col_ptr[i + 1]; j++) {
		k = at.row_ind[j];
		temp[k] = at.val[j] * D[k];
	    }
	    for (j = at.col_ptr[i]; j < at.col_ptr[i + 1]; j++) {
		k = at.row_ind[j];
		for (l = a.col_ptr[k]; l < a.col_ptr[k + 1]; l++) {
		    h = a.row_ind[l];
		    temp2[h] += temp[k] * a.val[l];
		}
	    }
	    for (j = adat.col_ptr[i]; j < adat.col_ptr[i + 1]; j++) {
		k = adat.row_ind[j];
		adat.val[j] = temp2[k];
		temp2[k] = 0.0;
	    }
	}
    }

    public NRsparseMat ref() {
	return adat;
    }

}
