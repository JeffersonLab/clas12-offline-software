package com.nr.la;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;

import org.netlib.util.doubleW;
import org.netlib.util.intW;

/**
 * Abstract base class for solving sparse linear equations by the preconditioned
 * biconjugate gradient method. To use, declare a derived class in which the
 * methods atimes and asolve are defined for your problem, along with any data
 * that they need. Then call the solve method.
 * 
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public abstract class Linbcg {
    public abstract void asolve(double[] b, double[] x, final int itrnsp);

    public abstract void atimes(double[] x, double[] r, final int itrnsp);

    public void solve(final double[] b, final double[] x, final int itol,
	    final double tol, final int itmax, final intW iter,
	    final doubleW err) {
	double ak, akden, bk, bkden = 1.0, bknum, bnrm, dxnrm, xnrm, zm1nrm, znrm = 0;
	final double EPS = 1.0e-14;
	int j, n = b.length;
	double[] p = new double[n];
	double[] pp = new double[n];
	double[] r = new double[n];
	double[] rr = new double[n];
	double[] z = new double[n];
	double[] zz = new double[n];
	iter.val = 0;
	atimes(x, r, 0);
	for (j = 0; j < n; j++) {
	    r[j] = b[j] - r[j];
	    rr[j] = r[j];
	}
	// atimes(r,rr,0);
	if (itol == 1) {
	    bnrm = snrm(b, itol);
	    asolve(r, z, 0);
	} else if (itol == 2) {
	    asolve(b, z, 0);
	    bnrm = snrm(z, itol);
	    asolve(r, z, 0);
	} else if (itol == 3 || itol == 4) {
	    asolve(b, z, 0);
	    bnrm = snrm(z, itol);
	    asolve(r, z, 0);
	    znrm = snrm(z, itol);
	} else
	    throw new IllegalArgumentException("illegal itol in linbcg");
	while (iter.val < itmax) {
	    ++iter.val;
	    asolve(rr, zz, 1);
	    for (bknum = 0.0, j = 0; j < n; j++)
		bknum += z[j] * rr[j];
	    if (iter.val == 1) {
		for (j = 0; j < n; j++) {
		    p[j] = z[j];
		    pp[j] = zz[j];
		}
	    } else {
		bk = bknum / bkden;
		for (j = 0; j < n; j++) {
		    p[j] = bk * p[j] + z[j];
		    pp[j] = bk * pp[j] + zz[j];
		}
	    }
	    bkden = bknum;
	    atimes(p, z, 0);
	    for (akden = 0.0, j = 0; j < n; j++)
		akden += z[j] * pp[j];
	    ak = bknum / akden;
	    atimes(pp, zz, 1);
	    for (j = 0; j < n; j++) {
		x[j] += ak * p[j];
		r[j] -= ak * z[j];
		rr[j] -= ak * zz[j];
	    }
	    asolve(r, z, 0);
	    if (itol == 1)
		err.val = snrm(r, itol) / bnrm;
	    else if (itol == 2)
		err.val = snrm(z, itol) / bnrm;
	    else if (itol == 3 || itol == 4) {
		zm1nrm = znrm;
		znrm = snrm(z, itol);
		if (abs(zm1nrm - znrm) > EPS * znrm) {
		    dxnrm = abs(ak) * snrm(p, itol);
		    err.val = znrm / abs(zm1nrm - znrm) * dxnrm;
		} else {
		    err.val = znrm / bnrm;
		    continue;
		}
		xnrm = snrm(x, itol);
		if (err.val <= 0.5 * xnrm)
		    err.val /= xnrm;
		else {
		    err.val = znrm / bnrm;
		    continue;
		}
	    }
	    if (err.val <= tol)
		break;
	}
    }

    /**
     * Compute one of two norms for a vector sx[0..n-1], as signaled by itol.
     * Used by solve.
     * 
     * @param sx
     * @param itol
     * @return
     */
    public double snrm(final double[] sx, final int itol) {
	int i, isamax, n = sx.length;
	double ans;
	if (itol <= 3) {
	    ans = 0.0;
	    for (i = 0; i < n; i++)
		ans += SQR(sx[i]);
	    return sqrt(ans);
	} else {
	    isamax = 0;
	    for (i = 0; i < n; i++) {
		if (abs(sx[i]) > abs(sx[isamax]))
		    isamax = i;
	    }
	    return abs(sx[isamax]);
	}
    }

}
