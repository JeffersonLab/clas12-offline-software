package com.nr.min;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;
import static com.nr.root.Roots.*;

import org.netlib.util.*;

import com.nr.RealValueFunWithDiff;

public class QuasiNewton {
    /**
     * Quasi-Newton or Variable Metric Methods in Multidimensions
     * 
     * @param p
     * @param gtol
     * @param iter
     * @param fret
     * @param funcd
     */
    public static void dfpmin(final double[] p, final double gtol,
	    final intW iter, final doubleW fret,
	    final RealValueFunWithDiff funcd) {
	final int ITMAX = 200;
	final double EPS = DBL_EPSILON;
	final double TOLX = 4 * EPS, STPMX = 100.0;
	booleanW check = new booleanW(false);
	double den, fac, fad, fae, fp, stpmax, sum = 0.0, sumdg, sumxi, temp, test;
	int n = p.length;
	double[] dg = new double[n];
	double[] g = new double[n];
	double[] hdg = new double[n];
	double[] pnew = new double[n];
	double[] xi = new double[n];
	double[][] hessin = new double[n][n];
	fp = funcd.funk(p);
	funcd.df(p, g);
	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < n; j++)
		hessin[i][j] = 0.0;
	    hessin[i][i] = 1.0;
	    xi[i] = -g[i];
	    sum += p[i] * p[i];
	}
	stpmax = STPMX * max(sqrt(sum), n);
	for (int its = 0; its < ITMAX; its++) {
	    iter.val = its;
	    lnsrch(p, fp, g, xi, pnew, fret, stpmax, check, funcd);
	    fp = fret.val;
	    for (int i = 0; i < n; i++) {
		xi[i] = pnew[i] - p[i];
		p[i] = pnew[i];
	    }
	    test = 0.0;
	    for (int i = 0; i < n; i++) {
		temp = abs(xi[i]) / max(abs(p[i]), 1.0);
		if (temp > test)
		    test = temp;
	    }
	    if (test < TOLX)
		return;
	    for (int i = 0; i < n; i++)
		dg[i] = g[i];
	    funcd.df(p, g);
	    test = 0.0;
	    den = max(abs(fret.val), 1.0);
	    for (int i = 0; i < n; i++) {
		temp = abs(g[i]) * max(abs(p[i]), 1.0) / den;
		if (temp > test)
		    test = temp;
	    }
	    if (test < gtol)
		return;
	    for (int i = 0; i < n; i++)
		dg[i] = g[i] - dg[i];
	    for (int i = 0; i < n; i++) {
		hdg[i] = 0.0;
		for (int j = 0; j < n; j++)
		    hdg[i] += hessin[i][j] * dg[j];
	    }
	    fac = fae = sumdg = sumxi = 0.0;
	    for (int i = 0; i < n; i++) {
		fac += dg[i] * xi[i];
		fae += dg[i] * hdg[i];
		sumdg += SQR(dg[i]);
		sumxi += SQR(xi[i]);
	    }
	    if (fac > sqrt(EPS * sumdg * sumxi)) {
		fac = 1.0 / fac;
		fad = 1.0 / fae;
		for (int i = 0; i < n; i++)
		    dg[i] = fac * xi[i] - fad * hdg[i];
		for (int i = 0; i < n; i++) {
		    for (int j = i; j < n; j++) {
			hessin[i][j] += fac * xi[i] * xi[j] - fad * hdg[i]
				* hdg[j] + fae * dg[i] * dg[j];
			hessin[j][i] = hessin[i][j];
		    }
		}
	    }
	    for (int i = 0; i < n; i++) {
		xi[i] = 0.0;
		for (int j = 0; j < n; j++)
		    xi[i] -= hessin[i][j] * g[j];
	    }
	}
	throw new IllegalArgumentException("too many iterations in dfpmin");
    }
}
