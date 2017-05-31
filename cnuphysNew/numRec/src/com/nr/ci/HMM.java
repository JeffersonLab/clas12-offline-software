package com.nr.ci;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;
import com.nr.ran.Ran;

/**
 * hidden Markov models Copyright (C) Numerical Recipes Software 1986-2007 Java
 * translation Copyright (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class HMM {
    public double[][] a, b;
    public int[] obs;
    public int fbdone;
    public int mstat, nobs, ksym;
    public int lrnrm;
    public double[][] alpha, beta, pstate;
    public int[] arnrm, brnrm;
    private final double BIG, BIGI;
    double lhood;

    public double loglikelihood() {
	return log(lhood) + lrnrm * log(BIGI);
    }

    public HMM(final double[][] aa, final double[][] bb, final int[] obss) {

	a = buildMatrix(aa);
	b = buildMatrix(bb);
	obs = buildVector(obss);
	fbdone = 0;
	mstat = a.length;
	nobs = obs.length;
	ksym = b[0].length;
	alpha = new double[nobs][mstat];
	beta = new double[nobs][mstat];
	pstate = new double[nobs][mstat];
	arnrm = new int[nobs];
	brnrm = new int[nobs];
	BIG = 1.e20;
	BIGI = 1. / BIG;

	int i, j, k;
	double sum;
	if (a[0].length != mstat)
	    throw new IllegalArgumentException("transition matrix not square");
	if (b.length != mstat)
	    throw new IllegalArgumentException("symbol prob matrix wrong size");
	for (i = 0; i < nobs; i++) {
	    if (obs[i] < 0 || obs[i] >= ksym)
		throw new IllegalArgumentException("bad data in obs");
	}
	for (i = 0; i < mstat; i++) {
	    sum = 0.;
	    for (j = 0; j < mstat; j++)
		sum += a[i][j];
	    if (abs(sum - 1.) > 0.01)
		throw new IllegalArgumentException(
			"transition matrix not normalized");
	    for (j = 0; j < mstat; j++)
		a[i][j] /= sum;
	}
	for (i = 0; i < mstat; i++) {
	    sum = 0.;
	    for (k = 0; k < ksym; k++)
		sum += b[i][k];
	    if (abs(sum - 1.) > 0.01)
		throw new IllegalArgumentException(
			"symbol prob matrix not normalized");
	    for (k = 0; k < ksym; k++)
		b[i][k] /= sum;
	}
    }

    public void forwardbackward() {
	int i, j, t;
	double sum, asum, bsum;
	for (i = 0; i < mstat; i++)
	    alpha[0][i] = b[i][obs[0]];
	arnrm[0] = 0;
	for (t = 1; t < nobs; t++) {
	    asum = 0;
	    for (j = 0; j < mstat; j++) {
		sum = 0.;
		for (i = 0; i < mstat; i++)
		    sum += alpha[t - 1][i] * a[i][j] * b[j][obs[t]];
		alpha[t][j] = sum;
		asum += sum;
	    }
	    arnrm[t] = arnrm[t - 1];
	    if (asum < BIGI) {
		++arnrm[t];
		for (j = 0; j < mstat; j++)
		    alpha[t][j] *= BIG;
	    }
	}
	for (i = 0; i < mstat; i++)
	    beta[nobs - 1][i] = 1.;
	brnrm[nobs - 1] = 0;
	for (t = nobs - 2; t >= 0; t--) {
	    bsum = 0.;
	    for (i = 0; i < mstat; i++) {
		sum = 0.;
		for (j = 0; j < mstat; j++)
		    sum += a[i][j] * b[j][obs[t + 1]] * beta[t + 1][j];
		beta[t][i] = sum;
		bsum += sum;
	    }
	    brnrm[t] = brnrm[t + 1];
	    if (bsum < BIGI) {
		++brnrm[t];
		for (j = 0; j < mstat; j++)
		    beta[t][j] *= BIG;
	    }
	}
	lhood = 0.;
	for (i = 0; i < mstat; i++)
	    lhood += alpha[0][i] * beta[0][i];
	lrnrm = arnrm[0] + brnrm[0];
	if (lhood != 0.)
	    while (lhood < BIGI) {
		lhood *= BIG;
		lrnrm++;
	    }
	for (t = 0; t < nobs; t++) {
	    sum = 0.;
	    for (i = 0; i < mstat; i++)
		sum += (pstate[t][i] = alpha[t][i] * beta[t][i]);
	    // sum = lhood*pow(BIGI, lrnrm - arnrm[t] - brnrm[t]);
	    for (i = 0; i < mstat; i++)
		pstate[t][i] /= sum;
	}
	fbdone = 1;
    }

    public void baumwelch() {
	int i, j, k, t;
	double num, denom, term;
	double[][] bnew = new double[mstat][ksym];
	double[] powtab = new double[10];
	for (i = 0; i < 10; i++)
	    powtab[i] = pow(BIGI, i - 6);
	if (fbdone != 1)
	    throw new IllegalArgumentException("must do forwardbackward first");
	for (i = 0; i < mstat; i++) {
	    denom = 0.;
	    for (k = 0; k < ksym; k++)
		bnew[i][k] = 0.;
	    for (t = 0; t < nobs - 1; t++) {
		term = (alpha[t][i] * beta[t][i] / lhood)
			* powtab[arnrm[t] + brnrm[t] - lrnrm + 6];
		denom += term;
		bnew[i][obs[t]] += term;
	    }
	    for (j = 0; j < mstat; j++) {
		num = 0.;
		for (t = 0; t < nobs - 1; t++) {
		    num += alpha[t][i] * b[j][obs[t + 1]] * beta[t + 1][j]
			    * powtab[arnrm[t] + brnrm[t + 1] - lrnrm + 6]
			    / lhood;
		}
		a[i][j] *= (num / denom);
	    }
	    for (k = 0; k < ksym; k++)
		bnew[i][k] /= denom;
	}
	b = bnew;
	fbdone = 0;
    }

    public static void markovgen(final double[][] atrans, final int[] out) {
	markovgen(atrans, out, 0, 1);
    }

    /**
     * Markov Models and Hidden Markov Modeling
     * 
     * @param atrans
     * @param out
     * @param istart
     * @param seed
     */
    public static void markovgen(final double[][] atrans, final int[] out,
	    final int istart, final int seed) {
	int i, ilo, ihi, ii, j, m = atrans.length, n = out.length;
	double[][] cum = buildMatrix(atrans);
	double r;
	Ran ran = new Ran(seed);
	if (m != atrans[0].length)
	    throw new IllegalArgumentException(
		    "transition matrix must be square");
	for (i = 0; i < m; i++) {
	    for (j = 1; j < m; j++)
		cum[i][j] += cum[i][j - 1];
	    if (abs(cum[i][m - 1] - 1.) > 0.01)
		throw new IllegalArgumentException(
			"transition matrix rows must sum to 1");
	}
	j = istart;
	out[0] = j;
	for (ii = 1; ii < n; ii++) {
	    r = ran.doub() * cum[j][m - 1];
	    ilo = 0;
	    ihi = m;
	    while (ihi - ilo > 1) {
		i = (ihi + ilo) >> 1;
		if (r > cum[j][i - 1])
		    ilo = i;
		else
		    ihi = i;
	    }
	    out[ii] = j = ilo;
	}
    }
}
