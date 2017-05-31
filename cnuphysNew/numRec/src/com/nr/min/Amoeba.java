package com.nr.min;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;
import com.nr.RealValueFun;

/**
 * Downhill Simplex Method in Multidimensions
 * 
 * downhill simplex minimization Copyright (C) Numerical Recipes Software
 * 1986-2007 Java translation Copyright (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Amoeba {
    final double ftol;
    int nfunc;
    int mpts;
    int ndim;
    double fmin;
    double[] y;
    double[][] p;

    public Amoeba(final double ftoll) {
	this.ftol = ftoll;
    }

    public double[] minimize(final double[] point, final double del,
	    final RealValueFun func) {
	double[] dels = buildVector(point.length, del);
	return minimize(point, dels, func);
    }

    public double[] minimize(final double[] point, final double[] dels,
	    final RealValueFun func) {
	int ndim = point.length;
	double[][] pp = new double[ndim + 1][ndim];
	for (int i = 0; i < ndim + 1; i++) {
	    for (int j = 0; j < ndim; j++)
		pp[i][j] = point[j];
	    if (i != 0)
		pp[i][i - 1] += dels[i - 1];
	}
	return minimize(pp, func);
    }

    public double[] minimize(final double[][] pp, final RealValueFun func) {
	final int NMAX = 5000;
	final double TINY = 1.0e-10;
	int ihi, ilo, inhi;
	mpts = pp.length;
	ndim = pp[0].length;
	double[] psum = new double[ndim];
	double[] pmin = new double[ndim];
	double[] x = new double[ndim];
	p = new double[pp.length][pp[0].length];
	copyAssign(p, pp); // XXX p=pp, in NR "=" is overloading, see "nr3.h"
	// y = resize(y,mpts);
	y = new double[mpts];
	for (int i = 0; i < mpts; i++) {
	    for (int j = 0; j < ndim; j++)
		x[j] = p[i][j];
	    y[i] = func.funk(x);
	}
	nfunc = 0;
	get_psum(p, psum);
	for (;;) {
	    ilo = 0;

	    // XXX ihi = y[0]>y[1] ? (inhi=1,0) : (inhi=0,1);
	    ihi = 0;
	    if (y[0] > y[1]) {
		inhi = 1;
		ihi = 0;
	    } else {
		inhi = 0;
		ihi = 1;
	    }

	    for (int i = 0; i < mpts; i++) {
		if (y[i] <= y[ilo])
		    ilo = i;
		if (y[i] > y[ihi]) {
		    inhi = ihi;
		    ihi = i;
		} else if (y[i] > y[inhi] && i != ihi)
		    inhi = i;
	    }
	    double rtol = 2.0 * abs(y[ihi] - y[ilo])
		    / (abs(y[ihi]) + abs(y[ilo]) + TINY);
	    if (rtol < ftol) {
		swap(y, 0, ilo);
		for (int i = 0; i < ndim; i++) {
		    // SWAP(p[0][i],p[ilo][i]);
		    double dum = p[0][i];
		    p[0][i] = p[ilo][i];
		    p[ilo][i] = dum;
		    pmin[i] = p[0][i];
		}
		fmin = y[0];
		return pmin;
	    }
	    if (nfunc >= NMAX) {
		throw new Error("NMAX exceeded");
	    }
	    nfunc += 2;
	    double ytry = amotry(p, y, psum, ihi, -1.0, func);
	    if (ytry <= y[ilo])
		ytry = amotry(p, y, psum, ihi, 2.0, func);
	    else if (ytry >= y[inhi]) {
		double ysave = y[ihi];
		ytry = amotry(p, y, psum, ihi, 0.5, func);
		if (ytry >= ysave) {
		    for (int i = 0; i < mpts; i++) {
			if (i != ilo) {
			    for (int j = 0; j < ndim; j++)
				p[i][j] = psum[j] = 0.5 * (p[i][j] + p[ilo][j]);
			    y[i] = func.funk(psum);
			}
		    }
		    nfunc += ndim;
		    get_psum(p, psum);
		}
	    } else
		--nfunc;
	}
    }

    public void get_psum(final double[][] p, final double[] psum) {
	for (int j = 0; j < ndim; j++) {
	    double sum = 0.0;
	    for (int i = 0; i < mpts; i++)
		sum += p[i][j];
	    psum[j] = sum;
	}
    }

    public double amotry(final double[][] p, final double[] y,
	    final double[] psum, final int ihi, final double fac,
	    final RealValueFun func) {
	double[] ptry = new double[ndim];
	double fac1 = (1.0 - fac) / ndim;
	double fac2 = fac1 - fac;
	for (int j = 0; j < ndim; j++)
	    ptry[j] = psum[j] * fac1 - p[ihi][j] * fac2;
	double ytry = func.funk(ptry);
	if (ytry < y[ihi]) {
	    y[ihi] = ytry;
	    for (int j = 0; j < ndim; j++) {
		psum[j] += ptry[j] - p[ihi][j];
		p[ihi][j] = ptry[j];
	    }
	}
	return ytry;
    }
}
