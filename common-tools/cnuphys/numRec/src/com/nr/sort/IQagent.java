package com.nr.sort;

import static java.lang.Math.*;

import java.util.Arrays;

/**
 * Object for estimating arbitrary quantile values from a continuing stream of
 * data values.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class IQagent {
    static final int nbuf = 1000;
    int nq, nt, nd;
    double[] pval, dbuf, qile;
    double q0, qm;

    public IQagent() {
	nq = 251;
	nt = 0;
	nd = 0;
	pval = new double[nq];
	dbuf = new double[nbuf];
	qile = new double[nq];
	q0 = 1.e99;
	qm = -1.e99;

	for (int j = 85; j <= 165; j++)
	    pval[j] = (j - 75.) / 100.;
	/*
	 * Set general purpose array of p-values ranging from 1.0e-6 to
	 * 1~1.0e-6. You can change this if you want.
	 */
	for (int j = 84; j >= 0; j--) {
	    pval[j] = 0.87191909 * pval[j + 1];
	    pval[250 - j] = 1. - pval[j];
	}
    }

    /**
     * Assimilate a new value from the stream.
     * 
     * @param datum
     */
    public void add(final double datum) {
	dbuf[nd++] = datum;
	if (datum < q0) {
	    q0 = datum;
	}
	if (datum > qm) {
	    qm = datum;
	}
	if (nd == nbuf)
	    update();
    }

    /**
     * Batch update.
     * 
     * This function is called by add or report and should not be called
     * directly by the user.
     */
    private void update() {
	int jd = 0, jq = 1, iq;
	double target, told = 0., tnew = 0., qold, qnew;
	double[] newqile = new double[nq];
	Arrays.sort(dbuf, 0, nd);
	qold = qnew = qile[0] = newqile[0] = q0;
	qile[nq - 1] = newqile[nq - 1] = qm;
	pval[0] = min(0.5 / (nt + nd), 0.5 * pval[1]);
	pval[nq - 1] = max(1. - 0.5 / (nt + nd), 0.5 * (1. + pval[nq - 2]));
	for (iq = 1; iq < nq - 1; iq++) {
	    target = (nt + nd) * pval[iq];
	    if (tnew < target)
		for (;;) {
		    if (jq < nq && (jd >= nd || qile[jq] < dbuf[jd])) {
			qnew = qile[jq];
			tnew = jd + nt * pval[jq++];
			if (tnew >= target)
			    break;
		    } else {
			qnew = dbuf[jd];
			tnew = told;
			if (qile[jq] > qile[jq - 1])
			    tnew += nt * (pval[jq] - pval[jq - 1])
				    * (qnew - qold) / (qile[jq] - qile[jq - 1]);
			jd++;
			if (tnew >= target)
			    break;
			told = tnew++;
			qold = qnew;
			if (tnew >= target)
			    break;
		    }
		    told = tnew;
		    qold = qnew;
		}
	    if (tnew == told)
		newqile[iq] = 0.5 * (qold + qnew);
	    else
		newqile[iq] = qold + (qnew - qold) * (target - told)
			/ (tnew - told);
	    told = tnew;
	    qold = qnew;
	}
	qile = newqile;
	nt += nd;
	nd = 0;
    }

    /**
     * Return estimated p-quantile for the data seen so far. (E.g., p D 0:5 for
     * median.)
     * 
     * @param p
     * @return
     */
    public double report(final double p) {
	double q;
	if (nd > 0)
	    update();
	int jl = 0, jh = nq - 1, j;
	while (jh - jl > 1) {
	    j = (jh + jl) >> 1;
	    if (p > pval[j])
		jl = j;
	    else
		jh = j;
	}
	j = jl;
	q = qile[j] + (qile[j + 1] - qile[j]) * (p - pval[j])
		/ (pval[j + 1] - pval[j]);
	return max(qile[0], min(qile[nq - 1], q));
    }
}
