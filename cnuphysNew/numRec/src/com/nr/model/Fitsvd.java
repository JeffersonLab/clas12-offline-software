package com.nr.model;

import static com.nr.NRUtil.*;
import com.nr.RealMultiValueFun;
import com.nr.UniVarRealMultiValueFun;
import com.nr.la.SVD;

/**
 * general linear fit using SVD
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Fitsvd {
    int ndat, ma;
    final double tol;
    // double[] *x,&y,&sig;
    double[] x, y, sig;
    UniVarRealMultiValueFun funcs;
    public double[] a;
    public double[][] covar;
    public double chisq;

    double[][] xmd;
    RealMultiValueFun funcsmd;

    public Fitsvd(final double[] xx, final double[] yy, final double[] ssig,
	    final UniVarRealMultiValueFun funks) {
	this(xx, yy, ssig, funks, 1.e-12);
    }

    public Fitsvd(final double[] xx, final double[] yy, final double[] ssig,
	    final UniVarRealMultiValueFun funks, final double TOL) {
	ndat = yy.length;
	x = xx;
	xmd = null;
	y = yy;
	sig = ssig;
	funcs = funks;
	tol = TOL;
    }

    public void fit() {
	int i, j, k;
	double tmp, thresh, sum;
	if (x != null)
	    ma = funcs.funk(x[0]).length;
	else
	    ma = funcsmd.funk(row(xmd, 0)).length;
	a = new double[ma];
	covar = new double[ma][ma];
	double[][] aa = new double[ndat][ma];
	double[] b = new double[ndat], afunc = new double[ma];
	for (i = 0; i < ndat; i++) {
	    if (x != null)
		afunc = funcs.funk(x[i]);
	    else
		afunc = funcsmd.funk(row(xmd, i));
	    tmp = 1.0 / sig[i];
	    for (j = 0; j < ma; j++)
		aa[i][j] = afunc[j] * tmp;
	    b[i] = y[i] * tmp;
	}
	SVD svd = new SVD(aa);
	thresh = (tol > 0. ? tol * svd.w[0] : -1.);
	svd.solve(b, a, thresh);
	chisq = 0.0;
	for (i = 0; i < ndat; i++) {
	    sum = 0.;
	    for (j = 0; j < ma; j++)
		sum += aa[i][j] * a[j];
	    chisq += SQR(sum - b[i]);
	}
	for (i = 0; i < ma; i++) {
	    for (j = 0; j < i + 1; j++) {
		sum = 0.0;
		for (k = 0; k < ma; k++)
		    if (svd.w[k] > svd.tsh)
			sum += svd.v[i][k] * svd.v[j][k] / SQR(svd.w[k]);
		covar[j][i] = covar[i][j] = sum;
	    }
	}
    }

    public Fitsvd(final double[][] xx, final double[] yy, final double[] ssig,
	    final RealMultiValueFun funks) {
	this(xx, yy, ssig, funks, 1.e-12);
    }

    public Fitsvd(final double[][] xx, final double[] yy, final double[] ssig,
	    final RealMultiValueFun funks, final double TOL) {
	ndat = yy.length;
	x = null;
	xmd = xx;
	y = yy;
	sig = ssig;
	funcsmd = funks;
	tol = TOL;
    }

    public double[] row(final double[][] a, final int i) {
	int j, n = a[0].length;
	double[] ans = new double[n];
	for (j = 0; j < n; j++)
	    ans[j] = a[i][j];
	return ans;
    }
}
