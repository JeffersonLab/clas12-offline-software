package com.nr.stat;

import static java.lang.Math.*;
import com.nr.la.LUdcmp;

/**
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class SavitzkyGolayFilter {
    private SavitzkyGolayFilter() {
    }

    /**
     * Returns in c[0..np-1], in wraparound order (N.B.!) consistent with the
     * argument respns in routine convlv, a set of Savitzky-Golay filter
     * coefficients. nl is the number of leftward (past) data points used, while
     * nr is the number of rightward (future) data points, making the total
     * number of data points used nl+nr+1. ld is the order of the derivative
     * desired (e.g., ld D 0 for smoothed function. For the derivative of order
     * k, you must multiply the array c by k!) m is the order of the smoothing
     * polynomial, also equal to the highest conserved moment; usual values are
     * m=2 or m=4
     * 
     * @param c
     * @param np
     * @param nl
     * @param nr
     * @param ld
     * @param m
     */
    public static void savgol(final double[] c, final int np, final int nl,
	    final int nr, final int ld, final int m) {
	int j, k, imj, ipj, kk, mm;
	double fac, sum;
	if (np < nl + nr + 1 || nl < 0 || nr < 0 || ld > m || nl + nr < m)
	    throw new IllegalArgumentException("bad args in savgol");
	double[][] a = new double[m + 1][m + 1];
	double[] b = new double[m + 1];
	for (ipj = 0; ipj <= (m << 1); ipj++) {
	    sum = (ipj != 0 ? 0.0 : 1.0);
	    for (k = 1; k <= nr; k++)
		sum += pow(k, ipj);
	    for (k = 1; k <= nl; k++)
		sum += pow(-k, ipj);
	    mm = min(ipj, 2 * m - ipj);
	    for (imj = -mm; imj <= mm; imj += 2)
		a[(ipj + imj) / 2][(ipj - imj) / 2] = sum;
	}
	LUdcmp alud = new LUdcmp(a);
	for (j = 0; j < m + 1; j++)
	    b[j] = 0.0;
	b[ld] = 1.0;
	alud.solve(b, b);
	for (kk = 0; kk < np; kk++)
	    c[kk] = 0.0;
	for (k = -nl; k <= nr; k++) {
	    sum = b[0];
	    fac = 1.0;
	    for (mm = 1; mm <= m; mm++)
		sum += b[mm] * (fac *= k);
	    kk = (np - k) % np;
	    c[kk] = sum;
	}
    }
}
