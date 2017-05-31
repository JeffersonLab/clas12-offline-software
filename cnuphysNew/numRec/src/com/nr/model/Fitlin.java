package com.nr.model;

import static com.nr.NRUtil.*;
import static com.nr.la.GaussJordan.*;
import com.nr.UniVarRealMultiValueFun;

/**
 * General linear fit Copyright (C) Numerical Recipes Software 1986-2007 Java
 * translation Copyright (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Fitlin {
    int ndat, ma;
    public double[] x, y, sig;
    UniVarRealMultiValueFun funcs;
    public boolean[] ia;

    public double[] a;
    public double[][] covar;
    public double chisq;

    public Fitlin(final double[] xx, final double[] yy, final double[] ssig,
	    final UniVarRealMultiValueFun funcs) {
	ndat = xx.length;
	x = xx;
	y = yy;
	sig = ssig;
	this.funcs = funcs;
	ma = funcs.funk(x[0]).length;
	a = new double[ma];
	covar = new double[ma][ma];
	ia = new boolean[ma];

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
	int i, j, k, l, m, mfit = 0;
	double ym, wt, sum, sig2i;
	double[] afunc = new double[ma];
	for (j = 0; j < ma; j++)
	    if (ia[j])
		mfit++;
	if (mfit == 0)
	    throw new IllegalArgumentException(
		    "lfit: no parameters to be fitted");
	double[][] temp = new double[mfit][mfit], beta = new double[mfit][1];
	for (i = 0; i < ndat; i++) {
	    afunc = funcs.funk(x[i]);
	    ym = y[i];
	    if (mfit < ma) {
		for (j = 0; j < ma; j++)
		    if (!ia[j])
			ym -= a[j] * afunc[j];
	    }
	    sig2i = 1.0 / SQR(sig[i]);
	    for (j = 0, l = 0; l < ma; l++) {
		if (ia[l]) {
		    wt = afunc[l] * sig2i;
		    for (k = 0, m = 0; m <= l; m++)
			if (ia[m])
			    temp[j][k++] += wt * afunc[m];
		    beta[j++][0] += ym * wt;
		}
	    }
	}
	for (j = 1; j < mfit; j++)
	    for (k = 0; k < j; k++)
		temp[k][j] = temp[j][k];
	gaussj(temp, beta);
	for (j = 0, l = 0; l < ma; l++)
	    if (ia[l])
		a[l] = beta[j++][0];
	chisq = 0.0;
	for (i = 0; i < ndat; i++) {
	    afunc = funcs.funk(x[i]);
	    sum = 0.0;
	    for (j = 0; j < ma; j++)
		sum += a[j] * afunc[j];
	    chisq += SQR((y[i] - sum) / sig[i]);
	}
	for (j = 0; j < mfit; j++)
	    for (k = 0; k < mfit; k++)
		covar[j][k] = temp[j][k];
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
