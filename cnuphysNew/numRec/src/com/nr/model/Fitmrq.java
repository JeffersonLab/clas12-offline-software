package com.nr.model;

import static com.nr.NRUtil.*;
import static com.nr.la.GaussJordan.*;
import static java.lang.Math.*;

import org.netlib.util.doubleW;

/**
 * Levenberg-Marquardt nonlinear fitting Copyright (C) Numerical Recipes
 * Software 1986-2007 Java translation Copyright (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Fitmrq {
    static final int NDONE = 4, ITMAX = 1000;
    int ndat, ma, mfit;
    double[] x, y, sig;
    final double tol;
    MultiFuncd funcs;
    public boolean[] ia;
    public double[] a;
    public double[][] covar;
    public double[][] alpha;
    public double chisq;

    public Fitmrq(final double[] xx, final double[] yy, final double[] ssig,
	    final double[] aa, final MultiFuncd funks) {
	this(xx, yy, ssig, aa, funks, 1.e-3);
    }

    public Fitmrq(final double[] xx, final double[] yy, final double[] ssig,
	    final double[] aa, final MultiFuncd funks, final double TOL) {
	ndat = xx.length;
	ma = aa.length;
	x = xx;
	y = yy;
	sig = ssig;
	tol = TOL;
	funcs = funks;
	ia = new boolean[ma];
	alpha = new double[ma][ma];
	a = buildVector(aa);
	covar = new double[ma][ma];
	for (int i = 0; i < ma; i++)
	    ia[i] = true;
    }

    public void hold(final int i, final double val) {
	ia[i] = false;
	a[i] = val;
    }

    public void free(final int i) {
	ia[i] = true;
    }

    public void fit() {
	int j, k, l, iter, done = 0;
	double alamda = .001, ochisq;
	double[] atry = new double[ma];
	double[] beta = new double[ma];
	double[] da = new double[ma];
	;
	mfit = 0;
	for (j = 0; j < ma; j++)
	    if (ia[j])
		mfit++;
	double[][] oneda = new double[mfit][1], temp = new double[mfit][mfit];
	mrqcof(a, alpha, beta);
	for (j = 0; j < ma; j++)
	    atry[j] = a[j];
	ochisq = chisq;
	for (iter = 0; iter < ITMAX; iter++) {
	    if (done == NDONE)
		alamda = 0.;
	    for (j = 0; j < mfit; j++) {
		for (k = 0; k < mfit; k++)
		    covar[j][k] = alpha[j][k];
		covar[j][j] = alpha[j][j] * (1.0 + alamda);
		for (k = 0; k < mfit; k++)
		    temp[j][k] = covar[j][k];
		oneda[j][0] = beta[j];
	    }
	    gaussj(temp, oneda);
	    for (j = 0; j < mfit; j++) {
		for (k = 0; k < mfit; k++)
		    covar[j][k] = temp[j][k];
		da[j] = oneda[j][0];
	    }
	    if (done == NDONE) {
		covsrt(covar);
		covsrt(alpha);
		return;
	    }
	    for (j = 0, l = 0; l < ma; l++)
		if (ia[l])
		    atry[l] = a[l] + da[j++];
	    mrqcof(atry, covar, da);
	    if (abs(chisq - ochisq) < max(tol, tol * chisq))
		done++;
	    if (chisq < ochisq) {
		alamda *= 0.1;
		ochisq = chisq;
		for (j = 0; j < mfit; j++) {
		    for (k = 0; k < mfit; k++)
			alpha[j][k] = covar[j][k];
		    beta[j] = da[j];
		}
		for (l = 0; l < ma; l++)
		    a[l] = atry[l];
	    } else {
		alamda *= 10.0;
		chisq = ochisq;
	    }
	}
	throw new IllegalArgumentException("Fitmrq too many iterations");
    }

    public void mrqcof(final double[] a, final double[][] alpha,
	    final double[] beta) {
	int i, j, k, l, m;
	double ymod, wt, sig2i, dy;
	doubleW ymodW = new doubleW(0);
	double[] dyda = new double[ma];
	for (j = 0; j < mfit; j++) {
	    for (k = 0; k <= j; k++)
		alpha[j][k] = 0.0;
	    beta[j] = 0.;
	}
	chisq = 0.;
	for (i = 0; i < ndat; i++) {
	    funcs.funk(x[i], a, ymodW, dyda);
	    ymod = ymodW.val;
	    sig2i = 1.0 / (sig[i] * sig[i]);
	    dy = y[i] - ymod;
	    for (j = 0, l = 0; l < ma; l++) {
		if (ia[l]) {
		    wt = dyda[l] * sig2i;
		    for (k = 0, m = 0; m < l + 1; m++)
			if (ia[m])
			    alpha[j][k++] += wt * dyda[m];
		    beta[j++] += dy * wt;
		}
	    }
	    chisq += dy * dy * sig2i;
	}
	for (j = 1; j < mfit; j++)
	    for (k = 0; k < j; k++)
		alpha[k][j] = alpha[j][k];
    }

    public void covsrt(final double[][] covar) {
	int i, j, k;
	for (i = mfit; i < ma; i++)
	    for (j = 0; j < i + 1; j++)
		covar[i][j] = covar[j][i] = 0.0;
	k = mfit - 1;
	for (j = ma - 1; j >= 0; j--) {
	    if (ia[j]) {
		for (i = 0; i < ma; i++) {
		    // SWAP(covar[i][k],covar[i][j]);
		    double swap = covar[i][k];
		    covar[i][k] = covar[i][j];
		    covar[i][j] = swap;
		}
		for (i = 0; i < ma; i++) {
		    // SWAP(covar[k][i],covar[j][i]);
		    double swap = covar[k][i];
		    covar[k][i] = covar[j][i];
		    covar[j][i] = swap;
		}
		k--;
	    }
	}
    }

}
