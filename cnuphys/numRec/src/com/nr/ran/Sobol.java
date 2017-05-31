package com.nr.ran;

import static java.lang.Math.*;

/**
 * Sobol quasi-random sequence
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Sobol {
    private Sobol() {
    }

    private static final int MAXBIT = 30, MAXDIM = 6;
    private static int[] mdeg = { 1, 2, 3, 3, 4, 4 };
    private static int in;
    private static int[] ix = new int[MAXDIM];
    private static int[][] iu = new int[MAXBIT][];
    private static int[] ip = { 0, 1, 1, 2, 1, 4 };
    // static int[] iv = {1,1,1,1,1,1,3,1,3,3,1,1,5,7,7,3,3,5,15,11,5,15,13,9};
    private static double fac;

    static {
	for (int i = 0; i < MAXBIT; i++)
	    iu[i] = new int[MAXDIM];
	int[] iv = { 1, 1, 1, 1, 1, 1, 3, 1, 3, 3, 1, 1, 5, 7, 7, 3, 3, 5, 15,
		11, 5, 15, 13, 9 };
	for (int j = 0; j < 4; j++)
	    for (int k = 0; k < MAXDIM; k++)
		iu[j][k] = iv[j * MAXDIM + k];
    }

    public static void sobseq(final int n, final double[] x) {
	int j, k, l;
	int i, im, ipp;

	if (n < 0) {
	    for (k = 0; k < MAXDIM; k++)
		ix[k] = 0;
	    in = 0;
	    if (iu[0][0] != 1)
		return;
	    // if (iv[0] != 1) return;
	    fac = 1.0 / (1 << MAXBIT);
	    // for (j=0,k=0;j<MAXBIT;j++,k+=MAXDIM) iu[j] = &iv[k];
	    {
		int[] iv = { 1, 1, 1, 1, 1, 1, 3, 1, 3, 3, 1, 1, 5, 7, 7, 3, 3,
			5, 15, 11, 5, 15, 13, 9 };
		for (j = 0; j < 4; j++)
		    for (k = 0; k < MAXDIM; k++)
			iu[j][k] = iv[j * MAXDIM + k];
	    }
	    for (k = 0; k < MAXDIM; k++) {
		for (j = 0; j < mdeg[k]; j++)
		    iu[j][k] <<= (MAXBIT - 1 - j);
		for (j = mdeg[k]; j < MAXBIT; j++) {
		    ipp = ip[k];
		    i = iu[j - mdeg[k]][k];
		    i ^= (i >> mdeg[k]);
		    for (l = mdeg[k] - 1; l >= 1; l--) {
			if ((ipp & 1) != 0)
			    i ^= iu[j - l][k];
			ipp >>= 1;
		    }
		    iu[j][k] = i;
		}
	    }
	} else {
	    im = in++;
	    for (j = 0; j < MAXBIT; j++) {
		if ((im & 1) == 0)
		    break;
		im >>= 1;
	    }
	    if (j >= MAXBIT)
		throw new IllegalArgumentException("MAXBIT too small in sobseq");
	    im = j * MAXDIM;
	    for (k = 0; k < min(n, MAXDIM); k++) {
		// ix[k] ^= iv[im+k];
		ix[k] ^= iu[j][k];
		x[k] = ix[k] * fac;
	    }
	}
    }
}
