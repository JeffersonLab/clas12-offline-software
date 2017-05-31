package com.nr.sort;

/**
 * Find equivalence classes Copyright (C) Numerical Recipes Software 1986-2007
 * Java translation Copyright (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class EClass {
    private EClass() {
    }

    public static void eclass(final int[] nf, final int[] lista,
	    final int[] listb) {
	int l, k, j, n = nf.length, m = lista.length;
	for (k = 0; k < n; k++)
	    nf[k] = k;
	for (l = 0; l < m; l++) {
	    j = lista[l];
	    while (nf[j] != j)
		j = nf[j];
	    k = listb[l];
	    while (nf[k] != k)
		k = nf[k];
	    if (j != k)
		nf[j] = k;
	}
	for (j = 0; j < n; j++)
	    while (nf[j] != nf[nf[j]])
		nf[j] = nf[nf[j]];
    }

    public static void eclazz(final int[] nf, final EquivalenceInf eq) {
	int kk, jj, n = nf.length;
	nf[0] = 0;
	for (jj = 1; jj < n; jj++) {
	    nf[jj] = jj;
	    for (kk = 0; kk < jj; kk++) {
		nf[kk] = nf[nf[kk]];
		if (eq.equiv(jj + 1, kk + 1))
		    nf[nf[nf[kk]]] = jj;
	    }
	}
	for (jj = 0; jj < n; jj++)
	    nf[jj] = nf[nf[jj]];
    }
}
